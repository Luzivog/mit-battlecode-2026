# EX: the rat exchange in rounds 1–300

Package EX (wave 5, track B): take the r1–300 rat exchange on better terms. W3 priced awubot's grabs and traps at
13.5 pp to it (docs/theory/w3-step-back.md). This doc holds the event-level diagnostic, an oracle that locates awubot's
grab value, and the arms.

Source keys: `E:` engine source at `engine.1.2.6`, `A:` `src/awubot/`, `C:` `src/challenger/`, `D:`
`~/battlecode26-data/scratch/ex/` (§6). Rounds are engine rounds; "a game" is a per-game mean over a run's games.

**Runs.** The diagnostic reads W3's main run `1119-174327-ab-a52f` (`goal`, seed base 14001, 570 cells, 1,140 games,
every replay; main 0.832). Arms and oracles play `goal` at seed bases 18001–18002, 1,000 cells (2,000 games) each,
paired with main on the same cells; arms read their changed cells' replays again (`tools/games replays --cells changed`).

---

## 0. Summary

1. **The exchange is fought, not ambushed.** 94% of grabs (both ways, r1–300) hit a rat that was already fighting: it saw
   an enemy rat in its last 3 turns, bit, or was hurt in the last 2 rounds. Rats mining, travelling or carrying cheese
   are grabbed 2.3 times a game. We already win it on average: by r300 45 of awubot's rats die against 36 of ours; our
   grabs kill 82% of their victims within 12 rounds against awubot's 72%, and awubot's traps hit us 27 times a game
   against our 39 on it (§1.1).
2. **Per configuration the two bots trade identically.** For a rat-turn near an enemy, the chance of being grabbed
   within 2 rounds, by allies and enemies within 3, HP band and mobility, is the same for our rats and awubot's in every
   cell (§1.3). It falls steeply with allies near (alone vs one enemy 5.1 per 100 turns, with three allies 0.5).
