package opp_cerr;

import battlecode.common.*;

import opp_cerr.fast.FastQueue;

public class Navigation {
    private static RobotController rc;

    // MAP
    public static int WIDTH, HEIGHT;
    // public static int[][] map;
    public static long[] seen = new long[60];
    public static long[] isDirt = new long[60];
    public static long[] passability = new long[60]; // 1 if known not passable
    public static long[] passabilityStrict = { // 1 if unknown or known not passable
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFL,
                0xFFFFFFFFFFFFFFFL
    };
    public static long[] botOccupied = new long[60]; // 1 if occupied

    public static long[] kingPassability; // 1 if known not passable

    public static final int LOCAL_REACH_ITERS = 5;

    public static int lastRecalcTurn = -1;
    public static MapLocation localReachCenter = new MapLocation(-1, -1);
    public static int[] localReachability = new int[9];

    public static int lastRecalcTurnDirt = -1;
    public static MapLocation localReachCenterDirt = new MapLocation(-1, -1);
    public static int[] localReachabilityDirt = new int[9];

    public static final RobotInfo[] localBotsTemp = new RobotInfo[100];

    public static final int ROUNDS_IN_DIR = 40;

    public static final int TILE_UNKNOWN = 0;
    public static final int TILE_EMPTY = 0b1;
    public static final int TILE_WALL = 0b10;
    public static final int TILE_DIRT = 0b100;

    public static MapInfo[] nearbyMapInfos;

    // EXPLORATION - 6x6 block tracking
    public static final int BLOCK_SIZE = 6;
    public static int BLOCKS_WIDTH, BLOCKS_HEIGHT;
    public static long[] visitedBlocks; // visitedBlocks[blockY] has bit blockX set if visited

    // SYMMETRY
    public static final int SYM_H = 0b1;
    public static final int SYM_V = 0b10;
    public static final int SYM_R = 0b100;
    private static int symLeft = 3;
    public static boolean symLocked = false;
    public static int symState =
            0b111; // Once symLocked == true, equals a SYM constant; use in switch
    public static boolean canSymH = true;
    public static boolean canSymV = true;
    public static boolean canSymR = true;

    public static boolean needUpdateSymmetry;

    public static FastQueue<MapLocation> symmetryQueue = new FastQueue<MapLocation>(500);
    public static final int SYMM_BYTECODE = 1000; // do not run this if < 2000 bc

    public static void init(RobotController r) {
        rc = r;
        WIDTH = rc.getMapWidth();
        HEIGHT = rc.getMapHeight();
        // map = new int[WIDTH][HEIGHT];

        //        Arrays.fill(kingPassability, );
        long kpdv = 1L | (1L << (WIDTH - 1));
        kingPassability =
                new long[] {
                    kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv,
                    kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv,
                    kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv,
                    kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv,
                    kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv, kpdv
                };
        kingPassability[0] = Utils.BASIC_MASKS[WIDTH];
        kingPassability[HEIGHT - 1] = Utils.BASIC_MASKS[WIDTH];

        // Initialize exploration tracking
        BLOCKS_WIDTH = (WIDTH + BLOCK_SIZE - 1) / BLOCK_SIZE; // ceil division
        BLOCKS_HEIGHT = (HEIGHT + BLOCK_SIZE - 1) / BLOCK_SIZE;
        visitedBlocks = new long[BLOCKS_HEIGHT];
    }

    // ^ ------------------ SYMMETRY -------------------

    public static boolean isSymH() {
        return symState == SYM_H;
    }

    public static boolean isSymV() {
        return symState == SYM_V;
    }

    public static boolean isSymR() {
        return symState == SYM_R;
    }

    public static void elimSymH() {
        if (canSymH && !symLocked) {
            symState &= ~SYM_H;
            canSymH = false;
            symLocked = --symLeft == 1;
        }
    }

    public static void elimSymV() {
        if (canSymV && !symLocked) {
            symState &= ~SYM_V;
            canSymV = false;
            symLocked = --symLeft == 1;
        }
    }

    public static void elimSymR() {
        if (canSymR && !symLocked) {
            symState &= ~SYM_R;
            canSymR = false;
            symLocked = --symLeft == 1;
        }
    }

    public static MapLocation getSymH(MapLocation loc) {
        return new MapLocation(loc.x, HEIGHT - loc.y - 1);
    }

