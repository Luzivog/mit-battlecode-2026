# CD: denying awubot the centre mines

Package CD (docs/theory/w2-classes.md §5, rank 1), measured on main `99c4fd9` (bot source as W2's `1557b1b`) against
awubot.

Source keys: `A:` `src/awubot/`; `C:` `src/challenger/`; `E:` the engine at tag `engine.1.2.6`, under
`engine/src/main/battlecode/` of a clone of the engine repo (docs/rules.md); `D:` data and tools in
`~/battlecode26-data/scratch/cd/` (§5). A centre mine is one whose Chebyshev distances to the two starting kings
differ by at most 3; its box is Chebyshev 4 around it (W2 §1). "Rich long" games are on maps with ≥ 8 mines and last ≥ r600; their losses and wins
are averaged per map over the maps holding both (W2's same-map rule). Windows are r100–600 (W2's race window) unless
stated.

## 0. Summary

1. **awubot takes the centre where we are not.** In our rich long losses awubot has 2,023 centre-box rat-turns a
   game against our 1,017, from 2.3× our visits (238 against 105, each ~10 rounds long). 82% of its centre-box
   rat-turns and 90% of its centre pickups (3,391 a game) happen with none of our rats in that box. 30% of its
   centre-box rat-turns come with an awubot king within 6 of the mine (ours 10%). So nothing our rats do where they
   meet awubot's miners can reach most of its centre take.
2. **The ceiling needs a dose no arm can buy.** Halving awubot's take (the dose of W2's half-denial oracle, +1.9 pp
   against main) means removing ~1,700 cheese a game. A trap hit on an awubot rat in a centre box costs it ~10 centre
   pickups in the next 30 rounds against its box-mates (and +20–27 pp death): ~170 hits, 3,400 cheese of traps, a
   game, against our income of ~4,250 by r600 in those games. Interception and dirt do less (§2).
3. **The three arms, built and read on replays against main on the same 118 cells (`L2-rich-long`, seed base
   93101), all failed the diagnostic**: none cut awubot's centre pickups (W2's bar).
   - (a) traps toward awubot's side: +3.3 traps a game in centre boxes, awubot's centre pickups −2.5% [−7.6, +2.1];
     its traps waited a median 55 rounds and 56% never fired, and it displaced combat traps.
   - (a2) the same at the busiest tiles (1 and 2 steps out): awubot's centre pickups +3.9% [−2.1, +10.2], ours
     −5.2%, our share at r500 −1.1 pp [−2.0, −0.3]. Its traps fired no sooner (44%, median 58 rounds): our rats
     are alone in a centre box when awubot is not using it, so they trap boxes we already hold.
   - (b) carrier targeting: awubot's centre pickups −1.2% [−6.2, +2.6]; carriers dying before delivery +0.03 a game.
   - (c) a dirt fence: awubot's centre pickups +5.3% [+0.4, +11.1], ours −3.9%, our share at r500 −0.9 pp
     [−1.6, −0.2].
   None went to a gain test; their paired scores on the diagnostic cells read H0 at δ 0.10 (+0.017, −0.004,
   +0.004, −0.009).
4. **CD is closed for these mechanisms.** The +8 pp of W2's oracle is real, but it is the value of awubot's rats
   leaving the centre cheese to ours at no cost to us; buying any part of it with traps, bites or dirt costs at least
   what it denies (§2), and each arm left awubot's take where it was. What is left is
   awubot's centre king, which our rats also rarely reach (§4).

---

## 1. The mechanism (`D:CDStats.java`, main's W2 run `1119-150554-ab-6083`)

Per game, r100–600; our losses (54 games) and wins (110) on the same rich long maps with a centre mine, ours /
awubot's (`D:cdan.py main.jsonl --samemap --win 1,2`):

