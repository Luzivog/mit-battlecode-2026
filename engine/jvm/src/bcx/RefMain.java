package bcx;

import battlecode.common.Team;
import battlecode.crossplay.CrossPlay;
import battlecode.crossplay.CrossPlayLanguage;
import battlecode.server.Config;
import battlecode.server.GameInfo;
import battlecode.server.GameMaker;
import battlecode.world.control.NullControlProvider;
import battlecode.world.control.PlayerControlProvider;
import battlecode.world.control.RobotControlProvider;
import battlecode.world.control.TeamControlProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Plays one spec (engine/AGENTS.md) in this JVM with the official control providers:
 * {@code java -cp jvm-classes:JAR bcx.RefMain SPEC OUTDIR [--dump] [--root DIR]}.
 * SPEC is a JSON object or a file holding one. Writes OUTDIR/result.json, hashes.txt, replay.bc26 (and dump.txt).
 * The repo root (maps/, build/classes) is --root, default the working directory.
 */
public final class RefMain {
    static final Set<String> FIELDS = Set.of("a", "b", "map", "seed", "engine", "classes", "classes_a", "classes_b", "id");

    public static void main(String[] args) throws Exception {
        String specArg = null;
        String outArg = null;
        boolean dump = false;
        String rootArg = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--dump")) dump = true;
            else if (args[i].equals("--root")) rootArg = args[++i];
            else if (specArg == null) specArg = args[i];
            else if (outArg == null) outArg = args[i];
            else usage("unexpected argument " + args[i]);
        }
        if (specArg == null || outArg == null) usage("missing SPEC or OUTDIR");
        Path root = Path.of(rootArg != null ? rootArg : ".").toAbsolutePath().normalize();
        Path out = Path.of(outArg).toAbsolutePath().normalize();
        Files.createDirectories(out);

        String specText = specArg.trim().startsWith("{") ? specArg : Files.readString(Path.of(specArg));
        Map<String, Object> spec;
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        try {
            spec = Json.parseObject(specText);
        } catch (RuntimeException e) {
            result.put("backend", "java");
            result.put("error", "bad spec: " + e.getMessage());
            finish(out, result, 2);
            return;
        }
        for (String k : new String[]{"id", "a", "b", "map"}) {
            if (spec.containsKey(k)) result.put(k, spec.get(k));
        }
        String engine = spec.containsKey("engine") ? String.valueOf(spec.get("engine")) : "1.2.6";
        result.put("engine", engine);

        String err = validate(spec);
        String jarVersion = jarVersion();
        if (err == null && !engine.equals(jarVersion)) {
            err = "spec engine " + engine + " but this JVM runs engine " + jarVersion;
        }
        if (err != null) {
            result.put("backend", "java");
            result.put("error", err);
            finish(out, result, 2);
            return;
        }

        String a = (String) spec.get("a");
        String b = (String) spec.get("b");
        String map = (String) spec.get("map");
        boolean hasSeed = spec.containsKey("seed");
        int seed = hasSeed ? ((Long) spec.get("seed")).intValue() : 0;
        String classes = spec.containsKey("classes") ? (String) spec.get("classes") : "build/classes";
        String classesA = resolve(root, spec.containsKey("classes_a") ? (String) spec.get("classes_a") : classes);
        String classesB = resolve(root, spec.containsKey("classes_b") ? (String) spec.get("classes_b") : classes);
        Path replay = out.resolve("replay.bc26");
        Files.deleteIfExists(replay);

        // battlecode.server.Main with the flags of ref.sh (-c=- and these -D), plus no websocket
        String[][] props = {
                {"bc.server.wait-for-client", "false"},
                {"bc.server.mode", "headless"},
                {"bc.server.websocket", "false"},
                {"bc.server.map-path", root.resolve("maps").toString()},
                {"bc.game.map-path", root.resolve("maps").toString()},
                {"bc.server.robot-player-to-system-out", "false"},
                {"bc.server.debug", "false"},
                {"bc.engine.debug-methods", "false"},
                {"bc.engine.enable-profiler", "false"},
                {"bc.engine.show-indicators", "true"},
                {"bc.game.team-a", a},
                {"bc.game.team-b", b},
                {"bc.game.team-a.url", classesA},
                {"bc.game.team-b.url", classesB},
                {"bc.game.team-a.package", a},
                {"bc.game.team-b.package", b},
                {"bc.game.maps", map},
                {"bc.server.validate-maps", "true"},
                {"bc.server.alternate-order", "false"},
                {"bc.server.save-file", replay.toString()},
        };
        for (String[] p : props) System.setProperty(p[0], p[1]);
        Config options = battlecode.server.Main.setupConfig(new String[]{"-c=-"});

        // as Main.runHeadless builds it
        String[] maps = options.get("bc.game.maps").split(",");
        GameInfo game = new GameInfo(
                options.get("bc.game.team-a"), CrossPlayLanguage.JAVA, options.get("bc.game.team-a.package"),
                options.get("bc.game.team-a.url"),
                options.get("bc.game.team-b"), CrossPlayLanguage.JAVA, options.get("bc.game.team-b.package"),
                options.get("bc.game.team-b.url"),
                maps, new File(options.get("bc.server.save-file")),
                options.getBoolean("bc.game.best-of-three") && maps.length == 3);

