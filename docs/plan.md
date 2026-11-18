# Plan: work packages to 90% against awubot

This plan comes from track T (docs/program.md) and is the input to track B. Its theory is in
`docs/theory/dominant-strategy.md` (cited `DS§n`); the lens files are cited `T1§n` … `T5§n`. Other source keys follow
DS: `R§` is rules.md, `E:` the engine 1.2.6, `C:` `src/challenger/`, and `A` marks arithmetic. "pp" means
percentage points of win rate against awubot, under official conditions, both seats.

## 0. Summary

- **Gate.** S1 (never die to our own code) merges first. It is certain and cheap, and it touches the assert lines
  that four other packages edit.
- **Wave 1.** S1, E1, KL, CT, CW, K1, CB, PT. They are built in parallel on disjoint code (§2) and merged one at a
  time, in that order. Expected: about 75% against awubot (range 66–82%; §5).
- **Wave 2.** E3, KB, E2, K2, K3, CX, CC, CK. Each starts when its lane's predecessor has merged. Expected: about
  89% if the combat class converts at about 75–80% (§5).
- **Wave 3.** Opened only if wave 2 stalls (§6).
- **Weakest assumption.** That consistent micro edges (CT, CW, CB, CX) win both seats on the maps decided by close
  combat: 15 of the 43 official maps, plus the combat part of 6 more. Nothing measured yet supports it; the mechanism
  argument is DS§2.6. If it fails, the ceiling after wave 2 is about 80%.

| rank | id | package | expected effect (pp) | depends on | lane |
|---|---|---|---|---|---|
| 1 | S1 | Never die to our own code | +0.5 to +1.5 on tune; certain wins on maps with ≥ 26 mines | – | safety |
| 2 | E1 | Upkeep reserve and siege spawns | +4 to +8 | S1 | economy |
| 3 | KL | King lock-in by budding: ≥ 3 kings before r1201, refill after | +4 to +8 | S1 | kings |
| 4 | CT | Trap the approach band | +3 to +8 | S1 | traps |
| 5 | CW | Cat-aware, value-gated throws; hold and bite | +3 to +6 | S1 | throws |
| 6 | K1 | King steps out of a cat's lane | +1 to +4 (official pool); large on `cat` | S1 | cats |
| 7 | CB | Minimal-cheese bites, cat-bite cheese, action theft fix | +1 to +3 | S1 | bites |
| 8 | PT | Endgame points conversion | +2 to +4 before KL, +0 to +2 after | S1 | points |
| 9 | E3 | Territory: miners by deficit, exploration that ends | +2 to +6 | S1, payload schema (§2.1) | territory |
| 10 | KB | Early second king (dose on the round) | +2 to +6, sign uncertain | KL | kings |
| 11 | E2 | Army from income and attrition | +2 to +5 | E1 | economy |
| 12 | K2 | Cat-lane memory and a safe king site | +1 to +3 (official pool); +10 on `cat` over K1 | K1 | cats |
| 13 | K3 | Rats defend the king from cats | +0 to +1 (official pool); +2 to +4 on tune | K1, PT, CW | cats |
| 14 | CX | Complete threat model and trap avoidance | 0 to +5, sign uncertain | CT | traps |
| 15 | CC | Carrier discipline against enemy throws | +1 to +4 | CW | throws |
| 16 | CK | King assault and throw shield | +1 to +4 | CW, K1 | throws |
| – | PC, XA, RB, IS, CH, BL, QD, JS, AB | wave 3 (§4.3) | – | – | – |

## 1. How track B runs a package

1. **Setup.** One agent per package, on its own branch from main's `src/challenger`. Edit only the files the package
   names (§2.2), one change at a time. Templates and regeneration: docs/codegen.md. Run
   `./gradlew build -x test` before every commit (AGENTS.md).
2. **Engine.** Accept on engine 1.2.6, the official engine. The lens data is 1.2.5 and only sizes the mechanisms;
   DS§10 lists the numbers that move.
3. **Two kinds of test**, both with `tools/games`' GSPRT (α = β = 0.05, paired seats). δ is the H1 edge over 50%.
   - **Gain test:** H1 = 50% + δ. Accept the package on H1.
   - **Harm test:** H1 = 50% − δ_h. It must end on H0. Use it for certain-sign fixes and enablers, and for the class
     pools a package must not hurt.
   - Expected games under H1 without pairing (A: 0.95·ln 19 / KL divergence): δ 0.15 → 61, 0.10 → 139,
     0.08 → 218, 0.05 → 559, 0.04 → 873, 0.03 → 1,553. Pairing cuts these when most pairs split; W0.1 measures by
     how much.
4. **Pool order.** First the class pool where the effect concentrates, at the larger δ. Then the tune pool at the
   package's tune δ, or a harm test there.
   - Class pools come from the tune split only, never from the sealed holdout: `cat`, `starve`, `choke`, `faceoff`
     (T4 WP5), `weak` (intel§3.1), `stress` (≥ 26 mines, many cats, heavy dirt) and **`points`**.
   - `points` is new: the 11 mirror points maps (DS§1.1) plus the points- and cooperation-prone tournament maps
     (groveisbeingdesecrated, t_minus_five_minutes, fullofsecrets, rose, clockwisdom; T3§6).
   - Always add `5t4rv4t10n_1337` explicitly (T4§3).
5. **Read the mechanism, not just the totals.** Every package names its diagnostic metric (§3). If the metric does
   not move, the result is noise whatever the totals say.
   - Read the flipped pairs. A pair counts as flipped only if it is outside W0.1's background rate.
   - Tools, all under `~/battlecode26-data/theory/`:
     - `t3/Coop2.java`: final D per game, kings, cheese, cat damage, raw held, formation rounds.
     - `t4/kcause.py`, `t4/src/KingTrace.java`: king HP by source.
     - `tools/Deep.java`: deaths by cause, traps placed and triggered, mines held, starvation turns, no-cause deaths.
     - `t2/tools/CombatStats.java`: throw outcomes, feeds, bites while carrying, hold times.
     - `t1/tools/Timeline.java`: income, rats and kings per 10 rounds.
     - `synthesis/KingCrowd.java`: king turns with ≥ 26 robots in view.

     Report deaths split by cause: combat, trap, collision, cat, formation (planned), disintegration, Error (DS§8
     item 8).
