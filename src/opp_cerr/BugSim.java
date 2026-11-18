package opp_cerr;

import battlecode.common.*;

import opp_cerr.fast.FastIntSet;
import opp_cerr.fast.FastRecentLocSet;

/*  PLAN
   1. simulate bugnav using existing knowledge within vision radius (e.g. we can rotate and see it)
       a. if we simulate bugnav within radius but run into unknown, rotate to gain knowledge
       b. if rotation on cooldown,
   2. optimistic bfs using existing knowledge and assuming non-visible squares have no bots
   3. rotate to view intended bfs direction
   4. attempt to move
   5. if blocked, bfs again and attempt to move

   *. what if there is a bot pileup? can we treat bots as immobile?
   *. avoid moving next to allies?
*/

public class BugSim {
    static RobotController rc;

    private static final int INF = Integer.MAX_VALUE;
    private static final int SIMS_PER_MOVE = 5;

    // penalties scaled by 10
    private static final int PENALTY_NOMOVE = 20;
    private static final int PENALTY_BLOCKED = 16;
    private static final int PENALTY_ROTATE = 5;
    private static final int PENALTY_DIRT = 4;
    private static final int PENALTY_REC_SURR = 100;

    static MapLocation myLoc;
    static boolean endSim = true;
    static final int MAX_SIM_DIST = 25;

    static MapLocation targLoc;
    static MapLocation bugLoc;
    static MapLocation nextBugLoc;
    static MapLocation tryLoc;
    static Direction tryDir;
    static int tryDist;
    static int bestTryDist;

    static boolean isBugging;
    static Direction bugDir;
    static final boolean SHOULD_GUESS_ROTATION = true;
    static boolean guessRotation;
    static boolean rotateRight;
    static int bestBugDist;
    static MapLocation BBDLoc;

    static final int BUG_PATH_MAXLEN = 100;
    static final MapLocation[] bugPath = new MapLocation[BUG_PATH_MAXLEN];
    static int bugPathSz;
    static final FastIntSet bugCodeSet = new FastIntSet();

    static boolean gatherInfo;
    static Direction infoDir;

    static final int MAX_NO_RESET_RADIUS = 5;

    static int fallbackUntil = -1;
    static int FALLBACK_TURNS = 100;
    static double fallbackCounter = 0;
    static final double FALLBACK_THRESHOLD = 2.5;
    static final double FALLBACK_MULTIPLIER = 0.8;
    static final FastRecentLocSet fallbackTracker = new FastRecentLocSet(10);

    public static void init(RobotController r) {
        rc = r;
        isBugging = false;
    }

    public static void setTarget(MapLocation tg) {
        // System.out.println("reset pathing!");
        targLoc = tg;
        bugLoc = rc.getLocation();
        bugDir = Direction.CENTER;
        isBugging = false;
        endSim = false;
        bugPathSz = 0;
        bugCodeSet.clear();
        fallbackTracker.clear();
    }

    public static void hackTarget(MapLocation tg) {
        if (targLoc == null) {
            setTarget(tg);
            return;
        }
        targLoc = tg;
        bestBugDist = tg.distanceSquaredTo(BBDLoc != null ? BBDLoc : bugLoc);
        bugCodeSet.clear();
    }

    public static void clearTarget() {
        // System.out.println("reset pathing!");
        targLoc = null;
        bugLoc = null;
    }

    public static void doFallback() {
        clearTarget();
        fallbackUntil = RobotPlayer.round + (FALLBACK_TURNS * (100 + rc.getRawCheese())) / 100;
        fallbackTracker.clear();
        fallbackCounter = 0;
        //        System.out.println("falling back");
    }

    public static void executeMovement(Direction d) throws GameActionException {
        if (rc.canTurn() && rc.getDirection() != d) {
            Attention.focus(d, null, Attention.AttentionType.SCOUT);
            if (Attention.getCurrentFocusType() == Attention.AttentionType.SCOUT) {
                Attention.turn();
            }
        }
        if (rc.getDirection() == d) {
            rc.moveForward();
        } else {
            rc.move(d);
        }
    }

