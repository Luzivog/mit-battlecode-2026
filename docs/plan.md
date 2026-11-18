# Plan: work packages to 90% against awubot

This plan comes from track T (docs/program.md) and is the input to track B. Its theory is in
`docs/theory/dominant-strategy.md` (cited `DS§n`); the lens files are cited `T1§n` … `T5§n`. Other source keys follow
DS:
- `R§` rules.md; `E:` the engine at tag 1.2.6; `C:` `src/challenger/`; `A` marks arithmetic.
- `TG:` the evaluation tool (`tools/games`, track E); `critic/` the plan critic's scratch in
  `~/battlecode26-data/theory/critic/`.

"pp" means percentage points of win rate against awubot, under official conditions, both seats. Effects are given on
the 43 official maps, the only pool with a loss table so far (DS§1.1). On those maps, flipping one map's losing seat is
+1 game of 86 (+1.16 pp). W0.3 rescales every effect to the goal pool (§1 item 4).

## 0. Summary

- **Before any wave-1 verdict:** W0.0 (gain tests paired against frozen main) and W0.1 (how often games flip, and the
  bytecode tax) must exist. Both are track E's (§3).
- **Gates.** S1 (never die to our own code) merges first: it is certain and cheap, and it touches the assert lines four
  other packages edit. RB (reclaim king bytecode, an enabler) merges before KL and K1, which add king code.
- **Wave 1, in merge order:** S1, RB, KL, CT, CW, E1, K1, CB, PT. Built in parallel on disjoint code (§2), merged
  one at a time, each rebased and re-tested on the current main (§1 item 6).
  - Expected: **about 69% against awubot (range 61–77%)**. This is a planning number until W0.1 measures f₀₂ (pairs
    lost 0–2) and W0.3 measures the class frequencies (§5).
- **Wave 2:** E3, KB, E2, K2, K3, CX, CC+CK. Each starts when its lane's predecessor has merged.
  - Expected: **about 84% (range 75–93%)**. 90% needs f₂₀ ≥ 0.82 with f₀₂ ≤ 0.02: every loss class converting at
    about 0.8 or more, COMBAT included (§5).
- **Wave 3** opens on the class with the largest residual (§6). Reaching 90% probably needs it.
- **Weakest assumption.** That consistent micro edges (CT, CW, CB, CX) win both seats on the maps decided by close
  combat: 15 of the 43 official maps, plus the combat part of 6 more. Nothing measured supports it. The only
  perturbation data shows chaos and possibly a bytecode tax (DS§2.6). If it fails, wave 2 ends near 75–80%.

| rank | id | package | expected effect (pp, official 43) | depends on | lane |
|---|---|---|---|---|---|
| 1 | S1 | Never die to our own code | certain on the ≥ 26-mine maps (sieveofriches in the goal pool; bridges, bridgesCatless, quadrants team-made); elsewhere positive sign, size unmeasured | – | safety |
| 2 | RB | Reclaim king bytecode (enabler) | none by itself; frees king bytecode for KL and K1 | S1 | bytecode |
| 3 | KL | King lock-in: relax the existing coordinator's gates first, budding only if that fails | +3 to +7 (bound +9.3) | S1, RB | kings |
| 4 | CT | Trap the approach band | +3 to +8 | S1 | traps |
| 5 | CW | Cat-aware, value-gated throws; hold and bite | +3 to +6 | S1 | throws |
| 6 | E1 | One spend gate on every global spend | +1 to +4 | KL, CT (it rewires their gates) | economy |
| 7 | K1 | King steps out of a cat's lane (B3 included) | +1 to +3; large on `L-cat` | S1, RB | cats |
| 8 | CB | Minimal-cheese bites (B6, B10 included) | +1 to +3 | S1 | bites |
| 9 | PT | Endgame conversion, including cat-bite cheese | +1 to +3 after KL | CB (`BabyRat.attack`) | points |
| 10 | E3 | Territory: miners by deficit, exploration that ends | +2 to +6 | S1, payload schema (§2.1) | territory |
| 11 | KB | Early second king (dose on the round) | +2 to +6, sign uncertain | KL | kings |
| 12 | E2 | Army from income and attrition | +2 to +5 | E1 | economy |
| 13 | K2 | Cat-lane memory and a safe king site | +1 to +3 | K1 | cats |
| 14 | K3 | Rats defend the king from cats | ≈ 0 on official maps; tminusten, attack_on_cat and sunandmoon on the goal pool | K1, PT, CW | cats |
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
   - **Expected games.** Under H1, n ≈ 5.6·r/δ² (A, Gaussian SPRT). r is the share of games whose result differs
     between branch and main, measured by W0.1. At δ = 0.04: r = 0.1 → 350 games, r = 0.3 → 1,050.