| | our losses | our wins |
|---|---|---|
| rat-turns | 8,041 / 9,364 | 9,862 / 8,121 |
| rat-turns in centre boxes | **1,017 / 2,023** | 2,095 / 1,261 |
| — with an enemy rat in the same box | 29% / **18%** | 16% / 27% |
| — with an enemy rat within d² 20 / Chebyshev 2 | 32%, 22% / 19%, 12% | 17%, 12% / 29%, 20% |
| — with an own king within 6 of the mine | 10% / **30%** | 23% / 19% |
| pickups in centre boxes | 2,079 / **3,391** | 3,913 / 2,132 |
| — with an enemy rat in the box | 13% / **10%** | 9% / 14% |
| king pickups at centre mines | 58 / 209 | 211 / 107 |
| cheese spawned at centre mines | 6,332 | 6,421 |
| entries into a centre box (visits) | **105 / 238** | 254 / 140 |
| — from the own side / the enemy side | 64%, 28% / 61%, 28% | 59%, 30% / 66%, 24% |
| rounds per visit; pickup per visit | 12.4, 19.6 / 10.3, 14.3 | 10.0, 15.5 / 11.0, 15.3 |
| exits beyond Chebyshev 6 holding ≥ 20 raw | 25 / 33 (1,952 raw) | 35 / 28 |
| — an enemy rat within d² 20 / Chebyshev 2 at the exit | 6%, 3% / **10%, 5%** | 5%, 2% / 9%, 4% |
| — died before delivering | 9% / 10% | 6% / 13% |
| carriers' rat-turns after a centre exit, until delivery: an enemy within Chebyshev 2 (r100–1000) | 3% / 5% | 3% / 6% |
| deaths in centre boxes (by trap, by bite) | 13.8 (26%, 25%) / 18.9 (35%, 19%) | 17.2 / 19.7 |
| rat traps placed (in centre boxes); hits landed in centre boxes | 69 (20); 21 / 47 (13); 13 | 86 (24); 24 / 45 (12); 13 |
| digs within 6 of a centre mine | 9 / 22 | 15 / 14 |
| king-turns at a centre mine; kings formed there | 45, 0.1 / 169, 0.4 | 157, 0.6 / 90, 0.2 |

- **Presence is visits.** awubot sends 2.3× our visits in our losses and we send 1.8× awubot's in our wins; a visit
  lasts about as long and picks about as much on both sides. The two sides rarely share a box: 82% of awubot's
  centre-box rat-turns in our losses have no rat of ours in that box, and 90% of its centre pickups. All centre-map
  games give the same picture (18.7% and 11.0%).
- **The centre king is a third of it.** 30% of awubot's centre-box rat-turns (20% of its pickups) have an awubot king
  within 6 of the mine, plus 209 a game of the king's own pickups; ours 10% in losses, 23% in wins.
- **Its carriers leave unseen.** One of our rats is within Chebyshev 2 at 5% of awubot's loaded exits and 5% of its
  carriers' turns until delivery: 1–2 exits a game.
- **Traps.** We already place 20 traps a game in centre boxes and land 21 hits there. A hit on an awubot rat in a
  centre box (r100–1000, rich long losses, 31.5 a game) against awubot rats in the same box that round, not hit
  (35.3): centre pickups in the next 30 rounds 10.0 against 20.9, died within 30 rounds 82% against 55%. With none of
  our rats within d² 20 of the victim (2 hits a game): 14.4 against 23.4 pickups, 60% against 41% dead. Inference:
  the victims are not a random draw (they walked into a fight), so ~10 pickups a hit is an upper estimate.
- **Dirt.** awubot's rats path through dirt from global 1,500 (`A:Pathfinder.java.jinja2:19-22`); awubot's global
  is ≥ 1,500 in 46–60% of our rich long losses at r150–600 (W2's econ series). We dig 53 dirt a game by r600 and
  place 7, so a fence has material.
- **Where awubot steps** (`D:TileTraffic.java`, rich losses, per centre mine, r100–600): a tile 1–2 steps from the
  mine toward awubot's start has ~50 awubot moves end within its 3×3 (a trap's trigger area), one every 10 rounds;
  tiles on our side ~30. Our rats stand within Chebyshev 1 of such a tile 20–30 turns per mine.

## 2. What each lever can reach, from §1

