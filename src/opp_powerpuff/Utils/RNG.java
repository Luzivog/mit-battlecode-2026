package opp_powerpuff.Utils;

import java.util.Random;

import opp_powerpuff.Rat;

public class RNG {
    public static Random rng;
    public static void init() {
        rng = new Random(Rat.rc.getID());
    }
}