4. **The 90% metric** is challenger's win rate against awubot on the **`goal` pool**, both seats, over seed passes
   starting with pass 0 (the official seeds, TG `cell_seed`), on 1.2.6.
   - `goal` = the 43 official maps plus the 52 visible tournament maps (TG `--pool official,tournament`).
   - It must also hold on the sealed holdout at the end.
   - Team-made maps (TG `TEAM_MADE`, 25) are a separate robustness check: no significant regression. They are not
     part of the goal.
5. **Pools, in the order a package uses them.**
   - **First gain pool: the package's loss-class pool**, as a `map:` list.
     - Interim lists, from DS§1.1 (1.2.5 mirror, official maps):
       - `L-starve`: the 8 STARVE maps and the 6 COMBAT+starve maps.
       - `L-cat`: dirtfulcat, dirtpassageway, keepout, plus the 9 visible CATPATH maps (TG class pool `cat`).
       - `L-points`: the 11 POINTS maps plus groveisbeingdesecrated, t_minus_five_minutes, fullofsecrets and rose.
         The last four are visible tournament maps that ended on points or in cooperation (T3§3.4).
       - `L-late`: the 9 r2000 POINTS maps plus the 9 maps with kills after r990 (DS§1.1).
       - `L-combat`: the 15 COMBAT maps.
       - `MP-combat`: the 8 COMBAT maps with ≤ 4 mines (averyfineline, DefaultSmall, canyoudig, tiny, starvation,
         trapped, whatsthecatdoin, jail).
     - W0.3 replaces the interim lists with per-cell classes on the goal pool.
     - The tool's feature pools (`cat` = CATPATH, `starve` = MINEPOOR, `faceoff`) are not loss classes. `starve` holds
       only 4 of the 8 STARVE maps but 8 of the 15 COMBAT maps, and `cat` holds none of the 3 CAT maps. Use them only
       where a package names them.
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
   - Read the flipped pairs; only flips beyond W0.1's background count.
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
    diagnostics did not move either. Record every H0 with its diagnostics in a closed-directions log (lessons§9).
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

Payload 0 keeps today's behaviour. The first package to merge adds the role switch in `BabyRat.init` and an encoder
next to `GlobalArray.registerSpawnedRat` (`C:comms/GlobalArray.java:86-104`); later packages add their case. Dropping
the `Rng.mcg()` call shifts the king's random stream, which is expected (arch§3).

**Shared array.** Free today: slot 15, slot 1 bits 2–9, slot 9 bits 3–9 and slot 21 bits 5–9 (arch§4.1).

| space | owner | use |
|---|---|---|
| slot 0 (`COUNTER`) | S1 | written modulo 1024. Today `ratCounter` grows without bound (`C:comms/GlobalArray.java:108`), and from the 1024th spawn the write at `:225` throws, because the engine rejects values > 1023 (`E:world/RobotControllerImpl.java:1312`). Readers use it modulo 1024 (E2) |
| slots 38–41 (`MIN_CHEESE` 38 → 42; once S1 is in, the cheese list of 22 slots rotates beyond 22 mines) | K2 | the last sighting of up to 2 cats: `encode2x2` location (10 bits), facing (3), round mod 128 (7) |
| slot 15 | PC | points estimator |
| the rest | reserved | amend this plan first |

