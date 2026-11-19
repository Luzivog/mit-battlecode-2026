# Plan: work packages to 90% against awubot

This plan comes from track T (docs/program.md) and is the input to track B. Its theory is in
`docs/theory/dominant-strategy.md` (cited `DS§n`); the lens files are cited `T1§n` … `T5§n`. Other source keys follow
DS:
- `R§` rules.md; `E:` the engine at tag 1.2.6; `C:` `src/challenger/`; `A` marks arithmetic.
- `TG:` the evaluation tool (`tools/games`, track E); `critic/` the plan critic's scratch in
  `~/battlecode26-data/theory/critic/`.

"pp" means percentage points of win rate against awubot, under official conditions, both seats. Effects are given on
the `goal` pool (official + visible tournament maps, engine 1.2.6), weighted by W0.3's per-cell loss classes (DS§1.1).
A package that lifts our per-game win probability on a class holding a share w of the cells from 0.5 to s adds
w·(s − 0.5) to the goal score (§5).

## 0. Summary

- **Wave 0 is done** (§3; docs/evaluation.md "The tests", "Pools", "Calibration"). Two measurements change the plan:
  - **Chaos.** A change that only moves bytecode flips about 47% of cells, with no measurable net cost (W0.1). Most
    games are close: small perturbations turn them into coin flips. A single flipped cell means nothing, a test below
    δ = 0.05 on `goal` costs ≥ ~1,500 cells, and **90% needs games that are no longer close** (§5).
  - **A measured lever.** A rat cap 4 lower scored 0.86 of games on maps with ≤ 4 mines and 0.33 on maps with ≥ 8
    (quick, 74 cells; §4.1 E2). E2 moves into wave 1 with that arm, gated on the mine count.
- **Loss classes on `goal`** (W0.3: 285 cells, 3 seed passes, 1.2.6; DS§1.1): COMBAT 34%, POINTS 25%, STARVE 22%,
  CAT 12%, COMBAT+starve 7%, SELF 1%. Each class is a list pool of its cells (§1 item 5).
- **Gates.** S1 (never die to our own code) merges first; RB (king bytecode) before KL and K1.
- **Wave 1, in merge order:** S1, RB, E2, KL, CT, CW, E1, K1, CB, PT. Built in parallel on disjoint code (§2), merged
  one at a time, each rebased and re-tested on the current main (§1 item 6). Expected: **about 70% against awubot
  (range 59–78%)**.
- **Wave 2:** E3, KB, K2, K3, CX, CC+CK, and E2's later arms. Expected: **about 79% (range 67–90%)**.
- **90% needs a per-game win probability near 0.9 in every class**, the high column of §5: games we win whatever the
  noise. Packages are ranked by the size of their effect, not their number. Wave 3 is probably needed (§6).
- **Weakest assumption.** That the combat packages (CT, CW, CB, CX) lift the COMBAT cells (34% of `goal`) from coin
  flips to 0.8–0.9. Nothing measured supports it yet. If COMBAT stays near 0.65, wave 2 ends near 75%.

| rank | id | package | expected effect (pp on `goal`) | depends on | lane |
|---|---|---|---|---|---|
| 1 | S1 | Never die to our own code | certain on sieveofriches (its 3 SELF cells: +0.5) and the overflow team maps; elsewhere positive sign, size unmeasured | – | safety |
| 2 | RB | Reclaim king bytecode (enabler) | none by itself; frees king bytecode for KL and K1 | S1 | bytecode |
| 3 | E2 | Army size by mine count (arm 1: rat cap 4 lower on ≤ 4-mine maps), then from income and attrition | +4 to +8 (arm 1 measured on quick, extrapolated) | S1 | economy |
| 4 | KL | King lock-in: relax the existing coordinator's gates first, budding only if that fails | +2 to +6 | S1, RB | kings |
| 5 | CT | Trap the approach band | +3 to +8 | S1 | traps |
| 6 | CW | Cat-aware, value-gated throws; hold and bite | +3 to +6 | S1 | throws |
| 7 | E1 | One spend gate on every global spend | +1 to +3 | E2, KL, CT (it rewires their gates) | economy |
| 8 | K1 | King steps out of a cat's lane (B3 included) | +1 to +4 | S1, RB | cats |
| 9 | CB | Minimal-cheese bites (B6, B10 included) | +1 to +3 | S1 | bites |
| 10 | PT | Endgame conversion, including cat-bite cheese | +1 to +2 after KL | CB (`BabyRat.attack`) | points |
| 11 | E3 | Territory: miners by deficit, exploration that ends | +2 to +6 | S1, payload schema (§2.1) | territory |
| 12 | KB | Early second king (dose on the round) | +2 to +6, sign uncertain | KL | kings |
| 13 | K2 | Cat-lane memory and a safe king site | +1 to +3 | K1 | cats |
| 14 | K3 | Rats defend the king from cats | +0.5 to +2 | K1, PT, CW | cats |
| 15 | CX | Complete threat model and trap avoidance | 0 to +5, sign uncertain | CT | traps |
| 16 | CC+CK | Carrier discipline, king assault and throw shield (one bundle) | +2 to +6 | CW, K1 | throws |
| – | PC, XA, IS, CH, BL, QD, JS, AB | wave 3 (§4.3) | – | – | – |

## 1. How track B runs a package

1. **Setup.** One agent per package, on its own branch from main's `src/challenger`. Edit only the files the package
   names (§2.2), one change at a time. Templates and regeneration: docs/codegen.md. Run `./gradlew build -x test`
   before every commit (AGENTS.md).
2. **Engine.** Accept on engine 1.2.6, the official engine. The lens data is 1.2.5 and only sizes mechanisms; DS§10
   lists the numbers that move.
3. **Tests are paired against frozen main, not against 50%.**
   - Every gain test, harm test and gauntlet run plays the same cells (map, seed, seat) with `ours` = branch, `ref` =
     a frozen snapshot of main and `theirs` = awubot (W0.0).
   - The statistic is the paired score s = (1 + w_branch − w_main)/2 per cell game, a pentanomial GSPRT with
     α = β = 0.05. δ is the branch's edge over main in win rate against awubot.
     - **Gain test:** H0 = 0.5, H1 = 0.5 + δ/2.
     - **Harm test:** H1 = 0.5 − δ_h/2; it must end on H0.
   - Why: once main is above 50% against awubot, a branch built on it inherits that edge. A null package after S1 alone
     (+2.2 pp on `tune`) would pass a gain test against 50% with probability 0.57 at δ = 0.04 (A, Wald OC).
   - **Expected cells.** Any change that moves bytecode flips about 47% of cells (W0.1), so most cells split or flip
     whatever a package does. `tools/games simulate` at a 0.5 split (docs/evaluation.md "Measured", mean–p90 at the H0
     point): δ = 0.10 needs 67–131 cells, δ = 0.05 needs 261–545. Cells grow as 1/δ², so δ = 0.03 needs about
     720–1,500 (A).
   - **So the smallest δ worth running on `goal` is 0.05.** A package expected below +5 pp on `goal` is tested on its
     loss-class pool, where its effect is concentrated, at δ ≥ 0.08. It is accepted on that gain plus a harm test on
     `goal` at δ_h = 0.05 (item 7).
4. **The 90% metric** is challenger's win rate against awubot on the **`goal` pool**, both seats, over seed passes
   starting with pass 0 (the official seeds, TG `cell_seed`), on 1.2.6.
   - `goal` = the 43 official maps plus the 52 visible tournament maps (TG `--pool official,tournament`).
   - It must also hold on the sealed holdout at the end.
   - Team-made maps (TG `TEAM_MADE`, 25) are a separate robustness check: no significant regression. They are not
     part of the goal.
5. **Pools, in the order a package uses them.**
   - **First gain pool: the package's loss-class pool.** These are list pools of W0.3 cells (`map:seed`, engine 1.2.6;
     DS§1.1) in `~/battlecode26-data/games/pools/`:

     | pool | cells | maps | what |
     |---|---|---|---|
     | `L-combat` | 96 | 49 | COMBAT: bites and throws; starvation < 200 HP and < 25% |
     | `L-starve` | 81 | 45 | STARVE (62) and COMBAT+starve (19) |
     | `L-points` | 72 | 33 | POINTS: 62 reach r1000, 10 are cooperation endings before r1000 |
     | `L-late` | 101 | 45 | points cells that reach r1000 plus kills after r990: what a lock-in by r1195 can reach |
     | `L-cat` | 33 | 19 | CAT: scratches ≥ 300 HP or ≥ 40% of the losing kings' HP |
     | `MP-combat` | 31 | 17 | COMBAT cells on maps with ≤ 4 mines |
     | `L-self` | 3 | 1 | SELF: the losing kings removed by their own code (sieveofriches, the ≥ 26-mine assert) |
     | `mines-le4`, `mines-5to7`, `mines-ge8` | – | 25 / 19 / 51 | `goal` maps by mine count (T4 `maps.tsv`) |

     - A `map:seed` entry plays its W0.3 seed in pass 0, so pass 0 replays the cells where main lost that way; later
       passes play derived seeds (docs/evaluation.md "Seeds and cells").
     - Classes move with the seed and the engine. 55 of the 95 maps keep one class in all 3 cells, and 20 of the 43
       official pass-0 cells keep their 1.2.5 class. Re-classify main after each wave (§5.2).
     - The tool's feature pools are not loss classes, except where a mechanism follows the feature: E2's arm 1 follows
       the mine count, so it uses `mines-le4` and `mines-ge8`.
   - **Then one confirmatory gain test on `goal`, on a fresh seed base.**
   - **Holdout hygiene.** Every pool is built from TG `pools.visible()` only. The lens tables report results on some
     sealed maps, so holdout results are slightly optimistic. Never tune a constant on those games.
6. **Rebase and re-test.** Branches are built in parallel but merged one at a time. Before merging, a branch is rebased
   on the current main and at least the harm test on `goal` is rerun, so the merged combination is the tested one.
7. **Arms, certain-sign fixes and bundles.**
   - Choose an arm (dose) on the loss-class pool by its diagnostic and the paired score. Then run **one**
     confirmatory GSPRT for the chosen arm only: k arms at α = 0.05 give a false accept with probability
     1 − 0.95^k (0.34 at k = 8).
   - Certain-sign bug fixes (named per package; S1 and RB entirely) are accepted on "diagnostic moved as predicted +
     harm test on `goal` passed". A package may land them as its own first merge.
   - Small same-class changes ship as one bundle: PT with the cat-bite cheese; CC with CK.
   - Why: a δ = 0.04 gain test accepts a true +1 pp change with probability 0.19, +2 pp with 0.50, +3 pp with 0.81
     (A, Wald OC).