    public static boolean move(MapLocation tg) throws GameActionException {
        return move(tg, targLoc != null && tg.distanceSquaredTo(targLoc) <= MAX_NO_RESET_RADIUS);
    }

    // returns whether at target
    public static boolean move(MapLocation tg, boolean override) throws GameActionException {

        if (tg == null) return false;

        if (!tg.equals(targLoc)) {
            if (override) hackTarget(tg);
            else setTarget(tg);
        }

        if (!rc.isMovementReady()) return false;

        // TODO maybe move this block to ebd of move
        if (gatherInfo && rc.canTurn()) {
            Attention.focus(BugSim.infoDir, null, Attention.AttentionType.SCOUT);
            if (Attention.getCurrentFocusType() == Attention.AttentionType.SCOUT) {
                Attention.turn();
            }
            Navigation.scoutVisible();
        }
        gatherInfo = false;

        myLoc = rc.getLocation();
        if (myLoc.equals(targLoc)) return true;
        if (myLoc.isAdjacentTo(targLoc)) {
            if (rc.canMove(myLoc.directionTo(targLoc))) rc.move(myLoc.directionTo(targLoc));
            return true;
        }

        if (fallbackUntil < RobotPlayer.round) {
            MapLocation fallbackLoc = fallbackTracker.first();
            if (fallbackLoc != null && fallbackLoc.distanceSquaredTo(myLoc) <= 2) {
                fallbackCounter += 1;
                rc.setIndicatorLine(myLoc, fallbackLoc, 255, 0, 255);
            }
            fallbackTracker.add(myLoc);
            fallbackCounter *= FALLBACK_MULTIPLIER;
        }
        if (fallbackCounter > FALLBACK_THRESHOLD) doFallback();
        if (fallbackUntil >= RobotPlayer.round) {
            BugNav.move(tg);
            return false;
        }

        endSim = bugLoc.equals(targLoc) || bugLoc.isAdjacentTo(targLoc);

        for (int i = 0; i < SIMS_PER_MOVE; i++) {
            if (endSim || bugLoc.distanceSquaredTo(myLoc) > MAX_SIM_DIST) break;
            if (isBugging) simBug();
            else simGreedy();
            if (appendBugLoc()) System.out.println("looped!");
            if (bugLoc.equals(targLoc)) break;
        }

        MapLocation anchor = getAnchor();

        //                rc.setIndicatorLine(ZEROZERO, targLoc, 0, 255, 0);
        //                if (anchor != null) {
        //                    for (int i = 0; i < bugPathSz; i++) {
        //                        rc.setIndicatorDot(bugPath[i], Robot.r, Robot.g, Robot.b);
        //                    }
        //        //            rc.setIndicatorLine(myLoc, anchor, Robot.r, Robot.g, Robot.b);
        //                    rc.setIndicatorLine(targLoc, anchor, Robot.r, Robot.g, Robot.b);
        //                    rc.setIndicatorDot(targLoc, Robot.r, Robot.g, Robot.b);
        //                    rc.setIndicatorDot(anchor, 0, 0, 255);
        //                }
        //                if (gatherInfo)
        //                    rc.setIndicatorDot(myLoc.add(infoDir), 255, 0, 0);

        bfs(anchor); // writes intended direction to tryDir
        if (tryDir.equals(Direction.CENTER)) return false;
        tryLoc = myLoc.add(tryDir);
        //        rc.setIndicatorLine(myLoc, tryLoc, 0, 255, 0);
        if (rc.canRemoveDirt(tryLoc)) rc.removeDirt(tryLoc);
        if (rc.canMove(tryDir)) { // if can move
            executeMovement(tryDir);
            return false;
        }

        // otherwise, is either blocked by robot or out of vision cone and blocked by dirt
        if (!rc.canSenseLocation(tryLoc) && rc.canTurn()) {
            Attention.focus(tryDir, null, Attention.AttentionType.SCOUT);
            if (Attention.getCurrentFocusType() == Attention.AttentionType.SCOUT) {
                Attention.turn();
            }
            if (rc.canRemoveDirt(tryLoc)) rc.removeDirt(tryLoc);
            if (rc.canMove(tryDir)) { // if can move
                executeMovement(tryDir);
                return false;
            }
        }

        return false;
    }

