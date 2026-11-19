package bcx;

import battlecode.instrumenter.TeamClassLoaderFactory;
import battlecode.server.Config;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import java.util.*;

/** Survey: instrument a team's classes with the official instrumenter, follow references, report what is used. */
public class Survey {
    public static void main(String[] a) throws Exception {
        String url = a[0];
        Config.getGlobalConfig().set("bc.engine.debug-methods", "false");
        TeamClassLoaderFactory f = new TeamClassLoaderFactory(url);
        TeamClassLoaderFactory.Loader L = f.createLoader(false);
        Map<String, Integer> opcodes = new TreeMap<>();
        Map<String, Integer> sysRefs = new TreeMap<>();
        Map<String, Integer> indy = new TreeMap<>();
        Set<String> translated = new TreeSet<>();
        Set<String> system = new TreeSet<>();
        Deque<String> work = new ArrayDeque<>();
        for (int i = 1; i < a.length; i++) work.add(a[i]);
        Set<String> seen = new HashSet<>(work);
        long insns = 0;
        while (!work.isEmpty()) {
            String n = work.poll();
            String dotted = n.replace('/', '.');
            byte[] b;
            try { L.loadClass(dotted); } catch (Throwable t) { System.out.println("LOADFAIL " + n + " " + t); }
            b = f.getCached(dotted);
            if (b == null) { system.add(n); continue; }
            translated.add(n);
            ClassNode cn = new ClassNode();
            new ClassReader(b).accept(cn, 0);
            List<String> refs = new ArrayList<>();
            if (cn.superName != null) refs.add(cn.superName);
            refs.addAll(cn.interfaces);
            for (MethodNode m : cn.methods) {
                for (AbstractInsnNode in : m.instructions) {
                    int op = in.getOpcode();
                    if (op < 0) continue;
                    insns++;
                    opcodes.merge(Printer.OPCODES[op], 1, Integer::sum);
                    if (in instanceof MethodInsnNode mi) { refs.add(mi.owner); if (isSys(f, mi.owner)) sysRefs.merge(mi.owner + "." + mi.name + mi.desc, 1, Integer::sum); }
                    else if (in instanceof FieldInsnNode fi) { refs.add(fi.owner); if (isSys(f, fi.owner)) sysRefs.merge("F " + fi.owner + "." + fi.name, 1, Integer::sum); }
                    else if (in instanceof TypeInsnNode ti) refs.add(ti.desc.startsWith("[") ? elem(ti.desc) : ti.desc);
                    else if (in instanceof InvokeDynamicInsnNode id) indy.merge(id.bsm.getOwner() + "." + id.bsm.getName() + " " + id.name + id.desc + " " + Arrays.toString(id.bsmArgs), 1, Integer::sum);
                    else if (in instanceof LdcInsnNode ld && ld.cst instanceof Type t) refs.add(t.getSort() == Type.OBJECT ? t.getInternalName() : elem(t.getDescriptor()));
                }
            }
            for (String r : refs) if (r != null && !r.startsWith("[") && seen.add(r)) work.add(r);
        }
        System.out.println("TRANSLATED " + translated.size() + " classes, " + insns + " insns");
        for (String t : translated) System.out.println("  T " + t);
        System.out.println("SYSTEM classes " + system.size());
        for (String s : system) System.out.println("  S " + s);
        System.out.println("OPCODES"); opcodes.forEach((k, v) -> System.out.println("  " + k + " " + v));
        System.out.println("SYSREFS"); sysRefs.forEach((k, v) -> System.out.println("  " + v + " " + k));
        System.out.println("INDY"); indy.forEach((k, v) -> System.out.println("  " + v + " " + k));
    }
    static String elem(String d) { String e = d.replaceAll("^\\[+", ""); return e.startsWith("L") ? e.substring(1, e.length() - 1) : null; }
    static boolean isSys(TeamClassLoaderFactory f, String owner) {
        if (owner.startsWith("[")) return true;
        return !(f.hasTeamClass(owner) || owner.startsWith("instrumented/"));
    }
}