8. **Read the mechanism, not just the totals.** Every package names a diagnostic (§4). If it does not move, the result
   is noise whatever the totals say.
   - W0.1's background is about 47% of cells flipping under any bytecode change. Read class totals, flips clustered
     on a map or a class, and the diagnostic, never single cells.
   - Tools, under `~/battlecode26-data/theory/`, all of which read replays (W0.4):
     - `t3/Coop2.java`: final D, kings, cheese, cat damage, raw held, formation rounds.
     - `t4/kcause.py`, `t4/src/KingTrace.java`: king HP by source.
     - `tools/Deep.java`: deaths by cause, traps placed and triggered, mines held, starvation turns.
     - `t2/tools/CombatStats.java`: throw outcomes, feeds, bites while carrying.
     - `t1/tools/Timeline.java`: income, rats and kings per 10 rounds.
     - `critic/Starve.java`: global cheese per round and spend channels up to starvation.
     - `synthesis/KingCrowd.java`: king turns with ≥ 26 robots in view.

     Report deaths split by cause: combat, trap, collision, cat, formation (planned), disintegration, Error.
9. **Bytecode budget.**
   - Every package reports, per unit type, the mean and p99 bytecode delta against main, and overruns (the round number
     changes during a turn, deliberate yields excluded).
   - Merge gate: overruns ≤ main's + 0.1% of that unit's turns.
   - King budgets (king mean 13.2k, max 19,988 of 20k; arch§8): KL ≤ +1k mean on formation turns; K1 and K2 ≤ +0.5k
     mean on cat turns; any other king change ≤ +0.3k. RB must free more than these combined before KL and K1 merge.
10. **Kill and lane closing.** Each package has a kill criterion (§4). A lane closes after 3 consecutive H0s only if the
    diagnostics did not move either. Record every H0 with its diagnostics in `docs/closed.md` (lessons§9).
11. **Gauntlet.** Before merge, play every sparring bot paired against main on the same cells, OBG and GST first. No
    significant drop.
12. **Report.** Branch, head commit, the arm chosen and why, GSPRT results per pool, diagnostic values against main,
    flipped pairs with causes, and the bytecode figures of item 9.

## 2. Shared resources and file ownership

### 2.1 Interfaces fixed by this plan

**Spawn payload.** 13 bits, carried in GlobalArray slots 22–31 (arch§4.1). Today
`C:robot/king/KingSpawningStrategy.java:93` sends `Rng.mcg() & 8191`, and `C:robot/rat/BabyRat.java:26` discards it.
A newborn reads its payload on its first turn, still on its spawn tile (arch§4.1).

| bits 12–10 (role) | meaning | argument (bits 9–0) | owner |
|---|---|---|---|
| 0 | default: today's MinerStrategy | – | – |
| 1 | miner with an assigned mine | `encode2x2(mine)` (≤ 957) | E3 |
| 2 | bud recruit | site centre relative to the recruit's **spawn tile** (dx, dy ∈ [−5, 5]: 7 bits) plus spawn order (3 bits; order 0 takes the centre). Relative to the spawn tile, the code stays right if the king moves between `buildRat` and the payload write one round later | KL arm B, KB |
| 3 | cat defender | – | K3 |
| 4–7 | reserved | – | – |

Payload 0 keeps today's behaviour. Two stale cases to handle (S2, S3): the 2-slot spawn-info write can tear (write
both or neither), and a king's turn resumed after an overrun writes spawn info for a rat that already had its first
turn; if another king cleared the slots that round, the stale entry stays readable for a round. The first package to merge adds the role switch in `BabyRat.init` and an encoder
next to `GlobalArray.registerSpawnedRat` (`C:comms/GlobalArray.java:86-104`); later packages add their case. Dropping
the `Rng.mcg()` call shifts the king's random stream, which is expected (arch§3).

**Shared array.** Free today: slot 15, slot 1 bits 2–9, slot 9 bits 3–9 and slot 21 bits 5–9 (arch§4.1).

| space | owner | use |
|---|---|---|
| slot 0 (`COUNTER`) | S1 | written modulo 1024. Today `ratCounter` grows without bound (`C:comms/GlobalArray.java:108`), and from the 1024th spawn the write at `:225` throws, because the engine rejects values > 1023 (`E:world/RobotControllerImpl.java:1312`). Readers use it modulo 1024 (E2) |
| slots 38–41 (`MIN_CHEESE` 38 → 42; once S1 is in, the cheese list of 22 slots rotates beyond 22 mines) | K2 | the last sighting of up to 2 cats: `encode2x2` location (10 bits), facing (3), round mod 128 (7) |
| slot 15 | PC | points estimator |
| the rest | reserved | amend this plan first |

E2's siege flag (its siege-spawn arm) stays king-only: no slot carries it, and only kings spawn.

**Squeak headers** (arch§4.2):
- Header 2, the unused lure stub that receivers ignore (`C:comms/Squeaker.java:112-130`, default case): S1 moves the
  panic squeak here. Today it is `7<<29`, which is negative and decodes as two phantom enemies. K2 later replaces it
  with the cat sighting.
- Header 3: CC's carried-rat report.

**`util/Economy.java`** (owner E1): `reserve()` = max(100, 2·K·H), with K counted from the shared array's king slots,
so every unit can compute it; `spendable()` = global − `reserve()`.
- Until E1 merges, packages that spend global use today's gates: the spawn floor of 100
  (`KingSpawningStrategy.java:121` after E2), the formation gate of 150 (`KingFormationCoordinatorStrategy.java:187-188`), and
  bite cheese at total ≥ 300 (`CombatStrategy.java.jinja2:440-443`).
- E1 rewires every gate that exists in main when it merges. A package merging after E1 uses `spendable()`.

### 2.2 File ownership

Packages on the same row either edit disjoint line ranges (parallel is fine; the integrator merges textually) or are
marked → (sequence: the later one starts from main after the earlier merged).