6. **Kill criteria** are per package (§3). A package that ends on H0 is recorded in a closed-directions log with its
   diagnostic (lessons§9). A lane closes after 3 consecutive H0s.
7. **Gauntlet.** Before merge, play every sparring bot paired against awubot's own score on the same cells, OBG and
   GST first. No significant drop.
8. **Report.** Branch, head commit, GSPRT results per pool, diagnostic values against the zero arm, the flipped pairs
   with their causes, and bytecode: mean and p99 per unit type, and overruns.

## 2. Shared resources and file ownership

### 2.1 Interfaces fixed by this plan

**Spawn payload.** 13 bits, carried in GlobalArray slots 22–31 (arch§4.1). Today
`C:robot/king/KingSpawningStrategy.java:93` sends `Rng.mcg() & 8191`, and `C:robot/rat/BabyRat.java:26` discards it.

| bits 12–10 (role) | meaning | argument (bits 9–0) | owner |
|---|---|---|---|
| 0 | default: today's MinerStrategy | – | – |
| 1 | miner with an assigned mine | `encode2x2(mine)` (≤ 957) | E3 |
| 2 | bud recruit | slot index (bits 3–0) and the site's offset code (bits 9–4) | KL, KB |
| 3 | cat defender | – | K3 |
| 4–7 | reserved | – | – |

Payload 0 must keep today's behaviour. The first package to merge adds the role switch in `BabyRat.init` and an
encoder next to `GlobalArray.registerSpawnedRat` (`C:comms/GlobalArray.java:86-104`); later packages add their
case. Dropping the `Rng.mcg()` call shifts every robot's random stream, which is expected (arch§3).

**Shared array.** Free today: slot 15, slot 1 bits 2–9, slot 9 bits 3–9 and slot 21 bits 5–9 (arch§4.1).

| space | owner | use |
|---|---|---|
| slots 38–41 (`MIN_CHEESE` 38 → 42; once S1 is in, the cheese list of 22 slots rotates beyond 22 mines) | K2 | the last sighting of up to 2 cats: `encode2x2` location (10 bits), facing (3 bits), round mod 128 (7 bits) |
| slot 15 | PC | points estimator |
| the rest | reserved | amend this plan first |

E2 needs no slot: deaths come from `ratCounter` (slot 0) and n from `getCurrentRatCost`.

**Squeak headers** (arch§4.2):
- header 2 (today the unused lure stub) → K2's cat sighting. It also replaces the `7<<29` panic squeak, which decodes
  as two phantom enemies (C:comms/Squeaker.java:173-217).
- header 3 → CC's carried-rat report.

**`util/Economy.java`** (owner E1): `reserve()` = 2·K·H, `spendable()` = global − `reserve()`, and a siege flag.
Every other package that spends cheese (KL, KB, CT, CB, PT, E2) gates on `Economy.spendable()` once E1 is in main.
Until then it uses its own constant floor, and the integrator swaps that floor at merge.

### 2.2 File ownership

Packages on the same row either edit disjoint line ranges (parallel is fine; the integrator merges textually) or
are marked → (sequence: the later one starts from main after the earlier merged).

| file (`src/challenger/`) | packages and regions |
|---|---|
| `RobotPlayer.java` | S1 (:84) |
| `RobotTracker.java.jinja2` | S1 (:46-49, :65-140) |
| `comms/GlobalArray.java` | S1 (:413-429) · KL or E3 (payload encoder near :86-104) · K2 (`MIN_CHEESE` :61, cat slots) · PC (slot 15) |
| `comms/Squeaker.java` | K2 (panic squeak, header 2) · CC (header 3) |
| `util/Economy.java` (new) | E1 |
| `robot/rat/BabyRat.java` | S1 (:34-77) · CB (:179-213 `rawMove`/`turn`, :240-250 `attack`) · KL / E3 / K3 (`init` :25-32, role switch) |
| `robot/rat/MinerStrategy.java` | E3 (:60-173 targets, exploration) · PT (:175-213 `shouldGoHome`) |
| `robot/rat/FormationStrategy.java`, new `robot/rat/BudStrategy.java` | KL → KB |
| new `robot/rat/CatDefenseStrategy.java` | K3 |
| `robot/king/RatKingStrategy.java` | KL (one line: run `KingBudStrategy` first, :24-29) |
| new `robot/king/KingBudStrategy.java` | KL → KB |
| `robot/king/KingSpawningStrategy.java` | E1 → E2 (:21-73) · E3 (:93 payload) |
| `robot/king/KingFormationCoordinatorStrategy.java` | KL |
| `robot/king/KingMiningStrategy.java.jinja2` | K1 → K2 |
| `robot/king/KingCombatStrategy.java.jinja2` | K1 → CK |
| `Pathfinder.java.jinja2` | K2 (cat-lane penalty, :678-725) |
| `robot/rat/combat/CombatStrategy.java.jinja2` | CW (:216-231) · CB (:440-443, :484-488) · PT (:453-457) → K3 (:455) · CX (:366-382) |
| `robot/rat/combat/CombatTracker.java.jinja2` | CX |
| `moves/TryThrow.java.jinja2` | S1 → CW → CC, CK |
| `moves/TryTrap.java.jinja2` | S1 → CT |
| `moves/TryAttack.java.jinja2`, `moves/TryRatNap.java.jinja2` | S1 → CB |
| `moves/TryPosition.java.jinja2` | S1 → CW → CC |
| `moves/TryFight.java.jinja2` | S1 → CX, CC |

