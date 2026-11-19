# Combat micro (`src/challenger/robot/rat/combat/**`, `robot/king/KingCombatStrategy`)

Audience: AI agents tuning or extending the micro. Unless a path is given, `file:line` refers to the **`.jinja2` template** under `src/challenger/` (for example `moves/TryThrow:362` means `src/challenger/robot/rat/combat/moves/TryThrow.java.jinja2:362`). Hand-written files (`BabyRat.java`, `Squeaker.java`, `KingSpawningStrategy.java`) are cited as `.java`.

Every claim here was checked against the code. Claims marked **(inference)** are interpretations. "Observed" numbers come from the instrumented runs in §13.

Related docs: rules.md (engine truth, especially §4 cooldowns, §8 actions, §9 carry/throw, §10 traps, §12 cats, §16 exploits), architecture.md (turn loop, trackers, comms, §1.6 asserts), codegen.md (Jinja), strategy.md (macro), testing.md (match running).

---

## 0. TL;DR: why this micro wins

1. **The whole micro is built around ratnapping.** An enemy can grab you if it has strictly more HP than you, or if it stands where you can't see it (rules.md §8 `carryRat`). `CombatTracker` precomputes, for every (move, facing) pair, whether any enemy could step next to you and grab you next turn (§4). Every candidate action adds `-100` if it would leave you grabbable. The result: rats approach, bite and grab only when they can't be grabbed back. They also turn to face the enemy (+2 for turning, and facing choices point at the target), so enemies with equal or lower HP can never grab them from the front.
2. **HP ordering is the currency.** Bites cost 2 cheese for 12 damage (`CombatStrategy:440`) instead of 10. Whoever lands the first bite goes ahead in HP, and from then on can grab the opponent while being immune to grabs from its front. The pipeline tries a grab before a bite (`CombatStrategy:236-256`).
3. **Grab, hold, throw for value.** A grabbed enemy is out of the fight (it can't act, and it is immune to bites). From two turns later it is thrown as soon as the line collides at step 2 (42 damage), hits a king or feeds a cat; otherwise the carrier holds it, acts with its free action, and lines up the next throw, up to 8 rounds (§6.1). Before CW, every carry was thrown at two turns and 22% landed in the open; with CW, 6% (L-combat). A grab is worth about a 42-damage hit plus a stun, while a bite is 12.
4. **Move and act in the same turn.** Each candidate is a (move, facing, action-before-or-after-move) triple, ranked by a hand-tuned score. The rat can step in and bite, or bite and back off ("secondary" moves), within the cooldown rules.
5. **Bytecode-dense code generation.** Byte-packed SIMD-style threat counters (8 neighbours per `long`) and fully unrolled per-(move × facing × throw-direction) evaluators fit the whole search into about 10–14k of the 17.5k bytecode limit (§13).

---

## 1. Files and generated-code map

| Template (`src/challenger/...`) | Generated lines | What is unrolled |
|---|---|---|
| `robot/rat/combat/CombatStrategy.java.jinja2` (533) | 523 | Barely templated. Contains the pipeline, the carry decision (hold or throw), `performMoves`, `shouldFight` and bite damage. |
| `robot/rat/combat/CombatTracker.java.jinja2` (462) | 15,960 | 5×5 `t_dx_dy` booleans; `low/high/moveThreat` × 9 moves; `threat{move}{facing}` 9×8 longs; `registerEnemyLow/High`: switch dx(−3..3) × dy(−3..3) × facing(8) → constant masks for 9 moves; `enemyMaskForMove`: 7×7×8×9 = 3,528 constant returns (the largest method, ≈32.6 KB, see codegen.md). |
| `robot/rat/combat/CombatCommon.java.jinja2` (74) | 72 | `canMove{N..NW,C}` booleans |
| `moves/TryThrow.java.jinja2` (768) | 36,930 | 9×9 `occ_dx_dy` occupancy grid (`grid_radius=4`): rats, kings' 3×3 and cats' 2×2. 288 methods: `tryThrow{move9}{throw8}`, `tryThrowSecondary…`, `tryThrowAlly…`, `tryThrowSecondaryAlly…`. Each has a ray scan unrolled at compile time with constant offsets. `markKingZone`: 81 cases × 3×3 writes. |
| `moves/TryTrap.java.jinja2` (459) | 5,203 | 5×5 `w_dx_dy` weights; `applyEnemyWeight` switch dx × dy × facing with the cone test done at template time; `face{F}{M}` / `place{P}{F}{M}` methods; `applyTrapWeights` switch on count (29..1) |
| `moves/TryAttack.java.jinja2` (213) | 1,345 | `selectMoves` switch on `myDir` (8) × 9 ordered moves; `move{X}` (9); `face{F}` (8) |
| `moves/TryRatNap.java.jinja2` (230) | 1,332 | Same shape as TryAttack |
| `moves/TryFight.java.jinja2` (160) | 1,209 | Same shape (no secondary moves) |
| `moves/TryPosition.java.jinja2` (115) | 1,013 | Same shape |
| `robot/king/KingCombatStrategy.java.jinja2` (278) | 664 | `processCell` ring, per-direction score switch, per-move cat-lane terms |

Related helpers (internals are in architecture.md): `RobotTracker.sortedEnemies/sortedAllies/sortedCats` (nearest first by center dist², cats kept separate), `Util.canSense(dx,dy,dir)` (rat cone, r² 20), `Util.canCatSense` (cat cone r² 17, 180°, from the 2×2 centre), `Util.chebyshevDistance(loc, RobotInfo)` (aware of 3×3 kings and 2×2 cats), `Pathfinder.multisourceBfs` (`Pathfinder.java.jinja2:924`), `EnemyManager.knownEnemies` (squeaked enemies), `GlobalArray.nearestRatKing`.

**Shared ordering** (`jinja.py:80-104`, `get_movements(facing)`): moves are evaluated in the order `CENTER, F, F+4(back), F+1, F−1, F+3, F−3, F+2, F−2`, where `F = myDir`. "Wide-related" (primary) moves are `C, F, F±1, F±2`. The non-wide moves `F+4, F±3` are only used as **secondary** moves (act first, then step back). Every comparison is a strict `>`, so **evaluation order is the tie-breaker**. Facing candidates for a move are usually `related_map[dirToTarget]` = {direction to the target, ±45°} (`templates/common.jinja2:4`).

---

## 2. Mechanics that matter for reading the scores

Engine truth is in rules.md. These are only the numbers the scores refer to.

- Cooldowns (rules.md §4): ready when < 10, and each turn subtracts 10. Forward move 10, strafe 18, turn 10, bite 10, carry/throw 20, rat trap 15. Carrying has **no** move penalty (the engine's 1.5× multiplier truncates to 1). Because of the +20 from a grab, **a grab at turn t means the earliest throw is at t+2.** Turn t+1 is always a movement-only "position" turn, which is why POSITION ≈ THROW in §13.
- Bite: `10 + ceil(sqrt(cheese))`. `getCheeseNeeded(k) = (k−1)²+1` (`CombatStrategy:484`) is the minimum cheese that buys +k damage. Kings and cats always get 10 (`BabyRat.java:246`).
- Grab legality: target adjacent and inside **my** 3-tile front cone, and (target HP < mine, or target can't see me, or ally). Enemy kings and cats can't be grabbed.
- A baby rat sees exactly 3 of its 8 neighbours (front and front diagonals). "Hidden" neighbours = the other 5.
- Throw (rules.md §9): flies in the thrower's facing. A collision deals `10 + 4·rem` to **both** the flier and the robot hit: 42 at step 2 on the throw turn (the first tile must be empty, rules.md §9), then 38/34/30/26/22/18 if the flier acts before the thrower, 30/26/22/18 if after. Landing on open ground or at the map edge deals 10. A rat thrown into a cat is **eaten**, and the cat sleeps 2 turns. Throw collisions never trigger a backstab.
- Rat trap: cost 20 (engine), damage 50, movement cooldown set to 30. It triggers when an enemy *enters* any tile of its 3×3.
- Cat: 4,000 HP, scratch 20 damage anywhere in its cone (r² 17, 180°), pounce r² 13 kills every rat on the landing tiles (rules.md §12).

---

## 3. Control flow: entering and leaving combat

### 3.1 Baby rat turn (`robot/rat/BabyRat.java:79-121`)

```
run():
  storeVisibleCells; AntColony.preTick; tryPickUpCheese; tryTransferCheese
  if beingCarried: if hp < 15 and carrier is enemy: rc.disintegrate()   # BabyRat.java:91 (deny the enemy a projectile; inference)
                   return
  if beingThrown: return
  if combatStrategy.runTick():            # combat pre-empts macro entirely
      considerPlacingCatTrap(); return    # cat traps only if coop and no enemies seen (BabyRat.java:36)
  # ---- macro path (combat returned false) ----
  RobotTracker.reset(); CellTracker.checkSymmetry(); updateHistoricalCatLocs()
  considerPlacingCatTrap(); primaryStrategy.runTick()   # MinerStrategy, see strategy.md
  combatStrategy.miniTick()               # opportunistic nap/attack/trap with leftover cooldowns
  executeGlance()                         # ±90° look-around turn
```

### 3.2 `CombatStrategy._runTick` (`CombatStrategy:143-277`): the entry gate

```
if carried or thrown: return false
funnyTarget = null
if not carrying, or carrying an ALLY:                     # :149 (carrying an ENEMY skips the gate: always fight)
    sources = all visible enemies (rats and kings)        # :152
            + visible cats with shouldFight(cat)          # :155
    if no sources and EnemyManager.numKnownEnemies>0 and canTurn:
        funnyTarget = nearest squeaked enemy; sources += it   # :160-172
    if no sources: return false                           # -> macro
    moveMask, bfsIters = Pathfinder.multisourceBfs(sources)   # 11×11 window, ≤6 expansions;
        # moveMask = bitmask of my 8 neighbours on a shortest path toward ANY source.
        # Obstacles: walls, dirt, and RobotTracker.allies. In combat that list only holds the source tiles of
        # the <=5 squeaks Squeaker.preTick processed this turn, NOT the visible allies, because
        # RobotTracker.reset() runs only on the macro path, after combat. Own-king tiles are written to the
        # unused mask localPassableExceptAllies (Pathfinder.java.jinja2:936-945). See B11.
        # 0 = nothing reachable within ~7 steps.
    [anti-glorp "troll", §3.4]                            # :183-199
    if moveMask == 0: return false                        # :200 enemies seen but not reachable -> macro
    if funnyTarget and bfsIters <= 5: turn(toward funnyTarget)   # :201
precomp(): CombatCommon.precomp() (canMove×9); CombatTracker.tick(); computeCellScores() (dead, returns at :349)
isLastMove = true      # suppresses RobotTracker/CellTracker refresh after each move (saves bytecode)
if carrying an ally:                                       # :216
    TryThrow.run() || TryPosition.run(); return true
if carrying an enemy (§6.1): always returns true
    action ready and a legal line: TryThrow.search()
        best >= 42 (a step-2 collision or better), or carry age >= 8, or HP <= 24: throw
        else hold: TryTrap.run() || TryAttack.run() ||
                   TryPosition aimed at the best line, if a tile is safe from grabs || throw
    action not ready: TryThrow.scan() (lines only), TryPosition aimed at the best line
if TryRatNap.run(): return true
if TryTrap.run():   return true
if TryAttack.run(): return true
if TryFight.run(): miniTick(); return true                 # TryFight itself ends with TryAttack.miniRun()
return false       # only reached when TryFight declines (cat-only target with no safe approach)
```

**Exit to macro happens only when** there are no sources, the BFS can't reach any source (`moveMask==0`), the rat is carried or thrown, or TryFight returns false (cat case, which also starts a 10-round cat-fight cooldown, `TryFight:45,63`). In every other case combat owns the whole turn: no macro, no glance. A rat carrying an **ally** with no enemies around falls through to macro while still holding it (it is auto-released after 10 turns).

Note: the BFS sources include **every** visible enemy, even ones `shouldFight` rejects. Rats approach stronger enemies too. `shouldFight` only gates TryAttack.

### 3.3 `shouldFight(enemy)` (`CombatStrategy:453-464`)

```
cat:   true if dist²(cat, nearestOwnKing) <= 36                        # king defence
       else time>500 && allCheese>1000 && no visible enemies && rawCheese==0
            && numberOfRatsAlive>=16 && (id%3==0 || time>=1800)        # 1/3 of rats hunt cats when rich; all after 1800
king:  true
rat:   true if enemy.hp - biteDmg <= myHp        # not losing the HP race by more than one bite
       or enemy can't see me
       or dist²(me, nearestOwnKing) <= 36        # always fight near own king
       or numAllies > numEnemies                 # visible counts (cone-limited)
```

### 3.4 The "glorp" troll (`CombatStrategy:183-199`)

"glorp" is undocumented in the repo. **(inference)** It is probably the name of an opposing team; the commits are "push anti-glrorp" `271c868` and "fix glorp trolling" `a70328e`. The rule applies only when `moveMask==0`, i.e. an enemy is visible but unreachable within the BFS window. It affects 10% of rats (`id%10==7`), at most once per lifetime (`shouldTroll`). Such a rat locks onto the nearest enemy, if that enemy is a baby rat that can see it. While the enemy stays sensable, the rat **returns true and does nothing**. When the enemy is lost, the rat clears the flag forever. The first version forced a fight (`moveMask=all, bfsIters=5`). The "fix" turned it into standing still. Observed once in 12 instrumented matches, so it is effectively inert against these opponents.

### 3.5 King (`robot/king/RatKingStrategy.java:24`)

The king runs `Spawning → KingCombatStrategy → Formation → Mining` **every turn, ignoring return values** (architecture.md §1.2). King combat therefore isn't exclusive. If it doesn't move, mining may move the king, and mining ignores enemies **(inference: it can walk into a fight)**. See §9.

---

## 4. `CombatTracker`: the grab-threat model (`CombatTracker:115-142`)

Computed once per combat turn in `precomp()`. Cost ≈ 3.2k bytecodes median for BFS + tracker (§13).

```
tick():
  loadThreatFromMap()     # t_dx_dy for |dx|,|dy|<=2: "an enemy could stand here" = on map && not a known WALL.
                          #   Unknown cells count as standable; dirt is NOT excluded (TODO at :180). Own cell = true.
  recomputeThreatMasks()  # moveThreat{m}: 8 bytes; byte i = 0xFF iff neighbour i of (me+m) is standable
  low* = high* = 0
  for each visible enemy BABY rat:   registerEnemy(Enemy(info))
  for each squeaked enemy (EnemyManager.knownEnemies, only those not in my vision): registerEnemy(e)
  precomputeThreatCombos()  # threat{m}{f} = (high{m} + (low{m} & hidden{f})) & moveThreat{m}

registerEnemy(e):  dx,dy = e - me; only |dx|,|dy| <= 3 count (switch default ignores the rest)
  bucket = e.hp > myHp ? high : low          # strict: equal HP is "low"
  for each move m (9): bucket{m} += enemy_mask(dx,dy,e.facing,m)
enemy_mask (template-time, :46-67): let P = me+m.
  If the enemy can SEE P (dist² <= 20 and P inside its current 90° cone):
     byte i += 1 for each neighbour i of P with Chebyshev(neighbour_i, enemy) <= 1
     (= cells the enemy can step to, or already occupies, to grab me)
hidden{f} (:104-113): 0xFF in the 5 neighbour bytes outside facing f's front-3.
```

Meaning: `threat{m}{f} != 0` means that after moving `m` and facing `f`, some enemy that currently sees my destination could step onto a standable cell next to me and grab me. A higher-HP enemy can do that from any side; an enemy with equal or lower HP only from behind me (hidden cells).

Query variants (`CombatTracker:308-393`), all wrapped as `getCellScoreFor(...) = hasThreat ? -100 : 0` (`CombatStrategy:366-382`):

| Call | Used by | Semantics |
|---|---|---|
| `hasThreat(m,f)` | TryFight, TryPosition, TryThrow (no enemy hit), TryTrap | raw |
| `hasThreat(m,f,target)` | TryRatNap, TryThrow (enemy hit), TryAttack if my HP ≤ 24 | The target's own contribution is removed (it will be carried or is ignored) |
| `hasThreat(m,f,target,dmg)` | TryAttack if my HP > 24 | If my bite kills the target, its contribution is removed. If the bite drops it from "high" to "low", its contribution is re-added as low (hidden cells only). Otherwise unchanged. |
| `hasAnyThreat(m)` | TryRatNap for **allies** | `(low+high) & moveThreat[m]`. Ignores facing, so it is stricter. |

What the model ignores (all of these are improvement candidates, §14): enemy bite damage and how many enemies can reach you, enemy **king** bites (r² 8), cats (scratch and pounce), enemies that currently face away but can turn, move and grab in one turn, dirt as an obstacle, enemies beyond Chebyshev 3, and allies blocking cells (the masks use terrain only). Squeaked enemies have HP rounded **up** to a bucket (`EnemyManager.java.jinja2:12`: ≤24→24, …, 81–88→88, 91–100→100). They therefore look stronger than they are, which makes the model more conservative.

---

## 5. Sequencing: `performMoves` and primary/secondary

`CombatStrategy.performMoves(moveDir, facing)` (`:295-331`) plays a (move, final facing) pair with the cheapest cooldown use:

```
CENTER:              turn(facing) if needed (needs canTurn)
moveDir==myDir:      move forward (10), then turn if facing!=myDir
moveDir==facing:     turn first, then move forward (10)       # "turn-move": no strafe
facing==myDir:       strafe (18)
else:                strafe, then turn
```

`CombatCommon.movementCost(m,f)` (`CombatCommon:63`) returns −1 (impossible), 0 (strafe) or 1 (forward, turn-move, or CENTER). Candidates with 0 pay `strafeCost()` = `Tune.STRAFE_LO` (0; 3 before FL) if the movement cooldown is < 2, else `Tune.STRAFE_HI` (8) (`CombatCommon:42`). The idea: a strafe from cooldown 0–1 still leaves the next turn's move available, so it costs nothing, while from 2–9 it costs that move.

- **Primary** = move, turn, then act from the new cell (TryAttack/TryRatNap/TryTrap `move{X}` → `face{F}`).
- **Secondary** = act from the current cell, then step backward (`F+4, F±3`) facing `myDir±1`: hit-and-run. TryAttack only allows it if the target is already adjacent (`:92`). TryRatNap requires adjacency and (my HP > target HP, or the target can't see me) (`:105`). TryTrap always allows it. TryThrow's secondary is "throw from here, then move F/F±1".

**Load-bearing asserts.** Assertions are enabled in the engine. `assert(performMoves(...))` performs the move, and a failing assert raises an `AssertionError` that kills the rat (architecture.md §1.6). Observed: `TryThrow` `assert(rc.canThrowRat())` (`TryThrow:106`) killed a carrier on DefaultMedium (§13). Never add side-effecting asserts; consider replacing these with `if (!x) return false;`.

---

## 6. The move generators

### 6.1 TryThrow (`moves/TryThrow`) and the carry decision (`CombatStrategy:216-240`)

```
search(): action ready; budget = min(9000, left-2000)
  populateThrowGrid(): occ 9×9 = visible enemy/ally baby rats; kings mark their full 3×3; cats their 2×2;
                       own nearest king's 3×3 from its shared-array location, seen or not
  carrying ally ? selectMovesAlly() : selectMoves(secondary = true)
  for move m in movement order (all 9), for throwDir d in related_map[myDir] (myDir, ±45°),
      and for m in {C, F} the other 5 directions after those 3:
      tryThrow{m}{d}; and after m==F also tryThrowSecondary{dir}{d} for dir,d in related_map[myDir]
  the best line: md, td, best, isSecondary; the best primary line: aimMove, aimFacing, aimBest
scan(): the same without the action check and without secondaries, for the turn after a grab
execute(): secondary ? (turn d; throw; performMoves(md,d)) : (performMoves(md,d); throw)
run() (ally carries): search; abort if best < 0; execute
```

An enemy carry (`CombatStrategy:216-240`) is thrown when `best ≥ STEP2_HIT` (42: a collision at step 2, a king or a
cat), when the carry is 8 rounds old (`Game.carryStartTime`; the last possible throw is at grab + 10 in either turn
order, and a bite or trap with raw cheese can leave the action unready for a turn), or at HP ≤ 24 (two 12-damage
bites). Otherwise the carrier holds it: TryTrap, then TryAttack, unchanged, act with the free action; else TryPosition
moves to a tile safe from grabs, aimed at the best primary line; with no safe tile, the carrier throws its best line.

Score of one candidate (`tryThrow`). The landing cell `me+m+d` must be free: if it is inside my current 3×3 this is checked with `canMove`, otherwise with sense + occupancy (cats included) + passable. The scan then follows the ray from the post-move cell at steps 2..5. Collision values are the engine's damage when the flier acts after the thrower (6 tiles: 42, 30, 26, 22), the lower bound of the two turn orders. The first cell to hit decides:

| Hit at step s | Score | Stop? |
|---|---|---|
| Unseen cell (`'\0'`) | +0 | stop |
| Wall or dirt | +42, 30, 26, 22 | stop |
| Off map | +10 (normal landing) | stop |
| Own king zone | = −1000 | stop |
| Own rat, s=2 | −36 | stop |
| Own rat, s≥3 | −16, −14, −12 | **continue** (it may move away) |
| Cat | +400: the flier is eaten, a kill, and the cat sleeps 2 turns (above the king) | stop |
| Enemy king zone | +300 | stop |
| Enemy rat, s=2 | +84 (42 to each), sets `enemyHit` | stop |
| Enemy rat, s≥3 | damage/(s−1) = 15.0, 8.67, 5.5 | continue |
| Nothing hit in steps 2–5 | 0 | – |

Then add `getCellScoreFor(m,d[,enemyHit])` (−100 if grab-able after the move with facing `d`; `enemyHit` is excluded from the threat). Cells more than 4 tiles from my current position (step 4 or 5, depending on the move) fall outside the 9×9 occupancy grid, so only terrain is checked there. Lines outside `related_map[myDir]` pass tiles the carrier cannot see: only remembered terrain and the own king's zone are known there.

**Ally throws** (`tryThrowAlly…`): the carried rat is an ally (§6.3). Only step 2 is scanned. The throw is accepted **only** if step 2 holds an enemy rat (+102) or an enemy king zone (+300). Anything else (unseen, wall, ally, dirt, cat, empty, off-map) rejects. The cell score still applies. Note 102−100 = 2 ≥ 0, so **a threatened ally throw is still taken**: 16 of 64 observed ally throws scored exactly 2.0.

### 6.2 TryPosition (`moves/TryPosition`), when carrying and not throwing

```
run(): if !movementReady: return false; search(); execute()
search(): for m in 9 moves, f in related_map[myDir]:
    score = 10·[non-strafe] + 5·[m != CENTER] + (-100 if threat(m,f)) + 2·[f != myDir]
            + aimValue if score >= 0 and (m, f) == (aimMove, aimFacing)
  true when the best score is >= 0 (a tile safe from grabs); the aim is used once
```

The aim is TryThrow's best primary line when it scores > 0: moving m and facing f puts the carrier where that line
starts, so next turn it can throw along f, or step forward along f and throw.

### 6.3 TryRatNap (`moves/TryRatNap`)

```
run(): needs actionReady; budget min(3000,(left-1000)/2)
for i, enemy in nearest 3 sortedEnemies:
    skip if cheb>2, hp <= 10 (RAT_BITE_DAMAGE, "just kill"), not BABY_RAT        # :54-56
    weight=30; selectMoves()
    if i==0 and rc.getCurrentRatCost() >= 30 (>= 8 baby rats alive):           # :62-63  ally sacrifice
        for ally in sortedAllies with cheb<=2, hp<=22, BABY_RAT: weight=10; selectMoves()
move{m} (primary): skip if ally && hasAnyThreat(m)                              # stricter danger for allies
       dest must be adjacent to target; skip if target.hp >= myHp && target sees dest  # legality, :139
       for f in related_map[dirTo(target)] with canSense(target,f):
           score = weight - strafeCost·[strafe] + cell(m,f,target); reject if <= 0.5; +2 if f != myDir
secondary (adjacent and (myHp > hp or unseen)): score = weight + 5 - strafe + cell; reject <= 0.5; +2 turn
accept best >= 0.5; play: primary -> move then carry (fallback: bite);
                          secondary -> carry (fallback: bite) then step back
```

Because the pipeline runs TryRatNap **before** TryTrap and TryAttack, a legal and safe grab always beats a bite. `miniRun` (`:197-229`) grabs any adjacent grabbable enemy among the nearest 3 (hp > bite damage) with no threat check. Its first loop is meant to prefer "a rat looking at us", but `canSense(dx,dy,info.direction)` actually tests whether the enemy is *in front of me along its own facing*, i.e. facing away (comment and code disagree). This only changes priority, since `canCarryRat` decides legality.

Ally-sacrifice rules, all together (the "ally throwing requires ≤ 22 hp and stricter danger requirements" commit `aaba1a7`):
- the ally has ≤ 22 HP and is within Chebyshev 2,
- I have ≥ 8 baby rats,
- the nearest enemy is a grab candidate (the ally loop sits inside the `i==0` iteration, after the `continue` filters),
- no enemy can reach a cell next to my destination at all (`hasAnyThreat`),
- the throw needs an enemy exactly 2 tiles down the line (§6.1).

Observed: 79 ally grabs against 1,469 enemy grabs.

### 6.4 TryTrap (`moves/TryTrap`)

```
run(): actionReady && can place in one of my front-3 cells                      # :59-62,110-124
  shouldDefRun = ratCost >= 70 (>= 24 baby rats)  ||  rawCheese >= 20           # :66  ("trap when many rats"; raw cheese is spent first)
  else need one of nearest 3 enemy rats "strong": (hp>=80 && hp >= myHp+bite) || hp==100     # :71,78-85
  weights w[5×5] (:127-153): -1000 if unsensable/impassable/mine/occupied, else 0
     -100 on each cell within Chebyshev 1 of an existing own rat trap (no overlapping 3×3s)  # :277
     for nearest 5 enemy rats that FACE ME (I'm in their cone, :206):
        cells adjacent to the enemy within its facing ±45°: += (3 - ang) * mult  (straight ahead +3, diagonal +2)
        mult = -1 if !shouldDefRun && enemy not strong  (keep traps for strong enemies)       # :192
  primary: m in wide moves (CENTER generates NOTHING, see bug below), f in related_map[m],
           skip if threat{m}{f}; place p in related_map[f] at me+m+p: score = w * (1.1 if f!=myDir)
  secondary: back moves, f in related_map[myDir], place in related_map[f] from current cell first:
           score = w - strafe + 0, ×1.1 if turning; requires cell score > -100
  accept best > 0.5; place + move in the right order
```

The result is a hidden trap on the cell an approaching enemy is about to step into.

**Bug:** `move{C}` iterates `related_map[dir_ord_map["Direction.CENTER"]]` = `related_map[8]`, which is undefined in Jinja, so the loop is silently empty. Generated `TryTrap.moveC()` is 3 lines that do nothing. Stay-and-place is therefore never considered by `run()`, and only `miniRun` covers it (after a FIGHT move or after macro, `:394-458`). `miniRun` places in a front-3 cell. It uses this turn's weights translated by the move offset when `run()` computed them this turn. Otherwise it zeroes them and **ignores existing traps**. It adds +(4−ang) for adjacent strong enemies (nearest 2) and places when the score is > 0.

### 6.5 TryAttack (`moves/TryAttack`)

```
run(): actionReady; budget min(3000,(left-1000)/2)
for nearest 3 enemies (rats AND kings) with shouldFight && cheb<=2:  weight = 10 (+30 if hp <= biteDmg)
if no target found and cats visible: consider sortedCats[0] (isCat)
primary move m (dest adjacent to target; for cats: dest must be OUTSIDE the cat's vision):
   f in related_map[dirTo(target)] with canSense(dest,target,f):
   score = weight - strafe + 2·[f!=myDir] + (myHp<=24 ? cell(m,f,target) : cell(m,f,target,biteDmg))
secondary (target already adjacent): bite now, then step back; same score terms. The current facing
   is NOT checked to see the target; if it can't, rc.attack throws.
accept best >= 0.5; bite costs getCheeseNeeded(2)=2 cheese when allCheese >= 300 (else 10 dmg, free)
```

- **HP ≤ 24 rule.** At low HP the rat ignores the target's own grab threat (only other enemies count). **(inference)** It is near death anyway, and `BabyRat` disintegrates at < 15 HP when carried.
- **No focus fire.** Targets are scored flat 10, plus 30 for a one-hit kill. Kings get no bonus and no cheese damage. `scoreEnemy()` (`CombatStrategy:467`: king 5, one-tap 3, ≤30 HP 1.3) is dead code.
- **`miniRun`** (`:203-218`) bites the nearest enemy if it is adjacent (it only looks at the first 2 and breaks at the first non-adjacent one), on the tile toward its centre. When `canAttack` refuses that tile (a king sensed through another tile, so the centre's direction is outside the cone), it bites nothing and ends the mini step.

### 6.6 TryFight (`moves/TryFight`): approach and kite, the default move

```
target = nearest visible enemy (rat or king) ; else nearest cat (if not on 10-round cooldown) ; else funnyTarget
for m in 9 moves (only CENTER if movement not ready); cat: dest must be strictly closer to cat centre
   and outside cat vision:
  for f in related_map[dirTo(target)]:
     score = cell(m,f)  [-100 if grab-able]
           + 10·[non-strafe or CENTER] - 5·[target not visible with f] + 30·[m in BFS moveMask]
           + 2·[f != myDir]
performMoves(best); TryAttack.miniRun()   (then CombatStrategy runs miniTick: nap > attack > trap)
cat case: if no legal move -> catFightCooldown=time; return false (macro for 10 rounds)
```

Typical values: a safe forward approach scores 42, holding scores 10–12, a safe sideways step toward the target 32, a threatened anything ≈ −58 to −88. Rats therefore advance to the edge of grab range and hold there. When even holding is threatened, they back off while still facing the target (≈ +2), which is the kiting behaviour. CENTER never gets the +30 (moveMask has bits 0–7 only). If no candidate is legal (can't move, can't turn, target not within ±45° of facing), `md==null` and `performMoves(null,…)` throws an NPE. That was observed once; the exception is swallowed and the turn lost.

---

## 7. Interaction with cats, enemy kings and own king defence

**Cats** (no cat term exists in `CombatTracker`):
- Rats engage cats only via `shouldFight(cat)`: the cat is within dist² 36 of our nearest king, or the late/rich hunting rule (§3.3).
- TryFight and TryAttack approach and bite **only from cells outside the cat's vision cone** (`canCatSense`). The cat's AI targets rats it sees, so this keeps biters out of its targeting **(inference)**.
- With enemy rats around, nothing keeps rats away from cats. The combat BFS (`multisourceBfs`) doesn't use the cat-avoidance masks that normal pathing uses (`Pathfinder.java.jinja2:678-725`).
- **TryThrow feeds enemies to cats.** A cat's 2×2 is in the occupancy grid: on the step-1 tile it blocks the throw, on the line at step ≥ 2 it scores 400 (the flier is eaten and the cat sleeps 2 turns), and ally throws never go into a cat (§6.1).
- Outside combat code: panic squeaks lure cats away from the king. A rat that saw a cat in the last 4 turns squeaks a no-op if the cat is within dist² 25 of our nearest king (`Squeaker.java:172-178`). Otherwise rats near cats stay silent. `BabyRat.considerPlacingCatTrap` (`BabyRat.java:34-77`) only runs in cooperation with no enemies known.

**Enemy kings:**
- They are BFS sources and TryFight targets, so rats swarm them.
- TryAttack always fights them (weight 10, 10 damage, no cheese).
- TryThrow gives +300 for a throw that hits an enemy king's 3×3; 3.5% of observed enemy throws.
- Kings can't be grabbed, and they are not registered as threats. Their bite (10 damage, r² 8) is ignored.
- **No coordinated king focus.** Cheese is never spent on king bites.

**Own king defence:**
- `shouldFight` returns true for every enemy when I'm within dist² 36 of our king, and for cats within 36 of the king.
- TryThrow never throws into our king's zone (−1000).
- Spawning aims new rats at the nearest visible enemy, facing it. After damage it allows panic spawns with a +10/+20 cheese threshold (`KingSpawningStrategy.java:38-71`). Note that its "≤1 neighbour" filter is dead: `senseNearbyRobots(loc,2,enemyTeam)` never returns our own team, so `cnt` is always 0 and it targets the nearest enemy.
- Measured (CC+CK, main against awubot on `faceoff` ∪ `L-combat`; `docs/closed.md`):
  - 70% of awubot's throws into our king (2.6 of 3.7 a game) fly one of our rats spawned in the last 10 rounds, grabbed
    next to the king that spawned it toward the enemy. Spawning on the far side instead cut those to 0.85, but doubled
    the bites our king took (13 → 26 a game) and lost 10 points: near-side spawns hold the ring tiles enemies bite from.
  - Of awubot's ~12 rescues a game (it grabs our carrier, which drops its victim), 1.2 come after a hold of 3 rounds or
    more: the exposure is the two rounds between our grab and the earliest throw, not CW's longer hold.
  - A rat on an enemy carrier's line near our king takes the throw in the king's place: keeping rats off carriers'
    step-2 tiles cut awubot's double hits on them by 2 a game and raised the throw HP into our king by 13–16.

**Cooperation/backstab:** combat code never checks `rc.isCooperation()`. The first bite or grab on an enemy starts the backstab. Observed first backstab at rounds 3–34 in every instrumented game. The strategic consequences are covered in strategy.md and rules.md §2.

---

## 8. Other combat-related details

- **Bite damage** `getBiteDamage()` = 12 if raw+global cheese ≥ 300, else 10 (`CombatStrategy:440`). This damage is also used by `shouldFight`, the TryAttack one-tap test and `hasThreat(...,dmg)`, `TryTrap.shouldTrap`, and `TryRatNap.miniRun`. `TryRatNap.run` uses the constant 10 instead (`:55`), so enemies at 11–12 HP get grabbed rather than killed.
- **Bytecode budgets** (§10 table): each `Try*` stops evaluating when it exceeds its budget. Evaluation order then matters: early candidates (CENTER, forward) are always seen.
- **Dead code:** `computeCellScores` (`CombatStrategy:346-364`, short-circuited by `if (1 == 1) return;` at :349), `getCellScoreForEnemy` (`:385-438`), `readCellScore`/`cacheCellScore` (`:334-344`), `scoreEnemy`, `canEnemyReach`, `getNearestTarget`, `trapCost`, `RAT_TRAP_COST/CAP`, `CombatTracker.GRID_RADIUS`/`isThreatAt`, and the `for_related`/`for_wide_related` macros. `getCellScoreForEnemy` contains an older, richer cell model (vision escape ±4, danger distance, HP ratio) that can be revived.
- **Exceptions seen** (instrumented, 12 matches): one TryFight NPE; two TryAttack primary bites that failed because `rawMove` → `tryTransferCheese` used the action cooldown first; Pathfinder turning without `canTurn` (macro); `KingSpawningStrategy` `rc.turn(CENTER)`. Each costs 500 bytecodes and the rest of the turn.

---

## 9. KingCombatStrategy (`robot/king/KingCombatStrategy`)

```
runTick() (:81-226): if no visible enemies and no cats: return false
  score[8] = 0
  processCell ring (:102-117): if a cell on the leading edge of a 3×3 move is impassable or occupied:
                               -1000 for the moves that need it; dirt: -2
  for each enemy (rat w=1, king w=3):                 # :118-142, no distance weighting
      d = dirTo(enemy): score[d] -= 3w; score[d±90°] += w; score[d±135°, d+180°] += w+1
  for each visible cat:                               # :143-188
      the same shape with w=5, on dirTo(cat's bottom-left tile)
      if the king's 3×3 is in the cat's forward lane (laneCross, :246-253), for each move:
          +30 if it leaves the lane; else +15 / -15 if it gets nearer to / farther from an open exit
          (laneMargin :256-261); plus (move · facing), i.e. -2..+2 along the lane
  move = argmax(score) over dirs with score > -1 (else CENTER); ties -> lowest ordinal (N first)   # :189-201
  digDirtInDirection(move)  (uses the action!) ; rawMove if canMove
  cats ? placeDefensiveDirt() : digSurroundingDirt()   # :202-206: dirt toward a cat within dist² 20 /
                                                       # dig ring dirt if global cheese >= 100
  if actionReady: bite the lowest-HP enemy within dist² 8, else the nearest cat (biteCat :229-239);
                  10 damage, no cheese; a bite on a cat is no backstab
```

**The cat lane.** An EXPLORE cat walks its 2×2 along its facing. A king in the way blocks it; the cat scratches the
blocking king every 3 rounds and, since a scratch counts as acting, never turns or pounces away (rules.md §12). Fleeing
along the lane loses: the cat moves 1 tile per 2 rounds, the king 1 per 4 (theory/t4-maps.md §2.4). So the king steps
across the lane.
- Geometry (`laneCross`), in doubled coordinates: r = 2·(king centre − cat centre), f = the cat's facing
  (`RobotInfo.direction`). The king is ahead iff r·f > 0. Its 3×3 meets the lane iff |r×f| ≤ w, with w = 3 for a
  cardinal facing and 6 for a diagonal one. `laneCross` returns c = r×f, or `NOT_IN_LANE`.
- A king step m changes c by 2·(m×f): ±2 per step across a cardinal facing, ±4 per diagonal step across a diagonal one.
  The lane is left at |c| > w.
- Exits: the two sides across the lane, along ±(fy, −fx). A side is open only if every 3×3 on the steps needed to
  clear the lane is known and passable (`isWalkable`, :264-269, via `CellTracker.isPassable3x3`). When neither side is
  open (a strip or pocket), only the along-lane term scores, which is main's flee.
- `entersCatLane(dir)` (:271-277) is the same test for the king's position after one step; mining uses it
  (strategy.md §6.2).

**Measured** (K1, against main with RB; `~/battlecode26-data/k1/k1diag.py`):
- `L-cat`, 394 games (`1119-004136-ab-4e63`):
  - scratch HP on our kings: lane block −37%, cone (ATTACK) +9%;
  - king deaths with a scratch in their last 10 rounds 70 vs 132, all king deaths 199 vs 235;
  - enemy bite HP on our kings +24%.
- `goal`, 190 games (`1119-004140-ab-a698`): lane block −57%, cone +24%, bite HP equal.
- Limits:
  - On pocket maps (tminusten, bed) no exit exists, and bug-nav can walk the 3×3 king into dead ends that only 1×1 rats
    pass.
  - The exit side ignores enemies beyond their direction terms.
  - A king that leaves the lane can still be scratched from the cone when the cat reaches a nearby waypoint.

---

## 10. Tunable constants (exhaustive)

Sensitivity: **H** = changes outcomes in most fights, **M** = situational, **L** = rare or minor. These are best guesses **(inference)** unless noted.

### Gate and global

| Name / expression | Value | Location | Meaning | Sens. |
|---|---|---|---|---|
| BFS depth / window | ≤6 expansions, 11×11 | `Pathfinder.java.jinja2:946-975` | Combat is entered only if an enemy or fightable cat is reachable within ≈7 steps | M |
| funny-target turn | `bfsIters <= 5` | `CombatStrategy:201` | Turn toward a squeaked enemy | L |
| troll selector | `id % 10 == 7`, once per lifetime | `CombatStrategy:183` | §3.4 | L |
| threat penalty | −100 (`Tune.THREAT_PENALTY`) | `CombatStrategy.getCellScoreFor` | Grab-threat cost, binary; every choice is the same above ~50 (the positive scores sum to ≤ 47) | **H, measured** (TN2 s5): 40 costs about 1.2 pp (+0.60 [+0.07, +1.14] per +60) |
| bite damage | 10 if allCheese < 300 (`Tune.BITE_BONUS_CHEESE`) else 12 | `CombatStrategy.getBiteDamage` | Damage and cheese per bite (2 cheese for +2) | H; the 300 flat (TN): −0.07 [−0.54, +0.44] pp per +150 (strategy.md §9) |
| cheese per +k damage | `(k−1)²+1` | `CombatStrategy:484-488` | Exact engine minimum, do not change | – |
| shouldFight: cat near king | dist²(cat, king) ≤ 36 | `CombatStrategy:455` | King defence vs cats | M |
| shouldFight: cat hunt | time>500, allCheese>1000, numEnemies==0, raw==0, rats≥16, id%3==0 or time≥1800 | `CombatStrategy:456` | Proactive cat damage (points) | M |
| shouldFight: HP race | `enemy.hp − bite ≤ myHp` | `CombatStrategy:459` | Fight if not behind by more than one bite | H |
| shouldFight: near king | dist²(me, king) ≤ 36 | `CombatStrategy:461` | Always defend near our king | M |
| shouldFight: numbers | numAllies > numEnemies (visible) | `CombatStrategy:462` | Fight when outnumbering | M |
| strafe cost | `Tune.STRAFE_LO` 0 if movement cooldown < 2, else `Tune.STRAFE_HI` 8 | `CombatCommon:43` | Penalty for 18-cooldown moves | M; FL: 3 → 0 +0.017 [+0.006, +0.028] (3,000 fresh `goal` cells); `STRAFE_HI` flat (+0.22 pp per +4) |
| low-HP desperation | myHp ≤ 24 | `moves/TryAttack:154,177` | Ignore the target's own grab threat | M |
| carried suicide | hp < 15 and carrier is enemy | `BabyRat.java:91` | Disintegrate while held | L |

### CombatTracker

| Name | Value | Location | Meaning | Sens. |
|---|---|---|---|---|
| enemy register window | dx,dy ∈ [−3,3] | `CombatTracker:256,283,417` | Threats beyond Chebyshev 3 ignored | M |
| enemy vision for threat | dist² ≤ 20 and cone | `CombatTracker:51-53` | An enemy must currently see my destination | H |
| high/low split | `enemy.hp > myHp` | `CombatTracker:246` | Strict, mirrors the engine's grab rule | – |
| standable cell | `mapData != '\2'` (wall) | `CombatTracker:201-203` | Unknown and dirt count as standable | L |
| squeaked HP buckets | 24,26,…,80,88,90,100 (rounded up) | `comms/EnemyManager.java.jinja2:12` | Pessimistic HP of squeaked enemies | L |
| squeak view radius | Chebyshev 4 | `EnemyManager.java.jinja2:18` | Squeaked enemies are relative to the sender | L |

### TryThrow

| Name | Value | Location | Meaning | Sens. |
|---|---|---|---|---|
| budget | min(9000, left−2000) | `moves/TryThrow` `start()` | | L |
| occupancy grid radius | 4 | `grid_radius` | Rats and cats beyond ±4 invisible to the scan | L |
| scan steps | s = 2..5 | `hit_damage` | Ray length considered | M |
| wall or dirt hit | 42, 30, 26, 22 (`hit_damage`) | | Engine damage, flier after the thrower (rules.md §9) | H |
| off-map | +10 | | Normal landing | L |
| own king | −1000 | | Never | – |
| own rat at s=2 | −36 | | Friendly collision | M |
| own rat s≥3 | −16/−14/−12, continue | | | L |
| cat | +400 (`feed_score`) | | A feed: a kill, ranked above the king | M |
| enemy king | +300 | | King hit (the actual damage is ≤ 42) | M |
| enemy rat at s=2 | 2·42 = 84 | | Double collision | H |
| enemy rat s≥3 | damage/(s−1) | | Discounted because it may move | L |
| throw threshold | `STEP2_HIT` = 42 | `CombatStrategy` carry branch | Below it an enemy is held | H |
| carry deadline | age ≥ 8 rounds | `CombatStrategy.mustThrow` | Last throw at grab + 10 | M |
| carrier HP floor | ≤ 24 | `CombatStrategy.mustThrow` | Two 12-damage bites | L |
| ally scan | step 2 only; enemy rat 102 / king 300 | `tryThrowAlly…` | | M |
| throw directions | `related_map[myDir]`; all 8 for moves C and F | `move{X}` | Search width | M |

### TryPosition

| Name | Value | Location | Sens. |
|---|---|---|---|
| non-strafe | +10 | `moves/TryPosition:83` | L |
| moving | +5 | `:84` | L |
| turning | +2 | `:87` | L |
| aim | + TryThrow's best primary line score, safe tiles only | `face{F}` | M |
| budget | min(3000, left−1000) | `search()` | L |

### TryRatNap

| Name | Value | Location | Meaning | Sens. |
|---|---|---|---|---|
| enemy weight | 30 (`Tune.RATNAP_W`) | `moves/TryRatNap` | Grab value; shifts every candidate alike, so it matters only below ~9 (strafes) or the ally's 10 | **H, measured** (TN2 s5): 5 costs about 2.3 pp (+1.17 [+0.59, +1.79] per +25) |
| ally weight | 10 | `:70` | Ally-sacrifice grab | M |
| candidates | nearest 3 enemies, cheb ≤ 2 | `:50,54` | | M |
| "just kill" skip | hp ≤ 10 (`RAT_BITE_DAMAGE`) | `:55` | Should arguably be `getBiteDamage()` | L |
| ally gating | ratCost ≥ 30 (≥ 8 rats); ally hp ≤ 22; cheb ≤ 2 | `:63,67,68` | | M |
| ally danger | `hasAnyThreat(move)` | `:134,180` | Stricter safety | M |
| secondary bonus | +5 | `:181` | Prefer grab-and-retreat | M |
| min score | > 0.5 before the turn bonus; accept ≥ 0.5 | `:162,184,77` | | L |
| turning | +2 | `:164,186` | | L |
| budget | min(3000,(left−1000)/2) | `:43` | | L |

### TryTrap

| Name | Value | Location | Meaning | Sens. |
|---|---|---|---|---|
| global trap mode | ratCost ≥ 70 (≥ 24 rats) or raw ≥ 20 (`Tune.TRAP_RAW`) | `moves/TryTrap.globalShouldRun` | Trap any approaching enemy | M; the 20 not confirmed by TN (strategy.md §9) |
| "strong" enemy | (hp ≥ 80 and hp ≥ myHp+bite) or hp == 100 | `:71` | Otherwise trap only for these | M |
| early-exit scan | nearest 3 | `:79` | | L |
| weights scan | nearest 5 enemy rats | `:164` | | L |
| weak-enemy mult | −1 | `:192` | Avoid trapping weak enemies | L |
| cone weight | +3 straight, +2 diagonal (`3−ang`, ang ≤ 1) | `:212-217` | Where the enemy will step | M |
| trap spacing | −100 within Chebyshev 1 of own trap | `:277` | | L |
| blocked cell | −1000 | `:136,140` | | – |
| turn multiplier | ×1.1 | `:336,379` | | L |
| accept | best > 0.5 | `:97` | | L |
| miniRun | nearest 2 adjacent, +(4−ang) for ang ≤ 3, accept > 0 | `:411-449` | | L |
| budget | min(5000,(left−1000)/2) | `:93` | | L |

### TryAttack / TryFight

| Name | Value | Location | Meaning | Sens. |
|---|---|---|---|---|
| attack weight | 10 | `moves/TryAttack:83` | | M |
| one-tap bonus | +30 if hp ≤ bite | `:84-85` | Kill priority | M |
| candidates | nearest 3 enemies, cheb ≤ 2; cat only if no enemy | `:48,53,80` | | M |
| turning | +2 | `:152,175` | | L |
| accept | ≥ 0.5 | `:62` | | L |
| miniRun | nearest 2, adjacent | `:202-204` | | L |
| fight non-strafe | +10 | `moves/TryFight:132` | | M |
| fight facing | −5 if target not visible | `:135` | | M |
| fight BFS step | +30 if move ∈ moveMask (`Tune.FIGHT_STEP`) | `moves/TryFight` | Approach drive; above ~17 an approach always wins | not confirmed (TN2): 10 read +1.3 pp in s5 (−0.64 [−1.40, +0.04] per +20), then −0.006 [−0.026, +0.014] against 30 on fresh seeds (s7) |
| fight turning | +2 | `:142` | | L |
| cat-fight cooldown | 10 rounds | `:45` | | L |
| cat approach | strictly closer and outside the cat cone | `:102,109` | | M |
| budget | left−1500 | `:59` | | L |

### King

| Name | Value | Location | Sens. |
|---|---|---|---|
| threat weights | rat 1, king 3 (the `CAT -> 5` case is dead: `sortedEnemies` holds no cats) | `KingCombatStrategy:122-124` | M |
| direction shape | −3w toward, +w at ±90°, +(w+1) at ±135°/180° | `:130-135` | M |
| cat flee | w = 5 in the same shape, toward the cat's bottom-left tile | `:150-155` | M |
| cat lane width | \|r×f\| ≤ 3 (cardinal facing), ≤ 6 (diagonal), ahead r·f > 0, doubled coordinates | `:246-253` | – (rules) |
| lane exit / nearer an open exit / farther | +30 / +15 / −15 | `:179-183` | M |
| along the lane | + (move · facing), −2..+2 | `:185` | L |
| open exit | every 3×3 on the steps to clear the lane known and passable | `:170-172,264-269` | L |
| blocked / dirt | −1000 / −2 | `:92,97` | L |
| default best | −1 (CENTER if nothing > −1) | `:190` | L |
| defensive dirt | cat dist² < 20; skip if ≥ 2 dirt in 3×3; closest to cat | `:22,45,47` | M |
| ring dig | global cheese ≥ 100 | `:71` | L |
| king bite range | dist² ≤ 8, lowest-HP enemy; else the nearest cat's first attackable tile | `:213-222,229-239` | L |
| panic spawn | +10 threshold (+10 more if HP ≤ 100), every > 4 rounds | `KingSpawningStrategy.java:59-66` | M |
| cat lure squeak | cat dist² < 25 to king | `Squeaker.java:174` | M |

---

## 11. Changing the generated code safely

- Edit the `.jinja2` file, then run `python3 jinja.py` from the repo root. It compiles all `challenger` templates once and skips `MapTracker` unless `--include-maptracker` is passed (codegen.md). Never hand-edit the generated `.java`.
- **To change a scalar weight**, edit the literal in the template. It is usually emitted once per unrolled copy (e.g. TryThrow's `score += 102;` appears 288 times in the generated file), so one template edit updates them all.
- **Template-time constants:**
  - `{{ rem * 4 }}` and `{{ 2 * (10 + 4 * rem) + 10 }}` are folded by Jinja.
  - `/` is float division (`12.666666666666666` in Java); use `intDiv` for integer division.
  - Template loops over `range(9,5,-1)` define the ray length.
  - `grid_radius` (TryThrow:21) sizes the occupancy grid. Raising it grows `markKingZone` quadratically.
- **Undefined Jinja lookups are silent.** `related_map[8]` (CENTER) yields an empty loop. This is how `TryTrap.moveC` ended up empty. Loop over `wide_related_map_center`/`moveDirs` or special-case CENTER.
- **Per-direction methods take no arguments**; state lives in static fields (`moveDir`, `newLoc`, `info`, `t`, `weight`, `score`). A new term must be set before `selectMoves()` or computed inside `face{F}`.
- **New threat information:**
  - extend `registerEnemy*` or add a parallel `long` per move (one byte per neighbour), then fold it into `precomputeThreatCombos`;
  - keep per-byte counts below 256;
  - the `hasThreat(…,target[,dmg])` subtraction assumes the target's mask was added exactly once.
- **Method size.** `CombatTracker.enemyMaskForMove` is already ≈32.6 KB, half the JVM's 64 KB method limit (codegen.md). Widening its window from ±3 to ±4 roughly doubles it **(inference: it would likely still compile, but check)**.
- **Bytecode.** Combat turns already end at a median of 9–12k and a p90 of 13–14k of the 17.5k limit, with a max of 17k (§13). Each `Try*` has its own budget and returns its best-so-far, so heavier scoring silently shrinks the search. The candidates evaluated last are the first to disappear: back moves, ±2 side moves and secondary throws.
- **Ordering is the tie-breaker** (strict `>`). Reordering `try_ord` in `jinja.py` changes behaviour everywhere.

---

## 12. Verified quirks and bugs (fix candidates)

| # | Where | Issue | Impact |
|---|---|---|---|
| B1 | `TryThrow` | **Fixed (S1, CW).** Throw-target legality ignored cats for non-adjacent landing cells, and the failing `assert(rc.canThrowRat())` killed the rat. Cats are now in the occupancy grid and a failed check skips the throw | Observed once in 12 games (DefaultMedium, r159) |
| B2 | `TryTrap:357-361` | `moveC()` is empty (`related_map[8]`): stay-and-place is never scored | Fewer and worse traps |
| B3 | `KingCombatStrategy:116-117` | **Fixed (K1).** The ring cells for N/NW were `(2,-2)` and `(1,-2)`; they are `(-2,2)` and `(-1,2)` | King escape errors |
| B4 | `TryFight:70` | `md==null` → NPE | Rare, turn lost |
| B5 | `TryAttack:169-197` | Secondary bite doesn't check that the current facing sees the target | Possible exception |
| B6 | `TryRatNap:55` | Uses 10 instead of `getBiteDamage()` (12) | 11–12 HP enemies are grabbed instead of killed |
| B7 | `TryRatNap:206-207` | "Looking at us" test is inverted (priority only) | L |
| B8 | `KingSpawningStrategy.java:44-46` | Neighbour count always 0 | L |
| B9 | `TryThrow` | **Fixed (CW).** Dirt collisions scored without the +10 that walls got; both now score the engine's damage | L |
| B10 | `BabyRat.java:249` via `TryAttack:69-70` | A move can trigger `tryTransferCheese`, which uses the action before the bite | Observed 2× |
| B11 | `Pathfinder.java.jinja2:936-945` | In the combat BFS, visible allies are not obstacles. `RobotTracker.allyCount` is reset to 0 in `updateRobots` (preTick). Before combat runs, only `Squeaker.processMessage` refills it, adding each squeak's source tile (`Squeaker.java:88`). The full ally list comes from `RobotTracker.reset()`, which `BabyRat.run` calls only after combat (`BabyRat.java:109`). Own-king tiles are cleared in the wrong mask. So `moveMask` can point through ally blobs | **(inference)** Rats queue behind allies instead of flanking; TryFight's +30 goes to blocked cells |

---

## 13. Observations (instrumented runs, 2026-11-18)

**Method.** I copied `src/challenger` to `/tmp/bc26-combat/src/challengerdbg` (package renamed with sed) and added `System.out.println` at each pipeline outcome. Note that `Class.getSimpleName` is forbidden by the sandbox, so use `e.toString()`. I compiled with `javac -cp battlecode26-java-1.2.5.jar` into `/tmp/bc26-combat/classes` and ran `battlecode.server.Main` directly with `-Dbc.game.team-*.url=/tmp/bc26-combat/classes`, which avoids gradle lock contention. `src/` was not touched. Script: `/tmp/bc26-combat/scripts/run_match.sh`; analyzer: `/tmp/bc26-combat/scripts/analyze.py`.

**Results** (A vs B; "dbg" = instrumented challenger):

| Match | Map | Winner (round) |
|---|---|---|
| challenger vs awubot_quals | micro1 | challenger (483) |
| challenger vs awubot_quals / awubot_quals vs challenger | microtest | **awubot_quals both sides** (169 / 165) |
| awubot_quals vs challenger | micro2 | challenger (196) |
| dbg vs awubot_quals | micro2, pvparena, knifefight, DefaultMedium, whatsthecatdoin | dbg (313 or 456 depending on the instrumentation variant; 147, 562, 175, 516) |
| challenger vs awubot_us / awubot_us vs challenger | knifefight | **awubot_us (100) / challenger (124)** |
| challenger vs awubot / awubot vs challenger (mirror) | microtest | **B both times** (142) |
| challenger vs awubot / awubot vs challenger (mirror) | pvparena | **A both times** (132) |
| dbg vs awubot | thunderdome; pvparena_mines | dbg (87); dbg (2000, points) |

- **Outcomes are chaotic and side-dependent.** Mirror matches are decided by the side. Adding printlns alone changed micro1 from a win at r483 to one at r428, and flipped knifefight vs awubot_us from a loss at r100 to a win at r93. Evaluate across both sides × many maps × several opponents; a single micro map proves nothing.
- awubot_quals beats the final bot on `microtest` from both sides. That is a concrete regression lead (it might be macro rather than micro: not investigated).

**Action mix** (12 dbg matches, combat turns):

| Outcome | Turns |
|---|---|
| FIGHT | 10,347 |
| ATTACK | 1,629 |
| RATNAP | 1,541 |
| THROW | 1,321 |
| POSITION | 1,279 |
| TRAP | 483 (+291 via miniTick) |
| CARRYIDLE | 226 |
| MINI nap / attack | 31 / 22 |

- Grabs are about as frequent as bites. Grabs: 1,469 enemy, 79 ally.
- **Enemy throw scores** (n=1,266):

| Score | Meaning | Share |
|---|---|---|
| 34–42 | wall or dirt | 40.8% |
| 102–299 | enemy rat at step 2 | 29.1% |
| 0 | nothing hit; enemy lands for 10 damage | 16.1% |
| 12.7–34 | | 5.2% |
| ≥ 300 | enemy king | 3.5% |
| (0, 12.7] | | 2.5% |
| < 0 | threatened, thrown anyway | 2.0% |

- Ally throws: 48 scored 102 and 16 scored 2 (threatened).
- **Standoff.** On micro maps, 104–180 FIGHT turns per match had the action ready and an enemy at Chebyshev 2 but no safe attack (`TryAttack.bestScore = −1`). Only 8–14 per match had an *adjacent* enemy with no attack, and the post-move `miniRun` bit in most of those. The micro is "advance to grab range edge, wait for a safe opening", not passive.
- **Bytecode.** Combat entry happens at ≈3.4k used (median; p90 4.0k). The BFS + tracker precomp takes 3.2k median (p90 5.4k).

| Decision | Median bytecode | p90 | Max |
|---|---|---|---|
| FIGHT | 10.2–10.9k | 13.6–14.2k | 17.0k |
| THROW | 10.9–12.4k | 13.3–13.9k | 14.9k |
| ATTACK | ≈10k | | |
| RATNAP | ≈9k | | |
| POSITION | 7.5k | | |

There were no overruns.
- Cats were visible in only 0–5% of combat turns on micro maps (they are mostly catless). Use DefaultMedium, whatsthecatdoin, knifefight or cat-heavy macro maps to test cat logic. On whatsthecatdoin, 239 ATTACK turns targeted cats.

---

## 14. Improvement surface (ranked hypotheses)

Each item lists the claim, then the evidence, then how to test it. The order is expected value / effort **(inference)**.

**H1. Feed enemies to cats and make throws cat-aware.** Done (CW, §6.1).
- Evidence: TryThrow has no cat term (§6.1, `TryThrow:258-293`). A thrown rat that hits a cat dies outright and the cat sleeps 2 turns (rules.md §9). That would replace a 34–42-damage throw with a 100-HP kill. B1 shows that a cat on the throw tile currently kills the carrier (failed `assert(rc.canThrowRat())`).
- Implementation: mark the cat's 2×2 in the occupancy grid as "cat" and score it high (≥ the +300 king value). Treat cat-occupied landing cells as illegal. Have TryPosition carry toward cats when one is within ~4 tiles.
- Test on cat maps, against awubot, both sides.

**H2. Goal-directed carrying (TryPosition).** Done (CW): the aim of §6.2, with the hold of §6.1.
- Evidence: every grab forces one movement-only turn (+20 carry cooldown, §2). POSITION ≈ THROW (1,279 vs 1,321). The move has no objective (§6.2). 16% of enemy throws hit nothing (0 points) and 2% are threatened.
- Implementation: score positions by the best next-turn throw from that cell. That could be a cheap version of TryThrow's scan: nearest wall, enemy or king in 3 directions. Alternatives are moving toward allies so the drop lands among them, or toward cats.
- Watch the bytecode: POSITION turns use the least (7.5k median), so there is room.

**H3. All 8 throw directions.** Done (CW) for moves C and F.
- Evidence: TryThrow only considers `myDir±45°` (`:166`), but a rat may turn to any direction before throwing.
- Implementation: extend to 8 directions only for `m ∈ {C, F}`, or prefilter with a cheap wall/enemy-in-line check. The budget is 9k and THROW decisions sit at a p90 of ≈13.9k total.

**H4. Use cheese on bites (dynamic damage).**
- Evidence: bites are fixed at 12 for 2 cheese. The marginal costs are 13 dmg = 5 cheese, 14 = 10, 15 = 17. `hasThreat(m,f,target,dmg)` already models HP-order flips, so a larger `dmg` directly unlocks safe bites (high → low flip), more one-taps (the +30 bonus) and more grabs next turn. Rich games (thresholds in `KingSpawningStrategy`) bank thousands of cheese.
- Implementation: per candidate, choose the minimal damage that flips or kills, capped by a cheese budget. Note that `attack()` already converts damage to cheese.
- Test: A/B in long games.

**H5. A richer danger model.**
- Evidence: §4 lists what is ignored. Most importantly, (a) the number of enemies able to bite me next turn (damage race), (b) enemy king bites, (c) cat scratch and pounce cells, and (d) enemies facing away that can turn and grab.
- Implementation: add a graded penalty (e.g. −8 per enemy able to reach adjacency and see me after its turn) instead of the binary −100 only. The masks already contain the counts (bytes of `low+high`). A popcount of the bytes is cheap.
- The revived `getCellScoreForEnemy` (dead code, §8) shows an earlier attempt.
- Risk: more conservative → fewer first bites. Tune against standoff counts.

**H6. Fix B2 (trap stay-and-place) and the miniRun trap-spacing gap.**
- Implementation: a template one-liner. Test the trap counts and win rate.

**H7. Target selection and focus fire.**
- Evidence: flat weights (§6.5), and grab weights are equal regardless of the target. Prefer:
  - bites on the lowest effective HP,
  - bites that flip a target to "low" relative to an adjacent ally (lets the ally grab it),
  - bites on kings when they are low or allies are many,
  - grabs on full-HP enemies (low ones die to bites),
  - grabs that have a good next-turn throw (H2).
- `scoreEnemy` is a ready hook.

**H8. King micro fixes.**
- Distance-weighted threat (every visible enemy counts equally, and the king sees r² 25). B3 is fixed (K1, §9).
- `digDirtInDirection` spends the action before the king's bite.
- The king doesn't bite cats.
- Mining may move the king toward enemies when combat holds still (§3.5).

**H9. Ally-sacrifice throws are rare (79 grabs).**
- Possible changes: relax HP ≤ 22 (e.g. HP ≤ 30), scan steps 2–3 with a discount, and reconsider accepting threatened throws at 102−100.
- **(Speculative)** Rules.md §16.5: ally grabs and throws never trigger a backstab. Ally throws into enemy rats or kings are therefore possible **during cooperation** without breaking it.

**H10. Small fixes: the anti-glorp troll, the "just kill" constant (B6) and the BFS obstacles (B11).** The first two are low impact; remove them if neutral. For B11, call `RobotTracker.reset()` (or a cheap ally-only pass) before `multisourceBfs` so flanking routes appear, and measure the bytecode cost.

**H11. Investigate the `microtest` loss to awubot_quals** (both sides, §13). Diff the quals combat templates: TryThrow differs by 528 lines and TryTrap by 250.

**H12. Joint search.** `src/awubot_mystery/robot/rat/combat/moves/TryAllCombat.java.jinja2` is an abandoned unified generator that scores nap, attack and fight per (move, facing) in one pass. It was not used in the final bot, and its effect is unknown. A joint search would also allow "move + nap + reposition" and "throw then bite" combos that the current sequential pipeline can't express.

---

## 15. Quick reference: per-turn decision for a baby rat in combat

```
[gate] sources? reachable within BFS? (else macro)
precomp: canMove×9, CombatTracker masks
carrying? -> ally: TryThrow (step-2 enemy or king only) -> else TryPosition
            enemy: throw if best line >= 42, age >= 8 or HP <= 24; else hold: TryTrap, TryAttack,
                   TryPosition aimed at the best line (throw if no tile is safe)
else:
  TryRatNap   weight 30 (enemy) / 10 (ally ≤22hp)  - strafe + (-100 if grab-able, target excluded)   >= 0.5
  TryTrap     only if (≥24 rats | raw≥20 | strong enemy near); hidden trap on the enemy's next step   > 0.5
  TryAttack   10 (+30 one-tap) - strafe + 2·turn + (-100 if grab-able after the bite)                 >= 0.5
  TryFight    always: approach along BFS (+30), stay non-strafe (+10), face target (-5 if not), avoid grab (-100)
              then miniRun bite / miniTick (nap > bite > trap) with the leftover action
```