    private static final FastRecentLocSet recentSurrounded = new FastRecentLocSet(10);

    // returns if >= 7 sides are not movable
    private static boolean recountSurrounded() {
        if (!rc.isMovementReady()) return false;
        int n = 0;
        for (Direction d : Utils.DIRECTIONS) if (!rc.canMove(d)) n++;
        if (n > 6) recentSurrounded.add(myLoc);
        else recentSurrounded.add();
        return n > 6;
    }

    // returns if loc already been in bugnav
    private static boolean appendBugLoc() {
        int code = getCode();

        if (!(bugPathSz > 0 && bugPath[bugPathSz - 1].equals(bugLoc))) {
            if (bugPathSz == BUG_PATH_MAXLEN) {
                bugPathSz = 10;
                bugPath[0] = bugPath[BUG_PATH_MAXLEN - 10];
                bugPath[1] = bugPath[BUG_PATH_MAXLEN - 9];
                bugPath[2] = bugPath[BUG_PATH_MAXLEN - 8];
                bugPath[3] = bugPath[BUG_PATH_MAXLEN - 7];
                bugPath[4] = bugPath[BUG_PATH_MAXLEN - 6];
                bugPath[5] = bugPath[BUG_PATH_MAXLEN - 5];
                bugPath[6] = bugPath[BUG_PATH_MAXLEN - 4];
                bugPath[7] = bugPath[BUG_PATH_MAXLEN - 3];
                bugPath[8] = bugPath[BUG_PATH_MAXLEN - 2];
                bugPath[9] = bugPath[BUG_PATH_MAXLEN - 1];
            }
            bugPath[bugPathSz++] = bugLoc;
        }

        return bugCodeSet.contains(code);
    }

    private static int getCode() {
        // bug x (6 bits)
        // bug y (6 bits)
        // is bugging (1 bit)
        // bug dir (4 bits)
        // best distance (13 bits)
        // rotate right (1 bit)
        if (isBugging)
            return bugLoc.x
                    | (bugLoc.y << 6)
                    | (1 << 12)
                    | (bugDir.ordinal() << 13)
                    | (bestBugDist << 17)
                    | ((rotateRight ? 1 : 0) << 30);
        else return bugLoc.x | (bugLoc.y << 6);
    }

    private static MapLocation getAnchor() {
        if (Navigation.lastRecalcTurn != RobotPlayer.round
                || !Navigation.localReachCenter.equals(myLoc)) Navigation.recalcLocalReach(myLoc);

        if (Navigation.localReachable(targLoc)) return targLoc;

        for (int i = bugPathSz - 1; i >= 0; --i) {
            if (Navigation.localReachable(bugPath[i])) return bugPath[i];
        }

        setTarget(targLoc);
        return null;
    }