| file (`src/challenger/`) | packages and regions |
|---|---|
| `RobotPlayer.java` | S1 (:84) |
| `RobotTracker.java.jinja2` | S1 (:46-49, :65-140) |
| `CellTracker.java.jinja2` | RB (`storeVisibleCellsKing` :260) · IS |
| `comms/GlobalArray.java` | S1 (:108, :225 counter; :413-429 overflow) · RB (`getRatKing` cache) · KL or E3 (payload encoder near :86-104) · K2 (`MIN_CHEESE` :61, cat slots) · PC (slot 15) |
| `comms/Squeaker.java` | S1 (:215-217 panic header) → K2 (header 2) · CC (header 3) · E3 (:91-98, mine squeak decoder: it reads only the first of the 3 mines `squeakMine` packs) |
| `comms/EnemyManager.java.jinja2` | S1 (`trySqueakKing` :73-87) |
| `util/Economy.java` (new) | E1 |
| `robot/rat/BabyRat.java` | S1 (:34-77) · CB (:179-213 `rawMove`/`turn`; :240-250 `attack`) → PT (cat case in the damage function) · E1 (:222 dig gate) · KL arm B / E3 / K3 (`init` :25-32, role switch) |
| `robot/rat/MinerStrategy.java` | E3 (:60-173 targets, exploration) · PT (:175-213 `shouldGoHome`) |
| `robot/rat/FormationStrategy.java` | KL (arm A: gates; arm B: deleted) |
| new `robot/rat/BudStrategy.java`, `robot/king/KingBudStrategy.java` | KL arm B → KB |
| new `robot/rat/CatDefenseStrategy.java` | K3 |
| `robot/king/RatKingStrategy.java` | KL arm B (:24-29) |
| `robot/king/KingSpawningStrategy.java` | S1 (turn) · E2 (:22-77, merged) → E1 (:121 gate) · E3 (payload write) |
| `robot/king/KingFormationCoordinatorStrategy.java` | KL (:141-151 aborts, :181-204 gates, :241 floor) → E1 (:187-188 cheese gate) |
| `robot/king/RatKing.java` | E1 (king digs :58, :68, :159, :224) |
| `robot/king/KingMiningStrategy.java.jinja2` | K1 → K2 |
| `robot/king/KingCombatStrategy.java.jinja2` | K1 (:72 dig gate is E1's) → CK |
| `Pathfinder.java.jinja2` | K2 (cat-lane penalty, :678-725) |
| `robot/rat/combat/CombatStrategy.java.jinja2` | CW (:216-231) · CB (:440-443, :484-488) → PT · PT (:453-457 cat gate) → K3 (:455) · CX (:366-382) |
| `robot/rat/combat/CombatTracker.java.jinja2` | CX |
| `moves/TryThrow.java.jinja2` | S1 → CW → CC+CK |
| `moves/TryTrap.java.jinja2` | S1 (assert lines) → CT → E1 (gate line :64-76) |
| `moves/TryAttack.java.jinja2`, `moves/TryRatNap.java.jinja2` | S1 → CB |
| `moves/TryPosition.java.jinja2` | S1 → CW → CC+CK |
| `moves/TryFight.java.jinja2` | S1 → CX, CC+CK |

Hidden coupling: CW's carrying branch calls `TryTrap` and `TryAttack` unchanged, as black boxes. Their behaviour
changes when CT or CB merge, so CW re-tests after rebasing (§1 item 6).

## 3. Wave 0: done

- **W0.0 Paired tests.** `tools/games ab challenger awubot` is a gain test against main by default, `--test harm` a
  harm test, `--ref none` plain mode for progress rows; `tools/games gauntlet` is paired with main's games
  (docs/evaluation.md "The tests").
- **W0.1 Null calibration** (docs/evaluation.md "Calibration", engine 1.2.6, plain mode against awubot):
  - RNG only (discarded spawn payload set to 0), `goal`: 4 / 276 / 5 cells won both / split / lost both: 3.2% flips.
  - Dead loop of about 235 bytecodes a rat turn, `goal`: 60 / 152 / 73: 46.7% flips, net −4.6% (p = 0.30).
  - About 150 bytecodes a turn for every robot, `full` pass 0: 39.2% flips, net +4.2% (p = 0.56).

  So r ≈ 0.47 for any change that moves bytecode, and no net bytecode cost is measurable. Consequences: §1 item 3,
  §5, DS§2.6.
- **W0.2 Pools.** `goal`, `teammade`, the feature pools, `points` (visible maps only) and list pools
  (docs/evaluation.md "Pools").
- **W0.3 Baseline loss table.**
  - The run: main (= awubot) against awubot on `goal`, 3 passes, 285 cells, every cell split, replays kept
    (docs/evaluation.md "W0.3 data": run `1118-222544-ab-6dea`).
  - Every cell is classified (`synthesis/w03_classify.py` → `synthesis/w03_cells.tsv`; DS§1.1), and the list pools
    of §1 item 5 are built from them.
  - The spend channels of the STARVE cells are ranked (`critic/Starve.java` → `synthesis/w03_starve.txt`; §4.1 E1).
  - Re-run it on the merged main after each wave: a plain run on `goal` with `--replays all`, then
    `w03_classify.py RUN_DIR` (§5.2).
- **W0.4 Replays.** `tools/games replays ID --cells changed` replays a run's games after the fact, deterministically;
  test runs stay replay-free (docs/evaluation.md "Protocol").

## 4. Packages

Each entry lists what the package changes, what it rests on, its effect, its test, its risks and its size. "Flips"
names the classes and maps of DS§1.1 where the package's mechanism decided the losing seat. Bytecode today (arch§8):
rats average 11.3k of 17.5k; kings 13.2k of 20k, with a maximum of 19,988.

### 4.1 Wave 1

#### S1. Never die to our own code
- **Change.**
  1. `C:comms/GlobalArray.java:427-428`: delete `else assert(false)`. When the list is full, `readCheeseMines`
     (:403-410) stops at slot 38 without a terminator, and the queue rotation (:421-423) cycles the rest.
  2. `moves/TryThrow.java.jinja2`: mark every visible cat's 2×2 as occupied for the step-1 legality checks
     (:258-293, :305-315, :410-417). Replace `assert(rc.canThrowRat())` (:101, :106) by a check that skips the throw.
  3. Every `assert(CombatStrategy.performMoves(...))` becomes `if (!...)`, keeping the call:
     - TryThrow :103, :105;
     - TryAttack :67, :71;
     - TryTrap :99, :103;
     - TryRatNap :95, and `assert(false)` at :82;
     - TryPosition :39.

     `assert(false)` in TryFight :55 becomes `return false`.
  4. `RobotTracker.java.jinja2`: keep the nearest 25 robots. With ≥ 26 in view the arrays of 25 throw (the king loses
     its whole turn), and with ≥ 44 the template asserts (:106, :137).
  5. `robot/rat/BabyRat.java:42,68`: null-check `senseRobotAtLocation`. Today an NPE loses the turn in front of a cat.
  6. `RobotPlayer.java:84`: `catch (Throwable t)`. Safe: the engine's kill signal extends `VirtualMachineError`, which
     every handler re-throws first (DS§8 item 1).
  7. The panic squeak (`Squeaker.java:215-217`) goes on header 2, which receivers ignore. Today `7<<29` is negative and
     decodes as two 24-HP phantom enemies at the sender (−4,−4) and (+3,−3) (`Squeaker.java:91-105`,
     `EnemyManager.decodeEnemy`). The squeak itself, and its effect on cats, are unchanged; K2 decides about it.
  8. `EnemyManager.java.jinja2:73-87` (`trySqueakKing`):
     - shift by the number already packed, not the loop index;
     - pad with `NULL` only in the unused half;
     - return false when no enemy is within ±4. Today that case squeaks `data = 0`: phantoms at (−4,−4).
  9. `KingSpawningStrategy.java:90`: the direct `rc.turn(...)` becomes `RatKing.turn` (rule 5), guarded by `canTurn`
     and a non-CENTER direction. Today it throws when the turn cooldown is not ready or the direction is CENTER, and
     that GameActionException costs the king the rest of its turn (combat§8). The rat is still built when it cannot
     turn.
  10. `GlobalArray.java:108/225`: the counter is written modulo 1024 (§2.1).

  Not in S1: cats as throw targets (CW), and a king in a cat lane (K1, K3).
- **Rests on.**
  - T5 A15: sieveofriches kings removed at r457–597 while leading; arch§1.6: bridges at r401, quadrants at r764.
  - T2§9.1: the TryThrow assert fired in 15 of 70 games; in all 5 instrumented failures a cat stood on step 1.
  - S:kingcrowd_mirror.txt: 11 king turns in 86 games with ≥ 26 robots in view.
  - Items 7–10: the code and engine lines cited, checked.
- **Effect.**
  - Certain: +1 pair on every ≥ 26-mine map where awubot learns 26 mines. That is sieveofriches in the goal pool;
    bridges, bridgesCatless and quadrants in the robustness set; tminusten too, once K3 keeps our king alive.
  - Elsewhere the sign is positive and the size unmeasured. The TryThrow deaths are about 1 in 400 throws. Items 5 and
    7–9 change behaviour (cat traps restored, no phantom fights, the king keeps its turn), all in our favour.
- **Test.**
  - Gain test on `overflow` at δ = 0.15: expect 2–0 on the overflow maps that end after awubot learns 26 mines.
  - Then the certain-sign rule (§1 item 7) on `goal`.
  - Diagnostics:
    - no `AssertionError` or "froze" lines in the logs;
    - our no-cause deaths are disintegrations only (Deep `diedNoCause`);
    - no thrower dies on a cat tile;
    - phantom-enemy squeaks received = 0;
    - king exceptions per game = 0.
  - **Kill:** none. A failing harm test means a bug: bisect items 3–10.
- **Risks.** Bytecode under 50 per turn.
- **Size.** 60–120 template lines.

#### RB. Reclaim king bytecode (enabler)
- **Change.** Two items from arch§8, no logic change:
  1. Cache the 5 king locations once per turn inside `GlobalArray` (`getRatKing` costs 20+ per call, called about 5×
     in many loops).
  2. Make `CellTracker.storeVisibleCellsKing` (about 3.4k per king turn) incremental: rescan only the cells whose
     content can have changed since the last scan.
- **Rests on.** arch§8 (king perception is about a quarter of the king's budget; king max 19,988; 26% of king turns
  above 15k on bridges); critic finding 11 (S1, KL and K1 all add king code).
- **Effect.** None by itself; budget for KL, K1, K2 and KB (§1 item 9).
- **Test.** Certain-sign rule: king mean bytecode drops by ≥ 2k on `goal`, and the harm test passes.
  - **Kill:** a measurable behaviour change, e.g. missed dirt or cheese updates (diff the king's map memory against a
    full scan on 20 sampled turns).
- **Size.** 60–150 lines.

#### E2. Army size by mine count, then from income and attrition
- **Arm 1 (measured; first).** Rat cap 4 lower (spawn threshold 40 → 30, and 30 → 20 on small maps;
  `C:robot/king/KingSpawningStrategy.java:21-32`) on maps with ≤ 4 mines; unchanged elsewhere. Gate arms: ≤ 4 and ≤ 7
  mines.
  - **Measured** (docs/evaluation.md "Calibration", run `1118-212307-ab-71d3`, `quick`, 74 cells, cap lowered on
    every map): 30 cells won both, 30 split, 14 lost both; score 0.608. By map, 10 games each (8 on three):

    | mines | maps (score) | games won |
    |---|---|---|
    | ≤ 4 | evileye 0.9, whereisthecheese 1.0, tiny 1.0, DefaultSmall 0.6, arrows 1.0, whatsthecatdoin 0.8, jail 0.7 | 60/70 = 0.86 |
    | 5–7 | knifefight 0.6, DefaultMedium 0.7 | 13/20 = 0.65 |
    | ≥ 8 | cheesefarm 0.12, sittingducks 0.25, pipes 0.5, TheHeist 0.1, keepout 0.5, tminusten 0.5, DefaultLarge 0.3 | 21/64 = 0.33 |

  - **Mines, not area.** cheesefarm and DefaultSmall are both 900 tiles: cheesefarm (10 mines) scored 0.12,
    DefaultSmall (4) 0.6. The champion's own small-map rule uses area (`:24-25`). T1§2.5 predicts the split: the army a
    team can sustain grows with income, so a fixed cap over-builds where income is low and trades are fast, and
    under-builds where income is high.
  - **The gate must be observable.** Use the known mine count, doubled once the symmetry is known (every mine's mirror
    is a mine), and the champion's cap until the estimate settles. Step 0: a probe logging the estimate and the true
    count per round on `mines-le4` and `mines-ge8`, to find when it settles.
- **Merged: arm 1a** (2026-11-19, 07da7d5). The cap is 4 lower while the map is ≤ 1600 tiles and not known to hold
  ≥ 5 mines: every map starts poor and lifts once the estimate (known mines plus their unknown mirrors, the smallest
  over the symmetries still possible) reaches 5. Waiting for the estimate to settle would miss the gain: the bank gap
  builds from r1 and poor-map games end around r150–400. Above 1600 tiles the low cap gained ~0 and rich maps took the
  leak. `goal` gain +0.061 [+0.015, +0.109] vs main (1119-013819-ab-c574); −8 was far worse (−0.455 vs 1a on
  `mines-le4`); +4 on ≥ 10 mines did nothing. Open: the mine squeak decoder reads 1 of 3 mines (E3 fixes it), which
  slows the estimate; rich maps need arm 2's income estimate, not mine counts.
- **Arm 1b.** On ≥ 8-mine maps the opposite sign: a higher cap, dose {+4, +8}. Basis: arm 1 lost those maps, and T1§2.5
  puts the sustainable army at 20–44 rats on low-contact maps.
- **Arm 2.** n* budget: spawn while n < n* = √((Î − 2K)/(2.5·δ̂)) (T1§2.5), or while `spendable()` − c(n) exceeds the
  next planned king's cost; floor 8.
  - δ̂ = deaths per rat-round, from the change in the modular `ratCounter` minus the change in n. n is known only as
    4·⌊n/4⌋ from `getCurrentRatCost`, so over 50 rounds δ̂ carries ±4 rats of quantisation, about the size of the
    signal: use a 200-round window, or n squeaked by rats.
  - Rats consumed by formation are excluded (DS§8 item 8).
  - Step 0: a probe build logs global cheese and spending every 50 rounds, to see how well Î and δ̂ can be estimated.
- **Arm 3.** Siege spawns: a dose on the spawn cap while an enemy is in the king's vision, {global/10, global/5,
  global/3}, in place of OBG's imported constant (global/5).
