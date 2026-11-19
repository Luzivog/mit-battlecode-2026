package bcx.tr;

import bcx.tr.Cls.Fld;
import bcx.tr.Cls.Mth;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.util.*;

/**
 * Translates one method's bytecode to a C++ function. The operand stack and the locals become typed C++ variables
 * (s{index}{kind}, l{slot}{kind}); branches become gotos; an exception is the pending g_exc, checked after every
 * instruction that can throw and dispatched to the method's handlers in exception-table order.
 */
final class Gen implements Opcodes {
    final Emit E;
    final Pool pool;
    final Layout L;
    final Mth m;
    final Cls cls;
    final Pool.Module mod;
    final StringBuilder b = new StringBuilder();
    final Set<String> vars = new TreeSet<>();
    final Map<String, String> dispatch = new LinkedHashMap<>();
    final StringBuilder dispatchCode = new StringBuilder();
    boolean needS;
    Frame<BasicValue>[] fr;
    AbstractInsnNode[] insns;
    int cur;

    Gen(Emit e, Mth m) {
        this.E = e;
        this.pool = e.pool;
        this.L = e.layout;
        this.m = m;
        this.cls = m.owner;
        this.mod = m.owner.mod;
    }

    String gen() {
        MethodNode mn = m.node;
        try {
            fr = new Analyzer<>(new BasicInterpreter()).analyze(cls.name, mn);
        } catch (AnalyzerException ex) {
            throw new TranslateException("analyzer failed on " + m + ": " + ex.getMessage(), ex);
        }
        insns = mn.instructions.toArray();
        Set<Integer> targets = new HashSet<>();
        for (AbstractInsnNode in : insns) {
            if (in instanceof JumpInsnNode j) targets.add(idx(j.label));
            else if (in instanceof TableSwitchInsnNode t) { targets.add(idx(t.dflt)); for (LabelNode l : t.labels) targets.add(idx(l)); }
            else if (in instanceof LookupSwitchInsnNode t) { targets.add(idx(t.dflt)); for (LabelNode l : t.labels) targets.add(idx(l)); }
        }
        for (TryCatchBlockNode tc : mn.tryCatchBlocks) targets.add(idx(tc.handler));
        // parameters into locals
        int slot = 0, p = 0;
        if (!m.isStatic()) { b.append("  l0a = p0;\n"); vars.add("Obj* l0a"); slot = 1; p = 1; }
        for (Type a : m.args()) {
            char k = Layout.kind(a);
            String v = "l" + slot + k;
            vars.add(Layout.kctype(k) + " " + v);
            b.append("  ").append(v).append(" = p").append(p++).append(";\n");
            slot += a.getSize();
        }
        for (cur = 0; cur < insns.length; cur++) {
            AbstractInsnNode in = insns[cur];
            if (targets.contains(cur)) b.append(" L").append(cur).append(":;\n");
            if (in.getOpcode() < 0 || fr[cur] == null) continue;
            insn(in);
        }
        StringBuilder out = new StringBuilder();
        out.append(Layout.proto(m, m.cname)).append(" {\n");
        if (needS) out.append("  BS").append(mod.index).append("* const S = (BS").append(mod.index).append("*)g_S;\n");
        for (String v : vars) out.append("  ").append(v).append(" = 0;\n");
        out.append(b);
        out.append(dispatchCode);
        out.append(" XR:\n  return").append(m.ret().getSort() == Type.VOID ? "" : " (" + Layout.ctype(m.ret()) + ")0").append(";\n}\n");
        return out.toString();
    }

    int idx(AbstractInsnNode n) { return m.node.instructions.indexOf(n); }

    // ---------------- operand stack
    int sp() { return fr[cur].getStackSize(); }

    char skind(int i) { return vk(fr[cur].getStack(i)); }

