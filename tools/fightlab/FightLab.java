package fightlab;

import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.common.UnitType;
import battlecode.crossplay.CrossPlay;
import battlecode.crossplay.CrossPlayLanguage;
import battlecode.server.Config;
import battlecode.server.GameInfo;
import battlecode.server.GameMaker;
import battlecode.server.GameState;
import battlecode.world.GameMapIO;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;
import battlecode.world.LiveMap;
import battlecode.world.TeamInfo;
import battlecode.world.control.NullControlProvider;
import battlecode.world.control.PlayerControlProvider;
import battlecode.world.control.TeamControlProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * The fight lab's game runner (docs/theory/fl-fight-lab.md): plays games on the unmodified official engine, one after
 * another in this JVM, each as bcplay.Play plays it (the cat RNG reset before each game), and reads the world while it
 * runs.
 *
 * {@code java -cp CLASSES:ENGINE_JAR fightlab.FightLab JOBS.jsonl OUT.jsonl [--bots DIR] [--maps DIR]}
 *
 * A job is one JSON object a line (flat; strings, ints and int lists only):
 *   id, a, b (packages), map, seed;
 *   r0, set: the properties bc.testing.fl.r0 and bc.testing.fl.set (tools/fightlab/mkcopy.py), absent: none;
 *   marks: rounds at whose end both teams' state is recorded; stop: the last round played (0: to the end);
 *   scan: 1 to record every engagement start (below) with its state;
 *   cx, cy: a point whose surroundings (Chebyshev 8) the marks also count.
 * A result line copies the job and adds the outcome (winner, win_type, round, when the game ended), mark states and
 * the scan. A team's state is [babies, baby HP, kings, king HP, global cheese, raw cheese, cheese transferred, HP lost,
 * cat damage, rat traps, dirt, (with cx: babies, baby HP, kings in the box)].
 *
 * An approach is an enemy baby rat within Chebyshev 4 of one of A's (5 from a king's centre), at the point between
 * them. The scan records a round's approach when none was within Chebyshev 8 in the 10 rounds before (a start), or
 * when no record is within Chebyshev 8 in the 30 rounds before (a sample of a longer fight). It reads only the state
 * at the end of that round, so a fork from the next round is chosen without knowing how the fight goes. A record:
 * [round, x, y, A babies within 5, B babies within 5, their HP, A kings within 8, B kings within 8, A and B global
 * cheese, A babies, B babies, start (1) or sample (0)].
 */