        FileSink sink = new FileSink(out, dump);
        Match.Result r;
        try {
            r = Match.play(options, game, hasSeed, seed, new OfficialControls(), sink);
        } catch (Throwable t) {
            // an Error escapes Server.run too; record it instead of hanging on robot threads
            t.printStackTrace();
            r = new Match.Result();
            r.error = "uncaught: " + t;
        } finally {
            sink.close();
        }

        result.put("seed", hasSeed ? seed : r.seed);
        int code = 0;
        if (r.error != null || r.winner == null) {
            result.put("error", r.error != null ? r.error : "no winner");
            code = 1;
        } else {
            result.put("winner", r.winner == Team.A ? "A" : "B");
            result.put("winner_pkg", r.winner == Team.A ? a : b);
            result.put("win_type", r.winType.name());
            result.put("round", r.round);
            result.put("hash", battlecode.world.BcxStateHash.hex16(r.hash));
        }
        result.put("backend", "java");
        finish(out, result, code);
    }

    static String validate(Map<String, Object> spec) {
        for (String k : spec.keySet()) {
            if (!FIELDS.contains(k)) return "unknown spec field " + k;
        }
        for (String k : new String[]{"a", "b", "map"}) {
            if (!(spec.get(k) instanceof String)) return "spec field " + k + " is required (a string)";
        }
        for (String k : new String[]{"engine", "classes", "classes_a", "classes_b"}) {
            if (spec.containsKey(k) && !(spec.get(k) instanceof String)) return "spec field " + k + " must be a string";
        }
        if (spec.containsKey("seed")) {
            Object s = spec.get("seed");
            if (!(s instanceof Long) || (Long) s < Integer.MIN_VALUE || (Long) s > Integer.MAX_VALUE) {
                return "spec field seed must be an int";
            }
        }
        if (spec.containsKey("engine")) {
            String e = (String) spec.get("engine");
            if (!e.equals("1.2.6") && !e.equals("1.2.5")) return "spec engine must be 1.2.6 or 1.2.5";
        }
        return null;
    }

    static String resolve(Path root, String p) {
        return root.resolve(p).toAbsolutePath().normalize().toString();
    }

    /** The engine version of the battlecode jar on the classpath (its battlecode_version resource). */
    static String jarVersion() throws IOException {
        try (InputStream in = GameInfo.class.getClassLoader().getResourceAsStream("battlecode_version")) {
            if (in == null) return "unknown";
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
    }

    static void finish(Path out, Map<String, Object> result, int code) throws IOException {
        double cpu = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
                .getProcessCpuTime() / 1e9;
        result.put("cpu_s", Double.parseDouble(String.format(Locale.ROOT, "%.2f", cpu)));
        String line = Json.write(result);
        Files.writeString(out.resolve("result.json"), line + "\n");
        System.out.println(line);
        System.out.flush();
        // robot threads must not keep the JVM alive (Main.main exits too)
        System.exit(code);
    }

    static void usage(String msg) {
        System.err.println("RefMain: " + msg);
        System.err.println("usage: java -cp JVM_CLASSES:JAR bcx.RefMain SPEC_JSON|SPEC_FILE OUTDIR [--dump] [--root REPO]");
        System.exit(2);
    }

    /** Server.createControlProvider, with the CrossPlay that Server.run creates and cleans up. */
    static final class OfficialControls implements Match.Controls {
        private CrossPlay crossPlay;

        @Override
        public RobotControlProvider create(GameInfo game, GameMaker gameMaker, boolean profilingEnabled) {
            crossPlay = new CrossPlay();
            TeamControlProvider teamProvider = new TeamControlProvider();
            teamProvider.registerControlProvider(Team.A, new PlayerControlProvider(Team.A, game.getTeamAPackage(),
                    game.getTeamALanguage(), crossPlay, game.getTeamAURL(), gameMaker.getMatchMaker().getOut(),
                    profilingEnabled));
            teamProvider.registerControlProvider(Team.B, new PlayerControlProvider(Team.B, game.getTeamBPackage(),
                    game.getTeamBLanguage(), crossPlay, game.getTeamBURL(), gameMaker.getMatchMaker().getOut(),
                    profilingEnabled));
            teamProvider.registerControlProvider(Team.NEUTRAL, new NullControlProvider());
            return teamProvider;
        }

        @Override
        public void cleanup() {
            if (crossPlay != null) crossPlay.cleanup();
        }
    }

    /** hashes.txt (ROUND HEX16 per line) and, with --dump, dump.txt (a header line per round). */
    static final class FileSink implements Match.Sink {
        private final BufferedWriter hashes;
        private final BufferedWriter dump;

        FileSink(Path out, boolean wantDump) throws IOException {
            hashes = writer(out.resolve("hashes.txt"));
            Path d = out.resolve("dump.txt");
            Files.deleteIfExists(d);
            dump = wantDump ? writer(d) : null;
        }

        private static BufferedWriter writer(Path p) throws IOException {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(p.toFile()), StandardCharsets.UTF_8),
                    1 << 16);
        }

        @Override
        public boolean wantDump() {
            return dump != null;
        }

        @Override
        public void round(int round, long hash, String text) {
            try {
                hashes.write(round + " " + battlecode.world.BcxStateHash.hex16(hash) + "\n");
                if (dump != null) {
                    dump.write("=== round " + round + " " + battlecode.world.BcxStateHash.hex16(hash) + "\n");
                    dump.write(text);
                    dump.write("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        void close() throws IOException {
            hashes.close();
            if (dump != null) dump.close();
        }
    }

    /** Just enough JSON: objects, arrays, strings, integers, decimals, true/false/null. */
    static final class Json {
        private final String s;
        private int i;

        private Json(String s) {
            this.s = s;
        }

        @SuppressWarnings("unchecked")
        static Map<String, Object> parseObject(String text) {
            Json j = new Json(text);
            Object v = j.value();
            j.ws();
            if (j.i != text.length()) throw new IllegalArgumentException("trailing characters at " + j.i);
            if (!(v instanceof Map)) throw new IllegalArgumentException("not a JSON object");
            return (Map<String, Object>) v;
        }

        private void ws() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        private char peek() {
            ws();
            if (i >= s.length()) throw new IllegalArgumentException("unexpected end");
            return s.charAt(i);
        }

        private void expect(char c) {
            if (peek() != c) throw new IllegalArgumentException("expected '" + c + "' at " + i);
            i++;
        }

        private Object value() {
            char c = peek();
            if (c == '{') {
                i++;
                LinkedHashMap<String, Object> m = new LinkedHashMap<>();
                if (peek() == '}') {
                    i++;
                    return m;
                }
                while (true) {
                    if (peek() != '"') throw new IllegalArgumentException("expected a key at " + i);
                    String k = string();
                    expect(':');
                    if (m.containsKey(k)) throw new IllegalArgumentException("duplicate key " + k);
                    m.put(k, value());
                    char d = peek();
                    i++;
                    if (d == '}') return m;
                    if (d != ',') throw new IllegalArgumentException("expected ',' or '}' at " + (i - 1));
                }
            }
            if (c == '[') {
                i++;
                List<Object> l = new ArrayList<>();
                if (peek() == ']') {
                    i++;
                    return l;
                }
                while (true) {
                    l.add(value());
                    char d = peek();
                    i++;
                    if (d == ']') return l;
                    if (d != ',') throw new IllegalArgumentException("expected ',' or ']' at " + (i - 1));
                }
            }
            if (c == '"') return string();
            if (s.startsWith("true", i)) {
                i += 4;
                return Boolean.TRUE;
            }
            if (s.startsWith("false", i)) {
                i += 5;
                return Boolean.FALSE;
            }
            if (s.startsWith("null", i)) {
                i += 4;
                return null;
            }
            int st = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            String num = s.substring(st, i);
            if (num.isEmpty()) throw new IllegalArgumentException("unexpected '" + c + "' at " + st);
            if (num.matches("-?\\d+")) return Long.parseLong(num);
            return Double.parseDouble(num);
        }

        private String string() {
            expect('"');
            StringBuilder b = new StringBuilder();
            while (true) {
                if (i >= s.length()) throw new IllegalArgumentException("unterminated string");
                char c = s.charAt(i++);
                if (c == '"') return b.toString();
                if (c != '\\') {
                    b.append(c);
                    continue;
                }
                char e = s.charAt(i++);
                switch (e) {
                    case 'n': b.append('\n'); break;
                    case 't': b.append('\t'); break;
                    case 'r': b.append('\r'); break;
                    case 'b': b.append('\b'); break;
                    case 'f': b.append('\f'); break;
                    case 'u': b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                    default: b.append(e);
                }
            }
        }

        static String write(Object v) {
            StringBuilder b = new StringBuilder();
            write(b, v);
            return b.toString();
        }

        @SuppressWarnings("unchecked")
        private static void write(StringBuilder b, Object v) {
            if (v == null) {
                b.append("null");
            } else if (v instanceof String) {
                String s = (String) v;
                b.append('"');
                for (int k = 0; k < s.length(); k++) {
                    char c = s.charAt(k);
                    if (c == '"' || c == '\\') b.append('\\').append(c);
                    else if (c == '\n') b.append("\\n");
                    else if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
                }
                b.append('"');
            } else if (v instanceof Map) {
                b.append('{');
                boolean first = true;
                for (Map.Entry<String, Object> e : ((Map<String, Object>) v).entrySet()) {
                    if (!first) b.append(", ");
                    first = false;
                    write(b, e.getKey());
                    b.append(": ");
                    write(b, e.getValue());
                }
                b.append('}');
            } else if (v instanceof List) {
                b.append('[');
                boolean first = true;
                for (Object o : (List<Object>) v) {
                    if (!first) b.append(", ");
                    first = false;
                    write(b, o);
                }
                b.append(']');
            } else {
                b.append(v);
            }
        }
    }
}