    // simulates bug move
    // returns whether bot needs to move vision cone to make good move
    private static boolean simBug() {
        assert (isBugging && bestBugDist < 8000 && !bugDir.equals(Direction.CENTER));

        // set bug dir if not decided
        if (guessRotation) {
            guessRotation = false;

            Direction dirL = bugDir;
            for (int j = 8; j-- > 0; ) {
                tryLoc = bugLoc.add(dirL);
                if (!rc.onTheMap(tryLoc)) continue;
                if (!Navigation.isKnown(tryLoc)) {
                    gatherInfo = true;
                    infoDir = bugLoc.directionTo(tryLoc);
                    endSim = true;
                    return true;
                } else if (Navigation.isPassable(tryLoc)) break;
                dirL = dirL.rotateLeft();
            }

            Direction dirR = bugDir;
            for (int j = 8; j-- > 0; ) {
                tryLoc = bugLoc.add(dirR);
                if (!rc.onTheMap(tryLoc)) continue;
                if (!Navigation.isKnown(tryLoc)) {
                    gatherInfo = true;
                    infoDir = bugLoc.directionTo(tryLoc);
                    endSim = true;
                    return true;
                } else if (Navigation.isPassable(tryLoc)) break;
                dirR = dirR.rotateRight();
            }

            // Check which results in a location closer to the target
            MapLocation locL = bugLoc.add(dirL);
            MapLocation locR = bugLoc.add(dirR);
            int lDist = targLoc.distanceSquaredTo(locL);
            int rDist = targLoc.distanceSquaredTo(locR);
            rotateRight = lDist > rDist && rc.onTheMap(locR);
        }

        nextBugLoc = null;
        // test rotations
        for (int i = 0; i < 8; i++) {
            tryLoc = bugLoc.add(bugDir);
            if (rc.onTheMap(tryLoc)) {
                if (Navigation.isKnown(tryLoc)) {
                    if (Navigation.isPassable(tryLoc)) {
                        nextBugLoc = tryLoc;
                        break;
                    }
                } else if (tryLoc.distanceSquaredTo(myLoc)
                        <= UnitType.BABY_RAT.visionConeRadiusSquared) {
                    endSim = true;
                    gatherInfo = true;
                    infoDir = myLoc.directionTo(tryLoc);
                    return true;
                } else {
                    endSim = true;
                    return false;
                }
            } else {
                rotateRight = !rotateRight;
                i = 0;
            }

            bugDir = rotate(bugDir);
        }

        // intended move
        if (nextBugLoc != null) {
            assert (!nextBugLoc.equals(bugLoc));
            bugLoc = nextBugLoc;
            bugDir = rotateOpposite(rotateOpposite(bugDir));
            if (bugLoc.distanceSquaredTo(targLoc) < bestBugDist) {
                isBugging = false;
                rc.setIndicatorString("ending bug");
            }

        } else {
            System.out.println("Trapped in a box!");
        }
        return false;
    }

    // simulates greedy move
    // returns whether bot needs to move vision cone to make good move
    private static boolean simGreedy() {
        nextBugLoc = bugLoc;
        bestTryDist = bugLoc.distanceSquaredTo(targLoc);

        //        if (bugLoc.distanceSquaredTo(myLoc) > 10) {
        //            endSim = true;
        //            return false;
        //        }

        for (Direction dir : Utils.DIRECTIONS) {
            tryLoc = bugLoc.add(dir);

            if (rc.onTheMap(tryLoc)) {
                tryDist = tryLoc.distanceSquaredTo(targLoc);
                if (tryDist < bestTryDist) {
                    if (Navigation.isKnown(tryLoc)) {
                        if (Navigation.isPassable(tryLoc)) {
                            nextBugLoc = tryLoc;
                            bestTryDist = tryDist;
                        }
                    } else {
                        if (tryLoc.distanceSquaredTo(myLoc)
                                <= UnitType.BABY_RAT.visionConeRadiusSquared) {
                            gatherInfo = true;
                            infoDir = myLoc.directionTo(tryLoc);
                        } else {
                            gatherInfo = false;
                        }
                        bestTryDist = tryDist;
                        nextBugLoc = null;
                    }
                }
            }
        }

        if (nextBugLoc == null) {
            endSim = true;
            if (gatherInfo) {
                rc.setIndicatorString("gatherinfo " + infoDir);
                return true;
            }
        } else if (nextBugLoc.equals(bugLoc)) {
            initBug();
            rc.setIndicatorString("starting bug");
        } else {
            bugLoc = nextBugLoc;
            bestBugDist = bestTryDist;
            BBDLoc = nextBugLoc;
        }

        return false;
    }

    private static void initBug() {
        isBugging = true;
        bugDir = bugLoc.directionTo(targLoc);
        guessRotation = SHOULD_GUESS_ROTATION;
        rotateRight = true;
        bestBugDist = bugLoc.distanceSquaredTo(targLoc);
        BBDLoc = bugLoc;
    }

    private static final MapLocation ZEROZERO = new MapLocation(0, 0);

