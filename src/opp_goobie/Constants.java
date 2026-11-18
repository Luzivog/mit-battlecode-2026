package opp_goobie;

public class Constants {
    public final static int INF = 1000000000;
    public final static int MIN_CHEESE_RETURN = 60;
    public final static int RAT_KING_HURT_HP = 150;
    public final static int RAT_KING_NEED_CHEESE_THRESHOLD = 300;
    public final static int MAX_RAT_SPAWNS = 16;
    public final static int MAX_RAT_SPAWNS_2 = 24;

    public final static int MIN_CHEESE_TO_RETURN = 20;
    public final static int MAX_CHEESE_TO_RETURN = 60;

    // Scale cheese return distance thresholds based on map size
    // Small maps (30x30): ~5-15 tiles, Large maps (60x60): ~10-30 tiles
    public final static int CHEESE_TILES_TO_RETURN_MIN = Math.max(5, (Robot.W + Robot.H) / 12);
    public final static int CHEESE_TILES_TO_RETURN_MAX = Math.max(15, (Robot.W + Robot.H) / 4);

    public final static int CHEESE_GLOBAL_NO_EFFECT = 1000;
    public final static int CHEESE_GLOBAL_MAX_EFFECT = 100;
    public final static int CHEESE_GLOBAL_RETURN_FLOOR = 10;

    public final static int CHEESE_MINE_BLACKLIST_COOLDOWN = (Robot.W + Robot.H) / 4;

    public final static int USE_ALL_CHEESE_THRESHOLD = 20;

    public final static int USE_CHEESE_FOR_TRAPS_THRESHOLD = 2000;

    public final static int MAX_SYMMETRY_QUEUE_SIZE = 50;
}
