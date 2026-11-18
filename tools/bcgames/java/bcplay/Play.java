package bcplay;

import battlecode.common.Team;
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
import battlecode.world.control.RobotControlProvider;
import battlecode.world.control.TeamControlProvider;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Random;

/**
 * Plays many games in one JVM with the unmodified engine on the classpath.
 *
 * Each game repeats what battlecode.server.Server.run() and runMatch() do for a one-map game, in the same order,
 * after resetting the engine's only cross-game state (InternalRobot.rand, the static cat RNG, seeded 1092 at class
 * init). So every game equals the same game played alone in a fresh JVM by battlecode.server.Main.
 *
 * stdin, one game per line, tab-separated: id, teamA, urlA, teamB, urlB, map, mapDir, replayPath ("" = none).
 * stdout: one JSON object per game. Engine and robot console output goes to stderr.
 */
public final class Play {
    private static final long CAT_RNG_SEED = 1092; // battlecode.world.InternalRobot: new Random(1092)

    public static void main(String[] args) throws Exception {
        PrintStream results = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
        Scanner scanner = new Scanner(new BufferedOutputStream(new FileOutputStream(FileDescriptor.err), 1 << 16));
        PrintStream console = new PrintStream(scanner, true, StandardCharsets.UTF_8);
        System.setOut(console);
        System.setErr(console);

        Config options = new Config(new String[0]);
        Config.setGlobalConfig(options);
        boolean validate = options.getBoolean("bc.server.validate-maps");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        results.println("{\"ready\":true}");
        for (String line; (line = in.readLine()) != null; ) {
            if (line.isEmpty()) continue;
            String[] f = line.split("\t", -1);
            String id = f[0];
            String json;
            try {
                json = play(f[1], f[2], f[3], f[4], f[5], f[6], f[7], validate, scanner);
                json = "{\"id\":" + quote(id) + "," + json.substring(1);
            } catch (Throwable t) {
                t.printStackTrace(console);
                json = "{\"id\":" + quote(id) + ",\"error\":" + quote(t.toString()) + "}";
            }
            results.println(json);
        }
    }