## 3. Wave 0: no bot change (owners: track E and the integrator)

- **W0.1 Null calibration.** Run challenger plus a pure bytecode perturbation against awubot on the tune pool: a
  dummy loop of about 200 bytecodes per rat turn, no logic change. Measure the share of pairs that become 2–0 or
  0–2. This is the background against which single-map flips are read.
  - If it is ≥ 20%, only class totals and diagnostics carry information.
  - It also sizes the pairing gain in §1 item 3.
- **W0.2 Pools.** Build the class pools of §1.4 from the tune split, with T4's classifier
  (`t4/classify.py`, `catclock.py`, `classes2.py`) placed next to `tools/games` (T4 WP5).
- **W0.3 Baseline loss table on the tune pool.** Run the challenger–awubot mirror once on the tune pool and classify
  every map as DS§1.1 does (`synthesis/losstable.py` with `kcause.py` and `Coop2.java`). The §5 arithmetic uses the
  43 official maps; the tune pool adds tournament maps where S1, K1–K3 and KL weigh more. Rerun §5 with this table.

## 4. Packages

Each entry lists what the package changes, what it rests on, its effect, how to test it, its risks and its size.

"Flips" names the classes and maps of DS§1.1 (43 official maps, mirror on 1.2.5) where the package's mechanism
decided the losing seat. In the mirror every map is 1–1, so flipping a map's losing seat is +1 game of 86 (+1.16 pp).

Bytecode today (arch§8): rats average 11.3k of 17.5k; kings 13.2k of 20k, with a maximum of 19,988 on DefaultLarge.

### 4.1 Wave 1

#### S1. Never die to our own code
- **Change.**
  1. `C:comms/GlobalArray.java:427-428`: delete `else assert(false)`. When the list is full, `readCheeseMines`
     (:403-410) stops at slot 38 without a terminator, and the queue rotation (:421-423) cycles the rest.
  2. `moves/TryThrow.java.jinja2`: mark every visible cat's 2×2 as occupied for the step-1 legality checks
     (:258-293, :305-315, :410-417). Replace `assert(rc.canThrowRat())` (:101, :106) by a check that skips the throw.
  3. Every `assert(CombatStrategy.performMoves(...))` becomes `if (!...)`, keeping the call:
     - TryAttack :67, :71;
     - TryTrap :99, :103;
     - TryRatNap :95, and `assert(false)` at :82;
     - TryPosition :39.

     Also `assert(false)` in TryFight :55 becomes `return false`.
  4. `RobotTracker.java.jinja2`: keep the nearest 25 robots. With ≥ 26 robots in view the arrays of size 25 throw
     (the king loses the whole turn), and with ≥ 44 the template asserts (:106, :137).
  5. `robot/rat/BabyRat.java:42,68`: null-check `senseRobotAtLocation` (an NPE that loses the turn in front of a cat).
  6. `RobotPlayer.java:84`: `catch (Throwable t)`. This is safe: the engine's kill signal extends
     `VirtualMachineError`, which every handler re-throws first (DS§8 item 1).

  Not in S1: cats as throw targets (CW), and a king in a cat lane (K1, K3).
- **Rests on.**
  - T5 A15: sieveofriches kings removed at r457–597 while leading.
  - arch§1.6: bridges r401, quadrants r764.
  - T2§9.1: the assert fires in 15 of 70 games, 0.26 per game, cat on step 1 in 5 of 5 instrumented failures.
  - S:kingcrowd_mirror.txt: ≥ 26 robots in a king's view on 11 king turns in 86 games.
- **Effect.** The sign is certain.
  - +1 pair on every map with ≥ 26 mines where awubot learns 26 mines: sieveofriches; bridges, bridgesCatless and
    quadrants (team-made); tminusten too, but only once K3 keeps our king alive.
  - Elsewhere about 0.26 of our rats saved per game: +0.5 to +1.5 pp on tune.
  - **Flips:** no official map; 1–2 tournament maps; any map's close game, at random.
- **Test.**
  - `stress` pool, gain test at δ = 0.15. Expect 2–0 on the ≥ 26-mine maps. Then a harm test on tune at δ_h = 0.04.
  - Diagnostic: no `AssertionError` or "froze" lines in the logs; our no-cause deaths are disintegrations only
    (Deep `diedNoCause`); no thrower dies on a cat tile.
  - **Kill:** none. A failing harm test means a bug: revert items 3–6 first and bisect.
- **Risks.** Behaviour unchanged except the random stream. Bytecode: under 50 per turn.
- **Size.** 40–90 template lines.