    public static MapLocation getSymV(MapLocation loc) {
        return new MapLocation(WIDTH - loc.x - 1, loc.y);
    }

    public static MapLocation getSymR(MapLocation loc) {
        return new MapLocation(WIDTH - loc.x - 1, HEIGHT - loc.y - 1);
    }

    // ^ -------------- SCOUTING AND UPDATE --------------------
    // ^

    public static void refreshTurn() {
        botOccupied = new long[HEIGHT];
        needUpdateSymmetry =
                ((Navigation.canSymR != Comms.isRSymPossible)
                        || (Navigation.canSymV != Comms.isVSymPossible)
                        || (Navigation.canSymH != Comms.isHSymPossible));

        // Mark current block as visited
        markCurrentBlockVisited();
    }

    // ^ ------------------ EXPLORATION -------------------

    /** Marks the block containing rc.getLocation() as visited. */
    public static void markCurrentBlockVisited() {
        try {
            MapLocation loc = rc.getLocation();
            int blockX = loc.x / BLOCK_SIZE;
            int blockY = loc.y / BLOCK_SIZE;
            visitedBlocks[blockY] |= (1L << blockX);
        } catch (Exception e) {
            // Ignore if rc not ready
        }
    }

    /** Checks if a block has been visited. */
    public static boolean isBlockVisited(int blockX, int blockY) {
        if (blockY < 0 || blockY >= BLOCKS_HEIGHT || blockX < 0 || blockX >= BLOCKS_WIDTH) {
            return true; // Out of bounds = "visited" (don't explore)
        }
        return (visitedBlocks[blockY] & (1L << blockX)) != 0;
    }

    /** Gets the center MapLocation of a block. */
    public static MapLocation getBlockCenter(int blockX, int blockY) {
        int centerX = blockX * BLOCK_SIZE + BLOCK_SIZE / 2;
        int centerY = blockY * BLOCK_SIZE + BLOCK_SIZE / 2;
        // Clamp to map bounds
        centerX = Math.min(centerX, WIDTH - 1);
        centerY = Math.min(centerY, HEIGHT - 1);
        return new MapLocation(centerX, centerY);
    }

    /**
     * Gets the closest unvisited block's center location for exploration. Loops outward in diamond
     * pattern (Manhattan distance). Randomly picks among blocks at the same distance. Returns
     * random location if all blocks have been visited.
     */
    static Direction exploreDir = null;

    static int exploreDirUntil = 0;

    private static MapLocation projectToEdge(MapLocation loc, Direction dir) {
        int dx = dir.dx;
        int dy = dir.dy;

        int stepsX = Integer.MAX_VALUE;
        int stepsY = Integer.MAX_VALUE;

        if (dx > 0) stepsX = rc.getMapWidth() - 1 - loc.x;
        else if (dx < 0) stepsX = loc.x;

        if (dy > 0) stepsY = rc.getMapHeight() - 1 - loc.y;
        else if (dy < 0) stepsY = loc.y;

        int steps = Math.min(stepsX, stepsY);
        if (steps < 0) steps = 0;

        return loc.translate(dx * steps, dy * steps);
    }

    public static MapLocation getExploreLocation() {
        MapLocation myLoc = rc.getLocation();
        int round = rc.getRoundNum();
        if (exploreDir != null) {
            MapLocation target = projectToEdge(myLoc, exploreDir);
            if (myLoc.distanceSquaredTo(target) <= 20) {
                exploreDir = null;
            }
        }
        if (exploreDir == null || round >= exploreDirUntil) {
            Direction bestDir = null;
            int bestDist = -1;

            for (int i = 0; i < 3; i++) {
                Direction d = Utils.DIRECTIONS[Robot.random.nextInt(8)];
                MapLocation edge = projectToEdge(myLoc, d);
                int dist = myLoc.distanceSquaredTo(edge);

                if (dist > bestDist) {
                    bestDist = dist;
                    bestDir = d;
                }
            }

            exploreDir = bestDir;
            exploreDirUntil = round + ROUNDS_IN_DIR;
        }

        return projectToEdge(myLoc, exploreDir);
    }

    public static void scoutVisible() throws GameActionException {
        if (Clock.getBytecodesLeft() < 200) return;
        nearbyMapInfos = rc.senseNearbyMapInfos();
        for (MapInfo tile : nearbyMapInfos) {
            if (Clock.getBytecodesLeft() < 300) return;
            updateTile(tile);
        }
    }