    static char vk(BasicValue v) {
        if (v == BasicValue.INT_VALUE) return 'i';
        if (v == BasicValue.LONG_VALUE) return 'j';
        if (v == BasicValue.FLOAT_VALUE) return 'f';
        if (v == BasicValue.DOUBLE_VALUE) return 'd';
        if (v == BasicValue.REFERENCE_VALUE) return 'a';
        throw new TranslateException("value kind " + v);
    }

    String s(int i, char k) {
        String v = "s" + i + k;
        vars.add(Layout.kctype(k) + " " + v);
        return v;
    }

    /** i-th from the top (0 = top) before the instruction. */
    String top(int i) { int x = sp() - 1 - i; return s(x, skind(x)); }

    String push(char k) { return s(sp(), k); }

    String loc(int slot, char k) {
        String v = "l" + slot + k;
        vars.add(Layout.kctype(k) + " " + v);
        return v;
    }

    void line(String code) { b.append("  ").append(code).append('\n'); }

    // ---------------- exceptions
    /** Label of the dispatch block for the instruction being translated. */
    String xl() {
        List<TryCatchBlockNode> hs = new ArrayList<>();
        for (TryCatchBlockNode tc : m.node.tryCatchBlocks) {
            int s = idx(tc.start), e = idx(tc.end);
            if (cur >= s && cur < e) hs.add(tc);
        }
        if (hs.isEmpty()) return "XR";
        StringBuilder key = new StringBuilder();
        for (TryCatchBlockNode tc : hs) key.append(tc.type).append('>').append(idx(tc.handler)).append(';');
        String k = key.toString();
        String lab = dispatch.get(k);
        if (lab != null) return lab;
        lab = "X" + dispatch.size();
        dispatch.put(k, lab);
        StringBuilder d = dispatchCode;
        d.append(" ").append(lab).append(": {\n    Obj* e = g_exc;\n");
        boolean all = false;
        for (TryCatchBlockNode tc : hs) {
            String h = "s0a";
            vars.add("Obj* s0a");
            String go = "{ g_exc = nullptr; " + h + " = e; goto L" + idx(tc.handler) + "; }";
            if (tc.type == null || tc.type.equals("java/lang/Throwable")) { d.append("    ").append(go).append('\n'); all = true; break; }
            Cls t = pool.ref(mod, tc.type);
            d.append("    if (rt_instanceof(e, &").append(E.klass(t)).append(")) ").append(go).append('\n');
        }
        if (!all) d.append("    goto XR;\n");
        d.append("  }\n");
        return lab;
    }

    String chk() { return "CK(" + xl() + ");"; }

    void nullchk(String v) { line("NN(" + v + ", " + xl() + ");"); }

    // ---------------- class initialization
    /** Emit the JVM's initialization check for class c, unless it is already known done here. */
    void initChk(Cls c) {
        if (!c.initReachable || c.initIndex < 0) return;
        if (cls.isSubclassOf(c)) return;
        String st;
        if (c.mod.perRobot) { needS = true; st = "S->init[" + c.initIndex + "]"; }
        else st = "g_init0[" + c.initIndex + "]";
        line("INIT(" + st + ", " + E.initFn(c) + ", " + xl() + ");");
    }

    String staticRef(Fld f) {
        if (f.owner.mod.perRobot) { needS = true; return "S->" + f.owner.cname + "." + f.cname; }
        return "s_" + f.owner.cname + "." + f.cname;
    }

