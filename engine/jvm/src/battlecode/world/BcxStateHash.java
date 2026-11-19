package battlecode.world;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Team;
import battlecode.common.TrapType;
import battlecode.common.UnitType;
import gnu.trove.TObjectProcedure;

import java.util.ArrayList;
import java.util.Random;

/**
 * Canonical state of a GameWorld after a round: a 64-bit hash, and the same content as text, one line per item.
 * In package battlecode.world to read package-private members. Plain Java only (translated to C++).
 *
 * Content, in this order (null = -1 for ids, ordinals and coordinates):
 *  - round: currentRound, running, winner (not when the domination factor is WON_BY_DUBIOUS_REASONS: a coin
 *    flip), domination factor.
 *  - world: cooperation, backstabber, backstab round (derived: round - getRoundsSinceBackstab()), numCats, map seed,
 *    map width/height/origin/symmetry/rounds/name.
 *  - team A, B: global cheese, cheese transferred, dirt, baby rats, rat kings, cat damage, damage suffered, points,
 *    round cheese change (gives oldCheeseCounts), rat and cat trap counts, ObjectInfo robot count and robot type
 *    counts; team NEUTRAL: robot count and type counts.
 *  - shared arrays of A and B.
 *  - rng: GameWorld.rand and InternalRobot.rand (the static cat RNG), as their 48-bit LCG states. java.util.Random's
 *    seed is private, so rngState() draws three ints, solves the LCG for the state and restores it with setSeed
 *    (exact: the engine never calls nextGaussian on these, so haveNextNextGaussian is always false).
 *  - exec: robot ids in execution order (ObjectInfo.eachDynamicBodyByExecOrder); order: ids in ObjectInfo.eachRobot
 *    order (the trove hash map's iteration order, used by processBeginningOfRound).
 *  - robot, in execution order: id, team, type, location, direction, chirality, died location, health, raw cheese,
 *    control bits, bytecode limit, bytecodes used, rounds alive, action/movement/turning cooldowns, carried robot id,
 *    remaining carried duration, carrier id, being thrown, turns since thrown or dropped, last grabber id, messages
 *    sent this turn, has traveled this round; then one squeak line per queued message (sender, round, bytes, source).
 *  - tile, every tile in index order (GameWorld's accessors): wall, dirt, cheese, cheese mine, robot id, flying
 *    robot id, team A and team B trap (id, team, type, location), the tile's trap trigger list (each trap, in
 *    list order).
 *    The dump prints only tiles with something on them; the hash covers every tile.
 *  - cat: per cat waypoint list of the map (id, locations).
 *  - bfs (round 1 only; the tables are built once in the GameWorld constructor): one sub-hash over
 *    getBfsDir(from, to, chirality) for every pair of tiles and both chiralities.
 *
 * Not reachable without reflection (private, no getter), so not covered:
 *  - InternalRobot: thrownDir (only isBeingThrown()), remainingThrowDuration, sleepTimeRemaining, catState,
 *    catWaypoints (the map's waypoints are covered), currentWaypoint, previousWaypoint, catTargetLoc, catTarget,
 *    catTurns, catTurnsStuck, indicatorString, cachedRobotInfo (getRobotInfo() would refresh the cache, so it is
 *    not called).
 *  - GameWorld: cheeseMines and each CheeseMine's last spawn round and pair, hasRunCheeseMinesThisRound (false
 *    after every round), currentNumberUnits, profilerCollections; hasTraveledIDs only by probing robot ids.
 *  - TeamInfo.cheeseCollected; ObjectInfo.catHealthByID; IDGenerator (its Random, cursor and shuffled block).
 *  - Per-robot player state: the sandbox, its statics and heap, and RobotMonitor's bytecode debt.
 *
 * The hash: every value is absorbed as a long by a multiply-rotate mixer (absorb), each line starts with a tag and
 * each list with its length; finish() xors the count and applies the splitmix64 finalizer. Labels are not hashed.
 */
public final class BcxStateHash {
    private static final long LCG_MUL = 0x5DEECE66DL;
    private static final long LCG_ADD = 0xBL;
    private static final long LCG_MASK = (1L << 48) - 1;
    // inverse of LCG_MUL modulo 2^48
    private static final long LCG_MUL_INV = 0xDFE05BCB1365L;

    private long state;
    private long count;
    private final StringBuilder sb;

    private BcxStateHash(boolean dump) {
        this.state = 0x6A09E667F3BCC909L;
        this.count = 0;
        this.sb = dump ? new StringBuilder() : null;
    }

    /** The state hash of the world after a round. */
    public static long hash(GameWorld w) {
        BcxStateHash h = new BcxStateHash(false);
        h.walk(w);
        return h.finish();
    }

