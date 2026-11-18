package bcx.subst;

import java.util.HashMap;
import java.util.Map;

/**
 * java.lang.Class for translated code (renamed to java/lang/Class): a mirror of a runtime Klass. Only what the
 * translated JDK and engine code call; everything else is a translation error.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class JClass implements java.io.Serializable, java.lang.reflect.Type {
    private long klass;

    private JClass() {}

    public native String getName();
    public native boolean isInstance(Object obj);
    public native boolean isAssignableFrom(JClass cls);
    public native boolean isInterface();
    public native boolean isArray();
    public native boolean isPrimitive();
    public native JClass getSuperclass();
    public native JClass getComponentType();
    public native int getModifiers();
    public native boolean desiredAssertionStatus();
    native boolean isEnum0();
    /** The enum's constants from its values(), cached per robot for a robot's class (as each robot has its own Class). */
    native Object[] getEnumConstantsShared();
    static native JClass getPrimitiveClass(String name);
    /** A system class by binary name, initialized when asked (JDK code uses it to run a static initializer). */
    public static native JClass forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException;
    public native JClass arrayType();
    /** Only what Unsafe.objectFieldOffset needs (java.util.Random's static initializer). */
    public native java.lang.reflect.Field getDeclaredField(String name) throws NoSuchFieldException;

    public JClass componentType() { return getComponentType(); }

    /** The class's Signature attribute, or null. */
    native String getGenericSignature0();
    /** A class by internal name, in this class's module first (bots' classes are per module), else the system's. */
    native JClass lookup0(String internalName);

    /** Only what HashMap.comparableClassFor needs: direct superinterfaces, parameterized ones with class arguments. */
    public java.lang.reflect.Type[] getGenericInterfaces() {
        String sig = getGenericSignature0();
        if (sig == null) return getInterfaces0();
        java.util.ArrayList<java.lang.reflect.Type> out = new java.util.ArrayList<>();
        int[] pos = {0};
        if (sig.charAt(0) == '<') skipTypeParams(sig, pos);
        parseType(sig, pos); // superclass
        while (pos[0] < sig.length()) out.add(parseType(sig, pos));
        return out.toArray(new java.lang.reflect.Type[0]);
    }

    native JClass[] getInterfaces0();

    private static void skipTypeParams(String s, int[] p) {
        int depth = 0;
        do {
            char c = s.charAt(p[0]++);
            if (c == '<') depth++;
            else if (c == '>') depth--;
        } while (depth > 0);
    }

    private java.lang.reflect.Type parseType(String s, int[] p) {
        char c = s.charAt(p[0]);
        if (c == 'T') { p[0] = s.indexOf(';', p[0]) + 1; return new PType(null, null); }
        if (c == '[') { p[0]++; parseType(s, p); return new PType(null, null); }
        if (c == '*') { p[0]++; return new PType(null, null); }
        if (c == '+' || c == '-') { p[0]++; parseType(s, p); return new PType(null, null); }
        if (c != 'L') { p[0]++; return new PType(null, null); }
        p[0]++;
        int start = p[0];
        while (s.charAt(p[0]) != '<' && s.charAt(p[0]) != ';') p[0]++;
        String name = s.substring(start, p[0]);
        JClass raw = lookup0(name);
        java.util.ArrayList<java.lang.reflect.Type> args = null;
        if (s.charAt(p[0]) == '<') {
            p[0]++;
            args = new java.util.ArrayList<>();
            while (s.charAt(p[0]) != '>') args.add(parseType(s, p));
            p[0]++;
        }
        while (s.charAt(p[0]) == '.') { // inner class of a parameterized type
            p[0]++;
            while (s.charAt(p[0]) != '<' && s.charAt(p[0]) != ';') p[0]++;
            if (s.charAt(p[0]) == '<') { p[0]++; while (s.charAt(p[0]) != '>') parseType(s, p); p[0]++; }
        }
        p[0]++; // ';'
        if (args == null) return raw == null ? new PType(null, null) : (java.lang.reflect.Type) (Object) raw;
        return new PType(raw, args.toArray(new java.lang.reflect.Type[0]));
    }

    public boolean isEnum() { return isEnum0(); }

    public Object[] getEnumConstants() {
        Object[] v = getEnumConstantsShared();
        return v == null ? null : v.clone();
    }

    Map<String, Object> enumConstantDirectory() {
        Object[] universe = getEnumConstantsShared();
        if (universe == null) throw new IllegalArgumentException(getName() + " is not an enum class");
        Map<String, Object> m = new HashMap<>((int) (universe.length / 0.75f) + 1);
        for (Object constant : universe) m.put(((Enum<?>) constant).name(), constant);
        return m;
    }

    public Object cast(Object obj) {
        if (obj != null && !isInstance(obj)) throw new ClassCastException("Cannot cast " + ((JClass) (Object) obj.getClass()).getName() + " to " + getName());
        return obj;
    }

    public String getSimpleName() {
        if (isArray()) return getComponentType().getSimpleName() + "[]";
        String n = getName();
        n = n.substring(n.lastIndexOf('.') + 1);
        int d = n.lastIndexOf('$');
        if (d >= 0) {
            n = n.substring(d + 1);
            int i = 0;
            while (i < n.length() && n.charAt(i) >= '0' && n.charAt(i) <= '9') i++;
            n = n.substring(i);
        }
        return n;
    }

    public String getCanonicalName() {
        if (isArray()) {
            String c = getComponentType().getCanonicalName();
            return c == null ? null : c + "[]";
        }
        return getName().replace('$', '.');
    }

    public String getTypeName() { return isArray() ? getComponentType().getTypeName() + "[]" : getName(); }

    public String getPackageName() {
        JClass c = this;
        while (c.isArray()) c = c.getComponentType();
        if (c.isPrimitive()) return "java.lang";
        String n = c.getName();
        int i = n.lastIndexOf('.');
        return i < 0 ? "" : n.substring(0, i);
    }

    public String toString() {
        String kind = isInterface() ? "interface " : isPrimitive() ? "" : "class ";
        return kind + getName();
    }

    public boolean isRecord() { return false; }
    public boolean isHidden() { return false; }
    public boolean isSealed() { return false; }
    public ClassLoader getClassLoader() { return null; }
    ClassLoader getClassLoader0() { return null; }
    public String descriptorString() {
        if (isPrimitive()) {
            switch (getName()) {
                case "int": return "I";
                case "long": return "J";
                case "boolean": return "Z";
                case "byte": return "B";
                case "char": return "C";
                case "short": return "S";
                case "float": return "F";
                case "double": return "D";
                default: return "V";
            }
        }
        if (isArray()) return "[" + getComponentType().descriptorString();
        return "L" + getName().replace('.', '/') + ";";
    }
}