    // ---------------- instructions
    void insn(AbstractInsnNode in) {
        int op = in.getOpcode();
        String[] le = E.rta.link.errors.get(in);
        if (le != null) {
            if (le[0].equals("fatal")) {
                line("rt_fatal(\"%s\", " + Names.cstr(le[1]) + ");");
                return;
            }
            if (le[0].equals("battlecode/instrumenter/InstrumentationException")) {
                String[] tm = le[1].split(":", 2);
                line("rt_throw_instr(" + (tm[0].equals("ILLEGAL") ? 0 : 1) + ", rt_cstring(" + Names.cstr(tm[1]) + ")); goto " + xl() + ";");
                return;
            }
            String kind = Glue.linkageKind(le[0]);
            line("rt_throw_new_" + kind + "(rt_cstring(" + Names.cstr(le[1]) + ")); goto " + xl() + ";");
            return;
        }
        switch (op) {
            case NOP -> {}
            case ACONST_NULL -> line(push('a') + " = nullptr;");
            case ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5 -> line(push('i') + " = " + (op - ICONST_0) + ";");
            case LCONST_0, LCONST_1 -> line(push('j') + " = " + (op - LCONST_0) + ";");
            case FCONST_0, FCONST_1, FCONST_2 -> line(push('f') + " = " + (op - FCONST_0) + ".0f;");
            case DCONST_0, DCONST_1 -> line(push('d') + " = " + (op - DCONST_0) + ".0;");
            case BIPUSH, SIPUSH -> line(push('i') + " = " + ((IntInsnNode) in).operand + ";");
            case LDC -> ldc(((LdcInsnNode) in).cst);
            case ILOAD, LLOAD, FLOAD, DLOAD, ALOAD -> {
                char k = "ijfda".charAt(op - ILOAD);
                line(push(k) + " = " + loc(((VarInsnNode) in).var, k) + ";");
            }
            case ISTORE, LSTORE, FSTORE, DSTORE, ASTORE -> {
                char k = "ijfda".charAt(op - ISTORE);
                line(loc(((VarInsnNode) in).var, k) + " = " + top(0) + ";");
            }
            case IINC -> {
                IincInsnNode ii = (IincInsnNode) in;
                String v = loc(ii.var, 'i');
                line(v + " = (jint)((juint)" + v + " + (juint)(" + ii.incr + "));");
            }
            case IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD -> arrayLoad(op);
            case IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE -> arrayStore(op);
            case POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP -> stackOp(op);
            case IADD, LADD -> bin(op == IADD ? 'i' : 'j', "+", true);
            case ISUB, LSUB -> bin(op == ISUB ? 'i' : 'j', "-", true);
            case IMUL, LMUL -> bin(op == IMUL ? 'i' : 'j', "*", true);
            case FADD, DADD -> bin(op == FADD ? 'f' : 'd', "+", false);
            case FSUB, DSUB -> bin(op == FSUB ? 'f' : 'd', "-", false);
            case FMUL, DMUL -> bin(op == FMUL ? 'f' : 'd', "*", false);
            case FDIV, DDIV -> bin(op == FDIV ? 'f' : 'd', "/", false);
            case IDIV, LDIV, IREM, LREM -> {
                char k = (op == IDIV || op == IREM) ? 'i' : 'j';
                String a = top(1), c = top(0);
                line("if (UNLIKELY(" + c + " == 0)) { rt_throw_arith(); goto " + xl() + "; }");
                String fn = (op == IDIV ? "rt_idiv" : op == LDIV ? "rt_ldiv" : op == IREM ? "rt_irem" : "rt_lrem");
                line(s(sp() - 2, k) + " = " + fn + "(" + a + ", " + c + ");");
            }
            case FREM -> line(s(sp() - 2, 'f') + " = rt_frem(" + top(1) + ", " + top(0) + ");");
            case DREM -> line(s(sp() - 2, 'd') + " = rt_drem(" + top(1) + ", " + top(0) + ");");
            case INEG -> line(top(0) + " = (jint)(0u - (juint)" + top(0) + ");");
            case LNEG -> line(top(0) + " = (jlong)(0ull - (julong)" + top(0) + ");");
            case FNEG, DNEG -> line(top(0) + " = -" + top(0) + ";");
            case ISHL -> line(s(sp() - 2, 'i') + " = (jint)((juint)" + top(1) + " << (" + top(0) + " & 31));");
            case ISHR -> line(s(sp() - 2, 'i') + " = " + top(1) + " >> (" + top(0) + " & 31);");
            case IUSHR -> line(s(sp() - 2, 'i') + " = (jint)((juint)" + top(1) + " >> (" + top(0) + " & 31));");
            case LSHL -> line(s(sp() - 2, 'j') + " = (jlong)((julong)" + top(1) + " << (" + top(0) + " & 63));");
            case LSHR -> line(s(sp() - 2, 'j') + " = " + top(1) + " >> (" + top(0) + " & 63);");
            case LUSHR -> line(s(sp() - 2, 'j') + " = (jlong)((julong)" + top(1) + " >> (" + top(0) + " & 63));");
            case IAND, LAND -> bin(op == IAND ? 'i' : 'j', "&", false);
            case IOR, LOR -> bin(op == IOR ? 'i' : 'j', "|", false);
            case IXOR, LXOR -> bin(op == IXOR ? 'i' : 'j', "^", false);
            case I2L -> conv('j', "(jlong)");
            case I2F -> conv('f', "(jfloat)");
            case I2D -> conv('d', "(jdouble)");
            case L2I -> conv('i', "(jint)");
            case L2F -> conv('f', "(jfloat)");
            case L2D -> conv('d', "(jdouble)");
            case F2I -> conv('i', "rt_f2i");
            case F2L -> conv('j', "rt_f2l");
            case F2D -> conv('d', "(jdouble)");
            case D2I -> conv('i', "rt_d2i");
            case D2L -> conv('j', "rt_d2l");
            case D2F -> conv('f', "(jfloat)");
            case I2B -> conv('i', "(jint)(jbyte)");
            case I2C -> conv('i', "(jint)(jchar)");
            case I2S -> conv('i', "(jint)(jshort)");
            case LCMP -> line(s(sp() - 2, 'i') + " = rt_lcmp(" + top(1) + ", " + top(0) + ");");
            case FCMPL, FCMPG, DCMPL, DCMPG -> {
                String fn = op == FCMPL ? "rt_fcmpl" : op == FCMPG ? "rt_fcmpg" : op == DCMPL ? "rt_dcmpl" : "rt_dcmpg";
                line(s(sp() - 2, 'i') + " = " + fn + "(" + top(1) + ", " + top(0) + ");");
            }
            case IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE -> {
                String[] ops = {"==", "!=", "<", ">=", ">", "<="};
                jump(top(0) + " " + ops[op - IFEQ] + " 0", (JumpInsnNode) in);
            }
            case IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE -> {
                String[] ops = {"==", "!=", "<", ">=", ">", "<="};
                jump(top(1) + " " + ops[op - IF_ICMPEQ] + " " + top(0), (JumpInsnNode) in);
            }
            case IF_ACMPEQ -> jump(top(1) + " == " + top(0), (JumpInsnNode) in);
            case IF_ACMPNE -> jump(top(1) + " != " + top(0), (JumpInsnNode) in);
            case IFNULL -> jump(top(0) + " == nullptr", (JumpInsnNode) in);
            case IFNONNULL -> jump(top(0) + " != nullptr", (JumpInsnNode) in);
            case GOTO -> line("goto L" + idx(((JumpInsnNode) in).label) + ";");
            case TABLESWITCH -> {
                TableSwitchInsnNode t = (TableSwitchInsnNode) in;
                StringBuilder sw = new StringBuilder("switch (" + top(0) + ") {");
                for (int i = 0; i < t.labels.size(); i++) sw.append(" case ").append(t.min + i).append(": goto L").append(idx(t.labels.get(i))).append(';');
                sw.append(" default: goto L").append(idx(t.dflt)).append("; }");
                line(sw.toString());
            }
            case LOOKUPSWITCH -> {
                LookupSwitchInsnNode t = (LookupSwitchInsnNode) in;
                StringBuilder sw = new StringBuilder("switch (" + top(0) + ") {");
                for (int i = 0; i < t.labels.size(); i++) sw.append(" case ").append(t.keys.get(i)).append(": goto L").append(idx(t.labels.get(i))).append(';');
                sw.append(" default: goto L").append(idx(t.dflt)).append("; }");
                line(sw.toString());
            }
            case IRETURN, LRETURN, FRETURN, DRETURN, ARETURN -> line("return " + top(0) + ";");
            case RETURN -> line("return;");
            case GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD -> field((FieldInsnNode) in);
            case INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE -> E.invoke(this, (MethodInsnNode) in);
            case NEW -> {
                Cls c = pool.ref(mod, ((TypeInsnNode) in).desc);
                if (E.natives.isCut(c)) {
                    line("rt_fatal(\"cut class instantiated: %s\", " + Names.cstr(c.name) + ");");
                    vars.add("Obj* " + push('a'));
                    break;
                }
                initChk(c);
                line(push('a') + " = rt_new(&" + E.klass(c) + ");");
            }
            case NEWARRAY -> {
                String desc = "[" + "    ZCFDBSIJ".charAt(((IntInsnNode) in).operand);
                newArray(desc);
            }
            case ANEWARRAY -> {
                String d = ((TypeInsnNode) in).desc;
                newArray("[" + (d.startsWith("[") ? d : "L" + d + ";"));
            }
            case MULTIANEWARRAY -> {
                MultiANewArrayInsnNode ma = (MultiANewArrayInsnNode) in;
                StringBuilder dims = new StringBuilder();
                for (int i = ma.dims - 1; i >= 0; i--) dims.append(", ").append(top(i));
                String res = s(sp() - ma.dims, 'a');
                line(res + " = rt_multianewarray(&" + E.arrayKlass(mod, ma.desc) + ", " + ma.dims + dims + "); " + chk());
            }
            case ARRAYLENGTH -> {
                nullchk(top(0));
                line(s(sp() - 1, 'i') + " = ALEN(" + top(0) + ");");
            }
            case ATHROW -> {
                nullchk(top(0));
                line("g_exc = " + top(0) + "; goto " + xl() + ";");
            }
            case CHECKCAST -> {
                String d = ((TypeInsnNode) in).desc;
                String k = typeKlass(d);
                if (!d.equals("java/lang/Object"))
                    line("if (UNLIKELY(" + top(0) + " != nullptr && !rt_instanceof(" + top(0) + ", " + k + "))) { rt_throw_cce(" + top(0) + ", " + k + "); goto " + xl() + "; }");
            }
            case INSTANCEOF -> {
                String k = typeKlass(((TypeInsnNode) in).desc);
                line(s(sp() - 1, 'i') + " = " + top(0) + " != nullptr && rt_instanceof(" + top(0) + ", " + k + ");");
            }
            case MONITORENTER, MONITOREXIT -> nullchk(top(0));
            default -> throw new TranslateException("opcode " + op + " in " + m);
        }
    }

