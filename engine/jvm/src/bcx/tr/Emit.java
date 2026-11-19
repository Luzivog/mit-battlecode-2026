package bcx.tr;

import bcx.tr.Cls.Fld;
import bcx.tr.Cls.Mth;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Writes the whole program: types.h and types_<team>.h (layouts, statics, prototypes), klasses.cpp (type info, init)
 * and the methods, m_<module>_<n>.cpp.
 */
final class Emit implements Opcodes {
    final Pool pool;
    final Rta rta;
    final Layout layout;
    final Natives natives;
    final Map<String, Integer> strings = new LinkedHashMap<>();
    final Map<String, String[]> arrays = new TreeMap<>();   // symbol -> {dims, elem symbol or prim desc, internal name}
    final List<Mth> emitted = new ArrayList<>();
    final Map<Pool.Module, List<Cls>> initOrder = new LinkedHashMap<>();
    final List<Mth> entries = new ArrayList<>();

    Emit(Pool pool, Rta rta, Layout layout, Natives natives) {
        this.pool = pool;
        this.rta = rta;
        this.layout = layout;
        this.natives = natives;
        // init indices: classes whose initialization does something (or whose supers' does)
        for (Cls c : layout.order) {
            if (!c.initReachable || !needsInit(c, new HashMap<>())) continue;
            List<Cls> l = initOrder.computeIfAbsent(c.mod, k -> new ArrayList<>());
            c.initIndex = l.size();
            l.add(c);
        }
    }

    /** Initialization does something: a <clinit>, constant fields, or a super(interface) that needs it. */
    static boolean needsInit(Cls c, Map<Cls, Boolean> memo) {
        Boolean b = memo.get(c);
        if (b != null) return b;
        memo.put(c, false);
        boolean r = false;
        Mth cl = c.own("<clinit>", "()V");
        if (cl != null && cl.reachable && !"rt_monitor_noop".equals(cl.nativeImpl)) r = true;
        for (Fld f : c.fields.values()) if (f.isStatic() && f.node.value != null) r = true;
        if (!c.isInterface()) {
            if (c.superCls() != null && c.superCls().initReachable && needsInit(c.superCls(), memo)) r = true;
            for (Cls i : c.allInterfaces()) if (i.initReachable && Rta.hasDefaults(i) && needsInit(i, memo)) r = true;
        }
        memo.put(c, r);
        return r;
    }

    int str(String s) { return strings.computeIfAbsent(s, k -> strings.size()); }

    String klass(Cls c) {
        if (c.id < 0) throw new TranslateException("class without layout: " + c);
        return "K_" + c.cname;
    }

    String arrayKlass(Pool.Module mod, String desc) {
        Type t = Type.getType(desc);
        int dims = t.getDimensions();
        Type e = t.getElementType();
        String elem, ename;
        if (e.getSort() == Type.OBJECT) {
            Cls ec = pool.ref(mod, e.getInternalName());
            elem = klass(ec);
            ename = "L" + ec.name + ";";
        } else {
            elem = "K_prim_" + e.getDescriptor();
            ename = e.getDescriptor();
        }
        String sym = null;
        for (int d = 1; d <= dims; d++) {
            sym = "KA" + d + "_" + elem.substring(2);
            if (!arrays.containsKey(sym)) arrays.put(sym, new String[]{"" + d, elem, "[".repeat(d) + ename});
        }
        return sym;
    }

    String initFn(Cls c) { return "ci_" + c.cname; }

    static String fn(Mth t) { return t.nativeImpl != null ? t.nativeImpl : t.cname; }

