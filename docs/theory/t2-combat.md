# T2 Combat: exchange math, dominant tactics, gaps

Lens T2 of track T (docs/program.md): bite, grab, throw and trap exchanges, HP races, cooldowns, cones,
engagement, kings, cats. Each claim carries its source:

- **E:** engine source, paths relative to `engine/src/main/battlecode/` at tag `engine.1.2.6`
  (clone: `~/battlecode26-data/engine/ref/battlecode26`). The `world/` code of 1.2.5, 1.2.6 and 1.2.7.private2
  differs only in `GameWorld.spawnCheese` (`git diff engine.1.2.5 engine.1.2.6 -- engine/src/main/battlecode/world/`;
  `engine.1.2.6..engine.1.2.7.private2` changes no `world/` source).
- **C:** champion code, `src/awubot/...`, `.jinja2` template line numbers; challenger is identical today.
- **O:** `src/opp_obg/...` (Old But Gold). **G:** `src/opp_gst/...` (GST).
- **R:** counts from the replays of the 24 validation games OBG/GST vs awubot (DefaultSmall, DefaultMedium,
  DefaultLarge, arrows, cheesefarm, keepout; both seats; engine 1.2.5, seed 0; TheHeist excluded, no contact).
  Tools and outputs: §13.
- **A:** my arithmetic or geometry, from the rules.

## 0. Headline

The dominant combat behaviour:

1. **Fight with traps and carries, not bites.** A bite deals 10–13. A rat trap deals 50 plus 3 turns without
   movement, for 20 cheese and an action that costs no tempo when no bite is possible; against these bots it
   fires 93–98% of the time (R). A throw that collides at step 2 deals 42 to the victim, and 42 to what it hits.
   In the 24 games, rat traps dealt 3–5× and throws 1.7–2.7× the damage of all bites; bites were 13–15% (§2.4).
2. **Fights are decided at Chebyshev distance 2.** Hold there on a diagonal, facing the enemy, and trap the tile
   between: every move the enemy can make except a retreat enters the trap's 3×3 (§2.4). awubot holds exactly
   there, cannot see traps, and traps only behind a gate.
3. **Carry, keep the action, throw for value.** The carrier can still bite (E: carrying does not block
   `attack`). Hold the victim, bite with the free action, and throw only into a cat (a kill), the enemy king, a
   second enemy at step 2, or a wall at step 2, or when the carry timer runs out. awubot throws at exactly t+2
   in 95% of carries, 22% of them into open ground (10 damage), and almost never bites while carrying. OBG lands
   3% in the open, collides at step 2 80% of the time and bites ~23 times per game while carrying (R).
