package bcxtest;

/** Test output: System.out on the JVM; a native (rt_test_print) in a translated test. */
public final class Out {
    public static void print(String s) { System.out.println(s); }
}