- **Effect.** Arm 1: maps with ≤ 4 mines hold 26% of `goal`'s cells (75 of 285; `mines-le4`). At the measured 0.86,
  discounted toward 0.75–0.8 for 10 games a map and for gate errors, that is +4 to +8 pp (A).
  - **Flips:** the ≤ 4-mine cells: 25 STARVE, 31 COMBAT, 8 COMBAT+starve, 5 CAT, 6 POINTS (W0.3).
- **Test.**
  - Arm 1: gain test on `mines-le4` at δ = 0.10, harm test on `mines-ge8` at δ_h = 0.05, then the confirmatory gain
    test on `goal` at δ = 0.05.
  - Arms 1b–3: chosen on `mines-ge8` and `L-starve`, each confirmed on `goal`.
  - Diagnostics: rats alive, global cheese at r200 and r500, starvation turns, per-map scores against the calibration
    run.
  - **Kill:** harm on `mines-ge8` (the gate leaks), or no gain on `mines-le4`.
- **Risks.** Fewer rats on small maps is the direction food's postmortem points to (intel§4: more rats lost them small
  maps). Gauntlet: mass spawners (SPAARK) on small maps.
- **Size.** Arm 1: 10–40 lines (gate and estimate). Arms 2–3: 80–150.

#### KL. King lock-in: ≥ 3 kings before r1201, refill after
- **Arm A (first; rule 7: simplify before adding).** T1 E1: reuse the existing coordinator with the endgame branch
  moved earlier. In rounds [T_lock, 1195], with kings < K_target:
  - skip `MINE_THRESHOLDS` and the 20·K rat gate (keep `numRats ≥ 12`, as the t > 1800 branch does);
  - drop the score floor that demands a mine ≥ 20 tiles from every king (`:241`);
  - relax the `numRats < req·K` abort to ≥ 12 rats;
  - move the r1100 abort to 1195 (`:145`).

  Sources: `C:robot/king/KingFormationCoordinatorStrategy.java:13,141-151,181-204,241`.
  - After r1200 with 1 king, the same relaxed gates apply at once (R§6).
  - Arms: T_lock ∈ {800, 1000}; K_target ∈ {3, 5}.
  - Why it should work there: formations complete when the bot is "rich or uncontested" (strategy§6.3), and the r1100
    points seats hold 786–2122 global (T3§3.5).
  - Cat-lane exclusion: none in arm A; the coordinator's cat abort is dead code (strategy§3), and K2 adds lanes later.
- **Merged: arm A at T_lock 800, K_target 3** (2026-11-19, 6349ecd), with three measured additions: a king counts as
  gone only after two rounds missing (an overrun king misses one liveness write; S3 fixed the root in `GlobalArray`
  and removed this filter); lock-in formations before r1200 need
  1000 global cheese; and they keep the known-mine thresholds (lock-in lost 15 net games of 62 on ≤ 4-mine maps). `L-late`
  gain +0.055 [+0.008, +0.106] vs main (1119-024054-ab-925a); `goal` harm passed (1119-032401-ab-0db4). Two
  confirmatory tests were run, so the false-accept chance is about 0.10. Seats with ≥ 3 kings at r1200: 47 of 293
  (main 9); a single king at r1500: 42 of 252 (main 120). Expected on `goal`: +1 to +2 pp, below the estimate.
  K_target 5 scored −0.05 to −0.06. **Starvation is the limit**, not completion: forming beside our own king
  completed 77% of attempts and scored −0.140, because kings sharing a mine starve. Arm B is not built as specified.
- **Arm B (only if arm A's formation-success diagnostic fails, < 70% of attempts completing).** Budding (DS§4.3):
  - new `KingBudStrategy` and `BudStrategy`, payload role 2 (§2.1);
  - the old coordinator and `FormationStrategy` are deleted (rule 7), with the r1800+ endgame behaviour reproduced
    by the bud;
  - recruits spawn without copying `trySpawn`'s direct `rc.turn` (rule 5);
  - the site avoids the king's own recent cat sightings (r² 25, 360°), until K2's shared lanes replace them.
- **Rests on.**
  - W0.3 (`synthesis/w03_cells.tsv` and the run's final totals): 72 POINTS cells (25% of `goal`). 62 reach r1000; 10
    are cooperation endings before r1000 (KB's, not KL's). Kings at the end, winner against loser: 2v2 in 35 cells,
    2v1 in 23, 1v1 in 7, 3v1 and 3v2 in 3 each, 2v3 in 1.
  - Recomputing the engine's points with more kings for the loser (A, from each game's final cat damage, cheese
    transferred and kings): one more king flips 29 of the 62 reachable cells, two more flip 40.
  - Points margins on `goal` are wider than on the official 1.2.5 maps: median 15 points, 15% within 2 (T3§3.2 had 6
    of 11 under 2).
  - T3§3.5: at r1100, half the points seats have 1 king, so a third king needs two formations. T1 E1; R§6.
- **Effect.** +2 to +6 pp: about 0.7 of 29–40 cells moved to ≈ 0.85 per game is +2.5 to +3.4 pp (A), plus part of the
  39 kill cells that end after r1000.
  - **Flips:** `L-late`.
- **Test.**
  - Arm and T_lock chosen on `L-late` (§1 item 7), then the confirmatory gain test on `goal` at δ = 0.04.
  - Diagnostics: formation attempts and completions in [T_lock, 1195]; kings at r1200 and r2000; paired final D
    (Coop2); KingCrowd ≥ 26 turns; king bytecode against its budget (§1 item 9).
  - **Kill:** D does not rise; kill losses in r1000–1300 rise.
- **Risks.**
  - The coordinator recruits every rat within 25 tiles (strategy§3): the army thins around r1000. Formations in that
    window are one at a time with a 120-round timeout, so two take time; T_lock 800 leaves room.
  - Cheese: KL runs on today's gate (global ≥ 150). E1, merging later, rewires it, and E1's paired test against a
    main that holds KL measures E1 × KL (§1 items 3 and 6).
- **Size.** Arm A: 30–80 lines. Arm B: 150–300 lines, minus the deleted code.

#### CT. Trap the approach band
- **Change.** `moves/TryTrap.java.jinja2`:
  - Gate (:64-90): action ready, the nearest enemy baby rat at Chebyshev 2, and (global ≥ G_trap or raw ≥ 20).
  - Target: the tile between, `myLoc + dirTo(enemy)`, else ±45° (T2§2.4; O:MicroManager.java:100-136;
    G:Utilities.java:1620-1650).
  - Allow a second trap whose 3×3 covers the enemy's approach tile: spacing at Chebyshev 2 instead of the −100 at
    Chebyshev 1 (:270-282).
  - Certain-sign first merge: fix B2, the empty `moveC` (:357-361).
  - Arms: G_trap ∈ {200, 400, 800}; a second arm traps the enemy-facing side of contested mines
    (G:Utilities.java:1695).
- **Rests on.**
  - Traps fire 93–98% of the time against every bot (T2§2.4, T5§2.3).
  - OBG and GST out-trap awubot 1.2–2.7× in their wins, and awubot loses more rats in all 9 of OBG's wins (T5§4.2,
    §5.2).
  - A trap is worth about 2.4 HP per cheese (T2§2.4).
  - Bound: out-trapping alone did not win for SPAARK, powerpuff or food (T5§6).
- **Effect.** +3 to +8 pp, most on maps with ≥ 8 mines.
  - **Flips:** COMBAT and COMBAT+starve through the exchange.
- **Test.**
  - Arm chosen on `L-combat` plus the COMBAT+starve maps, then the confirmatory gain test on `goal` at δ = 0.04, reported
    by mine class (≤ 4, 5–7, ≥ 8).
  - Diagnostics: traps per game; hits landed ÷ hits taken ≥ 1.4 (Deep `trapTrig`); death ratio; cheese floated.
  - **Kill:** hits landed/taken < 1.2, or harm on `MP-combat` (δ_h = 0.05).
- **Risks.**
  - Cheese against spawning: E1 later puts traps under the shared gate.
  - The cap of 25 traps.
  - In cooperation a fired trap blames us (R§2), which is harmless against awubot.
  - Bytecode: TryTrap keeps its budget.
- **Size.** 40–120 template lines.
- **Merged** (2026-11-19, 79cc92f): B2 (staying scores all 8 facings), the band trap (+20 on the tile between, +10 at
  ±45°, raw ≥ 20 or global ≥ 400) gated on ≥ 5 known mines (`util/MineEstimate`), and S2's two lost-turn sites.
  `goal` gain +0.119 [+0.045, +0.188] (1119-033639-ab-2d84); on top of CW +0.116 [+0.050, +0.183]
  (1119-052337-ab-fe57). Hits landed/taken 1.57 (main 1.04). Ungated, the trap won ≥ 8-mine maps (+0.11 to +0.17)
  and lost ≤ 4-mine maps (−0.18 to −0.19) by starving the king: global cheese starts at 2500 everywhere, so only the
  mine count separates them. Any later trap spender keeps the mine gate. `MineEstimate` duplicates E2's estimator
  (E1 removes the copy).

#### CW. Cat-aware, value-gated throws; hold and bite
- **Change.**
  - `moves/TryThrow.java.jinja2`:
    - a cat on the ray at step ≥ 2 is a feed, scored above every collision;
    - collision values 42 at step 2, then 30/26/22/18, the 6-tile schedule, a lower bound because turn order is
      unknown (T2§1.2);
    - dirt scores as a wall (B9).
  - `CombatStrategy.java.jinja2:216-231`:
    - throw only if the best line is at least "wall at step 2", or at carry turn ≥ 8, or when the carrier is threatened
      or at HP ≤ 20;
    - otherwise hold and act with the free action by calling `TryAttack` and `TryTrap` unchanged
      (E:world/RobotControllerImpl.java:717-727).
  - `moves/TryPosition.java.jinja2:83-87`: a goal term, the best next-turn line ranked cat > enemy king > enemy
    cluster > wall (combat H2).
  - Arm: all 8 throw directions for moves {C, F} (combat H3).
- **Rests on.**
  - OBG against awubot: 3% open landings, 80% step-2 collisions, about 23 bites per game while carrying. awubot: 22% open
    landings, 95% of carries thrown at exactly t+2 (T2§2.3).
  - A held grab is worth +80–100 HP (T2§2.2).
  - Throws are 22–31% of losing kings' HP (T2§4.2, T4§2.1).
