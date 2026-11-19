# FL: a fight lab for the micro

A lab that measures the micro on the fights of real games, validated against whole-game effects, then used to search
the micro's weights. Source keys: `C:` `src/challenger/`, `D:` `~/battlecode26-data/scratch/fl/` (§6), `L:` the lab's
data, `~/battlecode26-data/scratch/fl/lab/`. Rounds are engine rounds.

---

## 0. Summary

1. **The fights that decide games** (W2's 1,140 replays, 49,896 engagements, §1): in our COMBAT losses we still win
   the r1–300 rat exchange, by half as much as in our wins on the same maps (+5.6 against +11.7 rats, +317 against
   +1,181 HP), with the same number of fights. Group fights away from kings carry the largest share of the rat gap,
   fights at our king the largest share of the HP gap; our king dies inside a fight at our king in 31 of 43 early-fight
   losses, after a run of fights we won.
2. **The lab** (§2, `tools/fightlab/`): real games forked at their fights. A lab copy of challenger reads its micro
   constants from properties from the fork round on, so every arm shares the fork's state and memory; scenario maps
   cannot stage these fights (kings only, full HP, no memory). An arm is scored against a salted null on the same
   fork: the exchange in HP, rats, king HP and cheese over 80 rounds or to r300.
3. **Validation, single arms** (§3.1, 10 arms × 553 forks × 2 salts): reverting CT's band traps or CW's hold shows a
   loss in the lab (−98 and −52 HP of exchange a fork, both clear of 0; −4.4 and −2.5 pp in 1,000-cell whole-game
   tests). But across arms the lab does not rank whole-game effects: correlation +0.17 on the five priced fresh, +0.24
   on nine with past packages' prices. `THREAT_PENALTY` 40 is the lab's worst arm (−172 HP) and costs 1.2 pp; CX's
   graded threat gains 38 HP and no games: fight-level gains convert to wins at rates that differ by mechanism (CB and
   CX found the same with duels).
4. **Validation, a screen** (§3.2, the same 16-constant design in games and in the lab): over the 16 effects the lab
   predicts the games, r = +0.81 (Spearman +0.66), and ranks the same two constants first. But a native whole game
   now costs 2.4 CPU-s (XC) against 53 for a lab fork on Java: the games' screen resolved its effects at about a
   ninth of the lab's CPU. **Judge micro on native whole games; use the lab to see what a change does to fights.**
5. **The micro searched** (§4): 16 newly exposed constants and TN2's three are flat around main (all within ±0.4 pp
   per step but two; joint moves cost 3.8 pp). The two leads: `THROW_HOLD` 22 did not hold as a single move (the lab
   reads it as nothing); **`STRAFE_LO` 3 → 0** (a strafe from movement cooldown < 2 free in TryAttack, TryRatNap and
   TryTrap) passed the precision route's confirmation, **+0.017 [+0.006, +0.028]** on 3,000 fresh cells, after
   screens of +0.020 and −0.012; about +1.1 pp over all 4,760 fresh cells; the lab reads +162 HP a fork for it.

---

## 1. The fights that decide games

