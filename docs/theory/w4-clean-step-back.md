# W4: step back on clean main (the book emptied)

Where clean main (501bde1d, the per-map book emptied) still loses to awubot, what the book carried, and the levers
left, priced by oracles on the same cells. It plans the clean program's wave 2 (the goal: 90% on 3,000 fresh `goal`
cells without hardcoding). The map-feature table for GK, GE, GC and GM is `docs/theory/w4-map-features.md`.

`D:` = `~/battlecode26-data/scratch/w4/` (data and tools, §7). Seat = our seat. "vs null" subtracts the mean of three
salted-stream nulls on the same games (§3). Intervals are 95% bootstrap over maps.

**Runs.** Clean main `1121-095305-ab-b4b1` (challenger@cf20d501, bot source = 501bde1d, `goal`, seed base 30001, 3,000
cells, every loss replayed): **0.834** (5,004–996). Every oracle plays the same 3,000 cells (6,000 games) and is paired
game by game with it (`D:paired.py`). The book: `1121-095049-ab-6e6d` (integrator's: clean main against book-final on
the same 3,000 cells, seed base 26001): clean **0.842**, book-final **0.916**, clean − book **−0.074 [−0.097, −0.053]**.
Teammade: `1121-034517-ab-d1fc`, clean main 0.818 [0.765, 0.869] on 580 cells.

---

## 0. Summary

1. **The book was worth 7.4 pp**, in six families: the second king 31% of it (+2.3 pp), economy 22% (+1.6), the first
   king's site 15% (+1.1), cats 12% (+0.9), routes 11% (+0.8), knowledge 10% (+0.7). Its 112 map-seats without entries
   read −0.001 (§1).
2. **No observable map feature predicts where the book helped** (leave-one-map-out r ≤ +0.18 for every feature): the
   gains sit on particular maps. The best feature classes: a cat at a king by r200 (7 maps, book +0.21, clean 0.71),
   ≥ 15 mines (12 maps, +0.15), ≥ 5 cats, HORIZONTAL maps, seat B on VERTICAL maps (clean 0.77) (§1.2, the features doc).
3. **Where clean main loses** (996 losses, §2): starved 363, rats 308, points at r2000 143, cats 99, cooperation's end
   83 (the last cat killed in cooperation: 81 of them on TheHeist, peaceinourtime A, groveisbeingdesecrated and
   t_minus_five_minutes). 345 end before r300. Seat B 0.819 against A 0.849, the gap in starvation and rat losses on
   VERTICAL and HORIZONTAL maps. Games are decided early as in W3: our income share alone predicts the winner with AUC
   0.69 at r200, 0.80 at r300. A re-roll of the random stream (salted null) wins 69% of main's losses and loses 13%
   of its wins: most single losses are luck within a cell.
4. **Oracle prices on clean main** (§3), vs the nulls: awubot without throws **+12.3 pp**, without ratnaps or traps
   +12.7, without ratnaps +9.5, without traps +7.7, without centre-mine cheese +7.5, its king frozen in contact +7.4,
   without grabs of our wounded rats +6.6, without the squeak chase +3.6, without king hits +2.6, without formations
   +2.2. Ours: every mine known from round 1 +2.1 (the symmetry alone +0.3); the first king's site chosen per map-seat
   in hindsight about **+4.4 pp** out of sample (with the second king +4.9); every general site or second-king rule ≤ +0.9 or harmful.
5. **What 0.90 needs**: main 0.834 (0.839 null) → 0.90 is about +6 to +7 pp: 40% fewer losses (§5). The exchange
   (12.7) is the only reservoir that size; the per-map-seat site choice (4.4, with the second king 4.9) and the book's second-king and economy
   entries (+3.9 between them) are the next, but no observable rule reaches them yet.
6. **Wave 2, ranked** (§6): RX (the exchange: throws, ratnaps, wounded grabs), GK (sites: an in-game site choice that
   learns, not a rule from map features), GM (mines from symmetry, +2.1 ceiling), the king's safety from rats (+2.6,
   doubled since W3), GE's cooperation hold (83 losses on 4 maps) and GC's early-cat maps (clean 0.71).