- **Effect.** +3 to +6 pp.
  - **Flips:** throw-heavy COMBAT maps (knifefight 512 HP, trapped, streetsofnewyork, hatefullattice), and feeds on cat
    maps.
- **Test.**
  - Arm chosen on `L-combat`, then the confirmatory gain test on `goal` at δ = 0.04.
  - Diagnostics (CombatStats): open landings ≤ 8%; feeds per game; bites while carrying; mean hold; victims freed by
    our carrier's death.
  - **Kill:** freed victims outnumber the hits gained; THROW-turn overruns above the gate.
- **Risks.**
  - A carrier that dies frees its victim, so throw by turn 9 at the latest (T2§1.4).
  - THROW turns already reach 13.9k at p90 (combat§13).
  - Gauntlet: bots that grab the carrier rescue the victim (E:world/InternalRobot.java:685-700).
- **Size.** 120–250 template lines.
- **Merged** (2026-11-19, af60d68): arm C4, all four changes (damage-scored lines, hold unless step-2 or better,
  aim while holding, 8 directions). `goal` gain +0.112 [+0.050, +0.174] vs main (1119-035720-ab-1be9); merged on top
  of KL, +0.139 [+0.061, +0.219] (1119-043231-ab-22e2). Open landings 20.6% → 5.6%, step-2 collisions 59.5% → 83.1%.
  Holding barely adds bites (2.9 a game, OBG ~23): the gain is the throws. Holding exposes the carrier's blind side
  (grabs from outside its cone 1,011 → 1,243): CC's lead.

#### E1. One spend gate on every global spend
- **Closed** (2026-11-19; `docs/closed.md`). H ∈ {30, 60, 120} all lost on `L-starve` (−0.037, −0.012, −0.078). Its
  premise was wrong: the spawn cap is already a cost-scaled reserve (a cost-c rat needs global ≥ 10·(c − 10)), so a
  floor only stops the army rebuilding. Remaining starvation follows a lost income share; income, not floors, is the
  lever. Merged: its exact refactor, `KingSpawningStrategy` reads `util/MineEstimate` (every game equal to main's,
  1119-070620-ab-2f2e). There is no `spendable()`; later packages keep today's gates.
- **Rests on.** `critic/Starve.java` on W0.3's 62 STARVE cells (1.2.6; `synthesis/w03_starve.txt`).
  - From r1 to starvation, the losing seat spent a median of 2,180 on spawns (52% of its spending), up to 900 on rat
    traps (≤ 22%: traps pay from raw cheese first), 820 on upkeep (18%) and 178 on digs (4%).
  - From the first round below 100 global to the first below 2 (global recovers above 100 inside that window):
    spawns of cost > 10 took 17,820 over all 62 cells, traps up to 15,840, upkeep 23,238; the `cost == 10` exception
    took 1,440 and digs 960.
  - Today's spawn floor is 100 (`KingSpawningStrategy.java:121` after E2), but rats spend global with no floor: traps
    (`TryTrap.java.jinja2:64-76`), digs (`BabyRat.java:222`), king digs (`RatKing.java:58,68,159,224`;
    `KingCombatStrategy.java.jinja2:72`).
  - The winner also starved in 12 of the 62 cells (19%). A reserve can flip those. Elsewhere the loser starved after it
    lost the exchange and the share (T1§2.8), which CT, CW, E2 and E3 address.
- **Change.**
  - New `util/Economy.java` (§2.1): `reserve()` = max(100, 2K·H).
  - Every global spend checks `spendable()`: king spawns (`:121` after E2), traps when raw cheese does not cover them (after CT,
    `TryTrap.java.jinja2:64-76`), rat and king digs, bite cheese (`getBiteDamage` or CB's function), and formation
    (`KingFormationCoordinatorStrategy.java:187-188`).
  - The `cost == 10` exception stays: it spent 0–50 per loss (Starve.java) and is the only way to rebuild income with
    ≤ 3 rats (T1§2.8).
  - No siege waiver below 100.
  - Arms: H ∈ {30, 60, 120} (T1 proposed 30–60). The measured channels make the spawn and trap gates the two that
    matter.
- **Effect.** +1 to +3 pp.
  - **Flips:** the 12 STARVE cells where both kings starved (in `L-starve`), and later starvation elsewhere. It also
    protects E2's, KL's and CT's spending.
- **Test.**
  - Arm chosen on `L-starve`, then the confirmatory gain test on `goal` at δ = 0.04; harm test on `MP-combat` at
    δ_h = 0.05.
  - Diagnostics: our king's starvation turns (kcause, Deep) and the round global first falls below 2 (Starve.java),
    against main's; spend by channel.
  - **Kill:** starvation turns do not fall, or harm on `MP-combat` (fewer traps and rats lose early fights).
- **Risks.** Fewer traps when poor, against CT's gain. That is why E1 merges after E2, KL and CT and is measured
  against a main that holds them. Bytecode: trivial.
- **Size.** 40–100 lines across the gate sites.

#### K1. King steps out of a cat's lane
- **Change.**
  - `robot/king/KingCombatStrategy.java.jinja2:102-170`: score cat threats by the cat's facing. Leaving its 2-wide
    forward lane perpendicular comes first, moving away second (T4§2.4). Trigger: the lane covers the king's 3×3
    within an ETA of ≤ 2·(moves needed)·4 rounds, or the king was scratched in the last 3 turns (T5 P1b).
  - When the king cannot leave the lane, it bites the cat (:180-195).
  - Certain-sign first merge: B3 (:116-117 should read (−2,2) and (−1,2)).
  - `KingMiningStrategy.java.jinja2:173-278`: no return to a mine while a cat seen in the last 20 rounds is within d²
    64 of it (arm: d² 36, T5 P1b). No walk toward the centre box once any mine is known (:53-76).
- **Rests on.** CAT class: keepout 1000 HP, dirtfulcat 420, dirtpassageway 300 (DS§1.1); 117 scratches on awubot's
  kings against 50 on the opponents' (T5 A12); 7 king deaths in 20 CATPATH games against OBG (T4§2.1).
- **Effect.** +1 to +4 pp. CAT cells are 12% of `goal` (33 cells on 19 maps). The maps with all 3 cells CAT are
  sunandmoon, tminusten, bed, EscapeTheNight and directconfrontation. Pockets such as tminusten need K3.
  - **Flips:** `L-cat`, except the pockets.
- **Test.**
  - Gain test on `L-cat` at δ = 0.10, then the harm test on `goal`.
  - Diagnostics: scratch HP on our kings (kcause) and king deaths during cat turns must fall; king bytecode within
    budget.
  - **Kill:** scratch HP does not fall, or bite HP on the king rises (it flees into enemies).
- **Size.** 60–150 lines.

#### CB. Minimal-cheese bites
- **Certain-sign first merge.**
  - B6: `TryRatNap:55` uses `getBiteDamage`.
  - B10: `BabyRat.java:179-213` skips `tryTransferCheese` inside `rawMove`/`turn` when combat planned an action this
    turn (observed twice, combat§8).
- **Then the damage policy.** `CombatStrategy.java.jinja2:440-443` becomes a per-target function, used by `TryAttack`
  in `hasThreat(m,f,target,dmg)` and by `BabyRat.attack` (:245-250, which today forces 10 on non-rats). In order:
  - kill now, at (h − 11)² + 1 cheese;
  - flip HP order for my or an adjacent ally's grab next turn;
  - 13 against a 12-biter in a 1v1;
  - else 12;
  - enemy kings get extra damage once their HP is within 3 rounds of our adjacent biters at 12. Cheese left after the
    last enemy king dies is worth nothing (T2§4.1).

  Cats stay at 10 here; PT adds the cat case.
- **Rests on.** T2§2.1 (race table), T2 P4, combat H4.
- **Effect.** +1 to +3 pp.
  - **Flips:** frontal duels on FACEOFF and COMBAT maps.
- **Test.**
  - Gain test on `faceoff` plus `L-combat` at δ = 0.05, then the confirmatory test on `goal`.
  - Diagnostics: bites by damage level, one-shot kills, 1v1 duels won (CombatStats), cheese spent on bites.
  - **Kill:** duels won unchanged.
- **Size.** 40–100 lines.
- **Merged: CB1c only** (2026-11-19, f809bf3): after a combat move the bite or grab runs only if the action is ready,
  and an out-of-cone secondary target ends the combat turn. Game actions are main's; only the lost turn tails come back
  (`BabyRat.attack` throws 149 → 18 per 192 games). Every damage-policy and king-bite arm ended H0 (`docs/closed.md`):
  bite races are not a lever. Left: `TryAttack.miniRun` has no cone check (13 throws per 192 games). There is no
  bite-damage function on main: PT's cat case goes in `BabyRat.attack`, where cats are forced to 10.

#### PT. Endgame points conversion (bundle with cat-bite cheese)
- **Closed, not merged** (2026-11-19; `docs/closed.md`). Delivery before r2000 is a dead lever (ΔD ≤ +0.5, flips
  none). The late cat hunt gains cat damage and loses kings (net ≈ 0). PC1, the last-cat rule, lost on top of MC
  (−0.186 never dealing the last blow, −0.097 with one king): MC fixed main's own TheHeist cooperation endings, and
  holding moves the rest to r2000 losses on cheese. What remains on the cooperation maps is economy against bots that
  out-deliver us.
- **Change.**
  - `MinerStrategy.java:175-213`: from t ≥ 2000 − (distance to the nearest king + 20), any raw > 0 goes home. No
    exploration after r1800.
  - `CombatStrategy.java.jinja2:453-457`: from t ≥ T_hunt with no enemy seen for 50 rounds, rats with raw 0 hunt live
    cats. Prefer cats on the enemy's half, or low enough that the enemy would finish them. Arms: T_hunt ∈ {1300, 1600}.
  - The cat case of CB's damage function: 1–2 cheese per cat bite from t ≥ 1500 or in cooperation (DS§4.5).
  - The king's cat bite is K1's.
- **Rests on.** T3§3.2: cat bites flip cheesebottles and wallsofparadis; delivering raw cheese flips TheHeist; 2000 raw
  held at the TheHeist mirror ending. T3 P2, P4.
- **Effect.** +1 to +2 pp after KL. Cat bites and raw delivery move a few points, so its reach is the 11 points cells
  decided within 2 points (15% of `L-points`).
  - **Flips:** points cells with small margins (`L-points`).
- **Test.**
  - Gain test on `L-points` at δ = 0.08, plus the paired mean ΔD (Coop2).
  - **Kill:** paired mean ΔD ≤ 0.5 points, or late king-kill losses rise.
- **Size.** 50–120 lines. Merges after CB (`BabyRat.attack`).

### 4.2 Wave 2

#### E3. Territory: miners by deficit, exploration that ends
- **Closed** (2026-11-19): every lever lost on the ≥ 8-mine class pool (`docs/closed.md`). Against awubot, far trips
  to enemy-side mines hold the front; its distance-weighted lottery looks near a local optimum. Change 0, the mine
  squeak decoder fix (it read 1 of the 3 mines a squeak packs), is not merged either: no gain vs awubot and a lean
  against us vs OBG twice. Drop E3's +2 to +6 pp from the wave-2
  forecast.
- **Change.**
  - The king writes payload role 1 (`KingSpawningStrategy.java:93`) with the mine whose miner deficit is largest:
    - target miners per mine n_mine(d, L) (T1§2.6);
    - over our own and the contested mines (within 4 path tiles of the equidistant line);
    - never enemy-side mines.
  - `MinerStrategy.java:83-111` follows the assignment instead of the ⌊√d⌋ lottery.
  - Exploration (:70) stops once the symmetry and our half's mines are known.
  - Arm: near-home go-home thresholds 10 → 25 and 25 → 40 (T1 E5).
- **Rests on.** Harvest saturates at 86–93% of supply (T1§2.3, T5§2.1); OBG and GST hold more mines in 12 of 15 wins
  (T5§4.2, §5.2); awubot's lottery points 71% of its weight at enemy-half mines on keepout (T5 A6).
- **Effect.** +2 to +6 pp, on maps with ≥ 8 mines.
  - **Flips:** keepout, cheesefarm, hatefullattice, mercifullattice, safelycontained, cheeseguardians, and the four STARVE
    maps with 6–10 mines (DefaultMedium, popthecork, closeup, toomuchcheese), where the loser's income collapsed.
- **Test.**
  - Gain test on the ≥ 8-mine maps of `L-combat` and `L-starve`, then the confirmatory test on `goal` at δ = 0.04.
  - Diagnostics: mines held (Deep), transferred share in r200–600, deaths of lone rats.
  - **Kill:** share unchanged.
- **Size.** 150–250 lines. It can start any time after S1, but merges after CT: territory is held by the exchange
  (T5§9).

#### KB. Early second king
- **Merged: the cooperation king only** (2026-11-19, 2b6d29e). From r100, still in cooperation with one king, the
  coordinator forms a second without the 20-rat gate (12-rat floor) or the known-mine threshold, at a known unassigned
  mine on our side under every possible symmetry; a backstab aborts it. Coop maps +0.235 [+0.150, +0.357] on fresh
  seeds (1119-070812-ab-b6a7), `goal` harm +0.010 (1119-070818-ab-f29e). Expected on `goal`: about +1 pp. The round
  arms (r40–r350) all lost (`docs/closed.md`): the second king starves (rebuild, a second upkeep, digging, and it
  pays upkeep after the first king), and no bank gate fixed it. Kings learn mines from their own vision, so known-mine
  gates measure what the king has seen.
- **Change.** Arms on the earliest formation round, with KL's mechanism (arm A's gates, or arm B's bud if KL needed
  it):
  - an opening bud from the first 7 spawns (150 cheese, about r10), only where kk_dirt > 20 (T4 `maps.tsv`);
  - r40, r100, r200;
  - r350, OBG's rule (T5 P7);
  - the champion's gate (zero arm).

  On maps likely to end in cooperation (no enemy seen by r100): form before r150 (T3 P3c). This is the arm that can
  reach TheHeist and peaceinourtime.