| lever | what it touches | upper bound on awubot's centre take | cost |
|---|---|---|---|
| traps | every awubot step into a trap's 3×3; a trap fires once | ~10 pickups a hit (§1); halving needs ~170 hits a game | 20 cheese a trap: 3,400 a game for the half |
| interception | awubot carriers with one of ours within Chebyshev 2: 1–2 of its 33 loaded exits a game, 5% of carrier turns | the ~60 raw each of those carries, ~100 a game, if every one died | none, but it only reorders targets our rats already have |
| dirt | awubot's approach; it detours (global < 1,500) or digs (≥ 1,500: 5 cheese) | a few rounds a visit; cheese on or behind dirt stays pickable (`E:world/RobotControllerImpl.java:461-477`) | our team dirt (free); our own rats dig it too |

## 3. The arms

Diagnostic run per arm: `tools/games ab challenger awubot --pool L2-rich-long --seed-base 93101 --min-cells 118
--max-cells 118 --replays all --remote` (2 passes, 25 maps, 20 with a centre mine). Main's games on these cells
are the reference run's (`1119-163543-ab-75a1`, role r; main's bot source did not change between the runs).
`D:cdpair.py`: paired per game (196 games on the 20 centre maps), arm − main, 95% CI by bootstrap over maps.

All arms share the base (`C:util/CentreMines.java`): the first king writes its start to shared slot 15 on round 1;
a rat that knows the symmetry mirrors it to the enemy's start and classes its known mines (2×2-rounded) as W2 does.

| r100–600, per game | main | (a) traps | (a2) busiest tiles | (b) carriers | (c) dirt |
|---|---|---|---|---|---|
| awubot centre-box rat-turns | 1,547 | −4.4% [−9.9, −1.1] | +3.8% [−3.1, +12.8] | −1.7% [−7.0, +1.6] | +4.6% [−3.2, +14.3] |
| **awubot centre pickups** | 2,758 | **−2.5% [−7.6, +2.1]** | **+3.9% [−2.1, +10.2]** | **−1.2% [−6.2, +2.6]** | **+5.3% [+0.4, +11.1]** |
| awubot king pickups at centre mines | 112 | +8.9 | +10.1 | +5.2 | +26.3 |
| awubot deaths in centre boxes | 16.0 | −0.9 | +0.0 | −0.7 [−1.4, −0.2] | −0.2 |
| our centre pickups | 3,489 | −3.1% | −5.2% [−11.0, −0.1] | +0.7% | −3.9% [−8.3, −0.0] |
| our rat-turns | 9,389 | −1.0% | −2.7% [−4.6, −0.9] | +0.8% | −0.7% |
| our rat traps (in centre boxes) | 67.6 (18.5) | +2.3 (+3.3) | +3.2 (+4.8) | −0.6 (−1.1) | −1.9 (−1.2) |
| our dirt placed / digs within 6 | 2.1 / 15.4 | | | | +9.1 / +2.7 |
| our share of cheese transferred r300 / r500 | 0.531 / 0.554 | −0.005 / −0.004 | −0.006 / −0.011 [−0.020, −0.003] | +0.003 / +0.004 | −0.001 / −0.009 [−0.016, −0.002] |
| score on the 118 cells (main 0.754), GSPRT δ 0.10 | | +0.017 [−0.021, +0.060], H0 | −0.004 [−0.034, +0.027], H0 | +0.004 [−0.024, +0.034], H0 | −0.009 [−0.040, +0.024], H0 |
| run | | `1119-163543-ab-75a1` | `1119-164807-ab-523a` | `1119-163645-ab-7f56` | `1119-163738-ab-91f2` |

- **(a) traps on awubot's side** (branch `t3/cd-a-traps`, `C:robot/rat/CentreTrapStrategy.java`): in r100–600,
  outside cooperation, with raw ≥ 20 or global ≥ 400 and fewer than 15 team traps, a miner in a centre box that sees
  the tile 2 or 3 steps from the mine toward the enemy's start without our trap walks there (≤ 3 steps), faces it
  and traps it (the two areas overlap: a rat walking in takes both, 100 damage). It placed 985 traps outside fights in
  236 games, 887 of them on awubot's side (`D:TrapLife.java`; main: 10): 44% were triggered, a median 55 rounds after
  placement, against 94% within a round for combat traps; combat traps fell 3,608 → 3,286 (the action and the cap).
  Hits landed in centre boxes +0.1 a game.
