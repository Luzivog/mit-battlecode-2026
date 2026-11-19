# W3: where games against awubot are decided, and which levers are left

A step back from the loss-class framing of W1, W2 and wave 3: when the winner becomes predictable and from what, how
our won and lost games of one cell differ, and the untried levers priced by oracles.

Source keys: `E:` engine source at tag `engine.1.2.6` (`~/battlecode26-data/engine/ref/battlecode26/engine/src/main/battlecode/`),
`A:` `src/awubot/`, `C:` `src/challenger/`, `D:` data and tools in `~/battlecode26-data/scratch/w3/` (§7). Rounds are
engine rounds. "p" is a win probability for our side.

**Runs.** W2's main run `1119-150554-ab-6083` (`goal`, seed base 9201, 570 cells, every replay) and a fresh main run
`1119-174327-ab-a52f` (`challenger@3e93f6d`, bot source = main, `goal`, seed base 14001, 570 cells, every replay):
**948–192, 0.832 [0.800, 0.862]**. Every oracle below plays the same 570 cells of seed base 14001 and is paired game
by game with that run; two salted-stream nulls on the same cells (`w2_chsalt`, `w2_awusalt`) read +0.005 and −0.010
(null mean 0.829), so main's draw on 14001 is not a high one (W2 §4.6) and oracles read against main directly.

---

## 0. Summary

1. **Games are decided before r300.** The map alone predicts the winner with AUC 0.66–0.73 before round 1 (map and seat:
   true win-rate SD 0.15 between map-seat pairs). From r150 the game state takes over: AUC 0.82 at r150, 0.86 at
   r300, 0.91 at r500. The variable that carries it is the bank, the log ratio of the two teams' global cheese (alone
   0.68 at r150, 0.80 at r300, 0.90 at r500). A loss is "called" (p < 0.5 for good) at a median r300; the largest
   single fall of p comes at a median r350. Our own team's state alone barely beats the map prior: our bot cannot
   see that it is losing (§1).
2. **Three kinds of loss**: ~75 of 188 are sudden early king kills (over before r600, invisible 100 rounds ahead), ~35
   are called early and end r300–600, ~80 are long economic losses (a quarter of them on points at r2000) called
   400–800 rounds before the end (§1.2).
3. **Split cells part from the opening.** By r100 the state ranks our won and lost game of a cell correctly in 68% of
   split cells, 79% by r200. The first term that differs is the centre-mine pickups, already in r0–49 and by ~60
   cheese in r50–99, with deaths, spawns and own-side pickups equal: W2's centre race, a hundred rounds earlier (§2).
   Sending half our opening rats to the centre mines (cheat table) gains nothing (−0.021 against the null).
4. **The rat exchange is the largest reservoir found.** Oracle copies of awubot, paired with main on 1,140 games:
   - awubot without ratnaps **+0.091 [+0.063, +0.120]** (+0.092 on a second seed base, against its null); without
     throws (grabs kept) **+0.120 [+0.089, +0.151]**; without its blind-side grabs +0.070, without its HP-rule grabs
     +0.078;
   - awubot without rat traps **+0.069 [+0.037, +0.103]** (+0.077 on the second base);
   - **without both: +0.135 [+0.105, +0.166]**, main 0.967;
   - three quarters of the grab value (+0.075) and 60% of the trap value (+0.041) sit in r1–300.
   Our own weapons are worth more to us: main without ratnaps 0.409, without traps 0.440 (§3.6, §4).
