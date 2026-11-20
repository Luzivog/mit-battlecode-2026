package challenger;

// The constants tools/tune varies (tools/tune/AGENTS.md). Each is a compile-time constant: javac copies its value into
// every use, so this class is never loaded and moving a number here changes no bytecode. A line reads
//     public static final TYPE NAME = VALUE; // tune: STEP [MIN, MAX] meaning
// STEP is the design's half-swing (a variant plays VALUE - STEP or VALUE + STEP), MIN and MAX the values that still
// make sense. tools/tune rewrites VALUE and nothing else. Evidence for each value: docs/strategy.md §9, docs/combat.md §10.
public final class Tune {
    // PM (per-map tuning, docs/theory/pm-per-map.md): switches and constants for the book. Each default is main's.
    public static int PM_SYM = 0; // tune: 1 [0, 1] 1: on a book map-seat the robots know Book.symmetry from round 1 (0: they learn it)
    public static final int PM_SYM_B = 0; // tune: 1 [0, 1] 1: seat B's king reads the symmetry from its first scan's order (a general rule)
    public static int PM_COOP_HOLD = 0; // tune: 1 [0, 1] 1: in cooperation with one king, never deal a cat its last HP (PC1 A2)
    public static int PM_SIEGE = 0; // tune: 1 [0, 2] E2b's siege spawns: no rat cap with an enemy in the king's vision (1), or more enemies than our rats there (2)
    public static int PM_SKIP_FAILED = 0; // tune: 1 [0, 2] the coordinator skips mines whose formation timed out: 1 the last one (KF's T), 2 every one
    public static int DIRT_ROUND = 100; // tune: 50 [0, 400] rats path through dirt (and dig it) from this round once global >= DIRT_CHEESE
    public static final int SPAWN_POOR_CUT = 10; // tune: 10 [0, 30] rat-cap cut on maps <= 1600 tiles with < 5 mines (E2: 4 rats)

    // Spawning (robot/king/KingSpawningStrategy): a rat is built while its cost is at most the threshold.
    public static int SPAWN_CAP = 40; // tune: 10 [20, 80] threshold on a normal map: 16 rats
    public static int SPAWN_CAP_SMALL = 30; // tune: 10 [10, 60] threshold on maps of <= 900 tiles before r400
    public static final int SPAWN_CHEESE_DIV = 10; // tune: 3 [4, 30] threshold at most global / DIV + 10
    public static int SPAWN_RICH = 2000; // tune: 500 [500, 5000] 32 rats from global >= RICH - t/2 (t >= 100)
    public static final int SPAWN_VRICH = 3000; // tune: 750 [1000, 6000] no cap from global >= VRICH - 3t/4
    public static final int SPAWN_FLOOR = 100; // tune: 50 [0, 400] a rat above cost 10 needs global >= FLOOR

    // Miners (robot/rat/MinerStrategy).
    public static double EXPLORE_P = 0.6; // tune: 0.1 [0.0, 1.0] share of trips that explore once mines are known
    public static final int GOHOME_PCT = 100; // tune: 30 [40, 200] go-home loads (10, 25, 40, 60, 80, 105 by distance) in percent
    public static final int EXPLORE_KING_DSQ = 36; // tune: 20 [0, 200] exploration targets lie farther than this d² from our kings
    public static int IDLE_GOHOME = 50; // tune: 25 [0, 150] with nothing to do, go home above this raw cheese

    // New kings (robot/king/KingFormationCoordinatorStrategy, robot/rat/FormationStrategy).
    public static final int LOCK_IN_ROUND = 800; // tune: 200 [300, 1150] from it, form kings up to 3 without the rat gate
    public static final int LOCK_IN_MIN_CHEESE = 1000; // tune: 400 [200, 3000] global a lock-in formation needs before r1200
    public static final int KING_TARGET = 3; // tune: 1 [2, 5] the lock-in forms kings up to this many
    public static final int LOCK_IN_MIN_RATS = 12; // tune: 4 [4, 24] rats a lock-in or cooperation formation needs (numRats, a multiple of 4)
    public static final int COOP_ROUND = 100; // tune: 50 [20, 400] from it, one king in cooperation forms a second
    public static int MINES_KING2 = 5; // tune: 2 [1, 12] known mines a second king needs before r1200
    public static final int JOIN_DIST = 25; // tune: 5 [8, 60] rats within this distance (tiles) join a formation

    // Pathing (Pathfinder): rats path through dirt (and dig it) once global reaches this from r100.
    public static final int DIRT_CHEESE = 1500; // tune: 500 [500, 3000] global from which rats path through dirt

