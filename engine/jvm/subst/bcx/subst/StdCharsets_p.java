package bcx.subst;

import java.nio.charset.Charset;

/** Patch of sun.nio.cs.StandardCharsets (the provider): the charsets String handles, by their names and aliases. */
public final class StdCharsets_p {
    private static Charset utf16, utf16be, utf16le;

    public final Charset charsetForName(String charsetName) {
        String n = ascii(charsetName);
        switch (n) {
            case "utf-8": case "utf8": case "unicode-1-1-utf-8": return sun.nio.cs.UTF_8.INSTANCE;
            case "iso-8859-1": case "iso8859_1": case "latin1": case "l1": case "819": case "cp819": case "csisolatin1":
            case "ibm819": case "iso_8859_1": case "iso_8859-1": case "iso8859-1": case "iso-ir-100": case "8859_1":
                return sun.nio.cs.ISO_8859_1.INSTANCE;
            case "us-ascii": case "ascii": case "iso646-us": case "646": case "cp367": case "csascii": case "default":
            case "iso_646.irv:1991": case "us": case "ibm367": case "iso-ir-6": case "ansi_x3.4-1968": case "ansi_x3.4-1986":
                return sun.nio.cs.US_ASCII.INSTANCE;
            case "utf-16": case "utf_16": case "utf16": case "unicode":
                if (utf16 == null) utf16 = new sun.nio.cs.UTF_16();
                return utf16;
            case "utf-16be": case "utf_16be": case "iso-10646-ucs-2": case "x-utf-16be": case "unicodebigunmarked":
                if (utf16be == null) utf16be = new sun.nio.cs.UTF_16BE();
                return utf16be;
            case "utf-16le": case "utf_16le": case "x-utf-16le": case "unicodelittleunmarked":
                if (utf16le == null) utf16le = new sun.nio.cs.UTF_16LE();
                return utf16le;
            default: return null;
        }
    }

    private static String ascii(String s) {
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++) if (c[i] >= 'A' && c[i] <= 'Z') c[i] += 32;
        return new String(c);
    }
}
