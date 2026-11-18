package opp_powerpuff.Helpers;

import com.fasterxml.jackson.core.io.ContentReference;

import battlecode.common.Direction;

public class Constants {
    public static Direction[] directions = Direction.values();
    public static Direction[] adjacentDirections = {
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST,
        Direction.NORTHEAST,
        Direction.SOUTHEAST,
        Direction.SOUTHWEST,
        Direction.NORTHWEST
    };

    public static Direction[] values = {
        Direction.CENTER,
        Direction.WEST,
        Direction.NORTHWEST,
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST
    };

    public static final int INF = 9999;
    public static final int HEALTH_PLAY_AGGRESSIVE = 40;
    public static final int NAV_AVOID_CAT_DISTANCE = 17;
    public static final int BYTECODE_REQ_SYMMETRY = 4000;
    public static final int EMERGENCY_RADIUS_SQUARED = 12;
    public static final int EMERGENCY_RADIUS_SQUARED_WITH_PADDING = 15;

    // Backing out depending how much cheese
    public static final int CHEESE_RETREAT_LIMIT = 40;
    public static final int CHEESE_DEFAULT_LIMIT = 20;
    public static final int CHEESE_ABSOLUTE_LIMIT = 400;

    public static final int START_RAT_LIMIT = 16;

    public static final int DEAD_BABY_RAT_LIMIT = 4;
    public static final int DANGER_BABY_RAT_LIMIT = 8;
    public static final int MIN_BABY_RAT_LIMIT = 12;
    public static final int MAX_BABY_RAT_LIMIT = 16;
    public static final int WEALTHY_BABY_RAT_LIMIT = 20;
    public static final int KING_BABY_RAT_LIMIT = 24;

    public static final int DEAD_CHEESE_THRESHOLD = 125;
    public static final int DANGER_CHEESE_THRESHOLD = 500;
    public static final int MIN_CHEESE_THRESHOLD = 800;
    public static final int MAX_CHEESE_THRESHOLD = 1200;
    public static final int WEALTHY_CHEESE_THRESHOLD = 1400;
    public static final int KING_CHEESE_THRESHOLD = 1700;
    public static final int MIN_CHEESE_THRESHOLD_CAT = 800;

    public static final int MAX_TRAPS_PER_RAT = 3;
    public static final int MIN_CHEESE_GATHER_SCORE = 75;
    public static final int MAX_CHEESE_GATHER_SCORE = 90;
    public static final float NEUTRAL_CHEESE_GAIN_THERSHOLD = 2.5f;
    public static final int GOOD_CHEESE_GAIN_THRESHOLD = 4;
    public static final int BAD_CHEESE_GAIN_THRESHOLD = 0;

    public static final int MERGE_RAT_DISTANCE = 300;
    public static final int MERGE_RAT_KING_TURNS = 75;
    public static final int MIN_RAT_COUNT_FOR_RAT_KING = 16;

    // MICRO
    public static final int ONE_SHOT_BITE_THRESHOLD = 14;
    public static final int ONE_SHOT_THROW_THRESHOLD = 4 * 2 * 3;

    public static final int ENEMY_KING_SQUEAK_COOLDOWN = 3;

    public static final int ROUNDS_STUCK_ON_CHEESE = 4;
    public static final int EXPLORE_SYMMETRY_SCORE = 20;

    // public static final int CAT_HIT_MAX_CHEESE = 20;
    public static final int RAT_KING_MOVE_SCORE = 100;
    public static final int RETREAT_HEALTH = 50;
    public static final int ROUNDS_RUN_FROM_CAT = 4;

    // RAT KING
    public static final int ROUNDS_AFTER_CAT = 8;
    public static final int ROUNDS_AFTER_ENEMY = 0;
    public static final int SAFE_DISTANCE_SQUARED_FROM_CENTER = 36;
    public static final int ROUND_SPAWN_KING = 300;
    public static final int WEALTH = 500;
    public static final int MISSED_HEARTBEATS = 3;
    // GAME CONSTANTS
    public static final int TRAP_DAMAGE = 50;

    // CAT
    public static final int CAT_TRAP_COOLDOWN = 7;
    public static final int ALLY_ASSIST_RANGE = 18;
    public static final int ALLY_RADIUS = 10;
    public static final int ATTACK_CAT_ROUND = 400;
    public static final int ATTACK_CAT_ROUND_AGGRESSIVE = 1500;
}
