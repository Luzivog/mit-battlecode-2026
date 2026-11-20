# SQ: trips in pairs

Package SQ (wave 6): keep every trip but make trips in pairs, so that each encounter on the way has support, without
exploring less and without leaving mining for a fight (docs/theory/ex-exchange.md §7: awubot's support edge is
density, and buying density by exploring less or by reinforcing failed through the economy).

Source keys: `C:` `src/challenger/`, `D:` `~/battlecode26-data/scratch/sq/` (§5). Rounds are engine rounds; "a game"
is a per-game mean over a run's games. Measurements read main's run `1119-195054-ab-f4a7` (`goal`, seed base 18101,
1,000 cells, 2,000 games, every replay; main at 99ef794, 0.819).

---

## 0. Summary

1. **Pairs are better off, alone rats worse.** In r1–300 a third of our fight onsets (33.7 a game) start with no ally
   within 3 and lose 0.12 net grabs each; onsets of rats that travelled together for 5 rounds gain 0.14. Within strata
   (zone, HP, enemies, cheese), pairing every alone onset is worth at most +5.6 net grabs a game, 4.1 of it on rats'
   first trips (74% of r1–300 onsets come before a rat's first transfer) and 4.1 on the enemy side (§1).
2. **Income is limited by coverage, not by trips.** Rats find a mine's 9×9 empty on 43% of their turns there while
   cheese on our side waits 41 rounds for a pickup; a delivery is mostly one 20-cheese pile and a pickup takes the
   whole tile, so a pair does not split piles (§2).
3. **Prediction: about zero, at most +1 pp** (a grab is worth ~0.19 pp before r300), against an economy cost from
   halving the distinct destinations (§3).
4. **The arms did what they said and lost through the economy.** The king makes every second newborn follow the rat
   built just before it (P1: until its first transfer; PL: for life; P3: for life until r300). Pairs stay together
   (within 3 at age 20: 63% against main's 22%), alone onsets fall 32.9 → 29.9 a game and net grabs rise +0.6 a game,
   as predicted; but our cheese delivered falls 19 by r100 and 73 by r300 while awubot's rises 34, and the screens read
   **P1 −0.044 [−0.072, −0.017], PL −0.038 [−0.066, −0.012], P3 −0.030 [−0.056, −0.004]** (§4).
5. **Recommendation: merge nothing.** Support bought by pairing costs coverage, the third way (after D1's
   reinforcement and awubot's exploration oracle) that the density gap is shown to be the price of our economy.

---

## 1. Pairs today

### 1.1 How often our rats travel together

Quiet rat-turns (no enemy baby rat within Chebyshev 4), r1–300, per game; "ally" = an own baby rat within Chebyshev 3,
"pair" = the same own rat within 3 in each of the 5 rounds before (`D:sqan.py`):

| zone | ours: turns, ally, pair | travel turns (moved, outside a mine's 9×9): ally, pair | awubot's: ally, pair (travel pair) |
|---|---|---|---|
| within 6 of our king | 824, 0.83, 0.42 | 440, 0.82, 0.42 | 0.86, 0.45 (0.44) |
| our side | 1,799, 0.66, 0.44 | 1,056, 0.65, 0.44 | 0.70, 0.48 (0.48) |
| the enemy's side | 296, 0.47, 0.31 | 169, 0.44, 0.28 | 0.54, 0.38 (0.36) |

- Pairs form on their own: 44% of our travel on our side has a partner for 5 rounds, 28% on the enemy side. awubot's
  rats are paired a little more in every zone (its density, ex-exchange.md §7.2).

### 1.2 How encounters end, alone and in pairs

Fight episodes (ExStats's definition: a rat's run of turns with an enemy rat within Chebyshev 4), our rats, per onset:

| r1–300 | onsets a game | grabbed | grabs made | net | dead within 12 rounds of the end | cheese lost (held at death) |
|---|---|---|---|---|---|---|
| alone (no ally within 3) | 33.7 | 0.447 | 0.328 | −0.118 | 0.524 | 4.5 |
| travelled in a pair (pair at onset) | 24.7 | 0.273 | 0.408 | +0.135 | 0.376 | 2.4 |
| an ally within 3, not a pair | 41.1 | 0.392 | 0.442 | +0.050 | 0.558 | 2.0 |

- After r300 the same: alone 53.7 onsets a game, net −0.101; pairs 56.8, net +0.074.
- **Within strata** (round band × zone × HP > 50 × enemies within 3 × cheese carried; pairs that did not join an
  ally's fight; `D:sqpair.py`, 95% interval by bootstrap over maps): if every alone onset had its stratum's pair rates,
  r1–300 would give **+5.6 net grabs a game [+3.3, +6.8]** (grabbed −4.4, made +1.2, deaths −1.9, cheese lost −22);
  after r300 +6.4 [+4.4, +9.7]. This is an observational ceiling: the strata cannot remove every way a pair's
  encounter differs from a lone rat's.
- **Where**: 54% of our r1–300 onsets on the enemy side start alone (36% on our side, 10% near our king); the enemy
  side holds +4.1 of the +5.6.
- **When**: 74% of our r1–300 onsets come on a rat's first trip (before its first transfer), and first-trip alone onsets
  hold +4.1 of the +5.6 (enemy side +3.0). A rat's first trip is the one a king's spawn payload can pair
  (`D:sqtrip.py`).

## 2. What limits income

| r1–300, our view | mines a game | spawned a game | picked by us / by awubot by r300 | ground latency (stock / spawn rate) |
|---|---|---|---|---|
| our side | 2.97 | 1,832 | 0.71 / 0.10 | 41 rounds |
| centre | 2.62 | 1,599 | 0.40 / 0.36 | 59 rounds |
| the enemy's side | 2.97 | 1,835 | 0.14 / 0.67 | 40 rounds |

- **Rats wait at empty mines and cheese waits for rats**: 43% of our quiet turns inside a mine's 9×9 find it empty
  (about 430 rat-turns a game, 12% of our r1–300 rat-turns), while cheese on our side lies 41 rounds before pickup and
  19% of it is still on the ground at r300. Income is limited by where rats go (coverage), not by the number of trips.
- **Loads**: a delivery is 37.9 cheese on average; 52% of them are 20–29, one spawn's pile. A pickup takes the whole
  tile, so a pair does not split a pile: the first rat takes it. Moving rats carry 10.7 raw on average (r1–300), a move
  cost ×1.09: the slowdown a pair could save is small.

## 3. Prediction

- **Exchange**: a grab is worth about 0.19 pp in r1–300 (W3's oracle "awubot never grabs before r300" +7.5 pp for 38.7
  grabs a game) and 0.07 pp after (+2.5 pp for 36.5). The ceiling of §1.2, every alone onset paired, is +1.1 pp before
  r300 and +0.4 pp after; first trips alone +0.9 pp. A real arm pairs only part of them.
- **Economy**: two rats on one destination halve the distinct destinations. Coverage is what limits income (§2), and
  exploration's coverage is load-bearing (TN: EXPLORE_P 0.2 → 0.6 +2.7 pp, flat from 0.4).
- **Net: about zero, at most +1 pp**, the first-trip arm the most favourable (most of the exchange ceiling, the least
  coverage lost). Not negative, so the arms were built, knowing that even the ceiling is below what a 1,000-cell
  screen resolves (±1.7 pp) and that only a free economy would let it show.

## 4. Arms

`C:Tune.java` block `// SQ`, off on main's code: a king gives every second newborn (built within 2 rounds of the one
before) a spawn payload `4096 | direction to the leader's spawn tile << 9 | leader id & 511`, every other newborn `1`
(`C:robot/king/KingSpawningStrategy.pairPayload`). A possible leader holds still for its first 2 rounds; a follower
turns toward its leader's spawn tile (then scans quarter by quarter) until it identifies it, then, where MinerStrategy
would draw a trip, steps toward the leader while farther than 1 tile and skips its end-of-turn glance
(`C:robot/rat/MinerStrategy.follow`). A pair ends at `SQ_PAIR_TRIPS` transfers, at `SQ_PAIR_ROUND`, or after
`SQ_PAIR_LOST` (8) rounds without sight of the leader. Off, the switch compiles to main's instructions (javap,
`D:javap/cmp.py`).

- Built in two passes. Without the hold and the direction hint the leader walks out of view before its follower's
  first turn (13 of 32 pairs never identified in a 2-game probe); the end-of-turn glance hides an adjacent leader.
- Screens: `goal`, 1,000 cells (2,000 games), seed base 27001, `--remote`, every replay, paired with main de6e5b8
  (0.833 on these cells).

| arm | change | run | diff [95% CI] | cells better / worse | branch @ head |
|---|---|---|---|---|---|
| P1 | pairs until the follower's first transfer (`SQ_PAIR_TRIPS` 1) | `1119-215907-ab-0368` | **−0.044 [−0.072, −0.017]** | 161 / 228 | `t3/sq-p1` @ `57ab00fa` |
| PL | pairs for life (`SQ_PAIR_TRIPS` 99) | `1119-215928-ab-8cb8` | **−0.038 [−0.066, −0.012]** | 164 / 223 | `t3/sq-pl` @ `394cbc0a` |
| P3 | pairs for life, until r300 (`SQ_PAIR_ROUND` 300) | `1119-215947-ab-837a` | **−0.030 [−0.056, −0.004]** | 163 / 211 | `t3/sq-p3` @ `df05bfa5` |

No screen read above zero, so none went to a confirmation; P2 (pairs on far trips only) was not built: its ceiling
(the enemy side's +4.1 net grabs, ~0.8 pp) is below the economy cost every pairing arm paid.

**What P1 did** (all 2,000 games against main's 2,000 on the same cells, r1–300; `D:sqx.py`, `D:sqan.py`,
`D:sqser.py`):

| per game | P1 | main |
|---|---|---|
| king-made pairs within 3 at age 20 / 30 | 0.63 / 0.49 | 0.22 / 0.18 |
| allies within 3 per rat-turn | 2.16 | 1.94 |
| alone fight onsets | 29.9 | 32.9 |
| grabbed / grabs made / net | 36.5 / 39.6 / +3.17 | 37.3 / 39.9 / +2.59 |
| our rat deaths / awubot's | 34.9 / 45.1 | 35.3 / 45.3 |
| moving rat-turns | 2,750 | 2,906 |
| transfers | 33.6 | 34.6 |
| our share of our side's / the centre's spawned cheese | 0.68 / 0.36 | 0.71 / 0.40 |
| awubot's share of the centre's | 0.37 | 0.35 |

- Paired by cell and seat (`D:sqser.py`, results' 100-round series): our cheese delivered −19 at r100, −50 at r200,
  −73 at r300, −149 at r500; awubot's +12, +27, +34, +44. PL and P3 read the same within a few cheese.
- **The exchange gain is the predicted size** (+0.6 net grabs a game, ~0.1 pp); the loss is coverage: half the rats
  go where their leader goes, so our side and the centre are visited less, our pickups there fall 3–4 points and
  awubot takes part of what we leave. The leader's hold and the follower's waiting beside a mining leader cut our
  moving turns 5%.


## 5. Data and tools

- `D:SqStats.java OUT.jsonl replay...`: ExStats (docs/theory/ex-exchange.md §6) plus quiet-turn pairing by zone and
  band (`q`), fight episodes for all rounds with pair-at-onset, zone, cheese at the end and fate within 12 rounds,
  trip index (transfers before the onset) and age (`epis` fields 22–29), mine income per mine (spawned, picked by each
  team, ground stock integral; `mines`), mine-box waiting, transfers and loads (`inc`, `xferh`), and each king's
  consecutive spawn pairs with their distance by age (`pairs`). Compile against
  `~/battlecode26-data/engine/jars/battlecode26-java-1.2.6.jar` into `D:cls/`; `D:sqrun.sh RUN [NPROC]` runs it over a
  run's replays into `D:RUN/all.jsonl`.
- `D:sqan.py` (pairing, episodes, income), `D:sqpair.py` (stratified pair gain, bootstrap over maps), `D:sqtrip.py`
  (onsets by trip and zone), `D:sqpairs.py` (pair distance by age).
- `D:SqInd.java` and `D:SqIndDump.java`: indicator strings starting with a prefix in a replay (tally / dump; `SPAWN`
  dumps spawn actions). `./gradlew run` keeps indicators in its replay (tools/play drops them).
- `D:sqx.py` (exchange totals r1–300), `D:sqser.py RUN...` (paired 100-round series of an ab run, no replays needed).
- Arm replays read: `D:1119-215907-ab-0368/{o,r}.jsonl` (P1 and its main games). Runs kept: the three screens.
- `D:runs.txt`: every run.
