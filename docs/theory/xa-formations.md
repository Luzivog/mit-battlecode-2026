# XA: awubot's king formations, what they are worth and whether we can deny them

Package XA (plan §4.3), measured on main `9bfd416` (source identical to `6be881b`) against awubot on `goal`.

Source keys: `A:` `src/awubot/`; `D:` data and tools in `~/battlecode26-data/scratch/xa/` (§6). "Early" is a formation
before r1700, "late" from r1700.

## 0. Summary

1. **awubot's formations are causal and large.** Main wins 0.784 of 570 `goal` games against awubot, 0.847 against
   awubot that never forms a king (+0.063 [+0.033, +0.098]) and 0.851 against awubot whose formations gather but never
   complete (+0.067 [+0.039, +0.100]). The gain is on maps with ≥ 5 mines (≥ 8 mines: 0.748 → 0.837); on ≤ 4 mines
   it is 0. Points losses fall 41 → 17, king-kill losses 82 → 70.
2. **Early and late formations substitute.** Denying only those started up to r1700 gives +0.023 [−0.004, +0.051],
   only those after r1700 +0.018 [+0.005, +0.033]: awubot denied one forms the other.
3. **We cannot reach the gathering.** It lasts a median 14 rounds (early) or 11 (late) from the third rat at the mine
   to the new king. Our rats come within d² 20 of the site in 21 of 103 early and 5 of 26 late formations, ≥ 3 of
   them within d² 50 in 16 and 1; we bite near the site in 8 of 129. A reactive denial that won every such fight is
   worth about 0.13 × 6.3 ≈ 0.8 pp: too small to measure on `goal`. Of awubot's 140 real attempts in 190 printed
   games, 60 complete; the 42 that time out never gather (no rat within 25 tiles to join), with our rats no nearer
   than in the completed ones.
4. **The gathering has no signature.** Crowds of ≥ 6 awubot rats within d² 8 of a mine with no awubot king near
   occur 20.8 mine-rounds a game against awubot and 21.0 against awubot without formations. A rule keyed on a
   crowd fires on ordinary mining, not on formations.
5. **The site is not predictable.** awubot's own site score, given its true king positions and every mine, names the
   formation mine 40 times in 103 early formations and 8 in 26 late ones. Our rats know neither.
6. **Dirt does not hold.** awubot's rats plan through dirt once global ≥ 1500 (`A:Pathfinder.java.jinja2:18-27`;
   formations need ≥ 20 rats, which needs that wealth), and their local move digs a dirt step when no free closer tile
   exists (`A:Pathfinder.java.jinja2:803-860`, `A:robot/rat/BabyRat.java:220-223`): a crowd digs the ring around its
   target. Inference from code, not played.
7. **The new kings that matter sit where we never go.** Of 103 early formed kings, 50 die (we win 46 of those games)
   and 53 survive (we win 22). The survivors sit 33 tiles from our nearest king (25 for those that die), on rich maps
   with 24 vs 26 babies, and our rats come within d² 20 of them a median 0 times in the 200 rounds after formation.
8. **The other XA items have no room.** awubot's king starves in 196 of 379 games, and we win 189 of them: pressure
   on a starving king acts on won games. The cat lure was closed by K2 (steering is not what scratches kings).
9. **Our own formations are worth more.** Main with its formation starts disabled scores −0.111 [−0.167, −0.063]
   against main (285 cells: 51 worse, 7 better; points losses 41 → 101). On ≥ 8-mine maps it falls 0.748 → 0.578.
   We already form about twice as often as awubot (195 kings before r1200 against its 99 in 379 games).