- **(a2)** as (a), with the sites 1 and 2 steps from the mine (the busiest tiles of §1) and a team cap of 20
  (branch `t3/cd-a2-traps`). 1,121 traps placed outside fights in 236 games, 88% at rings 1–2: 44% triggered, a
  median 58 rounds after placement. Our rat-turns fell 2.7% (the walks, turns and the cheese) and our centre pickups
  5.2%. Where awubot's traffic is (one step into a site's 3×3 every 10 rounds in our rich losses) our rats are
  absent; where our rats are alone, awubot's traffic is low.
- **(b) carrier targeting** (branch `t3/cd-b-carriers`): TryAttack adds 20 to an enemy baby rat holding ≥ 20 raw
  within 6 of a centre mine, TryFight faces such a rat first (`C:robot/rat/combat/moves/TryAttack.java.jinja2`,
  `TryFight.java.jinja2`). Carriers dying before delivery +0.03 a game: the targets it reorders are already in
  reach of our bites.
- **(c) a dirt fence** (branch `t3/cd-c-dirt`, `C:robot/rat/CentreDirtStrategy.java`): with team dirt, a miner in a
  centre box puts dirt on the three tiles across the approach 3 steps from the mine toward the enemy's start. It
  placed 9 dirt a game; our own rats dug 2.7 more near the centre, our centre pickups fell 3.9% and awubot's rose
  5.3%: the fence slows our rats as much as awubot's, and awubot's go round or through.

## 4. What is left

- **awubot's centre king.** A third of its centre-box presence and 209 king pickups a game come with an awubot king
  at a centre mine. In our rich long losses awubot has one there in 54% of games (180 king-turns a game in
  r100–600 over all 57 losses; our wins: 26%, 67), formed there 0.4 times a game (wins 0.2), the rest its starting king walking to a
  centre mine. One of our rats is within d² 20 of it in 4% of those king-turns (wins 14%), and we bite it 1.5 times
  a game (wins 2.4): it is as far out of our reach as XA's new kings (xa-formations §0 item 7). Its value is
  unpriced: an oracle (awubot whose kings never take a centre-mine site or target) would price it before any arm.
- **Nothing else in the three mechanisms.** A trap removes about half its cost of awubot's centre take and fires
  only where our rats already are, a bite reaches the 5% of awubot's carriers that pass ours, and dirt slows both
  sides: the take is mostly out of our sight.

## 5. Tools, data, runs

- `D:` = `~/battlecode26-data/scratch/cd/`, Java compiled against the 1.2.6 jar into `cls/`:
  - `CDStats.java` (`CDStats OUT.jsonl replay...`) and `run.sh RUN_DIR OUT.jsonl [JOBS]`: per game, team and window,
    centre-box rat-turns and pickups (with an enemy rat in the box, within d² 20, Chebyshev 2, an own king within 6),
    entries by side, visits, loaded exits and their fate, deaths in boxes by cause, traps, digs and dirt near the
    centre, king-turns and formations at centre mines, carriers' exposure after an exit, and trap hits in boxes
    against same-box controls.
  - `cdan.py CD.jsonl [--samemap] [--minmines] [--minround] [--win]`: §1's table. `cdpair.py ARM.jsonl ARM_RUN
    REF.jsonl REF_RUN`: §3's paired diffs.
  - `TileTraffic.java`: awubot moves ending in each box tile's 3×3, our rat-turns there (`tt.tsv` for main's W2 run).
    `TrapLife.java`: our centre-box traps, where, whether and when they fired.
  - Data: `main.jsonl` (W2's main run), `runs/RUN.jsonl` (the arm runs).
- Runs: main (W2) `1119-150554-ab-6083`; arm diagnostics in §3. The reference run had 3 native failures (main's
  games on EscapeTheNight and closeup, `OSError: [Errno 28] No space left on device` on a remote node), replayed on
  Java; every exactness check matched.