#### E1. Upkeep reserve and siege spawns
- **Change.**
  - New `util/Economy.java` (§2.1).
  - `KingSpawningStrategy.java:69`: spawn only if `spendable() ≥ cost`. Delete the `cost == 10` exception.
  - Siege (an enemy baby rat in the king's vision, or HP lost this turn): the reserve drops to 2K·20, and OBG's
    emergency rule applies: spawn toward the threat while cost ≤ global/5 (O:RatKing.java:235-256).
  - Arms:
    - H ∈ {60, 120}, against the champion's rule as the zero arm.
    - Only if those stall on `starve`: an income-aware floor `max(0, 2K − Î)·min(2000 − t, 300)`, where Î is the
      king's 100-round global-cheese change plus its own spawn spending. T4's uncapped form blocks all spawning
      early (DS§8 item 5).
- **Rests on.** DS§4.1 item 1; T4§2.2–2.4, WP1; T5§2.2, P5; T1§2.8, C2.
- **Effect.** +4 to +8 pp on tune; +15 to +30 on `starve`.
  - **Flips:** STARVE (8: whereisthecheese, corridorofdoomanddespair, popthecork and closeup most surely), part of
    COMBAT+starve (6), and sittingducks (points; 660 HP starved).
  - The two packages that touch these maps differ: E1 removes our starvation, CT and E3 cause theirs.
- **Test.**
  - `starve` gain test at δ = 0.10, then tune at δ = 0.04, then a harm test on `faceoff` at δ_h = 0.05.
  - Diagnostic: our king's starvation turns fall to 0 outside sieges (kcause, Deep); awubot's do not; rats alive in
    r0–300 on MINEPOOR maps.
  - **Kill:** `starve` ends on H0, or harm on `faceoff` (fewer rats lose the early fights on tiny and DefaultSmall).
- **Risks.** A smaller army when poor (food lost small maps that way, intel§4). Gauntlet: SPAARK's and GST's mass
  spawning. Bytecode: trivial.
- **Size.** 40–90 lines.

#### KL. King lock-in by budding
- **Change.**
  - New `robot/king/KingBudStrategy.java`, run before spawning in `RatKingStrategy.runTick` (:24-29). While it buds
    it uses the king's action itself, so `KingSpawningStrategy` needs no edit.
  - New `robot/rat/BudStrategy.java`, payload role 2.
  - `KingFormationCoordinatorStrategy.java`: the old path is disabled while a bud is active. Its r1100 abort (:145)
    moves to 1195.
  - Policy, in rounds [T_lock, 1195] with kings < K_target, `spendable()` ≥ the bud cost 50 + Σ c(n..n+6), and no
    enemy rat within 8 tiles of the king for 10 rounds:
    - Site: a 3×3 whose centre is at Chebyshev 3 from the king's centre. All 9 tiles passable, no mine, not in a cat
      lane seen in the last 50 rounds, on the side away from the last enemy sighting.
    - Spawn 7 recruits on the ring tiles nearest the site (at most 3 lie inside it, DS§4.3). They step to fixed slots.
      The centre recruit calls `becomeRatKing` once 6 allies stand in the ring (R§8).
  - After r1200 with 1 king: bud at once.
  - The new king then walks to the nearest unassigned mine; KingMiningStrategy is unchanged.
  - Arms: T_lock ∈ {800, 1000}; K_target ∈ {3, 5}; budding against recruiting 7 existing rats (T1 E1).
- **Rests on.**
  - T3§3.2: a third king flips 10 of the 11 losing points seats.
  - T3§3.5: 790–2120 global at r1100 in every r2000 mirror seat, against a bud cost of 430 at 16 rats (A).
  - T1§2.7 and E1; T3 P1; R§6.
- **Effect.** +4 to +8 pp on tune (T1 and T3 agree); the upper bound is +11.6 (T3 P1).
  - **Flips:** POINTS (up to 10 of 11), and part of the 9 maps with kills after r990 (hatefullattice, cheesefarm,
    closeup, toomuchcheese, cheeseguardians, mercifullattice, keepout, safelycontained, Meow).
- **Test.**
  - `points` gain test at δ = 0.08, then tune at δ = 0.04.
  - Diagnostic: paired final D (Coop2), kings alive at r2000, bud success rate and rounds, KingCrowd ≥ 26 turns.
  - **Kill:** D does not rise; kill losses in r1000–1300 rise; more than 30% of buds abort.
- **Risks.**
  - A bud next to the king shares its cat lanes and throw lines, so the site must stay off lanes (DS§4.3).
  - Kings packed together can see more than 25 robots (S1 caps the sort).
  - King bytecode, with a maximum already near 20k: watch overruns and open RB if they exceed 0.5% of king turns.
  - Gauntlet: no vendored bot plays many kings; against GST and OBG the 7 rats spent at r800+ are affordable.
- **Size.** 150–300 lines.

#### CT. Trap the approach band
- **Change.** `moves/TryTrap.java.jinja2`:
  - Gate (:64-90): action ready, the nearest enemy baby rat at Chebyshev 2, and `spendable()` ≥ G_trap or raw ≥ 20.
  - Target: the tile between, `myLoc + dirTo(enemy)`, else ±45° (T2§2.4; O:MicroManager.java:100-136;
    G:Utilities.java:1620-1650).
  - Allow a second trap whose 3×3 covers the enemy's approach tile (spacing at Chebyshev 2 instead of the −100 at
    Chebyshev 1, :270-282).
  - Fix B2: `moveC` is empty (:357-361).
  - The pipeline order is unchanged: a safe grab still comes first.
  - Arms: G_trap ∈ {200, 400, 800}. A second arm adds traps on the enemy-facing side of contested mines
    (G:Utilities.java:1695).
- **Rests on.**
  - Traps fire 93–98% of the time against every bot (T2§2.4, T5§2.3).
  - OBG and GST out-trap awubot 1.2–2.7× in their wins, and awubot loses more rats in all 9 of OBG's wins (T5§4.2,
    §5.2).
  - 2.4 HP per cheese (T2§2.4).
  - Upper bound from T5§6: out-trapping alone did not win for the other bots.
- **Effect.** +3 to +8 pp, at the top of that range on maps with ≥ 8 mines.
  - **Flips:** COMBAT (15) and COMBAT+starve (6) through the exchange; most on the 21 maps with ≥ 8 mines.
- **Test.**
  - Tune gain test at δ = 0.04, with the G_trap dose, reported by mine class (≤ 4, 5–7, ≥ 8).
  - Diagnostic: traps per game; hits landed ÷ hits taken ≥ 1.4 (Deep `trapTrig`); death ratio; cheese floated.
  - **Kill:** hits landed/taken < 1.2, or harm on the ≤ 4-mine subset at δ_h = 0.05.
- **Risks.**
  - Cheese against spawning: GST's rate is about +60 traps, 1,200 cheese per game. Hence the `Economy` gate.
  - The cap of 25 traps.
  - In cooperation a fired trap blames us (R§2). Harmless against awubot.
  - Bytecode: TryTrap keeps its budget.
- **Size.** 40–120 template lines.