    // Combat (robot/rat/combat).
    // Move scores: a tile an enemy can grab us from loses THREAT_PENALTY (CombatStrategy.getCellScoreFor); TryRatNap scores
    // a grab of an enemy rat RATNAP_W (an ally 10); TryFight scores a step toward the fight FIGHT_STEP (a non-strafe +10,
    // facing the target 5). Each changes a choice only where it crosses the others: THREAT_PENALTY below ~50, FIGHT_STEP
    // below ~17, RATNAP_W below ~9 or the ally's 10, so their steps reach those values.
    public static final int THREAT_PENALTY = 100; // tune: 60 [10, 1000] score lost on a tile an enemy can grab us from
    public static final int RATNAP_W = 30; // tune: 25 [1, 200] TryRatNap's score of a grab of an enemy rat
    public static final int FIGHT_STEP = 30; // tune: 20 [0, 200] TryFight's score of a step toward the fight
    public static final int BITE_BONUS_CHEESE = 300; // tune: 150 [0, 2000] bites deal 12 (2 cheese) from this cheese, else 10
    public static final int TRAP_RAW = 20; // tune: 10 [0, 100] rats carrying this raw cheese place traps (TryTrap)
    public static final int BAND_GLOBAL_CHEESE = 400; // tune: 200 [0, 2000] band traps paid from global above this (TryTrap)

    // The rest of the micro's scores and gates (docs/combat.md §10; the fight lab, docs/theory/fl-fight-lab.md). A score is
    // compared only with the other scores of its own move generator (and its accept threshold, 0.5), so each step reaches
    // the nearest value where an order between two of them flips.
    // TryFight: a candidate scores FIGHT_NOSTRAFE for a non-strafe (forward, turn-move or hold), FIGHT_STEP on a BFS step
    // toward the fight, -FIGHT_FACE when the final facing does not see the target, +FIGHT_TURN for a facing change.
    public static final int FIGHT_NOSTRAFE = 10; // tune: 8 [-40, 60] TryFight's score of a move that is not a strafe
    public static final int FIGHT_FACE = 5; // tune: 5 [0, 40] TryFight's loss when the final facing does not see the target
    public static final int FIGHT_TURN = 2; // tune: 2 [0, 20] TryFight's score of a facing change
    // TryAttack: a bite scores ATTACK_W (+ATTACK_KILL if it kills), minus the strafe cost, +2 for a facing change.
    public static final int ATTACK_W = 10; // tune: 6 [1, 60] TryAttack's score of a bite (accepted from 0.5)
    public static final int ATTACK_KILL = 30; // tune: 20 [0, 100] TryAttack's extra score of a bite that kills
    public static final int ATTACK_LOWHP = 24; // tune: 12 [0, 100] at this HP or less a biter ignores its target's grab threat
    // TryRatNap: the ally sacrifice (an ally of ALLY_HP or less, with >= 8 rats) and the grab-then-step-back bonus.
    public static final int RATNAP_ALLY_W = 10; // tune: 8 [0, 60] TryRatNap's score of a grab of a weak ally (to throw)
    public static final int RATNAP_ALLY_HP = 22; // tune: 10 [0, 60] the ally sacrifice takes allies at this HP or less
    public static final int RATNAP_BACK = 5; // tune: 5 [0, 40] TryRatNap's extra score of grabbing, then stepping back
    // The strafe cost of TryAttack, TryRatNap and TryTrap: low when the movement cooldown is < 2 (the next move stays).
    public static final int STRAFE_LO = 0; // tune: 3 [0, 30] strafe cost from a movement cooldown < 2 (FL: 3 -> 0, +1.7 pp on 3,000 fresh cells)
    public static final int STRAFE_HI = 8; // tune: 4 [0, 40] strafe cost from a movement cooldown of 2 or more
    // Carrying an enemy (CombatStrategy): throw when the best line scores THROW_HOLD (42: a collision at step 2), else hold
    // and act with the free action; throw whatever the line once the carry is CARRY_AGE rounds old or at CARRY_HP.
    public static final int THROW_HOLD = 42; // tune: 20 [-1000, 300] the best line's score that throws a carried enemy
    public static final int CARRY_AGE = 8; // tune: 3 [2, 10] rounds of carrying after which any line is thrown
    public static final int CARRY_HP = 24; // tune: 12 [0, 100] carrier HP at or below which any line is thrown
    // TryThrow's scores of what a line hits (a wall at step 2 is the engine's 42).
    public static final int THROW_KING = 300; // tune: 150 [0, 1000] a line into an enemy king's 3x3
    public static final int THROW_FEED = 400; // tune: 200 [0, 1000] a line into a cat (the flier is eaten)
    public static final int THROW_RAT2 = 84; // tune: 40 [0, 400] a line into an enemy rat at step 2 (42 to each)
    // TryTrap: the band trap (the tile between us and an enemy rat at Chebyshev 2) once the map holds BAND_MIN_MINES
    // known mines (a large value: never); TRAP_STRONG_HP marks an enemy worth a trap outside the band and trap mode.
    public static final int BAND_MIN_MINES = 5; // tune: 3 [0, 99] known mines from which rats place band traps
    public static final int BAND_W = 20; // tune: 10 [0, 200] the band tile's trap weight (its two neighbours BAND_W_SIDE)
    public static final int BAND_W_SIDE = 10; // tune: 10 [0, 200] the trap weight of the band tile's neighbours
    public static final int TRAP_STRONG_HP = 80; // tune: 10 [0, 100] an enemy at this HP and a bite above ours is trapped
    // shouldFight: a rat fights any enemy within KING_DEF_DSQ of our king, and a cat within CAT_DEF_DSQ of it.
    public static final int KING_DEF_DSQ = 36; // tune: 20 [0, 200] d² from our king within which rats fight any enemy
    public static int CAT_DEF_DSQ = 36; // tune: 20 [0, 200] d² from our king within which rats fight a cat
    // A carried rat at less than this HP disintegrates (BabyRat), denying the enemy a projectile.
    public static final int DISINTEGRATE_HP = 15; // tune: 10 [0, 60] HP below which a rat carried by an enemy disintegrates

