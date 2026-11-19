package bcx;

import battlecode.common.Team;
import battlecode.server.Config;
import battlecode.server.ErrorReporter;
import battlecode.server.GameInfo;
import battlecode.server.GameMaker;
import battlecode.server.GameState;
import battlecode.world.BcxMaps;
import battlecode.world.BcxStateHash;
import battlecode.world.DominationFactor;
import battlecode.world.GameMapIO;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;
import battlecode.world.LiveMap;
import battlecode.world.TeamInfo;
import battlecode.world.control.RobotControlProvider;

import java.io.File;
import java.util.Random;

/**
 * The official battlecode.server.Server match loop (run() for one game, runMatch()) for exactly one game, plus the
 * state hash after every round. Plain Java only (translated to C++).
 */
public final class Match {
    private Match() {
    }

    /** Builds the control provider; the reference does what Server.createControlProvider does. */
    public interface Controls {
        RobotControlProvider create(GameInfo game, GameMaker gameMaker, boolean profilingEnabled);

        /** Where Server cleans up (its finally block: crossPlayServer.cleanup()). */
        void cleanup();

        /**
         * Whether the engine reported a crash of this robot since the last call: its run() ended by a Throwable other
         * than RobotDeathException (ErrorReporter's "Stack trace") or by returning ("froze"), as tools/play counts.
         */
        boolean crashReported(InternalRobot robot);
    }

    /** Receives each round's state hash, and its dump when wantDump(). */
    public interface Sink {
        boolean wantDump();

        void round(int round, long hash, String dump);
    }

    public static final class Result {
        /** null when the game could not be played (then error is set and no replay is written). */
        public Team winner;
        public DominationFactor winType;
        /** The last round played. */
        public int round;
        /** The seed the game played. */
        public int seed;
        /** The chain over the round hashes: BcxStateHash.chain() fed (round, hash) per round, finished. */
        public long hash;
        /** Draws (Random.next calls) from the cat RNG during the game. */
        public long catDraws;
        public String error;
        /** Per team (A, B), as tools/play reports them: GameWorld.isCooperation and the TeamInfo counters at the end. */
        public boolean cooperation;
        public final int[] points = new int[2], cheeseTransferred = new int[2], catDamage = new int[2],
                rats = new int[2], kings = new int[2], cheese = new int[2];
        /** Robots whose run() ended by a Throwable other than RobotDeathException or by returning (Controls.crashReported). */
        public final int[] crashed = new int[2];
        /** Each team's [cheese transferred, rats, kings, cat damage] every SERIES_EVERY rounds and at the end. */
        public final StringBuilder[] series = {new StringBuilder(), new StringBuilder()};
        GameWorld world;
    }

    /** Rounds between two samples of the series (tools/play's). */
    public static final int SERIES_EVERY = 100;

    /** A team's counters as a JSON object (tools/play's fields and order). */
    public static String teamJson(Result r, int t) {
        return "{\"points\": " + r.points[t] + ", \"cheese_transferred\": " + r.cheeseTransferred[t] + ", \"cat_damage\": "
                + r.catDamage[t] + ", \"rats\": " + r.rats[t] + ", \"kings\": " + r.kings[t] + ", \"cheese\": " + r.cheese[t]
                + ", \"crashed\": " + r.crashed[t] + "}";
    }

    /** The series as a JSON object (tools/play's). */
    public static String seriesJson(Result r) {
        return "{\"every\": " + SERIES_EVERY + ", \"fields\": [\"cheese_transferred\", \"rats\", \"kings\", \"cat_damage\"], "
                + "\"A\": [" + r.series[0] + "], \"B\": [" + r.series[1] + "]}";
    }

    static void sample(GameWorld w, Result r) {
        TeamInfo ti = w.getTeamInfo();
        for (Team t : new Team[]{Team.A, Team.B}) {
            StringBuilder s = r.series[t.ordinal()];
            s.append(s.length() > 0 ? ", " : "").append('[').append(ti.getCheeseTransferred(t)).append(", ")
                    .append(ti.getNumBabyRats(t)).append(", ").append(ti.getNumRatKings(t)).append(", ")
                    .append(ti.getDamageToCats(t)).append(']');
        }
    }

    /**
     * Each team's points as the engine would award them at round 2000 (GameWorld.setWinnerIfMorePoints adds them to
     * TeamInfo: the delta), and the counters. Called after the replay is written; the world is discarded after it.
     */
    static void finishStats(GameWorld w, Result r) {
        TeamInfo ti = w.getTeamInfo();
        r.cooperation = w.isCooperation();
        int[] before = {ti.getPoints(Team.A), ti.getPoints(Team.B)};
        w.setWinnerIfMorePoints();
        for (Team t : new Team[]{Team.A, Team.B}) {
            int i = t.ordinal();
            r.points[i] = ti.getPoints(t) - before[i];
            r.cheeseTransferred[i] = ti.getCheeseTransferred(t);
            r.catDamage[i] = ti.getDamageToCats(t);
            r.rats[i] = ti.getNumBabyRats(t);
            r.kings[i] = ti.getNumRatKings(t);
            r.cheese[i] = ti.getCheese(t);
        }
    }

    /** The control provider, counting each team's crashed robots (Controls.crashReported) after each turn. */
    static final class Crashes implements RobotControlProvider {
        private final RobotControlProvider p;
        private final Result r;
        private final Controls controls;

        Crashes(RobotControlProvider p, Result r, Controls controls) { this.p = p; this.r = r; this.controls = controls; }