    void invoke(Gen g, MethodInsnNode mi) {
        int op = mi.getOpcode();
        Type[] at = Type.getArgumentTypes(mi.desc);
        Type rt = Type.getReturnType(mi.desc);
        boolean inst = op != INVOKESTATIC;
        int n = at.length + (inst ? 1 : 0);
        int base = g.sp() - n;
        List<String> args = new ArrayList<>();
        for (int i = 0; i < n; i++) args.add(g.s(base + i, g.skind(base + i)));
        String res = rt.getSort() == Type.VOID ? null : g.s(base, Layout.kind(rt));
        String assign = res == null ? "" : res + " = ";
        String argl = String.join(", ", args);
        if (mi.owner.startsWith("[")) {
            g.nullchk(args.get(0));
            if (mi.name.equals("clone")) { g.line(assign + "rt_array_clone(" + args.get(0) + "); " + g.chk()); return; }
            Mth r = pool.get(pool.sys, "java/lang/Object").resolveMethod(mi.name, mi.desc, false);
            g.line(assign + "((" + Layout.fnType(r) + ")(" + args.get(0) + "->klass->vtable[" + r.vslot + "]))(" + argl + "); " + g.chk());
            return;
        }
        Cls owner = pool.ref(g.mod, mi.owner);
        Mth r = owner.resolveMethod(mi.name, mi.desc, mi.itf);
        if (r == null) throw new TranslateException("unresolved " + mi.owner + "." + mi.name + mi.desc + " in " + g.m);
        String call;
        switch (op) {
            case INVOKESTATIC -> {
                g.initChk(r.owner);
                call = fn(r) + "(" + argl + ")";
            }
            case INVOKESPECIAL -> {
                if (r.name.equals("<init>") && natives.isCut(owner) && !g.cls.isSubclassOf(owner)) {
                    g.line("rt_fatal(\"cut class constructed: %s\", " + Names.cstr(owner.name) + ");");
                    return;
                }
                Mth t = rta.special(g.cls, owner, r);
                if (!mi.name.equals("<init>")) g.nullchk(args.get(0));
                call = fn(t) + "(" + argl + ")";
            }
            default -> {
                g.nullchk(args.get(0));
                Mth d = !layout.anyInstance(owner) ? null : r.isPrivate() ? r : layout.devirtualize(owner, r);
                if (!layout.anyInstance(owner)) call = "rt_no_receiver<" + Layout.ctype(rt) + ">(" + str(r.toString()) + ")";
                else if (d != null) call = fn(d) + "(" + argl + ")";
                else if (r.owner.isInterface()) {
                    int k = layout.itableIndex(r.owner, r);
                    call = "((" + Layout.fnType(r) + ")(rt_itable(" + args.get(0) + "->klass, &" + klass(r.owner) + ")[" + k + "]))(" + argl + ")";
                } else {
                    int slot = layout.vslot(owner, r);
                    call = "((" + Layout.fnType(r) + ")(" + args.get(0) + "->klass->vtable[" + slot + "]))(" + argl + ")";
                }
            }
        }
        Mth tgt = op == INVOKESTATIC ? r : null;
        boolean nothrow = tgt != null && tgt.nothrow;
        g.line(assign + call + ";" + (nothrow ? "" : " " + g.chk()));
    }

    // ---------------- files
    void write(Path out, int filesHint) throws IOException {
        Files.createDirectories(out);
        // generate method bodies first (collects strings and array klasses), grouped by module so that a change to
        // one bot rewrites only that bot's method files
        Map<Pool.Module, List<String>> bodies = new TreeMap<>(Comparator.comparingInt((Pool.Module mo) -> mo.index));
        for (Cls c : layout.order) {
            for (Mth m : c.methods.values()) {
                if (!m.reachable || m.isAbstract() || m.nativeImpl != null) continue;
                bodies.computeIfAbsent(c.mod, k -> new ArrayList<>()).add(new Gen(this, m).gen() + "\n");
                m.emitted = true;
                emitted.add(m);
            }
        }
        for (String a : rta.arrayTypes) {
            Type t = Type.getType(a);
            // array types of classes outside any module the code references are skipped by ref()
            try { arrayKlass(moduleForArray(a), a); } catch (TranslateException e) { /* class unused */ }
        }
        writeTypes(out);
        writeKlasses(out.resolve("klasses.cpp"));
        // method files m_<module>_<n>.cpp: each module's methods packed into files of bounded size
        Map<String, StringBuilder> files = new LinkedHashMap<>();
        Map<String, Pool.Module> fileModule = new HashMap<>();
        long limit = 1_000_000;
        for (var e : bodies.entrySet()) {
            StringBuilder curf = null;
            int n = 0;
            for (String sb : e.getValue()) {
                if (curf == null || curf.length() + sb.length() > limit) {
                    curf = new StringBuilder();
                    String fn = "m_" + e.getKey().id + "_" + n++ + ".cpp";
                    files.put(fn, curf);
                    fileModule.put(fn, e.getKey());
                }
                curf.append(sb);
            }
        }
        // delete stale method files, then write the changed ones
        try (var ls = Files.list(out)) {
            for (Path p : ls.toList()) {
                String fn = p.getFileName().toString();
                if (fn.matches("m.*\\.cpp") && !files.containsKey(fn)) Files.delete(p);
            }
        }
        for (var e : files.entrySet())
            writeIfChanged(out.resolve(e.getKey()), includes(fileModule.get(e.getKey()), e.getValue()) + e.getValue());
    }