    private static void bfs(MapLocation anchor) throws GameActionException {
        tryDir = Direction.CENTER;
        if (anchor == null) return;

        int[] dirDists = rippedBFS(anchor);
        tryDist = dirDists[8] * 10 + PENALTY_NOMOVE;

        int tempDist;
        int ties = 1;
        for (int i = 0; i < 8; i++) {
            Direction d = Utils.DIRECTIONS[i];
            tempDist = dirDists[i] * 10;
            if (tempDist > 100) continue;

            tryLoc = myLoc.add(d);
            if (rc.getDirection() != d) tempDist += PENALTY_ROTATE;
            if (!rc.canMove(d)) {
                if (Navigation.knownAndPassable(tryLoc)) {
                    if (rc.canRemoveDirt(tryLoc)) {
                        tempDist += PENALTY_DIRT;
                    } else {
                        tempDist += PENALTY_BLOCKED;
                    }
                } else {
                    tempDist += PENALTY_NOMOVE;
                }
            }

            if (tempDist < tryDist) {
                tryDist = tempDist;
                tryDir = Utils.DIRECTIONS[i];
                ties = 1;
            } else if (tempDist == tryDist && Robot.idRandom.nextInt(++ties) == 0) {
                tryDist = tempDist;
                tryDir = Utils.DIRECTIONS[i];
            }
        }
    }

    private static Direction rotate(Direction dir) {
        return rotateRight ? dir.rotateRight() : dir.rotateLeft();
    }

    private static Direction rotateOpposite(Direction dir) {
        return rotateRight ? dir.rotateLeft() : dir.rotateRight();
    }

    // xcen expected to be within bounds
    // passable/unknown = 1, impassable = 0
    private static long row9(int y, int xcen) {
        if (y < 0 || y >= Navigation.HEIGHT) return 0x1FF;
        if (xcen < 4) {
            // pad left side with 1s
            return ((Navigation.passability[y] << 4 - xcen) | Utils.BASIC_MASKS[4 - xcen])
                    & Utils.BASIC_MASKS[9];
        } else if (xcen > Navigation.WIDTH - 5) {
            // pad right side with 1s
            int xl = xcen - 4;
            return ((Navigation.passability[y] >> xl) | ~Utils.BASIC_MASKS[Navigation.WIDTH - xl])
                    & Utils.BASIC_MASKS[9];
        } else {
            return (Navigation.passability[y] >> (xcen - 4)) & Utils.BASIC_MASKS[9];
        }
    }