    private static void updateTile(MapInfo m, MapLocation pos) {
        long xmask = 1L << pos.x;
        seen[pos.y] |= xmask;

        if (m.isWall()) {
            // map[pos.x][pos.y] = TILE_WALL;
            passability[pos.y] |= xmask;

            long maskToUse = pos.x != 0 ? 0b111L << (pos.x - 1) : 0b11L << (pos.x);
            kingPassability[pos.y] |= maskToUse;
            if (pos.y > 0) kingPassability[pos.y - 1] |= maskToUse;
            if (pos.y < HEIGHT - 1) kingPassability[pos.y + 1] |= maskToUse;
        } else {
            // map[pos.x][pos.y] = TILE_EMPTY;
            passabilityStrict[pos.y] &= ~(xmask);

            if (m.isDirt()) isDirt[pos.y] |= xmask;
            else isDirt[pos.y] &= ~xmask;
        }

        if (symmetryQueue.size() < 490) {
            symmetryQueue.add(pos);
        }
        if (m.hasCheeseMine()) {
            MacroData.queueCheeseMine(pos);
        }
    }

    /// Return: whether an update occurred
    public static void updateTile(MapInfo m) {
        MapLocation pos = m.getMapLocation();

        if (rc.canSenseRobotAtLocation(pos)) botOccupied[pos.y] |= 1L << pos.x;

        if (isKnown(pos)) return;
        updateTile(m, pos);
    }

    // TODO: can do some more symmetry elimation with the 5 distance ruin rule
    // Eliminates possible symmetries based on new scouted information
    public static void updateSymmetry() throws GameActionException {
        while (!symmetryQueue.isEmpty()) {
            if (Clock.getBytecodesLeft() < SYMM_BYTECODE) return;
            MapLocation pos = symmetryQueue.poll();

            boolean passable = isPassable(pos);
            if (canSymH) {
                MapLocation nxt = getSymH(pos);
                if (isKnown(nxt) && isPassable(nxt) != passable) {
                    elimSymH();
                }
            }
            if (canSymV) {
                MapLocation nxt = getSymV(pos);
                if (isKnown(nxt) && isPassable(nxt) != passable) {
                    elimSymV();
                }
            }
            if (canSymR) {
                MapLocation nxt = getSymR(pos);
                if (isKnown(nxt) && isPassable(nxt) != passable) {
                    elimSymR();
                }
            }
        }
    }

    public static boolean isPassable(MapLocation loc) {
        return (passability[loc.y] & (1L << loc.x)) == 0;
    }

    public static boolean isPassableKing(MapLocation loc) {
        return (kingPassability[loc.y] & (1L << loc.x)) == 0;
    }

    public static boolean isKnown(MapLocation loc) {
        return (seen[loc.y] & (1L << loc.x)) != 0;
    }

    public static boolean isKnownKing(MapLocation loc) {
        if (loc.y == 0 || loc.x == 0 || loc.y == HEIGHT - 1 || loc.x == WIDTH - 1) return true;

        return ((kingPassability[loc.y] & (111L << (loc.x - 1)))
                        | (kingPassability[loc.y + 1] & (111L << (loc.x - 1)))
                        | (kingPassability[loc.y - 1] & (111L << (loc.x - 1))))
                == 0;
    }

    public static boolean offOrBorderKing(MapLocation loc) {
        return loc.y <= 0 || loc.x <= 0 || loc.y >= HEIGHT - 1 || loc.x >= WIDTH - 1;
    }

    public static boolean knownAndPassable(MapLocation loc) {
        return isKnown(loc) && isPassable(loc);
    }

    // todo make strict (unseen = impassable)
    private static long row9strict(int y, int xcen) {
        if (y < 0 || y >= HEIGHT) return 0x1FF;
        if (xcen < 4) {
            // pad left side with 1s
            return ((passabilityStrict[y] << (4 - xcen)) | Utils.BASIC_MASKS[4 - xcen])
                    & Utils.BASIC_MASKS[9];
        } else if (xcen > WIDTH - 5) {
            // pad right side with 1s
            int xl = xcen - 4;
            return ((passabilityStrict[y] >> xl) | ~Utils.BASIC_MASKS[WIDTH - xl])
                    & Utils.BASIC_MASKS[9];
        } else {
            return (passabilityStrict[y] >> (xcen - 4)) & Utils.BASIC_MASKS[9];
        }
    }

