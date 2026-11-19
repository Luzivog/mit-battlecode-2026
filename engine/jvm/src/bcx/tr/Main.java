package bcx.tr;

import bcx.tr.Cls.Mth;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Translator command line.
 * <pre>
 * java bcx.tr.Main --out DIR --natives natives.txt [--team PKG=CLASSES_DIR]... --entry CLASS.NAME+DESC... [--debug-methods]
 * </pre>
 * Writes DIR/types.h, DIR/types_TEAM.h, DIR/klasses.cpp, DIR/m_MODULE_N.cpp and DIR/report.txt. Exits 1 if anything
 * reachable can't be translated, naming it.
 */
public final class Main {
    public static void main(String[] a) throws Exception {
        Path out = null, nat = null;
        List<String[]> teams = new ArrayList<>();
        List<String> entries = new ArrayList<>();
        boolean debug = false;
        for (int i = 0; i < a.length; i++) {
            switch (a[i]) {
                case "--out" -> out = Path.of(a[++i]);
                case "--natives" -> nat = Path.of(a[++i]);
                case "--team" -> teams.add(a[++i].split("=", 2));
                case "--entry" -> entries.add(a[++i]);
                case "--debug-methods" -> debug = true;
                default -> throw new IllegalArgumentException("unknown argument " + a[i]);
            }
        }
        long t0 = System.nanoTime();
        Pool pool = new Pool(debug);
        Natives natives = new Natives(pool, nat);
        for (String[] s : natives.substitutions) pool.substitute(s[0], s[1]);
        for (String[] s : natives.patches) pool.patch(s[0], s[1]);
        for (String s : natives.completes) pool.complete(s);
        for (String[] t : teams) pool.addTeam(t[0], t[1]);
        Rta rta = new Rta(pool, natives);
        Glue.roots(rta, pool);
        natives.reachRoots(rta);
        for (String e : entries) natives.reachDep(rta, pool.sys, "m:" + e);
        for (Pool.Module m : pool.teams) {
            Cls rp = pool.get(m, m.id + "/RobotPlayer");
            rta.init(rp);
            rta.reach(rp.resolveMethod("run", "(Lbattlecode/common/RobotController;)V", false));
            for (String c : natives.perRobotRoots) natives.reachDep(rta, m, c);
        }
        rta.run();
        StringBuilder rep = new StringBuilder();
        int nm = 0;
        for (Cls c : pool.all()) for (Mth m : c.methods.values()) if (m.reachable) nm++;
        rep.append("classes used ").append(rta.used.size()).append(", instantiated ").append(rta.instantiated.size())
                .append(", methods reachable ").append(nm).append('\n');
        if (!rta.problems.isEmpty()) {
            TreeSet<String> ps = new TreeSet<>(rta.problems);
            for (String p : ps) System.err.println("PROBLEM " + p);
            System.err.println(ps.size() + " problems");
            System.exit(1);
        }
        Layout layout = new Layout(pool, rta);
        Emit emit = new Emit(pool, rta, layout, natives);
        for (String en : entries) {
            int dot = en.lastIndexOf('.', en.indexOf('('));
            Cls c = pool.get(pool.sys, en.substring(0, dot));
            String rest = en.substring(dot + 1);
            emit.entries.add(c.own(rest.substring(0, rest.indexOf('(')), rest.substring(rest.indexOf('('))));
        }
        emit.write(out, 0);
        rep.append("methods emitted ").append(emit.emitted.size()).append(", strings ").append(emit.strings.size()).append('\n');
        TreeMap<String, Integer> perMod = new TreeMap<>();
        for (Mth m : emit.emitted) perMod.merge(m.owner.mod.id + " " + (m.owner.name.startsWith("instrumented/") ? "instrumented" : m.owner.name.startsWith("java/") || m.owner.name.startsWith("jdk/") || m.owner.name.startsWith("sun/") ? "jdk" : "own"), 1, Integer::sum);
        perMod.forEach((k, v) -> rep.append("  ").append(k).append(' ').append(v).append('\n'));
        rep.append(String.format("translated in %.1f s%n", (System.nanoTime() - t0) / 1e9));
        Files.writeString(out.resolve("report.txt"), rep.toString());
        System.out.print(rep);
    }
}