10. **What it means for the plan.** The rich-map gap of w1-classes §3.2 is mostly a king race: awubot's extra kings
    cost us 9 pp on ≥ 8-mine maps, ours earn us 17. awubot's come from its wealth (§2.2), not from anything near our
    rats. The lever is our economy and our own kings on rich maps (MC, KB-centre, TN's formation gates), not denial.

## 1. Method

- **Oracles.** Copies of awubot under other package names, each changed in one line, on branch `t3/xa-probes`:
  `xa_awucopy` (identical; plays the same 16 smoke games as awubot, "no behaviour change"), `xa_awunf` (the
  coordinator never starts a formation), `xa_awunc` (rats gather but never call `becomeRatKing`), `xa_awune` (no start
  up to r1700), `xa_awunl` (no start after r1700), `xa_awuprint` (prints each start, abort and completion). Main
  (`challenger@68b28c1`) played each in plain mode on `goal`, seed base 0, 3 passes (285 cells, 570 games), the same
  cells as main against awubot. Differences are paired by cell, CI by bootstrap over maps (`D:oracle_ci.py`).
- **Replays.** Main against awubot, `goal` 2 passes (run `1119-103009-ab-7431`, 379 replays), read by `D:XAStats.java`:
  every `UpgradeToRatKing` (centre, nearest mine, allies and enemies in the 3×3, both teams' kings), per mine and round
  the count of each team's babies within d² 8 and the other team's within d² 20 and 50, deaths, bites, king deaths and
  bites, and every 10 rounds each team's global cheese, babies, kings, transferred, cat damage and dirt. Positions
  come only from robots that took a turn that round (dead robots without a death record otherwise stay as ghosts,
  ~225 a game).
- **A gathering** is the run of rounds, back from the conversion, with ≥ 3 awubot babies within d² 8 of the
  formation's nearest mine.

## 2. What a formation is

### 2.1 The code (`A:robot/king/KingFormationCoordinatorStrategy.java`, `A:robot/rat/FormationStrategy.java`)

- Start (a king, every turn): global ≥ 150; up to r1800 ≥ 5 known mines with one king and numRats ≥ 20 (16 on
  ≤ 1200 tiles), after r1800 numRats ≥ 12; numRats = 4·⌊babies/4⌋. Site: the known mine (first 10, not the taboo
  box, not a king's) maximising −Σ d²(mine, king) − d²(mine, centre) − 50000 per king within d² max(400, W·H/5);
  none before r1800 if every candidate is that close. A king under 100 HP with no other king skips the gates
  ("danger").
- awubot's spawn cap is 16 rats (12 on ≤ 900 tiles before r400) unless global ≥ 2000 − t/2 (32), ≥ 3000 − 3t/4 (none), or a cat or a hit on its
  king adds 10 (`A:robot/king/KingSpawningStrategy.java:21-74`). So outside small maps a formation needs a rich awubot.
- Rats within d² 625 of the mine join; combat pre-empts the formation; any rat within d² 8 of the target (anywhere
  after r1900) converts once its 3×3 holds 7 allied babies and nothing impassable (rules.md §8).

### 2.2 How often, when, where (379 games of main against awubot)

| | games | we win |
|---|---|---|
| awubot forms first (before r1200) | 44 | 0.68 |
| we form first | 102 | 0.86 |
| neither | 233 | 0.79 |
| awubot forms at all | 103 | 0.69 (losses all on points or king kills: 32 of the 78) |

- 129 awubot formations, 0–3 a game; first one at r208 / r302 / r840 (quartiles). Ring: 7.7 allies; our rats crushed:
  1 in 129. Emergency ("danger") completions near a king death: 7.
- awubot's global cheese at conversion: median 1,784.
- **Attempts** (`xa_awuprint` against main, pass 0, both seats, 190 games, Java engine with robot output;
  `D:printrun/`). 160 starts are flickers, aborted the next turn when the coordinator's king count recovers. Of the
  140 real starts (in 56 games), 60 completed, 42 timed out after 120 rounds, 30 aborted otherwise (the rat count fell
  below 12 a king, or another king appeared), 8 were open at the end. Start to king: 14 / 26 / 53 rounds
  (quartiles). In the timeouts a median 0 awubot rats ever stand within d² 8 of the site (no rat was within 25
  tiles to join), and ≥ 3 of our rats come within d² 50 as often as in completed attempts (7 of 42 against 10 of
  57): awubot's failures are its own. 22 danger starts complete 3 times, mostly failing on global < 50. Of the
  first 46 normal starts, 28 had global above the rich line and the rest 20–32 rats already built.
- Our bites on awubot's king in the 40 rounds before the gathering: 4 of 103 early formations (base rate 0.10): our
  pressure does not trigger them.
- Late formations come at r1736–r1966 in games where awubot still has one king; we lose 8 of those 24 games, all on
  points, 6 of them 49–50 or with kings 1–2. A late king turns 2–1 into 2–2: 8.3 points under backstab weights. We
  cannot answer: with 2 kings no team may form after r1200 (rules.md §6).

### 2.3 Who sees the gathering

| | early (103) | late (26) |
|---|---|---|
| gathering length (rounds), quartiles | 9 / 14 / 25 | 6 / 11 / 17 |
| our rats within d² 20 of the site at some round | 21 | 5 |
| within d² 50 | 38 | 9 |
| ≥ 3 of ours within d² 50 | 16 | 1 |
| formations with our bites near the site | 6 | 2 |

### 2.4 After the formation (early formed kings, 103)

| | kings | we win | distance to our nearest king (median) | babies ours / awubot at formation | our rats within d² 20 in the next 200 rounds (5-round samples, median) |
|---|---|---|---|---|---|
| killed | 50 | 46 | 25 | 17.5 / 21 | 7 |
| survived | 53 | 22 | 33 | 24 / 26 | 0 |

First approach by our rats (d² 20): median 64 rounds after formation (quartiles 21 / 64 / 184), never for 32. Our
bites per formed king: median 2. Killed kings live a median 374 rounds.

## 3. The oracles

| opponent | won–lost | score | vs awubot, paired [95% CI] | our losses: king kill / points | run |
|---|---|---|---|---|---|
| awubot | 447–123 | 0.784 | – | 82 / 41 | `1119-114752-ab-b58f` |
| never forms (`xa_awunf`) | 483–87 | 0.847 | +0.063 [+0.033, +0.098] | 70 / 17 | `1119-114741-ab-ed0a` |
| gathers, never completes (`xa_awunc`) | 485–85 | 0.851 | +0.067 [+0.039, +0.100] | 68 / 17 | `1119-114746-ab-c2f9` |
| no start up to r1700 (`xa_awune`) | 460–110 | 0.807 | +0.023 [−0.004, +0.051] | 70 / 40 | `1119-120635-ab-977d` |
| no start after r1700 (`xa_awunl`) | 457–113 | 0.802 | +0.018 [+0.005, +0.033] | 81 / 32 | `1119-120631-ab-8ec5` |

By mine count (score against awubot / never forms / never completes): ≤ 4 mines 0.860 / 0.867 / 0.873 (150 games);
5–7 0.781 / 0.851 / 0.825 (114); ≥ 8 0.748 / 0.837 / 0.850 (306). Largest map gains without formations:
longwayaround 0 → 6 of 6, cheesebottles 3 → 6, where_are_you 3 → 6, peaceinourtime, uneruesansfin, warhammer,
fullofsecrets, cheddarchokehold, stareintotheabyss 4 → 6.

A pinned gathering costs awubot nothing measurable: never completing equals never starting.

## 4. The levers, against the data

| lever (brief, plan §4.3) | what it needs | measured | verdict |
|---|---|---|---|
| attack the gathering | our rats at the site within ~14 rounds | ≥ 3 of ours within d² 50 in 13% of formations; the crowd has no signature (§0 item 4) | ceiling ≈ 0.8 pp; not built |
| dirt on the 3×3 | team dirt (≥ 4 in 137 of 170 first formations, W1 run) and an empty tile next to the site before the crowd | the same presence; awubot digs its path when rich | not built |
| our rats on the 3×3 | ≥ 3 of ours in every candidate 3×3 | as above; they are crushed if one 3×3 converts | not built |
| steer a cat onto it | a cat within squeak range (r² 16) of the site | K2: squeaks steer, scratches do not follow | closed by K2 |
| pre-position at the predicted site | the site | 40 of 103 early and 8 of 26 late with full knowledge | not built |
| kill the new king | reach it | the decisive kings sit 33 tiles away, never approached | not built (a king tracker would be its own package) |
| pressure a starving king | – | 189 of 196 starving-king games are already won | no room |

## 5. Our own formations

Main with `tryStartFormation` returning at once (branch `t3/xa-our-noform`, `46d474b`), paired harm test against
main on the same 285 `goal` cells (`1119-122035-ab-00e1`): 384–186 (0.674) against main's 447–123 (0.784), diff
−0.111 [−0.167, −0.063]; 51 cells worse, 7 better, 227 equal. Our losses: points 41 → 101, king kills 82 → 85.

| mines | games | main | main without formations | awubot without formations (§3) |
|---|---|---|---|---|
| ≤ 4 | 150 | 0.860 | 0.827 | 0.867 |
| 5–7 | 114 | 0.781 | 0.728 | 0.851 |
| ≥ 8 | 306 | 0.748 | 0.578 | 0.837 |

Where the formed kings sit (379 games; distance to the side's own nearest king / the enemy's nearest king, medians):
ours before r1200 195 kings, 24 / 25 tiles, 46 died; awubot's 99, 25 / 28 tiles, 50 died. After r1200: ours 45
(30 / 23), awubot's 30 (25 / 28).

## 6. Data and tools (`~/battlecode26-data/scratch/xa/`)

- `XAStats.java` (`XAStats OUT.jsonl replay...`), compiled into `cls/` against the 1.2.6 jar; `m2.jsonl` (main's 379
  games), `nf.jsonl` (the 570 games against `xa_awunf`), `w1.jsonl` (W1's 570, ghost counts included).
- `xa.py` (teams, gathering episodes), `cmp.py RUN_A RUN_B` (per-map comparison), `oracle_ci.py BASE RUN...` (paired
  diffs by cell, bootstrap over maps).
- `Dump.java replay R0 R1 X0 Y0 X1 Y1 [EVERY]`: robots and actions in a box, per round.
- `KingAt.java`: matches `xa_awuprint` log lines (king id, round, position) to their replay.
- `printrun/`: `xa_awuprint` against main, pass 0 both seats, Java engine, robot output on (logs and replays).
