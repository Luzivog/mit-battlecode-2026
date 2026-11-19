package bcx.tr;

import bcx.tr.Cls.Mth;

/** Generated helpers the runtime calls: strings, mirrors, and the exceptions the JVM itself throws. */
final class Glue {
    static final String[][] THROWS = {
            // runtime function, exception class, constructor descriptor ("" for no message)
            {"npe", "java/lang/NullPointerException", "()V"},
            {"arith", "java/lang/ArithmeticException", "(Ljava/lang/String;)V"},
            {"aioobe", "java/lang/ArrayIndexOutOfBoundsException", "(Ljava/lang/String;)V"},
            {"cce", "java/lang/ClassCastException", "(Ljava/lang/String;)V"},
            {"negsize", "java/lang/NegativeArraySizeException", "(Ljava/lang/String;)V"},
            {"ase", "java/lang/ArrayStoreException", "(Ljava/lang/String;)V"},
            {"ncdfe", "java/lang/NoClassDefFoundError", "(Ljava/lang/String;)V"},
            {"ame", "java/lang/AbstractMethodError", "(Ljava/lang/String;)V"},
            {"internal", "java/lang/InternalError", "(Ljava/lang/String;)V"},
            {"nsme", "java/lang/NoSuchMethodError", "(Ljava/lang/String;)V"},
            {"nsfe", "java/lang/NoSuchFieldError", "(Ljava/lang/String;)V"},
            {"iae", "java/lang/IllegalAccessError", "(Ljava/lang/String;)V"},
            {"icce", "java/lang/IncompatibleClassChangeError", "(Ljava/lang/String;)V"},
            {"nsfx", "java/lang/NoSuchFieldException", "(Ljava/lang/String;)V"},
    };

    static String linkageKind(String errClass) {
        for (String[] t : THROWS) if (t[1].equals(errClass)) return t[0];
        throw new TranslateException("no runtime throw for " + errClass);
    }

    /** Methods and classes the glue needs, for the RTA. */
    static void roots(Rta rta, Pool pool) {
        Cls str = pool.get(pool.sys, "java/lang/String");
        rta.instantiate(str);
        rta.init(str);
        Cls cl = pool.get(pool.sys, "java/lang/Class");
        rta.instantiate(cl);
        for (String[] t : THROWS) {
            Cls c = pool.get(pool.sys, t[1]);
            rta.instantiate(c);
            rta.init(c);
            rta.reach(c.own("<init>", t[2]));
        }
        Cls eiie = pool.get(pool.sys, "java/lang/ExceptionInInitializerError");
        rta.instantiate(eiie);
        rta.init(eiie);
        rta.reach(eiie.own("<init>", "(Ljava/lang/Throwable;)V"));
        rta.use(pool.get(pool.sys, "java/lang/Error"));
        if (!pool.teams.isEmpty()) {
            Cls ie = pool.get(pool.sys, "battlecode/instrumenter/InstrumentationException");
            rta.instantiate(ie);
            rta.init(ie);
            rta.reach(ie.own("<init>", "(Lbattlecode/instrumenter/InstrumentationException$Type;Ljava/lang/String;)V"));
            rta.init(pool.get(pool.sys, "battlecode/instrumenter/InstrumentationException$Type"));
        }
        rta.arrayTypes.add("[B");
    }