#### CW. Cat-aware, value-gated throws; hold and bite
- **Change.**
  - `moves/TryThrow.java.jinja2`:
    - a cat on the ray at step ≥ 2 is a feed, scored above every collision;
    - collision values 42 at step 2, then 30/26/22/18, the 6-tile schedule, a lower bound since turn order is
      unknown (T2§1.2);
    - dirt scores as a wall (B9).
  - `CombatStrategy.java.jinja2:216-231`: throw only if the best line is at least "wall at step 2", or at carry turn
    ≥ 8, or when the carrier is threatened or at HP ≤ 20. Otherwise hold and act with the free action: bite or trap
    (E:world/RobotControllerImpl.java:717-727).
  - `moves/TryPosition.java.jinja2:83-87`: a goal term, the best next-turn line ranked cat > enemy king > enemy
    cluster > wall (combat H2).
  - Arm: all 8 throw directions for moves {C, F} (combat H3).
- **Rests on.**
  - OBG against awubot: 3% open landings, 80% step-2 collisions, about 23 bites per game while carrying. awubot: 22%
    open landings, 95% of carries thrown at exactly t+2 (T2§2.3).
  - A held grab is worth +80–100 HP (T2§2.2).
  - Throws are 22–31% of losing kings' HP (T2§4.2, T4§2.1).
- **Effect.** +3 to +6 pp.
  - **Flips:** throw-heavy COMBAT maps (knifefight 512 HP, trapped, streetsofnewyork, hatefullattice), and feeds on
    cat maps.
- **Test.**
  - Tune gain test at δ = 0.04, then `cat` at δ = 0.05.
  - Diagnostic (CombatStats): open landings ≤ 8%; feeds per game; bites while carrying; mean hold; victims freed by
    our carrier's death.
  - **Kill:** freed victims outnumber the hits gained; THROW-turn overruns above 0.5%.
- **Risks.**
  - A carrier that dies frees its victim, so throw by turn 9 at the latest (T2§1.4).
  - Bytecode: THROW turns already reach 13.9k at p90 (combat§13).
  - Gauntlet: bots that grab the carrier rescue the victim (E:world/InternalRobot.java:685-700).
- **Size.** 120–250 template lines.

#### K1. King steps out of a cat's lane
- **Change.**
  - `robot/king/KingCombatStrategy.java.jinja2:102-170`: score cat threats by the cat's facing. Leaving its 2-wide
    forward lane perpendicular comes first, moving away second (T4§2.4). Trigger: the lane covers the king's 3×3
    within an ETA of ≤ 2·(moves needed)·4 rounds, or the king was scratched in the last 3 turns (T5 P1b).
  - Fix B3 (:116-117: the cells should be (−2,2) and (−1,2)).
  - When the king cannot leave the lane, it bites the cat (:180-195).
  - `KingMiningStrategy.java.jinja2:173-278`: no return to a mine while a cat seen in the last 20 rounds is within d²
    64 of it (arm: d² 36, T5 P1b). No walk toward the centre box once any mine is known (:53-76).
- **Rests on.** CAT class: keepout 1000 HP, dirtfulcat 420, dirtpassageway 300 (DS§1.1); 117 scratches on awubot's
  kings against 50 on the opponents' (T5 A12); 7 king deaths in 20 CATPATH games against OBG (T4§2.1).
- **Effect.** +1 to +4 pp on the official pool, +15 to +30 on `cat` (except pocket maps, which need K3).
  - **Flips:** CAT (3); the cat share of tiny, cheesefarm and minimaze; tournament CATPATH maps.
- **Test.**
  - `cat` gain test at δ = 0.10, then a harm test on tune at δ_h = 0.04.
  - Diagnostic: scratch HP on our kings (kcause) and king deaths during cat turns must fall.
  - **Kill:** scratch HP does not fall, or bite HP on the king rises (it flees into enemies).
- **Risks.** Less mining time; fleeing into enemies. Bytecode: kings are tight; the scoring is a few hundred
  bytecodes.
- **Size.** 60–150 lines.

#### CB. Minimal-cheese bites, cat-bite cheese, action theft
- **Change.**
  - `CombatStrategy.java.jinja2:440-443`: per-target damage. In order:
    - kill now, at (h − 11)² + 1 cheese;
    - flip HP order for my grab or an adjacent ally's next turn;
    - 13 against a 12-biter in a 1v1;
    - else 12.
  - TryAttack passes that damage to `hasThreat(m,f,target,dmg)`.
  - `BabyRat.java:245-250`:
    - cats get 1–2 cheese from t ≥ 1500 or in cooperation (DS§4.5);
    - enemy kings get extra damage once their HP is within 3 rounds of our adjacent biters at 12. Cheese left after
      the last enemy king dies is worth nothing (T2§4.1).
  - B6: `TryRatNap:55` uses `getBiteDamage`.
  - B10: `BabyRat.java:179-213` skips `tryTransferCheese` inside `rawMove`/`turn` when combat planned an action this
    turn.
- **Rests on.** T2§2.1 (race table); T2 P4; combat H4, B6, B10 (observed twice); T3§1.4, §4.3.
- **Effect.** +1 to +3 pp.
  - **Flips:** frontal duels on FACEOFF and COMBAT maps; the cat share on cheesebottles and wallsofparadis.
- **Test.**
  - `faceoff` gain test at δ = 0.05, then a harm test on tune.
  - Diagnostic: bites by damage level, one-shot kills, 1v1 duels won (CombatStats), cheese spent on bites.
  - **Kill:** duels won unchanged.
- **Risks.** +100–300 cheese per game, so the `Economy` gate. Bytecode: small.
- **Size.** 40–100 lines.

