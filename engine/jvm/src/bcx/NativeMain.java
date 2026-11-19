package bcx;

import battlecode.common.Team;
import battlecode.crossplay.CrossPlayLanguage;
import battlecode.server.Config;
import battlecode.server.GameInfo;
import battlecode.server.GameMaker;
import battlecode.world.BcxStateHash;
import battlecode.world.InternalRobot;
import battlecode.world.control.NullControlProvider;
import battlecode.world.control.RobotControlProvider;
import battlecode.world.control.TeamControlProvider;

import java.io.File;

/**
 * The native engine's entry (translated to C++; engine/runtime/main.cpp calls play). Builds the game as RefMain
 * does and plays it with Match, the robots run by NativeControlProvider.
 */
public final class NativeMain {
    private NativeMain() {}

    /**
     * Returns "ok SEED WINNER WIN_TYPE ROUND HASH CAT_RNG_DRAWS", a tab and the result's other members as JSON
     * (cooperation, A, B, series), or "error MESSAGE".
     */
    public static String play(String a, String b, String map, String mapDir, boolean hasSeed, int seed, String replay,
                              boolean dump, String classesA, String classesB, long catSkip, boolean indicators) {
        Config options = new Config(new String[0]);
        options.set("bc.engine.show-indicators", indicators ? "true" : "false");
        options.set("bc.engine.enable-profiler", "false");
        options.set("bc.server.validate-maps", "true");
        options.set("bc.game.map-path", mapDir);
        GameInfo game = new GameInfo(a, CrossPlayLanguage.JAVA, a, classesA, b, CrossPlayLanguage.JAVA, b, classesB,
                new String[]{map}, new File(replay), false);
        Match.Result r = Match.play(options, game, hasSeed, seed, catSkip, new Controls(), new Sink(dump));
        if (r.winner == null) return "error " + (r.error != null ? r.error : "no winner");
        return "ok " + r.seed + " " + (r.winner == Team.A ? "A" : "B") + " " + r.winType.name() + " " + r.round + " "
                + BcxStateHash.hex16(r.hash) + " " + r.catDraws + "\t\"cooperation\": " + r.cooperation + ", \"A\": "
                + Match.teamJson(r, 0) + ", \"B\": " + Match.teamJson(r, 1) + ", \"series\": " + Match.seriesJson(r);
    }

    static final class Controls implements Match.Controls {
        public RobotControlProvider create(GameInfo game, GameMaker gameMaker, boolean profilingEnabled) {
            TeamControlProvider teamProvider = new TeamControlProvider();
            teamProvider.registerControlProvider(Team.A, new NativeControlProvider(game.getTeamAPackage()));
            teamProvider.registerControlProvider(Team.B, new NativeControlProvider(game.getTeamBPackage()));
            teamProvider.registerControlProvider(Team.NEUTRAL, new NullControlProvider());
            return teamProvider;
        }

        public void cleanup() {}

        public boolean crashReported(InternalRobot robot) { return NativeControlProvider.crashed(robot.getID()); }
    }

    static final class Sink implements Match.Sink {
        private final boolean dump;
        Sink(boolean dump) { this.dump = dump; }
        public boolean wantDump() { return dump; }
        public void round(int round, long hash, String d) { NativeIO.round(round, hash, d); }
    }
}
