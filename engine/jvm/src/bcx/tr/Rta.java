package bcx.tr;

import bcx.tr.Cls.Fld;
import bcx.tr.Cls.Mth;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

/** Rapid type analysis: which methods can run, which classes are instantiated or initialized. */
public final class Rta implements Opcodes {
    final Pool pool;
    final Natives natives;
    final Indy indy;
    final ArrayDeque<Mth> work = new ArrayDeque<>();
    final Map<Cls, Set<Mth>> virtualCalls = new HashMap<>();
    final List<Cls> instantiated = new ArrayList<>();
    final Set<Cls> used = new LinkedHashSet<>();   // classes needing a Klass
    final List<String> problems = new ArrayList<>();
    final Set<String> arrayTypes = new TreeSet<>();
    final Linkage link;

    Rta(Pool pool, Natives natives) {
        this.pool = pool;
        this.natives = natives;
        this.indy = new Indy(pool);
        this.link = new Linkage(pool);
    }

    void use(Cls c) {
        if (!used.add(c)) return;
        if (c.superCls() != null) use(c.superCls());
        for (Cls i : c.interfaces()) use(i);
    }

    Mth current;

    void reach(Mth m) {
        if (m.reachable) return;
        m.reachable = true;
        m.from = current;
        use(m.owner);
        work.add(m);
    }

    void instantiate(Cls c) {
        use(c);
        if (c.instantiated) return;
        if (c.isInterface() || c.isAbstract()) return;
        c.instantiated = true;
        instantiated.add(c);
        // StreamMethods.toList calls a public toArray() reflectively on whatever it gets
        Mth ta = c.resolveMethod("toArray", "()[Ljava/lang/Object;", false);
        if (ta != null && !ta.isStatic() && (ta.node.access & ACC_PUBLIC) != 0) {
            Mth sel = c.select(ta);
            if (sel != null && !sel.isAbstract()) reach(sel);
        }
        if (c.name.equals(System.getenv("BCX_WHY"))) System.err.println("WHY " + c + " instantiated in " + current + chain(current));
        List<Cls> sups = new ArrayList<>();
        for (Cls s = c; s != null; s = s.superCls()) sups.add(s);
        sups.addAll(c.allInterfaces());
        for (Cls s : sups) {
            Set<Mth> vc = virtualCalls.get(s);
            if (vc == null) continue;
            for (Mth r : new ArrayList<>(vc)) target(c, r);
        }
    }

    private void target(Cls c, Mth r) {
        Mth t = c.select(r);
        if (t == null) { problems.add("no selection of " + r + " in " + c); return; }
        if (!t.isAbstract()) reach(t);
    }

    void virtualCall(Mth r) {
        Set<Mth> s = virtualCalls.computeIfAbsent(r.owner, k -> new LinkedHashSet<>());
        if (!s.add(r)) return;
        for (Cls c : new ArrayList<>(instantiated)) if (c.isSubtypeOf(r.owner)) target(c, r);
    }

    /** Class initialization may run (JVMS 5.5): superclasses, and superinterfaces with default methods. */
    void init(Cls c) {
        use(c);
        if (c.initReachable) return;
        c.initReachable = true;
        if (!c.isInterface() && c.superCls() != null) init(c.superCls());
        if (!c.isInterface()) for (Cls i : c.allInterfaces()) if (hasDefaults(i)) init(i);
        Mth cl = c.own("<clinit>", "()V");
        if (cl != null) reach(cl);
        // Class.getEnumConstantsShared calls values() (EnumMap, EnumSet, Enum.valueOf)
        if ((c.node.access & ACC_ENUM) != 0 && "java/lang/Enum".equals(c.node.superName)) {
            Mth v = c.own("values", "()[L" + c.name + ";");
            if (v != null) reach(v);
        }
    }

    static boolean hasDefaults(Cls i) {
        for (Mth m : i.methods.values()) if (!m.isStatic() && !m.isAbstract() && !m.name.startsWith("<")) return true;
        return false;
    }

    void run() {
        while (!work.isEmpty()) {
            Mth m = work.poll();
            current = m;
            try {
                scan(m);
            } catch (TranslateException e) {
                problems.add(e.getMessage() + chain(m));
            }
        }
    }

