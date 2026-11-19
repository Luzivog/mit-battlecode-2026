package bcx.tr;

import bcx.tr.Cls.Mth;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Methods implemented in C++ instead of translated: JVM natives, VM-bound JDK methods, and the engine's per-robot
 * classes (RobotMonitor, Clock, ObjectMethods). The table is engine/jvm/natives.txt:
 * {@code sys|bot CLASS NAME DESC CFUN [dep ...]}, a dep being {@code m:CLASS.NAME DESC} (a method that becomes
 * reachable) or {@code new:CLASS} (a class the native instantiates); {@code root CLASS NAME DESC} lines are methods
 * the runtime calls.
 */
final class Natives {
    static final class Impl {
        final String cfun;
        final List<String> deps;
        String cut;   // a method cut from the program: reaching it at run time is fatal
        final boolean nothrow;  // never leaves an exception pending: no check after the call
        Impl(String cfun, List<String> deps) {
            this.cfun = cfun;
            this.nothrow = deps.contains("nothrow");
            this.deps = deps.stream().filter(d -> !d.equals("nothrow")).toList();
        }
    }

    private final Map<String, Impl> sys = new HashMap<>(), bot = new HashMap<>();
    final List<String[]> roots = new ArrayList<>();
    final List<String[]> substitutions = new ArrayList<>();
    final List<String[]> patches = new ArrayList<>();
    final List<String> completes = new ArrayList<>();
    final List<String> perRobotRoots = new ArrayList<>();
    final Pool pool;

    Natives(Pool pool, Path table) throws Exception {
        this.pool = pool;
        int n = 0;
        for (String line : Files.readAllLines(table)) {
            n++;
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] t = line.split("\\s+");
            if (t[0].equals("root")) { roots.add(new String[]{t[1], t[2], t[3]}); continue; }
            if (t[0].equals("subst")) { substitutions.add(new String[]{t[1], t[2]}); continue; }
            if (t[0].equals("patch")) { patches.add(new String[]{t[1], t[2]}); continue; }
            if (t[0].equals("robotroot")) { perRobotRoots.add(t[1]); continue; }
            if (t[0].equals("cutclass")) { cutClasses.add(t[1]); continue; }
            if (t[0].equals("complete")) { completes.add(t[1]); continue; }
            if (t[0].equals("cut")) {
                Impl impl = new Impl("cut_" + Names.mangle(t[1] + "." + t[2]) + "_" + Names.descHash(t[3]), List.of());
                impl.cut = t[1] + "." + t[2] + t[3];
                sys.put(t[1] + "." + t[2] + t[3], impl);
                continue;
            }
            if (t.length < 5) throw new TranslateException("natives.txt:" + n + ": " + line);
            Impl impl = new Impl(t[4], Arrays.asList(t).subList(5, t.length));
            String key = t[1] + "." + t[2] + t[3];
            (t[0].equals("bot") ? bot : sys).put(key, impl);
        }
    }

    final List<Mth> cutsReached = new ArrayList<>();
    /** Classes never instantiated: a `new` of one is fatal at run time. */
    final Set<String> cutClasses = new HashSet<>();

    /** A cut class or a subclass of one. */
    boolean isCut(Cls c) {
        for (Cls s = c; s != null; s = s.superCls()) if (cutClasses.contains(s.name)) return true;
        return false;
    }

    Impl lookup(Mth m) {
        String key = m.owner.name + "." + m.name + m.desc;
        if (!m.owner.mod.perRobot) return sys.get(key);
        Impl i = bot.get(key);
        if (i == null && m.owner.name.startsWith("instrumented/")) {
            Impl c = sys.get(key);
            if (c != null && c.cut != null) i = c;   // a cut of an instrumented JDK path applies in every team
        }
        return i;
    }

    boolean isIntrinsicCall(Pool.Module mod, MethodInsnNode mi) { return false; }

    void reachIntrinsic(Rta rta, Pool.Module mod, MethodInsnNode mi) {}

    void reachDep(Rta rta, Pool.Module mod, String dep) {
        if (dep.startsWith("new:")) {
            Cls c = pool.ref(mod, dep.substring(4));
            rta.instantiate(c);
            rta.init(c);
        } else if (dep.startsWith("inst:")) {
            rta.instantiate(pool.ref(mod, dep.substring(5)));   // made natively; its class initialization is not observable
        } else if (dep.startsWith("m:")) {
            String d = dep.substring(2);
            int dot = d.lastIndexOf('.', d.indexOf('('));
            String cn = d.substring(0, dot), rest = d.substring(dot + 1);
            int par = rest.indexOf('(');
            Cls c = pool.ref(mod, cn);
            Mth m = c.resolveMethod(rest.substring(0, par), rest.substring(par), c.isInterface());
            if (m == null) throw new TranslateException("native dep not found: " + dep);
            if (m.isStatic()) rta.init(c);
            if (m.isStatic() || m.name.equals("<init>") || m.isPrivate()) rta.reach(m);
            else rta.virtualCall(m);
        } else if (dep.startsWith("init:")) {
            rta.init(pool.ref(mod, dep.substring(5)));
        } else throw new TranslateException("bad native dep " + dep);
    }

    void reachRoots(Rta rta) {
        for (String[] r : roots) reachDep(rta, pool.sys, "m:" + r[0] + "." + r[1] + r[2]);
    }

    static String sig(Mth m) {
        StringBuilder b = new StringBuilder(Layout.ctype(m.ret())).append('(');
        boolean first = true;
        if (!m.isStatic()) { b.append("Obj*"); first = false; }
        for (Type a : m.args()) { if (!first) b.append(','); b.append(Layout.ctype(a)); first = false; }
        return b.append(')').toString();
    }
}
