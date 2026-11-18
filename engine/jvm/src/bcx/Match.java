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
import battlecode.world.LiveMap;
import battlecode.world.control.RobotControlProvider;

import java.io.File;

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
        public String error;
    }

    /**
     * Plays game.getMaps()[0] as Server.run() and Server.runMatch() do. With hasSeed, the map's seed is replaced as
     * the repo's GameMapIO override does.
     */
    public static Result play(Config options, GameInfo game, boolean hasSeed, int seed, Controls controls, Sink sink) {
        Result res = new Result();
        if (game.getMaps().length != 1) {
            res.error = "Match plays exactly one map";
            return res;
        }
        try {
            GameMaker gameMaker = new GameMaker(game, null, options.getBoolean("bc.engine.show-indicators"));
            gameMaker.makeGameHeader();
            try {
                boolean profilingEnabled = options.getBoolean("bc.engine.enable-profiler");
                RobotControlProvider prov = controls.create(game, gameMaker, profilingEnabled);
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

        res.hash = chain.finish();
        res.round = world.getCurrentRound();
        res.winner = world.getWinner();
        res.winType = world.getGameStats().getDominationFactor();
        return world.getWinner();
    }
}