    private static long row9strictDirt(int y, int xcen) {
        if (y < 0 || y >= HEIGHT) return 0x1FF;
        if (xcen < 4) {
            // pad left side with 1s
            return ((passabilityStrict[y] << (4 - xcen))
                            | (isDirt[y] << (4 - xcen))
                            | Utils.BASIC_MASKS[4 - xcen])
                    & Utils.BASIC_MASKS[9];
        } else if (xcen > WIDTH - 5) {
            // pad right side with 1s
            int xl = xcen - 4;
            return ((passabilityStrict[y] >> xl)
                            | (isDirt[y] >> xl)
                            | ~Utils.BASIC_MASKS[WIDTH - xl])
                    & Utils.BASIC_MASKS[9];
        } else {
            return ((passabilityStrict[y] >> (xcen - 4)) | (isDirt[y] << (xcen - 4)))
                    & Utils.BASIC_MASKS[9];
        }
    }

    public static void recalcLocalReach(MapLocation center) {
        lastRecalcTurn = RobotPlayer.round;

        long localMask1 = 0;
        long localMask0 = 1L << 40;
        long mask0 = 0x7FFFFFFFFFFFFFFFL;
        long mask1 = 0x3FFFFL;
        long temp;
        long loverflow = 0x7fbfdfeff7fbfdfeL;
        long roverflow = 0x3fdfeff7fbfdfeffL;

        long passable0 =
                (~((row9strict(center.y + 2, center.x) << 54)
                                        | (row9strict(center.y + 1, center.x) << 45)
                                        | (row9strict(center.y, center.x) << 36)
                                        | (row9strict(center.y - 1, center.x) << 27)
                                        | (row9strict(center.y - 2, center.x) << 18)
                                        | (row9strict(center.y - 3, center.x) << 9)
                                        | row9strict(center.y - 4, center.x))
                                | localMask0)
                        & 0b111111111111111111111111111111111111111111111011111110001111100L;
        long passable1 =
                (~((row9strict(center.y + 4, center.x) << 9) | row9strict(center.y + 3, center.x))
                                | localMask1)
                        & 0b001111100011111110L;
        long mask0Andpassable = mask0 & passable0;
        long mask1Andpassable = mask1 & passable1;

        // Run a BFS
        for (int i = 0; i++ < LOCAL_REACH_ITERS; ) {
            localMask0 =
                    localMask0 | ((localMask0 << 1) & loverflow) | ((localMask0 >>> 1) & roverflow);
            localMask1 =
                    localMask1 | ((localMask1 << 1) & loverflow) | ((localMask1 >>> 1) & roverflow);
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0Andpassable;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1Andpassable;
        }

        localReachCenter = center;
        localReachability =
                new int[] {
                    Math.toIntExact(localMask0 & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 9) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 18) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 27) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 36) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 45) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 54) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact(localMask1 & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask1 >> 9) & (Utils.BASIC_MASKS[9]))
                };
    }

    public static void recalcLocalReachDirt(MapLocation center) {
        lastRecalcTurnDirt = RobotPlayer.round;

        long localMask1 = 0;
        long localMask0 = 1L << 40;
        long mask0 = 0x7FFFFFFFFFFFFFFFL;
        long mask1 = 0x3FFFFL;
        long temp;
        long loverflow = 0x7fbfdfeff7fbfdfeL;
        long roverflow = 0x3fdfeff7fbfdfeffL;

        long passable0 =
                (~((row9strictDirt(center.y + 2, center.x) << 54)
                                        | (row9strictDirt(center.y + 1, center.x) << 45)
                                        | (row9strictDirt(center.y, center.x) << 36)
                                        | (row9strictDirt(center.y - 1, center.x) << 27)
                                        | (row9strictDirt(center.y - 2, center.x) << 18)
                                        | (row9strictDirt(center.y - 3, center.x) << 9)
                                        | row9strictDirt(center.y - 4, center.x))
                                | localMask0)
                        & 0b111111111111111111111111111111111111111111111011111110001111100L;
        long passable1 =
                (~((row9strictDirt(center.y + 4, center.x) << 9)
                                        | row9strictDirt(center.y + 3, center.x))
                                | localMask1)
                        & 0b001111100011111110L;
        long mask0Andpassable = mask0 & passable0;
        long mask1Andpassable = mask1 & passable1;

        // Run a BFS
        for (int i = 0; i++ < LOCAL_REACH_ITERS; ) {
            localMask0 =
                    localMask0 | ((localMask0 << 1) & loverflow) | ((localMask0 >>> 1) & roverflow);
            localMask1 =
                    localMask1 | ((localMask1 << 1) & loverflow) | ((localMask1 >>> 1) & roverflow);
            temp = localMask0;
            localMask0 =
                    (localMask0 | (localMask0 << 9) | (localMask0 >>> 9) | (localMask1 << 54))
                            & mask0Andpassable;
            localMask1 =
                    (localMask1 | (localMask1 << 9) | (localMask1 >>> 9) | (temp >>> 54))
                            & mask1Andpassable;
        }

        localReachCenterDirt = center;
        localReachabilityDirt =
                new int[] {
                    Math.toIntExact(localMask0 & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 9) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 18) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 27) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 36) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 45) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask0 >> 54) & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact(localMask1 & (Utils.BASIC_MASKS[9])),
                    Math.toIntExact((localMask1 >> 9) & (Utils.BASIC_MASKS[9]))
                };
    }

    public static boolean localReachable(MapLocation target) {
        if (target.distanceSquaredTo(localReachCenter) > UnitType.BABY_RAT.visionConeRadiusSquared)
            return false;
        int x = target.x - localReachCenter.x + 4;
        int y = target.y - localReachCenter.y + 4;
        return (localReachability[y] & (1 << x)) != 0;
    }

    public static boolean localReachableDirt(MapLocation target) {
        if (target.distanceSquaredTo(localReachCenterDirt)
                > UnitType.BABY_RAT.visionConeRadiusSquared) return false;
        int x = target.x - localReachCenterDirt.x + 4;
        int y = target.y - localReachCenterDirt.y + 4;
        return (localReachabilityDirt[y] & (1 << x)) != 0;
    }

    public static RobotInfo[] filterLocalReachable(RobotInfo[] toFilter)
            throws GameActionException {
        if (toFilter == null) return null;

        int count = 0;
        unrolled:
        {
            if (toFilter.length == 0) break unrolled;
            if (localReachable(toFilter[0].getLocation())) localBotsTemp[count++] = toFilter[0];
            if (toFilter.length == 1) break unrolled;
            if (localReachable(toFilter[1].getLocation())) localBotsTemp[count++] = toFilter[1];
            if (toFilter.length == 2) break unrolled;
            if (localReachable(toFilter[2].getLocation())) localBotsTemp[count++] = toFilter[2];
            if (toFilter.length == 3) break unrolled;
            if (localReachable(toFilter[3].getLocation())) localBotsTemp[count++] = toFilter[3];
            if (toFilter.length == 4) break unrolled;
            if (localReachable(toFilter[4].getLocation())) localBotsTemp[count++] = toFilter[4];
            if (toFilter.length == 5) break unrolled;
            if (localReachable(toFilter[5].getLocation())) localBotsTemp[count++] = toFilter[5];
            if (toFilter.length == 6) break unrolled;
            if (localReachable(toFilter[6].getLocation())) localBotsTemp[count++] = toFilter[6];
            if (toFilter.length == 7) break unrolled;
            if (localReachable(toFilter[7].getLocation())) localBotsTemp[count++] = toFilter[7];
            if (toFilter.length == 8) break unrolled;
            if (localReachable(toFilter[8].getLocation())) localBotsTemp[count++] = toFilter[8];
            if (toFilter.length == 9) break unrolled;
            if (localReachable(toFilter[9].getLocation())) localBotsTemp[count++] = toFilter[9];
            if (toFilter.length == 10) break unrolled;
            if (localReachable(toFilter[10].getLocation())) localBotsTemp[count++] = toFilter[10];
            if (toFilter.length == 11) break unrolled;
            if (localReachable(toFilter[11].getLocation())) localBotsTemp[count++] = toFilter[11];
            if (toFilter.length == 12) break unrolled;
            if (localReachable(toFilter[12].getLocation())) localBotsTemp[count++] = toFilter[12];
            if (toFilter.length == 13) break unrolled;
            if (localReachable(toFilter[13].getLocation())) localBotsTemp[count++] = toFilter[13];
            if (toFilter.length == 14) break unrolled;
            if (localReachable(toFilter[14].getLocation())) localBotsTemp[count++] = toFilter[14];
            if (toFilter.length == 15) break unrolled;
            if (localReachable(toFilter[15].getLocation())) localBotsTemp[count++] = toFilter[15];
            if (toFilter.length == 16) break unrolled;
            if (localReachable(toFilter[16].getLocation())) localBotsTemp[count++] = toFilter[16];
            if (toFilter.length == 17) break unrolled;
            if (localReachable(toFilter[17].getLocation())) localBotsTemp[count++] = toFilter[17];
            if (toFilter.length == 18) break unrolled;
            if (localReachable(toFilter[18].getLocation())) localBotsTemp[count++] = toFilter[18];
            if (toFilter.length == 19) break unrolled;
            if (localReachable(toFilter[19].getLocation())) localBotsTemp[count++] = toFilter[19];
            if (toFilter.length == 20) break unrolled;
            if (localReachable(toFilter[20].getLocation())) localBotsTemp[count++] = toFilter[20];
        }
        RobotInfo[] result = new RobotInfo[count];
        unrolled2:
        {
            if (count == 0) break unrolled2;
            result[0] = localBotsTemp[0];
            if (count == 1) break unrolled2;
            result[1] = localBotsTemp[1];
            if (count == 2) break unrolled2;
            result[2] = localBotsTemp[2];
            if (count == 3) break unrolled2;
            result[3] = localBotsTemp[3];
            if (count == 4) break unrolled2;
            result[4] = localBotsTemp[4];
            if (count == 5) break unrolled2;
            result[5] = localBotsTemp[5];
            if (count == 6) break unrolled2;
            result[6] = localBotsTemp[6];
            if (count == 7) break unrolled2;
            result[7] = localBotsTemp[7];
            if (count == 8) break unrolled2;
            result[8] = localBotsTemp[8];
            if (count == 9) break unrolled2;
            result[9] = localBotsTemp[9];
            if (count == 10) break unrolled2;
            result[10] = localBotsTemp[10];
            if (count == 11) break unrolled2;
            result[11] = localBotsTemp[11];
            if (count == 12) break unrolled2;
            result[12] = localBotsTemp[12];
            if (count == 13) break unrolled2;
            result[13] = localBotsTemp[13];
            if (count == 14) break unrolled2;
            result[14] = localBotsTemp[14];
            if (count == 15) break unrolled2;
            result[15] = localBotsTemp[15];
            if (count == 16) break unrolled2;
            result[16] = localBotsTemp[16];
            if (count == 17) break unrolled2;
            result[17] = localBotsTemp[17];
            if (count == 18) break unrolled2;
            result[18] = localBotsTemp[18];
            if (count == 19) break unrolled2;
            result[19] = localBotsTemp[19];
            if (count == 20) break unrolled2;
            result[20] = localBotsTemp[20];
        }
        return result;
    }

    public static RobotInfo[] filterLocalReachableDirt(RobotInfo[] toFilter)
            throws GameActionException {
        if (toFilter == null) return null;

        int count = 0;
        unrolled:
        {
            if (toFilter.length == 0) break unrolled;
            if (localReachableDirt(toFilter[0].getLocation())) localBotsTemp[count++] = toFilter[0];
            if (toFilter.length == 1) break unrolled;
            if (localReachableDirt(toFilter[1].getLocation())) localBotsTemp[count++] = toFilter[1];
            if (toFilter.length == 2) break unrolled;
            if (localReachableDirt(toFilter[2].getLocation())) localBotsTemp[count++] = toFilter[2];
            if (toFilter.length == 3) break unrolled;
            if (localReachableDirt(toFilter[3].getLocation())) localBotsTemp[count++] = toFilter[3];
            if (toFilter.length == 4) break unrolled;
            if (localReachableDirt(toFilter[4].getLocation())) localBotsTemp[count++] = toFilter[4];
            if (toFilter.length == 5) break unrolled;
            if (localReachableDirt(toFilter[5].getLocation())) localBotsTemp[count++] = toFilter[5];
            if (toFilter.length == 6) break unrolled;
            if (localReachableDirt(toFilter[6].getLocation())) localBotsTemp[count++] = toFilter[6];
            if (toFilter.length == 7) break unrolled;
            if (localReachableDirt(toFilter[7].getLocation())) localBotsTemp[count++] = toFilter[7];
            if (toFilter.length == 8) break unrolled;
            if (localReachableDirt(toFilter[8].getLocation())) localBotsTemp[count++] = toFilter[8];
            if (toFilter.length == 9) break unrolled;
            if (localReachableDirt(toFilter[9].getLocation())) localBotsTemp[count++] = toFilter[9];
            if (toFilter.length == 10) break unrolled;
            if (localReachableDirt(toFilter[10].getLocation()))
                localBotsTemp[count++] = toFilter[10];
            if (toFilter.length == 11) break unrolled;
            if (localReachableDirt(toFilter[11].getLocation()))
                localBotsTemp[count++] = toFilter[11];
            if (toFilter.length == 12) break unrolled;
            if (localReachableDirt(toFilter[12].getLocation()))
                localBotsTemp[count++] = toFilter[12];
            if (toFilter.length == 13) break unrolled;
            if (localReachableDirt(toFilter[13].getLocation()))
                localBotsTemp[count++] = toFilter[13];
            if (toFilter.length == 14) break unrolled;
            if (localReachableDirt(toFilter[14].getLocation()))
                localBotsTemp[count++] = toFilter[14];
            if (toFilter.length == 15) break unrolled;
            if (localReachableDirt(toFilter[15].getLocation()))
                localBotsTemp[count++] = toFilter[15];
            if (toFilter.length == 16) break unrolled;
            if (localReachableDirt(toFilter[16].getLocation()))
                localBotsTemp[count++] = toFilter[16];
            if (toFilter.length == 17) break unrolled;
            if (localReachableDirt(toFilter[17].getLocation()))
                localBotsTemp[count++] = toFilter[17];
            if (toFilter.length == 18) break unrolled;
            if (localReachableDirt(toFilter[18].getLocation()))
                localBotsTemp[count++] = toFilter[18];
            if (toFilter.length == 19) break unrolled;
            if (localReachableDirt(toFilter[19].getLocation()))
                localBotsTemp[count++] = toFilter[19];
            if (toFilter.length == 20) break unrolled;
            if (localReachableDirt(toFilter[20].getLocation()))
                localBotsTemp[count++] = toFilter[20];
        }
        RobotInfo[] result = new RobotInfo[count];
        unrolled2:
        {
            if (count == 0) break unrolled2;
            result[0] = localBotsTemp[0];
            if (count == 1) break unrolled2;
            result[1] = localBotsTemp[1];
            if (count == 2) break unrolled2;
            result[2] = localBotsTemp[2];
            if (count == 3) break unrolled2;
            result[3] = localBotsTemp[3];
            if (count == 4) break unrolled2;
            result[4] = localBotsTemp[4];
            if (count == 5) break unrolled2;
            result[5] = localBotsTemp[5];
            if (count == 6) break unrolled2;
            result[6] = localBotsTemp[6];
            if (count == 7) break unrolled2;
            result[7] = localBotsTemp[7];
            if (count == 8) break unrolled2;
            result[8] = localBotsTemp[8];
            if (count == 9) break unrolled2;
            result[9] = localBotsTemp[9];
            if (count == 10) break unrolled2;
            result[10] = localBotsTemp[10];
            if (count == 11) break unrolled2;
            result[11] = localBotsTemp[11];
            if (count == 12) break unrolled2;
            result[12] = localBotsTemp[12];
            if (count == 13) break unrolled2;
            result[13] = localBotsTemp[13];
            if (count == 14) break unrolled2;
            result[14] = localBotsTemp[14];
            if (count == 15) break unrolled2;
            result[15] = localBotsTemp[15];
            if (count == 16) break unrolled2;
            result[16] = localBotsTemp[16];
            if (count == 17) break unrolled2;
            result[17] = localBotsTemp[17];
            if (count == 18) break unrolled2;
            result[18] = localBotsTemp[18];
            if (count == 19) break unrolled2;
            result[19] = localBotsTemp[19];
            if (count == 20) break unrolled2;
            result[20] = localBotsTemp[20];
        }
        return result;
    }
}