---

## 1. What the book carried

### 1.1 By family

`D:famshare.py` over `D:mapjoin.json` (6e6d): a map-seat's paired gain (book-final − clean main, same cells, 32 games a
side) split equally among the families of its entries (`D:bookfam.json`, from `git show book-final:src/challenger/book.jsonl`).

| family (book switches) | map-seats | gain on its map-seats | share of the gap |
|---|---|---|---|
| second king (`CM_KING2_ROUND`, `KING2_SITE`, `MINES_KING2`) | 26 | +0.234 | 31% (+2.3 pp) |
| economy (`SPAWN_CAP`, `SPAWN_CAP_SMALL`, `SPAWN_RICH`, `PM_SIEGE`, `EXPLORE_P`, `DIRT_ROUND`, `IDLE_GOHOME`, `PM_COOP_HOLD`) | 24 | +0.219 | 22% (+1.6 pp) |
| first king's site (`KING1_SITE`, `CM_KING1_SITE`, `PM_SKIP_FAILED`) | 16 | +0.184 | 15% (+1.1 pp) |
| cats (`CAT_LANE`, `CM_LANE_MEMORY`, `CM_LANE_FLEE`, `CM_CAT_HUNT_START`, `CAT_DEF_DSQ`) | 17 | +0.125 | 12% (+0.9 pp) |
| routes (`KING_HAND`, `KING_PATH`, `SG_LOOK_RIGHT`) | 9 | +0.251 | 11% (+0.8 pp) |
| knowledge (`PM_SYM`, `CM_MINE_BOOK`, `AT_MAP`) | 10 | +0.245 | 10% (+0.7 pp) |
| map-seats without entries | 112 | −0.001 | 0 |

The kings' families (first site and second king) together are 46% of the gap (+3.4 pp); per map-seat every family
gained +0.12 to +0.25 where it was booked.

### 1.2 Where it helped, by observable features

`D:featpred.py` (every bin in `D:featpred.txt`): per goal map-seat the book's gain and clean main's rate by feature bin,
each feature scored by a leave-one-map-out prediction. No feature predicts the gain (r ≤ +0.18: cat clock +0.18, map
classes +0.08, mine density +0.07; the rest ≤ +0.06). The bins with the largest mean gain (each ≥ 5 goal maps):

| bin | maps | book gain | clean main |
|---|---|---|---|
| a cat reaches a king's 3×3 by r200 (directconfrontation, externalsustenance, groveisbeingdesecrated, RUN, sunandmoon, t_minus_five_minutes, tminusten) | 7 | **+0.212** | **0.707** |
| ≥ 15 mines | 12 | +0.153 | 0.783 |
| ≥ 5 cats | 7 | +0.145 | 0.812 |
| HORIZONTAL | 20 | +0.117 | 0.827 |
| nearest mine 13–20 king steps | 26 | +0.114 | 0.795 |
| mine density ≥ 8 per 1,000 tiles | 18 | +0.109 | 0.821 |
| seat B, VERTICAL | 27 | +0.094 | 0.773 |
| the second-acting seat | 95 | +0.086 | 0.831 |
| (every other map, for the cat clock) | 86 | +0.062 | 0.853 |

Each bin's mean is carried by a few of its maps (≥ 15 mines: TheHeist +0.52 both seats, tminusten, grove,
externalsustenance, longwayaround; sieveofriches, safelycontained, superioristhmus, seepastyourgreed ≤ +0.03). A rule
keyed on a bin captures at most the bin's mean, and only where the book's switch is the right one for the whole bin.

---

## 2. Where clean main loses