E1's siege flag stays king-only: no slot carries it, and only kings spawn.

**Squeak headers** (arch§4.2):
- Header 2, the unused lure stub that receivers ignore (`C:comms/Squeaker.java:112-130`, default case): S1 moves the
  panic squeak here. Today it is `7<<29`, which is negative and decodes as two phantom enemies. K2 later replaces it
  with the cat sighting.
- Header 3: CC's carried-rat report.

**`util/Economy.java`** (owner E1): `reserve()` = max(100, 2·K·H), with K counted from the shared array's king slots,
so every unit can compute it; `spendable()` = global − `reserve()`.
- Until E1 merges, packages that spend global use today's gates: the spawn floor of 100
  (`KingSpawningStrategy.java:69`), the formation gate of 150 (`KingFormationCoordinatorStrategy.java:187-188`), and
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
| `comms/Squeaker.java` | S1 (:215-217 panic header) → K2 (header 2) · CC (header 3) |
| `comms/EnemyManager.java.jinja2` | S1 (`trySqueakKing` :73-87) |
| `util/Economy.java` (new) | E1 |
| `robot/rat/BabyRat.java` | S1 (:34-77) · CB (:179-213 `rawMove`/`turn`; :240-250 `attack`) → PT (cat case in the damage function) · E1 (:222 dig gate) · KL arm B / E3 / K3 (`init` :25-32, role switch) |
| `robot/rat/MinerStrategy.java` | E3 (:60-173 targets, exploration) · PT (:175-213 `shouldGoHome`) |
| `robot/rat/FormationStrategy.java` | KL (arm A: gates; arm B: deleted) |
| new `robot/rat/BudStrategy.java`, `robot/king/KingBudStrategy.java` | KL arm B → KB |
| new `robot/rat/CatDefenseStrategy.java` | K3 |
| `robot/king/RatKingStrategy.java` | KL arm B (:24-29) |
| `robot/king/KingSpawningStrategy.java` | S1 (:90 turn) · E1 (:69 gate) → E2 (:21-73) · E3 (:93 payload) |
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

## 3. Wave 0: no bot change (owners: track E and the integrator)

- **W0.0 Paired test mode.** Gain and harm tests of a branch against a frozen snapshot of main, on the same cells,
  pentanomial bounds as in §1 item 3. It needs a snapshot package, because two `challenger` packages cannot share a
  name in one build. **Wave-1 verdicts wait for it.**
- **W0.1 Null calibration, before any wave-1 verdict.** Two arms against main, on `goal`:
  - A near-zero-cost random perturbation: write 0 instead of `Rng.mcg() & 8191` at `KingSpawningStrategy.java:93`. The
    payload is discarded (`BabyRat.java:26`) and `myIndex` is never read, so only the kings' random stream moves.
  - A dummy loop of about 200 bytecodes per rat turn: the bytecode tax.

  Report the flip rate r (2–0 plus 0–2) and the net shift separately. r sets the test cost (§1 item 3); the net shift
  of the second arm prices bytecode (DS§2.6).
- **W0.2 Pools.** `goal`, the interim loss-class lists of §1 item 5 (from `pools.visible()` only), and `overflow`
  (≥ 26 mines). T4's classifier sits next to `tools/games` (T4 WP5).
