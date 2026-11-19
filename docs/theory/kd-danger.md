# KD: a second king before our only king dies

Package KD (track B), measured on main `c774154` (bot source = `74a6b19`, KF merged) against awubot; the cat arms on
main `1557b1b` (TN merged).
Source keys: `C:` `src/challenger/`; `D:` data and tools in `~/battlecode26-data/scratch/kd/` (§6).

## 0. Summary

1. **A second king before the only one dies is mechanically feasible in COMBAT and CAT losses, not in STARVE and
   C+s.** Of main's 174 king-kill losses (1,140 games), 20 rounds before the death the bank is ≥ 50 and 7 of our
   babies can reach a free 3×3 beside the king in 35 of 62 COMBAT and 17 of 25 CAT losses, against 2 of 87 STARVE and
   C+s (§2.1). The babies are scattered: the gather, not a cluster, is what takes the time.
2. **Main's danger start fires too late.** Below 100 HP it fires a median 4 rounds before a COMBAT death (12 for CAT);
   HP < 200, a loss rate, or a cat signal give 20–57 rounds, at a cost of 40–55 false alarms in 921 won games (§2.2).
3. **Even with perfect foresight it buys nothing on `goal`.** Oracles that know main's death round and start 30 rounds
   ahead score −0.004 [−0.021, +0.015] at a site beside the king and −0.004 [−0.022, +0.014] at a far mine (570 cells
   each, §3). In COMBAT losses the new king forms (26–36 of 62) and dies 50–150 rounds later to the same army: 7 of 62
   won, the background rate. Only CAT moves: at a far site 13 of 25 CAT losses are won (12 of the 15 that formed). That
   is about +0.9 pp of `goal`, inside the noise a bytecode change brings.
4. **Every arm read H0.** Near site with main's trigger (N, NF), earlier triggers (T2, T2F), and the cat-targeted arms
   (CAT, CAT2) on the cat pools and `goal` (§3). tminusten is the one clean case (8 of 8 cells against 0.75).
5. **What binds**: a COMBAT death is a lost army, a STARVE death an empty bank, and a second king fixes neither; the
   CAT room is real but small (§4).

## 1. Method

- **Rules** (rules.md §1, §6, §8, checked in `E:world/RobotControllerImpl.java`): a team whose last king dies loses at
  once; `becomeRatKing` needs global ≥ 50, fewer than 5 kings and not (round > 1200 with ≥ 2 kings), so a lone king can
  form a second at any round; the 3×3 must be on the map, free of walls, dirt, kings and cats, with ≥ 7 allied babies
  in it (the centre included). The 8 other occupants die (enemies too), their raw cheese goes to our bank; the new king
  has the summed HP of the allies, capped at 600, and pays 2 cheese a turn (−10 HP when the bank is below 2).
- **Games.** Main against awubot on `goal`, seed base 8201, 570 cells (1,140 games), run `1119-135757-ab-1627`:
  921–219 (0.808). Every game's replay (`tools/games replays … --cells all`).