- **KL's finding applies:** a king beside another king starves on the shared mine (−0.140 on `L-late`); the new
  king's site must be its own mine.
- **Rests on.** T1§2.7: kings are cheapest early, and in 15% of kill games the winner's king ended below 300 HP.
  T3§4.2: 2v1 is +10 in cooperation. T3§3.2: peaceinourtime was decided by a second king.
- **Effect.** +2 to +6 pp; the sign is uncertain, because the opening loses presence (first contact r4–25).
- **Test.**
  - Arm chosen on `L-combat` plus `L-points` by diagnostics, then one confirmatory test on `goal`; harm test on
    `faceoff`.
  - Diagnostics: formation round; second king alive at r600 and r2000; deaths in r0–200; income share in r200–600.
- **Size.** 60–150 lines on top of KL.

#### K2. Cat-lane memory and a safe king site
- **Closed, not merged** (2026-11-19; `docs/closed.md`, branch `t3/k2-cat-lane-king-site`). Best arm D' (no
  mining step into a known lane) passed `goal` harm twice but its `L-cat` gain test ended H0, +0.021 [−0.038, +0.075].
  Cat-involved king deaths fell 30%, all king deaths 1%: enemies kill the kings cats no longer do. Squeak steering
  is not a lever, so the header-2 cat sighting is not worth building, and slots 38–41 stay free. Parked kings are
  rarely scratched; on 60+ maps a 3×3 king cannot reach any mine (2-wide gaps), which matters to every king-site
  rule (KB, K3, formations). A straight forward lane holds for ~4 steps, not 10 (t4-maps §7).
- **Change.**
  - Header-2 squeak "cat sighting" (cat id, location, facing, round) replaces the panic squeak. A rat relays a sighting
    only when no cat is within r² 16 of itself: in ATTACK a cat turns toward the oldest queued squeak it heard (R§12,
    §16.4), so a relay near our king would steer the cat there.
  - Kings keep the last sighting per cat in slots 38–41.
  - Kings and `Pathfinder` mark each cat's 2-wide forward lane for about 10 tiles.
  - `KingMiningStrategy`: the site is the home-side mine that minimises delivery to our own mines (T1§2.6), with its 3×3
    off known lanes and a perpendicular escape (T4§3). Ties go toward the centre.
  - Step 0: port `GameWorld.bfsFromTarget` and compare BFS lanes with straight segments on `L-cat` (T4§7).
- **Rests on.** T4 WP2 (components 1–4), T1 E6.
- **Effect.** +1 to +3 pp.
- **Test.**
  - Gain test on `L-cat` at δ = 0.08.
  - Diagnostics: king turns in a known lane; scratch HP on our kings; cat turns toward our king after our squeaks
    (must not rise).
- **Size.** 150–250 lines. Depends on K1.

#### K3. Rats defend the king from cats
- **After K2:** expect less than planned; in L-cat cells enemies, not cats, decide most king deaths.
- **Change.** New `CatDefenseStrategy`, entered when a lane hits our king within its ETA and the king cannot clear it,
  as in the pocket of DS§8 item 2.
  - Dirt across the lane 4–6 tiles out. Two placers out-pace one digging cat (T4§2.5), but team dirt starts at 0, so
    the strategy digs its stock first (5 cheese each).
  - Bites from outside the cat's 180° cone.
  - Grabbed enemies thrown into the cat (CW's scoring).
  - In cooperation, every rat hits a cat in our half, within PC's last-cat rule once PC exists.
  - The cat-gate radius at `CombatStrategy.java.jinja2:455` changes after PT's edit.
- **Rests on.** T4 WP3; the tminusten geometry (DS§8 item 2); OBG dealt 7,500–8,000 cat damage by r100 on tminusten
  (T4§2.1).
- **Effect.** +0.5 to +2 pp: the pocket CAT cells of `goal` (tminusten 3, sunandmoon 3, attack_on_cat 1; W0.3).
- **Test.**
  - Gain test on the pocket maps of `L-cat` at δ = 0.10.
  - Diagnostics: our king alive at r110 on tminusten; cat damage by r150.
- **Size.** 150–300 lines. Depends on K1, PT and CW.

#### CX. Complete threat model and trap avoidance
- **Closed** (2026-11-19; `docs/closed.md`). Four arms H0; the graded threat moved its diagnostics (deaths 74.2 → 70.3,
  grabs 65.1 → 60.5) with no win gain, and suspected-trap avoidance hit the kill criterion (standoffs 365 → 485, −0.090).
  Half the grabs on our rats come from enemies never in the victim's cone, and no observable rule predicts awubot's
  traps. `L-combat` is near its ceiling on current main (0.81–0.97) and no longer separates combat arms.
- **Change.** `CombatTracker.java.jinja2` and `getCellScoreFor` (`CombatStrategy:366-382`). Add:
  - bite-then-grab and pincer counts (T2 L3, L4);
  - enemies facing away that can turn and grab;
  - the cat's scratch cone and pounce reach;
  - as a separate arm, suspected awubot trap tiles: when our rat is "strong" (100 HP, or ≥ 80 HP and ≥ the awubot rat's
    HP + 12) and faces an awubot rat at Chebyshev ≤ 2, mark the 3×3 of its front cells next to us for N rounds
    (T5 P4, A8).

  Graded penalties next to the binary −100.
- **Rests on.** T2 P5, T5 P4, combat H5.
- **Effect.** 0 to +5 pp; the sign is uncertain, because more caution gives up first bites.
- **Test.**
  - Arm chosen on `L-combat`, then the confirmatory test on `goal`.
  - Diagnostics: our rats grabbed per game; awubot traps we trigger ÷ awubot traps placed (98% today); standoff turns.
  - **Kill:** standoff turns rise while the win rate stays flat.
- **Risks.**
  - `enemyMaskForMove` is already 32.6 KB of the JVM's 64 KB method limit (codegen.md).
  - Gauntlet: the trap-tile arm encodes awubot's own gate. OBG and GST trap on other gates (d² 3–8, cheese floors;
    T5§4.1, §5.1), so against them it only makes our rats avoid tiles for nothing. Keep that arm only if the gauntlet
    holds.
- **Size.** 200–400 template lines. Depends on CT.

#### CC+CK. Carrier discipline, king assault and throw shield (one bundle)
- **Closed, not merged** (2026-11-19; `docs/closed.md`). Ten arms and two bundles, every diagnostic moved as intended,
  no win gain: the full bundle on `goal` read +0.001 [−0.033, +0.034] over 345 cells. Each lever shifts rats between
  jobs (shield, bites, rescues). T4 WP4's far-side spawn is refuted (−0.100): near-side spawns are what keep enemies
  off the king's ring. The king assault turns bites into throws; damage to the enemy king is limited by how many rats
  reach it. Drop CC+CK's +2 to +6 pp from the forecast.