- **W0.3 Baseline loss table on `goal`.**
  - The run (track E): the challenger–awubot mirror on `goal`, ≥ 3 seed passes, engine 1.2.6, replays kept.
  - The classification (a plan step for whoever runs it once E's run lands): classify every (map, seed) cell as DS§1.1
    does (`synthesis/losstable.py` with `kcause.py` and `Coop2.java`), not one class per map. Classes move with the seed
    (T3§3.1: TheHeist and pipes ended on points in 4 of 4 games, sittingducks in 2 of 4) and with the engine (DS§10).
  - Rebuild the `L-*` pools as `map:` lists of the cells' maps. Recompute §5 with class frequencies.
  - Run `critic/Starve.java` on the STARVE cells to rank the spend channels, before E1 picks H.
- **W0.4 Replays for diagnostics.** The tool keeps replays only with `--replays losses|all`, and cached games have none
  (TG `play.py`). Either track E adds a "replays for this run" option, or each package reads its diagnostics from a
  separate replay pass on a fixed subset of cells.

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
- **Arm B (only if arm A's formation-success diagnostic fails, < 70% of attempts completing).** Budding (DS§4.3):
  - new `KingBudStrategy` and `BudStrategy`, payload role 2 (§2.1);
  - the old coordinator and `FormationStrategy` are deleted (rule 7), with the r1800+ endgame behaviour reproduced
    by the bud;
  - recruits spawn without copying `trySpawn`'s direct `rc.turn` (rule 5);
  - the site avoids the king's own recent cat sightings (r² 25, 360°), until K2's shared lanes replace them.
- **Rests on.**
  - T3§3.2: a third king flips 10 of the 11 losing points seats. **KL reaches 8 of them:** TheHeist (r390) and
    peaceinourtime (r451) end in cooperation before T_lock, and ZeroDay needs 4 kings against 2.
  - T3§3.5: at r1100, 9 of the 18 points seats have 1 king, so a third king needs two formations.
  - T1 E1; R§6.
- **Effect.** +3 to +7 pp; the bound is 8/86 = +9.3 pp (T3 P1's +11.6 counted the two early cooperation endings).
  - **Flips:** 8 POINTS maps (averystrangespace, cheesebottles, minimaze, pipes, rift, sittingducks, uneruesansfin,
    wallsofparadis), and part of the 9 maps with kills after r990.
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

#### E1. One spend gate on every global spend
- **Rests on.** `critic/Starve.java` on the 1.2.5 mirror replays (reproduced on closeup and whereisthecheese). From
  r1 to starvation, on the six STARVE losses that end before r600, the losing seats' bank went to:
  - replacement spawns: 1,480–2,070 (closeup, ending at r1218: 4,380);
  - rat traps: ≤ 480–900 (≤ because traps pay from raw cheese first);
  - upkeep: 268–558.

  The `cost == 10` exception spent 0–50 per loss. Today's spawn floor is 100 (`KingSpawningStrategy.java:69`), but rats
  spend global with no floor: traps (`TryTrap.java.jinja2:64-76`), digs (`BabyRat.java:222`), king digs
  (`RatKing.java:58,68,159,224`; `KingCombatStrategy.java.jinja2:72`). On 5 of the 8 STARVE maps the winner never ran
  out (minimum global 52–144). A reserve flips only the 3 maps where the winner also starved (whereisthecheese,
  corridorofdoomanddespair, arrows); the root cause elsewhere is the lost exchange and share (T1§2.8), which CT, CW and
  E3 address.
- **Change.**
  - New `util/Economy.java` (§2.1): `reserve()` = max(100, 2K·H).
  - Every global spend checks `spendable()`: king spawns (`:69`), traps when raw cheese does not cover them (after CT,
    `TryTrap.java.jinja2:64-76`), rat and king digs, bite cheese (`getBiteDamage` or CB's function), and formation
    (`KingFormationCoordinatorStrategy.java:187-188`).
  - The `cost == 10` exception stays: it spent 0–50 per loss (Starve.java) and is the only way to rebuild income with
    ≤ 3 rats (T1§2.8).
  - No siege waiver below 100.
  - Arms: H ∈ {30, 60, 120} (T1 proposed 30–60). The order and H follow W0.3's ranking of spend channels on 1.2.6.
- **Effect.** +1 to +4 pp.
  - **Flips:** 3 STARVE maps (about 0.8 each), and later starvation on COMBAT+starve maps. It also protects KL's and
    CT's spending.
- **Test.**
  - Arm chosen on `L-starve`, then the confirmatory gain test on `goal` at δ = 0.04; harm test on `MP-combat` at
    δ_h = 0.05.
  - Diagnostics: our king's starvation turns (kcause, Deep) and the round global first falls below 2 (Starve.java),
    against main's; spend by channel.
  - **Kill:** starvation turns do not fall, or harm on `MP-combat` (fewer traps and rats lose early fights).
- **Risks.** Fewer traps when poor, against CT's gain. That is why E1 merges after CT and is measured against a main
  that holds it. Bytecode: trivial.
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
- **Effect.** +1 to +3 pp on the official maps; larger on `L-cat`. Pocket maps such as tminusten need K3.
  - **Flips:** the 3 CAT maps; the cat share of tiny, cheesefarm and minimaze; tournament CATPATH maps.
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

#### PT. Endgame points conversion (bundle with cat-bite cheese)
- **Change.**
  - `MinerStrategy.java:175-213`: from t ≥ 2000 − (distance to the nearest king + 20), any raw > 0 goes home. No
    exploration after r1800.
  - `CombatStrategy.java.jinja2:453-457`: from t ≥ T_hunt with no enemy seen for 50 rounds, rats with raw 0 hunt live
    cats. Prefer cats on the enemy's half, or low enough that the enemy would finish them. Arms: T_hunt ∈ {1300, 1600}.
  - The cat case of CB's damage function: 1–2 cheese per cat bite from t ≥ 1500 or in cooperation (DS§4.5).
  - The king's cat bite is K1's.
- **Rests on.** T3§3.2: cat bites flip cheesebottles and wallsofparadis; delivering raw cheese flips TheHeist; 2000 raw
  held at the TheHeist mirror ending. T3 P2, P4.
- **Effect.** +1 to +3 pp after KL, which already takes most of the same maps.
  - **Flips:** TheHeist (the 2 early cooperation endings are out of KL's reach), cheesebottles, wallsofparadis.
- **Test.**
  - Gain test on `L-points` at δ = 0.08, plus the paired mean ΔD (Coop2).
  - **Kill:** paired mean ΔD ≤ 0.5 points, or late king-kill losses rise.
- **Size.** 50–120 lines. Merges after CB (`BabyRat.attack`).

### 4.2 Wave 2

#### E3. Territory: miners by deficit, exploration that ends
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
- **Change.** Arms on the earliest formation round, with KL's mechanism (arm A's gates, or arm B's bud if KL needed
  it):
  - an opening bud from the first 7 spawns (150 cheese, about r10), only where kk_dirt > 20 (T4 `maps.tsv`);
  - r40, r100, r200;
  - r350, OBG's rule (T5 P7);
  - the champion's gate (zero arm).

  On maps likely to end in cooperation (no enemy seen by r100): form before r150 (T3 P3c). This is the arm that can
  reach TheHeist and peaceinourtime.
- **Rests on.** T1§2.7: kings are cheapest early, and in 15% of kill games the winner's king ended below 300 HP.
  T3§4.2: 2v1 is +10 in cooperation. T3§3.2: peaceinourtime was decided by a second king.
- **Effect.** +2 to +6 pp; the sign is uncertain, because the opening loses presence (first contact r4–25).
- **Test.**
  - Arm chosen on `L-combat` plus `L-points` by diagnostics, then one confirmatory test on `goal`; harm test on
    `faceoff`.
  - Diagnostics: formation round; second king alive at r600 and r2000; deaths in r0–200; income share in r200–600.
- **Size.** 60–150 lines on top of KL.

#### E2. Army from income and attrition
- **Change.** `KingSpawningStrategy.java`: spawn while n < n* = √((Î − 2K)/(2.5·δ̂)) (T1§2.5), or while `spendable()`
  − c(n) exceeds the next planned king's cost. Floor 8.
  - δ̂ = deaths per rat-round. Deaths come from the change in the modular `ratCounter` minus the change in n. n is
    known only as 4·⌊n/4⌋ from `getCurrentRatCost`, so over 50 rounds δ̂ carries ±4 rats of quantisation, about the
    size of the signal: use a 200-round window, or n squeaked by rats.
  - Rats consumed by formation are excluded (DS§8 item 8).
  - A siege-spawn arm replaces OBG's imported constant (cost ≤ global/5) with a dose on the spawn cap while an enemy is
    in the king's vision: {global/10, global/5, global/3}.
  - Step 0: a probe build logs global cheese and spending every 50 rounds, to see how well Î and δ̂ can be estimated
    (DS§9).
- **Rests on.** T1§2.5, E3.
- **Effect.** +2 to +5 pp, on attrition maps (DefaultMedium, evileye, arrows, cheesefarm) and low-contact maps
  (minimaze, averystrangespace, wallsofparadis, ZeroDay).
- **Test.**
  - Arm chosen on `L-starve`, then the confirmatory test on `goal`; harm on `MP-combat`.
  - Diagnostics: idle bank, rats alive, deaths per rat-round.
- **Size.** 80–150 lines.

#### K2. Cat-lane memory and a safe king site
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
- **Effect.**
  - About 0 on the official maps (only peaceinourtime is a CATPATH map there).
  - On `goal`: tminusten, attack_on_cat and sunandmoon in both seats, about +1.5 pp (3 pairs of 95).
- **Test.**
  - Gain test on the pocket maps of `L-cat` at δ = 0.10.
  - Diagnostics: our king alive at r110 on tminusten; cat damage by r150.
- **Size.** 150–300 lines. Depends on K1, PT and CW.

#### CX. Complete threat model and trap avoidance
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

## 5. Order to 90%

### 5.1 Loss model

From DS§1.1 (43 official maps, 1.2.5 mirror). W0.3 replaces map counts with per-cell class frequencies on `goal`.

| class | maps | wave-1 owners (primary → residual) | wave-2 owners |
|---|---|---|---|
| POINTS | 11 | KL (8 reachable) → PT (TheHeist) | KB (peaceinourtime, TheHeist), PC |
| STARVE | 8 | E1 (the 3 maps where both kings starved) → CT, CW | E3 (the 4 maps with 6–10 mines), E2 |
| COMBAT+starve | 6 | CT, CW → E1 | E3, E2, CX |
| CAT | 3 | K1 | K2, K3 |
| COMBAT | 15 | CT, CW, CB | E3, CX, CC+CK, KB |
| self-inflicted (sieveofriches in `goal`; overflow team maps) | – | S1 | K3 (tminusten) |

The game win rate is 0.5 + 0.5·(f₂₀ − f₀₂), where f₂₀ and f₀₂ are the shares of map pairs won 2–0 and lost 0–2.
**90% needs f₂₀ − f₀₂ ≥ 0.8:** 34–35 of 43 maps won in both seats.

To avoid double counting, each class's conversion p is credited to its primary owner; residual owners act only on what
is left: p = 1 − (1 − p₁)(1 − p₂). f₀₂ is 0.045 in wave 1 and 0.02 in wave 2: a guess that W0.1 replaces. A measurable
bytecode tax would raise it.

| class (n) | p after wave 1 (low / mid / high) | p after wave 2 (low / mid / high) |
|---|---|---|
| POINTS (11) | 0.40 / 0.55 / 0.70 | 0.60 / 0.75 / 0.90 |
| STARVE (8) | 0.20 / 0.35 / 0.50 | 0.45 / 0.65 / 0.85 |
| COMBAT+starve (6) | 0.30 / 0.42 / 0.60 | 0.50 / 0.70 / 0.85 |
| CAT (3) | 0.30 / 0.55 / 0.70 | 0.55 / 0.75 / 0.90 |
| COMBAT (15) | 0.20 / 0.35 / 0.50 | 0.50 / 0.70 / 0.90 |
| f₂₀ | 0.27 / 0.42 / 0.58 | 0.52 / 0.71 / 0.88 |
| win rate | **61% / 69% / 77%** | **75% / 84% / 93%** |

Mid-column arithmetic, wave 1: (11·0.55 + 8·0.35 + 6·0.42 + 3·0.55 + 15·0.35)/43 = 18.3/43 = 0.425, so
0.5 + 0.5·(0.425 − 0.045) = 0.69. Wave 2: (11·0.75 + 8·0.65 + 6·0.70 + 3·0.75 + 15·0.70)/43 = 30.4/43 = 0.707, so 0.84.

What the p values rest on:
- POINTS, wave 1: KL reaches 8 of 11 (§4.1) at a formation reliability of about 0.7, plus PT on TheHeist.
- STARVE, wave 1: E1 flips at most the 3 both-starve maps (about 0.8 each); the exchange packages act on the rest.
- CAT: the share of scratch damage that comes before the king could step out.
- COMBAT: no measurement; DS§2.6.

**90% needs f₂₀ ≥ 0.82 with f₀₂ = 0.02**: every class at about 0.8 or above, between the mid and high columns.
With POINTS at 0.9 and STARVE, COMBAT+starve and CAT at 0.8, COMBAT needs 0.78 (A). POINTS is capped near 10/11
(ZeroDay needs 4 kings against 2).

### 5.2 Merge order and why

1. **S1.** Certain, cheap, and it edits the assert lines that CT, CW, CB and TryPosition's owners also touch.
2. **RB.** Certain-sign; king bytecode before KL and K1 add king code (§1 item 9).
3. **KL.** The largest, best-founded effect. Arm A is small and reuses existing code.
4. **CT, then CW.** The largest measured combat asymmetry, then the largest unmeasured one. Same class, so expect
   sub-additivity.
5. **E1.** It gates the spending of KL and CT, so it merges after them and is measured against a main that holds both.
6. **K1.** Concentrated on `L-cat`, cheap to confirm there.
7. **CB, then PT.** Small effects; their certain-sign parts land first (§1 item 7).
8. **Wave 2,** each package as its lane frees up. Inside a class, the larger expected effect first: E3 before E2; CX
   and CC+CK after CT and CW.
9. **After each merge** the integrator re-measures main against awubot on `goal`, the holdout and the gauntlet, and
   records the numbers in docs/progress.md. Every later gain test is paired against that new main (§1 item 3).

### 5.3 Parallel lanes

All lanes start once S1 is on main. CW waits for S1 only because they share TryThrow.

| lane | sequence | can run in parallel with |
|---|---|---|
| safety | S1 | – (merge first) |
| bytecode | RB | every other lane; merge before KL and K1 |
| kings | KL → KB | every other lane (payload schema §2.1) |
| traps | CT → CX | every other lane |
| throws | CW → CC+CK (also waits for K1) | every other lane |
| economy | E1 (after KL and CT merged) → E2 | every other lane; E1 rewires the gates that are in main |
| bites | CB → PT's cat case | every other lane |
| points | PT → PC | every other lane; K3 waits for PT |
| cats | K1 → K2 → K3 | every other lane |
| territory | E3 | every other lane (payload schema §2.1) |

At most 9 lanes run in parallel in wave 1. Merges are always serial.

## 6. If a wave stalls

1. **W0.1 shows a large flip rate or a bytecode tax.**
   - If r ≥ 0.3, single-map reads are noise: judge only class totals and diagnostics, and raise δ for small packages.
   - If the 200-bytecode arm loses measurably, every package's bytecode delta counts against it: move RB's further items
     (cheese-list rewrite on change only, arch§8) into wave 1, and prefer deletions.
2. **Wave 1 below 61% on `goal`.** Read the per-merge re-measures for 0–2 pairs, and revert or ablate the merge that
   introduced them. Check each class's diagnostic: a mechanism that did not fire is a bug, not a dead idea.
3. **Wave 1 in its 61–77% range.** This is expected; wave 1 does not reach 90% by itself. Start wave 2.
4. **Wave 2 below 90%.** Recompute §5.1 from W0.3's cells and open the class with the largest residual:
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