public final class FightLab {
    static final long CAT_RNG_SEED = 1092;
    static final com.sun.management.OperatingSystemMXBean OS =
            (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();

    public static void main(String[] args) throws Exception {
        String jobs = args[0], out = args[1], bots = "build/classes", maps = "maps";
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("--bots")) bots = args[++i];
            else if (args[i].equals("--maps")) maps = args[++i];
        }
        PrintStream results = new PrintStream(new FileOutputStream(out, true), true, StandardCharsets.UTF_8);
        PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8);
        System.setOut(console);
        Config options = new Config(new String[0]);
        Config.setGlobalConfig(options);
        try (BufferedReader in = Files.newBufferedReader(Path.of(jobs))) {
            for (String line; (line = in.readLine()) != null; ) {
                if (line.isBlank()) continue;
                Json job = Json.parse(line);
                String res;
                try {
                    res = play(job, options, bots, maps);
                } catch (Throwable t) {
                    t.printStackTrace(console);
                    res = job.with("error", Json.quote(t.toString()));
                }
                results.println(res);
            }
        }
        console.flush();
        System.exit(0); // robot threads must not keep the JVM alive
    }

    static String play(Json job, Config options, String bots, String maps) throws Exception {
        long t0 = System.nanoTime(), cpu0 = OS.getProcessCpuTime();
        InternalRobot.rand = new Random(CAT_RNG_SEED);
        String a = job.str("a"), b = job.str("b"), mapName = job.str("map");
        options.set("bc.testing.fl.r0", job.has("r0") ? String.valueOf(job.num("r0")) : String.valueOf(Integer.MAX_VALUE));
        options.set("bc.testing.fl.set", job.has("set") ? job.str("set") : "");
        int[] marks = job.has("marks") ? job.nums("marks") : new int[0];
        int stop = job.has("stop") ? job.num("stop") : 0;
        boolean scan = job.has("scan") && job.num("scan") != 0;
        boolean box = job.has("cx");
        int cx = box ? job.num("cx") : 0, cy = box ? job.num("cy") : 0;

        String url = new File(bots).getAbsolutePath();
        GameInfo info = new GameInfo(a, CrossPlayLanguage.JAVA, a, url, b, CrossPlayLanguage.JAVA, b, url,
                new String[]{mapName}, null, false);
        GameMaker gameMaker = new GameMaker(info, null, false);
        gameMaker.makeGameHeader();
        CrossPlay crossPlay = new CrossPlay();
        TeamControlProvider prov = new TeamControlProvider();
        OutputStream robotOut = gameMaker.getMatchMaker().getOut();
        prov.registerControlProvider(Team.A, new PlayerControlProvider(Team.A, a, CrossPlayLanguage.JAVA, crossPlay, url, robotOut, false));
        prov.registerControlProvider(Team.B, new PlayerControlProvider(Team.B, b, CrossPlayLanguage.JAVA, crossPlay, url, robotOut, false));
        prov.registerControlProvider(Team.NEUTRAL, new NullControlProvider());

        LiveMap m = GameMapIO.loadMap(mapName, new File(maps), false);
        if (job.has("seed")) {
            m = new LiveMap(m.getWidth(), m.getHeight(), m.getOrigin(), job.num("seed"), m.getRounds(), m.getMapName(),
                    m.getSymmetry(), m.getWallArray(), m.getDirtArray(), m.getCheeseMineArray(), m.getCheeseArray(),
                    m.getCatWaypointIDs(), m.getCatWaypoints(), m.getInitialBodies());
        }
        GameWorld world = new GameWorld(m, prov, gameMaker.getMatchMaker());
        StringBuilder markOut = new StringBuilder();
        Scan scanner = scan ? new Scan() : null;
        int mi = 0;
        boolean done = false;
        try {
            while (true) {
                if (world.runRound() == GameState.DONE) { done = true; break; }
                int r = world.getCurrentRound();
                if (scanner != null) scanner.round(world, r);
                while (mi < marks.length && marks[mi] <= r) {
                    if (marks[mi] == r) {
                        markOut.append(markOut.length() > 0 ? "," : "").append("{\"r\":").append(r)
                                .append(",\"A\":").append(state(world, Team.A, box, cx, cy))
                                .append(",\"B\":").append(state(world, Team.B, box, cx, cy)).append('}');
                    }
                    mi++;
                }
                if (stop > 0 && r >= stop) break;
            }
        } finally {
            if (!done) prov.matchEnded();
            crossPlay.cleanup();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\"done\":").append(done);
        sb.append(",\"round\":").append(world.getCurrentRound());
        if (done) {
            Team w = world.getWinner();
            sb.append(",\"winner\":").append(w == null ? "null" : "\"" + w.name() + "\"");
            sb.append(",\"win_type\":\"").append(world.getGameStats().getDominationFactor().name()).append('"');
        }
        sb.append(",\"end\":{\"A\":").append(state(world, Team.A, box, cx, cy)).append(",\"B\":")
                .append(state(world, Team.B, box, cx, cy)).append('}');
        sb.append(",\"marks\":[").append(markOut).append(']');
        if (scanner != null) sb.append(",\"scan\":[").append(scanner.out).append(']');
        sb.append(",\"cpu_s\":").append(String.format(Locale.ROOT, "%.2f", (OS.getProcessCpuTime() - cpu0) / 1e9));
        sb.append(",\"wall_ms\":").append((System.nanoTime() - t0) / 1_000_000);
        return job.with(sb.toString());
    }

    /** A team's state (see the class comment). */
    static String state(GameWorld world, Team t, boolean box, int cx, int cy) {
        long[] s = new long[14];
        world.getObjectInfo().eachRobot(r -> {
            if (r.getTeam() != t || r.getLocation() == null) return true;
            MapLocation l = r.getLocation();
            boolean in = box && Math.max(Math.abs(l.x - cx), Math.abs(l.y - cy)) <= 8;
            if (r.getType() == UnitType.BABY_RAT) {
                s[0]++; s[1] += r.getHealth(); s[5] += r.getCheese();
                if (in) { s[11]++; s[12] += r.getHealth(); }
            } else if (r.getType() == UnitType.RAT_KING) {
                s[2]++; s[3] += r.getHealth();
                if (in) s[13]++;
            }
            return true;
        });
        TeamInfo ti = world.getTeamInfo();
        s[4] = ti.getCheese(t); s[6] = ti.getCheeseTransferred(t); s[7] = ti.getDamageSuffered(t);
        s[8] = ti.getDamageToCats(t); s[9] = ratTraps(world, t); s[10] = ti.getDirt(t);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < (box ? 14 : 11); i++) sb.append(i > 0 ? "," : "").append(s[i]);
        return sb.append(']').toString();
    }

    static java.lang.reflect.Field TRAPS;

    @SuppressWarnings("unchecked")
    static int ratTraps(GameWorld world, Team t) {
        try {
            if (TRAPS == null) {
                TRAPS = GameWorld.class.getDeclaredField("trapCounts");
                TRAPS.setAccessible(true);
            }
            return ((java.util.HashMap<battlecode.common.TrapType, int[]>) TRAPS.get(world)).get(battlecode.common.TrapType.RAT_TRAP)[t.ordinal()];
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /** Engagement starts and samples (see the class comment). */
    static final class Scan {
        final StringBuilder out = new StringBuilder();
        final List<int[]> recent = new ArrayList<>(); // [round, x, y] of approaches in the last 10 rounds
        final List<int[]> taken = new ArrayList<>(); // [round, x, y] of recorded candidates in the last 30 rounds

        static boolean near(List<int[]> l, int x, int y) {
            for (int[] c : l) if (Math.max(Math.abs(c[1] - x), Math.abs(c[2] - y)) <= 8) return true;
            return false;
        }

        void round(GameWorld world, int r) {
            List<InternalRobot> as = new ArrayList<>(), bs = new ArrayList<>();
            world.getObjectInfo().eachRobot(x -> {
                if (x.getLocation() == null || x.getType() == UnitType.CAT) return true;
                (x.getTeam() == Team.A ? as : bs).add(x);
                return true;
            });
            recent.removeIf(c -> c[0] < r - 10);
            taken.removeIf(c -> c[0] < r - 30);
            List<int[]> now = new ArrayList<>();
            for (InternalRobot p : as) {
                for (InternalRobot q : bs) {
                    boolean pk = p.getType() == UnitType.RAT_KING, qk = q.getType() == UnitType.RAT_KING;
                    if (pk && qk) continue;
                    MapLocation pl = p.getLocation(), ql = q.getLocation();
                    if (Math.max(Math.abs(pl.x - ql.x), Math.abs(pl.y - ql.y)) > (pk || qk ? 5 : 4)) continue;
                    int x = (pl.x + ql.x) / 2, y = (pl.y + ql.y) / 2;
                    if (!near(now, x, y)) {
                        boolean start = !near(recent, x, y);
                        if (start || !near(taken, x, y)) {
                            int[] f = features(world, as, bs, r, x, y, start);
                            StringBuilder e = new StringBuilder("[");
                            for (int i = 0; i < f.length; i++) e.append(i > 0 ? "," : "").append(f[i]);
                            out.append(out.length() > 0 ? "," : "").append(e.append(']'));
                            taken.add(new int[]{r, x, y});
                        }
                    }
                    now.add(new int[]{r, x, y});
                }
            }
            recent.addAll(now);
        }

        static int[] features(GameWorld world, List<InternalRobot> as, List<InternalRobot> bs, int r, int x, int y,
                boolean start) {
            int[] f = new int[14];
            f[0] = r; f[1] = x; f[2] = y;
            for (int side = 0; side < 2; side++) {
                for (InternalRobot q : side == 0 ? as : bs) {
                    MapLocation l = q.getLocation();
                    int d = Math.max(Math.abs(l.x - x), Math.abs(l.y - y));
                    if (q.getType() == UnitType.BABY_RAT) {
                        if (d <= 5) { f[3 + side]++; f[5 + side] += q.getHealth(); }
                        f[11 + side]++;
                    } else if (d <= 8) f[7 + side]++;
                }
            }
            TeamInfo ti = world.getTeamInfo();
            f[9] = ti.getCheese(Team.A); f[10] = ti.getCheese(Team.B);
            f[13] = start ? 1 : 0;
            return f;
        }
    }

    /** A flat JSON object: strings, ints and lists of ints; the line is kept to be copied into the result. */
    static final class Json {
        final String text;
        final java.util.Map<String, String> raw = new java.util.LinkedHashMap<>();

        Json(String text) { this.text = text.trim(); }

        static Json parse(String s) {
            Json j = new Json(s);
            String t = j.text;
            int i = 1;
            while (i < t.length() - 1) {
                while (t.charAt(i) == ' ' || t.charAt(i) == ',') i++;
                if (t.charAt(i) == '}') break;
                int k0 = t.indexOf('"', i) + 1, k1 = t.indexOf('"', k0);
                String key = t.substring(k0, k1);
                i = t.indexOf(':', k1) + 1;
                while (t.charAt(i) == ' ') i++;
                int v0 = i;
                if (t.charAt(i) == '"') { i = t.indexOf('"', i + 1) + 1; }
                else if (t.charAt(i) == '[') { i = t.indexOf(']', i) + 1; }
                else { while (i < t.length() && ",}".indexOf(t.charAt(i)) < 0) i++; }
                j.raw.put(key, t.substring(v0, i).trim());
            }
            return j;
        }

        boolean has(String k) { return raw.containsKey(k) && !raw.get(k).equals("null"); }
        String str(String k) { String v = raw.get(k); return v.substring(1, v.length() - 1); }
        int num(String k) { return Integer.parseInt(raw.get(k)); }
        int[] nums(String k) {
            String v = raw.get(k).replace("[", "").replace("]", "").trim();
            if (v.isEmpty()) return new int[0];
            String[] p = v.split(",");
            int[] r = new int[p.length];
            for (int i = 0; i < p.length; i++) r[i] = Integer.parseInt(p[i].trim());
            return r;
        }
        String with(String key, String value) { return with("\"" + key + "\":" + value); }
        String with(String fields) { return text.substring(0, text.length() - 1) + "," + fields + "}"; }

        static String quote(String s) { return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") + "\""; }
    }
}