    String typeKlass(String desc) {
        if (desc.startsWith("[")) return "&" + E.arrayKlass(mod, desc);
        return "&" + E.klass(pool.ref(mod, desc));
    }

    void newArray(String desc) {
        String n = top(0);
        line("if (UNLIKELY(" + n + " < 0)) { rt_throw_negsize(" + n + "); goto " + xl() + "; }");
        line(s(sp() - 1, 'a') + " = rt_newarray(&" + E.arrayKlass(mod, desc) + ", " + n + ");");
    }

    void ldc(Object c) {
        if (c instanceof Integer i) line(push('i') + " = " + (i == Integer.MIN_VALUE ? "(jint)0x80000000" : i.toString()) + ";");
        else if (c instanceof Long l) line(push('j') + " = " + (l == Long.MIN_VALUE ? "(jlong)0x8000000000000000ull" : l + "ll") + ";");
        else if (c instanceof Float f) line(push('f') + " = rt_fbits(0x" + Integer.toHexString(Float.floatToRawIntBits(f)) + "u);");
        else if (c instanceof Double d) line(push('d') + " = rt_dbits(0x" + Long.toHexString(Double.doubleToRawLongBits(d)) + "ull);");
        else if (c instanceof String str) line(push('a') + " = g_str[" + E.str(str) + "];");
        else if (c instanceof Type t) {
            if (t.getSort() == Type.ARRAY) line(push('a') + " = rt_mirror(&" + E.arrayKlass(mod, t.getDescriptor()) + ");");
            else line(push('a') + " = rt_mirror(&" + E.klass(pool.ref(mod, t.getInternalName())) + ");");
        } else throw new TranslateException("ldc " + c);
    }