    static String play(String teamA, String urlA, String teamB, String urlB, String mapName, String mapDir,
                       String replayPath, boolean validate, Scanner scanner) throws Exception {
        long start = System.nanoTime();
        CountingRandom catRng = new CountingRandom(CAT_RNG_SEED);
        InternalRobot.rand = catRng;
        scanner.reset(teamA, teamB);

        File saveFile = replayPath.isEmpty() ? null : new File(replayPath);
        GameInfo info = new GameInfo(teamA, CrossPlayLanguage.JAVA, teamA, urlA,
                teamB, CrossPlayLanguage.JAVA, teamB, urlB, new String[]{mapName}, saveFile, false);
        GameMaker gameMaker = new GameMaker(info, null, false);
        gameMaker.makeGameHeader();
        CrossPlay crossPlay = new CrossPlay();
        GameWorld world;
        try {
            TeamControlProvider prov = new TeamControlProvider();
            OutputStream robotOut = gameMaker.getMatchMaker().getOut();
            prov.registerControlProvider(Team.A,
                    new PlayerControlProvider(Team.A, teamA, CrossPlayLanguage.JAVA, crossPlay, urlA, robotOut, false));
            prov.registerControlProvider(Team.B,
                    new PlayerControlProvider(Team.B, teamB, CrossPlayLanguage.JAVA, crossPlay, urlB, robotOut, false));
            prov.registerControlProvider(Team.NEUTRAL, new NullControlProvider());

            LiveMap map = GameMapIO.loadMap(mapName, new File(mapDir), false);
            world = new GameWorld(map, prov, gameMaker.getMatchMaker());
            if (validate) world.getGameMap().assertIsValid();
            while (world.runRound() != GameState.DONE) { }
        } finally {
            crossPlay.cleanup();
            System.out.flush();
            scanner.closeTrace();
        }
        Team winner = world.getWinner();
        if (winner == null) throw new IllegalStateException("game ended without a winner (engine error, see log)");
        gameMaker.makeGameFooter(winner);
        byte[] replay = gameMaker.toBytes();
        if (saveFile != null) Files.write(saveFile.toPath(), replay);
        long wallMs = (System.nanoTime() - start) / 1_000_000;

        TeamInfo ti = world.getTeamInfo();
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"winner\":\"").append(winner.name()).append('"');
        sb.append(",\"wintype\":\"").append(world.getGameStats().getDominationFactor().name()).append('"');
        sb.append(",\"rounds\":").append(world.getCurrentRound());
        sb.append(",\"wall_ms\":").append(wallMs);
        sb.append(",\"digest\":\"").append(sha256(replay).substring(0, 16)).append('"');
        for (Team t : new Team[]{Team.A, Team.B}) {
            sb.append(",\"").append(t.name()).append("\":{");
            sb.append("\"cheese_transferred\":").append(ti.getCheeseTransferred(t));
            sb.append(",\"cat_damage\":").append(ti.getDamageToCats(t));
            sb.append(",\"rats\":").append(ti.getNumBabyRats(t));
            sb.append(",\"kings\":").append(ti.getNumRatKings(t));
            sb.append(",\"cheese\":").append(ti.getCheese(t));
            sb.append(",\"frozen\":").append(scanner.frozen[t.ordinal()]);
            sb.append('}');
        }
        sb.append(",\"engine_errors\":").append(scanner.frozen[2]);
        sb.append(",\"cat_rng_draws\":").append(catRng.draws);
        sb.append('}');
        return sb.toString();
    }

    /** The cat RNG, counting draws: a game that never draws from it doesn't depend on its starting state. */
    static final class CountingRandom extends Random {
        long draws;
        CountingRandom(long seed) { super(seed); }
        @Override protected int next(int bits) { draws++; return super.next(bits); }
    }

    static String sha256(byte[] bytes) throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') sb.append('\\').append(c);
            else if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
            else sb.append(c);
        }
        return sb.append('"').toString();
    }

    /**
     * Passes console output through and counts robots frozen per team. The engine reports a Throwable escaping a
     * robot's run() as "Stack trace: " followed by the trace (ErrorReporter); the first frame in a team's package
     * names the team (no team frame: an engine error). A robot returning from run() prints "<Team>'s <id> ... froze".
     * Robots run one at a time, so traces don't interleave.
     */
    static final class Scanner extends OutputStream {
        final OutputStream out;
        final StringBuilder line = new StringBuilder();
        final int[] frozen = new int[3]; // A, B, unattributed
        String prefixA, prefixB;
        boolean inTrace;

        Scanner(OutputStream out) { this.out = out; }

        synchronized void reset(String teamA, String teamB) {
            frozen[0] = frozen[1] = frozen[2] = 0;
            prefixA = "at " + teamA + ".";
            prefixB = "at " + teamB + ".";
            closeTrace();
        }

        @Override public synchronized void write(int b) throws IOException {
            out.write(b);
            if (b == '\n') { out.flush(); onLine(line.toString()); line.setLength(0); }
            else line.append((char) b);
        }

        @Override public synchronized void write(byte[] b, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) write(b[i]);
        }

        @Override public synchronized void flush() throws IOException { out.flush(); }

        /** Ends the current game's scan: a trace with no team frame was an engine error. */
        synchronized void closeTrace() {
            if (inTrace) frozen[2]++;
            inTrace = false;
        }

        private void onLine(String s) {
            String t = s.trim();
            if (t.startsWith("Stack trace:")) { closeTrace(); inTrace = true; return; }
            if (inTrace && t.startsWith("at ")) {
                if (t.startsWith(prefixA)) { frozen[0]++; inTrace = false; }
                else if (t.startsWith(prefixB)) { frozen[1]++; inTrace = false; }
                return;
            }
            if (t.contains(" froze in round ") && t.contains(" because it returned from its run() method")) {
                if (t.startsWith("A's ")) frozen[0]++;
                else if (t.startsWith("B's ")) frozen[1]++;
            }
        }
    }
}
