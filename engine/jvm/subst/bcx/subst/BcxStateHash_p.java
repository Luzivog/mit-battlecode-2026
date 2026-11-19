package bcx.subst;

import java.util.Random;

/** Fast path of battlecode.world.BcxStateHash.rngState: the Random's 48-bit state read from its seed field, where the
 *  original draws three ints, solves the LCG for the state and restores it with setSeed. Both leave the seed as it
 *  was and clear haveNextNextGaussian. The original stays as rngState$orig; FastPath.CHECK runs both. */
public final class BcxStateHash_p {
    public static long rngState(Random r) {
        if (r.getClass() != Random.class) return rngState$orig(r);
        if (!FastPath.CHECK) return FastPath.randomState(r);
        long o = rngState$orig(r);
        long s = FastPath.randomState(r);
        if (s != o) FastPath.fail("BcxStateHash.rngState: " + s + " against " + o);
        return s;
    }

    @Orig
    public static long rngState$orig(Random r) { return 0; }
}
