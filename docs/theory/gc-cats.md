# GC: cats in general, then starvation

Clean program, track GC (seeds 33000-33999). Main `cf20d501` (clean, book empty) against awubot. Data and tools:
`~/battlecode26-data/scratch/gc/` (`D:` below). Paired diffs are arm − main on the same cells (seed base 33002,
950 `goal` cells, 1,900 games), 95% intervals clustered by map (`D:pm.py`).

## 0. Summary

1. **Cats are not the gap.** Clean main on fresh seeds (33001, 570 cells, `1121-044240-ab-0268`) wins 0.831; a CAT
   loss (our kings scratched to death, KD's rule) is 18 of 1,140 games (1.6%), a third of them tminusten.
2. **Lanes learned in game work as built and gain nothing on average.** Every robot remembers the cat 2×2s it sees,
   babies squeak their new corners to their king, kings mirror them by symmetry (the cat loops are symmetric on all
   95 goal maps): on bed the king knows 114 of the 140 loop tiles by r200. With lane memory and the lane flee:
   +0.005 ± 0.019. The oracle (every map's true loops given to the kings) reads +0.001 ± 0.028: the lever's ceiling is
   about zero, so better learning cannot help. Hunt from r500 and the wider cat defence read below zero (§2).
3. **Starvation** (the integrator's new focus, §3): 83 of clean main's 193 losses (STARVE 71 + C+s 12). It is an
   income race: at onset we have 4 babies to awubot's 11 and earn 240 cheese a 100 rounds to its 651, though awubot
   loses more rats than we do. 41 of the 71 STARVE kings never come within 3 tiles of a mine all game; 28 of them are
   on map-seats where no mine is reachable for a 3×3 king (56 of 190 map-seats; starvation loses 8.3% of the games
   there, 5.3% elsewhere).
4. **A trap gate on a low bank loses** (−3.0 pp at 200, −11.8 pp at 600): traps paid from cheese the king needs are
   still worth more than the cheese.

## 1. Mechanism built (switches, all off in main)

- `GC_LANES` (`CatLanes.see`, `Squeaker` header 3): every robot marks visible cats each turn; a baby queues the
  corners new to it (32-entry ring) and, within r² 16 of its king, squeaks two (12 bits each) before its map diff; a
  king marks them, and once the symmetry is known marks every 2×2's mirror (CellTracker ordinals: 1 rotational, 2 x
  flipped, 3 y flipped). Uses: K2 D' (`CM_LANE_MEMORY`) and the lane flee (`CM_LANE_FLEE`).
- `GC_LANE_MINES`: 0 keeps lane memory's step rule but not its mine skip.
- `GC_TRAP_BANK`: no rat trap while global cheese is below it (`TryTrap.verifyCanRun`).

## 2. Cat runs (seed base 33002, ref `1121-034642-ab-5465`)

| arm | run | games | arm − main |
|---|---|---|---|
| `GC_LANES` + memory + flee (smf) | `1121-034643-ab-a95d` | 1,900 | +0.005 ± 0.019 |
| as smf, no mine skip (smfs) | `1121-035702-ab-3c81` | 1,900 | +0.004 ± 0.020 |
| oracle: true loops (`t3/gc-oracle` d096511e) + memory + flee | `1121-054902-ab-4107` (stopped) | 1,533 | +0.001 ± 0.028 |
| king-only memory (`CM_LANE_MEMORY` 1) | `1121-030801-ab-e367` (stopped) | 555 | −0.007 ± 0.017 |
| `CM_CAT_HUNT_START` 500 | `1121-055252-ab-b14e` (stopped) | 1,710 | −0.005 ± 0.008 |
| `CAT_DEF_DSQ` 100 | `1121-055344-ab-66f7` (stopped) | 1,285 | −0.010 ± 0.025 |

- smf on the book's cat maps (bed, dirtfulcat, closeup, sunandmoon, ...) reads about +0.06 a map-seat, the book's
  direction; the other maps give it back. By feature (W4's table) nothing separates outside noise (small maps +0.030
  ± 0.044, VERTICAL +0.037 ± 0.034, ROTATIONAL −0.017 ± 0.028).
- Points: in clean main's r2000 games we deal 6,390 cat damage a game to awubot's 2,969; cooperation endings (cat
  damage weighs 50) are 45 of 1,140 games, on four maps. Neither is a cat-hunting gap.
- The first oracle probe (95 maps' loops as book tile sets) lost every game at round 1: `Book.apply` grew past the
  JVM's method size (`MethodTooLargeException`); the loops moved to a separate class (`LaneOracle`) keyed by a book int.

## 3. Starvation (clean main, seed base 33001, loss replays)

Readers: KD's `KDStats` + CM's classes (`D:kd.sh`), E1's `E1Stats`, CT's `CTStats`, and `D:st/StarveTrace.java`
(per team, every 25 rounds: bank, kings, babies, cheese delivered, spawn cheese, traps, digs, starvation turns, king
position, bites, cat traps). Analyses `D:starve1.py`, `D:starve2.py`, `D:reach.py` (3×3 reach from each start, CL's
`traps.py`).

| | us | awubot |
|---|---|---|
| starvation onset (median round) | 450 | |
| babies alive at onset (median) | 4 | 11 |
| cheese delivered by onset (median) | 1,503 | 2,095 |
| delivered in the 100 rounds before onset | 240 | 651 |
| rat traps by onset (mean) | 81 | 58 |
| bites by onset (mean) | 126 | 146 |
| rat deaths in the game: trap / bite / throw / cat | 12.5 / 19.4 / 25.5 / 7.9 | 18.5 / 23.2 / 22.8 / 9.7 |

- The king starves a median 74 rounds; an enemy baby is within d² 20 of it in 11% of those rounds: it is alone.
- 41 of 71 STARVE kings never come within Chebyshev 3 of a mine. A probe (king state printed every 25 rounds; six
  local Java-engine games on the maps' own seeds, scratch copy `D:wt2`, `D:probeA.txt`, `D:probeB.txt`) shows why: warhammer A's king holds mine target (18,10) at d² 164 from r50 to r557 and shuffles
  between (28,2) and (27,1); shell B's targets (32,22) at d² 386 and is stuck at (27,3) (stuck counter past 50) while
  spawning takes the bank from 1,862 at r25 to 420 at r100; mincemeat A's wanders its corner. A second king formed
  at a mine (warhammer's at (8,8)) mines.
- 30 STARVE kings sit at a mine and still starve: QueenOfHearts B's king mines (23,36) on a bank of 100-200 all game;
  the mine is shared with awubot's rats (the income race of AT3 and W2).
- Where no mine is reachable for a 3×3 king (56 of 190 map-seats), we win 0.851 and starvation loses 28 of 336
  games (8.3%); elsewhere 0.822 and 43 of 804 (5.3%).
- Spending: the bank's unexplained drain (Δbank − delivered + spawns + upkeep) is −1,476 a game for us and −1,002
  for awubot, the difference matching our 23 extra traps (≈ 460). E1's reserve could not reach it (it gated on global
  after paying); the bank gate on traps does, and loses (§0.4): the traps win more than their cheese buys.

## 4. The pocket trigger (closed)

At the integrator's request (GK owns the second king): `GC_POCKET_K2` on branch `t3/gc-pocket-k2` (9c1d48f9, from
GK's `t3/gk-runtime-king-sites` fd4a6680). `robot/king/Pocket.java` floods 3×3 king centres over the cells the king
has seen (seen walls block, dirt and unseen cells open) at the end of its turn, every 10 rounds, with 8,000 bytecodes
left; a flood that closes without touching unseen ground is the king's whole component, and with no known mine a
centre covers, no mine is in its reach. That ORs into GK's `king2` condition beside `gkCat`, and GK's `gkK2Site`
picks the site; the pocket king's own claim (its unreachable target) must be open to the new king, or the search
finds nothing (local probe: proven at r3 on warhammer A, no second king until that change; after it, a second king at
(18,10) by r100). Shell B's sight never closes the flood.

Screen against GK's head, `goal`, seed base 33003, 1,000 cells (ref `1121-100351-ab-5aa5`): from r30 (`1121-100524-ab-cd47`)
−0.022 ± 0.022; on the 56 pocket map-seats −0.076 ± 0.054, elsewhere +0.001 ± 0.001. From r150
(`1121-100601-ab-9782`, 963 games) pocket −0.053 ± 0.055. The pocket map-seats already win 0.846: a pocket king is fed
by its rats, and a second king there drains the bank for both (constrictinglattice −0.82 / −0.64, moonlitriverpark
−0.73 / −0.55); it gains only where the mines are far (Hike B, warhammer B, cheddarchokehold A about +0.2; nearest mine
≥ 18 steps: 14 map-seats, +0.03). The ~30 kings that starve at a mine they lose to awubot's rats are the rat exchange
(RX's track).