    void bin(char k, String o, boolean wrap) {
        String a = top(1), c = top(0), r = s(sp() - 2, k);
        if (wrap && k == 'i') line(r + " = (jint)((juint)" + a + " " + o + " (juint)" + c + ");");
        else if (wrap && k == 'j') line(r + " = (jlong)((julong)" + a + " " + o + " (julong)" + c + ");");
        else line(r + " = " + a + " " + o + " " + c + ";");
    }

    void conv(char k, String fn) {
        String a = top(0);
        line(s(sp() - 1, k) + " = " + fn + "(" + a + ");");
    }

    void jump(String cond, JumpInsnNode j) { line("if (" + cond + ") goto L" + idx(j.label) + ";"); }

    void arrayLoad(int op) {
        String a = top(1), i = top(0);
        nullchk(a);
        line("if (UNLIKELY((juint)" + i + " >= (juint)ALEN(" + a + "))) { rt_throw_aioobe(" + i + ", ALEN(" + a + ")); goto " + xl() + "; }");
        String et = switch (op) {
            case IALOAD -> "jint"; case LALOAD -> "jlong"; case FALOAD -> "jfloat"; case DALOAD -> "jdouble";
            case AALOAD -> "Obj*"; case BALOAD -> "jbyte"; case CALOAD -> "jchar"; default -> "jshort";
        };
        char k = switch (op) { case LALOAD -> 'j'; case FALOAD -> 'f'; case DALOAD -> 'd'; case AALOAD -> 'a'; default -> 'i'; };
        line(s(sp() - 2, k) + " = ADATA(" + et + ", " + a + ")[" + i + "];");
    }