#### PT. Endgame points conversion
- **Change.**
  - `MinerStrategy.java:175-213`: from t ≥ 2000 − (distance to the nearest king + 20), any raw > 0 goes home. No
    exploration after r1800.
  - `CombatStrategy.java.jinja2:453-457`: from t ≥ T_hunt with no enemy seen for 50 rounds, rats with raw 0 hunt
    live cats. Prefer cats on the enemy's half or low enough that the enemy would finish them. Arms: T_hunt ∈ {1300,
    1600}.
  - Cat-bite cheese is CB's; the king's cat bite is K1's.
- **Rests on.** T3§3.2: cat bites flip cheesebottles and wallsofparadis, delivering raw cheese flips TheHeist; 2000
  raw held at the TheHeist mirror ending. T3 P2, P4.
- **Effect.** +2 to +4 pp alone; +0 to +2 after KL, which already flips most of the same maps.
  - **Flips:** 3–4 of the 11 POINTS maps.
- **Test.**
  - `points` gain test at δ = 0.08 on wins, plus the paired mean ΔD (Coop2).
  - **Kill:** paired mean ΔD ≤ 0.5 points, or late king-kill losses rise.
- **Risks.** Hunters away from the king (hence hunting only without enemy contact); pounces. Bytecode: trivial.
- **Size.** 40–100 lines.

### 4.2 Wave 2

#### E3. Territory: miners by deficit, exploration that ends
- **Change.**
  - The king writes payload role 1 (`KingSpawningStrategy.java:93`) with the mine whose deficit is largest:
    - target miners per mine n_mine(d, L) (T1§2.6);
    - over our own and the contested mines (within 4 path tiles of the equidistant line; T1 `MineMedian`);
    - never enemy-side mines.
  - `MinerStrategy.java:83-111` follows the assignment instead of the ⌊√d⌋ lottery.
  - Exploration (:70) stops once the symmetry and our half's mines are known.
  - Arm: near-home go-home thresholds 10 → 25 and 25 → 40 (T1 E5).
- **Rests on.** Harvest saturates at 86–93% of supply (T1§2.3, T5§2.1); OBG and GST hold more mines in 12 of 15 wins
  (T5§4.2, §5.2); awubot's lottery points 71% of its weight at enemy-half mines on keepout (T5 A6).
- **Effect.** +2 to +6 pp, on maps with ≥ 8 mines.
  - **Flips:** keepout, cheesefarm, hatefullattice, mercifullattice, safelycontained, cheeseguardians (COMBAT and
    COMBAT+starve).
- **Test.**
  - Tune gain test at δ = 0.04, by mine class.
  - Diagnostic: mines held (Deep), transferred share in r200–600, deaths of lone rats.
  - **Kill:** share unchanged.
- **Risks.** One mine overloaded; awubot-style raids lost. Bytecode: cheap.
- **Size.** 150–250 lines. Start any time after S1; merge after CT (territory is held by the exchange, T5§9).

#### KB. Early second king
- **Change.** KL's primitive with earlier triggers. Arms on the earliest round:
  - an opening bud from the first 7 spawns (150 cheese, about r10), only where kk_dirt > 20 (T4 `maps.tsv`);
  - r40, r100, r200;
  - r350, OBG's rule (T5 P7);
  - the champion's gate as the zero arm.

  Maps likely to end in cooperation (no enemy seen by r100): bud before r150 (T3 P3c).
- **Rests on.** T1§2.7: kings are cheapest early, and in 15% of kill games the winner's king ended below 300 HP. T3§4.2:
  2v1 is +10 in cooperation. T3§3.2: peaceinourtime was decided by a second king.
- **Effect.** +2 to +6 pp; the sign is uncertain because the opening loses presence (first contact r4–25).
  - **Flips:** king races in COMBAT games; cooperation endings.
- **Test.**
  - Gain test per arm on tune at δ = 0.04, then a harm test on `faceoff`.
  - Diagnostic: formation round; second king alive at r600 and r2000; deaths in r0–200; income share in r200–600.
  - **Kill:** every arm ends on H0, or harm on `faceoff`.
- **Size.** 60–150 lines on top of KL.

#### E2. Army from income and attrition
- **Change.** `KingSpawningStrategy.java`: spawn while n < n* = √((Î − 2K)/(2.5·δ̂)) (T1§2.5), or while
  `spendable()` − c(n) exceeds the next planned king's cost. Floor 8.
  - δ̂: deaths from slot 0's `ratCounter` minus the change in n (n from `getCurrentRatCost`), excluding rats consumed
    by formation (DS§8 item 8).
  - Step 0: a probe build logging global cheese and spending every 50 rounds settles how well Î and δ̂ can be
    estimated (DS§9).
- **Rests on.** T1§2.5, E3.
- **Effect.** +2 to +5 pp, on attrition maps (DefaultMedium, evileye, arrows, cheesefarm) and low-contact maps
  (minimaze, averystrangespace, wallsofparadis, ZeroDay).
- **Test.**
  - Tune gain test at δ = 0.04 and `starve` at δ = 0.08.
  - Diagnostic: idle bank, rats alive, deaths per rat-round.
  - **Kill:** idle bank and deaths unchanged.
- **Size.** 80–150 lines.

#### K2. Cat-lane memory and a safe king site
- **Change.**
  - A header-2 squeak "cat sighting" (cat id, location, facing, round) replaces the panic squeak.
  - Kings keep the last sighting per cat in slots 38–41.
  - Kings and `Pathfinder` mark each cat's 2-wide forward lane for about 10 tiles.
  - `KingMiningStrategy`: the site is the home-side mine that minimises delivery to our own mines (T1§2.6), with its
    3×3 off known lanes and a perpendicular escape (T4§3). Ties go toward the centre.
  - Step 0: port `GameWorld.bfsFromTarget` and compare BFS lanes with straight segments on the `cat` pool (T4§7).
- **Rests on.** T4 WP2 (components 1–4), T1 E6.
- **Effect.** +1 to +3 pp on the official pool; about +10 on `cat` over K1.
- **Test.**
  - `cat` gain test at δ = 0.08.
  - Diagnostic: king turns spent in a known lane; scratch HP on our kings.