4. **HP order is permanent** (no healing in the engine). With equal per-bite damage the first biter wins a frontal
   race by one bite and nobody can grab. A +1 edge (13 against awubot's fixed 12, 5 cheese instead of 2) wins
   every isolated frontal race from either seat. With the initiative, it also gives a frontal grab at the second
   exchange (§2.1).
5. **Local numbers are super-linear.** Two attackers on one rat produce a grab within a round, by pincer or
   bite-then-grab (§1.3). Contact cannot be left cheaply: a retreat that keeps facing the enemy is a strafe, 5
   tiles per 9 rounds against the pursuer's 9. So commit only with ≥ 2 on 1.
6. **Kings die to throws as much as to bites.** Thrown-rat collisions dealt 22% of the damage taken by the 28
   kings that died in the 24 games, three quarters as much as all bites, and are thrown from outside the king's
   bite ring (§4.2).
7. **awubot's TryThrow `AssertionError` kills the carrier.** It fired in 15/70 validation games, ≈ 0.26 per game.
   The cause is a cat on the throw's first tile: 5 of 5 instrumented failures (§9.1). The dead carrier also drops
   its victim unharmed.

Top packages (§11): **P3** trap-first engagement, **P2** hold-and-bite carrying with value-gated, cat- and
king-aware throws (P1, the assert fix, comes first), **P4** minimal-cheese bites.

---

## 1. Primitives

### 1.1 Per-round budget

| item | value | source |
|---|---|---|
| readiness | counter < 10; −10 at each own turn, floor 0; frozen while carried, thrown or (cat) asleep | E: InternalRobot.java:1162-1170 |
| forward move, bite, turn | cost 10: once per round each, all three in one turn, any order | rules §4, §8 |
| strafe (move ≠ facing) | cost 18: counter 0→18→8→26→16→6→… gives 5 strafes per 9 rounds | A |
| grab, throw, drop | cost 20: next action 2 turns later | rules §4 |
| rat trap | cost 15: next action next turn (15→5) | A |
| raw cheese c | move and action costs ×(1+0.01c), truncated. Forward and bite are free up to c = 9, strafe up to c = 5. At c = 50: 15/27/15; at c = 100: 20/36/20. Bite rate = 10/cost per round | E: InternalRobot.java:479-517 |
| carrying | no penalty: `(int)1.5 == 1` | rules §4 |
| trap victim | movement counter **set** to 30, then the move cost is added: 40 or 48, so no move for 3 turns. Action and turn are unaffected | E: GameWorld.java:627-651, RobotControllerImpl.java:886-913 |

### 1.2 Damage menu

| source | damage | cheese | action cost | notes | source |
|---|---|---|---|---|---|
| bite (rat or king) | 10 + ⌈√c⌉, no cap | c, raw first then global | 10×(1+0.01·raw) | a king reaches its whole 16-tile ring | E: InternalRobot.java:580-627; RobotControllerImpl.java:1148-1156 |
| rat trap | 50 to each enemy that enters its 3×3, plus the stun above | 20 | 15 | invisible to enemies; fires once; several traps entered at once all fire | E: GameWorld.java:627-651; RobotControllerImpl.java:864-883 |
| throw collision | table below, to the flier **and** the robot hit (kings included) | 0 | 20 after the grab's 20 | no backstab | E: InternalRobot.java:780-813 |
| throw landing (end of flight or map edge) | 10 to the flier, +10 to its three counters | | | | E: InternalRobot.java:754-778 |
| throw into a cat | the flier dies (all its HP), the cat sleeps 2 turns | | | | E: InternalRobot.java:815-845 |
| cat scratch | 20 | | | | rules §12 |
| king starvation | 10 per king turn while global cheese < 2 | | | | rules §3 |

Throw collision damage by flight step (step 1 is the tile in front of the thrower):

| step | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|---|---|---|---|---|---|---|---|
| flier acts before the thrower ("early", 8 tiles) | never | 42 | 38 | 34 | 30 | 26 | 22 | 18 |
| flier acts after the thrower ("late", 6 tiles) | never | 42 | 30 | 26 | 22 | 18 | – | – |

Derivation (E):
- `getThrown` sets remaining = 4 and flies steps 1 and 2 at once (InternalRobot.java:718-729).
- Collision damage is `10 + 4·(2·remaining + (first step of the pair ? 1 : 0))` (:780-783).
- Later pairs fly at the flier's own turns, after a decrement. A flier thrown earlier in the same round skips that
  round's flight (:1143-1160).
- **Step 1 never collides**: `assertCanThrowRat` requires that tile on the map, passable and free of robots
  (RobotControllerImpl.java:1393-1409). So **46 never happens** and the most a throw deals is 42.

This corrects rules.md §9 and §15, combat.md §2 and §6.1, and T5's "up to 46"; awubot's scan (`rem` 9..6) scores
4 too high at every step (C: TryThrow.java.jinja2:319-376). R: of 4,195 thrown rats, none took 46; all 2,657
step-2 collisions dealt 42; later collisions took 38/34/30/26/22/18 only.

### 1.3 Grab legality and the 90° cone

Legality (E: RobotControllerImpl.java:1460-1536):
- the grabber is an action-ready baby rat that is not carrying;
- the target is adjacent and inside the grabber's cone;
- the target is a baby rat and not flying;
- the target cannot see the grabber, or is an ally, or has **strictly** less HP;
- the grabber did not just release this same enemy (2 of the target's turns).

A baby rat sees 3 of its 8 neighbours (rules §7). Lemmas (A). "E" is an enemy whose move, turn and action are
all ready, with free tiles around:

- **L1, orthogonal adjacency is exposed.** E at (0,1) can reach 5 tiles adjacent to me (W, NW, N, NE, E). My cone
  covers 3 consecutive directions. E turns to face me, strafes onto an uncovered tile and grabs, all in one turn.
  This needs E's strafe (18) and turn.
- **L2, diagonal contact and distance 2 are coverable.** E at (1,1) reaches only N, NE, E. From (2,0) it reaches
  NE, E, SE; from (2,1), E and NE; from (2,2), NE. If I face E, an enemy with HP ≤ mine cannot grab me next turn.
- **L3, pincer.** Two enemies whose reachable sets together span more than 3 consecutive directions make me
  grabbable, whatever my facing and HP. Example: (2,0) and (0,2) reach {SE, E, NE} ∪ {NE, N, NW}. Knight-offset
  pairs such as (2,1) and (1,2) do not: {E, NE} ∪ {N, NE}.
- **L4, bite-then-grab.** HP order is checked at the grabber's turn, after earlier actors have acted. Two of us
  adjacent to one enemy, all at equal HP: whichever of us acts first bites, and the other grabs from the front.
  This works in either turn order.
- **L5, no healing.** Every `addHealth` call in `world/` is negative; only king formation sets HP
  (RobotControllerImpl.java:1227-1279). A damaged rat can be grabbed from the front, for the rest of its life,
  by any enemy with more HP. A fresh spawn (100 HP) can grab any damaged enemy in its cone.

### 1.4 Carry timeline (grab at round t)

- **Carrier.** The grab adds 20 to its action counter, so its action is ready again at t+2. It moves at full
  speed. It can bite, trap or dig while carrying: `assertIsActionReady` blocks only robots that are carried or
  thrown (E: RobotControllerImpl.java:717-727).
- **Victim.**
  - Its counters are frozen; it cannot be bitten and is invisible to grid sensing.
  - Timer 10, decremented at each of its turns. At the 11th it is released onto carrier tile + **its own**
    facing; if that tile is blocked it **swaps** and becomes the carrier (E: InternalRobot.java:1116-1141,
    :659-683).
  - The carrier's last safe throw is at round t+9. t+10 is the last possible one in either turn order (A).
- **If the carrier dies,** the victim is dropped on its tile and traps there fire (E: GameWorld.java:1145-1148).
- **If the carrier is grabbed,** the victim is dropped: this is a rescue (E: InternalRobot.java:685-700).
- **Throwing** needs the action ready and the step-1 tile in the facing direction on the map, passable and free
  of robots, cats included.

### 1.5 Turn order

Robots act in spawn order, so the order of two rats is fixed for life (rules §3). Within a pair, the earlier rat
holds the initiative at distance 2: it can step into contact and act before the other can respond. A bot cannot
read an enemy's slot from the API.

---

## 2. Exchange math

### 2.1 The frontal duel (A)

Two rats at 100 HP, in diagonal contact, facing each other (no positional grab, by L2).

Minimum cheese to kill 100 HP in k bites (each bite costs (d−11)²+1 cheese for d ≥ 11; mixes are cheapest because
the cost is convex):

| bites k | 10 | 9 | 8 | 7 | 6 | 5 |
|---|---|---|---|---|---|---|
| damage mix | 10×10 | 8×11 + 12 | 4×13 + 4×12 | 5×14 + 2×15 | 4×17 + 2×16 | 5×20 |
| cheese | 0 | 10 | 28 | 84 | 200 | 410 |

Bite races (alternating; "first" acts first in the round):

| first (dmg) | second (dmg) | winner | survivor HP | frontal grab possible? |
|---|---|---|---|---|
| A (12) | B (12) | A, 9th bite | 4 | never: HP is equal at each grabber's turn |
| A (12) | B (13) | **B**, 8th bite, round 8 | 4 | never |
| A (13) | B (12) | A, round 8 | 16 | yes: at A's turn in round 2, 88 vs 87 |
| A (12) | B (10) | A, round 9 | 20 | yes: at A's turn in round 2, 90 vs 88 |

Consequences:
- At equal damage, the initiative wins by one bite and no HP grab ever becomes legal in a 1v1.
- A +1 per-bite edge beats the initiative. awubot bites 12 when its raw + global cheese is ≥ 300, otherwise 10;
  kings and cats always get 10 (C: CombatStrategy.java.jinja2:440-443; BabyRat.java:245-250). Biting 13 against
  it costs 28 cheese per kill (+18 over the 12-only 18), to win a duel worth one rat each way.
- **Kill now.** A target at h HP (h > 10) dies to one bite costing (h−11)²+1 cheese: 1, 2, 5, 10, 17 for
  h = 11..15. awubot pays 2 for 12 even when 13 would kill (h = 13 needs 5).

### 2.2 Grab versus bite (A, over the carrier's turns)

| plan | carrier's actions | victim out of action | damage to the victim | damage the victim deals us |
|---|---|---|---|---|
| 4 bites (t..t+3) | 4 bites | 0 | 48 | 48 |
| grab, throw at t+2 (awubot) | grab, throw | carried 2 + flight 1–3 + landing stun 1–2 ≈ 4–6 turns | E[throw] ≈ 34 | ≈ 0–12 in the window |
| grab, hold, bite others, throw by t+9 | grab, up to 7 bites, throw | ≈ 10–13 turns | E[throw] ≈ 41 + up to 84 to others | 0 |

E[throw] uses the outcome mixes of §2.3: awubot 0.22·10 + 0.585·42 + 0.14·30 + 0.055·60 ≈ 34; OBG
0.03·10 + 0.80·42 + 0.11·30 + 0.06·60 ≈ 41 (60 HP assumed for a fed victim).

- A grab is worth about +100 HP-equivalents over a bite exchange.
- Holding adds about +80 more when other enemies are in reach: the extra bites, minus the bites an early-thrown
  victim delivers after it lands.
- The cost is the carrier's exposure: if it dies, the victim drops free.
- An awubot victim under 15 HP disintegrates while carried (C: BabyRat.java:89-95; T5 §3.2 A10). That denies the
  throw, but it is still a kill.

### 2.3 Throw choice, measured (R)

| | throws | open landing (10) | step-2 collision (42) | later collision | fed to a cat | thrown exactly 2 rounds after the grab | bites while carrying (12 games) |
|---|---|---|---|---|---|---|---|
| awubot vs GST | 1,160 | 22% | 58% | 14% | 7% | 95% | 19 |
| GST | 634 | 13% | 53% | 21% | 13% | 44% | 250 |
| awubot vs OBG | 1,285 | 22% | 59% | 14% | 4% | 95% | 14 |
| OBG | 1,116 | 3% | 80% | 11% | 6% | 55% (mean hold 7.5) | 272 |

"Fed" counts thrown rats that died with no damage record: the engine kills a rat thrown into a cat without one
(E: InternalRobot.java:825-832). awubot's throw grid has no cats (C: TryThrow.java.jinja2:258-293), so its 4–7%
of feeds are accidents. GST scores a cat at 200, above any collision (G: Utilities.java:1144-1175).

Dominant ranking (A):
1. cat: a kill, the victim's whole HP;
2. enemy king at step 2: 42 to the king, thrown from outside its bite ring;
3. enemy rat at step 2: 42 + 42;
4. wall or dirt at step 2: 42 (dirt and walls deal the same, both `!isPassable`, E: InternalRobot.java:833-834);
5. later collisions: ≤ 38;
6. open landing: 10.

"Keep holding" beats items 4–6 while the carrier has a target to bite and time on the timer. awubot scores dirt
10 below walls (C: TryThrow.java.jinja2:374 vs :333).

### 2.4 Traps

Counts (R, 12 games per pair):

| | awubot vs GST | GST | awubot vs OBG | OBG |
|---|---|---|---|---|
| rat traps placed | 755 | 1,541 | 761 | 1,045 |
| traps walked into (triggering team) | 1,469 | 702 | 1,027 | 747 |
| share of own traps that hit an enemy | 93% | 95% | 98% | 98% |

Damage by source against awubot's rats (R; bites at ≤ 12, collision damage to second robots not recorded):
- GST: 1,267 bites (≤ 15k HP), 1,469 trap hits (73k HP), 634 throws (~25k HP to the victims).
- OBG: 1,409 bites (≤ 17k HP), 1,027 trap hits (51k HP), 1,116 throws (~46k HP to the victims).

Traps and throws carry the fight; bites are 13–15% of this damage.

Value (A):
- 20 cheese buys 0.95 × 50 = 47.5 HP plus 3 turns without movement, about 2.4 HP per cheese. A bite's bonus
  costs at least 1 cheese per HP (+2 for 2).
- When no bite is possible the action is otherwise idle, and 15 leaves the next turn ready.

**The pin.** Me at (0,0), enemy at (2,0), trap on the tile between, (1,0). The trap's 3×3 (x ∈ [0,2], y ∈ [−1,1])
contains the enemy's tile and every move it can make except a retreat to x = 3. Stepping in or sideways costs
−50 and leaves it immobile for 3 turns. Diagonal case: me (0,0), enemy (2,2), trap (1,1): same result. awubot
approaches along its BFS to Chebyshev 2 and steps in when an attack is safe (C: TryFight.java.jinja2:132-142;
combat.md §6.6). It has no trap model; enemy traps are invisible (rules §10).

Combos:
- **Overlap.** Two traps whose areas share the tile an enemy enters deal 100 at once: damage stacks, the stun
  does not. That is a full-HP kill for 40 cheese (E: RobotControllerImpl.java:864-883 fires every trigger).
  awubot's placement adds −100 within Chebyshev 1 of its own traps (C: TryTrap.java.jinja2:270-282), so an
  overlap needs traps at Chebyshev 2 sharing the approach tile.
- **Trap, then grab.** The victim is left at 50 HP, so any of our rats above 50 HP can grab it from the front (L5).
  It cannot move for 3 turns, so it cannot reach a blind side or retreat. Trap 50 + throw 42 = 92, then one bite.

Cost:
- GST spent ≈ 2,570 cheese per game on traps (1,541 × 20 / 12); awubot spent ≈ 1,260.
- awubot floats 700–1,850 cheese for long stretches at its rat cap (strategy.md §10 item 1). Traps are a use for
  idle cheese, but they compete with T1's spawn packages for the same float.

The three trap policies:
- **awubot.** Traps only if its rat cost is ≥ 70 (≥ 24 rats) or raw ≥ 20; otherwise only against a "strong" enemy
  (HP ≥ 80 and ≥ its own HP + bite, or exactly 100). It places only on cells adjacent to an enemy that faces it
  (C: TryTrap.java.jinja2:64-90; combat.md §6.4).
- **GST.** Traps whenever the nearest enemy is at d² 3–8 and raw ≥ 20 or all cheese > 600
  (G: BabyRatBehaviors/BRMicro.java:484-497; Utilities.java:1620-1650), plus traps on the enemy-facing side of
  mines (Utilities.java:1695; BabyRat.java:251-253).
- **OBG.** Traps toward an enemy at d² 3–8 under a cheese gate and when the enemy has ≥ 50 HP
  (O: MicroManager.java:100-136; `MicroInfo.shouldPutTrap`).

### 2.5 Raw cheese in combat (A)

- A rat with c raw cheese bites and moves at (int)(10·(1+0.01c)): 2 bites per 3 rounds at 50 raw, 1 per 2 at 100
  (rules §4).
- Its raw cheese drops on its tile when it dies (E: GameWorld.java:1159-1162). Pickup has no cooldown, so the
  killer collects it.
- Bite cheese is spent raw first, and the cooldown uses the raw amount before spending (rules §4). A laden rat
  that dumps c−9 raw into one bite fights at full speed afterwards and denies the drop.
- Enemy raw cheese is visible (`RobotInfo.cheeseAmount`).
- awubot ignores raw cheese in targeting and engagement; it only gates traps on raw ≥ 20 (combat.md §6.4–6.5).
  OBG retreats above 20 raw (O: MicroManager.java:20, :247).

### 2.6 Group fights (A)

L3 and L4 make local numbers super-linear. A rat facing two attackers is grabbable within a round, and a grabbed
rat is out of the fight for 4–13 turns (§2.2). Each of n attackers can reach the target's ring of 8, so contacts
grow with local numbers squared in the open. The side that brings 2 onto 1 first takes the rat; 1v1 trades at
the front gain nothing for the side without the initiative.

---

## 3. Engagement and disengagement (A)

| distance at the end of my turn | what the enemy can do next turn | rule |
|---|---|---|
| Chebyshev ≥ 3 | nothing to me | safe; approach on the diagonal |
| 2 | step in and bite or grab, acting first | **the trap band.** Trap the tile between, face the enemy and hold. Step in only (a) with ≥ 2 on 1, (b) against a trapped, stunned or weaker target, or (c) with a grab on arrival |
| 1, diagonal, facing it | bite only (L2), unless it has more HP or a partner (L3, L4) | contact: bite, grab, or trap the others |
| 1, orthogonal | grab me from my blind side (L1) | never end a turn here against a ready enemy, unless it is grabbed this turn |

**Leaving contact.**
- A retreat that keeps facing the enemy is a strafe at 18: 5 tiles per 9 rounds against the pursuer's 9.
- Turning my back and running forward matches its speed, but exposes my back to a grab regardless of HP.
- At equal speeds, escape works only if I act before the pursuer in the round or start at distance ≥ 3.

So the decision that matters is whether to engage: commit with local superiority (≥ 2 on 1, an HP lead, traps
laid, near our king), otherwise hold at 2–3.

**awubot's gate.** `shouldFight` fights any enemy that is not ahead by more than one bite, cannot see it, or is
within d² 36 of its king, or when visible allies outnumber visible enemies
(C: CombatStrategy.java.jinja2:453-464). Its combat BFS approaches every visible enemy regardless
(combat.md §3.2). Its only safety term is a binary −100 on grab exposure (C: CombatTracker.java.jinja2:46-67).

---

## 4. Fighting kings

### 4.1 Mechanics (rules §6–8; A)

- A king has 600 HP and a 3×3 body. It does not heal, cannot be grabbed, bites one target per round anywhere in
  its 16-tile ring (dist² ≤ 8) and moves once per 4 rounds.
- It spawns only on a free ring tile (rules §8). Every ring tile is adjacent to the king, so our rats on the ring
  both bite it and block its spawns: up to 16 bites per round.
- **Kill time** at 12 per bite: 600/(12n) rounds, i.e. 12.5 rounds for n = 4, 8.3 for 6, 6.3 for 8. awubot's king
  answers with 10 per round on the lowest-HP enemy, with no cheese (combat.md §9): one dead attacker per 10 rounds.
- **Finishing with cheese.** Killing the last king ends the game, so cheese left over is worth nothing. Example:
  6 attackers, 600 HP, 36 bites at 17 (37 cheese each, 1,332 total) kill in 6 rounds instead of 8–9.
- **Throws.** A carrier at Chebyshev 3 from the king's centre is outside its bite ring. Throwing toward the king
  hits a king tile at step 2: 42 to the king and 42 to the thrown enemy rat. awubot scores an enemy-king-zone hit
  +300 (C: TryThrow.java.jinja2:358), but only inside its 9×9 grid, and it never carries toward a king.

### 4.2 What killed kings (R)

28 dead kings, 16,800 HP, split from per-king-turn HP deltas:

| source | HP | share |
|---|---|---|
| bites (counted at 10) | 4,950 | 29% |
| starvation-like: −10 with no bite on that king | 2,620, plus up to 980 in mixed steps | 16–21% |
| thrown-rat collisions (42, 38 … 18) | 3,660 | 22% |
| −20 steps; cat scratch is the only 20-damage source, but double ticks are possible | 2,680 | 16% |
| cheese bites above 10 (11–13) | 570 | 3% |
| traps (50) | ≈ 260 | 2% |

Throws into kings did three quarters as much as all bites. Starvation, an economy loss (T1), did about a sixth.

### 4.3 Defence

- Fresh spawns next to our king (100 HP) can grab any damaged attacker from the front (L5). awubot panic-spawns
  toward the threat (strategy.md §5.2).
- Our king's bites with cheese at kill or flip thresholds (§2.1) cost it nothing extra. awubot's king never spends
  cheese, and `digDirtInDirection` can use its action first (combat.md §9).
- Traps around our king pin attackers (§2.4). A trap at Chebyshev 3 from the king's centre covers the approach
  to the ring.

---

## 5. Cats as weapons (rules §9, §12; R)

- A thrown rat that meets a cat at any step ≥ 2 dies outright, and the cat sleeps 2 cat turns
  (E: InternalRobot.java:825-832). It is the only one-action kill of a healthy rat. A cat on step 1 blocks the
  throw, because `assertCanThrowRat` counts it as a robot.
- awubot's throw grid has no cats, so cat tiles look like open floor. Its lines through cats feed by accident
  (4–7% of its throws, R), and a cat on the step-1 tile kills the thrower (§9.1).
- **Carrying toward a cat.** A carrier moves at full speed for up to 9 turns. A cat walks at most 1 tile per 2
  rounds and is 2×2. A cat within reach of a throw line (up to 8 tiles) is a target.
- Luring and steering cats with squeaks or target order belong to T4/T5. The combat-relevant facts: in ATTACK a
  cat targets the last robot in its scan order, either team, kings included, and scratches 20 per 3 rounds
  (rules §12, §16).

---

## 6. The dominant local policy

Per rat turn, in priority order (A):

1. **Invariant: end the turn not grabbable.** That means not orthogonally adjacent to a ready enemy (L1), not
   inside a pincer (L3), not where a bite-then-grab works (L4), not on the step-2 tiles of an enemy carrier whose
   action is ready (§10), and not inside a cat's scratch cone or pounce reach.
2. **Carrying an enemy.** Throw if the best line ends in a cat, the enemy king, an enemy rat at step 2 or a wall
   at step 2. Also throw if the timer is in its last two turns, or the carrier will die or be grabbed this round.
   Otherwise hold: bite or trap with the action, and move toward the best line (cat > king > enemy cluster > wall).
3. **A legal and safe grab** (HP lead, blind side, trapped or stunned target, or as the partner in a
   bite-then-grab): grab.
4. **Enemy at Chebyshev 2** and cheese above the floor: trap the tile between. When one trap is already down,
   overlap a second on the enemy's approach.
5. **Bite with the minimal cheese** that kills now, flips the HP order for my or a partner's grab next turn, or
   keeps a won race (13 against a 12-biter).
6. **Move.** Hold at distance 2 on the diagonal, facing the enemy. Advance only with ≥ 2 on 1 or against a
   trapped, stunned or weaker target. Flank to build a pincer.

Targeting: grabbable now > killable now > trapped or stunned > most raw cheese (slow, drops loot) > lowest HP.
Near the enemy king: throws into it > ring tiles > cheese-boosted finishing bites.

---

## 7. Where awubot deviates

| # | dominant | awubot | pointer | evidence |
|---|---|---|---|---|
| D1 | hold the victim, bite with the free action, throw for value or at the deadline | throws at the first ready turn (t+2) whatever the score; never bites while carrying; TryPosition has no goal | C: CombatStrategy.java.jinja2:216-231; TryThrow.java.jinja2:91-95; TryPosition.java.jinja2:83-87 | R: 95% of carries thrown at exactly 2 rounds, 22% open landings; 14–19 bites while carrying in 12 games vs 250–272 for GST/OBG |
| D2 | cats in the throw scan: illegal at step 1, a kill at step ≥ 2 | cats absent from the occupancy grid; a failed `assert(rc.canThrowRat())` kills the carrier | C: TryThrow.java.jinja2:258-293, :101, :106 | §9.1: 5/5 instrumented failures had a cat on step 1 |
| D3 | correct throw values: 42 at step 2, then 38/34… or 30/26…; dirt = wall | scores 46/42/38/34, and dirt 10 below walls | C: TryThrow.java.jinja2:319-376 | E and R, §1.2 |
| D4 | trap the gap at distance 2 whenever affordable; allow overlaps | gated (≥ 24 rats, raw ≥ 20, or a "strong" enemy); only cells next to an enemy that faces it; −100 within Chebyshev 1 of its own traps | C: TryTrap.java.jinja2:64-90, :270-282 | R: places 0.5–0.7× the traps of GST and OBG; walks into 1.4–2.1× the traps it lands |
| D5 | minimal-cheese bites (kill, flip, race) | 12 for 2 cheese at ≥ 300 total, else 10; always 10 on kings and cats | C: CombatStrategy.java.jinja2:440-443; BabyRat.java:245-250 | A, §2.1 |
| D6 | threat = every enemy that can reach and face me, combos and cats included | binary −100 from enemies that currently see my destination; no combos, cats or king bites | C: CombatTracker.java.jinja2:46-67, :245-252; combat.md §4 | A, §1.3 |
| D7 | raw-cheese-aware engagement and targeting | none | combat.md §6.5 | A, §2.5 |
| D8 | throw into the enemy king from outside its ring; finish with cheese; our king bites with cheese | +300 only when the king's zone is in its scan, no carry toward it; no cheese on kings; its own king always bites 10; B3 ring bug | C: TryThrow.java.jinja2:358; BabyRat.java:245-250; combat.md §9 | R, §4.2 |
| D9 | a move or turn must not spend the action planned for that turn | `rawMove` and `turn` call `tryTransferCheese`, which can use the action before the planned bite or throw | C: BabyRat.java:179-213 | combat.md B10 (observed twice on bites) |

What awubot does right, to keep: the grab-exposure model as a hard constraint; grab before bite; facing the target;
move-plus-act sequencing with hit-and-run secondaries; ally-sacrifice throws (low-HP allies into an enemy at step
2); a per-(move, facing) search that fits the bytecode budget (combat.md §0).

## 8. Where OBG and GST deviate

**OBG** (O):
- **Carrying matches D1.** It grabs when legal and otherwise bites, including while carrying
  (MyUnit.java:97-160). It holds until a collision is available, accepting "nothing hit" only in the last 2 carry
  turns (ThrowManager.java:17, :32).
- **Throw ranking** (ThrowManager.java:8-15, CollisionData.java): enemy king 7 > enemy at step 2 6 > cat 5 >
  wall 4 > enemy 3 > nothing 2 > ally 1 > own king 0. A feed (a kill) is worth more than a step-2 hit, so the
  cat is ranked too low (A).
- **Traps** toward enemies at d² 3–8 (MicroManager.java:100-136).
- **Bite cheese** at HP breakpoints, but overpaid: 1/4/9 for +1/+2/+3 where the minimum is 1/2/5
  (MyUnit.java:113-150).
- **Safety tiers** add 11 per extra enemy within reach to the enemy's max HP (`MicroInfo.safe`), an approximation
  of L4.

**GST** (G):
- **Throws** when the score is ≥ 15, after ≥ 8 carry turns, or at HP ≤ 20 (Utilities.java:1022-1049). It scores
  a cat at 200 and bites while carrying (BabyRatBehaviors/BRMicro.java:480-508).
- **Traps** about 2× as often as awubot, under the d² 3–8 rule and on mines (§2.4).
- **Not dominant:** 13% open landings and 21% later collisions, so its throws are weaker than OBG's. Its king
  starved in its losses (R: 15–52 starvation-like steps on GST kings).

---

## 9. Bugs that decide games

### 9.1 awubot's TryThrow `AssertionError` kills the carrier: exact cause and frequency

**Path** (C, E):
1. TryThrow checks the throw's step-1 tile against its occupancy grid (rats and kings only) and `isPassable`,
   which is true under a cat. This happens whenever the tile is not both adjacent and movement-ready: primary
   throws at TryThrow.java.jinja2:305-315, secondary throws at :410-417.
2. A cat on that tile passes both checks. `canThrowRat` then fails, because the engine counts any robot there
   (E: RobotControllerImpl.java:1405).
3. `assert(rc.canThrowRat())` (:101 secondary, :106 primary) throws an `AssertionError`, which escapes
   `catch (Exception)`. `run()` exits, `SandboxedRobotPlayer` marks the robot terminated (E:
   instrumenter/SandboxedRobotPlayer.java:234-236), and `GameWorld.updateRobot` destroys it at the end of that
   turn (E: GameWorld.java:354-359).
4. `destroyRobot` drops the carried victim on the carrier's tile, unharmed apart from any traps there, and drops
   the carrier's raw cheese (E: GameWorld.java:1145-1162).

**Confirmed by instrumentation.** I ran a scratch copy of awubot, `awudbg`, with only a diagnostic print added
before each of the two asserts, in 4 games. There were 5 failures, all in the cheesefarm mirror. In every one
the action was ready, raw cheese was 0, and a **CAT** stood on the step-1 tile (3 primary, 2 secondary)
(`~/battlecode26-data/theory/t2/runs/throwfail.txt`).

**A second, theoretical path.** `rawMove` and `turn` call `tryTransferCheese` (C: BabyRat.java:179-213). If the
pre-throw move or turn brings an own-king tile into transfer range while raw > 0, the action is spent and the
assert fails. It was not seen in the 5 samples, because the start-of-turn transfer (BabyRat.java:88) pre-empts
most cases.

**Frequency.**
- 18 asserts in 70 validation games (16 at `TryThrow.java:156`, primary; 2 at `:151`, secondary) = 0.26 per
  game. Poisson P(≥ 1) = 1 − e^−0.26 = 23%, against 15/70 = 21% observed.
- At ≈ 100 awubot throws per game (R: 2,445 over 24 games), that is ≈ 1 in 400 throws.
- By map: cheesefarm 7/18 games, keepout 3/4, arrows 1/4 (4 events), DefaultLarge 1/4, DefaultSmall 2/18,
  DefaultMedium 1/18. These are fights near cats.

**Cost per event.** One dead rat (100 HP, 10–60 cheese to replace, its raw cheese dropped), plus a lost throw
(≈ 34–42 HP that the victim never takes), with the victim back in play. challenger runs the same code. The fix
is P1.

### 9.2 `IndexOutOfBoundsException` from `rc.move()` (1.2.5, and 1.2.6 too)

- **Cause** (E: RobotControllerImpl.java:864-883; GameWorld.java:1145-1148). `processTrapsAtLocation` walks the
  tile's trigger list by index. If the mover dies to a trap while carrying a rat, `destroyRobot` drops that rat on
  the same tile and runs its trap processing. That can remove more traps from the same list, and the outer loop
  then indexes past the end.
- **When.** A carrier dies on entering a tile whose trigger list also holds a trap that fires on its cargo, for
  instance an enemy carrying our rat into an area covered by both our trap and theirs.
- **Effect.** The exception reaches a robot that is already dead, and the rest of that move's traps are not
  processed. It is harmless to the mover's team.
- The trap code is identical in 1.2.6 and 1.2.7.private2 (§0 source note). In the validation logs it appeared
  only in Powerpuff moves.

---

## 10. The flight-squeak hypothesis (opponents agent): mostly refuted

- **Mechanism (E: InternalRobot.java:1143-1160).** Steps 1–2 happen inside the thrower's turn, so nothing can
  react to them. Each later pair happens at the flier's own turn, before its code runs. A squeak from the flier
  can therefore only warn about the pair after the one it has just flown. Each ally acts once between two flier
  turns, so a warned ally gets one move. For an early flier, steps 3–4 can never be warned.
- **Ceiling.** awubot's later collisions are 14% of its throws, walls and dirt included (R): at most ≈ 14 per game
  at ≈ 30 HP each, under 400 HP per game, and only part of that is dodgeable.
- **The large target is step 2.** About half of awubot's step-2 collisions hit a second enemy rat: 29% of its
  throws (combat.md §13), 42 HP each, ≈ 30 per game, ≈ 1,250 HP per game. Those can only be avoided before the
  throw, and the throw is predictable:
  - awubot throws at exactly t+2 in 95% of carries (R);
  - its lines are its facing ±45°, after one optional move (C: TryThrow.java.jinja2:160-190);
  - the carried rat can sense its carrier and squeak its position and facing.

The worthwhile package is P6 (pre-throw discipline). The flight squeak is low value (P9).

---

## 11. Work packages

**Test protocol for all of them.** Use `tools/games`: challenger vs awubot, paired seats, sequential test on the
quick pool, then on the tune pool. Then run the gauntlet of sparring bots (opp_obg and opp_gst first) to check for
regressions, and read the flipped games. Check the mechanism on the run's replays with the scratch analyzer (§13):
the metric the package targets must move, otherwise the result is noise.

### P1. Remove the throw assert; put cats in the throw grid
- **Hypothesis.** Mark cat tiles in TryThrow: a cat on step 1 makes the throw illegal, a cat at step ≥ 2 is a feed
  scored above every collision. Replace `assert(rc.canThrowRat())` with a check, and keep the side effect of the
  `performMoves` asserts. challenger then loses no carrier to the assert and turns some throws into kills.
- **Expected effect.** +0.5 to +1.5 pp vs awubot: it saves ≈ 0.26 rats plus their throws per game on our side and
  adds feeds on cat maps. The sign is certain.
- **Test.** Tune pool, plus the cat maps in §9.1. Metrics: no AssertionError in the logs; the share of our throws
  that feed a cat.
- **Risks.** Bytecode for marking each visible cat's 2×2 in the 9×9 grid. TryThrow is the most expensive
  generator (combat.md §1).
- **Size.** Small: ≈ 30–60 template lines in TryThrow.

### P2. Hold-and-bite carrying with value-gated throws (after P1)
- **Hypothesis.** Throw only into a cat, the enemy king, an enemy rat at step 2 or a wall at step 2, or at carry
  turn ≥ 8, or when the carrier is about to die or be grabbed. Otherwise bite or trap with the action and move
  toward the best line. This adds ≈ 80–100 enemy HP removed per grab (§2.2) and cuts open landings from 22%
  toward OBG's 3%.
- **Expected effect.** +3 to +6 pp vs awubot. OBG's carry profile (R) shows it works against awubot.
- **Test.** Tune pool. Metrics: open-landing share, bites while carrying per game, mean hold, step-2 share; compare
  with OBG's numbers in §2.3.
- **Risks.**
  - Carrier exposure: if it dies the victim goes free, and if the release tile is blocked the victim swaps, so
    throw by turn 9 at the latest.
  - TryPosition needs a goal term (H2 in combat.md).
  - Bytecode on carrying turns (TryThrow p90 ≈ 13.9k).
- **Size.** Medium: the CombatStrategy carrying branch, TryThrow thresholds and timer, a TryPosition goal; ≈
  100–200 template lines.

### P3. Trap-first engagement
- **Hypothesis.** When the action is ready and cheese is above a floor, place a rat trap on the tile between us
  and any enemy rat at Chebyshev 2. Drop the "≥ 24 rats or strong enemy" gate, and allow a second trap whose
  area overlaps the enemy's approach tile. challenger then lands more trap hits on awubot than it takes, as GST
  (+767) and OBG (+280) did over 12 games each (R).
- **Expected effect.** +3 to +8 pp vs awubot. This is the largest measured combat asymmetry in the OBG/GST games,
  but GST's losses show it does not win alone. T5's P2 estimates +10–20 pp on mine-rich maps.
- **Test.** Tune pool. Metrics: traps placed, and hits landed and taken per game (target: landed ≥ 1.4×
  taken); cheese floated. Run dose-response on the cheese floor (200 / 400 / 800) and on the distance band
  (d² 4–8 vs 4–5).
- **Risks.**
  - Cheese against spawning: about +60 traps per game, ≈ 1,200 cheese, at GST's rate. Coordinate with T1's spawn
    packages.
  - The cap of 25 active traps.
- **Size.** Small to medium: the TryTrap gate, weights for the gap tile and the spacing rule; ≈ 40–100 template
  lines.

### P4. Minimal-cheese bites
- **Hypothesis.** Choose the bite damage per target:
  1. kill now, at (h−11)²+1 cheese;
  2. else flip the HP order for my or a partner's grab next turn;
  3. else keep a won race (13 against a 12-biter in a 1v1);
  4. else 12 when affordable.

  This wins every isolated frontal race against awubot (§2.1).
- **Expected effect.** +1 to +3 pp.
- **Test.** Quick pool, then tune pool. Metrics: bites by damage level, 1v1 contacts won, cheese spent on bites.
- **Risks.** Cheese: +3 per 13-bite, ≈ +100–300 per game. `hasThreat(m, f, target, dmg)` already takes a damage
  argument (combat.md §4), so the threat model can use the chosen damage.
- **Size.** Small: `getBiteDamage` becomes a per-target function; ≈ 30–60 lines.

### P5. Complete threat model
- **Hypothesis.** Add four threats to the model:
  1. bite-then-grab: count the enemies that can reach and face me; I am grabbable if one of them would out-HP me
     after the others bite;
  2. enemies facing away that can turn first;
  3. a cat's scratch cone and pounce reach;
  4. suspect enemy trap tiles: tiles in front of an enemy rat that stood within Chebyshev 2 of us.

  This removes avoidable grabs and trap hits.
- **Expected effect.** 0 to +3 pp, sign uncertain: more conservative play gives up first bites.
- **Test.** Tune pool. Metrics: our rats grabbed per game, trap hits taken, standoff turns.
- **Risks.** Bytecode in the largest generated method (`CombatTracker.enemyMaskForMove`, ≈ 32.6 KB); passivity.
- **Size.** Medium to large.

### P6. Pre-throw discipline against enemy carriers
- **Hypothesis.** When an ally is carried by an enemy (the ally vanishes and the carrier's `carryingRobot` is
  set):
  1. our other rats do not end their turns on the carrier's step-2 tiles for its next ready turn (lines at its
     facing ±45°, after one move);
  2. they stand on its step-1 tiles or bite it;
  3. they grab it when it cannot see them or has less HP, which drops the ally.

  The carried rat squeaks its carrier's position and facing each turn.
- **Expected effect.** +1 to +4 pp, by removing part of ≈ 30 double hits per game (≈ 1,250 HP).
- **Test.** Tune pool. Metrics: awubot's step-2 collision share against us; ally rescues per game.
- **Risks.** Movement constraints during a fight; the one-squeak-per-turn budget; cats hear squeaks.
- **Size.** Medium.

### P7. Raw-cheese-aware combat
- **Hypothesis.** Target laden enemies first (slow, and they drop loot). Rats carrying ≥ 20 raw do not start
  engagements away from our king. A laden rat about to die dumps its raw cheese into a bite.
- **Expected effect.** 0 to +2 pp.
- **Size.** Small.

### P8. King assault and defence
- **Hypothesis.**
  1. Carry victims toward the enemy king when it is within ≈ 6 tiles, and throw into its 3×3 from Chebyshev 3.
  2. Occupy its ring tiles.
  3. Spend global cheese on finishing bites once the king's HP is within reach.
  4. Our king bites with cheese at kill or flip thresholds; fix B3.
- **Expected effect.** +1 to +3 pp, concentrated in games that reach a king.
- **Size.** Medium.

### P9. Flight squeak
Low value (§10). Do it after P6, if at all.

**Order.** P1 then P2, which share TryThrow. P3 and P4 are independent. P5 comes after P3 (the suspect-trap term).
P6 and P8 come after P2.

---

## 12. Not settled

1. **The −20 king steps (§4.2).** Cat scratches, or two −10 events within one king turn? Separating them needs
   per-event attribution with cat positions.
2. **The transfer path (§9.1).** Whether it ever kills a carrier in practice is unknown.
3. **The expected effects in §11** come from mechanisms and the OBG/GST profiles, not from runs of challenger.
   Each needs its `tools/games` run.
4. **An enemy rat's execution slot** (who holds the initiative in a pair) is not exposed by the API. Inferring it
   from the timing of several observers' squeaks was not attempted.
5. **Throw distance (6 vs 8 tiles)** depends on the pair's turn order and changes the values of later collisions
   (§1.2), so throw scoring has to cover both cases.

## 13. Reproducing the numbers

Everything is in scratch, `~/battlecode26-data/theory/t2/`.

- `replays/`: copies of the 28 OBG/GST validation replays (engine 1.2.5, seed 0), plus the validation logs that
  contain AssertionErrors.
- `tools/CombatStats.java`, per team: bites (rat/king/cat), grabs, throws, throw outcomes by damage value, cat
  feeds, rat traps placed and walked into, bites while carrying, hold times.
  Run it with `javac -cp $JAR CombatStats.java` and
  `java --add-opens java.base/java.nio=ALL-UNNAMED -cp $JAR:. CombatStats <replays>`.
  `$JAR` is the gradle-cached `battlecode26-java-1.2.5.jar`.
- **Replay attribution.** Actions go into the record of the robot whose `endTurn` comes next
  (E: server/GameMaker.java:519-541). A throw closes the thrower's and the flier's records mid-turn
  (E: InternalRobot.java:702-716), so a step-2 outcome comes before its `ThrowRat`; the analyzer handles this.
  Collision damage to the robot hit is not recorded, and `TriggerTrap.team` is the triggering team.
- `tools/KingDmg.java`: per-king HP deltas, split into bites, starvation-like steps, and the rest by size.
- `tools/MapStats.java`: size, cats, mines, walls and dirt for each map.
- `src/awudbg`, `run.sh`: the instrumented awubot (a print before each `assert(rc.canThrowRat())`) and a
  direct-java runner (the map's own seed).
- Outputs:
  - `runs/obg_gst_combat2.txt` (per game);
  - `runs/king_damage.txt`;
  - `runs/throwfail.txt`;
  - `runs/*.log` (the 4 diagnostic games).