`D:losstab.py` over the 996 loss replays (`D:kl_main.jsonl`, W3's `KingLedger`: our kings' HP lost by source). Cause:
POINTS (r2000 without a king kill), COOP (the last cat died in cooperation: points decide early, rules.md §1), STARVE
(starvation ≥ 400 HP or ≥ 40% of our kings' HP lost), CAT (scratches ≥ 300 or ≥ 40%), RATS (the rest: bites,
throws; W1's rules).

| cause | before r300 | r300–599 | r600–999 | r1000–1999 | r2000 | all | per 100 games |
|---|---|---|---|---|---|---|---|
| STARVE | 69 | 116 | 88 | 90 | – | 363 | 6.0 |
| RATS | 172 | 71 | 32 | 33 | – | 308 | 5.1 |
| POINTS | 1 | – | – | – | 142 | 143 | 2.4 |
| CAT | 59 | 21 | 9 | 10 | – | 99 | 1.6 |
| COOP | 44 | 27 | 8 | 4 | – | 83 | 1.4 |
| all | 345 | 235 | 137 | 137 | 142 | 996 | 16.6 |

- Our kings' HP lost a lost game: RATS losses 302 to bites, 118 to enemy throws, 88 to starvation; CAT 493 to
  scratches; STARVE 473 to starvation.
- **When it is decided**: our share of the cheese transferred (results series) separates wins from losses with AUC
  0.54 at r100, 0.69 at r200, 0.80 at r300, 0.83 at r400, 0.85 at r500 (median share 0.545 in wins against 0.464 in
  losses at r300). W3's finding holds on clean main: the economy race and the exchange of r1–300.
- **By seat and symmetry** (losses per 100 games): seat B VERTICAL RATS 8.8, STARVE 8.5 against seat A 5.0, 5.4; seat
  B HORIZONTAL STARVE 8.6 against 4.6; ROTATIONAL even. Seat B 0.819, A 0.849.
- **Concentration**: the true spread between map-seats is SD 0.127 (binomial noise removed); 7 map-seats under 0.5
  hold 13% of the losses, 32 under 0.7 hold 42%, half the losses sit on the worst 42 map-seats. The worst:
  houseofcards B 0.35 (RATS 13, STARVE 7), TheHeist B 0.38 and A 0.41 (all COOP), peaceinourtime A 0.44 (COOP 12,
  CAT 6), asabovesobelow B 0.45 (RATS 14), cheddarchokehold B 0.47 (STARVE 15), popthecork B 0.48, squaresaremodernart
  B 0.50, longwayaround A 0.52 (POINTS 10), groveisbeingdesecrated B 0.53 (COOP 12) (`D:losstab_main.txt`).
- **By cause, the map-seats**: COOP on 8 map-seats of 4 maps; POINTS on 44 (longwayaround, rift, RUN, uneruesansfin,
  where_are_you, cheesebottles); CAT on 30 (tminusten 19, closeup 12, bed 11, externalsustenance 11, peaceinourtime,
  directconfrontation, toomuchcheese); STARVE on 108 and RATS on 89 (spread).
- **Most single losses are luck within the cell.** Each salted null wins about 690 of main's 996 losses and loses about
  660 of its 5,004 wins on the same games (§3): the outcome of a cell moves with the random stream, so a loss is a
  probability, and a change is judged only by its rate over many cells.

---

## 3. Oracle prices

Every oracle plays the same 3,000 cells (seed base 30001) and is paired game by game with clean main. awubot copies
(from W2, W3, EX, XA, OX; `src/awubot` unchanged, so they still apply) price what that ability of awubot's costs us;
challenger copies (probe branch `t3/w4-probes`) price "ours with X". Three salted-stream nulls (`w4_chsalt`,
`w4_chsalt2`: our random stream salted; `w2_awusalt`: awubot's) read +0.008, +0.005, +0.003: null mean 0.839, clean
main's draw on 30001 is 0.5 pp low, and "vs null" is the column to quote. (`D:oracletab.py` prints the table.)

| oracle | run | score | vs clean main [95% CI] | vs null | changed (+ / −) |
|---|---|---|---|---|---|
| **awubot: neither ratnaps nor traps** (`w3_awunonaptrap`) | `1121-034750-ab-6e04` | 0.966 | **+0.132 [+0.112, +0.154]** | +0.127 | 947 (+870 / −77) |
| **awubot: never throws our rats** (grabs kept, `w3_awunothrow`) | `1121-103930-ab-5260` | 0.962 | **+0.128 [+0.108, +0.149]** | +0.123 | 896 (+832 / −64) |
| awubot: never ratnaps (`w3_awunonap`) | `1121-103748-ab-31b6` | 0.934 | +0.100 [+0.081, +0.121] | +0.095 | 933 (+766 / −167) |
| awubot: no rat traps (`w3_awunotrap`) | `1121-103748-ab-73d8` | 0.916 | +0.082 [+0.062, +0.103] | +0.077 | 1010 (+752 / −258) |
| awubot: never takes centre-mine cheese (`w2_awunoc`, cheat table) | `1121-095306-ab-b0ec` | 0.914 | +0.080 [+0.056, +0.105] | +0.075 | 1228 (+855 / −373) |
| awubot: its king loses its turn when it senses ours (`oxkfz`; null `oxknull` +0.001) | `1121-104409-ab-5e99` | 0.913 | +0.079 [+0.064, +0.094] | +0.074 | 707 (+590 / −117) |
| awubot: never grabs our rats at HP ≤ 50 (`ex_awunowound`) | `1121-034750-ab-a7c6` + `1121-095306-ab-30d6` | 0.905 | +0.071 [+0.057, +0.087] | +0.066 | 975 (+702 / −273) |
| awubot: never chases squeaked enemies (`ex_awunochase`) | `1121-103042-ab-01d5` | 0.875 | +0.041 [+0.027, +0.055] | +0.036 | 1111 (+679 / −432) |
| awubot: its rats never bite or throw into our kings (`w3_awunok`) | `1121-104358-ab-312b` | 0.865 | +0.031 [+0.022, +0.041] | +0.026 | 487 (+337 / −150) |
| awubot: never forms a king (`xa_awunf`) | `1121-103013-ab-5e16` | 0.862 | +0.028 [+0.013, +0.044] | +0.022 | 325 (+245 / −80) |
| ours: the symmetry and every mine known from round 1 (book data, all maps) | `1121-095307-ab-6941` | 0.866 | +0.032 [+0.017, +0.047] | +0.027 | 1336 (+764 / −572) |
| ours: every mine known from round 1 (`CM_MINE_BOOK`) | `1121-102918-ab-f426` | 0.861 | +0.026 [+0.013, +0.041] | +0.021 | 1325 (+742 / −583) |
| ours: the symmetry known from round 1 (`PM_SYM`) | `1121-102833-ab-403f` | 0.842 | +0.008 [−0.005, +0.022] | +0.003 | 1171 (+610 / −561) |
| ours: first king at the start corner, every map (`CM_KING1_SITE` 1) | `1121-101113-ab-03ee` | 0.694 | −0.141 [−0.189, −0.093] | −0.146 | 1891 |
| ours: first king beside our nearest mine (2) | `1121-101213-ab-beda` | 0.848 | +0.014 [−0.009, +0.036] | +0.009 | 1275 |
| ours: beside our second-nearest mine (3) | `1121-101922-ab-f7a9` | 0.801 | −0.033 [−0.078, +0.006] | −0.038 | 1533 |
| ours: beside the contested mine on our side (4) | `1121-102034-ab-0518` | 0.796 | −0.038 [−0.074, −0.004] | −0.043 | 1532 |
| ours: nearest the middle of our mines (5) | `1121-102206-ab-9ea7` | 0.813 | −0.021 [−0.054, +0.010] | −0.026 | 1452 |
| ours: a second king from r30 (`CM_KING2_ROUND` 30, main's mine choice) | `1121-104506-ab-5a3e` | 0.774 | −0.060 [−0.089, −0.033] | −0.065 | 843 (+242 / −601) |
| ours: a second king from r80 | `1121-104945-ab-2c8f` | 0.780 | −0.054 [−0.084, −0.029] | −0.060 | 794 (+234 / −560) |
| ours: a second king from r30 at our second-nearest / contested / middle mine (`KING2_SITE`) | `1121-105620-ab-9b75` | 0.520 | −0.314 [−0.379, −0.249] | −0.319 | 2536 (+327 / −2209) |
| ours: `MINES_KING2` 3 (main 5) | `1121-105159-ab-4f95` | 0.830 | −0.004 [−0.012, +0.002] | −0.010 | 98 (+36 / −62) |
| ours: `MINES_KING2` 8 | `1121-105402-ab-0d52` | 0.830 | −0.004 [−0.019, +0.009] | −0.009 | 211 (+93 / −118) |

### 3.1 Which losses each oracle converts

`D:convert.py`: of main's losses by cause, how many the oracle wins on the same game, and how many of main's wins it
loses. The nulls' row is what a re-roll does.

| run | RATS (308) | CAT (99) | STARVE (363) | POINTS (143) | COOP (83) | main's wins lost |
|---|---|---|---|---|---|---|
| nulls (mean of 3) | 220 | 64 | 260 | 109 | 43 | 665 |
| neither ratnaps nor traps | 302 | 70 | 346 | 137 | 15 | 77 |
| never throws | 289 | 65 | 342 | 136 | 0 | 64 |
| never ratnaps | 287 | 65 | 294 | 115 | 5 | 167 |
| no rat traps | 244 | 56 | 309 | 130 | 13 | 258 |
| no centre cheese | 252 | 82 | 325 | 131 | 65 | 373 |
| no wounded grabs | 262 | 56 | 272 | 112 | 0 | 273 |
| no squeak chase | 240 | 59 | 253 | 110 | 17 | 432 |
| never hurts our king | 193 | 27 | 68 | 47 | 2 | 150 |
| never forms | 36 | 14 | 63 | 116 | 16 | 80 |
| ours: mines and symmetry known | 228 | 67 | 290 | 123 | 56 | 572 |

- The exchange oracles (throws, ratnaps, traps) convert nearly every RATS, STARVE and POINTS loss and keep main's
  wins: the exchange decides those games. They convert the COOP losses less than a re-roll (0–15 against 43): a game
  without grabs stays in cooperation until the last cat dies, and points decide there.
- **awubot's formations decide the points games**: without them it loses 116 of main's 143 points losses and changes
  little else (325 results; XA: most of their value is after r1700).
- The king-hit oracle converts 193 of the 308 RATS losses and little else: on clean main our king's safety from rats
  is worth +2.6 pp (W3 on the old main: +1.3 against its null).
- Knowing the mines converts about 30 STARVE losses more than a re-roll and loses 90 fewer wins.

### 3.2 The first king's site chosen per map-seat (hindsight)

`D:hindsight.py`, over clean main and the five site arms on every map-seat (32 games each): the best arm per map-seat
reads **+0.083** in-sample; chosen on one half of the cells (by seed order) and scored on the other, **+0.048**. The
same procedure over three salted nulls reads +0.059 in-sample and +0.004 cross-fit (noise alone), so a perfect
per-map-seat choice among these five types is worth about **+4.4 pp** (GK's ceiling; CM4 found +0.054 in-sample on
49 maps of the old main).

What a rule from features gets (`D:sitetab.py`, `D:sitetab.json`: each arm's effect per map-seat): the best type per
feature bin is worth at most +0.04 to +0.08 on its bin, about +0.5 pp of `goal`:

| bin | map-seats | best type | its effect | nearest mine (main's own habit) |
|---|---|---|---|---|
| a cat at a king by r200 | 14 | second-nearest mine | +0.081 | +0.076 |
| nearest mine 13–20 steps | 52 | contested | +0.041 | +0.039 |
| ≤ 4 mines | 50 | nearest | +0.041 | – |
| HORIZONTAL | 40 | nearest | +0.040 | – |
| every other bin | | nearest or none | ≤ +0.026 | |

Holding the start corner loses in every bin but two (no own mine +0.020, early cats +0.030), most where the nearest
mine is > 20 steps (−0.372) and on large maps (−0.226).

### 3.3 The second king (hindsight)

`D:hindsight.py` over clean main and the five second-king arms (§3's last five rows): the best per map-seat reads
+0.036 in-sample, **+0.018** cross-fit. Every arm loses or is flat on average (an early second king for every map
−0.060 from r30, −0.054 from r80; sent to a chosen mine −0.314; the `MINES_KING2` gate at 3 or 8 −0.004): the second
king starves the first's income (KB, docs/closed.md), and the book's +0.234 on its 26 map-seats came from a site and
round chosen per map with book data, which these generic arms do not reproduce. **The first site and the second king
chosen together** (the ten arms and main): +0.092 in-sample, **+0.053** cross-fit. Calibration on clean main's run
alone: the best of the three nulls reads +0.059 in-sample and +0.004 cross-fit. So the reachable prices of a perfect
per-map-seat choice are about **+4.4 pp** (the first site), **+1.4 pp** (the second king, these arms) and **+4.9 pp**
(both).

---

## 4. Levers checked and closed here

- **The symmetry from round 1**: +0.003 against the nulls (PM_SYM on every map). Knowing the symmetry adds nothing on
  its own; knowing the mines does (+0.021).
- **One first-king site type for every map**: start −0.146, second-nearest −0.038, contested −0.043, middle −0.026,
  nearest +0.009 (vs null): none. Main's own choice is already the nearest mine's on average (CM4 §3).
- **One second-king rule for every map** (from r30, from r80, at a chosen mine from r30; `MINES_KING2` 3 or 8): −0.065,
  −0.060, −0.319, −0.010, −0.009 against the nulls. An early second king, general or at a site, loses (as KB's round
  arms did): its gains are per map (the book's 26 map-seats).
- **The awubot-copy reads repeat W3/EX/XA/OX on clean main** within their intervals, except two that grew: king hits
  +1.3 → +2.6 pp, and formations fell +6.3 (XA, its own null) → +2.2.

---

## 5. What 0.90 needs

- Clean main reads 0.834 here (null 0.839), 0.842 on 26001, 0.818 on teammade. 0.90 on `goal` is +6 to +7 pp: 40% of
  the losses (996 → 600 of 6,000).
- **The reservoirs, priced on clean main** (vs null, overlapping): the exchange 12.7 (throws alone 12.3; ratnaps 9.5,
  traps 7.7, wounded grabs 6.6, support chase 3.6), the centre 7.5 (out of our sight: CD), awubot's king's contact
  turns 7.4 (no mechanism: OX), the per-map-seat site choice 4.4 (4.9 with the second king; no observable rule), our mines' knowledge 2.1,
  king hits 2.6, formations 2.2 (points games), cooperation's end 1.4 (83 losses on 4 maps), cats 1.6 (99 losses).
- **The two routes**: lifting the worst 40 map-seats to 0.9 is about +6 pp (in sample; less out of it), and the loss
  mass is spread (111 map-seats under 0.9 hold 90% of the losses); so most of the gain must come from changes that act
  on every map. The exchange is that change (its oracles convert every class but COOP). The king's site, chosen per
  map-seat from what the game shows (not from map features: §1.2, §3.2), is the second.

---

## 6. Wave 2, ranked

| rank | package | lever and mechanism | price (vs null) | first arm |
|---|---|---|---|---|
| 1 | **RX** (running) | the exchange: awubot's throws (+12.3), ratnaps (+9.5), traps (+7.7), its grabs of our rats at HP ≤ 50 (+6.6); its own code as the opponent model | ceiling 12.7; a third of it is +4 | the throw lines: awubot's `TryThrow` scores every (step, facing) by the carried rat's flight, 2–5 tiles out: our king in the line +300, one of our rats at the first collision tile +102, a wall or dirt +10 + 4·remaining (A:`robot/rat/combat/moves/TryThrow.java.jinja2`); so it throws our rats into our king, then into our nearest rats, then into walls. Arm 1: our robots within 5 of an awubot carrier step off its 8 lines, the king first (throws alone price the whole exchange: a grab without the throw gains awubot nothing) |
| 2 | **GK** (running) | the first king's site per map-seat (hindsight +4.4; with the second king +4.9) and the book's second-king entries (+2.3) | 4.4 (site), 1.4 (second king), 4.9 both | an in-game site choice, not a feature rule: the king reads its income per mine visited in r1–150 and re-seats beside the mine its miners deliver most from; first on the 7 early-cat maps, where seating at our nearest or second-nearest mine reads +0.08 |
| 3 | **GM** (running) | every mine known from round 1 (+2.1; the symmetry adds 0.3) | ceiling 2.1 | mirror every mine seen through the inferred symmetry into the mine list at once (`PM_SYM_B`'s inference, then MineBook from mirrored sightings), so miners and kings target the far side's mines before exploring |
| 4 | king safety from rats | awubot's bites and throws into our kings: 193 of 308 RATS losses | 2.6 (doubled since W3, which closed king defence at 1.3) | RX's throw-line arm applied to the king's 3×3 first (118 HP a RATS loss come from enemy throws); then re-open W3's closed king defence only with this price |
| 5 | **GE** cooperation hold | the last cat killed in cooperation hands the game to points: 83 losses, 81 on four maps | ≤ 1.4 | `PM_COOP_HOLD` 1 everywhere (a game-state rule: in cooperation with one king, never deal a cat its last HP); judge on `goal` and the 4 maps (TheHeist, peaceinourtime, grove, t_minus_five_minutes) |
| 6 | **GC** early-cat maps | a cat reaches a king's 3×3 by r200: clean 0.707 on 7 maps, the book +0.21 there | ~1.3 if the book's gain is recovered | trigger on a cat seen within reach of our king's start in r1–200 (observable), then the book's cat switches (`CM_LANE_MEMORY`, the second-nearest-mine seat) |
| 7 | points games | awubot's late formations decide 116 of 143 points losses | ≤ 2.2 | our own r1700+ formation timing against awubot's (KF's coordinator), or deny its late formations' ring (XA: out of reach) |
| – | centre | awubot's centre-mine cheese +7.5 | closed (CD: out of our sight) | – |
| – | awubot's king's contact turns | +7.4 (OX: +7.1) | no mechanism (OX) | – |

---

## 7. Data, tools and probes

- `D:` = `~/battlecode26-data/scratch/w4/`:
  - `mapfeat.py OUT.json` (from `tools/`): static features per map and seat (`bcplay.MapInfo` through
    `bcgames.mapclass`); `mapjoin.py GOAL_AB TEAMMADE OUT.json`: per map-seat clean main's rate and the book's paired
    gain; `bookfam.py`: book-final's entries by family per map-seat; `famshare.py`, `featpred.py`, `featmd.py`.
  - `losstab.py RUN KL.jsonl [OUT.json]`: the loss table (cause, phase, seat, income-share AUC by round, map-seats);
    `kl_main.jsonl`: W3's `KingLedger` over the 996 loss replays of `b4b1` (kept: `tools/games keep`).
  - `paired.py BASE RUN... --nulls=...` (W3's, runs joinable by `+`), `oracletab.py` (§3's table), `convert.py`
    (§3.1), `hindsight.py BASE ARM...` (best arm per map-seat, in-sample and cross-fit), `sitetab.py` (§3.2).
  - `oraclebook.py VARIANT OUT.jsonl`: oracle books on every goal map (sym, mines, know, site1–5, k2site);
    `mkcopy.sh`, `cycle.sh` (launch queued runs, at most 7 open); `allruns.txt`, `runs.txt`, `done.txt`, `oracle-revs.txt`.
- Probe branch `t3/w4-probes` (worktree `D:wt/`, never merged): `w4_chsalt`, `w4_chsalt2` (salted nulls of clean
  main); oracle books and Tune arms as commits of `src/challenger` (`D:oracle-revs.txt`). The awubot copies come from
  `origin/t3/w3-probes`, `w2-probes`, `ex-probes`, `xa-probes`, `ox-probe` (`D:probe-revs.txt`).
- An oracle book with every map's walls and dirt (`AT_MAP` on all 95 maps) does not compile: `Book.apply` exceeds the
  JVM's 64 KB method limit; the mines and symmetry fit.