- **Size.** 150–250 lines. Depends on K1.

#### K3. Rats defend the king from cats
- **Change.** New `CatDefenseStrategy`, entered when a lane hits our king within its ETA and the king cannot clear
  it: the pocket test of DS§8 item 2.
  - Dirt across the lane 4–6 tiles out: two placers out-pace one digging cat (T4§2.5).
  - Bites from outside the cat's 180° cone.
  - Grabbed enemies thrown into the cat (CW's scoring).
  - In cooperation, every rat hits a cat in our half, within PC's last-cat rule once PC exists.
  - The cat-gate radius at `CombatStrategy.java.jinja2:455` changes after PT's edit.
- **Rests on.** T4 WP3; the tminusten geometry (DS§8 item 2); OBG dealt 7,500–8,000 cat damage by r100 on tminusten
  (T4§2.1).
- **Effect.** tminusten, attack_on_cat and sunandmoon in both seats: +0 to +1 pp on the official pool, +2 to +4 on
  tune (many tournament maps).
- **Test.**
  - Gain test on the `cat` pocket subset at δ = 0.10.
  - Diagnostic: our king alive at r110 on tminusten; cat damage by r150.
- **Size.** 150–300 lines. Depends on K1, PT and CW.

#### CX. Complete threat model and trap avoidance
- **Change.** `CombatTracker.java.jinja2` and `getCellScoreFor` (`CombatStrategy:366-382`). Add:
  - bite-then-grab and pincer counts (T2 L3, L4);
  - enemies facing away that can turn and grab;
  - the cat's scratch cone and pounce reach;
  - suspected awubot trap tiles: when our rat is "strong" (100 HP, or ≥ 80 HP and ≥ the awubot rat's HP + 12) and
    faces an awubot rat at Chebyshev ≤ 2, mark the 3×3 of its front cells next to us for N rounds (T5 P4, A8).

  Graded penalties next to the binary −100.
- **Rests on.** T2 P5, T5 P4, combat H5.
- **Effect.** 0 to +5 pp; the sign is uncertain, because more caution gives up first bites.
- **Test.**
  - Tune gain test at δ = 0.04.
  - Diagnostic: our rats grabbed per game; awubot traps triggered by us ÷ awubot traps placed (98% today); standoff
    turns.
  - **Kill:** standoff turns rise while the win rate stays flat.
- **Risks.** `enemyMaskForMove` is already 32.6 KB of the JVM's 64 KB method limit (codegen.md).
- **Size.** 200–400 template lines. Depends on CT.

#### CC. Carrier discipline against enemy throws
- **Change.** While an ally is carried by an enemy:
  - do not end a turn on the carrier's step-2 tiles (its facing ±45°, after one move);
  - stand on its step-1 tiles, bite it, and grab it when legal, which drops our rat (E:world/InternalRobot.java:685-700);
  - the carried rat squeaks its carrier's position and facing (header 3).
- **Rests on.** T2§10 and P6: about 30 double hits per game, about 1,250 HP.
- **Effect.** +1 to +4 pp.
- **Test.**
  - Tune gain test at δ = 0.04.
  - Diagnostic: awubot's step-2 collision share against us; rescues per game.
- **Size.** 100–200 lines. Depends on CW.

#### CK. King assault and throw shield
- **Change.**
  - Carry victims toward an enemy king within about 6 tiles and throw from Chebyshev 3, outside its bite ring
    (T2§4.1). Occupy its ring.
  - Our king bites with cheese at kill or flip thresholds.
  - Never dig our own ring dirt while enemies are within 8 tiles; delete `digSurroundingDirt` first. Place dirt on
    ring tiles aligned with enemy carriers. Spawn on the far side (T4 WP4).
- **Rests on.** T2 P8, T4 WP4; throws plus residual are 31% of losing kings' HP (T4§2.1).
- **Effect.** +1 to +4 pp on `faceoff`.
- **Test.**
  - `faceoff` gain test at δ = 0.05.
  - Diagnostic: throw HP taken by our king; throw HP we deal to theirs.
- **Size.** 120–250 lines. Depends on CW and K1.

### 4.3 Wave 3 (opened by §6)

| id | package | content | source |
|---|---|---|---|
| PC | Cooperation-ending control | last-cat rule, delivery before the cat clock, cat HP taken from the enemy's share; points estimator in slot 15 | T3 P3, P8 |
| XA | awubot exploits | lure cats onto its king (cats target the last robot in scan order, squeaks steer them); pressure a king showing the starvation signature; deny its late formation (dirt on the 3×3, attack the gathering) | R§16.3-4; T4§2.6; T3 P5; opp§3 Gravy |
| RB | Reclaim king bytecode (an enabler, triggered by king overruns) | cache the 5 king locations per turn; incremental king cell scan instead of 3.4k per turn; rewrite the cheese list only on change | arch§8 |
| IS | Symmetry by mines, enemy-king candidates | resolves the 10 of 14 maps where walls cannot | T4 WP6 |
| CH | Choke play | traps and dirt at cut ≤ 2 when ahead on points | T4 WP7 |
| BL | Blame policy | no rat traps or first bite in cooperation; cat traps in the 100-round victim window | T3 P6 |
| QD | Quals combat diff | awubot_quals beats the final bot on microtest from both seats; TryThrow differs by 528 lines, TryTrap by 250 | combat H11 |
| JS | Joint combat search | one pass scoring nap, attack and fight per (move, facing): "move + nap + reposition", "throw then bite" | combat H12 |
| AB | Ablations | remove champion features one at a time: anti-glorp troll, panic squeak, eternal exploration, ally-sacrifice gate | lessons§9 |

## 5. Order to 90%

### 5.1 Loss model

From DS§1.1, 43 official maps in the mirror:

| class | maps | wave-1 owners (primary → residual) | wave-2 owners |
|---|---|---|---|
| POINTS | 11 | KL → PT, CB (cat cheese) | KB, PC |
| STARVE | 8 | E1 → CT, CW | E2 |
| COMBAT+starve | 6 | E1, CT, CW | E3, E2, CX |
| CAT | 3 | K1 | K2, K3 |
| COMBAT | 15 | CT, CW, CB | E3, CX, CC, CK, KB |
| self-inflicted (tournament: sieveofriches, tminusten; team-made overflow maps) | – | S1 (tminusten also needs K1 and K3) | K3 |

The game win rate is 0.5 + 0.5·(f₂₀ − f₀₂), where f₂₀ and f₀₂ are the shares of map pairs won 2–0 and lost 0–2.
**90% needs f₂₀ − f₀₂ ≥ 0.8**: 34–35 of 43 maps won in both seats.

To avoid double counting, each class's conversion p is credited to its primary owner. Residual owners act only on
what is left: p = 1 − (1 − p₁)(1 − p₂). f₀₂ = 0.03–0.06 covers losses from the RNG reshuffle and side effects (W0.1
replaces this guess).

| class (n) | p after wave 1 (low / mid / high) | p after wave 2 (needed for 90%) |
|---|---|---|
| POINTS (11) | 0.50 / 0.72 / 0.85 | 0.85 |
| STARVE (8) | 0.50 / 0.68 / 0.80 | 0.85 |
| COMBAT+starve (6) | 0.35 / 0.51 / 0.65 | 0.80 |
| CAT (3) | 0.30 / 0.55 / 0.70 | 0.85 |
| COMBAT (15) | 0.20 / 0.35 / 0.50 | **0.75–0.80** |
| f₂₀ | 0.36 / 0.54 / 0.68 | 0.81 |
| win rate (f₀₂ ≈ 0.045, then 0.02) | **66% / 75% / 82%** | **89–90%** |

Mid-column arithmetic: (11·0.72 + 8·0.68 + 6·0.51 + 3·0.55 + 15·0.35)/43 = 23.3/43 = 0.54.

What the p values rest on:
- POINTS: T3§3.2 (a third king flips 10 of 11) times the lock-in's formation reliability, assumed 0.6–0.8.
- STARVE: T4§2.2 (the side that starves more loses 4 of 5), discounted for maps where the king was also bitten.
- CAT: the share of scratch damage that comes before the king could step out.
- COMBAT: no measurement; DS§2.6's model.

The tune pool adds tournament maps rich in CATPATH, CHOKE and MINE-OVERFLOW (T4§3: 10 + 23 + 2 of 78). There S1,
K1–K3, KL and PC weigh more, and the COMBAT share is probably lower. W0.3 recomputes this table on the real tune pool.

### 5.2 Merge order and why

1. **S1.** Certain, cheap, and it edits the assert lines that CT, CW, CB and TryPosition's owners also touch.
2. **E1 and KL.** The two largest, best-founded effects. They are measured on class pools at δ 0.08–0.10, which is
   cheap. They act on different classes (STARVE against POINTS), so their effects nearly add.
3. **CT, then CW.** The largest measured combat asymmetry, then the largest unmeasured one. Same class (COMBAT);
   expect sub-additivity.
4. **K1.** Concentrated on `cat`, cheap to confirm there.
5. **CB and PT.** Small effects. Test them on class pools: their tune-pool δ would cost about 1,500 games.
6. **Wave 2,** each package as its lane frees up. Inside a class, merge the larger expected effect first:
   E3 before E2; CX, CC and CK after CT and CW.
7. **After each merge** the integrator re-measures main on tune, holdout and the gauntlet. The baseline then moves:
   later packages are judged against awubot with everything merged, so their measured gains shrink where classes
   overlap. That is expected and is not a reason to drop them.

### 5.3 Parallel lanes

All lanes start once S1 is on main; CW waits for S1 only because they share TryThrow.

| lane | sequence | can run in parallel with |
|---|---|---|
| safety | S1 | – (merge first) |
| economy | E1 → E2 | every other lane |
| kings | KL → KB | every other lane (payload schema §2.1) |
| traps | CT → CX | every other lane |
| throws | CW → CC → CK (CK also waits for K1) | every other lane |
| bites | CB | every other lane |
| points | PT → PC | every other lane; K3 waits for PT |
| cats | K1 → K2 → K3 | every other lane |
| territory | E3 | every other lane (payload schema §2.1) |

At most 8 lanes run in parallel in wave 1. Merges are always serial.

## 6. If a wave stalls

1. **Wave 1 below 66% on tune.** Read the per-merge re-measures for 0–2 pairs. Revert or ablate the merge that
   introduced them. Check each class's diagnostic: a mechanism that did not fire is a bug, not a dead idea.
2. **Wave 1 in its 66–82% range.** This is expected; wave 1 does not reach 90% by itself. Start wave 2.
3. **Wave 2 below 90%.** Recompute §5.1's table on the tune pool and open the class with the largest residual:
   - COMBAT: QD, then JS, then OBG micro components (`MicroManager`'s safety tiers that count numbers) as an
     alternative arm to CX.
   - POINTS or cooperation: PC, BL.
   - CHOKE: CH.
   - CAT: XA's lure.
   - Any class: XA's pressure on a crippled king, then AB.
4. **A package regresses one class while gaining overall.** Gate it on a map feature the bot can observe: known mine
   count (≤ 4 against ≥ 8, T5§8.6), kk from symmetry, cut. Never on map names or exact sizes (lessons§7).
5. **A lane with 3 consecutive H0s** is closed, with its diagnostics recorded in the closed-directions log. It
   reopens only with new evidence.
6. **The 90% verdict** needs the tune pool, the sealed holdout and the gauntlet, all on engine 1.2.6, both seats
   (program.md).
