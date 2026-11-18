package bcx.subst;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** java.util.zip.GZIPOutputStream for translated code (renamed): buffers, then gzips with zlib in one call. */
public class JGZIPOutputStream extends java.io.FilterOutputStream {
    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private boolean finished;

    public JGZIPOutputStream(OutputStream out) throws IOException { super(out); }
    public JGZIPOutputStream(OutputStream out, int size) throws IOException { super(out); }
    public JGZIPOutputStream(OutputStream out, boolean syncFlush) throws IOException { super(out); }

    private static native byte[] gzip0(byte[] data);

    public void write(int b) { buf.write(b); }
    public void write(byte[] b, int off, int len) { buf.write(b, off, len); }
    public void flush() throws IOException { out.flush(); }

    public void finish() throws IOException {
        if (finished) return;
        finished = true;
        byte[] z = gzip0(buf.toByteArray());
        out.write(z, 0, z.length);
    }

    public void close() throws IOException {
        finish();
        out.close();
    }
}