    private static int[] rippedBFS(MapLocation anchor) {
        long localMask0 = 0;
        long localMask1 = 0;
        int index = ((anchor.y - (myLoc.y - 4)) * 9) + (anchor.x - (myLoc.x - 4));
        if (index >= 63) {
            localMask1 = 1L << (index - 63);
        } else {
            localMask0 = 1L << index;
        }

        long temp;
        long adjIntersection;
        long adjTiles = 0b000000000000111000000111000000111000000000000000000000000000000L;
        long mask0 = 0x7FFFFFFFFFFFFFFFL;
        long mask1 = 0x3FFFFL;
        long loverflow = 0x7fbfdfeff7fbfdfeL;
        long roverflow = 0x3fdfeff7fbfdfeffL;

        long passible0 =
                (~((row9(myLoc.y + 2, myLoc.x) << 54)
                                        | (row9(myLoc.y + 1, myLoc.x) << 45)
                                        | (row9(myLoc.y, myLoc.x) << 36)
                                        | (row9(myLoc.y - 1, myLoc.x) << 27)
                                        | (row9(myLoc.y - 2, myLoc.x) << 18)
                                        | (row9(myLoc.y - 3, myLoc.x) << 9)
                                        | row9(myLoc.y - 4, myLoc.x))
                                | localMask0)
                        & 0b111111111111111111111111111111111111111111111011111110001111100L;

        long passible1 =
                (~((row9(myLoc.y + 4, myLoc.x) << 9) | row9(myLoc.y + 3, myLoc.x)) | localMask1)
                        & 0b001111100011111110L;

        int dist00 = 10000;
        int dist01 = 10000;
        int dist02 = 10000;
        int dist10 = 10000;
        int dist11 = 10000;
        int dist12 = 10000;
        int dist20 = 10000;
        int dist21 = 10000;
        int dist22 = 10000;

        // Run a BFS
        long mask0AndPassible = mask0 & passible0;
        long mask1AndPassible = mask1 & passible1;
        bfsLoop:
        {
            adjIntersection = (localMask0 & adjTiles);
            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 0);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 0);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 0);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 0);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 0);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 0);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 0);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 0);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 0);
            }

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 1);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 1);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 1);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 1);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 1);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 1);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 1);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 1);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 1);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 2);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 2);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 2);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 2);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 2);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 2);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 2);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 2);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 2);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 3);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 3);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 3);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 3);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 3);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 3);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 3);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 3);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 3);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 4);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 4);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 4);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 4);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 4);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 4);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 4);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 4);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 4);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 5);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 5);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 5);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 5);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 5);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 5);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 5);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 5);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 5);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 6);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 6);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 6);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 6);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 6);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 6);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 6);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 6);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 6);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 7);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 7);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 7);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 7);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 7);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 7);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 7);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 7);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 7);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 8);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 8);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 8);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 8);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 8);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 8);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 8);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 8);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 8);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 9);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 9);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 9);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 9);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 9);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 9);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 9);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 9);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 9);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 10);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 10);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 10);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 10);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 10);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 10);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 10);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 10);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 10);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 11);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 11);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 11);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 11);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 11);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 11);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 11);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 11);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 11);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 12);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 12);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 12);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 12);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 12);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 12);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 12);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 12);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 12);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 13);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 13);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 13);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 13);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 13);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 13);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 13);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 13);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 13);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 14);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 14);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 14);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 14);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 14);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 14);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 14);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 14);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 14);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 15);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 15);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 15);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 15);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 15);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 15);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 15);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 15);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 15);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 16);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 16);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 16);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 16);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 16);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 16);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 16);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 16);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 16);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 17);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 17);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 17);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 17);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 17);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 17);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 17);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 17);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 17);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 18);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 18);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 18);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 18);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 18);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 18);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 18);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 18);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 18);
            }

            if (adjIntersection == (passible0 & adjTiles)) break bfsLoop;

            localMask0 =
                    (localMask0
                            | ((localMask0 << 1) & loverflow)
                            | ((localMask0 >>> 1) & roverflow));
            localMask1 =
                    (localMask1
                            | ((localMask1 << 1) & loverflow)
                            | ((localMask1 >>> 1) & roverflow));
            //            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0AndPassible;
            //            localMask1 = (localMask1 | (localMask1 << 9) | (localMask1 >>> 9)
            //                    | (temp >>> 54)) & mask1AndPassible;

            adjIntersection = (localMask0 & adjTiles);

            if ((0x40000000L & adjIntersection) != 0) {
                dist00 = Math.min(dist00, 19);
            }

            if ((0x8000000000L & adjIntersection) != 0) {
                dist01 = Math.min(dist01, 19);
            }

            if ((0x1000000000000L & adjIntersection) != 0) {
                dist02 = Math.min(dist02, 19);
            }

            if ((0x80000000L & adjIntersection) != 0) {
                dist10 = Math.min(dist10, 19);
            }

            if ((0x10000000000L & adjIntersection) != 0) {
                dist11 = Math.min(dist11, 19);
            }

            if ((0x2000000000000L & adjIntersection) != 0) {
                dist12 = Math.min(dist12, 19);
            }

            if ((0x100000000L & adjIntersection) != 0) {
                dist20 = Math.min(dist20, 19);
            }

            if ((0x20000000000L & adjIntersection) != 0) {
                dist21 = Math.min(dist21, 19);
            }

            if ((0x4000000000000L & adjIntersection) != 0) {
                dist22 = Math.min(dist22, 19);
            }
        }

        return new int[] {dist12, dist22, dist21, dist20, dist10, dist00, dist01, dist02, dist11};
    }
}
