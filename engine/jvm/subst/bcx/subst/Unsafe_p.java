package bcx.subst;

/** Fast paths of jdk.internal.misc.Unsafe.get{Short,Int,Long}Unaligned(o, offset, bigEndian) (HotSpot intrinsics;
 *  ByteBuffer's reads go through them): one native read (FastPath). The originals stay as NAME$orig;
 *  FastPath.CHECK runs both. */
public final class Unsafe_p {
    public final short getShortUnaligned(Object o, long offset, boolean bigEndian) {
        short f = FastPath.unalignedShort(o, offset, bigEndian);
        if (FastPath.CHECK && f != getShortUnaligned$orig(o, offset, bigEndian)) FastPath.fail("Unsafe.getShortUnaligned");
        return f;
    }

    public final int getIntUnaligned(Object o, long offset, boolean bigEndian) {
        int f = FastPath.unalignedInt(o, offset, bigEndian);
        if (FastPath.CHECK && f != getIntUnaligned$orig(o, offset, bigEndian)) FastPath.fail("Unsafe.getIntUnaligned");
        return f;
    }

    public final long getLongUnaligned(Object o, long offset, boolean bigEndian) {
        long f = FastPath.unalignedLong(o, offset, bigEndian);
        if (FastPath.CHECK && f != getLongUnaligned$orig(o, offset, bigEndian)) FastPath.fail("Unsafe.getLongUnaligned");
        return f;
    }

    @Orig
    public final short getShortUnaligned$orig(Object o, long offset, boolean bigEndian) { return 0; }

    @Orig
    public final int getIntUnaligned$orig(Object o, long offset, boolean bigEndian) { return 0; }

    @Orig
    public final long getLongUnaligned$orig(Object o, long offset, boolean bigEndian) { return 0; }
}