    private Pool.Module moduleForArray(String desc) { return pool.sys; }

    static void writeIfChanged(Path p, String s) throws IOException {
        if (Files.exists(p) && Files.readString(p).equals(s)) return;
        Files.writeString(p, s);
    }

    /**
     * types.h holds the system module's layouts, statics and prototypes; types_<team>.h each team's (so a changed bot
     * rewrites only its own header). A symbol's module is in its name: c<module index>_...
     */
    private void writeTypes(Path out) throws IOException {
        Map<Integer, StringBuilder> hb = new TreeMap<>();
        java.util.function.IntFunction<StringBuilder> sb = i -> hb.computeIfAbsent(i, k -> new StringBuilder());
        for (Cls c : layout.order) if (!c.isInterface()) sb.apply(c.mod.index).append("struct ").append(c.cname).append(";\n#define HAS_").append(c.cname).append(" 1\n");
        for (Cls c : layout.order) {
            if (c.isInterface()) continue;
            Cls s = c.superCls();
            StringBuilder b = sb.apply(c.mod.index);
            b.append("struct ").append(c.cname).append(" : ").append(s == null ? "Obj" : s.cname).append(" {");
            for (Fld f : c.fields.values()) if (!f.isStatic()) b.append(' ').append(Layout.ctype(Type.getType(f.desc))).append(' ').append(f.cname).append(';');
            b.append(" };\n");
        }
        for (Cls c : layout.order) {
            if (c.staticFields.isEmpty()) continue;
            StringBuilder b = sb.apply(c.mod.index);
            b.append("struct S_").append(c.cname).append(" {");
            for (Fld f : c.staticFields) b.append(' ').append(Layout.ctype(Type.getType(f.desc))).append(' ').append(f.cname).append(';');
            b.append(" };\n");
            if (!c.mod.perRobot) b.append("extern S_").append(c.cname).append(" s_").append(c.cname).append(";\n");
        }
        for (Pool.Module m : pool.teams) {
            StringBuilder b = sb.apply(m.index);
            b.append("struct BS").append(m.index).append(" {\n");
            for (Cls c : layout.order) if (c.mod == m && !c.staticFields.isEmpty()) b.append("  S_").append(c.cname).append(' ').append(c.cname).append(";\n");
            int n = initOrder.getOrDefault(m, List.of()).size();
            b.append("  uint8_t init[").append(Math.max(n, 1)).append("];\n};\n");
        }
        sb.apply(0).append("extern uint8_t g_init0[];\n\n");
        for (Cls c : layout.order) sb.apply(c.mod.index).append("extern Klass ").append(klass(c)).append(";\n");
        for (var a : arrays.entrySet()) sb.apply(symbolModule(a.getValue()[1])).append("extern Klass ").append(a.getKey()).append(";\n");
        for (Cls c : layout.order) if (c.initIndex >= 0) sb.apply(c.mod.index).append("void ").append(initFn(c)).append("();\n");
        for (Mth m : emitted) sb.apply(m.owner.mod.index).append(Layout.proto(m, m.cname)).append(";\n");
        Set<String> protos = new HashSet<>();
        for (Cls c : layout.order)
            for (Mth m : c.methods.values())
                if (m.reachable && m.nativeImpl != null && protos.add(m.nativeImpl))
                    sb.apply(0).append(Layout.proto(m, m.nativeImpl)).append(";\n");
        String gen = "// generated by bcx.tr: do not edit\n#pragma once\n";
        writeIfChanged(out.resolve("types.h"), gen + "#include \"rt.h\"\n\n" + sb.apply(0));
        Set<String> headers = new HashSet<>();
        for (Pool.Module m : pool.teams) {
            String h = header(m);
            headers.add(h);
            writeIfChanged(out.resolve(h), gen + "#include \"types.h\"\n\n" + sb.apply(m.index));
        }
        try (var ls = Files.list(out)) {
            for (Path q : ls.toList()) {
                String fn = q.getFileName().toString();
                if (fn.startsWith("types_") && fn.endsWith(".h") && !headers.contains(fn)) Files.delete(q);
            }
        }
    }

