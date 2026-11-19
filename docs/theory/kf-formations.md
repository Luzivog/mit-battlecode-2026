# KF: our king formations on rich maps

Package KF (plan §4.3), measured on main `b962918` (bot source identical to `9bfd416`, XA's main) against awubot.
"Rich" is a map with ≥ 8 mines; "early" is a formation by r1200 (kings formed then pay upkeep to the end, rules.md §6).
Source keys: `C:` `src/challenger/`; `D:` data and tools in `~/battlecode26-data/scratch/kf/` (§6).

## 0. Summary

1. **The race is lost before the formation code runs.** On rich maps we form first in 348 of 816 probe games and win
   0.86 of them; awubot first, 0.66; neither, 0.70. But in 138 of 195 rich-map losses we never start a normal or
   lock-in formation by r1200: the gates never open.
2. **The rat gate binds, and it is the bank.** A second king needs 20 rats; the spawn cap holds 16 until global
   reaches the rich spawn line (2000 − t/2, `C:robot/king/KingSpawningStrategy.java`). In rich losses where we did not
   form first, our first king's coordinator stood at "< 5 known mines" in 74% of r0–200 king-rounds, but with fewer
   than 20 rats in all but 2%; in r400–800 the rat gates (fewer than 20, or fewer than 12) held 51–57%. Relaxing either
   gate loses: forming with 16 rats once the bank holds 1,000 on rich maps scored −0.127, counting the map's mines
   instead of the known ones −0.040 (§3).
3. **The known-mine gate is a map-control signal.** With the map's mines counted, the probe formed more kings (0.87 →
   1.06 a rich game), but more died within 300 rounds (73 → 127) and timed-out attempts doubled (rat-rounds wasted in
   them per lost rich game 795 → 1,330). Fewer than 5 known mines means the known ones are far or contested.
4. **Our attempts are wasteful, but the waste does not decide games.** Rich-map attempts complete 0.75 in wins, 0.46 in
   losses. A timed-out attempt restarts the next round at the same site (138 of 190 timeouts; 73 of those time out
   again), with ~20 rats in formation and enemies within d² 20 of the site for a median 85 of its 121 rounds.
   Own-side sites complete 0.90, enemy-side 0.67, and kings formed on the enemy side die within 300 rounds 23% of the
   time (own side 3%). Skipping a timed-out site and preferring own-side sites moved those numbers (§3) but no score.
5. **Two map clusters are formation bugs.** rose (cooperation to r2000): the cooperation search finds no own-side site
   all game and main loses 5 of 6 at 1 king to 2. where_are_you: lock-in attempts time out at the same site three times
   in a row and we end 2 kings to 3.

## 1. Method

- **Probe.** `kf_probe` (branch `t3/kf-probes`, never merged) is main with one indicator string a king turn,
  `KF state rats knownMines GATE | event`, written by `KingFormationCoordinatorStrategy` (the first gate that failed,
  in code order, or `START`; events: start with site, mode, score and global; abort with cause; done), and `KFR` on
  each rat turn spent in `FormationStrategy`. The native engine keeps indicator strings in the replay when a spec asks
  for them (`indicators: true`; `tools/play` turns them off), so the probe needs no robot output. It plays like main:
  0.782 against main's 0.784 on the same 570 `goal` games.
- **Games.** The 285 cells of XA's runs (`goal`, seed base 0, 3 passes) and the 475 cells of TN's `goal` run at seed
  base 61002: 760 cells, 1,520 games (816 on rich maps), played on Imperial (`D:wt/kfprobe/kfplay.py`, run
  `run-1119-125243-e1e1`). Main's own 379 replays (XA's `1119-103009-ab-7431`) give the same picture where they
  overlap (§2.1).
- **Reader.** `D:KFStats.java` per game: every formation (both teams), every king's fate (HP lost to starvation,
  bites, scratches), our coordinator's gates and events, rats in formation per round (and within d² 8 and 2 of the
  site, enemies within d² 20), our kings' positions at each start. Positions only from robots that took a turn
  (XA's ghost rule).
- **Gates**, as the coordinator checks them (`C:robot/king/KingFormationCoordinatorStrategy.java`): `RMIN` fewer than
  12·kings rats (aborts any formation), `CUT` the engine's cutoff, `DANGER` king below 100 HP with one king (skips the
  gates), `CHEESE` global < 150, `LCHEESE` lock-in global < 1000, `MINES` known mines below {5, 9, 15}[kings − 1]
  before r1200, `RATS` fewer than 20·kings, `LRATS`/`CRATS` lock-in and cooperation floors, `ACTIVE` another king's
  formation, `COOPKEY` a cooperation search that found nothing, unchanged inputs, `SITE` no site beyond the floor.

