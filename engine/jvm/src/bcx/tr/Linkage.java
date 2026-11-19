package bcx.tr;

import bcx.tr.Cls.Fld;
import bcx.tr.Cls.Mth;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.module.ModuleDescriptor;
import java.util.*;

/**
 * The JVM's linkage errors for a robot's code (JVMS 5.4.3, 5.4.4), which a game can hit: the instrumenter rewrites
 * references (System to its injected System, java.util to instrumented copies), and some no longer resolve or are
 * no longer accessible from the team's loader. Such an instruction throws NoSuchMethodError, NoSuchFieldError,
 * IllegalAccessError, NoClassDefFoundError or IncompatibleClassChangeError when it executes, as in the official
 * engine. System code (the JDK, the engine) links as it does in the JVM.
 */
final class Linkage implements Opcodes {
    /** The packages the official engine's JVM opens to unnamed modules (build.gradle's run, --add-opens). */
    static final Set<String> OPENED = Set.of("jdk/internal/misc", "jdk/internal/math", "jdk/internal/util",
            "jdk/internal/access", "sun/security/action");

    final Pool pool;
    private final Map<String, Boolean> pkgAccessible = new HashMap<>();
    /** Instructions that throw a linkage error: error class (slash form) and message. */
    final Map<AbstractInsnNode, String[]> errors = new IdentityHashMap<>();

    Linkage(Pool pool) { this.pool = pool; }

    /** Is a public class of package pkg (slash form), outside the team's loader, accessible from the team's unnamed module? */
    boolean packageExported(String pkg) {
        return pkgAccessible.computeIfAbsent(pkg, p -> {
            if (OPENED.contains(p)) return true;
            String dotted = p.replace('/', '.');
            for (Module m : ModuleLayer.boot().modules()) {
                ModuleDescriptor d = m.getDescriptor();
                if (!d.packages().contains(dotted)) continue;
                for (ModuleDescriptor.Exports e : d.exports()) if (e.source().equals(dotted) && !e.isQualified()) return true;
                return false;
            }
            return true; // the class path: unnamed modules export everything
        });
    }

    /** Resolve a class reference from module code: null and an error recorded if it fails. */
    Cls cls(Pool.Module mod, Cls from, String name, AbstractInsnNode in) {
        if (!mod.perRobot) return pool.ref(mod, name);
        Cls c;
        try {
            c = pool.ref(mod, name);
            loadSupers(c, new HashSet<>());
        } catch (TranslateException e) {
            for (Throwable t = e; t != null; t = t.getCause()) {
                if (t instanceof TeamClassRefused r) {
                    System.err.println("translate: " + mod + ": the instrumenter refuses team class " + r.cls
                            + "; a robot reaching it ends the native game (TeamClassLoaderFactory.hasError is not modelled)");
                    errors.put(in, new String[]{"fatal", "team class " + r.cls + " refused by the instrumenter: the official"
                            + " engine marks the team broken (TeamClassLoaderFactory.hasError), not modelled"});
                    return null;
                }
            }
            // the official instrumenter refusing a class: its InstrumentationException reaches the bot
            for (Throwable t = e; t != null; t = t.getCause()) {
                if (t instanceof battlecode.instrumenter.InstrumentationException ie) {
                    errors.put(in, new String[]{"battlecode/instrumenter/InstrumentationException", ie.type.name() + ":" + t.getMessage().substring(ie.type.name().length() + 1)});
                    return null;
                }
            }
            errors.put(in, new String[]{"java/lang/NoClassDefFoundError", name});
            return null;
        }
        String vf = verifyLoadFailure(c);
        if (vf != null) {
            errors.put(in, new String[]{"java/lang/IncompatibleClassChangeError", vf});
            return null;
        }
        String sealed = sealedViolation(c);
        if (sealed != null) {
            errors.put(in, new String[]{"java/lang/IncompatibleClassChangeError", "class " + c.name.replace('/', '.')
                    + " cannot inherit from sealed class " + sealed.replace('/', '.')});
            return null;
        }
        if (!classAccessible(from, c)) {
            errors.put(in, new String[]{"java/lang/IllegalAccessError", "failed to access class " + c.name.replace('/', '.')
                    + " from class " + from.name.replace('/', '.')});
            return null;
        }
        return c;
    }

    /**
     * HotSpot's verifier, linking c, loads a class D created by one of c's methods when it checks D against the
     * method's class-typed (not interface) return type; if D cannot load (a sealed parent that does not list it),
     * linking c fails with D's error. This models that one case (java.util.EnumSet.noneOf's RegularEnumSet).
     */
    private final Map<Cls, Optional<String>> verifyCache = new HashMap<>();

    String verifyLoadFailure(Cls c) {
        Optional<String> o = verifyCache.get(c);
        if (o != null) return o.orElse(null);
        verifyCache.put(c, Optional.empty());
        String found = null;
        for (MethodNode m : c.node.methods) {
            Type rt = Type.getReturnType(m.desc);
            if (rt.getSort() != Type.OBJECT) continue;
            Cls ret;
            try { ret = pool.ref(c.mod, rt.getInternalName()); } catch (TranslateException e) { continue; }
            if (ret.isInterface()) continue;
            for (AbstractInsnNode in : m.instructions) {
                if (in.getOpcode() != NEW) continue;
                String dn = ((TypeInsnNode) in).desc;
                if (dn.equals(ret.name)) continue;
                Cls d;
                try { d = pool.ref(c.mod, dn); } catch (TranslateException e) { continue; }
                String sv = sealedViolation(d);
                if (sv != null && d.isSubclassOf(ret)) {
                    found = "class " + d.name.replace('/', '.') + " cannot inherit from sealed class " + sv.replace('/', '.');
                    break;
                }
            }
            if (found != null) break;
        }
        verifyCache.put(c, Optional.ofNullable(found));
        return found;
    }