    // KR: the king's bug hand (Pathfinder; docs/theory/kr-king-routes.md). Main's king always starts left-handed; a
    // mirror reverses that, so on reflective maps the two seats' kings go round mirrored walls on opposite sides.
    // 0: left (main); 1: right; 2: right for chirality 1; 3: right for chirality 1 on VERTICAL and HORIZONTAL maps;
    // 4: right for chirality 1 on VERTICAL maps; 5: right for team B on VERTICAL maps; 6: right for team B; 7: right for
    // team B except a chirality-1 king on HORIZONTAL or ROTATIONAL maps. The king reads its chirality and, for
    // chirality 1, the symmetry from its first scan's order (CellTracker); a chirality-0 king of team B knows the
    // symmetry once the rats find it.
    public static int KING_HAND = 7; // tune: 1 [0, 7] the kings' bug hand (above): 0 left, 1 right

    // CM (book switches, docs/theory/cm-cat-maps.md): each default leaves main's games unchanged; 1 turns a 0/1 switch on.
    // The last-cat hold is PM_COOP_HOLD (above): CM's CM_LAST_CAT_HOLD was the same code.
    // From this round, with one king, a second king at the book's site Book.KING2_SITE, else main's mine choice, whatever
    // the cooperation (robot/king/KingFormationCoordinatorStrategy); 2000 or more: never.
    public static int CM_KING2_ROUND = 2000; // tune: 30 [15, 2000] round of the early second king (2000 never)
    // K2 D': kings remember cat footprints (CatLanes, from round 1 the book's Book.CAT_LANE where it has one), park and
    // chase cheese off them, take no mining step into a known lane (robot/king/KingMiningStrategy).
    public static int CM_LANE_MEMORY = 0; // tune: 1 [0, 1] kings' cat-lane memory for mining (0 off)
    // With a cat in view and lanes known, the king's flee loses 4 a lane tile its step adds (robot/king/KingCombatStrategy).
    public static int CM_LANE_FLEE = 0; // tune: 1 [0, 1] the king's flee off known cat lanes (0 off)
    // PT 2d's late cat hunt starts at this round (robot/rat/combat/CombatStrategy).
    public static int CM_CAT_HUNT_START = 1300; // tune: 300 [300, 2000] round from which idle rats hunt cats
    // The first king's site: k > 0 makes the k-th tile of Book.KING1_SITE its mine target from round 1 (robot/king/
    // KingMiningStrategy); 0 main.
    public static int CM_KING1_SITE = 0; // tune: 1 [0, 12] the first king's book site (0 off, k: the k-th tile)
    // Every baby rat starts with the map's mines from the book (Book.CM_MINES) among its known mines (MineBook); 0 main.
    public static int CM_MINE_BOOK = 0; // tune: 1 [0, 1] baby rats know the map's mines from the book (0 off)

    // SG (docs/theory/sg-seat-gaps.md). The rats' bug lookahead (Pathfinder.doLookahead) prefers its left-hand bug:
    // it advances it first on equal paths, takes only its arrival and breaks score ties to it; 1 mirrors each rule.
    public static int SG_LOOK_RIGHT = 0; // tune: 1 [0, 1] the rats' lookahead's preferred hand: 0 left, 1 right
    // 1: the king plans its walk to a mine or the centre over its own map (robot/king/KingPath, MS's KP3) instead of the bug.
    public static int KING_PATH = 0; // tune: 1 [0, 1] the king's route planner: 0 off (main), 1 on
    // AT (docs/theory/at5-shelf.md). 1: every robot starts with the book's walls and round-1 dirt of the map (Book.AT_WALL,
    // Book.AT_DIRT) in CellTracker, every cell seen (MS's ms_okb v2 oracle as book data).
    public static int AT_MAP = 0; // tune: 1 [0, 1] the map's walls and dirt from the book: 0 off (main), 1 on
}