## 2. Step 0

### 2.1 Who forms first (by r1200)

| mines | we form first | awubot first | neither |
|---|---|---|---|
| ≤ 4 | 16 games, won 0.75 | 0 | 384, 0.87 |
| 5–7 | 48, 1.00 | 10, 0.40 | 246, 0.79 |
| ≥ 8 | **348, 0.86** | **160, 0.66** | **308, 0.70** |

Main's replays (379 games): ≥ 8 mines 88 (0.86), 40 (0.72), 75 (0.67). Both sides form at about the same bank: our
first formation at a median 1,792 global in rich wins and 1,824 in losses, awubot's at 1,823 and 1,776. Our bank on
rich maps (main's replays, medians): r100 1,688 (wins) / 1,670 (losses), r300 1,672 / 1,017, r400 1,690 / 646; awubot's
in our losses r300 1,576, r400 918. In wins our bank sits under the rich spawn line from r100 to r700 with the
16-rat army.

### 2.2 Attempts (by r1200)

| mines | outcome | games | attempts a game | done | timed out | aborted (cause) | open | start: round / global / rats (quartiles) | start → king (rounds) | site own / mid / enemy |
|---|---|---|---|---|---|---|---|---|---|---|
| ≤ 4 | won | 345 | 0.04 | 12 | 0 | 2 (RMIN) | 1 | 103/103/104 / 2089/2156/2276 / 12 | 3/4/5 | 12/0/3 |
| ≤ 4 | lost | 55 | 0.07 | 4 | 0 | 0 | 0 | 103/104/104 / 2249/2269/2286 / 12 | 3/3/4 | 4/0/0 |
| 5–7 | won | 247 | 0.26 | 54 | 7 | 1 (RMIN) | 3 | 314/455/732 / 1208/1628/1734 / 12/16/20 | 24/56/81 | 23/18/24 |
| 5–7 | lost | 57 | 0.07 | 0 | 1 | 1 (RMIN) | 2 | 232/268/437 / 751/906/1601 / 12/12/16 | – | 3/0/1 |
| ≥ 8 | won | 621 | 1.33 | 620 | 138 | 41 (RMIN 16, CUT 11, BS 9, NC 3, ROSE 2) | 27 | 184/401/800 / 1560/1846/2052 / 16/20/28 | 16/26/46 | 345/277/204 |
| ≥ 8 | lost | 195 | 1.08 | 96 | 66 | 25 (RMIN 12, CUT 9, BS 3, NC 1) | 23 | 193/421/800 / 1339/1812/2032 / 16/20/28 | 19/29/58 | 82/85/43 |

Modes on rich maps (done / attempts): normal 385/467 (wins), 56/89 (losses); lock-in 174/250, 17/57 (28 timed out);
cooperation 58/94, 21/41; danger 3/15, 2/23 (the first king dies 0–30 rounds after a danger start). Sides are
Chebyshev distance to each starting king, "own" when ours is nearer by more than 3 (W1's `mcan.py` rule).

**Timeouts.** In rich losses: ~20 rats join (quartiles 13/20/26), a median 1 ever stands within d² 8 of the site,
enemies are within d² 20 of it in 85 of its 121 rounds, and the site is a median 40 tiles from our start (completed
ones: 22). The next attempt starts one round later, at the same site in 138 of 190 timeouts (73 of those time out
again; chains of up to 7). At another site the next attempt completed 39 of 52. Rat-rounds spent in formation per
rich game: in timed-out attempts 349 (wins) / 795 (losses), in completed ones 533 / 293. A completed formation took
357 rat-rounds (median); completions take 26 rounds (p80 58, p90 83, p95 99).

**Sites by side, rich maps, by r1200:**

| mode, side | attempts | done | timed out |
|---|---|---|---|
| normal, own | 178 | 0.90 | 0.08 |
| normal, mid | 236 | 0.79 | 0.13 |
| normal, enemy | 142 | 0.67 | 0.27 |
| lock-in, own / mid / enemy | 125 / 80 / 102 | 0.65 / 0.61 / 0.60 | 0.25 / 0.30 / 0.30 |

The site score (`scoreMineForFormation`) takes the known mine beyond every king's floor (d² ≥ max(400, W·H/5)) nearest
our kings and the map centre. With every mine known it would put 236 of 556 normal sites on the enemy side
(`D:sitesim.py`); without the centre term 153; preferring own-side mines beyond the floor 91.

### 2.3 Which gate held

Our first king's gate, share of its king-rounds, in rich games where we did not form first (148 losses, 320 wins):

| rounds | losses | wins |
|---|---|---|
| r0–200 | MINES with < 20 rats 0.74, RMIN 0.10, COOPKEY 0.06, RATS 0.04, MINES with ≥ 20 rats 0.02 | MINES < 20 rats 0.68, RATS 0.13, RMIN 0.09 |
| r200–400 | MINES < 20 rats 0.39, RATS 0.19, RMIN 0.14, COOPKEY 0.10, MINES ≥ 20 rats 0.07 | RATS 0.36, MINES < 20 rats 0.29, RMIN 0.13, MINES ≥ 20 rats 0.08 |
| r400–600 | RMIN 0.30, MINES < 20 rats 0.27, RATS 0.22, COOPKEY 0.07 | RATS 0.36, RMIN 0.20, MINES < 20 rats 0.16, MINES ≥ 20 rats 0.13, SITE 0.07 |
| r600–800 | RMIN 0.29, RATS 0.28, MINES < 20 rats 0.22, COOPKEY 0.10 | RATS 0.34, RMIN 0.24, MINES ≥ 20 rats 0.16 |
| r800–1200 | LCHEESE 0.32, RMIN 0.30, MINES ≥ 20 rats 0.11, COOPKEY 0.09 | MINES ≥ 20 rats 0.31, LCHEESE 0.21, RMIN 0.16 |

- The known-mine gate alone (with ≥ 20 rats) holds 2–7% of rounds in losses before r800. Known mines on rich maps
  (true median 10): r200 a median 4 in losses (68% below 5), 5 in wins (43%); r400 5 and 6.
- In 45 of the 138 no-attempt rich losses the king sat on ≥ 1,500 global with 16+ rats behind MINES and RATS
  (11,978 king-rounds in r100–800). Only 12–18 of the 138 ever had RATS alone holding with ≥ 1,000–1,500 global.
- The third king (lock-in, 9 known mines): in rich games ending at r2000 with 2 kings to 2 (99 games, 26 lost), MINES
  held 66% of r800–1200 king-rounds; 2 kings to 3 (9 games, all lost), 55%.
- COOPKEY during long cooperation is rose's (§2.5); the normal gates would not have passed there either.

### 2.4 The formed kings (by r1200)

| mines | outcome | kings | at: round / global / babies (quartiles) | to our nearest king / the enemy's (median tiles) | died by the end | within 300 rounds | HP lost a king: starvation / bites / scratches |
|---|---|---|---|---|---|---|---|
| 5–7 | won | 54 | 419/577/831 / 958/1460/1615 / 16/16/20 | 22 / 24 | 12 | 3 | 3 / 58 / 59 |
| ≥ 8 | won | 616 | 220/392/807 / 1552/1802/1910 / 17/22/30 | 24 / 26 | 106 | 40 | 0 / 63 / 67 |
| ≥ 8 | lost | 96 | 207/325/654 / 1409/1815/1874 / 16/21/25 | 22 / 25 | 58 | 33 | 16 / 177 / 88 |

By side (rich maps, all outcomes): own 309 kings, 16% die (3% within 300 rounds); mid 250, 24% (12%); enemy 153, 33%
(23%). Formed kings rarely starve: the 20-rat and bank gates already make formations rich.

### 2.5 Rich-map losses with fewer kings at the end

Of 77 rich points losses, 32 end with fewer kings than awubot (1 v 2: 23, 2 v 3: 9) and 39 level (2 v 2: 27, 1 v 1:
12). Of 118 rich king-kill losses, 89 never had a second king (ending at r246/550/738). Clusters:
- **rose** (6 of 6 probe losses, main 5 of 6): cooperation to r2000, the king knows 3–4 of 8 mines, none on our side
  under every symmetry still possible; `COOPKEY` all game. Cooperation mode replaces the lock-in and the late refill,
  so we never form; awubot forms at ~r1900 and wins 2 to 1.
- **where_are_you** (4 losses, 2 v 3): lock-in attempts at r800, 921, 1043 time out at one site, then the cutoff.
- **longwayaround, fullofsecrets** (5 losses, 2 v 3): the third king waits on 9 known mines all lock-in.
- **diamond** (5), FreeTheCheese, toomuchcheese, Meow, mushroom: `RMIN` and `LCHEESE`, the army and bank are gone.

## 3. Arms

Gain tests against main (`challenger@b962918`), awubot, `mines-ge8`, seed base 7101, δ 0.05, unless noted.

| arm | change (`C:robot/king/KingFormationCoordinatorStrategy.java`) | result | mechanism (probe) | run |
|---|---|---|---|---|
| T | skip the site of the last timed-out formation | +0.000 [−0.018, +0.018], H0 (83 cells, 1 better, 1 worse); where_are_you, 40 cells at seed base 7104: +0.013, H0 (7 / 6) | where_are_you: the next site times out too | `1119-130507-ab-70ee`, `1119-132208-ab-5997` |
| S1 | delete the site score's pull to the centre | −0.019 [−0.053, +0.000], H0 (77 cells, 0 / 3) | – | `1119-130510-ab-cb4f` |
| S2 | among sites beyond the floor, prefer own side (`isOnOurSide`) | −0.019 [−0.048, +0.005], H0 (106 cells, 1 / 5) | – | `1119-130514-ab-3895` |
| T + S2 | both | −0.017 [−0.050, +0.017], H0 (60 cells, 1 / 3) | rich losses: timeout rat-rounds 795 → 570 a game, enemy-side sites 43 → 23, timeouts 66 → 49, formed kings dead within 300 rounds 73 → 56; kings formed 0.87 a game, unchanged | `1119-130522-ab-1af5` |
| M | known-mine gate on `MineEstimate.atLeast` (the map's mines) | −0.040 [−0.075, −0.007], H0 (76 cells, 1 / 6) | kings formed 0.87 → 1.06 a game, dead within 300 rounds 73 → 127, timeouts in losses 66 → 136 | `1119-130518-ab-6390` |
| MR | M, and a second king with 16 rats once global ≥ 1,000 on ≥ 8 estimated mines | −0.083 [−0.137, −0.035], H0 (108 cells, 5 / 21) | in the first 55 cells 16 games lost (king kills r500–1500, 4 on points), 2 won: a formation takes 6 of the 16 rats | `1119-131509-ab-c8a6` |
| M3 | M for the third king only (9 known mines) | −0.008 [−0.026, +0.000], H0 (62 cells, 0 / 1) | – | `1119-132045-ab-e5ab` |
| K | the site is the mine beyond the floor nearest the map centre (KB-centre) | −0.009 [−0.030, +0.013], H0 (117 cells, 2 / 4) | every mine known: 303 of 556 normal sites on the enemy side | `1119-132454-ab-109d` |
| **C** (merged candidate) | the cooperation mode (own-side sites only, no rat or mine gate) ends at the lock-in round; a cooperation game then forms under the lock-in's rules | rose, 40 cells at seed base 7102: **79–1 against main's 47–33 on the same cells, +0.400** (25 better, 0 worse; H1 at δ 0.20); we end with 2 kings in 80 of 80 games (main: 1 king in 48); `goal` harm, seed base 7103: H0 (no harm), +0.004 [+0.000, +0.011] (137 cells, 1 better, 0 worse) | rose is the only map where a cooperation search fails past r800 (probe: 10 of 1,520 games at r800, 7 of them rose) | `1119-132048-ab-f61e`, `1119-132052-ab-e929` |

**C, the merge protocol** (branch `t3/kf-rich-map-king-formations`, bot code = `t3/kf-arm-c`; main `b962918`):
- `goal` gain, δ 0.05, seed base 7107: H0, +0.006 [+0.000, +0.020] (158 cells: 2 better, 0 worse) (`1119-132722-ab-84c9`).
  Rose is one map of 95; at +0.40 there the expected gain on `goal` is about +0.4 pp, below what δ 0.05 resolves.
- `goal` harm, seed base 7103: no harm, +0.004 [+0.000, +0.011] (137 cells, 1 / 0) (`1119-132052-ab-e929`).
- `teammade` harm, seed base 7105: no harm, +0.014 [+0.000, +0.043] (71 cells, 2 / 0) (`1119-132529-ab-4f5f`).
- Gauntlet (`1119-132722-gauntlet-629d`): no harm against OBG (101 cells, all equal), GST (+0.007, 147 cells, 2 / 0)
  and 10 of the 11 others; `awubot_us` inconclusive at the cap (32 cells, 1 worse: TheHeist seat A, a cooperation
  game past r800 with one king, lost 2 kings to 3 at r1067 where main won 2 to 2); a harm test against `awubot_us` on
  `quick` at seed base 7108 passed with all 32 cells unchanged (`1119-134929-ab-88df`).
- Smoke (`1119-132723-ab-9ab3`): 8 cells unchanged, none of our robots killed by its own exception.

## 4. What binds

- **Wealth, through the 20-rat gate.** Both bots form at about 1,800 global, where the rich spawn line lets the army
  pass 16. In rich losses our bank falls from r200 (1,381 → 646 by r400) while awubot's holds; in wins it sits at
  ~1,650. 138 of 195 rich losses never open the gates by r1200, and the gates that hold are the rat count and the
  known mines, together. Opening either one forms kings into weakness: MR's army collapses (−0.083), M's extra kings
  form at contested sites and die (−0.040). The race is won by the economy that gets a side to the line first, not by
  the formation code. That is MC's and TN's ground (TN's EXPLORE_P 0.6 gains on ≥ 8-mine maps).
- **The formation code is efficient where it matters.** Conversions consume the minimum (6 rats in 211 of 240), kings
  formed by r1200 rarely starve (0 HP a king in rich wins, 16 in losses), and failed attempts concentrate in games we
  attempt (9 of 195 rich losses attempted and formed nothing). Site and timeout fixes move their diagnostics and no
  score.
- **Two clusters were real bugs.** rose: the cooperation mode outranked the lock-in and the late refill for the whole
  game. Arm C (cooperation mode only before the lock-in round) fixes it (§3). where_are_you: repeated lock-in timeouts
  at one site; arm T moves the next attempt elsewhere, and that site times out too (§3): every site there is contested.

## 5. Next

- The rich-map economy before r400: income share decides the long games (W1 §3.2), and formations follow it.
- The danger formation (first king below 100 HP, alone) completes 4 of 40 times in rich losses (all rounds): the king dies 0–30
  rounds after the start, before rats gather at a far site. An emergency site near the rats, started at a higher HP,
  is untested. 89 of 118 rich king-kill losses never had a second king (ending at a median r550).
- The third king on rich maps waits on 9 known mines (§2.3), but opening that gate (M3) changed 1 cell in 62: the
  lock-in's cheese and rat gates hold next. More kings on rich maps come from a richer economy, not from the gates.

## 6. Data and tools (`~/battlecode26-data/scratch/kf/`)

- `wt/`: worktree of `t3/kf-probes`: `src/kf_probe` (main + indicator strings), `kf_pts2` (T + S2), `kf_pm` (M);
  `kfprobe/kfplay.py SPECS OUT BCX_GZ` plays specs natively with indicator strings on a node (`tools/run --kit --each
  SPECS --chunk 4 -- python3 kfprobe/kfplay.py {file} {out} GZ`; `tools/games build PKG --remote` prints GZ).
- `KFStats.java` (`KFStats OUT.jsonl OURPKG replay...`, compiled into `cls/` against the 1.2.6 jar); `p1.jsonl` (the
  probe's 1,520 games), `ts2.jsonl`, `pm.jsonl` (the arm probes), `m2.jsonl` (main's 379 replays).
- `kfan.py` (who forms first, formations by class), `kfgates.py` (attempts, gates), `step0.py` (§2 tables),
  `mech.py` (an arm probe against the base), `sitesim.py` (site-score variants with every mine known).
