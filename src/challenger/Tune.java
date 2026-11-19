package challenger;

// The constants tools/tune varies (tools/tune/AGENTS.md). Each is a compile-time constant: javac copies its value into
// every use, so this class is never loaded and moving a number here changes no bytecode. A line reads
//     public static final TYPE NAME = VALUE; // tune: STEP [MIN, MAX] meaning
// STEP is the design's half-swing (a variant plays VALUE - STEP or VALUE + STEP), MIN and MAX the values that still
// make sense. tools/tune rewrites VALUE and nothing else. Evidence for each value: docs/strategy.md §9, docs/combat.md §10.
public final class Tune {
    // Spawning (robot/king/KingSpawningStrategy): a rat is built while its cost is at most the threshold.
    public static final int SPAWN_CAP = 40; // tune: 10 [20, 80] threshold on a normal map: 16 rats
    public static final int SPAWN_CAP_SMALL = 30; // tune: 10 [10, 60] threshold on maps of <= 900 tiles before r400
    public static final int SPAWN_CHEESE_DIV = 10; // tune: 3 [4, 30] threshold at most global / DIV + 10
    public static final int SPAWN_RICH = 2000; // tune: 500 [500, 5000] 32 rats from global >= RICH - t/2 (t >= 100)
    public static final int SPAWN_VRICH = 3000; // tune: 750 [1000, 6000] no cap from global >= VRICH - 3t/4
    public static final int SPAWN_FLOOR = 100; // tune: 50 [0, 400] a rat above cost 10 needs global >= FLOOR

    // Miners (robot/rat/MinerStrategy).
    public static final double EXPLORE_P = 0.2; // tune: 0.1 [0.0, 1.0] share of trips that explore once mines are known
    public static final int GOHOME_PCT = 100; // tune: 30 [40, 200] go-home loads (10, 25, 40, 60, 80, 105 by distance) in percent
    public static final int IDLE_GOHOME = 50; // tune: 25 [0, 150] with nothing to do, go home above this raw cheese

    // New kings (robot/king/KingFormationCoordinatorStrategy, robot/rat/FormationStrategy).
    public static final int LOCK_IN_ROUND = 800; // tune: 200 [300, 1150] from it, form kings up to 3 without the rat gate
    public static final int LOCK_IN_MIN_CHEESE = 1000; // tune: 400 [200, 3000] global a lock-in formation needs before r1200
    public static final int COOP_ROUND = 100; // tune: 50 [20, 400] from it, one king in cooperation forms a second
    public static final int MINES_KING2 = 5; // tune: 2 [1, 12] known mines a second king needs before r1200
    public static final int JOIN_DIST = 25; // tune: 5 [8, 60] rats within this distance (tiles) join a formation

    // Pathing (Pathfinder): rats path through dirt (and dig it) once global reaches this from r100.
    public static final int DIRT_CHEESE = 1500; // tune: 500 [500, 3000] global from which rats path through dirt

    // Combat (robot/rat/combat).
    public static final int BITE_BONUS_CHEESE = 300; // tune: 150 [0, 2000] bites deal 12 (2 cheese) from this cheese, else 10
    public static final int TRAP_RAW = 20; // tune: 10 [0, 100] rats carrying this raw cheese place traps (TryTrap)
    public static final int BAND_GLOBAL_CHEESE = 400; // tune: 200 [0, 2000] band traps paid from global above this (TryTrap)
}
