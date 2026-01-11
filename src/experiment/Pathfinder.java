package experiment;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import static experiment.Game.loc;
import static experiment.Game.locX;
import static experiment.Game.locY;
import static experiment.Game.mapHeight;
import static experiment.Game.rc;
import experiment.util.Profiler;
import experiment.util.Rng;
import static experiment.util.Util.chebyshevDistance;
import static experiment.util.Util.indicate;
import static experiment.util.Util.indicatorString;

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
                if (!path19.equals(loc)) {
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
        switch ((nextPathIndex - i + 19) % 20) {
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
            leftDone = leftBug.hitEdge || leftBug.nextPathIndex == 19;
            rightDone = rightBug.hitEdge || leftBug.nextPathIndex == 19;
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
        if(!rc.getLocation().equals(pos) && (!leftReady || !rightReady)) return !(leftDone && rightDone);
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
                            if(greedyPassable && chebyshevDistance(rc.getLocation(), greedyLoc) <= 5) {
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
            var leftGood = (leftPassable || !CellTracker.isSeen(leftLoc)) && rc.onTheMap(leftLoc);
            var rightPassable = CellTracker.isPassable(rightLoc);
            var rightGood = (rightPassable || !CellTracker.isSeen(rightLoc)) && rc.onTheMap(rightLoc);
            if(leftGood && !rightGood) {
                            if(leftPassable && chebyshevDistance(rc.getLocation(), leftLoc) <= 5) {
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
                            if(rightPassable && chebyshevDistance(rc.getLocation(), rightLoc) <= 5) {
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
                                if(leftPassable && chebyshevDistance(rc.getLocation(), leftLoc) <= 5) {
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
                                if(rightPassable && chebyshevDistance(rc.getLocation(), rightLoc) <= 5) {
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
            default -> {
                assert false : "index out of range";
                return null;
            }
        }
    }

    public static void turn(Direction dir) throws GameActionException {
        if(dir == Direction.CENTER || dir == rc.getDirection()) return;
        if(!rc.canTurn()) return; // Turn not ready
        rc.turn(dir);
        CellTracker.storeVisibleCells();
    }
    
    public static boolean isFacing(Direction dir) {
        return rc.getDirection() == dir;
    }

    public static void moveForward() throws GameActionException {
        rc.moveForward();
        CellTracker.storeVisibleCellsPostMove();
    }

    public static void makeMove(Direction dir) throws GameActionException {
        if(dir == Direction.CENTER) return;
        
        // Try to turn toward target direction
        if(rc.getDirection() != dir) {
            if(rc.canTurn()) {
                rc.turn(dir);
                CellTracker.storeVisibleCells();
            } else {
                // Can't turn yet, nothing more we can do
                return;
            }
        }
        
        // Now try to move forward
        if(!rc.isMovementReady()) return;
        
        var nextLoc = rc.getLocation().add(dir);
        if(rc.canSenseLocation(nextLoc) && rc.senseMapInfo(nextLoc).isDirt()) {
            if(rc.canRemoveDirt(nextLoc)) rc.removeDirt(nextLoc);
            else return;
        }
        if(rc.canMoveForward()) moveForward();
        loc = rc.getLocation();
        locX = loc.x;
        locY = loc.y;
    }

    public static void makeMove(MapLocation target) throws GameActionException {
        // If movement not ready, at least try to turn toward target
        if(!rc.isMovementReady()) {
            Direction toTarget = rc.getLocation().directionTo(target);
            if(toTarget != Direction.CENTER && rc.getDirection() != toTarget && rc.canTurn()) {
                rc.turn(toTarget);
                CellTracker.storeVisibleCells();
            }
            return;
        }
        
        // TODO: better handling when target changes
        if(bug == null || !target.equals(bug.target)) {
            bug = new BugnavInstance(true, target);
        }
        // Profiler.begin();
        bugLoop: {
            Profiler.begin();
            if(!bug.advance()) break bugLoop;
            Profiler.end("bug");
            Profiler.begin();
            if(!bug.advance()) break bugLoop;
            Profiler.end("bug");
            Profiler.begin();
            if(!bug.advance()) break bugLoop;
            Profiler.end("bug");
            Profiler.begin();
            if(!bug.advance()) break bugLoop;
            Profiler.end("bug");
            Profiler.begin();
            if(!bug.advance()) break bugLoop;
            Profiler.end("bug");
        }
        // Profiler.end("bug");

        // no bfs needed
        {
            var dir = rc.getLocation().directionTo(bug.pos);
            if(rc.getLocation().add(dir).equals(bug.pos)) {
                indicate("adjacent");
                makeMove(dir);
                return;
            }
        }
        // TODO: what if location == bug.pos?

        // bfs
        Profiler.begin();
        CellTracker.computeLocalBfsMasks();
        var loc = rc.getLocation();
        // d{i}0 + d{i}1 is the mask of reachable within i steps
        long d00 = 0, d01 = 0;
        long d10 = 0, d11 = 0;

        Direction bestMove = null;
        // reconstruction helper variables
        long cur0 = 0, cur1 = 0;
        int curDist = 0;

        long d20 = 0;
        long d21 = 0;
        long d30 = 0;
        long d31 = 0;
        long d40 = 0;
        long d41 = 0;
        long d50 = 0;
        long d51 = 0;
        long d60 = 0;
        long d61 = 0;
        long d70 = 0;
        long d71 = 0;
        long d80 = 0;
        long d81 = 0;
        long d90 = 0;
        long d91 = 0;
        long d100 = 0;
        long d101 = 0;
        long d110 = 0;
        long d111 = 0;
        long d120 = 0;
        long d121 = 0;
        long d130 = 0;
        long d131 = 0;
        long d140 = 0;
        long d141 = 0;
        long d150 = 0;
        long d151 = 0;
        long d160 = 0;
        long d161 = 0;
        long d170 = 0;
        long d171 = 0;
        long d180 = 0;
        long d181 = 0;
        long d190 = 0;
        long d191 = 0;
        long d200 = 0;
        long d201 = 0;
        long d210 = 0;
        long d211 = 0;

        bfsLoop: {
                    {
            int xi = bug.getRecentPos(0 / 2).x - loc.x + 5;
            int yi = bug.getRecentPos(0 / 2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d00 |= 1l << idx;
                } else {
                    d01 |= 1l << (idx - 61);
                }
            }
        }

            // if(rc.getRoundNum() >= 377) {
            //     indicatorString = "";
            //     CellTracker.printLocalMask(d00, d01);
            //     Clock.yield();
            // }

            // Profiler.begin();

            long t00 = d00;
            long t01 = d01;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t00 |= (t00 & 0xfbff7feffdffbffl) << 1;
            t01 |= (t01 & 0x7feffdffbff7fefl) << 1;
            t00 |= (t00 & 0x1f7feffdffbff7fel) >> 1;
            t01 |= (t01 & 0xffdffbff7feffdel) >> 1;

            t01 |= ((t01 & 0x1ffffffffffffl) << 11) | (t00 >> 50);
            t00 |= (t00 & 0x3ffffffffffffl) << 11;
            t00 |= (t00 >> 11) | ((t01 & 0x7ffl) << 50);
            t01 |= t01 >> 11;
            t00 &= CellTracker.localPassable0;
            t01 &= CellTracker.localPassable1;
            d20 = t00;
            d21 = t01;
            t00 &= ~CellTracker.localDirt0;
            t01 &= ~CellTracker.localDirt1;
            d10 |= t00;
            d11 |= t01;
            // Profiler.end("bfs iteration");
            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d10 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d11 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d11 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d10 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d10 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d10 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d11 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d11 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d10;
                    cur1 = d11;
                    curDist = 1;
                    indicate("bfs iterations: " + 1);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t10 = d10;
            long t11 = d11;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t10 |= (t10 & 0xfbff7feffdffbffl) << 1;
            t11 |= (t11 & 0x7feffdffbff7fefl) << 1;
            t10 |= (t10 & 0x1f7feffdffbff7fel) >> 1;
            t11 |= (t11 & 0xffdffbff7feffdel) >> 1;

            t11 |= ((t11 & 0x1ffffffffffffl) << 11) | (t10 >> 50);
            t10 |= (t10 & 0x3ffffffffffffl) << 11;
            t10 |= (t10 >> 11) | ((t11 & 0x7ffl) << 50);
            t11 |= t11 >> 11;
            t10 &= CellTracker.localPassable0;
            t11 &= CellTracker.localPassable1;
            d30 = t10;
            d31 = t11;
            t10 &= ~CellTracker.localDirt0;
            t11 &= ~CellTracker.localDirt1;
            d20 |= t10;
            d21 |= t11;
            // Profiler.end("bfs iteration");
                    {
            int xi = bug.getRecentPos(2 / 2).x - loc.x + 5;
            int yi = bug.getRecentPos(2 / 2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d20 |= 1l << idx;
                } else {
                    d21 |= 1l << (idx - 61);
                }
            }
        }

            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d20 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d21 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d21 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d20 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d20 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d20 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d21 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d21 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d20;
                    cur1 = d21;
                    curDist = 2;
                    indicate("bfs iterations: " + 2);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t20 = d20;
            long t21 = d21;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t20 |= (t20 & 0xfbff7feffdffbffl) << 1;
            t21 |= (t21 & 0x7feffdffbff7fefl) << 1;
            t20 |= (t20 & 0x1f7feffdffbff7fel) >> 1;
            t21 |= (t21 & 0xffdffbff7feffdel) >> 1;

            t21 |= ((t21 & 0x1ffffffffffffl) << 11) | (t20 >> 50);
            t20 |= (t20 & 0x3ffffffffffffl) << 11;
            t20 |= (t20 >> 11) | ((t21 & 0x7ffl) << 50);
            t21 |= t21 >> 11;
            t20 &= CellTracker.localPassable0;
            t21 &= CellTracker.localPassable1;
            d40 = t20;
            d41 = t21;
            t20 &= ~CellTracker.localDirt0;
            t21 &= ~CellTracker.localDirt1;
            d30 |= t20;
            d31 |= t21;
            // Profiler.end("bfs iteration");
            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d30 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d31 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d31 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d30 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d30 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d30 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d31 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d31 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d30;
                    cur1 = d31;
                    curDist = 3;
                    indicate("bfs iterations: " + 3);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t30 = d30;
            long t31 = d31;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t30 |= (t30 & 0xfbff7feffdffbffl) << 1;
            t31 |= (t31 & 0x7feffdffbff7fefl) << 1;
            t30 |= (t30 & 0x1f7feffdffbff7fel) >> 1;
            t31 |= (t31 & 0xffdffbff7feffdel) >> 1;

            t31 |= ((t31 & 0x1ffffffffffffl) << 11) | (t30 >> 50);
            t30 |= (t30 & 0x3ffffffffffffl) << 11;
            t30 |= (t30 >> 11) | ((t31 & 0x7ffl) << 50);
            t31 |= t31 >> 11;
            t30 &= CellTracker.localPassable0;
            t31 &= CellTracker.localPassable1;
            d50 = t30;
            d51 = t31;
            t30 &= ~CellTracker.localDirt0;
            t31 &= ~CellTracker.localDirt1;
            d40 |= t30;
            d41 |= t31;
            // Profiler.end("bfs iteration");
                    {
            int xi = bug.getRecentPos(4 / 2).x - loc.x + 5;
            int yi = bug.getRecentPos(4 / 2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d40 |= 1l << idx;
                } else {
                    d41 |= 1l << (idx - 61);
                }
            }
        }

            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d40 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d41 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d41 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d40 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d40 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d40 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d41 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d41 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d40;
                    cur1 = d41;
                    curDist = 4;
                    indicate("bfs iterations: " + 4);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t40 = d40;
            long t41 = d41;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t40 |= (t40 & 0xfbff7feffdffbffl) << 1;
            t41 |= (t41 & 0x7feffdffbff7fefl) << 1;
            t40 |= (t40 & 0x1f7feffdffbff7fel) >> 1;
            t41 |= (t41 & 0xffdffbff7feffdel) >> 1;

            t41 |= ((t41 & 0x1ffffffffffffl) << 11) | (t40 >> 50);
            t40 |= (t40 & 0x3ffffffffffffl) << 11;
            t40 |= (t40 >> 11) | ((t41 & 0x7ffl) << 50);
            t41 |= t41 >> 11;
            t40 &= CellTracker.localPassable0;
            t41 &= CellTracker.localPassable1;
            d60 = t40;
            d61 = t41;
            t40 &= ~CellTracker.localDirt0;
            t41 &= ~CellTracker.localDirt1;
            d50 |= t40;
            d51 |= t41;
            // Profiler.end("bfs iteration");
            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d50 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d51 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d51 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d50 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d50 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d50 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d51 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d51 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d50;
                    cur1 = d51;
                    curDist = 5;
                    indicate("bfs iterations: " + 5);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t50 = d50;
            long t51 = d51;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t50 |= (t50 & 0xfbff7feffdffbffl) << 1;
            t51 |= (t51 & 0x7feffdffbff7fefl) << 1;
            t50 |= (t50 & 0x1f7feffdffbff7fel) >> 1;
            t51 |= (t51 & 0xffdffbff7feffdel) >> 1;

            t51 |= ((t51 & 0x1ffffffffffffl) << 11) | (t50 >> 50);
            t50 |= (t50 & 0x3ffffffffffffl) << 11;
            t50 |= (t50 >> 11) | ((t51 & 0x7ffl) << 50);
            t51 |= t51 >> 11;
            t50 &= CellTracker.localPassable0;
            t51 &= CellTracker.localPassable1;
            d70 = t50;
            d71 = t51;
            t50 &= ~CellTracker.localDirt0;
            t51 &= ~CellTracker.localDirt1;
            d60 |= t50;
            d61 |= t51;
            // Profiler.end("bfs iteration");
                    {
            int xi = bug.getRecentPos(6 / 2).x - loc.x + 5;
            int yi = bug.getRecentPos(6 / 2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d60 |= 1l << idx;
                } else {
                    d61 |= 1l << (idx - 61);
                }
            }
        }

            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d60 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d61 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d61 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d60 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d60 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d60 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d61 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d61 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d60;
                    cur1 = d61;
                    curDist = 6;
                    indicate("bfs iterations: " + 6);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t60 = d60;
            long t61 = d61;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t60 |= (t60 & 0xfbff7feffdffbffl) << 1;
            t61 |= (t61 & 0x7feffdffbff7fefl) << 1;
            t60 |= (t60 & 0x1f7feffdffbff7fel) >> 1;
            t61 |= (t61 & 0xffdffbff7feffdel) >> 1;

            t61 |= ((t61 & 0x1ffffffffffffl) << 11) | (t60 >> 50);
            t60 |= (t60 & 0x3ffffffffffffl) << 11;
            t60 |= (t60 >> 11) | ((t61 & 0x7ffl) << 50);
            t61 |= t61 >> 11;
            t60 &= CellTracker.localPassable0;
            t61 &= CellTracker.localPassable1;
            d80 = t60;
            d81 = t61;
            t60 &= ~CellTracker.localDirt0;
            t61 &= ~CellTracker.localDirt1;
            d70 |= t60;
            d71 |= t61;
            // Profiler.end("bfs iteration");
            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d70 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d71 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d71 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d70 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d70 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d70 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d71 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d71 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d70;
                    cur1 = d71;
                    curDist = 7;
                    indicate("bfs iterations: " + 7);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t70 = d70;
            long t71 = d71;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t70 |= (t70 & 0xfbff7feffdffbffl) << 1;
            t71 |= (t71 & 0x7feffdffbff7fefl) << 1;
            t70 |= (t70 & 0x1f7feffdffbff7fel) >> 1;
            t71 |= (t71 & 0xffdffbff7feffdel) >> 1;

            t71 |= ((t71 & 0x1ffffffffffffl) << 11) | (t70 >> 50);
            t70 |= (t70 & 0x3ffffffffffffl) << 11;
            t70 |= (t70 >> 11) | ((t71 & 0x7ffl) << 50);
            t71 |= t71 >> 11;
            t70 &= CellTracker.localPassable0;
            t71 &= CellTracker.localPassable1;
            d90 = t70;
            d91 = t71;
            t70 &= ~CellTracker.localDirt0;
            t71 &= ~CellTracker.localDirt1;
            d80 |= t70;
            d81 |= t71;
            // Profiler.end("bfs iteration");
                    {
            int xi = bug.getRecentPos(8 / 2).x - loc.x + 5;
            int yi = bug.getRecentPos(8 / 2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d80 |= 1l << idx;
                } else {
                    d81 |= 1l << (idx - 61);
                }
            }
        }

            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d80 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d81 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d81 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d80 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d80 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d80 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d81 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d81 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d80;
                    cur1 = d81;
                    curDist = 8;
                    indicate("bfs iterations: " + 8);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t80 = d80;
            long t81 = d81;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t80 |= (t80 & 0xfbff7feffdffbffl) << 1;
            t81 |= (t81 & 0x7feffdffbff7fefl) << 1;
            t80 |= (t80 & 0x1f7feffdffbff7fel) >> 1;
            t81 |= (t81 & 0xffdffbff7feffdel) >> 1;

            t81 |= ((t81 & 0x1ffffffffffffl) << 11) | (t80 >> 50);
            t80 |= (t80 & 0x3ffffffffffffl) << 11;
            t80 |= (t80 >> 11) | ((t81 & 0x7ffl) << 50);
            t81 |= t81 >> 11;
            t80 &= CellTracker.localPassable0;
            t81 &= CellTracker.localPassable1;
            d100 = t80;
            d101 = t81;
            t80 &= ~CellTracker.localDirt0;
            t81 &= ~CellTracker.localDirt1;
            d90 |= t80;
            d91 |= t81;
            // Profiler.end("bfs iteration");
            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d90 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d91 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d91 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d90 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d90 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d90 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d91 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d91 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d90;
                    cur1 = d91;
                    curDist = 9;
                    indicate("bfs iterations: " + 9);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t90 = d90;
            long t91 = d91;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t90 |= (t90 & 0xfbff7feffdffbffl) << 1;
            t91 |= (t91 & 0x7feffdffbff7fefl) << 1;
            t90 |= (t90 & 0x1f7feffdffbff7fel) >> 1;
            t91 |= (t91 & 0xffdffbff7feffdel) >> 1;

            t91 |= ((t91 & 0x1ffffffffffffl) << 11) | (t90 >> 50);
            t90 |= (t90 & 0x3ffffffffffffl) << 11;
            t90 |= (t90 >> 11) | ((t91 & 0x7ffl) << 50);
            t91 |= t91 >> 11;
            t90 &= CellTracker.localPassable0;
            t91 &= CellTracker.localPassable1;
            d110 = t90;
            d111 = t91;
            t90 &= ~CellTracker.localDirt0;
            t91 &= ~CellTracker.localDirt1;
            d100 |= t90;
            d101 |= t91;
            // Profiler.end("bfs iteration");
                    {
            int xi = bug.getRecentPos(10 / 2).x - loc.x + 5;
            int yi = bug.getRecentPos(10 / 2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d100 |= 1l << idx;
                } else {
                    d101 |= 1l << (idx - 61);
                }
            }
        }

            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d100 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d101 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d101 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d100 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d100 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d100 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d101 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d101 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d100;
                    cur1 = d101;
                    curDist = 10;
                    indicate("bfs iterations: " + 10);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t100 = d100;
            long t101 = d101;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t100 |= (t100 & 0xfbff7feffdffbffl) << 1;
            t101 |= (t101 & 0x7feffdffbff7fefl) << 1;
            t100 |= (t100 & 0x1f7feffdffbff7fel) >> 1;
            t101 |= (t101 & 0xffdffbff7feffdel) >> 1;

            t101 |= ((t101 & 0x1ffffffffffffl) << 11) | (t100 >> 50);
            t100 |= (t100 & 0x3ffffffffffffl) << 11;
            t100 |= (t100 >> 11) | ((t101 & 0x7ffl) << 50);
            t101 |= t101 >> 11;
            t100 &= CellTracker.localPassable0;
            t101 &= CellTracker.localPassable1;
            d120 = t100;
            d121 = t101;
            t100 &= ~CellTracker.localDirt0;
            t101 &= ~CellTracker.localDirt1;
            d110 |= t100;
            d111 |= t101;
            // Profiler.end("bfs iteration");
            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d110 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d111 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d111 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d110 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d110 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d110 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d111 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d111 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d110;
                    cur1 = d111;
                    curDist = 11;
                    indicate("bfs iterations: " + 11);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t110 = d110;
            long t111 = d111;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t110 |= (t110 & 0xfbff7feffdffbffl) << 1;
            t111 |= (t111 & 0x7feffdffbff7fefl) << 1;
            t110 |= (t110 & 0x1f7feffdffbff7fel) >> 1;
            t111 |= (t111 & 0xffdffbff7feffdel) >> 1;

            t111 |= ((t111 & 0x1ffffffffffffl) << 11) | (t110 >> 50);
            t110 |= (t110 & 0x3ffffffffffffl) << 11;
            t110 |= (t110 >> 11) | ((t111 & 0x7ffl) << 50);
            t111 |= t111 >> 11;
            t110 &= CellTracker.localPassable0;
            t111 &= CellTracker.localPassable1;
            d130 = t110;
            d131 = t111;
            t110 &= ~CellTracker.localDirt0;
            t111 &= ~CellTracker.localDirt1;
            d120 |= t110;
            d121 |= t111;
            // Profiler.end("bfs iteration");
                    {
            int xi = bug.getRecentPos(12 / 2).x - loc.x + 5;
            int yi = bug.getRecentPos(12 / 2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d120 |= 1l << idx;
                } else {
                    d121 |= 1l << (idx - 61);
                }
            }
        }

            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d120 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d121 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d121 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d120 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d120 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d120 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d121 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d121 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d120;
                    cur1 = d121;
                    curDist = 12;
                    indicate("bfs iterations: " + 12);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t120 = d120;
            long t121 = d121;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t120 |= (t120 & 0xfbff7feffdffbffl) << 1;
            t121 |= (t121 & 0x7feffdffbff7fefl) << 1;
            t120 |= (t120 & 0x1f7feffdffbff7fel) >> 1;
            t121 |= (t121 & 0xffdffbff7feffdel) >> 1;

            t121 |= ((t121 & 0x1ffffffffffffl) << 11) | (t120 >> 50);
            t120 |= (t120 & 0x3ffffffffffffl) << 11;
            t120 |= (t120 >> 11) | ((t121 & 0x7ffl) << 50);
            t121 |= t121 >> 11;
            t120 &= CellTracker.localPassable0;
            t121 &= CellTracker.localPassable1;
            d140 = t120;
            d141 = t121;
            t120 &= ~CellTracker.localDirt0;
            t121 &= ~CellTracker.localDirt1;
            d130 |= t120;
            d131 |= t121;
            // Profiler.end("bfs iteration");
            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d130 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d131 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d131 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d130 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d130 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d130 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d131 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d131 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d130;
                    cur1 = d131;
                    curDist = 13;
                    indicate("bfs iterations: " + 13);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t130 = d130;
            long t131 = d131;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t130 |= (t130 & 0xfbff7feffdffbffl) << 1;
            t131 |= (t131 & 0x7feffdffbff7fefl) << 1;
            t130 |= (t130 & 0x1f7feffdffbff7fel) >> 1;
            t131 |= (t131 & 0xffdffbff7feffdel) >> 1;

            t131 |= ((t131 & 0x1ffffffffffffl) << 11) | (t130 >> 50);
            t130 |= (t130 & 0x3ffffffffffffl) << 11;
            t130 |= (t130 >> 11) | ((t131 & 0x7ffl) << 50);
            t131 |= t131 >> 11;
            t130 &= CellTracker.localPassable0;
            t131 &= CellTracker.localPassable1;
            d150 = t130;
            d151 = t131;
            t130 &= ~CellTracker.localDirt0;
            t131 &= ~CellTracker.localDirt1;
            d140 |= t130;
            d141 |= t131;
            // Profiler.end("bfs iteration");
                    {
            int xi = bug.getRecentPos(14 / 2).x - loc.x + 5;
            int yi = bug.getRecentPos(14 / 2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d140 |= 1l << idx;
                } else {
                    d141 |= 1l << (idx - 61);
                }
            }
        }

            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d140 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d141 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d141 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d140 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d140 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d140 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d141 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d141 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d140;
                    cur1 = d141;
                    curDist = 14;
                    indicate("bfs iterations: " + 14);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t140 = d140;
            long t141 = d141;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t140 |= (t140 & 0xfbff7feffdffbffl) << 1;
            t141 |= (t141 & 0x7feffdffbff7fefl) << 1;
            t140 |= (t140 & 0x1f7feffdffbff7fel) >> 1;
            t141 |= (t141 & 0xffdffbff7feffdel) >> 1;

            t141 |= ((t141 & 0x1ffffffffffffl) << 11) | (t140 >> 50);
            t140 |= (t140 & 0x3ffffffffffffl) << 11;
            t140 |= (t140 >> 11) | ((t141 & 0x7ffl) << 50);
            t141 |= t141 >> 11;
            t140 &= CellTracker.localPassable0;
            t141 &= CellTracker.localPassable1;
            d160 = t140;
            d161 = t141;
            t140 &= ~CellTracker.localDirt0;
            t141 &= ~CellTracker.localDirt1;
            d150 |= t140;
            d151 |= t141;
            // Profiler.end("bfs iteration");
            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d150 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d151 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d151 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d150 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d150 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d150 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d151 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d151 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d150;
                    cur1 = d151;
                    curDist = 15;
                    indicate("bfs iterations: " + 15);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t150 = d150;
            long t151 = d151;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t150 |= (t150 & 0xfbff7feffdffbffl) << 1;
            t151 |= (t151 & 0x7feffdffbff7fefl) << 1;
            t150 |= (t150 & 0x1f7feffdffbff7fel) >> 1;
            t151 |= (t151 & 0xffdffbff7feffdel) >> 1;

            t151 |= ((t151 & 0x1ffffffffffffl) << 11) | (t150 >> 50);
            t150 |= (t150 & 0x3ffffffffffffl) << 11;
            t150 |= (t150 >> 11) | ((t151 & 0x7ffl) << 50);
            t151 |= t151 >> 11;
            t150 &= CellTracker.localPassable0;
            t151 &= CellTracker.localPassable1;
            d170 = t150;
            d171 = t151;
            t150 &= ~CellTracker.localDirt0;
            t151 &= ~CellTracker.localDirt1;
            d160 |= t150;
            d161 |= t151;
            // Profiler.end("bfs iteration");
                    {
            int xi = bug.getRecentPos(16 / 2).x - loc.x + 5;
            int yi = bug.getRecentPos(16 / 2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d160 |= 1l << idx;
                } else {
                    d161 |= 1l << (idx - 61);
                }
            }
        }

            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d160 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d161 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d161 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d160 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d160 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d160 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d161 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d161 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d160;
                    cur1 = d161;
                    curDist = 16;
                    indicate("bfs iterations: " + 16);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t160 = d160;
            long t161 = d161;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t160 |= (t160 & 0xfbff7feffdffbffl) << 1;
            t161 |= (t161 & 0x7feffdffbff7fefl) << 1;
            t160 |= (t160 & 0x1f7feffdffbff7fel) >> 1;
            t161 |= (t161 & 0xffdffbff7feffdel) >> 1;

            t161 |= ((t161 & 0x1ffffffffffffl) << 11) | (t160 >> 50);
            t160 |= (t160 & 0x3ffffffffffffl) << 11;
            t160 |= (t160 >> 11) | ((t161 & 0x7ffl) << 50);
            t161 |= t161 >> 11;
            t160 &= CellTracker.localPassable0;
            t161 &= CellTracker.localPassable1;
            d180 = t160;
            d181 = t161;
            t160 &= ~CellTracker.localDirt0;
            t161 &= ~CellTracker.localDirt1;
            d170 |= t160;
            d171 |= t161;
            // Profiler.end("bfs iteration");
            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d170 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d171 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d171 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d170 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d170 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d170 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d171 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d171 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d170;
                    cur1 = d171;
                    curDist = 17;
                    indicate("bfs iterations: " + 17);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t170 = d170;
            long t171 = d171;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t170 |= (t170 & 0xfbff7feffdffbffl) << 1;
            t171 |= (t171 & 0x7feffdffbff7fefl) << 1;
            t170 |= (t170 & 0x1f7feffdffbff7fel) >> 1;
            t171 |= (t171 & 0xffdffbff7feffdel) >> 1;

            t171 |= ((t171 & 0x1ffffffffffffl) << 11) | (t170 >> 50);
            t170 |= (t170 & 0x3ffffffffffffl) << 11;
            t170 |= (t170 >> 11) | ((t171 & 0x7ffl) << 50);
            t171 |= t171 >> 11;
            t170 &= CellTracker.localPassable0;
            t171 &= CellTracker.localPassable1;
            d190 = t170;
            d191 = t171;
            t170 &= ~CellTracker.localDirt0;
            t171 &= ~CellTracker.localDirt1;
            d180 |= t170;
            d181 |= t171;
            // Profiler.end("bfs iteration");
                    {
            int xi = bug.getRecentPos(18 / 2).x - loc.x + 5;
            int yi = bug.getRecentPos(18 / 2).y - loc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11) {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d180 |= 1l << idx;
                } else {
                    d181 |= 1l << (idx - 61);
                }
            }
        }

            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d180 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d181 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d181 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d180 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d180 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d180 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d181 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d181 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d180;
                    cur1 = d181;
                    curDist = 18;
                    indicate("bfs iterations: " + 18);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t180 = d180;
            long t181 = d181;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t180 |= (t180 & 0xfbff7feffdffbffl) << 1;
            t181 |= (t181 & 0x7feffdffbff7fefl) << 1;
            t180 |= (t180 & 0x1f7feffdffbff7fel) >> 1;
            t181 |= (t181 & 0xffdffbff7feffdel) >> 1;

            t181 |= ((t181 & 0x1ffffffffffffl) << 11) | (t180 >> 50);
            t180 |= (t180 & 0x3ffffffffffffl) << 11;
            t180 |= (t180 >> 11) | ((t181 & 0x7ffl) << 50);
            t181 |= t181 >> 11;
            t180 &= CellTracker.localPassable0;
            t181 &= CellTracker.localPassable1;
            d200 = t180;
            d201 = t181;
            t180 &= ~CellTracker.localDirt0;
            t181 &= ~CellTracker.localDirt1;
            d190 |= t180;
            d191 |= t181;
            // Profiler.end("bfs iteration");
            // if(rc.getRoundNum() >= 377) {
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

                double bestScore = 1e18;
                if(
                   (d190 & (1l << 49))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d191 & (1l << 0))
                   != 0) {
                    var nextLoc = loc.add(Direction.EAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.EAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d191 & (1l << 10))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTH);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTH;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d190 & (1l << 59))
                   != 0) {
                    var nextLoc = loc.add(Direction.WEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.WEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d190 & (1l << 48))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d190 & (1l << 50))
                   != 0) {
                    var nextLoc = loc.add(Direction.SOUTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.SOUTHEAST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d191 & (1l << 9))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHWEST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHWEST;
                            bestScore = score;
                        }
                    }
                }
                if(
                   (d191 & (1l << 11))
                   != 0) {
                    var nextLoc = loc.add(Direction.NORTHEAST);
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
                        if(score < bestScore) {
                            bestMove = Direction.NORTHEAST;
                            bestScore = score;
                        }
                    }
                }
                // Profiler.end("bfs best move");
                if(bestMove != null) {
                    cur0 = d190;
                    cur1 = d191;
                    curDist = 19;
                    indicate("bfs iterations: " + 19);
                    break bfsLoop;
                }
            }

            // Profiler.begin();

            long t190 = d190;
            long t191 = d191;
            // we don't need to worry about the "seam" here because it's at the cell we are standing on
            t190 |= (t190 & 0xfbff7feffdffbffl) << 1;
            t191 |= (t191 & 0x7feffdffbff7fefl) << 1;
            t190 |= (t190 & 0x1f7feffdffbff7fel) >> 1;
            t191 |= (t191 & 0xffdffbff7feffdel) >> 1;

            t191 |= ((t191 & 0x1ffffffffffffl) << 11) | (t190 >> 50);
            t190 |= (t190 & 0x3ffffffffffffl) << 11;
            t190 |= (t190 >> 11) | ((t191 & 0x7ffl) << 50);
            t191 |= t191 >> 11;
            t190 &= CellTracker.localPassable0;
            t191 &= CellTracker.localPassable1;
            d210 = t190;
            d211 = t191;
            t190 &= ~CellTracker.localDirt0;
            t191 &= ~CellTracker.localDirt1;
            d200 |= t190;
            d201 |= t191;
            // Profiler.end("bfs iteration");
        }
        Profiler.end("bfs");

        if(bestMove != null) {
            Profiler.begin();
            var curLoc = loc.add(bestMove);
            var lastDir = bestMove;
            var curX = curLoc.x - loc.x + 5;
            var curY = curLoc.y - loc.y + 5;
            bfsPath0 = curLoc;
            bfsPathLength = 1;
            // IF TLE: get rid of this and detect deadlocks another way
            // the case: https://discord.com/channels/@me/1323561256082346025/1459202766655324210
            bfsReconstruct:
            while(true) {
                long isDirt;
                {
                    int idx = curY * 11 + curX;
                    if(idx < 61) {
                        isDirt = (CellTracker.localDirt0 >> idx) & 1l;
                    } else {
                        isDirt = (CellTracker.localDirt1 >> (idx - 61)) & 1l;
                    }
                }
                int weight = isDirt != 0 ? 2 : 1;
                if(curDist - weight < 0) break bfsReconstruct;
                switch(curDist) {
                    case 1 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d00;
                                prev1 = d01;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 2 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d10;
                                prev1 = d11;
                            }
                            case 2 -> {
                                prev0 = d00;
                                prev1 = d01;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 3 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d20;
                                prev1 = d21;
                            }
                            case 2 -> {
                                prev0 = d10;
                                prev1 = d11;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 4 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d30;
                                prev1 = d31;
                            }
                            case 2 -> {
                                prev0 = d20;
                                prev1 = d21;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 5 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d40;
                                prev1 = d41;
                            }
                            case 2 -> {
                                prev0 = d30;
                                prev1 = d31;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 6 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d50;
                                prev1 = d51;
                            }
                            case 2 -> {
                                prev0 = d40;
                                prev1 = d41;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 7 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d60;
                                prev1 = d61;
                            }
                            case 2 -> {
                                prev0 = d50;
                                prev1 = d51;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 8 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d70;
                                prev1 = d71;
                            }
                            case 2 -> {
                                prev0 = d60;
                                prev1 = d61;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 9 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d80;
                                prev1 = d81;
                            }
                            case 2 -> {
                                prev0 = d70;
                                prev1 = d71;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 10 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d90;
                                prev1 = d91;
                            }
                            case 2 -> {
                                prev0 = d80;
                                prev1 = d81;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 11 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d100;
                                prev1 = d101;
                            }
                            case 2 -> {
                                prev0 = d90;
                                prev1 = d91;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 12 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d110;
                                prev1 = d111;
                            }
                            case 2 -> {
                                prev0 = d100;
                                prev1 = d101;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 13 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d120;
                                prev1 = d121;
                            }
                            case 2 -> {
                                prev0 = d110;
                                prev1 = d111;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 14 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d130;
                                prev1 = d131;
                            }
                            case 2 -> {
                                prev0 = d120;
                                prev1 = d121;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 15 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d140;
                                prev1 = d141;
                            }
                            case 2 -> {
                                prev0 = d130;
                                prev1 = d131;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 16 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d150;
                                prev1 = d151;
                            }
                            case 2 -> {
                                prev0 = d140;
                                prev1 = d141;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 17 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d160;
                                prev1 = d161;
                            }
                            case 2 -> {
                                prev0 = d150;
                                prev1 = d151;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 18 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d170;
                                prev1 = d171;
                            }
                            case 2 -> {
                                prev0 = d160;
                                prev1 = d161;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                    case 19 -> {
                        long prev0 = 0, prev1 = 0;
                        switch(weight) {
                            case 1 -> {
                                prev0 = d180;
                                prev1 = d181;
                            }
                            case 2 -> {
                                prev0 = d170;
                                prev1 = d171;
                            }
                        }

                        dirLoop: {
                            var leftDir = lastDir;
                            var rightDir = lastDir.rotateRight();
                            for(int i = 0; i < 7; i++) {
                                Direction dir;
                                if(i % 2 == 0) {
                                    dir = leftDir;
                                    leftDir = leftDir.rotateLeft();
                                } else {
                                    dir = rightDir;
                                    rightDir = rightDir.rotateRight();
                                }
                                var dx = dir.dx;
                                var dy = dir.dy;
                                // Profiler.begin();
                                curX += dx;
                                curY += dy;
                                long v = 0;
                                {
                                    if(curX >= 0 && curX < 11 && curY >= 0 && curY < 11) {
                                        int idx = curY * 11 + curX;
                                        if(idx < 61) {
                                            v = (prev0 >> idx) & 1l;
                                        } else {
                                            v = (prev1 >> (idx - 61)) & 1l;
                                        }
                                        // Profiler.end("one dir");
                                        if(v != 0) {
                                            lastDir = dir;
                                            curLoc = new MapLocation(loc.x + curX - 5, loc.y + curY - 5);
                                            appendPosition(curLoc);
                                            rc.setIndicatorDot(curLoc, 0, 0, 255);
                                            curDist -= weight;
                                            break dirLoop;
                                        }
                                    }
                                }
                                curX -= dx;
                                curY -= dy;
                            }
                            break bfsReconstruct;
                        }
                    }
                }
            }
            Profiler.end("bfs reconstruct");
            makeMove(bestMove);
            return;
        }

        indicatorString = "";
        CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        assert false : "TODO: fallback when bfs can't find path";
    }
}