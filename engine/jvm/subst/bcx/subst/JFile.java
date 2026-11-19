package bcx.subst;

/** java.io.File for translated code (renamed): a path string; existence asked natively. */
public class JFile implements java.io.Serializable, Comparable<JFile> {
    private final String path;
    public static final char separatorChar = '/';
    public static final String separator = "/";
    public static final char pathSeparatorChar = ':';
    public static final String pathSeparator = ":";

    public JFile(String pathname) {
        if (pathname == null) throw new NullPointerException();
        path = pathname;
    }

    public JFile(String parent, String child) {
        if (child == null) throw new NullPointerException();
        path = parent == null ? child : join(parent, child);
    }

    public JFile(JFile parent, String child) {
        if (child == null) throw new NullPointerException();
        path = parent == null ? child : join(parent.path, child);
    }

    private static String join(String p, String c) {
        if (p.isEmpty()) return "/" + c;
        return p.endsWith("/") ? p + c : p + "/" + c;
    }

    public String getPath() { return path; }
    public String getName() { return path.substring(path.lastIndexOf('/') + 1); }
    public String getAbsolutePath() { return path; }
    public JFile getAbsoluteFile() { return this; }
    public boolean exists() { return bcx.NativeIO.exists(path); }
    public String toString() { return path; }
    public int compareTo(JFile o) { return path.compareTo(o.path); }
    public boolean equals(Object o) { return o instanceof JFile f && f.path.equals(path); }
    public int hashCode() { return path.hashCode() ^ 1234321; }
}