    void arrayStore(int op) {
        String a = top(2), i = top(1), v = top(0);
        nullchk(a);
        line("if (UNLIKELY((juint)" + i + " >= (juint)ALEN(" + a + "))) { rt_throw_aioobe(" + i + ", ALEN(" + a + ")); goto " + xl() + "; }");
        switch (op) {
            case AASTORE -> {
                line("if (UNLIKELY(" + v + " != nullptr && !rt_store_ok(" + a + ", " + v + "))) { rt_throw_ase(" + v + "); goto " + xl() + "; }");
                line("ADATA(Obj*, " + a + ")[" + i + "] = " + v + ";");
            }
            case BASTORE -> line("ADATA(jbyte, " + a + ")[" + i + "] = rt_is_bool_array(" + a + ") ? (jbyte)(" + v + " & 1) : (jbyte)" + v + ";");
            case IASTORE -> line("ADATA(jint, " + a + ")[" + i + "] = " + v + ";");
            case LASTORE -> line("ADATA(jlong, " + a + ")[" + i + "] = " + v + ";");
            case FASTORE -> line("ADATA(jfloat, " + a + ")[" + i + "] = " + v + ";");
            case DASTORE -> line("ADATA(jdouble, " + a + ")[" + i + "] = " + v + ";");
            case CASTORE -> line("ADATA(jchar, " + a + ")[" + i + "] = (jchar)" + v + ";");
            case SASTORE -> line("ADATA(jshort, " + a + ")[" + i + "] = (jshort)" + v + ";");
        }
    }

    boolean cat2(int i) { char k = skind(i); return k == 'j' || k == 'd'; }

