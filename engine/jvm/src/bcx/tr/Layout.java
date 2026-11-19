package bcx.tr;

import bcx.tr.Cls.Fld;
import bcx.tr.Cls.Mth;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

/** Object layouts, vtables and itables, devirtualization by the RTA's instantiated set. */
final class Layout implements Opcodes {
    final Pool pool;
    final Rta rta;
    final List<Cls> order = new ArrayList<>();               // used classes, supertypes first
    final Map<Cls, Map<String, Integer>> mirandas = new HashMap<>();
    final Map<Cls, List<Mth>> itableMethods = new HashMap<>(); // per interface: its dispatchable methods

    Layout(Pool pool, Rta rta) {
        this.pool = pool;
        this.rta = rta;
        Set<Cls> seen = new HashSet<>();
        for (Cls c : rta.used) visit(c, seen);
        int id = 0;
        for (Cls c : order) c.id = id++;
        for (Cls c : order) if (!c.isInterface()) vtable(c);
        for (Cls c : order) if (c.isInterface()) itableMethods.put(c, ifaceMethods(c));
        for (Cls c : order) if (c.instantiated) itables(c);
        for (Cls c : order) {
            c.staticFields = new ArrayList<>();
            for (Fld f : c.fields.values()) if (f.isStatic()) c.staticFields.add(f);
        }
    }

    private void visit(Cls c, Set<Cls> seen) {
        if (!seen.add(c)) return;
        if (c.superCls() != null) visit(c.superCls(), seen);
        for (Cls i : c.interfaces()) visit(i, seen);
        order.add(c);
    }

    private static boolean virtualCandidate(Mth m) {
        return !m.isStatic() && !m.isPrivate() && !m.name.equals("<init>") && !m.name.equals("<clinit>");
    }

    private void vtable(Cls c) {
        if (c.vtable != null) return;
        List<Mth> vt = c.superCls() == null ? new ArrayList<>() : new ArrayList<>(c.superCls().vtable);
        for (Mth m : c.methods.values()) {
            if (!virtualCandidate(m)) continue;
            int slot = -1;
            for (int i = 0; i < vt.size(); i++) {
                Mth s = vt.get(i);
                if (s.name.equals(m.name) && s.desc.equals(m.desc) && !s.owner.isInterface() && Cls.overrides(m, s)) {
                    slot = i;
                    break;
                }
            }
            if (slot >= 0) { vt.set(slot, m); m.vslot = slot; }
            else { m.vslot = vt.size(); vt.add(m); }
        }
        // interface methods reachable by invokevirtual on this class: one slot per signature
        Map<String, Integer> mir = new HashMap<>(c.superCls() == null ? Map.of() : mirandas.getOrDefault(c.superCls(), Map.of()));
        for (Cls i : c.allInterfaces()) {
            for (Mth m : i.methods.values()) {
                if (!virtualCandidate(m)) continue;
                String sig = m.name + m.desc;
                boolean inClass = false;
                for (Mth s : vt) if (s.name.equals(m.name) && s.desc.equals(m.desc) && !s.owner.isInterface()) { inClass = true; break; }
                if (inClass) continue;
                Mth sel = c.isAbstract() || c.isInterface() ? c.maximallySpecific(m.name, m.desc, true) : c.select(m);
                if (sel == null) sel = m;
                Integer slot = mir.get(sig);
                if (slot == null) { mir.put(sig, vt.size()); vt.add(sel); }
                else vt.set(slot, sel);
            }
        }
        mirandas.put(c, mir);
        c.vtable = vt;
    }

    private List<Mth> ifaceMethods(Cls i) {
        List<Mth> l = new ArrayList<>();
        for (Mth m : i.methods.values()) if (virtualCandidate(m)) l.add(m);
        return l;
    }

    private void itables(Cls c) {
        c.itables = new LinkedHashMap<>();
        for (Cls i : c.allInterfaces()) {
            List<Mth> ms = itableMethods.get(i);
            if (ms == null) continue;
            Mth[] t = new Mth[ms.size()];
            for (int k = 0; k < t.length; k++) t[k] = c.select(ms.get(k));
            c.itables.put(i, t);
        }
    }

    /** vtable slot for an invokevirtual whose resolved method is r, on static receiver class c. */
    int vslot(Cls c, Mth r) {
        if (!r.owner.isInterface()) {
            if (r.vslot < 0) throw new TranslateException("no vtable slot for " + r);
            return r.vslot;
        }
        Integer s = mirandas.getOrDefault(c, Map.of()).get(r.name + r.desc);
        if (s == null) throw new TranslateException("no miranda slot for " + r + " in " + c);
        return s;
    }

    int itableIndex(Cls iface, Mth r) {
        List<Mth> ms = itableMethods.get(iface);
        int k = ms == null ? -1 : ms.indexOf(r);
        if (k < 0) throw new TranslateException("no itable entry for " + r + " in " + iface);
        return k;
    }

    /** The single method a virtual call can reach, or null if several (or none). */
    Mth devirtualize(Cls staticCls, Mth r) {
        if (r.isPrivate()) return r;
        Mth only = null;
        for (Cls c : rta.instantiated) {
            if (!c.isSubtypeOf(staticCls)) continue;
            Mth t = c.select(r);
            if (t == null || t.isAbstract()) return null;
            if (only == null) only = t;
            else if (only != t) return null;
        }
        return only;
    }

    boolean anyInstance(Cls staticCls) {
        for (Cls c : rta.instantiated) if (c.isSubtypeOf(staticCls)) return true;
        return false;
    }

    // ---------------- C++ types
    static String ctype(Type t) {
        return switch (t.getSort()) {
            case Type.BOOLEAN -> "jboolean";
            case Type.BYTE -> "jbyte";
            case Type.CHAR -> "jchar";
            case Type.SHORT -> "jshort";
            case Type.INT -> "jint";
            case Type.LONG -> "jlong";
            case Type.FLOAT -> "jfloat";
            case Type.DOUBLE -> "jdouble";
            case Type.VOID -> "void";
            default -> "Obj*";
        };
    }

    /** The stack kind a value of this type has on the operand stack. */
    static char kind(Type t) {
        return switch (t.getSort()) {
            case Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> 'i';
            case Type.LONG -> 'j';
            case Type.FLOAT -> 'f';
            case Type.DOUBLE -> 'd';
            case Type.VOID -> 'v';
            default -> 'a';
        };
    }

    static String kctype(char k) {
        return switch (k) {
            case 'i' -> "jint";
            case 'j' -> "jlong";
            case 'f' -> "jfloat";
            case 'd' -> "jdouble";
            case 'a' -> "Obj*";
            default -> throw new IllegalArgumentException("kind " + k);
        };
    }

    static String fnType(Mth m) {
        StringBuilder b = new StringBuilder(ctype(m.ret())).append("(*)(");
        boolean first = true;
        if (!m.isStatic()) { b.append("Obj*"); first = false; }
        for (Type a : m.args()) { if (!first) b.append(", "); b.append(ctype(a)); first = false; }
        return b.append(")").toString();
    }

    static String proto(Mth m, String fname) {
        StringBuilder b = new StringBuilder(ctype(m.ret())).append(' ').append(fname).append('(');
        boolean first = true;
        int i = 0;
        if (!m.isStatic()) { b.append("Obj* p0"); first = false; i++; }
        for (Type a : m.args()) { if (!first) b.append(", "); b.append(ctype(a)).append(" p").append(i++); first = false; }
        return b.append(")").toString();
    }
}