3. **The wounded rats lose it.** Our rats at HP > 50 are net grab-positive (+17.8 a game: grabbed 11.3, grab 29.1). At
   HP ≤ 50 they are net −16.1: those that cannot move next turn (a trap's stun or a landing) are grabbed 12.1 times and
   grab 0.9; mobile ones are grabbed 12.9 and grab 8.0. 72% of grabbed wounded rats were hurt in the 3 rounds before:
   the wound and the HP-rule grab are one combo inside one fight. awubot's wounded rats fare worse (net −19.6).
4. **The oracle: nearly all of awubot's grab value is in grabs of our wounded rats.** An awubot that never grabs an enemy
   rat at HP ≤ 50 is worth **+0.077 [+0.055, +0.099]** to us (main 0.838 → 0.914, 2,000 paired games), against +0.091
   for an awubot that never grabs at all (W3, 14001) (§2).
5. **No arm takes it.** Seven arms, each a different mechanism, none above zero on its 1,000-cell screen (§3): carriers
   treated as harmless (A1) −0.003; wounded rats back away (W) **−0.048**; healthy rats guard wounded allies (G)
   −0.009; stunned wounded rats trap their approach (S) **−0.021**; the ally sacrifice up to 42 HP (T) **−0.018**;
   TryTrap traps the approach to a wounded ally (P) −0.004; healthy rats close on wounded enemies (K) −0.005. On their
   replays, none moved the grabs on our wounded rats: W left their turns near enemies and the grabs on them unchanged
   (165 vs 165 turns, 15.6 vs 15.3 grabs a game) and added 2 blind grabs; G, S and A1 left them unchanged too.
6. **Recommendation: merge nothing.** The reservoir is real and located (wounded rats, the 3 rounds after the wound), but
   our rats cannot leave it (a wounded rat moves no faster than its pursuer), cannot shield it cheaply, and every local
   rule tried costs more than it saves: the CX lesson, now for the wounded class. What's left (§5) is FL's ground: joint
   search of the micro on the lab's engagements, with the wounded-rat combo as the target.

---

## 1. The exchange, event by event (r1–300)

`D:ExStats.java` reads every grab of an enemy baby rat and its outcome (throw and what the flier hit, drop, rescue,
death), every rat-trap hit, every death with its cause, and per rat-turn the local configuration, for both teams. Its
grab and trap-hit counts match W3's `GrabStats` on a test game (82/82 grabs, 54/54 trap hits); W3's thrown count was
low (43 throws where the replay holds 66 `ThrowRat` actions), so "thrown in about 60% of carries" is ~83%.

### 1.1 Totals (per game, r1–300)

| | awubot on us | us on awubot |
|---|---|---|
| grabs (all / in our wins / in our losses) | 38.6 / 38.6 / 38.8 | 40.6 / 41.9 / 34.5 |
| blind / HP rule | 18.6 / 19.2 | 17.6 / 22.2 |
| thrown | 32.3 (83%) | 33.0 (81%) |
| thrown into: wall or dirt / the victim's ally / the victim's king / ground / a cat | 16.9 / 7.6 / 1.0 / 6.0 / 0.15 | 19.8 / 8.9 / 1.3 / 1.8 / 1.1 |
| rescued (carrier grabbed or killed) | 4.1 (11%) | 4.9 (12%) |
| victim dead within 12 rounds | 27.7 (72%) | 33.2 (82%) |
| rat-trap hits; then grabbed within 3 rounds; dead within 12 | 27.1; 8.9; 18.1 | 39.2; 14.2; 30.4 |
| bites on rats; on kings | 52.7; 2.3 | 49.7; 8.2 |
| rat deaths | 36.2 (ours) | 45.1 (theirs) |

- Traps placed in r1–300 nearly all fire: ours 41.8 placed, 40.3 fired; awubot's 28.4 and 27.9.
- 90% of awubot's throws that hit one of our rats do it at step 2 on the throw turn (42 damage): a flier cannot be
  dodged.
- **Our losses kill less; they do not die more.** In our losses awubot's grabs on us are unchanged (38.8) and ours fall
  (34.5), with shorter games. In the 148 split cells the won and the lost game part by ~1 grab and ~1 net death per 50
  rounds from r50, every term a little (`D:exsplit.py`).
- Net kills predict the winner only weakly: at r100 P(win) runs from 0.69 at −4 to 0.89 at +4.
- Zones (Chebyshev to the kings): both bots grab more on their own side; net kills +5.3 a game on our side, ~0 on
  awubot's.

### 1.2 What the victim was doing

| awubot's grabs of our rats, r1–300 | a game | victim dead within 12 |
|---|---|---|
| fighting (enemy rat seen in 3 turns, bit, or hurt in 2 rounds) | 36.3 (94%) | 74% |
| travelling / at a mine / carrying cheese | 1.3 / 0.7 / 0.3 | 38–49% |

- Blind grabs (18.6): the victim never saw the grabber in 7.3, saw it the turn before in 4.4, 2–3 rounds before in 3.6,
  4–10 rounds before in 2.9. An ally of the victim saw the grabber's start tile in 7.3 of them: shared warning reaches at
  most those, and the grabber moves after the sighting.
- The grabber stepped in from Chebyshev 2 in 60% of grabs and was adjacent already in 40%.
- Support: with an ally at Chebyshev 1, 24% of grabs are rescued and 71% thrown; with none within 6, 0.4% and 94%. The
  victim dies 70–74% whatever the support: a rescue pays through the carrier (rescued victims die 48% against 74%
  thrown, and the carrier becomes our victim).

### 1.3 Hazard per rat-turn

Per rat-turn near an enemy rat (Chebyshev 4) in r1–300: allies and enemies within Chebyshev 3, HP band, mobility (can
it move next turn: end-of-turn move cooldown < 20); grabbed within 2 rounds, grabs it made (`D:exhaz.py`).

| our rats | rat-turns a game | grabbed a game | grabs made a game | net | awubot's net |
|---|---|---|---|---|---|
| HP > 50, mobile | 276 | 11.3 | 29.1 | **+17.8** | +17.3 |
| HP > 50, cannot move next turn | 33 | 1.5 | 0.7 | −0.8 | −0.9 |
| HP ≤ 50, mobile | 144 | 12.9 | 8.0 | **−4.8** | −3.5 |
| HP ≤ 50, cannot move next turn | 84 | 12.1 | 0.9 | **−11.3** | −16.1 |

- The rates per cell match between the bots (HP > 50, mobile, alone vs one enemy: grabbed 5.1 per 100 turns, awubot's
  4.8; with three allies 0.5 and 0.4). What differs is how many turns each side spends in each cell: our traps put
  more of awubot's rats in the stunned-wounded row (108 turns a game against our 84).
- **The combo**: 18 of the 25 wounded rats grabbed a game were hurt in the 3 rounds before the grab.
- **Stunned wounded rats** are grabbed 12.2 times a game, 8.0 of them by the HP rule from the front (the victim sees
  the grabber; facing is no defence); half their grabbers were already adjacent at the start of their turn.
- **Mobile wounded rats do not flee**: of the 12.9 grabbed a game, 6.1 had not moved in their last turn, 4.0 kept their
  distance, 2.7 closed on the grabber, 0.15 moved away.
- 44% of grabs on us hit a rat with no ally within 3.
- HP-rule grabs where one 12-damage bite on the grabber beforehand would have ended its HP lead: 4.9 a game, 2.4 with
  an ally of the victim within 2 (too few for a protective-bite rule).

---

## 2. Oracles

Awubot copies on the probe branch `t3/ex-probes`, each paired game by game with main's games on the same cells (the
screens' reference games, run `1119-191145-ab-6407`, main 0.838 on 18002; `D:paired.py`, 95% CI by bootstrap over maps).

| oracle | seed base | run | score | vs main [95% CI] | results changed (+ / −) |
|---|---|---|---|---|---|
| **awubot never grabs an enemy rat at HP ≤ 50** (`ex_awunowound`) | 18002 | `1119-191321-ab-cb5c` | **0.914** | **+0.077 [+0.055, +0.099]** | 312 (233 / 79) |
| awubot never grabs an enemy rat at exactly 50 HP (`ex_awuno50`) | 18002 | `1119-192932-ab-f8ad` | 0.839 | +0.001 [−0.017, +0.020] | 354 (178 / 176) |

- Against W3's "awubot never ratnaps" (+0.091 on 14001, +0.092 against the null on 14002), the wounded-rat grabs carry
  almost all of the grab value: the healthy-rat exchange is even.
- The exactly-50 oracle (the one-trap victims, 14 of ~49 wounded grabs a game over the whole game) leaks: one bite takes
  the rat off 50 and awubot grabs it the next turn. It does not split the +0.077.

---

## 3. Arms

Each a commit on its own branch off main, screened on `goal`, 1,000 cells, `--remote` (`tools/games ab challenger@REV
awubot --ref challenger@origin/main --min-cells 1000 --max-cells 1000`). The screens on 18002 share main's reference
games.

| arm | change | seed base | run | diff [95% CI] | cells changed (+ / −) | mechanism (changed cells' replays, r1–300) | branch @ head |
|---|---|---|---|---|---|---|---|
| A1 | an enemy carrier is not a grab threat (carrying blocks a grab; a throw or drop spends the action): CombatTracker skips it, shouldFight is true against it | 18001 | `1119-184829-ab-3840` | −0.003 [−0.023, +0.015] | 300 (147 / 153) | rescues of our grabbed rats 7.2 vs 7.3 a game: did not fire | `t3/ex-a1-carrier` @ `511dfb7` |
| W | a rat at HP ≤ 50 with a visible stronger enemy rat within Chebyshev 3 backs away: TryFight's approach term goes to steps that increase the distance, and a forward run is a candidate | 18002 | `1119-190428-ab-3961` | **−0.048 [−0.069, −0.028]** | 318 (120 / 198) | wounded turns near enemies 165 vs 165, grabs on them 15.6 vs 15.3; blind grabs on us 21.6 → 23.6; their grabs 9.2 → 7.6 | `t3/ex-w-wounded` @ `d2c0ff6` |
| G | a rat above 50 HP scores FIGHT_STEP for a step ending adjacent to a visible wounded ally (HP ≤ 50) | 18002 | `1119-190803-ab-4fba` | −0.009 [−0.026, +0.006] | 248 (117 / 131) | grabs on our wounded rats: mobile 15.3 vs 15.6, stunned 14.6 vs 14.5 a game | `t3/ex-g-guard` @ `181237f` |
| S | trap shield: a rat that cannot move, at HP ≤ 50, with a stronger enemy rat at Chebyshev 2 and none adjacent, faces it and traps the front tile covering its approach (`TryShield`, after TryRatNap) | 18002 | `1119-191145-ab-6407` | **−0.021 [−0.040, −0.004]** | 285 (125 / 160) | traps placed 44.0 vs 45.2 a game (the trigger is rare); grabs on stunned wounded rats 14.0 vs 13.9 | `t3/ex-s-shield` @ `7f4d7c1` |
| T | throw first: TryRatNap's ally sacrifice (grab an ally, throw it only into an enemy at step 2) up to 42 HP, was 22 | 18002 | `1119-192600-ab-8a72` | **−0.018 [−0.033, −0.004]** | 218 (94 / 124) | not replayed | `t3/ex-t-throwfirst` @ `326cb4f` |
| P | TryTrap also weights, and opens for, the approach tiles of an enemy rat that faces a visible wounded ally it out-HPs | 18002 | `1119-192646-ab-20c7` | −0.004 [−0.019, +0.012] | 199 (99 / 100) | not replayed | `t3/ex-p-traprotect` @ `9411b27` |
| K | a rat above 50 HP that sees a wounded enemy rat (HP ≤ 50) within Chebyshev 4 targets it and scores FIGHT_STEP on steps that close on it | 18002 | `1119-193041-ab-b514` | −0.005 [−0.021, +0.011] | 301 (146 / 155) | not replayed | `t3/ex-k-wounded-target` @ `e2f9547` |

No screen read above zero, so no arm went to a confirmation. S passed `--pool smoke` (no robot killed by its own
exception; `1119-191150-ab-1eea`).

---

## 4. What the arms teach

- **The wounded-rat grabs are locked in by the wound.** The combo completes within 3 rounds of the wound, the grabber
  stepping in from Chebyshev 2 or already adjacent. A wounded rat moves no faster than its pursuer: W made it run, its time near enemies
  and the grabs on it did not move, and the run showed its back (+2 blind grabs a game). Guards (G) and traps (S, P)
  placed by the local rules either did not change the grabs or fired too rarely.
- **The local rules sit at an optimum for the wounded class too.** Like CX's caution arms for the threat model and
  TN2's constants, every behaviour change here re-randomises 20–30% of cells and loses a little or a lot; the champion's
  22-HP sacrifice limit (T) and the wounded rats' place in the fight (W) are load-bearing.
- **Carriers.** A1's premise is sound (a carrier cannot grab on its next turn), but rescues did not follow: what kept
  our rats from grabbing carriers was not the carrier's own threat (inference: legality, as CC found).

---

## 5. What's left

- **The target for FL**: the wounded-rat combo, worth up to +0.077 (§2). FL's fight lab can stage it (a trap or bite
  on one of ours at Chebyshev 2 from an awubot rat with more HP, allies 0–3) and search the micro jointly (combat.md
  H12, `TryAllCombat`), where each of this package's local rules failed alone.
- Unpriced and unbuilt: a cheaper wound-to-grab break than a trap (dirt on the approach tile needs team dirt); the
  "just released" enemy (a rat that threw or dropped last turn cannot grab this turn) as an extension of A1.
- Shared blind-side warning (W3's EX (a)) reaches at most 7.3 blind grabs a game and the grabber moves after the
  sighting; travelling and mining exposure is 2.3 grabs a game. Both below what a 1,000-cell screen resolves.

---

## 6. Data and tools

- `D:` = `~/battlecode26-data/scratch/ex/`. Java readers compile against
  `~/battlecode26-data/engine/jars/battlecode26-java-1.2.6.jar` into `D:cls/` (`java -cp JAR:cls NAME OUT replay...`).
  - `ExStats.java OUT.jsonl replay...`: per game the grabs (rule, victim HP, cheese, activity proxies, the victim's
    vision history of the grabber, allies, outcome, what a throw hit, fate within 12 rounds, grab-tile offset, the
    victim's last move relative to the grabber), ally carries, trap hits (cause, place, fate), deaths (cause, cheese
    held), traps placed and fired and bites in r1–300, and the per-rat-turn hazard grid (allies × enemies within 3 ×
    HP band × mobility: turns, grabbed, grabs made). Ghost-safe (robots without a turn this round or the last ignored).
  - `exrun.sh RUN`: ExStats over a run's replays, candidate (`o`) and reference (`r`) apart, into `D:RUN/`.
  - `ExTrace.java REPLAY TEAM R0 R1 [MAX]`: an ASCII snapshot of the 11×11 around each grab by TEAM in rounds R0–R1,
    then the victim's and the grabber's next 4 rounds.
- Python 3: `exdiag.py EX.jsonl... [--rmax=R]` (totals, wins and losses), `exclass.py` (grabs by rule, activity,
  vision, support, zone, stun), `exhaz.py` (the hazard grid), `exsplit.py` (split cells by 50 rounds), `paired.py
  BASE[:role] RUN[:role]...` (paired diff, bootstrap CI over maps).
- Data: `D:m14001/` (W3's main run through ExStats), `D:RUN/` for each replayed arm, `D:runs.txt` (every run).
- Probes on `t3/ex-probes` (worktree `D:wt/`, `D:mkcopy.sh`), never merged: `ex_awunowound`, `ex_awuno50`.