    static String code(Emit e) {
        Pool pool = e.pool;
        StringBuilder b = new StringBuilder("\n// ---- glue\n");
        Cls str = pool.find(pool.sys, "java/lang/String");
        String bytes = e.arrayKlass(pool.sys, "[B");
        b.append("Obj* rt_make_string(const jchar* s, jint n) {\n")
                .append("  bool latin1 = true;\n  for (jint i = 0; i < n; i++) if (s[i] > 255) { latin1 = false; break; }\n")
                .append("  Obj* v = rt_newarray(&").append(bytes).append(", latin1 ? n : 2 * n);\n")
                .append("  if (latin1) for (jint i = 0; i < n; i++) ADATA(jbyte, v)[i] = (jbyte)s[i];\n")
                .append("  else memcpy(ADATA(jbyte, v), s, 2 * (size_t)n);\n")
                .append("  ").append(str.cname).append("* o = (").append(str.cname).append("*)rt_new(&").append(e.klass(str)).append(");\n")
                .append("  o->f_value = v; o->f_coder = latin1 ? 0 : 1;\n  return o;\n}\n");
        b.append("jint rt_string_length(Obj* s) { ").append(str.cname).append("* o = (").append(str.cname)
                .append("*)s; return o->f_coder ? ALEN(o->f_value) / 2 : ALEN(o->f_value); }\n");
        b.append("jchar rt_string_char(Obj* s, jint i) { ").append(str.cname).append("* o = (").append(str.cname)
                .append("*)s; return o->f_coder ? ADATA(jchar, o->f_value)[i] : (jchar)(uint8_t)ADATA(jbyte, o->f_value)[i]; }\n");
        Cls cl = pool.find(pool.sys, "java/lang/Class");
        b.append("Obj* rt_mirror_slow(Klass* k) {\n  ").append(cl.cname).append("* m = (").append(cl.cname).append("*)rt_new(&").append(e.klass(cl)).append(");\n")
                .append("  m->f_klass = (jlong)(intptr_t)k;\n  k->mirror = m;\n  return m;\n}\n");
        b.append("Klass* rt_mirror_klass(Obj* m) { return (Klass*)(intptr_t)((").append(cl.cname).append("*)m)->f_klass; }\n");
        for (String[] t : THROWS) {
            Cls c = pool.find(pool.sys, t[1]);
            Mth ctor = c.own("<init>", t[2]);
            b.append("void rt_throw_new_").append(t[0]).append("(Obj* msg) {\n");
            if (c.initIndex >= 0) b.append("  if (g_init0[").append(c.initIndex).append("] != 2) { ").append(e.initFn(c)).append("(); if (g_exc) return; }\n");
            b.append("  Obj* x = rt_new(&").append(e.klass(c)).append(");\n  ").append(Emit.fn(ctor)).append("(x")
                    .append(t[2].equals("()V") ? "" : ", msg").append(");\n  if (!g_exc) g_exc = x;\n}\n");
        }
        Cls eiie = pool.find(pool.sys, "java/lang/ExceptionInInitializerError");
        Cls err = pool.find(pool.sys, "java/lang/Error");
        b.append("void rt_init_failed() {\n  Obj* ex = g_exc;\n  if (rt_instanceof(ex, &").append(e.klass(err)).append(")) return;\n  g_exc = nullptr;\n");
        if (eiie.initIndex >= 0) b.append("  if (g_init0[").append(eiie.initIndex).append("] != 2) { ").append(e.initFn(eiie)).append("(); if (g_exc) return; }\n");
        b.append("  Obj* w = rt_new(&").append(e.klass(eiie)).append(");\n  ").append(Emit.fn(eiie.own("<init>", "(Ljava/lang/Throwable;)V")))
                .append("(w, ex);\n  if (!g_exc) g_exc = w;\n}\n");
        // a class the official instrumenter refuses: InstrumentationException(type, message) at the reference
        b.append("void rt_throw_instr(int type, Obj* msg) {\n");
        Cls ie = pool.find(pool.sys, "battlecode/instrumenter/InstrumentationException");
        if (ie != null && ie.id >= 0) {
            Cls ty = pool.find(pool.sys, "battlecode/instrumenter/InstrumentationException$Type");
            b.append("  ").append(e.initFn(ty)).append("(); if (g_exc) return;\n");
            if (ie.initIndex >= 0) b.append("  ").append(e.initFn(ie)).append("(); if (g_exc) return;\n");
            b.append("  Obj* t = type == 0 ? s_").append(ty.cname).append(".f_ILLEGAL : s_").append(ty.cname).append(".f_MISSING;\n");
            b.append("  Obj* x = rt_new(&").append(e.klass(ie)).append(");\n  ")
                    .append(Emit.fn(ie.own("<init>", "(Lbattlecode/instrumenter/InstrumentationException$Type;Ljava/lang/String;)V")))
                    .append("(x, t, msg);\n  if (!g_exc) g_exc = x;\n");
        } else b.append("  rt_fatal(\"InstrumentationException without a team\");\n");
        b.append("}\n");
        // the classes the VM initializes before any code runs (instances made natively, no `new`)
        b.append("void rt_init_core() {\n");
        for (String cn : new String[]{"java/lang/Object", "java/lang/String", "java/lang/Class"}) {
            Cls c = pool.find(pool.sys, cn);
            if (c != null && c.initIndex >= 0) b.append("  ").append(e.initFn(c)).append("();\n");
        }
        b.append("}\n");
        // ObjectMethods: which classes hash by identity (Object's, Enum's or Character's hashCode)
        Cls obj = pool.find(pool.sys, "java/lang/Object");
        Mth oh = obj.own("hashCode", "()I"), ots = obj.own("toString", "()Ljava/lang/String;");
        b.append("extern const int g_slot_hashCode = ").append(oh.vslot).append(", g_slot_toString = ").append(ots.vslot).append(";\n");
        b.append("extern void* const g_identity_hash_fns[3] = {");
        for (String cn : new String[]{"java/lang/Object", "java/lang/Enum", "java/lang/Character"}) {
            Cls c = pool.find(pool.sys, cn);
            Mth h = c == null ? null : c.own("hashCode", "()I");
            b.append(' ').append(h != null && h.reachable && !h.isAbstract() ? "(void*)&" + Emit.fn(h) : "nullptr").append(',');
        }
        b.append(" };\nextern void* const g_object_toString_fn = ").append(ots.reachable ? "(void*)&" + Emit.fn(ots) : "nullptr").append(";\n");
        // Unsafe.objectFieldOffset
        b.append("long rt_field_offset(Klass* k, const char* name) {\n");
        for (Cls c : e.layout.order) {
            if (c.isInterface() || c.id < 0) continue;
            boolean any = false;
            for (Cls.Fld f : c.fields.values()) if (!f.isStatic()) { any = true; break; }
            if (!any) continue;
            b.append("  if (k == &").append(e.klass(c)).append(") {");
            for (Cls.Fld f : c.fields.values())
                if (!f.isStatic()) b.append(" if (!strcmp(name, ").append(Names.cstr(f.name)).append(")) return offsetof(").append(c.cname).append(", ").append(f.cname).append(");");
            b.append(" }\n");
        }
        b.append("  return -1;\n}\n");
        // StreamMethods.toList's reflective toArray()
        b.append("static void* find_toArray(Klass* k) {\n");
        for (Cls c : e.layout.order) {
            if (!c.instantiated) continue;
            Mth ta = c.resolveMethod("toArray", "()[Ljava/lang/Object;", false);
            if (ta == null || ta.isStatic() || (ta.node.access & org.objectweb.asm.Opcodes.ACC_PUBLIC) == 0) continue;
            Mth sel = c.select(ta);
            if (sel == null || sel.isAbstract() || !sel.reachable) continue;
            b.append("  if (k == &").append(e.klass(c)).append(") return (void*)&").append(Emit.fn(sel)).append(";\n");
        }
        b.append("  return nullptr;\n}\n");
        b.append("jboolean rt_has_toArray(Obj* o) { if (!o) { rt_throw_npe(); return 0; } return find_toArray(o->klass) != nullptr; }\n");
        b.append("Obj* rt_invoke_toArray(Obj* o) { return ((Obj * (*)(Obj*)) find_toArray(o->klass))(o); }\n");
        // methods cut from the program
        for (Mth m : e.natives.cutsReached)
            b.append(Layout.proto(m, m.nativeImpl)).append(" { rt_fatal(\"cut method reached: %s\", ").append(Names.cstr(m.toString())).append("); }\n");
        // entry points (--entry), in order
        b.append("extern void* const g_entry_fns[] = {");
        for (Mth m : e.entries) b.append(" (void*)&").append(Emit.fn(m)).append(',');
        b.append(" nullptr };\nextern void (*const g_entry_inits[])() = {");
        for (Mth m : e.entries) b.append(' ').append(m.owner.initIndex >= 0 ? e.initFn(m.owner) : "nullptr").append(',');
        b.append(" nullptr };\n");
        return b.toString();
    }
}