        public void matchStarted(GameWorld w) { p.matchStarted(w); }
        public void matchEnded() { p.matchEnded(); }
        public void roundStarted() { p.roundStarted(); }
        public void roundEnded() { p.roundEnded(); }
        public void robotSpawned(InternalRobot robot) { p.robotSpawned(robot); }
        public void robotKilled(InternalRobot robot) { p.robotKilled(robot); }
        public int getBytecodesUsed(InternalRobot robot) { return p.getBytecodesUsed(robot); }
        public boolean getTerminated(InternalRobot robot) { return p.getTerminated(robot); }

        public void runRobot(InternalRobot robot) {
            controls.crashReported(robot); // nothing before this turn belongs to it
            p.runRobot(robot);
            if (controls.crashReported(robot) && robot.getTeam().ordinal() < 2) r.crashed[robot.getTeam().ordinal()]++;
        }
    }

    /**
     * The engine's only state that outlives a game in a JVM: the cats' RNG, InternalRobot.rand, made new Random(1092)
     * by InternalRobot's static initializer. Game N of an official match (one JVM, several maps) starts it where the
     * games before left it: catSkip draws (Random.next calls) later. A plain Random, as the engine's own.
     */
    static Random catRng(long skip) {
        Random r = new Random(1092);
        for (long i = 0; i < skip; i++) r.nextInt(); // one next(32) each
        return r;
    }

    /** The draws between two states of a java.util.Random (its LCG steps), or -1 past max. */
    static long draws(long from, long to, long max) {
        long x = from;
        for (long n = 0; n <= max; n++) {
            if (x == to) return n;
            x = (x * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        }
        return -1;
    }

    /**
     * Plays game.getMaps()[0] as Server.run() and Server.runMatch() do. With hasSeed, the map's seed is replaced as
     * the repo's GameMapIO override does; the cat RNG starts catSkip draws after its first state (catRng).
     */
    public static Result play(Config options, GameInfo game, boolean hasSeed, int seed, long catSkip, Controls controls,
            Sink sink) {
        Result res = new Result();
        Random cats = catRng(catSkip);
        InternalRobot.rand = cats;
        long start = BcxStateHash.rngState(cats);
        try {
            return play(options, game, hasSeed, seed, controls, sink, res);
        } finally {
            res.catDraws = draws(start, BcxStateHash.rngState(cats), 1L << 32);
        }
    }

    private static Result play(Config options, GameInfo game, boolean hasSeed, int seed, Controls controls, Sink sink,
            Result res) {
        if (game.getMaps().length != 1) {
            res.error = "Match plays exactly one map";
            return res;
        }
        try {
            GameMaker gameMaker = new GameMaker(game, null, options.getBoolean("bc.engine.show-indicators"));
            gameMaker.makeGameHeader();
            try {
                boolean profilingEnabled = options.getBoolean("bc.engine.enable-profiler");
                RobotControlProvider prov = new Crashes(controls.create(game, gameMaker, profilingEnabled), res, controls);
                boolean checkMapGuarantees = options.getBoolean("bc.server.validate-maps");

                int aWins = 0;
                int bWins = 0;
                Team winner;
                try {
                    winner = runMatch(options, game, prov, gameMaker, checkMapGuarantees, hasSeed, seed, sink, res);
                } catch (Exception e) {
                    ErrorReporter.report(e);
                    res.winner = null;
                    res.error = "runMatch: " + e;
                    return res;
                }
                if (winner == null) {
                    // Server.run's switch (winner) throws here: no footer, no replay
                    res.error = "no winner";
                    return res;
                }
                if (winner == Team.A) {
                    aWins++;
                } else if (winner == Team.B) {
                    bWins++;
                }
                Team gameWinner = aWins >= bWins ? Team.A : Team.B;
                gameMaker.makeGameFooter(gameWinner);
                gameMaker.writeGame(game.getSaveFile());
                finishStats(res.world, res);
            } finally {
                controls.cleanup();
            }
        } catch (Exception e) {
            res.winner = null;
            res.error = "server: " + e;
        }
        return res;
    }

    private static Team runMatch(Config options, GameInfo game, RobotControlProvider prov, GameMaker gameMaker,
            boolean checkMapGuarantees, boolean hasSeed, int seed, Sink sink, Result res) throws Exception {
        String mapName = game.getMaps()[0];
        LiveMap loadedMap = GameMapIO.loadMap(mapName, new File(options.get("bc.game.map-path")), false);
        if (hasSeed) {
            loadedMap = BcxMaps.withSeed(loadedMap, seed);
        }
        res.seed = loadedMap.getSeed();

        GameWorld world = new GameWorld(loadedMap, prov, gameMaker.getMatchMaker());
        if (checkMapGuarantees) {
            world.getGameMap().assertIsValid();
        }

        BcxStateHash chain = BcxStateHash.chain();
        boolean wantDump = sink.wantDump();
        while (true) {
            int before = world.getCurrentRound();
            GameState state = world.runRound();
            int round = world.getCurrentRound();
            if (!GameState.DONE.equals(state) && round % SERIES_EVERY == 0) {
                sample(world, res);
            }
            if (round != before) {
                long h = BcxStateHash.hash(world);
                String d = wantDump ? BcxStateHash.dump(world) : null;
                chain.add(round);
                chain.add(h);
                sink.round(round, h, d);
            }
            if (GameState.DONE.equals(state)) {
                break;
            }
        }

        sample(world, res);
        res.world = world;
        res.hash = chain.finish();
        res.round = world.getCurrentRound();
        res.winner = world.getWinner();
        res.winType = world.getGameStats().getDominationFactor();
        return world.getWinner();
    }
}