    static String header(Pool.Module m) { return "types_" + m.id + ".h"; }

    /** The module index in a symbol (K_c3_..., KA1_c3_..., c3_...); K_prim_ and others: the system module. */
    static int symbolModule(String sym) {
        var mt = MODULE_REF.matcher(sym);
        return mt.find() ? Integer.parseInt(mt.group(1)) : 0;
    }

    static final java.util.regex.Pattern MODULE_REF = java.util.regex.Pattern.compile("(?<![A-Za-z0-9])c(\\d+)_");

    /** The #include lines of a method file of module m: types.h, m's header, and the header of every team it names. */
    String includes(Pool.Module m, CharSequence code) {
        Set<Integer> refs = new TreeSet<>();
        if (m.perRobot) refs.add(m.index);
        var mt = MODULE_REF.matcher(code);
        while (mt.find()) refs.add(Integer.parseInt(mt.group(1)));
        StringBuilder b = new StringBuilder("#include \"types.h\"\n");
        for (Pool.Module t : pool.teams) if (refs.contains(t.index)) b.append("#include \"").append(header(t)).append("\"\n");
        return b.append('\n').toString();
    }

    private void writeKlasses(Path p) throws IOException {
        StringBuilder b = new StringBuilder("// generated by bcx.tr: do not edit\n#include \"types.h\"\n");
        for (Pool.Module m : pool.teams) b.append("#include \"").append(header(m)).append("\"\n");
        b.append('\n');
        int n0 = initOrder.getOrDefault(pool.sys, List.of()).size();
        b.append("uint8_t g_init0[").append(Math.max(n0, 1)).append("];\n");
        for (Cls c : layout.order)
            if (!c.staticFields.isEmpty() && !c.mod.perRobot) b.append("S_").append(c.cname).append(" s_").append(c.cname).append(";\n");
        b.append('\n');
        // vtables, itables, displays
        for (Cls c : layout.order) {
            if (c.vtable != null && !c.vtable.isEmpty()) {
                b.append("static void* vt_").append(c.cname).append("[] = {");
                for (Mth m : c.vtable) b.append(' ').append(slotFn(m)).append(',');
                b.append(" };\n");
            }
            if (c.itables != null && !c.itables.isEmpty()) {
                int k = 0;
                for (Map.Entry<Cls, Mth[]> e : c.itables.entrySet()) {
                    b.append("static void* it_").append(c.cname).append('_').append(k++).append("[] = {");
                    for (Mth m : e.getValue()) b.append(' ').append(m == null ? "(void*)&rt_abstract_method" : slotFn(m)).append(',');
                    if (e.getValue().length == 0) b.append(" nullptr");
                    b.append(" };\n");
                }
            }
            List<Cls> ifs = new ArrayList<>(c.allInterfaces());
            ifs.removeIf(i -> i.id < 0);
            if (!ifs.isEmpty()) {
                b.append("static Klass* if_").append(c.cname).append("[] = {");
                for (Cls i : ifs) b.append(" &").append(klass(i)).append(',');
                b.append(" };\n");
                if (c.itables != null) {
                    b.append("static void** its_").append(c.cname).append("[] = {");
                    int k = 0;
                    Map<Cls, Integer> pos = new HashMap<>();
                    for (Cls i : c.itables.keySet()) pos.put(i, k++);
                    for (Cls i : ifs) {
                        Integer x = pos.get(i);
                        b.append(' ').append(x == null ? "nullptr" : "it_" + c.cname + "_" + x).append(',');
                    }
                    b.append(" };\n");
                }
            }
            if (!c.isInterface()) {
                List<Cls> chain = new ArrayList<>();
                for (Cls s = c; s != null; s = s.superCls()) chain.add(0, s);
                b.append("static Klass* dp_").append(c.cname).append("[] = {");
                for (Cls s : chain) b.append(" &").append(klass(s)).append(',');
                b.append(" };\n");
            }
        }
        b.append('\n');
        for (Cls c : layout.order) {
            List<Cls> ifs = new ArrayList<>(c.allInterfaces());
            ifs.removeIf(i -> i.id < 0);
            int depth = 0;
            for (Cls s = c.superCls(); s != null; s = s.superCls()) depth++;
            int flags = 0;
            if (c.isInterface()) flags |= 1;
            if (c.isAbstract()) flags |= 4;
            if ((c.node.access & ACC_ENUM) != 0) flags |= 8;
            flags |= (c.node.access & 0x7fff) << 8;
            Mth values = (c.node.access & ACC_ENUM) != 0 ? c.own("values", "()[L" + c.name + ";") : null;
            Mth clinit = c.own("<clinit>", "()V");
            boolean hasClinit = clinit != null && clinit.reachable;
            b.append("Klass ").append(klass(c)).append(" = { ")
                    .append(Names.cstr(c.name)).append(", ")
                    .append(c.superCls() == null ? "nullptr" : "&" + klass(c.superCls())).append(", ")
                    .append(ifs.isEmpty() ? "nullptr" : "if_" + c.cname).append(", ")
                    .append(c.itables != null && !ifs.isEmpty() ? "its_" + c.cname : "nullptr").append(", ")
                    .append(ifs.size()).append(", ")
                    .append(depth).append(", ")
                    .append(c.isInterface() ? "nullptr" : "dp_" + c.cname).append(", ")
                    .append(c.isInterface() ? "0" : "sizeof(" + c.cname + ")").append(", ")
                    .append(flags).append(", 0, nullptr, nullptr, nullptr, ")
                    .append(c.mod.index).append(", ")
                    .append(c.initIndex).append(", ")
                    .append(values != null && values.reachable ? "(void*)&" + fn(values) : "nullptr").append(", ")
                    .append(c.vtable == null ? 0 : c.vtable.size()).append(", ")
                    .append(c.vtable == null || c.vtable.isEmpty() ? "nullptr" : "vt_" + c.cname).append(", ")
                    .append(c.node.signature == null ? "nullptr" : Names.cstr(c.node.signature)).append(", ")
                    .append(c.initIndex >= 0 ? initFn(c) : "nullptr")
                    .append(" };\n");
        }
        for (Map.Entry<String, String[]> e : arrays.entrySet()) {
            String[] a = e.getValue();
            int dims = Integer.parseInt(a[0]);
            String comp = dims == 1 ? a[1] : "KA" + (dims - 1) + "_" + a[1].substring(2);
            boolean prim = dims == 1 && a[1].startsWith("K_prim_");
            String et = prim ? "'" + a[1].substring(7) + "'" : "'L'";
            Cls obj = pool.find(pool.sys, "java/lang/Object");
            b.append("Klass ").append(e.getKey()).append(" = { ").append(Names.cstr(a[2]))
                    .append(", &").append(klass(obj)).append(", nullptr, nullptr, 0, 1, dp_").append(obj.cname)
                    .append(", 0, 2, ").append(et).append(", &").append(comp).append(", nullptr, nullptr, 0, -1, nullptr, ")
                    .append(obj.vtable.size()).append(", vt_").append(obj.cname).append(", nullptr, nullptr };\n");
        }
        b.append("Klass* const g_all_klasses[] = {");
        for (Cls c : layout.order) b.append(" &").append(klass(c)).append(',');
        b.append(" nullptr };\nconst int g_n_klasses = ").append(layout.order.size()).append(";\n");
        // array klass links (component -> array)
        b.append("\nvoid rt_link_arrays() {\n");
        for (Map.Entry<String, String[]> e : arrays.entrySet()) {
            String[] a = e.getValue();
            int dims = Integer.parseInt(a[0]);
            String comp = dims == 1 ? a[1] : "KA" + (dims - 1) + "_" + a[1].substring(2);
            b.append("  ").append(comp).append(".array_klass = &").append(e.getKey()).append(";\n");
        }
        b.append("}\n\n");
        // class initialization (JVMS 5.5)
        for (Cls c : layout.order) {
            if (c.initIndex < 0) continue;
            String st = c.mod.perRobot ? "((BS" + c.mod.index + "*)g_S)->init[" + c.initIndex + "]" : "g_init0[" + c.initIndex + "]";
            b.append("void ").append(initFn(c)).append("() {\n");
            b.append("  uint8_t& st = ").append(st).append(";\n");
            b.append("  if (st == 2 || st == 1) return;\n");
            b.append("  if (st == 3) { rt_throw_ncdfe(&").append(klass(c)).append("); return; }\n");
            b.append("  st = 1;\n");
            List<Cls> pre = new ArrayList<>();
            if (!c.isInterface() && c.superCls() != null && c.superCls().initIndex >= 0) pre.add(c.superCls());
            if (!c.isInterface()) for (Cls i : c.allInterfaces()) if (i.initIndex >= 0 && Rta.hasDefaults(i)) pre.add(i);
            for (Cls s : pre) b.append("  ").append(initFn(s)).append("(); if (g_exc) { st = 3; return; }\n");
            for (Fld f : c.staticFields) {
                if (f.node.value == null) continue;
                String ref = (c.mod.perRobot ? "((BS" + c.mod.index + "*)g_S)->" + c.cname : "s_" + c.cname) + "." + f.cname;
                Object v = f.node.value;
                String lit;
                if (v instanceof String s) lit = "g_str[" + str(s) + "]";
                else if (v instanceof Float x) lit = "rt_fbits(0x" + Integer.toHexString(Float.floatToRawIntBits(x)) + "u)";
                else if (v instanceof Double x) lit = "rt_dbits(0x" + Long.toHexString(Double.doubleToRawLongBits(x)) + "ull)";
                else if (v instanceof Long x) lit = x == Long.MIN_VALUE ? "(jlong)0x8000000000000000ull" : x + "ll";
                else if (v instanceof Integer x) lit = x == Integer.MIN_VALUE ? "(jint)0x80000000" : x.toString();
                else throw new TranslateException("constant " + v);
                b.append("  ").append(ref).append(" = ").append(lit).append(";\n");
            }
            Mth clinit = c.own("<clinit>", "()V");
            if (clinit != null && clinit.reachable) {
                b.append("  ").append(fn(clinit)).append("();\n");
                b.append("  if (g_exc) { rt_init_failed(); st = 3; return; }\n");
            }
            b.append("  st = 2;\n}\n");
        }
        // strings
        b.append("\nObj* g_str[").append(Math.max(1, strings.size())).append("];\n");
        int k = 0;
        for (String s : strings.keySet()) {
            b.append("static const jchar sd_").append(k++).append("[] = {");
            for (int i = 0; i < s.length(); i++) b.append(i == 0 ? "" : ",").append((int) s.charAt(i));
            if (s.isEmpty()) b.append('0');
            b.append("};\n");
        }
        b.append("void rt_init_strings() {\n");
        k = 0;
        for (String s : strings.keySet()) { b.append("  g_str[").append(k).append("] = rt_make_string(sd_").append(k).append(", ").append(s.length()).append(");\n"); k++; }
        b.append("}\n");
        // module table
        b.append("\nconst ModuleInfo g_modules[] = {\n");
        for (Pool.Module m : pool.teams) {
            Cls rp = pool.find(m, m.id + "/RobotPlayer");
            Mth run = rp == null ? null : rp.own("run", "(Lbattlecode/common/RobotController;)V");
            b.append("  { ").append(Names.cstr(m.id)).append(", sizeof(BS").append(m.index).append("), offsetof(BS").append(m.index)
                    .append(", init), ").append(rp != null && rp.initIndex >= 0 ? initFn(rp) : "nullptr").append(", ")
                    .append(run != null && run.reachable ? "&" + fn(run) : "nullptr").append(" },\n");
        }
        b.append("  { nullptr, 0, 0, nullptr, nullptr }\n};\n");
        b.append(Glue.code(this));
        writeIfChanged(p, b.toString());
    }

    String slotFn(Mth m) {
        if (m == null || m.isAbstract() || !m.reachable) return "(void*)&rt_abstract_method";
        return "(void*)&" + fn(m);
    }
}
