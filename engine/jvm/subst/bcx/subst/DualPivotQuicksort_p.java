package bcx.subst;

/**
 * Patch of java.util.DualPivotQuicksort: the JDK's sorts without their parallel branch, which Arrays.sort never
 * takes (it passes parallelism 0) but which would pull in ForkJoin and threads.
 */
final class DualPivotQuicksort_p {
    static final class Sorter {}

    static void sort(int[] a, int parallelism, int low, int high) { sort((Sorter) null, a, 0, low, high); }

    static void sort(long[] a, int parallelism, int low, int high) { sort((Sorter) null, a, 0, low, high); }

    static void sort(float[] a, int parallelism, int low, int high) {
        int numNegativeZero = 0;
        for (int k = high; k > low; ) {
            float ak = a[--k];
            if (ak == 0.0f && Float.floatToRawIntBits(ak) < 0) {
                numNegativeZero += 1;
                a[k] = 0.0f;
            } else if (ak != ak) {
                a[k] = a[--high];
                a[high] = ak;
            }
        }
        sort((Sorter) null, a, 0, low, high);
        if (++numNegativeZero == 1) return;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            if (a[middle] < 0) low = middle + 1;
            else high = middle - 1;
        }
        while (--numNegativeZero > 0) a[++high] = -0.0f;
    }

    static void sort(double[] a, int parallelism, int low, int high) {
        int numNegativeZero = 0;
        for (int k = high; k > low; ) {
            double ak = a[--k];
            if (ak == 0.0d && Double.doubleToRawLongBits(ak) < 0) {
                numNegativeZero += 1;
                a[k] = 0.0d;
            } else if (ak != ak) {
                a[k] = a[--high];
                a[high] = ak;
            }
        }
        sort((Sorter) null, a, 0, low, high);
        if (++numNegativeZero == 1) return;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            if (a[middle] < 0) low = middle + 1;
            else high = middle - 1;
        }
        while (--numNegativeZero > 0) a[++high] = -0.0d;
    }

    @Orig static void sort(Sorter sorter, int[] a, int bits, int low, int high) {}
    @Orig static void sort(Sorter sorter, long[] a, int bits, int low, int high) {}
    @Orig static void sort(Sorter sorter, float[] a, int bits, int low, int high) {}
    @Orig static void sort(Sorter sorter, double[] a, int bits, int low, int high) {}
}