**Data.** W2's run `1119-150554-ab-6083` (main against awubot, `goal`, seed base 9201, 570 cells, every replay), its
classes and mechanism groups (W2 §2, W1's `mech.py`). `D:Engage.java` reads every replay into engagements: a combat
event is a bite or grab of an enemy, a throw collision, or a rat trap fired; events within 8 rounds and Chebyshev 6 of
an engagement's centre join it. Each robot's HP drop between its turns, and the HP it held when it died, is charged to
the engagement within Chebyshev 8 (cat scratches and pounces and a king's starvation are not). Per engagement: our and
their babies within 5 of the first event and their HP, kings within 8, global cheese, bites, grabs, throws, traps
fired, HP lost, babies and kings dead, who acted first, wall and dirt in the 7×7. 49,896 engagements in 1,140 games.
`D:step1.py`, `step1b.py`, `step1c.py`.

**Kinds** (by the first event's surroundings): a skirmish (≤ 2 rats in all, or one a side), lopsided (one side has
one), a group fight (≥ 2 a side, no king), at our king (our king within 8, theirs not), at their king, both kings.

### 1.1 We still win the exchange in our losses, by half as much

Per game, the babies exchanged (theirs dead − ours) and the HP exchange (HP they lost − HP we lost) in engagements
starting in each window:

| group (games) | r0–100 babies / HP | r100–200 | r200–300 | r300–500 |
|---|---|---|---|---|
| wins (952) | +4.8 / +445 | +3.7 / +379 | +2.8 / +292 | +6.5 / +629 |
| lost fight-early (54) | +3.1 / +113 | +1.5 / +3 | +0.8 / +71 | +1.0 / +5 |
| lost snowball (40) | +1.2 / +105 | +1.7 / +116 | +0.8 / +89 | +0.8 / +99 |
| lost COMBAT or COMBAT+starve (74) | +3.1 / +162 | +2.6 / +143 | +0.8 / +61 | +2.0 / +166 |

On the same maps (maps with both a loss of the group and a win; per map, then over maps), r0–300:

| group | maps | babies exchanged, loss / win | HP exchange, loss / win | engagements, loss / win |
|---|---|---|---|---|
| fight-early | 28 | +5.1 / +11.8 | +203 / +1,190 | 21.0 / 20.4 |
| snowball | 19 | +4.8 / +8.9 | +389 / +741 | 22.4 / 21.9 |
| COMBAT (any) | 38 | +5.6 / +11.7 | +317 / +1,181 | 19.0 / 18.6 |

- The number of fights is the same in losses and wins; the exchange per fight is what differs: half the babies, a
  quarter of the HP. This is W3's finding (games decided by the r1–300 exchange) seen fight by fight.
- The early exchange predicts the game: won 0.31 of the games where we lost 4–8 more babies than awubot by r200,
  0.69 at −1 to −4, 0.81 at −1 to +2, 0.90 above +9.

### 1.2 Where the gap sits

The r0–300 gap (loss − win on the same maps) split by kind, COMBAT losses (38 maps): all −6.1 babies, −864 HP, of
which group fights −2.8 / −265, fights at our king −1.1 / −205, both kings −1.5 / −194, at their king −0.1 / −137,
skirmishes and lopsided −0.6 / −63. Fight-early losses (28 maps): −6.7 / −988, group −2.7 / −245, at our king
−0.7 / −250, both kings −1.5 / −213. Snowballs: −4.1 / −352, group −1.9 / −164.

- **Group fights** carry the largest share of the babies; **fights at our king** the largest share of the HP: in
  fight-early losses our king loses 156 HP a game in them by r150 (wins 25), 48 HP a fight (wins 13), with the same
  share of fights outnumbered at the start (37% / 42%) and the first one at the same round (median r66 / r70).
- In fights at our king in fight-early losses awubot bites more than we do (7.2 against 5.4 a fight; wins 4.7 / 5.0)
  and we grab and throw less (3.3 grabs, 2.8 throws; wins 4.0, 3.4); awubot's grabs there are 13.3 a game against 9.2
  on the same maps' wins.
- Who starts a fight does not separate them (we start 45% of the fights at our king in those losses, 51% in wins);
  nor does terrain (wall and dirt in the 7×7: 10.6 against 12.5).
- Our last king dies inside a fight at our king in 31 of the 43 fight-early losses where it died in an engagement
  (both kings 6, group 3, their king 3); in snowballs 14 of 28 (group fights 11); with +6.9 babies exchanged in our
  favour before that fight (snowballs +12.0): the king is killed by a fight we lose after a run we won.

So the lab's score must weigh both the rat exchange and the king's HP, in the first 300 rounds, and its forks must
cover group fights and fights at our king.

---

## 2. The lab

**A fork harness on real games** (`tools/fightlab/`, its AGENTS.md). Scenario maps cannot hold the fights that decide
games: the engine accepts only kings as a map's initial bodies (`LiveMap.assertIsValid`), every unit starts at full HP
with no cheese, and a staged rat has none of a real rat's memory (map, mines, squeaked enemies), which the micro reads
(`MineEstimate` gates CT's band traps). So the lab forks real games instead:

- **The lab copy** `src/flch` (`mkcopy.py`, never committed): challenger with every `Tune` constant a field instead of a
  compile-time constant. From the round named by the property `bc.testing.fl.r0` (bots may read `bc.testing.*`), each
  robot loads `bc.testing.fl.set` ("NAME=V,...,salt=S") at the start of its turn. Before that round every arm runs the
  same code with main's values, so all arms of a fork share its state; `salt` xors S into the robot's random stream.
  Past packages' code arms ride along behind lab-only constants whose 0 is main's code (`patches/codearms.py`: CB's
  bite-damage policy `CB_MAXDMG`, CC's A3 carrier step-2 penalty `CC_STEP2`, CX's A1 graded threat `CX_PAIR`).
- **The runner** `FightLab.java`: plays jobs on the official 1.2.6 jar in one JVM (each game as `bcplay.Play` plays it:
  the cat RNG reset), records both teams' state at given rounds (babies and their HP, kings and their HP, global and
  raw cheese, cheese transferred, HP lost, cat damage, traps, dirt), stops at a round or plays to the end, and in a
  base game scans for fights.
- **Forks** (`lab.py scan`, `forks`): base games of the lab copy against awubot, both seats, on `goal` cells. An
  approach is an enemy baby rat within Chebyshev 4 of one of ours (5 from a king); the scan records the first approach
  of a place (none within Chebyshev 8 for 10 rounds) and a sample every 30 rounds of a longer fight, from the state at
  that round's end only, so a fork at the next round is chosen without knowing how the fight goes (no regression to
  the mean, docs/evaluation.md). The lab copy scores like main: 0.82 in its 300 base games (main 0.831 on `flv`'s cells).
- **Arms** (`lab.py arms`): every arm plays every fork with each salt; `null` is main's values with the same salts, and
  an arm is read against the null of the same fork and salt (paired until the arm's values first change a decision).
  Scores (`lab.py score`), ours minus theirs from the fork to r0 + 10, 20, 40, 80 (or an absolute round): babies, baby
  and king HP, kings, cheese (global + raw), transferred, the HP exchange (HP they lost − HP we lost); `labP`, the win
  probability of a logistic fit of the result on the fork's state and the window (`lab.py calibrate`, on forks played
  to the end); `same`, the share of forks whose window the arm left exactly as the null's.
- **Cost.** Java only (the native engine cannot read the properties): a job costs about 32 + 0.085 × (last round)
  CPU-s on DoC; a fork at r86 stopped at r0 + 80 about 53 CPU-s (the 11,060-job validation: 584,000 CPU-s), a fork
  played to the end 103. A native whole game costs 2.4 CPU-s (the `fls1` screen, after XC).

**Noise.** Two salts of the null on one fork give different results in 16% of forks played to the end (pilot, 112
forks): a fork played to the end is as noisy as a whole game. The lab's information has to come from the window.

## 3. Validation

### 3.1 Ten arms with whole-game prices

The lab set `lab1/val`: 553 forks in r1–300 (2 a game, 280 base games of `goal` at seed base 17003, fork round mean
r86), 10 arms × 2 salts, windows to r0 + 80 (run `run-1119-183347-3106`, 11,060 jobs). The whole-game prices: the
weight arms as `tools/tune points` campaign `flv` (`goal`, 1,000 cells, seed base 17002, paired with main: runs in
§6); the code arms from their packages' rows in docs/closed.md (older mains, smaller pools).

| arm | change | whole game (pp) | lab: HP exchange at +80 (HP a fork) | rats | cheese | labP | same |
|---|---|---|---|---|---|---|---|
| ctrev (CT revert) | `BAND_MIN_MINES` 99: no band traps | **−4.4 [−6.2, −2.6]** | **−98 [−138, −57]** | −0.09 | +0.000 | +0.0005 | 0.18 |
| cwrev (CW revert) | `THROW_HOLD` −1000: throw at the first ready turn | **−2.5 [−4.5, −0.7]** | **−52 [−82, −23]** | −0.02 | −0.010 | −0.0025 | 0.10 |
| step10 | `FIGHT_STEP` 10 | −1.5 [−3.2, +0.4] | +8 [−22, +37] | +0.03 | −0.002 | −0.0049 | 0.08 |
| threat40 | `THREAT_PENALTY` 40 | −1.2 [−3.2, +0.8] | **−172 [−211, −135]** | −0.19 | −0.026 | −0.0111 | 0.11 |
| ratnap5 | `RATNAP_W` 5 | −0.6 [−2.4, +1.1] | −30 [−63, −2] | −0.01 | −0.009 | −0.0037 | 0.23 |
| cb13 | CB: bite damage per target, cap 13 | +2.7 (L-combat, H0) | +10 [−18, +34] | −0.01 | +0.005 | +0.0025 | 0.07 |
| cb16 | the same, cap 16 | −4.8 (L-combat) | +15 [−12, +42] | −0.03 | −0.019 | −0.0060 | 0.07 |
| cxa1 | CX A1: −50 per extra grab pair | −0.7 (`goal`), +2.2 (L-combat) | **+38 [+8, +71]** | −0.05 | −0.004 | −0.0014 | 0.17 |
| cca3 | CC A3: −42 on carriers' step-2 tiles | −0.2 (faceoff ∪ L-combat) | +1 [−34, +34] | −0.01 | +0.001 | −0.0012 | 0.14 |

- **The reverts show a loss in the lab.** Reverting CT's band traps loses 98 HP of exchange per fork in 80 rounds,
  reverting CW's hold 52, both with intervals clear of 0.
- **But the lab does not rank whole-game effects.** Across the five arms measured fresh, the HP exchange correlates
  +0.17 with the whole-game effect (Spearman +0.20), the rats +0.12, the calibrated win probability −0.68 (the
  pre-registered score: its fit on 1,130 forks played to the end weighs kings, cheese and transfers, and none of the
  arms moves those in 80 rounds); over all nine with the recorded results, +0.24, +0.19, +0.32. `THREAT_PENALTY` 40 is
  the lab's worst arm (−172 HP: rats that accept grab threats lose the fight in front of them) and costs 1.2 pp of
  games; CX A1 gains 38 HP in the lab (fewer of our rats grabbed, CX's own diagnostic) and nothing in games; CB's
  cap 16 spends the cheese the lab sees (−0.019) and its HP gain does not pay for it.
- Played to the end, forks are whole games: on the pilot (118 forks × 2 salts) the result differences read ±0.06 for
  every arm, as noisy as whole games of the same count.

### 3.2 Sixteen constants, one design, in both

The same two-level resolution-IV design (tools/bctune's: 32 variants, each of 16 micro constants at main's value ±
one step) played twice: in whole games (`tools/tune` campaign `fls1`: 380 `goal` cells a variant, seed base 17011,
24,320 variant games, native) and in the lab (`lab1/scr2`: each game's first fork, 280 forks, one salt, windows to
r0 + 80 and r300; 9,240 jobs). Effects are per +1 step, CIs by map.

| constant | main ± step | games: pp per step (z) | lab: HP exchange to r300 (z) | lab: rats to r300 (z) |
|---|---|---|---|---|
| `THROW_HOLD` | 42 ± 20 | **−1.66 [−2.21, −1.09] (−5.8)** | **−85 [−110, −65] (−7.6)** | **−0.33 (−5.9)** |
| `STRAFE_LO` | 3 ± 3 | **−0.96 [−1.45, −0.49] (−3.9)** | **−112 [−137, −87] (−8.9)** | −0.14 (−2.6) |
| `CARRY_AGE` | 8 ± 2 | −0.32 (−1.5) | −24 (−2.8) | −0.05 (−1.1) |
| `FIGHT_TURN` | 2 ± 2 | −0.32 (−1.3) | −48 (−4.9) | −0.15 (−2.6) |
| `ATTACK_LOWHP` | 24 ± 12 | −0.23 (−0.9) | −15 (−1.8) | −0.03 (−0.7) |
| `TRAP_STRONG_HP` | 80 ± 10 | −0.09 (−0.6) | −30 (−2.9) | +0.03 (+0.7) |
| `FIGHT_FACE` | 5 ± 5 | −0.08 (−0.4) | +13 (+1.3) | +0.12 (+2.4) |
| `BAND_W` | 20 ± 10 | −0.03 (−0.2) | +1 (+0.2) | +0.07 (+1.5) |
| `KING_DEF_DSQ` | 36 ± 20 | +0.00 (+0.0) | −1 (−0.2) | −0.02 (−0.4) |
| `FIGHT_NOSTRAFE` | 10 ± 8 | +0.05 (+0.2) | +8 (+1.0) | +0.07 (+1.4) |
| `RATNAP_ALLY_HP` | 22 ± 10 | +0.08 (+0.4) | −38 (−4.5) | −0.11 (−2.7) |
| `FIGHT_STEP` | 30 ± 15 | +0.14 (+0.7) | −21 (−2.4) | −0.08 (−1.9) |
| `ATTACK_W` | 10 ± 6 | +0.17 (+1.0) | +14 (+1.8) | +0.03 (+0.7) |
| `STRAFE_HI` | 8 ± 4 | +0.22 (+1.0) | −24 (−2.8) | −0.02 (−0.4) |
| `ATTACK_KILL` | 30 ± 20 | +0.39 (+1.6) | +4 (+0.4) | +0.04 (+0.7) |
| `RATNAP_BACK` | 5 ± 5 | +0.38 (+1.7) | +35 (+3.2) | −0.00 (−0.0) |
| curvature (variants' mean − main) | | −3.8 pp [−6.1, −1.5] | −63 HP | −0.79 rats |

- **Over the 16 effects the lab predicts the games**: Pearson +0.81, Spearman +0.66 for the HP exchange to r300
  (+0.80 / +0.66 to r0 + 80; cheese to r300 +0.75; rats +0.76 / +0.47; king HP +0.64 / +0.31); +0.55 without the two
  large effects. Both rank `THROW_HOLD` and `STRAFE_LO` first, both negative; both find the 16 constants' joint move
  harmful (main sits near a local optimum).
- **Power for cost: games win.** The lab's z for the two large effects (−8.9, −7.6) cost 564,000 DoC CPU-s (Java);
  the games' (−3.9, −5.8) cost 60,800 (25,080 native games at 2.4 CPU-s each since XC) plus 32 builds. At equal CPU
  the games' z would be about 3× the lab's (z grows as the root of the cost): the native engine made whole games far
  cheaper than any Java lab can be. The lab's one cost advantage is that a variant needs no build.
- **Why single arms failed and the screen did not.** The screen compares 16 constants on one footing (each at ±1 step,
  averaged over the other 15, with whole-game effects known to ±0.5 pp), and its large effects act in the fights the
  window holds (throws, strafes). The ten arms differ in mechanism and reach (CT's band traps pay off on rich maps
  past the window, `THREAT_PENALTY` 40 loses fights it then does not lose games by), and their whole-game prices are
  known to ±2 pp. The lab measures what the micro does to fights; how much of that becomes games differs by mechanism.

## 4. The micro searched

The screen's two leads, moved alone from main, in whole games on fresh seed bases (paired with main):

| variant | `fls2`, seed base 17012, 760 cells | `fls3`, seed base 17013, 1,000 cells | pooled (1,760 cells) |
|---|---|---|---|
| `STRAFE_LO` 0 | +0.020 [+0.001, +0.039] | −0.012 [−0.031, +0.007] | +0.002 |
| `THROW_HOLD` 22 | +0.019 [+0.003, +0.034] | −0.013 [−0.033, +0.007] | +0.001 |
| both | +0.017 [−0.001, +0.034] | −0.003 [−0.025, +0.018] | +0.006 |
| `THROW_HOLD` 30 | +0.013 [−0.007, +0.032] | | |
| `THROW_HOLD` 10 | +0.009 [−0.008, +0.026] | | |
| `STRAFE_LO` 1 | +0.009 [−0.011, +0.028] | | |

- On these two seed bases neither single move holds up. The screen's slopes are averages over a region where 15
  other constants also move (curvature −3.8 pp), so a slope there need not hold at main.
- **The lab on the same single moves** (`lab1/pts`: 280 first forks × 2 salts, run `run-1119-195611-03535f`), each
  against the null of the same fork and salt:

| variant | HP exchange to r0 + 80 | HP exchange to r300 | rats to r300 | king HP to r300 | same (r300) |
|---|---|---|---|---|---|
| `STRAFE_LO` 0 | **+87 [+51, +124]** | **+162 [+66, +265]** | −0.07 [−0.50, +0.37] | +27 [−6, +58] | 0.06 |
| `THROW_HOLD` 22 | −3 [−39, +34] | −2 [−89, +80] | −0.01 | −1 | 0.12 |
| both | +98 [+56, +142] | +166 [+73, +262] | +0.21 | +16 | 0.04 |

  The lab reads `THROW_HOLD` 22 alone as doing nothing (its screen slope came from the other constants' region) and
  `STRAFE_LO` 0 as a clear gain in fights: cheaper strafes win HP in the fight at hand. Whether that becomes games is
  the question the validation (§3.1) says the lab cannot answer, so it went to the precision route.

### 4.1 `STRAFE_LO` 0, the precision route

- **Confirmation**: `goal`, 3,000 cells, seed base 17014 (fresh), against main (`99ef794`, 0.821 there):
  **+0.017 [+0.006, +0.028]**, 505 cells better, 409 worse (run `1119-200722-ab-bcaf`, branch `t3/fl-strafe0`
  `42ad4b2`). Its lower bound clears 0, the precision route's rule; the decision rule was fixed before the run.
- **Its screens disagree**: `fls2` +0.020 (17012, 760 cells), `fls3` −0.012 (17013, 1,000 cells). Over all 4,760
  fresh cells (fls2, fls3 and the confirmation) +0.011. The whole-game screen's slope (−0.96 pp per +3, z −3.9, 17011)
  and the lab's fight-level gain (+162 HP a fork) point the same way.
- **The change**: a strafe from a movement cooldown below 2 costs nothing in TryAttack, TryRatNap and TryTrap
  (`CombatCommon.strafeCost`, which charged 3 there and 8 from 2 up; TryFight never charges it). With the next move
  still free after such a strafe, those generators now prefer a strafing bite, grab or trap over a forward one only
  on the other scores; at 8 (cooldown ≥ 2) a strafe still costs the next turn's move. One instruction operand changes
  (`iconst_3` → `iconst_0` in `CombatCommon.strafeCost`).
- **Smoke** (`1119-201829-ab-3e4a`): no robot of ours killed by its own exception (awubot 6). **Gauntlet**
  (`1119-201828-gauntlet-e9c6`, harm tests against main): no harm; OBG +0.023 [−0.038, +0.083] (107 cells), GST +0.000
  [−0.039, +0.040] (104); `quick` opponents pass, `opp_cerr` (−0.016), `opp_gravy` (−0.047 [−0.125, +0.000]: 2 worse
  cells of 32) and `awubot_quals` (1 worse of 32) end inconclusive at their 32-cell cap.
- On the branch: `t3/fl-fight-lab` carries `STRAFE_LO` 0 (the bot equals the tested `42ad4b2`).

## 5. Against docs/closed.md

- New rows (FL): the ten validation arms' whole-game prices (CT's band and CW's hold load-bearing; TN2's three
  re-measured), the 14 flat constants of the screen, the two leads that did not confirm, and the lab as a predictor
  of single arms.
- Reopened: none. The lab confirms CB's and CX's finding that fight-level gains need not become wins (CX A1: +38 HP a
  fork, no games; `THREAT_PENALTY` 40: −172 HP, −1.2 pp).

## 6. Tools, data, runs

- Tools: `tools/fightlab/` (AGENTS.md there): `mkcopy.py`, `patches/codearms.py`, `FightLab.java`, `task.sh`,
  `lab.py` (`scan`, `forks`, `arms`, `screen`, `fetch`, `calibrate`, `score`, `fit`). Lab data `L:pilot/`, `L:lab1/`
  (forks, arm sets `val`, `scr2`, `pts`; `scr1` is the broken first screen, §6.1), `L:calib.json`.
- Step 1: `D:Engage.java` (engagements of a replay), `D:step1.py`, `step1b.py`, `step1c.py`, output `D:w2_eng.jsonl`.
- Runs (remote, `tools/run`): pilot scan `run-1119-175438-0788`, pilot arms `run-1119-180316-0592`, lab1 scan
  `run-1119-181954-73fc`, validation `run-1119-183347-3106`, screen `run-1119-193047-b790` (broken first screen
  `run-1119-190210-7248`), single moves `run-1119-195611-03535f`.
- Whole games (`tools/tune`): `flv` (ref `1119-180056-ab-3189`; cwrev `1119-182122-ab-915e`, ctrev
  `1119-180927-ab-3e52`, ratnap5 `1119-181548-ab-b2f6`, threat40 `1119-182233-ab-ec25`, step10 `1119-182343-ab-031b`),
  `fls1` (ref `1119-190136-ab-b465`, 32 variants), `fls2` (ref `1119-193031-ab-a436`; h30 `1119-193227-ab-da6f`, h22
  `1119-193327-ab-a72d`, h10 `1119-193441-ab-1dcb`, s0 `1119-193452-ab-9603`, s1 `1119-193637-ab-a7fa`, h22s0
  `1119-193735-ab-9e6f`), `fls3` (ref `1119-194333-ab-203c`; s0 `1119-194333-ab-e552`, h22 `1119-194334-ab-e24a`,
  h22s0 `1119-194334-ab-4b1b`). Seed bases used: 17001–17003, 17011–17013.

### 6.1 Traps

- The sandbox refuses reflection: `String.split` into more than 10 parts grows an `ArrayList` by `Array.newInstance`
  and throws. The lab's first screen (`scr1`) lost every 16-constant variant to it (all 32 played one "no values"
  game; caught because their effects were all exactly 0). The loader now parses with `indexOf`.
- The kit's engine is 1.2.5 (`engine_version.txt`) and `build/classes` holds the `src/battlecode` overrides first:
  the runner uses its own classpath (the official 1.2.6 jar shipped in `tools/fightlab/lib`, the bots as team URLs).
- A Tune constant copied into another class's `static final` freezes at class init; mkcopy leaves those final (the
  formation constants), so they are not settable in the lab.
