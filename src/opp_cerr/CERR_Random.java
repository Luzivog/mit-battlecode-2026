package opp_cerr;

import java.util.ArrayList;
import java.util.List;

public class CERR_Random extends java.util.Random {

    private static long SEED = 899719;
    private static java.util.Random generator = new java.util.Random();
    private static List<CERR_Random> instances = new ArrayList<>();

    public CERR_Random() {
        super();
        instances.add(this);
    }

    public static void init(int id) {
        generator.setSeed(SEED + id);
        for (CERR_Random instance : instances) {
            instance.setSeed(generator.nextLong());
        }
    }
}
