package bcx.subst;

import java.io.OutputStream;

/**
 * java.io.PrintStream for translated code (renamed to java/io/PrintStream). Text goes to a native sink: a file
 * descriptor, or nowhere. Not part of any game's outcome.
 */
public class JPrintStream extends java.io.FilterOutputStream implements Appendable, java.io.Closeable {
    private final int fd;

    public static JPrintStream make(int fd) { return new JPrintStream(fd); }

    private JPrintStream(int fd) { super(null); this.fd = fd; }
    public JPrintStream(OutputStream out) { super(out); this.fd = -1; }
    public JPrintStream(OutputStream out, boolean autoFlush) { super(out); this.fd = -1; }
    public JPrintStream(OutputStream out, boolean autoFlush, String encoding) { super(out); this.fd = -1; }

    private static native void write0(int fd, String s);

    private void emit(String s) {
        if (fd >= 0) write0(fd, s);
    }

    public void flush() {}
    public void close() {}
    public boolean checkError() { return false; }
    public void write(int b) { emit(String.valueOf((char) (b & 0xff))); }
    public void write(byte[] buf, int off, int len) { emit(new String(buf, off, len)); }
    public void write(byte[] buf) { write(buf, 0, buf.length); }

    public void print(boolean b) { emit(String.valueOf(b)); }
    public void print(char c) { emit(String.valueOf(c)); }
    public void print(int i) { emit(String.valueOf(i)); }
    public void print(long l) { emit(String.valueOf(l)); }
    public void print(float f) { emit(String.valueOf(f)); }
    public void print(double d) { emit(String.valueOf(d)); }
    public void print(char[] s) { emit(new String(s)); }
    public void print(String s) { emit(String.valueOf(s)); }
    public void print(Object obj) { emit(String.valueOf(obj)); }
    public void println() { emit("\n"); }
    public void println(boolean x) { emit(x + "\n"); }
    public void println(char x) { emit(x + "\n"); }
    public void println(int x) { emit(x + "\n"); }
    public void println(long x) { emit(x + "\n"); }
    public void println(float x) { emit(x + "\n"); }
    public void println(double x) { emit(x + "\n"); }
    public void println(char[] x) { emit(new String(x) + "\n"); }
    public void println(String x) { emit(x + "\n"); }
    public void println(Object x) { emit(String.valueOf(x) + "\n"); }
    public JPrintStream printf(String format, Object... args) { emit(String.format(format, args)); return this; }
    public JPrintStream format(String format, Object... args) { emit(String.format(format, args)); return this; }
    public JPrintStream append(CharSequence csq) { emit(String.valueOf(csq)); return this; }
    public JPrintStream append(CharSequence csq, int start, int end) { emit(String.valueOf(csq == null ? "null" : csq).substring(start, end)); return this; }
    public JPrintStream append(char c) { emit(String.valueOf(c)); return this; }
}
