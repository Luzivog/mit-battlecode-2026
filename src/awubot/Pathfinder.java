package awubot;

import battlecode.common.*;
import static awubot.util.Util.*;
import awubot.util.Rng;
import awubot.util.Profiler;
import static awubot.Game.*;


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
        pos = rc.getLocation();
        nextPathIndex = 0;
        stackSize = 0;
        emptyStackTurns = 1000;
        bottomDir = topDir = pos.directionTo(target);
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
        if(!rc.getLocation().equals(lastLoc) || !rc.getDirection().equals(lastDir)) {
            // we moved, so we might be able to advance previously stuck stuff further
            lastLoc = rc.getLocation();
            lastDir = rc.getDirection();
            leftDone = leftBug.hitEdge || leftBug.nextPathIndex == 24;
            rightDone = rightBug.hitEdge || leftBug.nextPathIndex == 24;
        }

        if(!(leftDone && rightDone)) {
            if(rightDone) {
                            leftBug.outOfSight = false;
            leftDone = !leftBug.advance();
            rc.setIndicatorDot(leftBug.pos, 255, 0, 255);

            } else if(leftDone) {
                            rightBug.outOfSight = false;
            rightDone = !rightBug.advance();
            rc.setIndicatorDot(rightBug.pos, 255, 0, 255);

            } else if(leftBug.nextPathIndex > rightBug.nextPathIndex) {
                            rightBug.outOfSight = false;
            rightDone = !rightBug.advance();
            rc.setIndicatorDot(rightBug.pos, 255, 0, 255);

            } else {
                            leftBug.outOfSight = false;
            leftDone = !leftBug.advance();
            rc.setIndicatorDot(leftBug.pos, 255, 0, 255);

            }
        }
        // Profiler.end("lookahead");

        boolean leftReady = leftBug.nextPathIndex >= 5 || leftBug.hitEdge || leftBug.outOfSight || leftBug.arrived;
        boolean rightReady = rightBug.nextPathIndex >= 5 || rightBug.hitEdge || rightBug.outOfSight || rightBug.arrived;
        indicate("ready " + leftReady + " " + rightReady);
        if(!leftReady || !rightReady) return !(leftDone && rightDone);

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

            } else if(!better.outOfSight || rc.getLocation().isAdjacentTo(pos)) {
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
        indicate("leftTurn " + leftTurn);

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
                            if(greedyPassable && chebyshevDistance(rc.getLocation(), greedyLoc) <= 5) {
                indicate("greedyLoc " + greedyLoc);
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
            var leftGood = (leftPassable || !CellTracker.isSeen(leftLoc)) && rc.onTheMap(leftLoc);
            var rightPassable = CellTracker.isPassable(rightLoc);
            var rightGood = (rightPassable || !CellTracker.isSeen(rightLoc)) && rc.onTheMap(rightLoc);
            if(leftGood && !rightGood) {
                            if(leftPassable && chebyshevDistance(rc.getLocation(), leftLoc) <= 5) {
                indicate("leftLoc " + leftLoc);
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
                            if(rightPassable && chebyshevDistance(rc.getLocation(), rightLoc) <= 5) {
                indicate("rightLoc " + rightLoc);
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
                                if(leftPassable && chebyshevDistance(rc.getLocation(), leftLoc) <= 5) {
                indicate("leftLoc " + leftLoc);
                pos = leftLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            return false;

                } else {
                                if(rightPassable && chebyshevDistance(rc.getLocation(), rightLoc) <= 5) {
                indicate("rightLoc " + rightLoc);
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) {
                        rc.setIndicatorDot(nextPos, 0, 255, 255);
                        outOfSight = true;
                    } else if(isMaster) {
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

    public static void turn(Direction dir) throws GameActionException {
        rc.turn(dir);
        CellTracker.storeVisibleCells();
    }

    public static void moveForward() throws GameActionException {
        rc.moveForward();
        CellTracker.storeVisibleCellsPostMove();
    }

    public static void makeMove(Direction dir) throws GameActionException {
        if(dir == Direction.CENTER) return;
        if(dir != rc.getDirection()) turn(dir);
        moveForward();
    }

    public static void makeMove(MapLocation target) throws GameActionException {
        // TODO: better handling when target changes
        if(bug == null || !target.equals(bug.target)) {
            indicate("RETARGET");
            bug = new BugnavInstance(true, target);
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
        rc.setIndicatorDot(bug.pos, 255, 0, 0);

        if(!rc.isMovementReady()) return;

        // no bfs needed
        var dir = rc.getLocation().directionTo(bug.pos);
        if(rc.getLocation().add(dir).equals(bug.pos)) {
            indicate("adjacent");
            makeMove(dir);
            return;
        }
        // TODO: what if location == bug.pos?

        // bfs
        Profiler.begin();
        CellTracker.computeLocalPassable();
        var loc = rc.getLocation();
        // d{i}0 + d{i}1 is the mask of reachable within i steps
        long d00 = 0, d01 = 0;
                {
            int xi = bug.pos.x - loc.x + 5;
            int yi = bug.pos.y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d00 |= 1l << idx;
                } else {
                    d01 |= 1l << (idx - 61);
                }
            }
        }

        Direction bestMove = null;
        double bestScore = 1e18;
        long d10 = 0, d11 = 0;
        d10 = d00;
        d11 = d01;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(1).x - loc.x + 5;
            int yi = bug.getRecentPos(1).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d10 |= 1l << idx;
                } else {
                    d11 |= 1l << (idx - 61);
                }
            }
        }

        d10 &= CellTracker.localPassable0;
        d11 &= CellTracker.localPassable1;
        if((d10 & 0x807000000000000l) != 0 || (d11 & 0xe01) != 0) {
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

            if(
               (d10 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d11 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d11 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d10 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d10 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d10 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d11 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d11 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 1);
                return;
            }
        }
        long d20 = 0, d21 = 0;
        d20 = d10;
        d21 = d11;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(2).x - loc.x + 5;
            int yi = bug.getRecentPos(2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d20 |= 1l << idx;
                } else {
                    d21 |= 1l << (idx - 61);
                }
            }
        }

        d20 &= CellTracker.localPassable0;
        d21 &= CellTracker.localPassable1;
        if((d20 & 0x807000000000000l) != 0 || (d21 & 0xe01) != 0) {
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

            if(
               (d20 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d21 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d21 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d20 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d20 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d20 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d21 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d21 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 2);
                return;
            }
        }
        long d30 = 0, d31 = 0;
        d30 = d20;
        d31 = d21;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(3).x - loc.x + 5;
            int yi = bug.getRecentPos(3).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d30 |= 1l << idx;
                } else {
                    d31 |= 1l << (idx - 61);
                }
            }
        }

        d30 &= CellTracker.localPassable0;
        d31 &= CellTracker.localPassable1;
        if((d30 & 0x807000000000000l) != 0 || (d31 & 0xe01) != 0) {
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

            if(
               (d30 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d31 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d31 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d30 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d30 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d30 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d31 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d31 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 3);
                return;
            }
        }
        long d40 = 0, d41 = 0;
        d40 = d30;
        d41 = d31;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(4).x - loc.x + 5;
            int yi = bug.getRecentPos(4).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d40 |= 1l << idx;
                } else {
                    d41 |= 1l << (idx - 61);
                }
            }
        }

        d40 &= CellTracker.localPassable0;
        d41 &= CellTracker.localPassable1;
        if((d40 & 0x807000000000000l) != 0 || (d41 & 0xe01) != 0) {
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

            if(
               (d40 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d41 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d41 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d40 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d40 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d40 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d41 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d41 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 4);
                return;
            }
        }
        long d50 = 0, d51 = 0;
        d50 = d40;
        d51 = d41;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(5).x - loc.x + 5;
            int yi = bug.getRecentPos(5).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d50 |= 1l << idx;
                } else {
                    d51 |= 1l << (idx - 61);
                }
            }
        }

        d50 &= CellTracker.localPassable0;
        d51 &= CellTracker.localPassable1;
        if((d50 & 0x807000000000000l) != 0 || (d51 & 0xe01) != 0) {
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

            if(
               (d50 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d51 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d51 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d50 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d50 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d50 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d51 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d51 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 5);
                return;
            }
        }
        long d60 = 0, d61 = 0;
        d60 = d50;
        d61 = d51;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(6).x - loc.x + 5;
            int yi = bug.getRecentPos(6).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d60 |= 1l << idx;
                } else {
                    d61 |= 1l << (idx - 61);
                }
            }
        }

        d60 &= CellTracker.localPassable0;
        d61 &= CellTracker.localPassable1;
        if((d60 & 0x807000000000000l) != 0 || (d61 & 0xe01) != 0) {
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

            if(
               (d60 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d61 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d61 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d60 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d60 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d60 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d61 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d61 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 6);
                return;
            }
        }
        long d70 = 0, d71 = 0;
        d70 = d60;
        d71 = d61;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(7).x - loc.x + 5;
            int yi = bug.getRecentPos(7).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d70 |= 1l << idx;
                } else {
                    d71 |= 1l << (idx - 61);
                }
            }
        }

        d70 &= CellTracker.localPassable0;
        d71 &= CellTracker.localPassable1;
        if((d70 & 0x807000000000000l) != 0 || (d71 & 0xe01) != 0) {
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

            if(
               (d70 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d71 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d71 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d70 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d70 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d70 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d71 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d71 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 7);
                return;
            }
        }
        long d80 = 0, d81 = 0;
        d80 = d70;
        d81 = d71;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(8).x - loc.x + 5;
            int yi = bug.getRecentPos(8).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d80 |= 1l << idx;
                } else {
                    d81 |= 1l << (idx - 61);
                }
            }
        }

        d80 &= CellTracker.localPassable0;
        d81 &= CellTracker.localPassable1;
        if((d80 & 0x807000000000000l) != 0 || (d81 & 0xe01) != 0) {
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

            if(
               (d80 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d81 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d81 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d80 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d80 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d80 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d81 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d81 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 8);
                return;
            }
        }
        long d90 = 0, d91 = 0;
        d90 = d80;
        d91 = d81;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(9).x - loc.x + 5;
            int yi = bug.getRecentPos(9).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d90 |= 1l << idx;
                } else {
                    d91 |= 1l << (idx - 61);
                }
            }
        }

        d90 &= CellTracker.localPassable0;
        d91 &= CellTracker.localPassable1;
        if((d90 & 0x807000000000000l) != 0 || (d91 & 0xe01) != 0) {
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

            if(
               (d90 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d91 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d91 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d90 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d90 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d90 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d91 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d91 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 9);
                return;
            }
        }
        long d100 = 0, d101 = 0;
        d100 = d90;
        d101 = d91;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(10).x - loc.x + 5;
            int yi = bug.getRecentPos(10).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d100 |= 1l << idx;
                } else {
                    d101 |= 1l << (idx - 61);
                }
            }
        }

        d100 &= CellTracker.localPassable0;
        d101 &= CellTracker.localPassable1;
        if((d100 & 0x807000000000000l) != 0 || (d101 & 0xe01) != 0) {
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

            if(
               (d100 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d101 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d101 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d100 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d100 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d100 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d101 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d101 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 10);
                return;
            }
        }
        long d110 = 0, d111 = 0;
        d110 = d100;
        d111 = d101;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(11).x - loc.x + 5;
            int yi = bug.getRecentPos(11).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d110 |= 1l << idx;
                } else {
                    d111 |= 1l << (idx - 61);
                }
            }
        }

        d110 &= CellTracker.localPassable0;
        d111 &= CellTracker.localPassable1;
        if((d110 & 0x807000000000000l) != 0 || (d111 & 0xe01) != 0) {
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

            if(
               (d110 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d111 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d111 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d110 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d110 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d110 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d111 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d111 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 11);
                return;
            }
        }
        long d120 = 0, d121 = 0;
        d120 = d110;
        d121 = d111;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(12).x - loc.x + 5;
            int yi = bug.getRecentPos(12).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d120 |= 1l << idx;
                } else {
                    d121 |= 1l << (idx - 61);
                }
            }
        }

        d120 &= CellTracker.localPassable0;
        d121 &= CellTracker.localPassable1;
        if((d120 & 0x807000000000000l) != 0 || (d121 & 0xe01) != 0) {
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

            if(
               (d120 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d121 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d121 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d120 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d120 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d120 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d121 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d121 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 12);
                return;
            }
        }
        long d130 = 0, d131 = 0;
        d130 = d120;
        d131 = d121;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(13).x - loc.x + 5;
            int yi = bug.getRecentPos(13).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d130 |= 1l << idx;
                } else {
                    d131 |= 1l << (idx - 61);
                }
            }
        }

        d130 &= CellTracker.localPassable0;
        d131 &= CellTracker.localPassable1;
        if((d130 & 0x807000000000000l) != 0 || (d131 & 0xe01) != 0) {
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

            if(
               (d130 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d131 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d131 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d130 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d130 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d130 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d131 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d131 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 13);
                return;
            }
        }
        long d140 = 0, d141 = 0;
        d140 = d130;
        d141 = d131;

        // if(rc.getRoundNum() >= 507) {
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
                {
            int xi = bug.getRecentPos(14).x - loc.x + 5;
            int yi = bug.getRecentPos(14).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d140 |= 1l << idx;
                } else {
                    d141 |= 1l << (idx - 61);
                }
            }
        }

        d140 &= CellTracker.localPassable0;
        d141 &= CellTracker.localPassable1;
        if((d140 & 0x807000000000000l) != 0 || (d141 & 0xe01) != 0) {
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

            if(
               (d140 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d141 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d141 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d140 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d140 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d140 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d141 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d141 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 14);
                return;
            }
        }
        long d150 = 0, d151 = 0;
        d150 = d140;
        d151 = d141;

        // if(rc.getRoundNum() >= 507) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d150, d151);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d150 |= (d150 & 0xfbff7feffdffbffl) << 1;
        d151 |= (d151 & 0x7feffdffbff7fefl) << 1;
        d150 |= (d150 & 0x1f7feffdffbff7fel) >> 1;
        d151 |= (d151 & 0xffdffbff7feffdel) >> 1;

        d151 |= ((d151 & 0x1ffffffffffffl) << 11) | (d150 >> 50);
        d150 |= (d150 & 0x3ffffffffffffl) << 11;
        d150 |= (d150 >> 11) | ((d151 & 0x7ffl) << 50);
        d151 |= d151 >> 11;
                {
            int xi = bug.getRecentPos(15).x - loc.x + 5;
            int yi = bug.getRecentPos(15).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d150 |= 1l << idx;
                } else {
                    d151 |= 1l << (idx - 61);
                }
            }
        }

        d150 &= CellTracker.localPassable0;
        d151 &= CellTracker.localPassable1;
        if((d150 & 0x807000000000000l) != 0 || (d151 & 0xe01) != 0) {
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

            if(
               (d150 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d151 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d151 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d150 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d150 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d150 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d151 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d151 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 15);
                return;
            }
        }
        long d160 = 0, d161 = 0;
        d160 = d150;
        d161 = d151;

        // if(rc.getRoundNum() >= 507) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d160, d161);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d160 |= (d160 & 0xfbff7feffdffbffl) << 1;
        d161 |= (d161 & 0x7feffdffbff7fefl) << 1;
        d160 |= (d160 & 0x1f7feffdffbff7fel) >> 1;
        d161 |= (d161 & 0xffdffbff7feffdel) >> 1;

        d161 |= ((d161 & 0x1ffffffffffffl) << 11) | (d160 >> 50);
        d160 |= (d160 & 0x3ffffffffffffl) << 11;
        d160 |= (d160 >> 11) | ((d161 & 0x7ffl) << 50);
        d161 |= d161 >> 11;
                {
            int xi = bug.getRecentPos(16).x - loc.x + 5;
            int yi = bug.getRecentPos(16).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d160 |= 1l << idx;
                } else {
                    d161 |= 1l << (idx - 61);
                }
            }
        }

        d160 &= CellTracker.localPassable0;
        d161 &= CellTracker.localPassable1;
        if((d160 & 0x807000000000000l) != 0 || (d161 & 0xe01) != 0) {
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

            if(
               (d160 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d161 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d161 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d160 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d160 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d160 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d161 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d161 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 16);
                return;
            }
        }
        long d170 = 0, d171 = 0;
        d170 = d160;
        d171 = d161;

        // if(rc.getRoundNum() >= 507) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d170, d171);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d170 |= (d170 & 0xfbff7feffdffbffl) << 1;
        d171 |= (d171 & 0x7feffdffbff7fefl) << 1;
        d170 |= (d170 & 0x1f7feffdffbff7fel) >> 1;
        d171 |= (d171 & 0xffdffbff7feffdel) >> 1;

        d171 |= ((d171 & 0x1ffffffffffffl) << 11) | (d170 >> 50);
        d170 |= (d170 & 0x3ffffffffffffl) << 11;
        d170 |= (d170 >> 11) | ((d171 & 0x7ffl) << 50);
        d171 |= d171 >> 11;
                {
            int xi = bug.getRecentPos(17).x - loc.x + 5;
            int yi = bug.getRecentPos(17).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d170 |= 1l << idx;
                } else {
                    d171 |= 1l << (idx - 61);
                }
            }
        }

        d170 &= CellTracker.localPassable0;
        d171 &= CellTracker.localPassable1;
        if((d170 & 0x807000000000000l) != 0 || (d171 & 0xe01) != 0) {
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

            if(
               (d170 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d171 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d171 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d170 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d170 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d170 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d171 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d171 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 17);
                return;
            }
        }
        long d180 = 0, d181 = 0;
        d180 = d170;
        d181 = d171;

        // if(rc.getRoundNum() >= 507) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d180, d181);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d180 |= (d180 & 0xfbff7feffdffbffl) << 1;
        d181 |= (d181 & 0x7feffdffbff7fefl) << 1;
        d180 |= (d180 & 0x1f7feffdffbff7fel) >> 1;
        d181 |= (d181 & 0xffdffbff7feffdel) >> 1;

        d181 |= ((d181 & 0x1ffffffffffffl) << 11) | (d180 >> 50);
        d180 |= (d180 & 0x3ffffffffffffl) << 11;
        d180 |= (d180 >> 11) | ((d181 & 0x7ffl) << 50);
        d181 |= d181 >> 11;
                {
            int xi = bug.getRecentPos(18).x - loc.x + 5;
            int yi = bug.getRecentPos(18).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d180 |= 1l << idx;
                } else {
                    d181 |= 1l << (idx - 61);
                }
            }
        }

        d180 &= CellTracker.localPassable0;
        d181 &= CellTracker.localPassable1;
        if((d180 & 0x807000000000000l) != 0 || (d181 & 0xe01) != 0) {
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

            if(
               (d180 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d181 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d181 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d180 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d180 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d180 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d181 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d181 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 18);
                return;
            }
        }
        long d190 = 0, d191 = 0;
        d190 = d180;
        d191 = d181;

        // if(rc.getRoundNum() >= 507) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d190, d191);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d190 |= (d190 & 0xfbff7feffdffbffl) << 1;
        d191 |= (d191 & 0x7feffdffbff7fefl) << 1;
        d190 |= (d190 & 0x1f7feffdffbff7fel) >> 1;
        d191 |= (d191 & 0xffdffbff7feffdel) >> 1;

        d191 |= ((d191 & 0x1ffffffffffffl) << 11) | (d190 >> 50);
        d190 |= (d190 & 0x3ffffffffffffl) << 11;
        d190 |= (d190 >> 11) | ((d191 & 0x7ffl) << 50);
        d191 |= d191 >> 11;
                {
            int xi = bug.getRecentPos(19).x - loc.x + 5;
            int yi = bug.getRecentPos(19).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d190 |= 1l << idx;
                } else {
                    d191 |= 1l << (idx - 61);
                }
            }
        }

        d190 &= CellTracker.localPassable0;
        d191 &= CellTracker.localPassable1;
        if((d190 & 0x807000000000000l) != 0 || (d191 & 0xe01) != 0) {
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

            if(
               (d190 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d191 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d191 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d190 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d190 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d190 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d191 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d191 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 19);
                return;
            }
        }
        long d200 = 0, d201 = 0;
        d200 = d190;
        d201 = d191;

        // if(rc.getRoundNum() >= 507) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d200, d201);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d200 |= (d200 & 0xfbff7feffdffbffl) << 1;
        d201 |= (d201 & 0x7feffdffbff7fefl) << 1;
        d200 |= (d200 & 0x1f7feffdffbff7fel) >> 1;
        d201 |= (d201 & 0xffdffbff7feffdel) >> 1;

        d201 |= ((d201 & 0x1ffffffffffffl) << 11) | (d200 >> 50);
        d200 |= (d200 & 0x3ffffffffffffl) << 11;
        d200 |= (d200 >> 11) | ((d201 & 0x7ffl) << 50);
        d201 |= d201 >> 11;
                {
            int xi = bug.getRecentPos(20).x - loc.x + 5;
            int yi = bug.getRecentPos(20).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d200 |= 1l << idx;
                } else {
                    d201 |= 1l << (idx - 61);
                }
            }
        }

        d200 &= CellTracker.localPassable0;
        d201 &= CellTracker.localPassable1;
        if((d200 & 0x807000000000000l) != 0 || (d201 & 0xe01) != 0) {
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

            if(
               (d200 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d201 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d201 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d200 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d200 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d200 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d201 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d201 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 20);
                return;
            }
        }
        long d210 = 0, d211 = 0;
        d210 = d200;
        d211 = d201;

        // if(rc.getRoundNum() >= 507) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d210, d211);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d210 |= (d210 & 0xfbff7feffdffbffl) << 1;
        d211 |= (d211 & 0x7feffdffbff7fefl) << 1;
        d210 |= (d210 & 0x1f7feffdffbff7fel) >> 1;
        d211 |= (d211 & 0xffdffbff7feffdel) >> 1;

        d211 |= ((d211 & 0x1ffffffffffffl) << 11) | (d210 >> 50);
        d210 |= (d210 & 0x3ffffffffffffl) << 11;
        d210 |= (d210 >> 11) | ((d211 & 0x7ffl) << 50);
        d211 |= d211 >> 11;
                {
            int xi = bug.getRecentPos(21).x - loc.x + 5;
            int yi = bug.getRecentPos(21).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d210 |= 1l << idx;
                } else {
                    d211 |= 1l << (idx - 61);
                }
            }
        }

        d210 &= CellTracker.localPassable0;
        d211 &= CellTracker.localPassable1;
        if((d210 & 0x807000000000000l) != 0 || (d211 & 0xe01) != 0) {
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

            if(
               (d210 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d211 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d211 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d210 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d210 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d210 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d211 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d211 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 21);
                return;
            }
        }
        long d220 = 0, d221 = 0;
        d220 = d210;
        d221 = d211;

        // if(rc.getRoundNum() >= 507) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d220, d221);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d220 |= (d220 & 0xfbff7feffdffbffl) << 1;
        d221 |= (d221 & 0x7feffdffbff7fefl) << 1;
        d220 |= (d220 & 0x1f7feffdffbff7fel) >> 1;
        d221 |= (d221 & 0xffdffbff7feffdel) >> 1;

        d221 |= ((d221 & 0x1ffffffffffffl) << 11) | (d220 >> 50);
        d220 |= (d220 & 0x3ffffffffffffl) << 11;
        d220 |= (d220 >> 11) | ((d221 & 0x7ffl) << 50);
        d221 |= d221 >> 11;
                {
            int xi = bug.getRecentPos(22).x - loc.x + 5;
            int yi = bug.getRecentPos(22).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d220 |= 1l << idx;
                } else {
                    d221 |= 1l << (idx - 61);
                }
            }
        }

        d220 &= CellTracker.localPassable0;
        d221 &= CellTracker.localPassable1;
        if((d220 & 0x807000000000000l) != 0 || (d221 & 0xe01) != 0) {
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

            if(
               (d220 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d221 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d221 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d220 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d220 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d220 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d221 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d221 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 22);
                return;
            }
        }
        long d230 = 0, d231 = 0;
        d230 = d220;
        d231 = d221;

        // if(rc.getRoundNum() >= 507) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d230, d231);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d230 |= (d230 & 0xfbff7feffdffbffl) << 1;
        d231 |= (d231 & 0x7feffdffbff7fefl) << 1;
        d230 |= (d230 & 0x1f7feffdffbff7fel) >> 1;
        d231 |= (d231 & 0xffdffbff7feffdel) >> 1;

        d231 |= ((d231 & 0x1ffffffffffffl) << 11) | (d230 >> 50);
        d230 |= (d230 & 0x3ffffffffffffl) << 11;
        d230 |= (d230 >> 11) | ((d231 & 0x7ffl) << 50);
        d231 |= d231 >> 11;
                {
            int xi = bug.getRecentPos(23).x - loc.x + 5;
            int yi = bug.getRecentPos(23).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d230 |= 1l << idx;
                } else {
                    d231 |= 1l << (idx - 61);
                }
            }
        }

        d230 &= CellTracker.localPassable0;
        d231 &= CellTracker.localPassable1;
        if((d230 & 0x807000000000000l) != 0 || (d231 & 0xe01) != 0) {
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

            if(
               (d230 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d231 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d231 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d230 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d230 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d230 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d231 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d231 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 23);
                return;
            }
        }
        long d240 = 0, d241 = 0;
        d240 = d230;
        d241 = d231;

        // if(rc.getRoundNum() >= 507) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d240, d241);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d240 |= (d240 & 0xfbff7feffdffbffl) << 1;
        d241 |= (d241 & 0x7feffdffbff7fefl) << 1;
        d240 |= (d240 & 0x1f7feffdffbff7fel) >> 1;
        d241 |= (d241 & 0xffdffbff7feffdel) >> 1;

        d241 |= ((d241 & 0x1ffffffffffffl) << 11) | (d240 >> 50);
        d240 |= (d240 & 0x3ffffffffffffl) << 11;
        d240 |= (d240 >> 11) | ((d241 & 0x7ffl) << 50);
        d241 |= d241 >> 11;
                {
            int xi = bug.getRecentPos(24).x - loc.x + 5;
            int yi = bug.getRecentPos(24).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d240 |= 1l << idx;
                } else {
                    d241 |= 1l << (idx - 61);
                }
            }
        }

        d240 &= CellTracker.localPassable0;
        d241 &= CellTracker.localPassable1;
        if((d240 & 0x807000000000000l) != 0 || (d241 & 0xe01) != 0) {
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

            if(
               (d240 & (1l << 49))
               != 0 && rc.canMove(Direction.SOUTH)) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 49))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            if(
               (d241 & (1l << 0))
               != 0 && rc.canMove(Direction.EAST)) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 0))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            if(
               (d241 & (1l << 10))
               != 0 && rc.canMove(Direction.NORTH)) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 10))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            if(
               (d240 & (1l << 59))
               != 0 && rc.canMove(Direction.WEST)) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 59))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            if(
               (d240 & (1l << 48))
               != 0 && rc.canMove(Direction.SOUTHWEST)) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 48))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d240 & (1l << 50))
               != 0 && rc.canMove(Direction.SOUTHEAST)) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent0 & (1l << 50))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            if(
               (d241 & (1l << 9))
               != 0 && rc.canMove(Direction.NORTHWEST)) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 9))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            if(
               (d241 & (1l << 11))
               != 0 && rc.canMove(Direction.NORTHEAST)) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                if(
                   (notWallAdjacent1 & (1l << 11))
                   != 0
                   ) {
                    score /= 2;
                }
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            if(bestMove != null) {
                Profiler.end("bfs");
                makeMove(bestMove);
                // System.out.println("iterations: " + 24);
                return;
            }
        }
        indicatorString = "";
        CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        assert false : "TODO: fallback when bfs can't find path";
    }
}