- **Reader.** `D:KDStats.java` per game: our kings (born, died, HP lost to starvation, bites, scratches, the rest; KF's
  attribution), our formations (with the lone king's HP and distance), every round with one king of ours (HP, bank,
  babies, our babies within d² 8/20/50 of the king, enemy babies within d² 8/20, nearest enemy king and cat, HP lost
  that turn by source), and for a game lost by king kill the 60 rounds before our last king's death:
  - for every free 3×3 (on the map, no wall, dirt, king or cat), the gather time **T7**: the 7th smallest BFS
    distance (8 neighbours, through tiles free of walls and dirt) from our babies to its centre;
  - the best such site anywhere (S), within d² 50 of our king (K), and within Chebyshev 2 of a mine (M), each "safe":
    no enemy baby within d² 8 and no cat within d² 18.
  Positions only from robots that took a turn that round (XA's ghost rule).
- **Feasible at lag L**: at round death − L, global ≥ 50 and T7 + 2 ≤ L (the 7th baby arrives and a round to convert).
  It ignores combat: babies that fight do not walk, and the gather is a lower bound.
- **Loss classes**: W0.3's rules on our kings' HP lost over the game (w1-classes §1): STARVE (starvation ≥ 400 HP or
  ≥ 40%), CAT (scratches ≥ 300 or ≥ 40%), C+s (starvation ≥ 200 or ≥ 25%), COMBAT (the rest).
- **Oracles** (XA's method, xa-formations §1): probe copies of a candidate whose lone king knows the round it died in
  main's game on that cell. The table is keyed by the map size, our team and our first spawned rat's id (spawned ids
  come from an RNG seeded by the map seed, `E:world/IDGenerator.java`; initial bodies keep the map's ids); the key
  matched main's in all 46 lost games checked. A probe adds bytecode, so its games diverge from main's: it fires on that
  knowledge, not on the exact round (§3).

## 2. Step 0

### 2.1 The losses

219 losses: 45 on points, 174 by king kill (172 with a lone king for the last 30 rounds). By class and richness, with
what an emergency formation beside the king (site K) would find 20 and 30 rounds before the death:

| class | mines | losses | had a 2nd king | death round (median) | d−20: bank ≥ 50 / babies ≥ 7 / feasible | d−30: bank ≥ 50 / babies ≥ 7 / feasible | d−10: feasible |
|---|---|---|---|---|---|---|---|
| COMBAT | ≥ 8 | 23 | 3 | 473 | 15 / 17 / 10 | 20 / 16 / 14 | 5 |
| COMBAT | < 8 | 39 | 0 | 240 | 30 / 24 / 25 | 31 / 28 / 26 | 8 |
| CAT | ≥ 8 | 17 | 2 | 304 | 16 / 16 / 12 | 17 / 16 / 15 | 9 |
| CAT | < 8 | 8 | 1 | 237 | 7 / 6 / 5 | 7 / 5 / 5 | 4 |
| C+s | ≥ 8 | 7 | 4 | 1028 | 0 / 0 / 0 | 1 / 1 / 1 | 0 |
| C+s | < 8 | 9 | 2 | 498 | 2 / 1 / 0 | 2 / 2 / 1 | 1 |
| STARVE | ≥ 8 | 41 | 11 | 1027 | 5 / 1 / 1 | 6 / 3 / 2 | 1 |
| STARVE | < 8 | 30 | 3 | 546 | 1 / 1 / 1 | 3 / 1 / 1 | 0 |
| **all** | | **174** | 26 | 509 | 76 / 66 / 54 | 87 / 72 / 65 | 28 |

- **STARVE and C+s are out of reach**: the bank is at 0–1 (median) from d−30 on, with 3–4 babies.
- **COMBAT and CAT are within reach on paper**: 35 of 62 and 17 of 25 at d−20, 40 and 20 at d−30. CAT losses are rich
  (bank median ~1,000, 11–12 babies at d−20): the cat kills a king its team could have replaced.
- **The babies are scattered, not clustered.** At d−20 in COMBAT losses the king has a median 0 / 1 / 3 of our babies
  within d² 8 / 20 / 50 and 1 enemy baby within d² 20; 7 babies within d² 20 of any free 3×3 almost never exist
  (1–3 of 51 in an older sample). What makes a formation feasible is the gather: the 7th baby reaches a free 3×3
  within d² 50 of the king in a median 12–13 rounds (S anywhere 10, at a mine 19–22).
- **A site beside the king is as good as any**: feasible at d−30 in 40 of 62 COMBAT losses at K, 40 at S, 35 at M.

### 2.2 How early the death is predictable

Lead (rounds before the death) at a trigger's first firing with one king, and how often it fires in our won games
(921, where any firing is a false alarm that costs a formation):

| trigger | COMBAT: lead (median) / ≥ 10 rounds early with bank ≥ 50 | CAT | C+s | STARVE | fires in won games |
|---|---|---|---|---|---|
| HP < 100 (main's danger start) | 4 / 17 of 62 | 12 / 18 of 25 | 6 / 1 | 9 / 1 | 20 of 921 |
| HP < 150 | 8 / 23 | 20 / 21 | 10 / 0 | 14 / 3 | 33 |
| HP < 200 | 19 / 32 | 27 / 22 | 15 / 1 | 19 / 2 | 48 |
| HP < 300 | 54 / 47 | 42 / 21 | 32 / 2 | 34 / 4 | 89 |
| time to death < 20 (HP / loss rate over 10 rounds) | 20 / 36 | 17 / 21 | 16 / 2 | 17 / 4 | 43 |
| HP < 200 or time to death < 20 | 30 / 41 | 27 / 22 | 16 / 3 | 19 / 2 | 55 |

- COMBAT deaths come in two shapes: 19 of 62 are bursts (≥ 300 HP 20 rounds before the death: throws and many biters)
  and 19 are long tails (below 150 HP for the last 30 rounds); CAT deaths are steady scratching, none a burst.
- Main's danger start fires a median 4 rounds before a COMBAT death. A lone king fell below 100 HP in 193 of the 1,140
  games (20 won); main completed a formation while its lone king was below 100 HP 9 times, and won 7 of those games:
  every one of those sites was far from the old king (d² 45–1,552).

## 3. Arms

The mechanism (`C:robot/king/KingFormationCoordinatorStrategy.java`, `C:robot/rat/FormationStrategy.java`,
`C:robot/rat/BabyRat.java`, `C:robot/king/KingSpawningStrategy.java`, `C:comms/GlobalArray.java`), common to every arm:
- with one king and the danger trigger, the coordinator skips the 12-rat abort (4 babies, counted in fours), aborts any
  normal formation it runs, and starts a **danger formation** at the danger site once global ≥ 50;
- slot 9 bit 3 marks it; rats within d² 625 of the site run it **before combat**, ignore the cat pause and keep their
  cheese (the formation banks it);
- the king spawns toward the site, whatever the cap, while global − cost ≥ 100;
- the attempt restarts after 30 rounds, or when the site is known blocked or overlaps the king; a danger formation whose
  coordinator died is cleared.

Gain tests against main, awubot, unless noted. Old main is `c774154` (bot = `74a6b19`); new main `1557b1b` (TN merged:
`EXPLORE_P` 0.6).

| arm | danger trigger | site | pool, seed base, δ | result | run |
|---|---|---|---|---|---|
| N (old main) | HP < 100 (main's) | a free 3×3 at Chebyshev 3 (seen now) | L-combat ∪ L-cat, 8301, 0.10 | +0.008 [−0.022, +0.039], H0 (196 cells: 19 better, 16 worse) | `1119-141413-ab-db2e` |
| | | | `goal`, 8302, 0.05 | +0.008 [−0.020, +0.036], H0 (183 cells: 15 / 12) | `1119-141414-ab-620d` |
| | | | `goal`, 8201 (step-0 cells), stopped | −0.022 [−0.047, +0.000] (268 cells: 12 / 22) | `1119-142217-ab-a386` |
| T2 (old main) | HP < 200, or a 10-round loss rate that kills within 20 rounds | as N | L-combat ∪ L-cat, 8301, stopped | −0.016 [−0.057, +0.024] (123 cells: 10 / 15) | `1119-142448-ab-7af5` |
| NF (old main) | HP < 100 | rings 3–5 from the king's map memory | L-combat ∪ L-cat, 8301, 0.10 | +0.000 [−0.036, +0.036], H0 (172 cells: 17 / 17) | `1119-143312-ab-b142` |
| T2F (old main) | as T2 | as NF | L-combat ∪ L-cat, 8301, stopped | −0.029 [−0.065, +0.009] (172 cells: 14 / 25) | `1119-143244-ab-6de1` |
| | | | `goal`, 8302, 0.05 | −0.012 [−0.047, +0.023], H0 (123 cells: 8 / 11) | `1119-143251-ab-6c2b` |

**Oracles** (old main, `goal`, the 570 step-0 cells): the lone king knows the round it died in main's game and starts
the danger 30 rounds before it, or below 300 HP, whichever comes first (below 100 HP in every other game). Paired
against main's own games on those cells.

| oracle | site | result | run |
|---|---|---|---|
| near (NF's mechanism) | rings 3–5 beside the king | −0.004 [−0.021, +0.015] (570 cells: 36 better, 39 worse) | `1119-143155-ab-0082` |
| far | the known mine nearest the king at d² ≥ 100, else NF's | −0.004 [−0.022, +0.014] (570 cells: 40 better, 42 worse) | `1119-143401-ab-1788` |
| near, before the site fix | Chebyshev 3, seen now | −0.014 [−0.036, +0.008] (257 cells, stopped) | `1119-142213-ab-1a7d` |

Main's games by its result and loss class (`D:oran.py`): games the oracle won, those where it formed a king with one
king alive (in main's won games: with that king below 300 HP), and the oracle's wins among those. Near oracle (1,120
of its games) / far oracle (1,140):

| main's class | games | oracle won | formed | won among formed |
|---|---|---|---|---|
| COMBAT | 61 / 62 | 7 / 7 | 36 / 26 | 3 / 3 |
| CAT | 24 / 25 | 9 / **13** | 16 / 15 | 8 / **12** |
| C+s | 16 / 16 | 3 / 3 | 8 / 6 | 1 / 1 |
| STARVE | 70 / 71 | 3 / 4 | 18 / 14 | 2 / 2 |
| POINTS | 44 / 45 | 14 / 14 | 35 / 36 | 14 / 14 |
| main won | 905 / 921 | 863 / 875 | 15 / 16 | 13 / 13 |

- **COMBAT**: with the death known 30 rounds ahead, a king forms in 36 of 61 and the game is won in 3. The new king
  dies 50–150 rounds after the old one, to the same army (bites), at either site: COMBAT losses are lost armies, and
  a king's HP restored from 7 rats does not change who holds the field. 7 of 61 is the background: a probe's games
  diverge from main's, and lost games flip back at about that rate (STARVE, where no formation is possible: 3 of 70;
  points losses, close by nature: 14 of 44).
- **CAT**: the far site wins 13 of 25 (12 of the 15 that formed), the near site 9 of 24. The team is rich and the cat,
  not an army, kills the king; a king formed out of the cat's reach plays on. That is the whole ceiling: about +10
  games in 1,140 (+0.9 pp), with perfect foresight.
- **Main's won games**: 46 of 921 lost in the far oracle, 3 of them after a formation. The other 43, and the wins
  regained in COMBAT, C+s, STARVE and points losses (28), are the divergence any bytecode change brings (W0.1); they
  net about −1.3 pp here and set the noise any KD arm is read against.

**The cat arms** (new main `1557b1b`/`c327f09`, branch `t3/kd-arm-cat` and `t3/kd-arm-cat2`): the oracles' only room
is CAT, at a far site, so the trigger targets cats. A cat kills a king slowly (no CAT death in step 0 was a burst), and
"below 400 HP with ≥ 60 HP lost in 20 rounds to scratches" fires in 24 of 25 CAT losses a median 57 rounds before the
death (23 of them ≥ 10 rounds early with bank ≥ 50), in 5 of 62 COMBAT and 7 of 71 STARVE losses, and in 40 of 921
won games. The king counts HP lost while a cat is in its sight.

| arm | danger trigger | site | pool, seed base, δ | result | run |
|---|---|---|---|---|---|
| CAT | HP < 100, or below 400 HP with 60 HP lost in 20 rounds while a cat is in sight | the known mine nearest the king at d² ≥ 100, else NF's rings | `L-cat` ∪ `L1-cat` ∪ `cat`, 8311, 0.10 | −0.028 [−0.090, +0.037], H0 (89 cells: 7 better, 14 worse) | `1119-144355-ab-a7ce` |
| | | | `goal`, 8312, 0.05 | −0.009 [−0.037, +0.018], H0 (222 cells: 16 / 18) | `1119-144355-ab-8fdb` |
| CAT2 | as CAT | a cat danger forms only at a far mine | `L-cat` ∪ `L1-cat` ∪ `cat`, 8313, 0.10 | +0.022 [−0.058, +0.113], H0 (116 cells: 16 better, 14 worse) | `1119-144801-ab-746b` |

CAT's danger formations against the ref's game on the same cell (`D:dpair.py`, the cat pools): 6 lost games won
(tminusten 4, sunandmoon 2), 5 won games lost (RUN twice, from formations at r942 and r1237; sunandmoon once, at a
site beside the king; directconfrontation; canyoudig, at a site beside the king), 23 won either way: net +1 game. The
other 145 games changed by divergence alone, 10 to losses and 4 to wins. tminusten, where the seat-A king walks the
bottom edge into the cat (w1-classes §3.6), went 8 of 8 cells won against the ref's 0.75.

## 4. What binds

- **COMBAT losses are lost armies.** 20 rounds before a COMBAT death the bank is ≥ 50 and 7 babies can reach a free
  3×3 in 35 of 62 losses, and with the death known 30 rounds ahead the oracles form a king in 26–36 of them. The new
  king then dies 50–150 rounds later, to bites, at either site: the enemy army that killed the first king holds the
  field, and a king rebuilt from 7 of our rats is one more target. Wins regained there (7 of 62) equal the
  background. An earlier trigger (T2) only pulls defenders off kings that would have lived.
- **STARVE and C+s are out of reach**: no bank. A second king also starves (KB).
- **CAT is real and small.** A rich team loses its only king to a cat; a king formed away from the cat saves the game
  (far oracle: 12 of the 15 CAT losses where it formed). But CAT losses are 25 of 1,140 games, so the ceiling, with
  perfect foresight, is about +0.9 pp, inside the ±1.5 pp that divergence moves a 570-cell run. A real trigger fires
  late in some and in won games too (RUN's late formations), and the arm reads net 0 on the cat pools.
- **The existing danger start was already right in kind.** It completed 9 times in 1,140 games and won 7: always at a
  far site. What it lacks is starts (it fires a median 4 rounds before a COMBAT death, after the 12-rat abort), and
  that room is COMBAT's, which a second king does not save.

## 5. Next

- Nothing in KD for the merge: no arm passed, and the measured ceiling (+0.9 pp, CAT only) is below what `goal`
  resolves at δ 0.05.
- tminusten alone is a clean case (8 of 8 cells against 0.75): the seat-A king's walk along the edge into the cat is
  KS/K2's ground (a king-site or cat-lane fix), cheaper than a formation.
- COMBAT deaths (the largest reachable class) are decided by the army before the king is in danger: CX/CC+CK found the
  king-assault levers closed; what remains is the income share that sizes the armies (w1-classes §3.2, MC, TN).

## 6. Data and tools (`~/battlecode26-data/scratch/kd/`)

- `KDStats.java` (`KDStats OUT.jsonl OURPKG replay...`, compiled into `cls/` against the 1.2.6 jar); `MapFree.java`
  (free 3×3s of a map, wall indexing check).
- `step0.py KD.jsonl ROWS.json` (rows a lost game), `table.py` / `compact.py ROWS.json` (§2.1), `trig.py ROWS.json
  ALL.jsonl` (§2.2), `dform.py ALL.jsonl` (danger formations in an arm's replays, paired with the ref's game).
- `s0.jsonl` (main's 219 losses), `s0w.jsonl` (its 921 wins), `s0.rows.json`.
- `wt/`: worktree of `t3/kd-probes`: `src/kd_oracle`, `src/kd_orfar` (oracles, §3); `oracle_cases.txt` (the table).