- **Change.**
  - CC: while an enemy carries one of our rats:
    - do not end a turn on the carrier's step-2 tiles (its facing ±45°, after one move);
    - stand on its step-1 tiles, bite it, and grab it when legal, which drops our rat (E:world/InternalRobot.java:685-700);
    - the carried rat squeaks its carrier's position and facing (header 3).
  - CK:
    - carry victims toward an enemy king within about 6 tiles and throw from Chebyshev 3, outside its bite ring
      (T2§4.1); occupy its ring;
    - our king bites with cheese at kill or flip thresholds;
    - never dig our own ring dirt while enemies are within 8 tiles (delete `digSurroundingDirt` first); place dirt on
      ring tiles aligned with enemy carriers; spawn on the far side (T4 WP4).
- **Rests on.** T2§10, P6 (about 30 double hits per game, about 1,250 HP), P8; T4 WP4; throws plus residual are 31% of
  losing kings' HP (T4§2.1).
- **Effect.** +2 to +6 pp, on FACEOFF and the smaller OPEN maps.
- **Test.**
  - Gain test on `faceoff` plus `L-combat` at δ = 0.05, then the confirmatory test on `goal`.
  - Diagnostics: awubot's step-2 collision share against us; rescues per game; throw HP taken by our king; throw HP we
    deal to theirs.
- **Size.** 220–450 lines. Depends on CW and K1.

### 4.3 Wave 3 (opened by §6)

| id | package | content | source |
|---|---|---|---|
| PC | Cooperation-ending control | last-cat rule, delivery before the cat clock, cat HP taken from the enemy's share; points estimator in slot 15 | T3 P3, P8 |
| XA | awubot exploits | lure cats onto its king (cats target the last robot in scan order, squeaks steer them); pressure a king showing the starvation signature; deny its late formation (dirt on the 3×3, attack the gathering) | R§16.3-4; T4§2.6; T3 P5; opp§3 Gravy |
| IS | Symmetry by mines, enemy-king candidates | resolves the 10 of 14 maps where walls cannot | T4 WP6 |
| CH | Choke play | traps and dirt at cut ≤ 2 when ahead on points | T4 WP7 |
| BL | Blame policy | no rat traps or first bite in cooperation; cat traps in the 100-round victim window | T3 P6 |
| QD | Quals combat diff | awubot_quals beats the final bot on microtest from both seats; TryThrow differs by 528 lines, TryTrap by 250 | combat H11 |
| JS | Joint combat search | one pass scoring nap, attack and fight per (move, facing): "move + nap + reposition", "throw then bite" | combat H12 |
| AB | Ablations | remove champion features one at a time: anti-glorp troll, eternal exploration, ally-sacrifice gate | lessons§9 |

- **MC merged** (2026-11-19, 4bd83c9): rats chase only cheese they can reach within 6 steps. Vision ignores walls and
  dirt, and unreachable chases took 28% of rat-turns in long rich-map losses. `goal` gain +0.037 [+0.011, +0.065] on
  fresh seeds (1119-082724-ab-c977); merge harm on current main +0.006 [−0.043, +0.056] (1119-100436-ab-7a0a).
  Centre-mine holding is not a lever (our own rats take 80–92% of a respawn within 2 rounds); the centre share is
  mostly a symptom of losing. Exploration pays as well as mine trips once its chases are counted.
- **E2b closed** (2026-11-19; `docs/closed.md`): no arm that lifts the rat cap during a siege beat main on fresh seeds
  (SR on `goal` +0.013 [−0.013, +0.040]); on E2's capped maps siege spawns double starvation. The king's panic spawn
  boost is load-bearing (−0.019 [−0.038, −0.002] without it); the cat boost is neutral and stays. E2's income cap
  (arm 2) has little room: on rich maps both teams sit at 16–18 rats with 1,300–1,700 banked.
- **AB result** (2026-11-19; `docs/closed.md`): 13 ablations, none gains. Load-bearing: the squeaked-enemy chase
  (−0.087 without it), eternal exploration (−0.041), the rich spawn lines (−0.093). Ten deletions are neutral alone,
  but eight of them bundled lose to OBG (−0.045): small losses add up. Only the dead-code deletion (no logic change)
  is a merge candidate. Ablation is mostly exhausted outside other packages' code.
- **XA closed** (2026-11-19; `docs/closed.md`, `docs/theory/xa-formations.md`): awubot's formations cost us 6.3 pp
  (+0.063 [+0.033, +0.098] against an awubot copy that never forms), all on maps with ≥ 5 mines, but nothing of ours
  is there to deny them: a gathering lasts a median 14 rounds and ≥ 3 of our rats are near it in 13% of formations
  (≈ 0.8 pp at best); a crowd at a mine looks the same with or without formations. Our own formations are worth more
  (main without them: −0.111 [−0.167, −0.063]). The rich-map gap is a king race won by economy and our own kings.
- **KF merged** (2026-11-19, e547d46; `docs/theory/kf-formations.md`): the cooperation king's mode ends at the lock-in
  round, so a cooperation game then forms under the lock-in's rules. On rose (cooperation to r2000, no own-side mine
  known) we never formed and lost 5 of 6 at 1 king to 2; fixed: 79–1 against main's 47–33 on 40 fresh cells. `goal`
  gain H0 (+0.006, one map of 95), no harm on `goal`, `teammade` or the gauntlet. Every other arm was H0 (site,
  timeout, mine and rat gates, KB-centre): rich-map formations follow wealth (the 20-rat gate is the rich spawn line),
  and opening a gate forms kings into weakness.
- **TN merged** (2026-11-19, abcfd6c; docs/evaluation.md "Tuning constants"): 18 constants moved to `Tune.java`
  (no instruction changed), `tools/tune` screens and fits them. One moved: `EXPLORE_P` 0.2 → 0.6, the share of miner
  trips that explore, gaining on ≥ 8-mine maps (our cheese share at r500 +1.5 pp there). Fresh seeds: s4 +0.034
  [+0.011, +0.056], `goal` gain at δ 0.05 H0 at +0.019 [−0.007, +0.044], `mines-ge8` +0.038 [+0.004, +0.075],
  `teammade` passed +0.041, `goal` harm after KF +0.026; gauntlet no harm. The rest are flat or did not replicate.
- **KD closed** (2026-11-19; `docs/closed.md`, `docs/theory/kd-danger.md`): a second king formed before our only king
  dies gains nothing measurable. Oracles that knew main's king-death round and formed 30 rounds ahead read −0.004 on
  `goal` (beside the king or at a far site). COMBAT deaths: the new king dies 50–150 rounds later to the same army.
  STARVE deaths: the bank is empty. CAT deaths: the far site wins 13 of 25, about +0.9 pp, too few to measure. Arms
  N, NF, T2F, CAT, CAT2 all H0. tminusten (seat-A king walks into the cat) is a single-map king-site case.

- **W2 done** (2026-11-19; `docs/theory/w2-classes.md`): main against awubot on fresh cells (seed base 9201, 570
  cells) 0.835; points losses −70% since W1, fights, starvation and cats unchanged; losses cluster on 15 maps. The
  rich-map income gap is the centre mines (81% of it in long rich losses), taken by presence, not by fights or
  efficiency, and what varies is awubot's commitment there (its random luck, which is also the seat effect). An
  awubot that never takes centre cheese loses +0.080 [+0.044, +0.115] more (+0.130 on centre maps); our own presence
  (three oracles with the centre mines given) gains nothing. Ranked: CD (deny awubot the centre: traps, carrier
  interception, dirt; unpriced mechanism, ceiling +8 pp), CL (cat lane from one seat, ≤ 3 pp). Oracles must be read
  against salted copies of main: main's draw on one seed base shifts every arm alike (§4.6 there).

## 5. Order to 90%

### 5.1 Loss model on `goal`

**The measured chaos changes the model.** A change that only moves bytecode flips about 47% of cells (W0.1), so on
most cells each game is close to a coin flip once the bots differ at all. Model the goal score as

    score = Σ_c w_c · s_c

where w_c is the class's share of `goal`'s cells (W0.3) and s_c our per-game win probability on that class's cells:
0.5 today, in every class. A package that converts a share p of a class's losing cells into wins at per-game
probability s_win lifts s_c by about p·(s_win − 0.5).

The share of cells lost in both seats follows: about (1 − s)² when the two games of a cell are independent. It is 9% at
s = 0.7 and 1% at s = 0.9. So f₀₂ ≤ 0.02 holds only once s ≥ 0.86: it is a consequence of a large edge, not a separate
assumption.

| class | cells (w) | wave-1 owners | wave-2 owners | s after wave 1 (low / mid / high) | **s measured after wave 1** | s after wave 2 (low / mid / high) |
|---|---|---|---|---|---|---|
| COMBAT | 96 (0.337) | CT, CW, CB; E2 on its 31 cells with ≤ 4 mines | E3, CX, CC+CK, KB | 0.58 / 0.70 / 0.78 | **0.82** (35 of 192 games lost) | 0.65 / 0.78 / 0.90 |
| POINTS | 72 (0.253) | KL (29–40 of the 62 that reach r1000), PT | KB (the 10 early cooperation endings), PC | 0.60 / 0.68 / 0.76 | **0.72** (40 of 144) | 0.65 / 0.76 / 0.88 |
| STARVE | 62 (0.218) | E2 (25 cells with ≤ 4 mines), E1 (12 where both starved), CT, CW | E3 (25 cells with ≥ 8 mines), E2's later arms | 0.60 / 0.70 / 0.80 | **0.82** (22 of 124) | 0.68 / 0.80 / 0.90 |
| CAT | 33 (0.116) | K1 | K2, K3 (pockets) | 0.58 / 0.68 / 0.78 | **0.73** (18 of 66) | 0.70 / 0.82 / 0.92 |
| COMBAT+starve | 19 (0.067) | E2 (8 with ≤ 4 mines), CT, CW, E1 | E3, CX | 0.58 / 0.68 / 0.78 | **0.82** (7 of 38) | 0.65 / 0.78 / 0.90 |
| SELF | 3 (0.011) | S1 | – | 0.85 / 0.95 / 1.00 | **1.00** (0 of 6) | 0.90 / 0.97 / 1.00 |
| **score** | 285 | | | **59% / 70% / 78%** | **78.6%** (448–122) | **67% / 79% / 90%** |

