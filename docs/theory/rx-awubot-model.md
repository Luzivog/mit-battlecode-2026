# RX: the rat exchange against a model of awubot

Track RX of the clean program (docs/program.md): use awubot's own decision code as the model of what its rats do next
turn, and play our micro against that prediction. Source keys: `A:` `src/awubot/`, `C:` `src/challenger/`, `D:`
`~/battlecode26-data/scratch/rx/` (§8). Seed bases 35000-35999 are RX's. Unless marked, figures are per game, rounds
1–300, over 300 replays of main's baseline run on `goal` at seed base 35001 (§6). The replay format records no damage
for bites (a `RatAttack` has no `DamageAction`), cat scratches or the robot a thrown rat hits; the reader applies them
itself (12 a bite, 20 a scratch, the flier's damage), after which its HP matches every robot's next record at 99.9% of
turns. Figures from before that fix (bites missed) overstated every "exact" count; all below are after it.

---

## 0. Summary

1. **awubot's grab rule is a near-perfect predictor given the full state.** It grabs whenever a legal grab exists from a
   tile its own tracker calls safe (§1). A replica on replays predicts 99.7% of its 34.8 r1–300 grabs a game, victim
   right 99.9%; 87% of the turns where its rule allows a grab end in one.
2. **Deterrence is empty.** Our victims are alone when grabbed: awubot's tracker would block 3.6% of its grabs if our
   rats' facing did not matter, 7.9% if every rat of ours within 3 counted.
3. **Defence is limited by what our rats see** (§3). In 13.3 of the 28.1 grabs that follow the victim's turn a pose
   existed from which awubot's rule would not have grabbed; in 7.8 the grabber was outside the victim's cone (no rat of
   ours saw it in 6.7), in 5.4 the victim's step fired a hidden awubot trap and flipped the HP order.
4. **Our tracker's flags are more than half false against awubot** (§2), but removing the false ones loses (§6: an
   enemy that just threw −0.018 [−0.034, −0.001], occupied tiles −0.009): the caution is load-bearing.
5. **Offence is already taken** (§5): we grab 84% of the visible grabs awubot's rule calls safe for us; the punish
   window after awubot's grabs (its carrier cannot act next turn) has a second rat of ours in reach in 8.8 of 34.8 grabs,
   a legal grab of the carrier in 3.0, 1.2 seen and taken.
6. **The one large prize found: awubot's carriers throwing our rats into our other rats** (§7), 7.4 hits a game, worth
   **+0.051 [+0.033, +0.069]** to us if its carriers never did (+0.066 with its ally-sacrifice throws too). It is not
   reachable by dodging: 77% of the hits follow a move or turn by the carrier, and the set of tiles it can hit flags 136
   of our rats' turns a game at 4.5% precision; both dodge arms read ≤ 0.
7. **awubot's traps are predictable through its own gate** (§4; 31% precision, 81% recall), and avoiding them still
   does not pay (CX C, 44% precision, −9 pp).
8. **Nothing to merge.** Six arms screened, none above zero (§6); the RX switches stay off in the branch.

---

## 1. awubot's grab rule, replicated on replays

awubot grabs an enemy rat in two places (`A:robot/rat/combat/moves/TryRatNap.java`): `run()`, first in its combat
pipeline, and `miniRun()`, after a TryFight step (no threat check). `run()` takes the first three robots of its
`sortedEnemies` (its cone, nearest first, kings counted) within Chebyshev 2, baby rats above 10 HP, and tries every
(move, facing) of its primary moves (stay, forward, ±45°, ±90°) and, when the target is adjacent and grabbable now, a
grab from where it stands followed by a step back. A candidate needs the destination adjacent to the target, the target
in the new facing's cone, and a legal grab (target HP below its own, or the target not seeing the destination); it
scores 30 (35 for grab-then-step-back), −3 or −8 for a strafe, +2 for a turn, and −100 when its own CombatTracker
flags the destination: any of our other rats it sees (or was squeaked) whose cone covers the destination and that can
step next to it, from any side if its HP is above the grabber's, else from a tile behind the grabber's new facing. Any
candidate above 0.5 is taken, so **awubot grabs whenever a legal grab exists from a tile its tracker calls safe.**