    /** The same content as hash(w), as text, one line per item. */
    public static String dump(GameWorld w) {
        BcxStateHash h = new BcxStateHash(true);
        h.walk(w);
        return h.sb.toString();
    }

    /** A hasher over a stream of values, for chaining round hashes. */
    public static BcxStateHash chain() {
        return new BcxStateHash(false);
    }

    public void add(long v) {
        absorb(v);
    }

    public long finish() {
        long x = state ^ count;
        x ^= x >>> 30;
        x *= 0xBF58476D1CE4E5B9L;
        x ^= x >>> 27;
        x *= 0x94D049BB133111EBL;
        x ^= x >>> 31;
        return x;
    }

    public static String hex16(long v) {
        char[] c = new char[16];
        for (int i = 15; i >= 0; i--) {
            int d = (int) (v & 15);
            c[i] = (char) (d < 10 ? '0' + d : 'a' + d - 10);
            v >>>= 4;
        }
        return new String(c);
    }

    private void absorb(long v) {
        long k = v * 0x9E3779B97F4A7C15L;
        k ^= k >>> 32;
        long s = state ^ k;
        s = (s << 27) | (s >>> 37);
        state = s * 0xC2B2AE3D27D4EB4FL + 0x165667B19E3779F9L;
        count++;
    }

    // ---- emitters: one line = tag, then key/value pairs ----

