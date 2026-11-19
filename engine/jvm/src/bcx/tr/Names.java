package bcx.tr;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

/** C++ identifiers for JVM names. Injective: letters and digits stay, everything else is escaped. */
final class Names {
    static String mangle(String s) {
        StringBuilder b = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) b.append(c);
            else if (c == '/') b.append('_');
            else if (c == '_') b.append("_1");
            else if (c == '$') b.append("_2");
            else if (c == '<') b.append("_3");
            else if (c == '>') b.append("_4");
            else b.append("_x").append(Integer.toHexString(c)).append('_');
        }
        return b.toString();
    }

    static String descHash(String desc) {
        long h = 0xcbf29ce484222325L;
        for (int i = 0; i < desc.length(); i++) { h ^= desc.charAt(i); h *= 0x100000001b3L; }
        return Long.toHexString((h ^ (h >>> 32)) & 0xffffffffL);
    }

    /** C/C++ string literal for a Java string (as UTF-16 code units, escaped). */
    static String cstr(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') b.append('\\').append(c);
            else if (c >= 32 && c < 127 && c != '?') b.append(c);
            else b.append(String.format("\\x%02x\"\"", (int) c & 0xff));
        }
        return b.append('"').toString();
    }
}

final class Rename {
    static byte[] rename(byte[] b, java.util.Map<String, String> map) {
        ClassReader cr = new ClassReader(b);
        ClassWriter cw = new ClassWriter(0);
        cr.accept(new ClassRemapper(cw, new SimpleRemapper(map)), 0);
        return cw.toByteArray();
    }
}

class TranslateException extends RuntimeException {
    TranslateException(String m) { super(m); }
    TranslateException(String m, Throwable c) { super(m, c); }
}

/**
 * A team's own class that the official instrumenter refuses. The official engine then marks the whole team broken
 * (TeamClassLoaderFactory.hasError): every later class load of every robot of the team fails. Not modelled: reaching
 * it ends the native game with an error.
 */
final class TeamClassRefused extends TranslateException {
    final String cls;
    TeamClassRefused(String cls, String m, Throwable c) { super(m, c); this.cls = cls; }
}