    /** Loading a class loads its superclass and superinterfaces: a failure there is the reference's failure. */
    private static void loadSupers(Cls c, Set<Cls> seen) {
        if (!seen.add(c)) return;
        if (c.superCls() != null) loadSupers(c.superCls(), seen);
        for (Cls i : c.interfaces()) loadSupers(i, seen);
    }

    /** A class whose superclass or superinterface is sealed without listing it fails to load (JVMS 5.3.5). */
    static String sealedViolation(Cls c) {
        List<Cls> sups = new ArrayList<>();
        if (c.superCls() != null) sups.add(c.superCls());
        sups.addAll(c.interfaces());
        for (Cls s : sups) {
            List<String> p = s.node.permittedSubclasses;
            if (p != null && !p.contains(c.name)) return s.name;
        }
        return null;
    }

    boolean classAccessible(Cls from, Cls c) {
        if (c.mod == from.mod || from.synthetic) return true; // the same loader; javac-compiled code is consistent within it
        if ((c.node.access & ACC_PUBLIC) == 0) return false;
        return packageExported(c.pkg());
    }

    private boolean samePackage(Cls a, Cls b) { return a.mod == b.mod && a.pkg().equals(b.pkg()); }

    boolean memberAccessible(Cls from, Cls declaring, int access) {
        if (from == declaring) return true;
        if (from.synthetic) return true; // the JDK's spun lambda classes are hidden nestmates of their caller
        if ((access & ACC_PUBLIC) != 0) return true;
        if ((access & ACC_PRIVATE) != 0) return isNestmate(from, declaring);
        if (samePackage(from, declaring)) return true;
        if ((access & ACC_PROTECTED) != 0) return from.isSubclassOf(declaring);
        return false;
    }

    private boolean isNestmate(Cls a, Cls b) {
        if (a.mod != b.mod) return false;
        String ha = a.node.nestHostClass == null ? a.name : a.node.nestHostClass;
        String hb = b.node.nestHostClass == null ? b.name : b.node.nestHostClass;
        return ha.equals(hb);
    }

    /** The resolved method, or null with an error recorded (robot code only). */
    Mth method(Cls from, MethodInsnNode mi) {
        Pool.Module mod = from.mod;
        if (mi.owner.startsWith("[")) return null;
        Cls owner = cls(mod, from, mi.owner, mi);
        if (owner == null) return null;
        Mth r;
        if (owner.isInterface() != mi.itf && mod.perRobot) {
            errors.put(mi, new String[]{"java/lang/IncompatibleClassChangeError", "Found " + (owner.isInterface() ? "interface " : "class ")
                    + owner.name.replace('/', '.') + ", but " + (owner.isInterface() ? "class" : "interface") + " was expected"});
            return null;
        }
        r = owner.resolveMethod(mi.name, mi.desc, mi.itf);
        if (r == null) {
            if (!mod.perRobot) throw new TranslateException("unresolved method " + mi.owner + "." + mi.name + mi.desc + " in " + from);
            errors.put(mi, new String[]{"java/lang/NoSuchMethodError", "'" + javaSig(mi.owner, mi.name, mi.desc) + "'"});
            return null;
        }
        if (!mod.perRobot) return r;
        if (!memberAccessible(from, r.owner, r.node.access)) {
            errors.put(mi, new String[]{"java/lang/IllegalAccessError", "class " + from.name.replace('/', '.')
                    + " tried to access method '" + javaSig(r.owner.name, r.name, r.desc) + "'"});
            return null;
        }
        boolean wantStatic = mi.getOpcode() == INVOKESTATIC;
        if (wantStatic != r.isStatic()) {
            errors.put(mi, new String[]{"java/lang/IncompatibleClassChangeError", "Expected " + (wantStatic ? "static" : "non-static")
                    + " method '" + javaSig(r.owner.name, r.name, r.desc) + "'"});
            return null;
        }
        return r;
    }

    /** The resolved field, or null with an error recorded (robot code only). */
    Fld field(Cls from, FieldInsnNode fi) {
        Pool.Module mod = from.mod;
        Cls owner = cls(mod, from, fi.owner, fi);
        if (owner == null) return null;
        Fld f = owner.resolveField(fi.name, fi.desc);
        if (f == null) {
            if (!mod.perRobot) throw new TranslateException("unresolved field " + fi.owner + "." + fi.name);
            errors.put(fi, new String[]{"java/lang/NoSuchFieldError", fi.name});
            return null;
        }
        if (!mod.perRobot) return f;
        if (!memberAccessible(from, f.owner, f.node.access)) {
            errors.put(fi, new String[]{"java/lang/IllegalAccessError", "class " + from.name.replace('/', '.')
                    + " tried to access field " + f.owner.name.replace('/', '.') + "." + f.name});
            return null;
        }
        boolean wantStatic = fi.getOpcode() == GETSTATIC || fi.getOpcode() == PUTSTATIC;
        if (wantStatic != f.isStatic()) {
            errors.put(fi, new String[]{"java/lang/IncompatibleClassChangeError", "Expected " + (wantStatic ? "static" : "non-static")
                    + " field " + f.owner.name.replace('/', '.') + "." + f.name});
            return null;
        }
        return f;
    }

    static String javaSig(String owner, String name, String desc) {
        Type t = Type.getMethodType(desc);
        StringBuilder b = new StringBuilder(t.getReturnType().getClassName()).append(' ').append(owner.replace('/', '.'))
                .append('.').append(name).append('(');
        Type[] a = t.getArgumentTypes();
        for (int i = 0; i < a.length; i++) b.append(i > 0 ? ", " : "").append(a[i].getClassName());
        return b.append(')').toString();
    }
}