    void stackOp(int op) {
        int n = sp();
        switch (op) {
            case POP, POP2 -> {}
            case DUP -> line(s(n, skind(n - 1)) + " = " + s(n - 1, skind(n - 1)) + ";");
            case SWAP -> {
                char ka = skind(n - 2), kb = skind(n - 1);
                String t = "t_swap" + ka;
                vars.add(Layout.kctype(ka) + " " + t);
                line(t + " = " + s(n - 2, ka) + "; " + s(n - 2, kb) + " = " + s(n - 1, kb) + "; " + s(n - 1, ka) + " = " + t + ";");
            }
            case DUP_X1 -> shuffle(new int[]{n - 2, n - 1}, new int[]{n - 1, n - 2, n - 1});
            case DUP_X2 -> {
                if (cat2(n - 2)) shuffle(new int[]{n - 2, n - 1}, new int[]{n - 1, n - 2, n - 1});
                else shuffle(new int[]{n - 3, n - 2, n - 1}, new int[]{n - 1, n - 3, n - 2, n - 1});
            }
            case DUP2 -> {
                if (cat2(n - 1)) shuffle(new int[]{n - 1}, new int[]{n - 1, n - 1});
                else shuffle(new int[]{n - 2, n - 1}, new int[]{n - 2, n - 1, n - 2, n - 1});
            }
            case DUP2_X1 -> {
                if (cat2(n - 1)) shuffle(new int[]{n - 2, n - 1}, new int[]{n - 1, n - 2, n - 1});
                else shuffle(new int[]{n - 3, n - 2, n - 1}, new int[]{n - 2, n - 1, n - 3, n - 2, n - 1});
            }
            case DUP2_X2 -> {
                boolean c1 = cat2(n - 1);
                if (c1 && cat2(n - 2)) shuffle(new int[]{n - 2, n - 1}, new int[]{n - 1, n - 2, n - 1});
                else if (c1) shuffle(new int[]{n - 3, n - 2, n - 1}, new int[]{n - 1, n - 3, n - 2, n - 1});
                else if (cat2(n - 3)) shuffle(new int[]{n - 3, n - 2, n - 1}, new int[]{n - 2, n - 1, n - 3, n - 2, n - 1});
                else shuffle(new int[]{n - 4, n - 3, n - 2, n - 1}, new int[]{n - 2, n - 1, n - 4, n - 3, n - 2, n - 1});
            }
        }
    }

    /** Rewrite the stack: the values at indices `from` (bottom to top) are replaced by the sequence `to`. */
    void shuffle(int[] from, int[] to) {
        int base = from[0];
        StringBuilder sb = new StringBuilder("{ ");
        for (int i = 0; i < from.length; i++) {
            char k = skind(from[i]);
            sb.append(Layout.kctype(k)).append(" t").append(i).append(" = ").append(s(from[i], k)).append("; ");
        }
        for (int i = 0; i < to.length; i++) {
            int src = -1;
            for (int j = 0; j < from.length; j++) if (from[j] == to[i]) src = j;
            char k = skind(to[i]);
            sb.append(s(base + i, k)).append(" = t").append(src).append("; ");
        }
        sb.append("}");
        line(sb.toString());
    }

    void field(FieldInsnNode fi) {
        Cls owner = pool.ref(mod, fi.owner);
        Fld f = owner.resolveField(fi.name, fi.desc);
        if (f == null) throw new TranslateException("unresolved field " + fi.owner + "." + fi.name);
        char k = Layout.kind(Type.getType(f.desc));
        String ct = Layout.ctype(Type.getType(f.desc));
        switch (fi.getOpcode()) {
            case GETSTATIC -> { initChk(f.owner); line(push(k) + " = " + staticRef(f) + ";"); }
            case PUTSTATIC -> { initChk(f.owner); line(staticRef(f) + " = (" + ct + ")" + top(0) + ";"); }
            case GETFIELD -> {
                String o = top(0);
                nullchk(o);
                line(s(sp() - 1, k) + " = ((" + f.owner.cname + "*)" + o + ")->" + f.cname + ";");
            }
            case PUTFIELD -> {
                String o = top(1), v = top(0);
                nullchk(o);
                line("((" + f.owner.cname + "*)" + o + ")->" + f.cname + " = (" + ct + ")" + v + ";");
            }
        }
    }
}