5. **King safety is worth little.** An awubot whose rats never bite or throw into our kings: +0.015 [−0.002, +0.032]
   (+0.013 against the null on 14002). The king's death ends games the exchange and the economy already decided.
   This closes king defence (dirt rings, ring tiles, carriers' lines, emergency kings) as a route (§3.7).
6. **The other levers asked about are small or out of reach**: (a) a raid has windows (awubot's king alone in 80% of
   losses) but no trigger, since our state cannot tell a loss; (b) a perfect finisher ≤ 2.6 pp (~1 pp of wire races),
   and OX's +7.1 pp king freeze has no mechanism we reach; (c) cooperation ends at a median r25; (d) cats visit both
   kings alike (10–11% of samples) and no steering adds the HP needed; (e) choosing constants per map gains
   −0.018 to +0.013 out of sample; the map-seat tail (6 pairs under 0.5, 12% of losses) is a set of separate
   pathologies worth ~1.9 pp (§3).
7. **What 0.90 needs** (§6): +6–9 pp. The exchange (13.5 pp, most of it before r300) is the only reachable reservoir
   that size; the centre (+8, W2) is out of our rats' sight (CD). A third to a half of the exchange, plus the map-seat
   pathologies and RS's small gains, reaches 0.90.
8. **Recommendation**: make the next wave's main package the r1–300 exchange (EX): cut awubot's blind-side and HP-rule
   grabs, its throws' damage and our trap hits, each mechanism judged by the precision route because each is worth
   1–3 pp; give FL these prices as its targets. Second, the worst map-seat pairs one by one (MS). Stop king defence,
   raids, cat steering, cooperation play, per-map constants and centre presence.

---

## 1. When, and by what, a game is decided

### 1.1 A predictor by round

Per round R, over the games still running, a logistic model (L2, folds by map, so every game is predicted by a model
fit on other maps) on the state at R: our share of cheese transferred and of the last 100 rounds' income, the log
ratio of global cheese, rats alive, kings, kings' HP, rat deaths, cat damage, rats near each king; with and without a
map prior (the map's win rate on the run's other cells). `D:predict.py`, `D:decide.py`, W1Trace and W2Econ series.

| R | games running | map prior alone: AUC | state alone | state + map | best single variable (AUC) | losses already over |
|---|---|---|---|---|---|---|
| 0 | 1140 | 0.73 (0.75 with the mirror's seat) | – | – | – | 0 |
| 100 | 1129 | 0.73 | 0.60 | 0.76 | global cheese ratio 0.63 | 3 |
| 150 | 1090 | 0.73 | 0.73 | 0.82 | global cheese ratio 0.68 | 13 |
| 200 | 968 | 0.72 | 0.75 | 0.82 | global cheese ratio 0.71 | 27 |
| 300 | 770 | 0.72 | 0.82 | 0.86 | global cheese ratio 0.80 | 60 |
| 500 | 551 | 0.72 | 0.88 | 0.91 | global cheese ratio 0.90 | 92 |
| 800 | 330 | 0.77 | 0.88 | 0.94 | global cheese ratio 0.91 | 133 |

(W2's run. On the 14001 run the leave-cell-out map prior is weaker, AUC 0.66, and state + map reads 0.72 at r100,
0.77 at r150, 0.84 at r300, 0.88 at r800; the same shape, the bank (with held raw cheese, 0.72 at r150 and 0.82 at
r300) again the best single variable up to r300.)

- **The map decides a quarter of the outcome before round 1.** The map prior alone has AUC 0.73 (0.66 on 14001); adding the mirror's
  seat (who wins the awubot mirror on that cell and seat) 0.75. Pooling 10,640 main-equivalent games (main and its
  salted copies on four seed bases, `D:mapvar.py`), the true spread of win rates is SD 0.12 between maps and 0.15
  between map-seat pairs (binomial noise removed): the seat of a map matters as much again as the map.
- **The state takes over from r150 and carries it by r300.** The variable that carries it is the bank, the log ratio
  of the two teams' global cheese: alone it reaches AUC 0.68 at r150, 0.80 at r300, 0.90 at r500. Income share,
  rats alive and our king's HP add to it; kings, cat damage and the rats near each king add little before r800.
  The bank is income minus spending (respawns after deaths, spawns, traps, formations): a summary of the economy
  race and the rat exchange together, not a separate cause.
- **Our bot cannot see that it is losing.** A model on what our own team knows (our global cheese, income, rats,
  kings, king HP, deaths) has AUC 0.54–0.72 by round, and with the map prior 0.74–0.79: never much above the map
  alone. The information is in awubot's state (`D:observable.py`). A rule that switches plans when behind has nothing
  to fire on.

### 1.2 Three kinds of loss

Each loss's series of p (state + map), "called" at the first round from which p < 0.5 at every later sample (W2's run;
14001 the same within a few games):

| loss ends | losses | never called | called < 100 rounds before the end | 100–300 before | > 300 before | median rounds from the last p ≥ 0.7 to the end |
|---|---|---|---|---|---|---|
| before r300 | 60 | 20 | 33 | 7 | 0 | 104 |
| r300–600 | 50 | 5 | 16 | 26 | 3 | 242 |
| r600–1000 | 29 | 3 | 5 | 6 | 15 | 423 |
| r1000–1999 | 27 | 2 | 1 | 3 | 21 | 800 |
| r2000 (points) | 22 | 6 | 5 | 1 | 10 | 575 |

- **Sudden early king kills** (74 of 188; 80 of 192 on 14001): over before r600 and invisible in the state 100 rounds earlier.
  Fights decide them.
- **Early-called losses** (36; 31): ending r300–600, called at r100–400.
- **Long economic losses** (78; 81): ending after r600 (starvation, late fights, points), called a median 400–800
  rounds before the end, mostly by r200–500. The largest single fall of p in a loss comes at a median r350.

So a change must act **before r300**: on the fights of the first 300 rounds for the first kind, on the economy race
of r0–300 for the others. Later rounds hold the consequences.

---

## 2. The close games

148 of the 570 cells split (W2's run; 148 again on 14001). Our won and lost game of a cell are the same map and seed
from mirrored seats.

### 2.1 When the two games part

| R | both alive | P(the won game has the higher p) | won − lost: global cheese (log ratio, ours vs awubot's) | rat deaths, ours − awubot's | rats alive (log ratio) |
|---|---|---|---|---|---|
| 100 | 143 | 0.68 | +0.10 | −1.7 | +0.01 |
| 150 | 131 | 0.74 | +0.32 | −2.8 | +0.12 |
| 200 | 117 | 0.79 | +0.47 | −3.9 | +0.34 |
| 300 | 82 | 0.82 | +0.64 | −3.3 | +0.40 |
| 500 | 48 | 0.92 | +1.38 | −1.2 | +0.92 |

In 127 of 148 split cells the lost game's p falls ≥ 0.2 below the won game's for good before the lost game ends, at
a median r250 (quartiles r150, r450). By r100 the state already ranks the two games correctly in 68% of cells.

### 2.2 The earliest difference: who picks the centre-mine cheese in r0–100

Per 50 rounds, ours / awubot's, the won game and the lost game of each split cell (`D:splitwin.py`, W2Econ):

| rounds | centre-mine pickups: won game | lost game | rat-turns in centre boxes: won | lost | own-side pickups: won | lost | rat deaths: won | lost |
|---|---|---|---|---|---|---|---|---|
| 0–49 | 47 / 45 | 38 / 47 | 11 / 13 | 10 / 13 | 120 / 121 | 120 / 127 | 3.8 / 5.1 | 4.0 / 4.7 |
| 50–99 | 138 / 120 | 110 / 156 | 34 / 35 | 27 / 43 | 191 / 174 | 183 / 189 | 7.8 / 9.9 | 8.2 / 9.5 |
| 100–149 | 149 / 124 | 126 / 159 | 35 / 34 | 30 / 51 | 174 / 156 | 157 / 182 | 8.1 / 10.6 | 8.1 / 9.3 |
| 150–199 | 188 / 140 | 139 / 190 | 50 / 43 | 37 / 62 | 185 / 154 | 171 / 186 | 6.2 / 8.6 | 6.3 / 7.8 |
| 200–249 | 263 / 130 | 136 / 262 | 80 / 50 | 40 / 86 | 211 / 179 | 199 / 219 | 5.1 / 6.5 | 5.4 / 6.0 |
| 250–299 | 349 / 166 | 175 / 290 | 118 / 46 | 54 / 119 | 204 / 185 | 205 / 223 | 5.3 / 5.9 | 5.0 / 5.9 |

(The 14001 run repeats it: r50–99 centre pickups 119 / 101 in the won game, 85 / 124 in the lost one.)

- **The first thing that differs is the centre race, from the opening.** Our deaths and spawns are the same in both
  games through r200 and own-side pickups within 10% through r150; the centre-mine pickups already differ in r0–49
  and swing by about 60 cheese in r50–99, then double every 50 rounds. Both terms move: our centre presence is lower *and* awubot's higher in the
  lost game, with about the same total in the boxes (r50–99: 69 rat-turns in both games).
- This dates W2's centre race (§3.2 there: opens at r150–250) a hundred rounds earlier. In r0–100 the rats at the
  centre are the opening rats, whose first targets are random rays from the king (`C:robot/rat/MinerStrategy.java`
  `pickEarlyExplorationTarget`, rounds ≤ 20) and the first rat's walk to the map centre. W2's centre-presence oracle
  (`w2_chctr`) acted only after r20.
- What would have had to differ in the lost game: our opening rats at the centre mines instead of awubot's, in r0–100.
  The oracle `w3_chopen` (§4) tries it with the centre mines given: −0.024 [−0.054, +0.005] against main, −0.021
  against the null. Our presence does not take the race in the opening either. What else differs in those rounds is
  the exchange: the net rat deaths (ours − awubot's) already favour the won game by 1.7 at r100 and 3.9 at r200
  (§2.1), and the oracles of §4 put three quarters of awubot's grab value in r1–300.

### 2.3 Two maps where the seat is structural

On most maps the seat of a split is W2's "awubot's random luck" (§4.2 there). Two maps are different: almost all their
mines sit on the symmetry line, and the seat that acts first wins (`D:turnorder.py`, per map-seat p from 10,640 pooled
main-equivalent games). Starting kings keep the map's ids; on 81 of 95 maps team A's king has the lower id, and its
spawned rats then act before B's every round (rules.md §3).

| map | mines on the line | our p from the first-acting seat | from the second | the awubot mirror's copy from the first-acting seat |
|---|---|---|---|---|
| EscapeTheNight | 9 of 11 | 0.99 | 0.09 (1 win in 56 games) | 6 of 6 |
| popthecork | 8 of 10 | 0.99 | 0.36 | 6 of 6 |

All our lost games there are STARVE: from the second seat awubot's centre pickups in r150–300 are 1.2–6× ours
(EscapeTheNight, 14001), from the first ours are 3× and more of awubot's. A first-mover edge at contested piles
(pickup is free on the rat's own turn) would explain it, but it is not general: over the 20 maps with ≥ 2/3 of their
mines on the line, the mirror's copy wins 0.50 from the first-acting seat, and we win 0.833 against 0.760. So these
two map-seats are pathologies to read one by one (about 0.7 pp together), not a turn-order lever.

---

## 3. Untried levers, checked against docs/closed.md

Each lever with the measurement that prices or rejects it. Oracle runs are in §4.

### 3.1 (a) A raid while awubot commits to the centre

- **The windows exist.** In 150 of 188 losses (W2's run) awubot's only king had at most one of its own rats within
  Chebyshev 6 at some 10-round sample in r50–1200, first at a median r90, for a median 60 rounds; in wins 827 of 952.
  At r200–400 of our losses awubot's king has 3–4 of its rats within 6, ours sit 22–23 tiles from it on average and
  0.6 of them are within 8 (ghost-safe positions, `D:W3Stats.java`, `D:exposure.py`).
- **Nothing fires it.** Our own state cannot tell a lost game from a won one (§1.1), and the windows are as common in
  wins (87%). A raid would have to run on every game, sending rats 22 tiles from the mines while the economy race of
  r0–300 is being decided (§2.2).
- **Its mirror is priced, and it is small.** Removing every bite and throw of awubot's rats on our kings is worth
  +0.015 (§4): an attack on a king mostly ends a game the economy and the rat exchange have already decided. A raid on
  awubot's king faces the same logic from the other side; its value would come only from losses where we are behind
  and win the race to the king, which no oracle here prices. Not recommended before the exchange (§3.6) is better.

### 3.2 (b) Killing awubot's exposed king

- awubot's only king was at ≤ 200 HP at some sample in 30 of 188 losses (16%), at ≤ 100 in 13; in about 12 of them
  both kings died within ~10 rounds (a race lost at the wire: 12 HP at r270 when ours died at r270, 38 at r150 / r152,
  64 at r240 / r241, 120 at r100 / r105 twice, 140 at r100 / r103). A perfect finisher is worth at most those
  30 games, 2.6 pp, and realistically the dozen wire races, about 1 pp.
- "Alone and exposed" (one awubot king, ≥ 3 of our rats and ≤ 1 of its own within 6): 7 of 188 losses. Our rats are
  rarely there: one of ours is within Chebyshev 5 of awubot's king in 11% of its 10-round samples up to r300 in our
  losses and 5% after (wins: 15% and 12%; 14001, `D:W3Stats.java`).
- **OX's ceiling.** An awubot whose king loses its whole turn whenever it senses one of our robots is worth +7.1 pp
  [+4.7, +9.5] to us (docs/theory/ox-awubot-failures.md, run `1119-175520-ab-1e04`): what awubot's king does in those
  contact turns (spawning, fleeing, biting) is worth that much to it. No mechanism of ours takes those turns away:
  presence near the king provokes the same reactions the freeze removes, and OX found no failure of awubot's that our
  robots can trigger there (≥ 6 of ours in its king's view in 2 of 128 losses). The buildable form would deny the
  king's actions themselves, chiefly its spawns (occupied or buried ring tiles); it needs our rats at its king, which
  §3.1 found missing. Unpriced, ranked below the exchange (§5).

### 3.3 (c) Damage that does not backstab

- Cooperation ends at a median r25 (10–90%: r9–r117; W2's run, `D:coop.py`), in 77% of games by r50 and 85–90% by
  r100; 43 of 1,140 games never leave it. Before r25 no king is within reach. Throws (CW) and formation crush already
  do their damage after the backstab, where they need no exemption. No lever.

### 3.4 (d) Cats against awubot's king

- In our losses a cat is within Chebyshev 5 of awubot's king in 10.4% of king-samples, of ours in 11.2%; cats scratch
  awubot's kings 5.5 times a game and ours 6.7 (`D:coop.py`). The cats already visit both kings alike; they deal
  about 110 HP to awubot's kings in our losses.
- Steering acts only in a cat's 8-turn ATTACK at a waypoint (rules.md §12), targeting is the last rat or king in the
  cat's scan, and feeding buys 2 cat turns for a rat (about 50 cheese at 16 rats). Making cats deal the ~450 HP more
  that awubot's king has left at our median loss needs several ATTACK episodes in the right place: no mechanism
  reaches it. As defence, the whole CAT class is 35 losses (3.1 pp) and KD priced a perfect answer at ~0.9 pp.
  Rejected.

### 3.5 (e) Opening and build order by map class

- **Per-map choice of constants gains nothing out of sample.** Across TN's screens (s1: 32 variants × 285 cells, s2: 17
  × 475, s5: 33 × 380, s7: 9 × 760), choosing per map the best of main and the variants on one half of the seeds and
  scoring it on the other half: +0.013, +0.009, −0.018, +0.002 (`D:interact.py`). The variants' map-specific effects
  replicate weakly between halves (split-half r 0.03–0.11): with ≤ 8 cells a map the choice is noise.
- **Opening presence at the centre gains nothing.** `w3_chopen` sends the even-id opening rats (first turn ≤ 20) to a
  centre mine from the cheat table instead of a random ray: −0.024 [−0.054, +0.005] against main, −0.021 against the
  null mean (§4). With W2's three presence oracles (after r20), presence at the centre is now priced at zero from the
  first round on.
- **The map-seat tail is real but scattered.** Six map-seat pairs win under 0.5 and hold 12% of the losses; 30 pairs
  under 0.7 hold 39% (10,640 pooled games, `D:mapvar.py`). Their causes differ (§2.3; W2 §2.3): centre-line maps from
  the second seat (EscapeTheNight B, popthecork B), cats on a seat's lane (peaceinourtime A, bed B, sunandmoon B,
  tminusten A), small-map fights (houseofcards B, DefaultSmall), points (longwayaround). Lifting the six pairs under
  0.5 to 0.9 is worth about 1.9 pp, one investigation each.

### 3.6 (f) The rat exchange: awubot's grabs and traps

The oracles of §4 price what awubot's weapons do to us. Two of them are large:

- **awubot's ratnapping is worth +0.091 [+0.063, +0.120]** (main against an awubot that never grabs; +0.092 against
  the null on a second seed base). Its throws alone, the grab kept: **+0.120 [+0.089, +0.151]** (holding a rat to the
  auto-release without throwing it is worse for awubot than not grabbing). Without its blind-side grabs (it still
  grabs a rat that sees it but has less HP): +0.070 [+0.045, +0.096]; without its HP-rule grabs (blind ones kept):
  +0.078 [+0.052, +0.104]. The two halves overlap: either removal takes most of the value.
- **awubot's rat traps are worth +0.069 [+0.037, +0.103]** (+0.077 against the null on 14002).
- **Together: +0.135 [+0.105, +0.166].** Main beats an awubot without grabs and traps 0.967.
- **Most of it is in r1–300.** No grabs in r1–300 only: +0.075 [+0.046, +0.104]; after r300 only: +0.025 [+0.010,
  +0.041]. No traps in r1–300 only: +0.041 [+0.009, +0.073] of the +0.069. This is the window of §1.
- **Our own weapons are worth far more to us**: challenger without ratnaps scores 0.409 (−0.423), without rat traps
  0.440 (−0.391). CW and CT made these our main weapons; the exchange is where both bots' strength lies.
- Where they act (main's losses converted, against the salted nulls' 52/80, 31/51, 24/30 on the same games): the
  no-grab oracle wins 68 of 80 sudden early losses and loses only 38 of main's wins (nulls 117–132); the no-trap
  oracle wins 38 of 51 long losses and 29 of 30 points losses (`D:byregime.py`). Grabs decide the early fights,
  traps the long games.
- **The exchange itself** (14001 replays, `D:GrabStats.java`): awubot grabs our rats 82 times a game in our losses and
  74 in our wins, we grab its rats 73 / 80; half of awubot's grabs are blind-side (41 in losses), and 24% follow a
  trap hit on the victim within 3 rounds. Each grab is thrown in about 60% of carries, and half of awubot's victims
  die within 12 rounds: about 42 of our ~84 deaths a game. Our rats fire awubot's traps 67 times a game in losses and
  60 in wins; awubot's fire ours 90 / 100.
- **Where a blind grab comes from**: the grabber's tile at the start of its turn was in the victim's own cone in 19% of
  blind grabs (it stepped round), in another of our robots' sight in 34%, and seen by none of ours in about half.
- Where our rats fire awubot's traps (`D:TrapWhere.java`, losses): 36% before r300, 47% within 4 of a mine, 9%
  within 6 of awubot's king, 14% within 6 of ours; 23% are grabbed within 3 rounds.
- CX (closed) cut awubot's grabs by ~7% with a threat model and gained nothing: against a 9 pp ceiling that is the
  expected +0.6 pp, below what its tests could resolve. The exchange is the largest reservoir measured in any
  package (above the centre's +8), but it is the sum of ~150 encounters a game, so any one mechanism takes a slice:
  it must be measured by the precision route (docs/evaluation.md item 5), not a δ 0.05 test.

### 3.7 King safety

- An awubot that never bites our kings: +0.010 [−0.005, +0.025] (67 results changed); that never throws into them:
  −0.008 [−0.022, +0.005]; neither: +0.015 [−0.002, +0.032], and on 14002 +0.038 [+0.021, +0.056] against main,
  +0.013 against the null. In COMBAT losses our king loses 250 HP to bites and 96
  to throws a game (`D:KingLedger.java`), yet removing both converts 27 of 80 sudden losses against the nulls' ~52.
  **A king that cannot be hurt by rats is worth 1.5–4 pp at most**: the king's death mostly ends games already lost. This
  closes king defence as a route to 0.90: rings of dirt around the king, CC/CK's ring-tile and carrier arms, KD's
  emergency kings.

---

## 4. Oracles

All plain runs of main (`challenger@origin/main`, bot source unchanged since 1557b1b) against a probe on `goal`, 570
cells (1,140 games), `--remote`, paired game by game with main's own run on the same cells (`D:paired.py`, 95% CI by
bootstrap over maps). "vs null" subtracts the mean of the salted-stream nulls on the same cells. Probes on
`t3/w3-probes` (§7); a probe that changes awubot measures what that feature of awubot is worth against us.

| oracle | seed base | run | score | vs main [95% CI] | vs null | results changed (+ / −) | awubot robots killed by its own errors |
|---|---|---|---|---|---|---|---|
| main | 14001 | `1119-174327-ab-a52f` | 0.832 [0.800, 0.862] | – | +0.003 | – | 250 |
| null: challenger, salted stream (`w2_chsalt`) | 14001 | `1119-174327-ab-4c21` | 0.837 | +0.005 [−0.020, +0.032] | – | 240 (123 / 117) | – |
| null: awubot, salted stream (`w2_awusalt`) | 14001 | `1119-174328-ab-6cae` | 0.822 | −0.010 [−0.035, +0.016] | – | 253 (121 / 132) | – |
| awubot never bites a king (`w3_awunokb`) | 14001 | `1119-174328-ab-b14f` | 0.841 | +0.010 [−0.005, +0.025] | +0.012 | 67 (39 / 28) | 245 |
| awubot never throws into a king (`w3_awunokt`) | 14001 | `1119-174333-ab-ef49` | 0.824 | −0.008 [−0.022, +0.005] | −0.006 | 61 (26 / 35) | 267 |
| **awubot never hurts a king with its rats** (both, `w3_awunok`) | 14001 | `1119-174338-ab-eee6` | 0.846 | **+0.015 [−0.002, +0.032]** | +0.017 | 83 (50 / 33) | 271 |
| the same | 14002 | `1119-181330-ab-2328` | 0.845 | +0.038 [+0.021, +0.056] | +0.013 | 103 (73 / 30) | 211 |
| **awubot never ratnaps** (`w3_awunonap`) | 14001 | `1119-174344-ab-2b96` | 0.923 | **+0.091 [+0.063, +0.120]** | +0.093 | 180 (142 / 38) | 5 |
| the same | 14002 | `1119-181329-ab-0277` | 0.924 | +0.117 [+0.087, +0.146] | +0.092 | 203 (168 / 35) | 1 |
| awubot grabs only rats that see it: no blind-side grabs (`w3_awunapseen`) | 14001 | `1119-180253-ab-e521` | 0.902 | +0.070 [+0.045, +0.096] | +0.073 | 178 (129 / 49) | 111 |
| awubot grabs only rats that cannot see it: no HP-rule grabs (`w3_awunapblind`) | 14001 | `1119-180310-ab-7746` | 0.912 | +0.078 [+0.052, +0.104] | +0.081 | 168 (128 / 40) | 28 |
| **awubot never throws an enemy rat** (holds it to the auto-release; `w3_awunothrow`) | 14001 | `1119-180239-ab-698f` | 0.952 | **+0.120 [+0.089, +0.151]** | +0.123 | 178 (157 / 21) | 4 |
| awubot no ratnaps in r1–300 (`w3_awunap300`) | 14001 | `1119-181331-ab-0d98` | 0.907 | +0.075 [+0.046, +0.104] | +0.078 | 188 (137 / 51) | 87 |
| awubot no ratnaps after r300 (`w3_awunapL`) | 14001 | `1119-181341-ab-3fe8` | 0.857 | +0.025 [+0.010, +0.041] | +0.028 | 85 (57 / 28) | 138 |
| **awubot never places a rat trap** (`w3_awunotrap`) | 14001 | `1119-174350-ab-2bfb` | 0.901 | **+0.069 [+0.037, +0.103]** | +0.071 | 195 (137 / 58) | 270 |
| the same | 14002 | `1119-181330-ab-c8a7` | 0.909 | +0.102 [+0.075, +0.132] | +0.077 | 204 (160 / 44) | 277 |
| awubot no rat traps in r1–300 (`w3_awunotrap300`) | 14001 | `1119-181345-ab-db2c` | 0.871 | +0.041 [+0.009, +0.073] | +0.044 | 202 (124 / 78) | 175 |
| **awubot neither ratnaps nor traps** (`w3_awunonaptrap`) | 14001 | `1119-181710-ab-ccea` | **0.967** | **+0.135 [+0.105, +0.166]** | +0.138 | 182 (168 / 14) | 0 |
| challenger never ratnaps (`w3_chnonap`) | 14001 | `1119-180324-ab-6ecb` | 0.409 | −0.423 [−0.485, −0.358] | – | 565 (42 / 523) | 235 |
| challenger never places a rat trap (`w3_chnotrap`) | 14001 | `1119-180342-ab-f7de` | 0.440 | −0.391 [−0.452, −0.328] | – | 530 (43 / 487) | 232 |
| challenger's even-id opening rats go to a centre mine (cheat table, `w3_chopen`) | 14001 | `1119-180357-ab-3556` | 0.808 | −0.024 [−0.054, +0.005] | −0.021 | 283 (128 / 155) | 239 |
| main | 14002 | `1119-181328-ab-56b6` | 0.807 | – | −0.025 | – | 227 |
| null: challenger, salted stream (`w2_chsalt`) | 14002 | `1119-182508-ab-54fb` | 0.832 | +0.025 [−0.002, +0.051] | – | 258 (143 / 115) | – |

- **Self-kills do not bias the reads.** awubot's own-error deaths sit in its throw code (OX: every one at
  `TryThrow.run`'s `assert(rc.canThrowRat())`; they fall from ~250 a run to 1–28 when grabs or throws go), but 250
  over 1,140 games is 0.2 a game, below the intervals.
- **Main's draw.** On 14002 main reads 0.807 against its salted null's 0.832: oracles there read ~2.5 pp high against
  main, so the 14002 rows' "vs null" column is the one to compare with 14001.
- **Which losses they win** (main's 192 losses on 14001, by §1.2's regime; the salted nulls win 52 of 80 sudden,
  15 of 31 early-called, 31 of 51 long and 24 of 30 points losses by re-randomisation alone, and lose ~125 of main's
  wins; `D:byregime.py`):

| oracle | sudden (80) | early-called (31) | long (51) | points (30) | main's wins lost |
|---|---|---|---|---|---|
| nulls (mean) | 52 | 15 | 31 | 24 | 125 |
| no king hits | 27 | 7 | 11 | 5 | 33 |
| no ratnaps | 68 | 21 | 34 | 19 | 38 |
| no ratnaps in r1–300 | 64 | 20 | 32 | 20 | 51 |
| no throws | 63 | 26 | 40 | 28 | 21 |
| no rat traps | 49 | 21 | 38 | 29 | 58 |
| neither ratnaps nor traps | 70 | 25 | 43 | 30 | 14 |

  (A change that touches few games, such as the king oracles, re-randomises few of them, so its counts sit below the
  nulls': read those against main.) awubot's grabs decide the sudden early losses, its traps the long and points
  games, its throws both.

---

## 5. Packages, ranked

Gains are pp of `goal`. "Ceiling" is an oracle's value; "expected" what a buildable version could get.

| rank | id | change | evidence | ceiling / expected | test | risk |
|---|---|---|---|---|---|---|
| 1 | **EX** | The r1–300 rat exchange, defence: fewer of our rats grabbed, thrown and trapped. Mechanisms, each its own arm: (a) shared blind-side warning (an ally that sees an enemy step to a teammate's blind side squeaks it; 34% of blind grabs start in an ally's sight, 19% in the victim's own); (b) HP-rule exposure (a damaged rat facing a healthier enemy is grabbable while it watches it: 39 grabs a game; pull damaged rats behind healthy ones); (c) throw damage (half of awubot's victims die within 12 rounds; double hits on our rats near the carrier's line); (d) trap hits (66 a game, 47% within 4 of a mine, 23% followed by a grab) | §3.6, §4: no grabs +0.091, no throws +0.120, no blind grabs +0.070, no HP grabs +0.078, no traps +0.069 (+0.077 on 14002), neither +0.135; r1–300 carries 75% / 60% | ceiling 13.5; expected +1 to +3 an arm, +4 to +6 stacked | diagnostic on replays first (`D:GrabStats.java`, `D:TrapWhere.java`: grabs, blind/HP split, throws, trap hits per game, r1–300); then the precision route, 3,000 cells on two fresh seed bases (docs/evaluation.md item 5) | high: CX's caution arms lost presence and aggression faster than they saved grabs (−0.09); FL tunes the same code |
| 2 | **EX-o** | The exchange, offence: more of awubot's rats grabbed, thrown and trapped by ours. | our grabs are worth 42 pp to us, our traps 39 pp (§4); awubot already fires 90–100 of our traps a game and we grab 73–80 of its rats | no oracle prices more of it; TN2 found the grab weight and threat penalty load-bearing | – / unknown | FL's ground (micro tuning on the native engine) | medium |
| 3 | **MS** | The worst map-seat pairs, one at a time: EscapeTheNight B (1 win in 56), popthecork B, houseofcards B, asabovesobelow B, peaceinourtime A, bed B | §2.3, §3.5: 6 pairs under 0.5 hold 12% of losses (10,640 pooled games); the two centre-line maps lose all games from the second-acting seat to awubot's centre take | ~1.9 (all six to 0.9); expected +0.5 to +1 | per map: replays of the pair, then `map:` pools of 40+ cells at fresh seeds, then a `goal` harm test | medium: a map-specific rule must not hurt the other maps |
| – | KP | Deny awubot's king its contact turns (spawn tiles occupied or buried) | OX: a king frozen whenever it senses one of ours +7.1 [+4.7, +9.5]; ours are within 5 of it in 5–11% of its samples in losses | ceiling 7.1, no mechanism | not now: needs our rats at its king, which §3.1 found missing | – |
| – | centre | Deny awubot the centre mines | W2 +8.0, CD: out of sight | closed | – | – |
| – | king defence | Any protection of our king against rats | §3.7: +0.015 / +0.013 against the null | ≤ 1.5–4 | closed | – |
| – | raid, finisher, cats, cooperation, per-map constants, opening presence | §3.1–3.5 | each ≤ 1–2.6 pp or no window | closed | – | – |

---

## 6. What 0.90 needs

- **The target.** Main reads 0.832 (14001), 0.835 (W2, 9201), 0.807 against a null of 0.832 (14002) and 0.841 on
  1,520 games (61013): about 0.83. 0.90 is +7 pp, about 80 of 1,140 games.
- **The reservoirs, priced**: the exchange 13.5 pp (union of awubot's grabs and traps; its throws alone 12.0), the
  centre 8.0 (W2; out of sight, CD), awubot's king's contact turns 7.1 (OX; no mechanism), awubot's formations 2.1
  (W2), our king's safety ≤ 1.5–4, the map-seat tail ~1.9, cats ~0.9 (KD). They overlap: the centre race and the
  exchange of r1–300 are the same rounds, and the oracles' gains are not additive (no grabs + no traps 13.5 against
  9.1 + 6.9).
- **The two routes** (`D:mapvar.py`): cutting every map-seat pair's loss rate by 43%, or lifting the worst 40 pairs to
  0.97. The loss mass is spread: 116 of 190 pairs are under 0.9 and hold 89% of the losses. Only a change that acts
  on every map gets there, and the exchange is that change: its oracles convert the sudden, early and long losses
  alike (§4).
- **The arithmetic.** Capturing a third of the exchange is ~+4.5 pp; with MS (~+1) and RS's re-tested small gains
  (+1 to +2, in progress) the sum reaches 0.90. Capturing it needs several arms of 1–3 pp each stacked, so each must
  pass the precision route; a δ 0.05 test cannot see them (CX's arms read H0 at +0.02).

---

## 7. Data, tools and probes

- `D:` = `~/battlecode26-data/scratch/w3/`. Java readers compile against
  `~/battlecode26-data/engine/jars/battlecode26-java-1.2.6.jar` into `D:cls/` (`java -cp JAR:cls NAME OUT.jsonl
  replay.bc26...`):
  - `W3Stats.java`: the end of cooperation (round, team, actions), every 10 rounds per king its position, HP, own and
    enemy rats within 6 and the nearest enemy rat, cats within 5; per team the rats' distance to the nearest enemy
    king. Ghost-safe (positions from robots that took a turn this round or the last).
  - `KingLedger.java`: HP lost by kings per game and team: enemy bites, scratches, throw collisions (by the thrower's
    team), traps, starvation, rest.
  - `GrabStats.java`: enemy grabs by grabber team: blind or HP rule, after a trap hit, thrown / dropped / released,
    victim dead within 12 rounds, the blind grabber's start tile seen by the victim or by an ally; trap hits and deaths
    per team; grabs per 100 rounds. (A thrown rat's collision record precedes its thrower's `ThrowRat` in a replay.)
  - `KingBites.java`, `ThrowDbg.java`: bites on kings by attacker type; a replay's throw and grab records.
- Python (`python3`, numpy):
  - `predict.py ECON.jsonl`: univariate AUC by round and a cross-validated logistic model (folds by map), with and
    without the map prior (§1.1); `decide.py TRACE.jsonl ECON.jsonl`: p series per game (W1Trace, 10 rounds), called
    rounds, the regime table, the largest fall's attribution, `pwin.json`; `observable.py` (own-state model);
    `seatprior.py MAIN MIRROR` (r0 map and mirror-seat prior).
  - `splitcells.py`, `splitecon.py`, `splitwin.py ECON.jsonl [TERMS]`: split cells, won against lost game (§2).
  - `mapvar.py`: map and map-seat variance over pooled main-equivalent runs, `mapseat_p.json`; `turnorder.py` (§2.3).
  - `interact.py CAMPAIGN...`: TN screens' per-map choice out of sample (§3.5).
  - `windows.py`, `exposure.py`, `kingrace.py`, `coop.py` (§3); `grabtab.py GRAB.jsonl`.
  - `paired.py BASE RUN... [--nulls=N1,N2]`: a run's score on the games it shares with BASE, the paired diff with a
    bootstrap CI over maps, results changed, and the diff against the nulls' mean; `byregime.py RUN...`: main's
    losses each oracle wins, by regime.
  - `mkcopy.sh SRC DST`: an oracle copy of a package in the probe worktree (java only, package renamed).
- Data: `D:m14001/` (the 14001 main run through W3Stats, W1Trace, W2Econ, KingLedger, GrabStats),
  `D:w2main.w3s.jsonl` (W2's main run through W3Stats), `D:pwin_w2.json`, `D:pwin_14001.json`, `D:runs.txt` (every
  run of this package).
- Probe branch `t3/w3-probes` (worktree `D:wt/`, never merged); every probe a copy under its own package name:
  awubot copies `w3_awunokb` (never bites a king), `w3_awunokt` (never throws into a king), `w3_awunok` (both),
  `w3_awunonap` (never ratnaps), `w3_awunotrap` (never places a rat trap), `w3_awunonaptrap` (neither),
  `w3_awunothrow` (never throws an enemy rat), `w3_awunapseen` (grabs only a rat that sees it), `w3_awunapblind`
  (grabs only a rat that cannot see it), `w3_awunap300` / `w3_awunapL` (no ratnaps before / after r300),
  `w3_awunotrap300` (no rat traps before r300); challenger copies `w3_chnonap`, `w3_chnotrap` (our weapons removed),
  `w3_chopen` (even-id opening rats to a centre mine, W2's cheat table).