    private void scan(Mth m) {
        Natives.Impl ni = natives.lookup(m);
        if (ni == null && m.isNative()) { problems.add("native without implementation: " + m + chain(m)); return; }
        if (ni != null) {
            m.nativeImpl = ni.cfun;
            m.nothrow = ni.nothrow;
            if (ni.cut != null) natives.cutsReached.add(m);
            for (String dep : ni.deps) natives.reachDep(this, m.owner.mod, dep);
            return;
        }
        if (m.isNative()) { problems.add("native without implementation: " + m); return; }
        if (m.isAbstract()) return;
        indy.rewrite(this, m);
        Pool.Module mod = m.owner.mod;
        for (AbstractInsnNode in : m.node.instructions) {
            switch (in.getType()) {
                case AbstractInsnNode.TYPE_INSN -> {
                    TypeInsnNode t = (TypeInsnNode) in;
                    String en = t.desc.startsWith("[") ? Type.getType(t.desc).getElementType().getSort() == Type.OBJECT
                            ? Type.getType(t.desc).getElementType().getInternalName() : null : t.desc;
                    if (en != null && link.cls(mod, m.owner, en, in) == null) break;
                    if (in.getOpcode() == NEW) {
                        Cls c = pool.ref(mod, t.desc);
                        if (natives.isCut(c)) use(c);
                        else { instantiate(c); init(c); }
                    }
                    else typeRef(mod, t.desc.startsWith("[") ? t.desc : "L" + t.desc + ";", in.getOpcode() == ANEWARRAY);
                }
                case AbstractInsnNode.MULTIANEWARRAY_INSN -> typeRef(mod, ((MultiANewArrayInsnNode) in).desc, false);
                case AbstractInsnNode.INT_INSN -> {
                    if (in.getOpcode() == NEWARRAY) arrayTypes.add("[" + "    ZCFDBSIJ".charAt(((IntInsnNode) in).operand));
                }
                case AbstractInsnNode.FIELD_INSN -> {
                    FieldInsnNode f = (FieldInsnNode) in;
                    Fld fd = link.field(m.owner, f);
                    if (fd == null) break;
                    use(fd.owner);
                    if (in.getOpcode() == GETSTATIC || in.getOpcode() == PUTSTATIC) init(fd.owner);
                    typeRef(mod, f.desc, false);
                }
                case AbstractInsnNode.METHOD_INSN -> method(m, (MethodInsnNode) in);
                case AbstractInsnNode.LDC_INSN -> {
                    Object c = ((LdcInsnNode) in).cst;
                    if (c instanceof Type t) {
                        if (t.getSort() == Type.OBJECT) { Cls k = pool.ref(mod, t.getInternalName()); use(k); k.mirrorNeeded = true; }
                        else if (t.getSort() == Type.ARRAY) typeRef(mod, t.getDescriptor(), false);
                        else throw new TranslateException("ldc " + c + " in " + m);
                    } else if (!(c instanceof Number || c instanceof String)) {
                        throw new TranslateException("ldc " + c.getClass().getSimpleName() + " in " + m);
                    }
                }
                case AbstractInsnNode.INVOKE_DYNAMIC_INSN ->
                        throw new TranslateException("invokedynamic left after rewrite in " + m + ": " + ((InvokeDynamicInsnNode) in).bsm);
                default -> {
                    int op = in.getOpcode();
                    if (op == JSR || op == RET) throw new TranslateException("jsr/ret in " + m);
                }
            }
        }
        for (TryCatchBlockNode tc : m.node.tryCatchBlocks) if (tc.type != null) use(pool.ref(mod, tc.type));
    }

    static String chain(Mth m) {
        StringBuilder b = new StringBuilder();
        int n = 0;
        for (Mth f = m.from; f != null && n < 60; f = f.from, n++) b.append("\n      <- ").append(f);
        return b.toString();
    }

    void typeRef(Pool.Module mod, String desc, boolean arrayOf) {
        Type t = Type.getType(desc);
        if (t.getSort() == Type.ARRAY) {
            arrayTypes.add(desc);
            Type e = t.getElementType();
            if (e.getSort() == Type.OBJECT) use(pool.ref(mod, e.getInternalName()));
            if (arrayOf) arrayTypes.add("[" + desc);
        } else if (t.getSort() == Type.OBJECT) {
            use(pool.ref(mod, t.getInternalName()));
            if (arrayOf) arrayTypes.add("[" + desc);
        }
    }

    private void method(Mth from, MethodInsnNode mi) {
        Pool.Module mod = from.owner.mod;
        if (mi.owner.startsWith("[")) {
            // a method of an array: clone or Object's
            if (mi.name.equals("clone")) return;
            Cls obj = pool.get(pool.sys, "java/lang/Object");
            Mth r = obj.resolveMethod(mi.name, mi.desc, false);
            if (r == null) throw new TranslateException("array method " + mi.name + mi.desc);
            virtualCall(r);
            return;
        }
        Mth r = link.method(from.owner, mi);
        if (r == null) return;
        Cls owner = pool.ref(mod, mi.owner);
        switch (mi.getOpcode()) {
            case INVOKESTATIC -> { init(r.owner); reach(r); }
            case INVOKESPECIAL -> {
                if (r.name.equals("<init>") && natives.isCut(owner) && !from.owner.isSubclassOf(owner)) return;
                Mth t = special(from.owner, owner, r);
                if (t == null) throw new TranslateException("invokespecial selects nothing: " + r + " in " + from);
                reach(t);
            }
            default -> {
                // a private method is still only reached through an instance of its class
                if (r.isStatic()) reach(r);
                else virtualCall(r);
            }
        }
    }

    /** invokespecial selection (JVMS 6.5 invokespecial). */
    Mth special(Cls current, Cls symbolic, Mth resolved) {
        if (resolved.name.equals("<init>") || resolved.isPrivate() || symbolic.isInterface()
                || !(current.superCls() != null && current.superCls().isSubclassOf(symbolic)) || current == symbolic) {
            if (symbolic.isInterface() && !resolved.name.equals("<init>") && !resolved.isPrivate()) {
                // super call to a default method: resolved is the target unless abstract
                return resolved.isAbstract() ? null : resolved;
            }
            return resolved;
        }
        for (Cls c = current.superCls(); c != null; c = c.superCls()) {
            Mth m = c.own(resolved.name, resolved.desc);
            if (m != null && !m.isStatic()) return m;
        }
        return current.superCls().maximallySpecific(resolved.name, resolved.desc, false);
    }
}