    private void line(int tag, String name) {
        absorb(tag);
        if (sb != null) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(name);
        }
    }

    private void v(String key, long x) {
        absorb(x);
        if (sb != null) sb.append(' ').append(key).append('=').append(x);
    }

    private void v(String key, boolean x) {
        absorb(x ? 1 : 0);
        if (sb != null) sb.append(' ').append(key).append('=').append(x ? 1 : 0);
    }

    private void v(String key, Team t) {
        absorb(t == null ? -1 : t.ordinal());
        if (sb != null) sb.append(' ').append(key).append('=').append(t == null ? "-" : t.name());
    }

    private void v(String key, UnitType t) {
        absorb(t == null ? -1 : t.ordinal());
        if (sb != null) sb.append(' ').append(key).append('=').append(t == null ? "-" : t.name());
    }

    private void v(String key, TrapType t) {
        absorb(t == null ? -1 : t.ordinal());
        if (sb != null) sb.append(' ').append(key).append('=').append(t == null ? "-" : t.name());
    }

    private void v(String key, Direction d) {
        absorb(d == null ? -1 : d.ordinal());
        if (sb != null) sb.append(' ').append(key).append('=').append(d == null ? "-" : d.name());
    }

    private void v(String key, DominationFactor d) {
        absorb(d == null ? -1 : d.ordinal());
        if (sb != null) sb.append(' ').append(key).append('=').append(d == null ? "-" : d.name());
    }

    private void v(String key, MapLocation m) {
        absorb(m == null ? -1 : m.x);
        absorb(m == null ? -1 : m.y);
        if (sb != null) {
            sb.append(' ').append(key).append('=');
            if (m == null) sb.append('-');
            else sb.append(m.x).append(',').append(m.y);
        }
    }

    private void v(String key, String s) {
        absorb(s == null ? -1 : s.length());
        if (s != null) {
            for (int i = 0; i < s.length(); i++) absorb(s.charAt(i));
        }
        if (sb != null) sb.append(' ').append(key).append('=').append(s == null ? "-" : s);
    }

    private void vid(String key, InternalRobot r) {
        v(key, r == null ? -1 : r.getID());
    }

    // ---- the walk ----

    private static final class Collect implements TObjectProcedure<InternalRobot> {
        final ArrayList<InternalRobot> list = new ArrayList<InternalRobot>();

        public boolean execute(InternalRobot r) {
            list.add(r);
            return true;
        }
    }

    private void walk(GameWorld w) {
        int round = w.currentRound;
        LiveMap map = w.getGameMap();
        TeamInfo ti = w.getTeamInfo();
        ObjectInfo oi = w.getObjectInfo();

        line(1, "round");
        v("round", round);
        v("running", w.running);
        DominationFactor dom = w.gameStats.getDominationFactor();
        if (dom == DominationFactor.WON_BY_DUBIOUS_REASONS) {
            // the coin flip (unseeded Math.random) is not part of the state
            absorb(-2);
            if (sb != null) sb.append(" winner=coin_flip");
        } else {
            v("winner", w.gameStats.getWinner());
        }
        v("win_type", dom);

        line(2, "world");
        boolean coop = w.isCooperation();
        v("coop", coop);
        v("backstabber", w.getBackStabbingTeam());
        v("backstab_round", coop ? -1 : round - w.getRoundsSinceBackstab());
        v("cats", w.getNumCats());
        v("seed", w.getMapSeed());
        v("width", map.getWidth());
        v("height", map.getHeight());
        v("origin", map.getOrigin());
        v("symmetry", map.getSymmetry().ordinal());
        v("rounds", map.getRounds());
        v("name", map.getMapName());

        Team[] teams = {Team.A, Team.B, Team.NEUTRAL};
        UnitType[] types = UnitType.values();
        for (int t = 0; t < 3; t++) {
            Team team = teams[t];
            line(3, "team");
            v("team", team);
            if (team != Team.NEUTRAL) {
                v("cheese", ti.getCheese(team));
                v("transferred", ti.getCheeseTransferred(team));
                v("dirt", ti.getDirt(team));
                v("baby_rats", ti.getNumBabyRats(team));
                v("rat_kings", ti.getNumRatKings(team));
                v("cat_damage", ti.getDamageToCats(team));
                v("damage_suffered", ti.getDamageSuffered(team));
                v("points", ti.getPoints(team));
                v("cheese_change", ti.getRoundCheeseChange(team));
                v("rat_traps", w.getTrapCount(TrapType.RAT_TRAP, team));
                v("cat_traps", w.getTrapCount(TrapType.CAT_TRAP, team));
            }
            v("robots", oi.getRobotCount(team));
            for (int u = 0; u < types.length; u++) {
                v(types[u].name(), oi.getRobotTypeCount(team, types[u]));
            }
        }

        for (int t = 0; t < 2; t++) {
            line(4, "shared");
            v("team", teams[t]);
            if (sb != null) sb.append(" values=");
            for (int i = 0; i < GameConstants.SHARED_ARRAY_SIZE; i++) {
                int x = w.readSharedArray(i, teams[t]);
                absorb(x);
                if (sb != null) {
                    if (i > 0) sb.append(',');
                    sb.append(x);
                }
            }
        }

        line(5, "rng");
        v("world", rngState(w.rand));
        v("cat", rngState(InternalRobot.rand));

        Collect exec = new Collect();
        oi.eachDynamicBodyByExecOrder(exec);
        Collect order = new Collect();
        oi.eachRobot(order);
        idList(6, "exec", exec.list);
        idList(7, "order", order.list);

        for (int i = 0; i < exec.list.size(); i++) {
            robot(w, exec.list.get(i));
        }

        tiles(w, map);

        ArrayList<Integer> catIds = map.getCatWaypointIDs();
        ArrayList<int[]> catWaypoints = map.getCatWaypoints();
        for (int i = 0; i < catIds.size(); i++) {
            line(10, "cat");
            v("id", catIds.get(i).intValue());
            int[] wp = catWaypoints.get(i);
            v("n", wp.length);
            if (sb != null) sb.append(" waypoints=");
            for (int j = 0; j < wp.length; j++) {
                absorb(wp[j]);
                if (sb != null) {
                    if (j > 0) sb.append(',');
                    sb.append(wp[j]);
                }
            }
        }

        if (round == 1) {
            line(11, "bfs");
            v("hash", bfsHash(w, map));
        }
    }

    private void idList(int tag, String name, ArrayList<InternalRobot> list) {
        line(tag, name);
        v("n", list.size());
        if (sb != null) sb.append(" ids=");
        for (int i = 0; i < list.size(); i++) {
            int id = list.get(i).getID();
            absorb(id);
            if (sb != null) {
                if (i > 0) sb.append(',');
                sb.append(id);
            }
        }
    }

    private void robot(GameWorld w, InternalRobot r) {
        line(8, "robot");
        v("id", r.getID());
        v("team", r.getTeam());
        v("type", r.getType());
        v("loc", r.getLocation());
        v("dir", r.getDirection());
        v("chirality", r.getChirality());
        v("died_loc", r.getDiedLocation());
        v("health", r.getHealth());
        v("cheese", r.getCheese());
        v("control_bits", r.getControlBits());
        v("bc_limit", r.getBytecodeLimit());
        v("bc_used", r.getBytecodesUsed());
        v("rounds_alive", r.getRoundsAlive());
        v("action_cd", r.getActionCooldownTurns());
        v("move_cd", r.getMovementCooldownTurns());
        v("turn_cd", r.getTurningCooldownTurns());
        vid("carrying", r.getRobotBeingCarried());
        v("carried_left", r.getRemainingCarriedDuration());
        vid("carrier", r.getGrabbedByRobot());
        v("thrown", r.isBeingThrown());
        v("since_thrown", r.getTurnsSinceThrownOrDropped());
        v("last_grabber", r.getLastGrabberId());
        v("sent", r.getSentMessagesCount());
        v("traveled", w.getHasTraveledRobot(r.getID()));
        Message[] msgs = r.getMessages();
        v("squeaks", msgs.length);
        for (int i = 0; i < msgs.length; i++) {
            Message m = msgs[i];
            line(9, "squeak");
            v("to", r.getID());
            v("sender", m.getSenderID());
            v("round", m.getRound());
            v("bytes", m.getBytes());
            v("source", m.getSource());
        }
    }

    private void tiles(GameWorld w, LiveMap map) {
        int width = map.getWidth();
        int n = width * map.getHeight();
        MapLocation origin = map.getOrigin();
        for (int i = 0; i < n; i++) {
            MapLocation loc = new MapLocation(i % width + origin.x, i / width + origin.y);
            boolean wall = w.getWall(loc);
            boolean dirt = w.getDirt(loc);
            int cheese = w.getCheese(loc);
            boolean mine = w.hasCheeseMine(loc);
            InternalRobot robot = w.getRobot(loc);
            InternalRobot flying = w.getFlyingRobot(loc);
            Trap trapA = w.getTrap(loc, Team.A);
            Trap trapB = w.getTrap(loc, Team.B);
            ArrayList<Trap> triggers = w.getTrapTriggers(loc);
            boolean show = wall || dirt || cheese != 0 || mine || robot != null || flying != null
                    || trapA != null || trapB != null || triggers.size() > 0;
            // an empty tile is hashed but not printed
            int mark = sb == null ? 0 : sb.length();
            line(12, "tile");
            v("i", i);
            v("x", loc.x);
            v("y", loc.y);
            v("wall", wall);
            v("dirt", dirt);
            v("cheese", cheese);
            v("mine", mine);
            vid("robot", robot);
            vid("flying", flying);
            trap("trap_a", trapA);
            trap("trap_b", trapB);
            v("triggers", triggers.size());
            for (int j = 0; j < triggers.size(); j++) {
                trap("trigger", triggers.get(j));
            }
            if (sb != null && !show) {
                sb.setLength(mark);
            }
        }
    }

    private void trap(String key, Trap t) {
        if (t == null) {
            absorb(-1);
            if (sb != null) sb.append(' ').append(key).append("=-");
            return;
        }
        absorb(t.getId());
        absorb(t.getTeam().ordinal());
        absorb(t.getType().ordinal());
        MapLocation loc = t.getLocation();
        absorb(loc.x);
        absorb(loc.y);
        if (sb != null) {
            sb.append(' ').append(key).append('=').append(t.getId()).append('/').append(t.getTeam().name())
                    .append('/').append(t.getType().name()).append('/').append(loc.x).append(',').append(loc.y);
        }
    }

    private static long bfsHash(GameWorld w, LiveMap map) {
        int width = map.getWidth();
        int n = width * map.getHeight();
        MapLocation origin = map.getOrigin();
        MapLocation[] locs = new MapLocation[n];
        for (int i = 0; i < n; i++) {
            locs[i] = new MapLocation(i % width + origin.x, i / width + origin.y);
        }
        BcxStateHash h = new BcxStateHash(false);
        for (int c = 0; c < 2; c++) {
            for (int from = 0; from < n; from++) {
                for (int to = 0; to < n; to++) {
                    Direction d = w.getBfsDir(locs[from], locs[to], c);
                    h.absorb(d == null ? -1 : d.ordinal());
                }
            }
        }
        return h.finish();
    }

    /**
     * The 48-bit state of a java.util.Random, left unchanged: draws three ints, finds the state before the second
     * draw (16 unknown low bits, checked against the next two draws), steps back one draw, restores it.
     */
    public static long rngState(Random r) {
        int a = r.nextInt();
        int b = r.nextInt();
        int c = r.nextInt();
        long hi = ((long) a & 0xFFFFFFFFL) << 16;
        long found = -1;
        int matches = 0;
        for (long lo = 0; lo < 65536; lo++) {
            long s1 = hi | lo;
            long s2 = (s1 * LCG_MUL + LCG_ADD) & LCG_MASK;
            if ((int) (s2 >>> 16) != b) continue;
            long s3 = (s2 * LCG_MUL + LCG_ADD) & LCG_MASK;
            if ((int) (s3 >>> 16) != c) continue;
            found = s1;
            matches++;
        }
        if (matches != 1) {
            throw new IllegalStateException("BcxStateHash: cannot solve the Random state (" + matches + " matches)");
        }
        long s0 = ((found - LCG_ADD) * LCG_MUL_INV) & LCG_MASK;
        r.setSeed(s0 ^ LCG_MUL);
        return s0;
    }
}