**Measured after wave 1** (docs/theory/w1-classes.md): main `ca54f76` (S1, RB, E2 arm 1a, S2, KL, CW, CT, S3, CB1c)
against awubot on the same 285 cells (run `1119-065328-ab-12f4`, plain mode, `--replays all`). Cells keep their W0.3
class in the column above. COMBAT, STARVE and COMBAT+starve passed the high forecast; POINTS and CAT sit between mid
and high. The losses themselves, classified by W0.3's rules:

| class | W0.3 losses (share) | lost games after wave 1 (share) | change | list pool |
|---|---|---|---|---|
| POINTS | 72 (25%) | 41 (34%): 12 cooperation endings, 29 at r2000 | −43% | `L1-points`, `L1-coop` |
| COMBAT | 96 (34%) | 32 (26%) | −67% | `L1-combat` |
| STARVE | 62 (22%) | 27 (22%) | −56% | `L1-starve` |
| CAT | 33 (12%) | 15 (12%) | −55% | `L1-cat` |
| COMBAT+starve | 19 (7%) | 7 (6%) | −63% | `L1-combat-starve` |
| SELF | 3 (1%) | 0 | −100% | – |
| **all** | 285 of 570 games | 122 of 570 games | −57% | |

By mechanism (w1-classes §3), 57% of the losses are economic: income share by r500 decides the long games, and the
centre mines decide the income (`L1-econ`). 75 of the 122 losses are on maps with ≥ 8 mines (s = 0.755 there).

Mid-column arithmetic:
- Wave 1: 0.337·0.70 + 0.253·0.68 + 0.218·0.70 + 0.116·0.68 + 0.067·0.68 + 0.011·0.95 = 0.696.
- Wave 2: 0.337·0.78 + 0.253·0.76 + 0.218·0.80 + 0.116·0.82 + 0.067·0.78 + 0.011·0.97 = 0.788.

What the s values rest on:
- **COMBAT and STARVE, wave 1.** E2's measured 0.86 on ≤ 4-mine maps (§4.1), discounted to about 0.8, covers 32% of
  the COMBAT cells and 40% of the STARVE cells. E1 adds the 12 both-starve cells. The unmeasured combat packages are
  assumed to reach 0.6–0.7 on the rest.
- **POINTS, wave 1.** KL converts about 0.7 of 29–40 cells to about 0.85 per game; PT reaches the cells within 2
  points (§4.1).
- **CAT.** The share of scratch damage that comes before the king could step out; pockets wait for K3.
- **The wave-2 high column is a target, not a forecast.** Nothing measured yet shows any class at 0.9.

**90% needs Σ w_c s_c ≥ 0.9.** With SELF at 1.0, that is a weighted mean s of about 0.9 over the other 99% of cells:
every class at about 0.9, which is the wave-2 high column. A game at s = 0.9 is one we win whatever the noise. **That is
a strength gap, not a pile of small edges**:
- in Elo terms 0.70 is +147, 0.79 +230 and 0.90 +382 (A: 400·log10(s/(1 − s)));
- the measured rat-cap arm alone was +76 on `quick` (score 0.608).

### 5.2 What the chaos implies for order and δ

1. **Rank by size of effect.** A +2 pp package moves one class's s by about 0.06–0.09. The 0.9 target needs packages
   that move a class by 0.2–0.4: S1 on SELF, E2 on mine-poor cells, KL on late points cells, and whatever the combat
   packages prove on COMBAT.
2. **δ.** On `goal` only δ ≥ 0.05 (§1 item 3). Small packages: a gain test on their loss-class pool at δ = 0.08–0.10,
   plus a harm test on `goal`. Bundle same-class small changes (§1 item 7).
3. **Cumulative harm.** A harm test at δ_h = 0.05 passes a −2 pp change most of the time. After each merge the
   integrator re-measures main in plain mode on `goal`, at least 2 passes (190 cells, 380 games: a 95% CI of about
   ±5 pp). If main falls below the previous merge by more than that, bisect the merges with gain tests between
   adjacent mains.
4. **Re-classify after each wave.** As the bot changes, the losing cells change class. Re-run W0.3's classification on
   the merged main (§3) and recompute this table with the measured s_c per class, not these guesses.
5. **Prefer structural levers awubot cannot adapt to**: army size by mines, kings by r1201, a spend gate, cat safety,
   traps it walks into. They change many engagements per game in the same direction, which is what moves a class past
   the noise (DS§2.6).

### 5.3 Merge order and why

1. **S1.** Certain, cheap, and it edits the assert lines that CT, CW, CB and TryPosition's owners also touch.
2. **RB.** Certain-sign; king bytecode before KL and K1 add king code (§1 item 9).
3. **E2 arm 1.** The only measured lever (+4 to +8 pp). It edits `KingSpawningStrategy.java:21-73` before E1's gate at
   `:69`.
4. **KL.** The largest founded effect on POINTS. Arm A is small and reuses existing code.
5. **CT, then CW.** The largest measured combat asymmetry, then the largest unmeasured one. Same class, so expect
   sub-additivity.
6. **E1.** It gates the spending of E2, KL and CT, so it merges after them and is measured against a main that holds
   all three.
7. **K1.** Concentrated on `L-cat`, cheap to confirm there.
8. **CB, then PT.** Small effects: class-pool gains plus a harm test on `goal` (§5.2 item 2); their certain-sign parts
   land first (§1 item 7).
9. **Wave 2,** each package as its lane frees up. Inside a class, the larger expected effect first: E3 before E2's later
   arms; CX and CC+CK after CT and CW.
10. **After each merge**, the plain re-measure of §5.2 item 3, recorded in docs/progress.md. Every later gain test is
    paired against that new main (§1 item 3). **After each wave**, the re-classification of §5.2 item 4.

### 5.4 Parallel lanes

All lanes start once S1 is on main. CW waits for S1 only because they share TryThrow.

| lane | sequence | can run in parallel with |
|---|---|---|
| safety | S1 | – (merge first) |
| bytecode | RB | every other lane; merge before KL and K1 |
| economy | E2 arm 1 → E1 (after KL and CT merged) → E2's later arms | every other lane; E1 rewires the gates that are in main |
| kings | KL → KB | every other lane (payload schema §2.1) |
| traps | CT → CX | every other lane |
| throws | CW → CC+CK (also waits for K1) | every other lane |
| bites | CB → PT's cat case | every other lane |
| points | PT → PC | every other lane; K3 waits for PT |
| cats | K1 → K2 → K3 | every other lane |
| territory | E3 | every other lane (payload schema §2.1) |

At most 10 lanes run in parallel in wave 1. Merges are always serial.

### 5.5 Open bugs (certain-sign, unowned)
- Lead, unmeasured (CX): our rats send no enemy squeaks for 4 turns after seeing a cat, nor on turns with ≤ 1,000
  bytecode left; half the grabs on our rats come from enemies the victim never saw.

Found by packages outside their files; each is a small fix judged by its diagnostic plus a `goal` harm test. S4's
probe counts them on 380 `goal` games (own seeds and seed 7, both seats; `~/battlecode26-data/scratch/s4/`).
- Fixed (S4): `TryAttack.miniRun` bit the tile toward the nearest adjacent enemy's centre without asking the engine.
  9 throws per 380 games, each losing the turn tail: 4 kings sensed through a tile in the cone while the centre's
  tile is outside it, 4 enemies listed on our own tile or on an empty tile, 1 turn resumed after an overrun. It asks
  `canAttack` first: 0 throws. Harm `1119-080920-ab-9afe` PASS (97 cells, none changed).
- Fixed (S4): the formation coordinator wrote its own mine into slot `kingId+1`, left from when a new king took
  id = king count. It misled only the coordinator, which saw its own mine as another king's: 423 king turns per 380
  games, 5 target changes. On t_minus_five_minutes it can make the first and the cooperation king each target the
  other's mine and stand still ~20 rounds at ~18k bytecode. Deleted: 0. Harm `1119-080920-ab-b37b` PASS (1 cell
  worse, t_minus_five_minutes seat A); that map on 30 fresh seeds, `1119-082919-ab-006e`: 2 better, 0 worse.
- Not fixed: on corridorofdoomanddespair both kings start with dirt inside their 3×3, and `digDirtInDirection` digs
  only the tiles that enter the 3×3. It is 1 of the 150 map files in `maps/`; our king never moves there and main
  wins both seats by KILL (r322, r339). Dirt cannot enter a king's 3×3 later (`placeDirt` needs a tile with no ground
  robot), so a fix would only cost bytecode on every king move.
- Open, under 1 per 100 games each (S4 probe on the fixed branch, 380 games): `TryAttack.run`'s bite after its move
  finds no robot (3), `TryRatNap.run`'s fallback bite finds no robot (3), a move into an occupied tile (`Game._rawMove`
  from `BabyRat.rawMove(dir)`, 2), a rescan outside vision (`CellTracker`, 1), and `BabyRat.run`'s carried-rat check
  reads a null carrier (1). The probe saw an enemy listed on the biter's own tile, and one sensed on a tile the robot
  grid holds empty: robots off the grid but not carried (cause not established).

## 6. If a wave stalls

1. **W0.1 measured r ≈ 0.47 and no net bytecode cost** (§3). Single-cell and single-map reads are noise: judge class
   totals and diagnostics, and test small packages on their class pools (§5.2). RB's further items (cheese-list rewrite
   on change only, arch§8) stay in wave 3 unless a later measurement shows a cost.
2. **Wave 1 below 59% on `goal`.** Read the per-merge re-measures for 0–2 pairs, and revert or ablate the merge that
   introduced them. Check each class's diagnostic: a mechanism that did not fire is a bug, not a dead idea.
3. **Wave 1 in its 59–78% range.** This is expected; wave 1 does not reach 90% by itself. Start wave 2.
4. **Wave 2 below 90%.** Re-classify main (§5.2 item 4) and open the class with the lowest measured s_c:
   - COMBAT: QD, then JS, then OBG micro components (`MicroManager`'s safety tiers that count numbers) as an
     alternative arm to CX.
   - POINTS or cooperation: PC, BL.
   - CHOKE: CH.
   - CAT: XA's lure.
   - Any class: XA's pressure on a crippled king, then AB.
5. **A package regresses one class while gaining overall.** Gate it on a map feature the bot can observe: known mine
   count (≤ 4 against ≥ 8, T5§8.6), kk from symmetry, cut. Never on map names or exact sizes (lessons§7).
6. **The 90% verdict** needs `goal`, the sealed holdout and the gauntlet, all on engine 1.2.6, both seats
   (program.md), with team-made maps as a robustness check.
