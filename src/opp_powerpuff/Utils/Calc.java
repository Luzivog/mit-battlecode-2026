package opp_powerpuff.Utils;

public class Calc {
    public static int determineNumBabyRats(int ratSpawnCost) {
        // cost = 10 + 10(rats / 4)
        // 4 * (cost - 10) / 10 = rats
        ratSpawnCost -= 10;
        int cheeseAdditional = ratSpawnCost / 10;
        int numRats = cheeseAdditional * 4;
        return numRats + 2;
    }

    public static float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    public static float map(float x, float a, float b, float c, float d) {
        float ratio = (x - a) / (b - a);
        return c + ratio * (d - c);
    }

    public static int cheeseForDamage(int damage) {
        // sqrt x = n 
        // 10 + ceil(n) = y
        // ceil(n) = y - 10
        // worst case, x = n => y - 10 + 1
        // x = (y - 10 + 1) ^ 2
        if (damage <= 10) return 0;
        
        int excess = damage - 10;
        return (excess - 1) * (excess - 1) + 1;
    }
}