`D:tools/Rx.java`, hooked into a copy of EX's replay reader (`D:tools/RxStats.java`), evaluates the rule at the start of
every awubot baby rat's turn on the world as it stands then (positions, facings, HP after earlier turns' damage,
cooldowns from the rat's last record, dirt), ignoring awubot's squeaked enemies and its bytecode budget.

| r1–300, a game | |
|---|---|
| awubot grabs of our rats | 34.8 |
| predicted by the replica (a safe candidate existed) | 99.7% |
| predicted victim = actual victim | 99.9% |
| turns with a safe candidate | 40.0 (87% end in a grab) |
| grabs its tracker would block if our rats' facing did not matter (rats of ours it sees) | 3.6% |
| ... if every rat of ours within 3 counted, seen or not | 7.9% |

## 2. Our tracker against awubot's rule

Our CombatTracker (awubot's code) flags a tile when an enemy rat that sees it can step next to it, from any side if its
HP is above ours, else from our hidden side. For each rat of ours an awubot rat sees within Chebyshev 2 at the start of
the awubot rat's turn (98 such flags a game):

| the flag | share |
|---|---|
| awubot has a safe grab of that rat | 42% |
| awubot's action is not ready (61% of these: it threw or dropped a rat on its last turn) | 20% |
| awubot cannot move (6 points of 14: a trap's stun) | 14% |
| its grab tile is held by another robot | 11% |
| its own tracker deters it | 9% |

## 3. Could our rats have escaped? (exact poses)

At each of our rats' turns with an awubot rat within 3 (399 a game), every reachable (move, facing) is scored twice:
exactly (would any awubot rat, in its state then, have a safe legal grab of it) and by our tracker (from the rats our
rat saw at its turn start). The chosen pose is then checked, and the rat followed to the next awubot turns.

| a game | |
|---|---|
| our rat exactly cornered (no pose safe from awubot's rule) | 22.5 |
| cornered by our tracker | 35.9 (an exactly safe pose existed in 18.3) |
| chosen pose exactly threatened | 33.1 (an exactly safe pose existed in 16.6; one safe by both models in 15.4) |
| grabbed right after its turn | 28.1: chosen pose exactly threatened 25.6, with an exact escape 13.3 |
| ... of the 13.3: grabber unseen at turn start, pose unflagged | 7.8 |
| ... grabber seen, our tracker flagged the chosen pose | 5.4 |

A local probe (`rx_probe` on `t3/rx-probes`: main printing each turn's deciding stage; 6 Java games joined to the
replica, read before the HP fix, so its counts are indicative) shows what those turns were:
- **Seen and flagged**: the deciding stage was TryRatNap or TryAttack in 31 of 62 grabbed cases, TryFight forced (every
  candidate threatened) in 11. The typical case: our rat at 100 HP steps to bite or grab an awubot rat it out-HPs, its
  step fires an awubot trap (50 HP, no move for 3 turns), the HP order flips, and the rat it bit (88 HP) grabs it. The
  tracker was right when it chose; the trap was hidden.
- **Unseen**: TryFight against a visible enemy in 23 of 59, the macro path in 9: a second awubot rat steps in from
  outside our rat's cone.

Both gaps are information our rats do not have, not a fault of the model. Of the grabs by an unseen grabber with an
exact escape (8.6 a game on 150 replays), an ally of the victim within squeak range (r² 16) saw the grabber in 1.9; in
6.7 no rat of ours saw it: a better squeak choice reaches at most the 1.9.

## 4. awubot's traps through its own gate

awubot's TryTrap runs when its action is ready, no grab fired, a front tile is free, and either it carries ≥ 20 raw
cheese, or 24 or more of its rats are alive, or one of the three enemies it sees nearest is a baby rat at 100 HP or at
≥ 80 and ≥ its HP + 10 (`A:robot/rat/combat/moves/TryTrap.java` `shouldTrap`). On 100 replays: 26.1 traps a game
placed by such turns, 0.2 outside the gate; the "strong rat in view" branch alone 17.8.

For each tile our rat can step to (1,783 a game, 6.9% inside an awubot trap's 3×3):

| rule: the tile is within 1 of a front tile of an awubot rat within 3 that ... | flagged a game | precision | recall |
|---|---|---|---|
| any (CX's kind of zone) | 838 | 0.139 | 0.947 |
| sees a strong rat of ours or carries 20 raw (computable in game) | 545 | 0.194 | 0.862 |
| had TryTrap's gate on at its last turn | 367 | 0.281 | 0.842 |
| ... and bit, grabbed and threw nothing then | 320 | 0.311 | 0.813 |
| CX arm C (enemy at 2 seeing me while I am strong to it, tiles between) | 135 | 0.437 | 0.480 |

CX's arm C, the most precise rule here, cost 9 pp in games (docs/closed.md, CX C): a standoff costs more than the traps
it avoids. A trap-avoidance arm from awubot's gate is therefore not built.

## 5. Offence with the same model

**Punish.** After a grab awubot's carrier has action cooldown 20: it cannot bite, grab or throw on its next turn (100%
of 34.8 grabs a game), only move or turn (TryPosition, to a tile its tracker calls safe). Every rat of ours gets one
turn before that. A second rat of ours is within Chebyshev 2 of the carrier in 8.8 grabs a game; it has a legal grab of
the carrier (its HP above the carrier's, or off the carrier's cone, which faces where the victim stood) in 3.0, 2.6 of
them safe from awubot's other rats by the exact rule; it sees the carrier at turn start in 1.2. We rescue 1.2 a game:
73% of the seen chances. A punish policy has the 1.8 unseen chances a game to gain without moving rats closer together;
more needs rats near each other, which SQ priced at −3 to −4.4 pp (coverage).

**Mirror** (our grabs by awubot's rule): awubot's rats end 42.9 turns a game where a rat of ours that sees them could
grab them legally and safely; we grab 31.8 of them before their next turn. At our rats' own turns: 44.6 with such a
grab, taken in 37.4, a bite instead in 5.5 (a probe of 6 local games: 42 of 45 such bites after TryRatNap's step fired a
hidden awubot trap, which made the carry illegal; the rest a bite after TryFight's step, `TryAttack.miniRun` before
`TryRatNap.miniRun`, the order in awubot too), something else in 1.7.

## 6. Arms

Each a switch in `C:Tune.java` (off in main), on in one commit on its own branch off the RX branch, screened on `goal`,
1,000 cells, against main on the same cells (`tools/games ab challenger@REV awubot --ref challenger@origin/main
--remote`). Seed bases: 35002 (look, occ, threw), 35003 (grabfirst), 35004 (dodge).

| arm | change | run | cells | better / worse | diff [95% CI] | branch @ head |
|---|---|---|---|---|---|---|
| look2 | a combat rat with action and turn ready and no visible enemy rat within 2 turns toward an enemy rat within 2 it knows of (squeaked, or seen on its last turn) before fighting | `1121-034638-ab-c101` (stopped) | 231 | 39 / 29 | flat (+0.02 unpaired-CI wide) | `t3/rx-look2` @ `4b50398d` |
| occ | tiles held by our robots, kings or cats leave the grab-threat masks | `1121-034639-ab-9ebf` | 1,000 | 132 / 153 | −0.009 [−0.025, +0.007] | `t3/rx-occ` @ `3e67aa68` |
| threw | an enemy rat seen carrying on our last turn and carrying nothing now is no grab threat (its action is not ready) | `1121-034640-ab-d112` | 1,000 | 117 / 142 | **−0.018 [−0.034, −0.001]** | `t3/rx-threw` @ `7c9d4a08` |
| grabfirst | after TryFight's step, TryRatNap.miniRun before TryAttack.miniRun | `1121-040840-ab-9a69` (stopped) | 974 | 85 / 82 | −0.002 [−0.017, +0.014] | `t3/rx-grabfirst` @ `0d349b8a` |
| dodge | a tile 2 (3) straight ahead of a visible enemy carrier loses 40 (20) in every move score | `1121-052241-ab-36d2` (stopped) | 283 | 28 / 45 | −0.030 | `t3/rx-dodge` @ `bca5c305` |
| dodge2 | dodge, only carriers seen carrying on our last turn (`RX_DODGE` 2); seed base 35005 | `1121-095136-ab-ac28` | 1,000 | 112 / 119 | −0.004 [−0.020, +0.011] | `t3/rx-dodge2` @ `0de8c6ca` |

- **Precision of the tracker is not a lever.** Removing flags that are certainly false against awubot (an enemy that
  just threw cannot grab) loses, and removing the occupied-tile ones does nothing: the caution is load-bearing even
  where the threat is false (CX's B found the converse cost, more flags −9 pp; TN2 and FL the same for the penalty).

## 7. awubot's throws into our rats

Oracles (awubot copies on `t3/rx-probes` @ `93041aa9`/`02342944`, plain against main on the baseline's cells, seed base
35001, paired with `1121-040625-ab-5fe9`, main 0.843):

| oracle | run | games | diff [95% CI] |
|---|---|---|---|
| awubot never throws a line that passes one of our rats within 5 tiles (`rx_awunohit`: every enemy-rat hit scores −1000, carrier and ally-sacrifice throws) | `1121-034637-ab-daf4` | 2,000 | **+0.066 [+0.047, +0.084]** (279 results changed, 205 / 74) |
| ... its carriers' throws only (ally throws unchanged, `rx_awunohitc`) | `1121-095136-ab-35eb` | 2,000 | **+0.051 [+0.033, +0.069]** (303 changed, 202 / 101) |
| awubot never grabs its own wounded ally to throw (`rx_awunosac`) | `1121-072348-ab-2e4e` (stopped) | 286 | +0.000 [−0.048, +0.045] |

- The value is in the carrier's throw of our grabbed rat into another of our rats (42 to each at step 2): 7.4 such hits
  a game in r1–300, 41 HP each (150 replays; its sacrificed rats hit ours 1.7 times more). The sacrifice is worth nothing
  to awubot.
- **The carrier's throw is not a straight line.** On the replays of dodge v2 against main on 100 cells
  (`1121-102216-ab-f2be`, every replay), awubot's throws hit a rat beside the victim 7.58 times a game against main's
  7.70: the dodge did not fire where it mattered. Of main's 7.4 hits a game (150 replays), the hit rat had ended its turn
  on the carrier's tile 2 or 3 ahead (as the carrier then stood) in 1.6; in 5.7 the carrier moved or turned first:
  TryThrow picks the best (move, direction) over every move and, standing or stepping forward, every direction. The
  union of the tiles that choice can hit (step 2 from any reachable tile) holds 6.1 of the 7.4 hits but flags 136 of our
  rats' turn ends a game (90 with the carrier in view): 4.5% precision; for rats at ≤ 42 HP, whom a hit kills, 3.3%. No
  dodge rule reaches the oracle's value without standing off from every carrier.

## 8. Data and tools

- `D:` = `~/battlecode26-data/scratch/rx/`; `D:runs.txt` lists every run.
- `D:tools/RxStats.java` (EX's reader with HP tracking fixed: bites, scratches and throw hits applied) + `D:tools/Rx.java`
  (awubot's grab rule; our tracker's flags; exact escape poses; trap gate and trap tiles; punish window; mirror; throw
  hits and line sets). Compile against `~/battlecode26-data/engine/jars/battlecode26-java-1.2.6.jar` into `D:tools/cls`;
  run `RXOUT=out.jsonl java -cp JAR:cls RxStats /dev/null replay...`; aggregate with `D:tools/rxagg.py`,
  `punagg.py`, `probejoin.py`, `vcjoin.py` (probe joins); `D:tools/exrole.sh RUN` runs EX's ExStats per role.
- Probes on `t3/rx-probes` (worktree `D:wt`, `D:mkcopy.sh`), never merged: `rx_awudet`, `rx_awudetall`, `rx_awunohit`,
  `rx_awunohitc`, `rx_awunosac` (oracles), `rx_probe` (main printing each turn's deciding stage and TryRatNap's
  candidates; local Java only, `gradlew run`).
- Baseline: main against awubot, `goal`, seed base 35001, 1,000 cells, 0.843 [0.818, 0.868] (`1121-040625-ab-5fe9`; its
  first 567 replays under `1121-030327-ab-e173`).

## 9. Wave 2: the throw itself (main 615a0bf9)

W4 priced an awubot that never throws our rats at +12.3 pp on clean main (docs/theory/w4-clean-step-back.md §3): the
throw, not the grab, carries the exchange. Its line score (A:`robot/rat/combat/moves/TryThrow.java.jinja2`): our king
anywhere in the flight +300; one of our rats at the line's first collision, step 2, 2 × (10 + 36) + 10 = 102 (later
steps a fraction, and the scan goes on); a wall or dirt 10 + 4 × remaining (46 at step 2, then 42, 38, 34); off the map
10. With a wall in reach the victim takes 42 whatever our other rats do; dodging only spares bystanders. Rescue (taking
the carrier before it throws) spares the victim.

**The two rescue windows** (replica, 300 replays, r1–300, a game): awubot grabs at its turn t; at t + 1 its action is
not ready (cooldown 20), so it only moves (TryPosition, lining up its best line); at t + 2 it throws (29.4 of its 34.8
grabs are thrown on that turn). Every rat of ours has two turns before the throw.

| | window 0 (t → t+1) | window 1 (t+1 → t+2) | either |
|---|---|---|---|
| a rat of ours within 2 of the carrier | 8.8 | 9.5 | – |
| ... with a legal grab of it (HP above its, or off its cone) | 3.1 (seen 1.3) | 4.1 (seen 2.5) | 6.3 (seen 3.7) |
| ... with a bite of it | 6.4 | 7.0 | 10.5 |
| rescued | 1.2 | 2.1 | 3.3 |

We already rescue on ~90% of the seen legal chances; 2.6 a game are legal but unseen.

**The throw-line set** for a carrier that can throw next turn: every tile at steps 2–5 of any (step, direction) line
its TryThrow considers (step to a free tile or none; facing or ±45°, any direction when it stays or steps forward;
first tile empty; path clear) holds 5.2 of the 7.4 hits a game on our rats, and our rats end 34 turns a game on such a
tile: 15% precision (the step-2-only set of §7: 4.5%).

| arm | change | run | cells | better / worse | diff [95% CI] | branch @ head |
|---|---|---|---|---|---|---|
| lines | a tile on any throw line of an enemy carrier seen carrying on our last turn too loses 30 in every move score (`RX_LINES`) | `1121-111441-ab-cfd7` (35006) | 1,000 | 151 / 170 | −0.013 [−0.031, +0.005] | `t3/rx2-lines` @ `8e10a251` |
| rescue | a rat carried by an enemy squeaks its carrier every turn; a combat rat with action and turn ready and no visible enemy rat within 2 first turns toward a squeaked enemy within 2 (`RX_RESCUE`) | `1121-111557-ab-1d9b` (35007) | 1,000 | 141 / 105 | **+0.021 [+0.004, +0.039]** | `t3/rx2-rescue` @ `d2a01b2f` |

- Lines: the prediction is three times sharper than dodge v2's and still loses; standing off a ready carrier's lines
  costs more than its hits. The king's avoidance was not built (the king moves once in 4 turns; its 3×3 lies on many
  lines).
