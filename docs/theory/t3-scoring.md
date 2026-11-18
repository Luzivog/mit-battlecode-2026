# T3: scoring and game theory

Lens: cooperation and backstab, the point formula, tiebreaks, when to keep or break cooperation, how to win on
points, the weight of cat damage, best responses by opponent type.

Source keys used below:
- `R§n`: `docs/rules.md` section n.
- `E:path:line`: engine source at tag `engine.1.2.6`, under `engine/src/main/battlecode/`. Local clone:
  `~/battlecode26-data/engine/ref/battlecode26`. The 1.2.5 and 1.2.6 sources differ only inside `GameWorld.spawnCheese`
  (R§0), and every scoring line cited here matches the line numbers in `docs/rules.md`.
- `src/...:line`: bot code. Generated classes are cited by their `.jinja2` template line.
- `D:file`: my parsed data and tools in `~/battlecode26-data/theory/t3/` (§9 says how to reproduce them).
- **Mirror**: the official-conditions run of challenger (identical to awubot) against awubot: 43 official maps, both
  seats, engine 1.2.5 (the result file's `engine_version`; `--engine official` ran the unmodified 1.2.5 jar), each map's own seed. The main checkout holds its results
  (`match_results/abtest_2026-11-18_19-30-32.json`) and replays (`matches/run-2026-11-18_19-30-32-*.bc26`), parsed
  into `D:mirror_official2.txt`. Identical code means the seat decides every map 1-1. These games therefore show
  how the game ends and how close each ending is, not who is stronger.

---

## 0. Headline: the dominant behaviour in this domain

1. **Kings are the main scoring variable.** The number of living kings decides the game-ending check: you lose
   only when your last king dies. It is also 50% of the points after a backstab and 30% before one. Holding 3 kings
   against 2 is worth +10 points of margin, more than any cat or cheese lead seen in close games. The engine stops
   formation of a third king after round 1200 (E:world/RobotControllerImpl.java:1173-1178). So: **have ≥ 3 kings
   alive before r1200, refill to 2 after any loss, and protect them late**. The champion almost never has a third
   king; in the mirror, 8 of the 11 points games ended 2 kings against 2.
2. **The point comparison works like a comparison of real numbers.** Truncation never creates a tie except at exact
   equality (§1.2). Points games are decided by tiny margins: in 6 of the 11 mirror points games the final lead D was
   under 2 points (the winner scored under 51), and in 3 it was under 1 (§3.2). Any systematic edge of +1 to +2
   points converts these games from seat-decided coin flips into wins. They are 25.6% of mirror games.
3. **Under cooperation the game has a clock: the last cat's death.** The team that deals the final blow to the last
   cat ends the game, so it chooses the ending moment. Never kill the last cat while behind. Deliver all carried
   cheese before the end. Take cat HP from cats the enemy would otherwise kill. Get the king lead early on maps where
   contact is late. The champion lost two final-tournament games this way: it ended the grand-final game on
   `groveisbeingdesecrated` itself while behind on cheese 180 to 550, and on `t_minus_five_minutes` a cat pounced over
   the wall into Goobie's half, where Goobie dealt the 240 HP that decided the game (§3.4).
4. **Late in a game going to r2000, cat damage on live cats is the cheapest source of points.** One bite on a cat
   (10 HP) is worth roughly 100× one round of a rat's mining at typical end-of-game totals (§1.4). Convert spare
   global cheese and raw cheese into points before r2000: deliver all raw cheese, bite cats with 1-2 cheese each, and
   reform a second king if down to one. The tiebreaks are nearly worthless (§1.5).
5. **Cooperation versus backstab.** Against bots that backstab on contact (the champion and every strong public bot),
   cooperation lasts until the first contact: round 20 median in the mirror (§3.1). The choices left are who takes
   the blame (the victim gets 100 rounds of exclusive cat-trap placement, which no public bot uses), and, on maps
   where the sides are separated by dirt, whether to dig toward the enemy. Against a bot that does not backstab
   first, choose the scoring regime by ΔD = 40·(c − k) (§2.3).

---

## 1. The scoring function (derivation)

### 1.1 Formula and when it is read

`setWinnerIfMorePoints` (E:world/GameWorld.java:911-951):

```
share_i   = mine_i / (mine_i + theirs_i)      (0 if the total is 0), computed as a float
points    = (int)( w_cat·share_cat + w_king·share_king + w_cheese·share_cheese )      ×100 scale
coop:      w_cat = 50, w_king = 30, w_cheese = 20
backstab:  w_cat = 30, w_king = 50, w_cheese = 20
```

The categories (R§1):

| Category | Counted as | Source |
|---|---|---|
| cat | Damage dealt to cats by bites and cat traps, each hit capped at the cat's remaining HP. It is credited to the biter's team or the trap owner's team. | E:world/InternalRobot.java:617-619, E:world/GameWorld.java:635-637 |
| king | Kings alive at the moment the score is read | E:world/GameWorld.java:928 |
| cheese | Cheese transferred by baby rats, plus cheese picked up by kings. Not counted: raw cheese still carried, cheese absorbed into global by a formation, the 2500 starting cheese. | E:world/RobotControllerImpl.java:500,524,1388 and 1240-1267 |

When the score is read:
- Last cat dies in cooperation: at the instant of that cat's destruction, in the middle of the round
  (E:world/GameWorld.java:1181-1182 → `checkWin` → `setWinnerifAllCatsDead` :852-866). The winner is fixed then:
  later events in the same round cannot override it (:992-994).
- Round 2000: after all turns of round 2000 (`processEndOfRound` → `checkEndOfMatch`, :968-978, :1021).
- Both teams lose their last king in the same round: scored on points (:983-990). The same line also re-scores a
  king-kill win on points if, later in that round, the last cat dies in cooperation. That is a theoretical edge
  case and does not appear in the data.

**Difference form.** In a points ending each team has ≥ 1 king, so the king total is positive. The cat and cheese
totals are positive in every mirror points game. A category whose total is 0 (no cat damage at all, e.g. maps whose
cats are walled in) gives 0 to both teams and drops out. Over the categories present, my lead in points is

```
D = p_me − p_them = Σ_i  w_i · (m_i − t_i) / (m_i + t_i)          (m = mine, t = theirs)
```

Each category contributes its weight times the relative difference. D = 2·(p_me − 50) when all three categories
are present. I win iff D > 0 (§1.2).

### 1.2 Truncation never creates a tie

The categories with a nonzero total have weights summing to S ∈ {100, 80, 70, 50, 30} (kings are always present),
which is always even.
Then p_me + p_them = S. If p_me > S/2, then ⌊p_me⌋ ≥ S/2 and ⌊p_them⌋ ≤ S/2 − 1, so I win. **The engine's truncated
comparison is therefore the real-number comparison**, apart from float rounding at exact equality. A points tie needs
exactly equal real points. That happens only with fully symmetric totals, e.g. cat damage 4000/4000, kings 1/1 and
equal cheese transferred. No mirror or sparring game tied. In the tournament data the only ties are 2 degenerate
games where both teams lost their last king in the same round with every total at 0 (closeup and mushroom, decided
by coin flip, `D:official_tourney2.txt`).

### 1.3 Kings

D_king = w_king·(a − b)/(a + b) for a kings against b:

| a v b | 1v1, 2v2 | 2v1 | 3v2 | 3v1 | 4v2 | 4v3 | 5v2 | 5v3 | 2v3 |
|---|---|---|---|---|---|---|---|---|---|
| backstab (50) | 0 | +16.7 | +10.0 | +25.0 | +16.7 | +7.1 | +21.4 | +12.5 | −10.0 |
| coop (30) | 0 | +10.0 | +6.0 | +15.0 | +10.0 | +4.3 | +12.9 | +7.5 | −6.0 |

Engine limits (R§6, E:world/RobotControllerImpl.java:1166-1178): at most 5 kings. After round 1200 a team with
≥ 2 kings cannot form another; a team with 1 king can still form a second. **The number of kings at r2000 is
therefore at most max(kings formed by r1200 that survive, 2).**

### 1.4 Exchange rates

These are the partial derivatives of D at parity (m = t), for typical end-of-game totals. T is the total cat damage
by both teams at the end; X is the total cheese transferred by both teams.

| Lever | 1 point of D costs | Typical r2000 game (T = 8000, X = 40000) | Early cooperation ending (T = 8000, X ≈ 730, the `groveisbeingdesecrated` final) |
|---|---|---|---|
| Extra cat damage (enemy's unchanged), backstab / coop | T/30 / T/50 HP | 267 / 160 HP | 160 HP |
| Cat damage taken from the enemy's share (T fixed), backstab / coop | T/60 / T/100 HP | 133 / 80 HP | 80 HP |
| Extra cheese transferred | X/20 | 2000 cheese | 36 cheese |
| Cheese taken from the enemy (kill a loaded rat, pick up its drop) | X/40 | 1000 cheese | 18 cheese |
| One more king at 2v2 → 3v2, backstab / coop | +10 / +6 | – | – |
| Losing one king at 2v2 → 1v2, backstab | −16.7 | – | – |

What one rat-round is worth late in a backstab game heading to r2000, with T = 8000 and X = 40000:
- **A bite on a cat that would otherwise survive:** 10 HP × 30/T = **0.0375 points**; 0.045 with a 2-cheese bite
  (12 HP).
- **A round of mining:** the mirror r2000 games transfer about 8k–26k cheese per team over 2000 rounds with roughly
  15–40 baby rats (D:mirror_official2.txt), i.e. about 0.2–0.7 cheese per rat-round. At 20/X = 0.0005 points per
  cheese that is **about 0.0001–0.00035 points**.

So at the end of a points game a bite that lands on a live cat is worth **100× or more** a rat-round of mining. If the
cat would otherwise die to the enemy, the bite takes HP from the enemy's share and is worth 2× more again. If the cat
would otherwise die to us anyway, it only shifts timing and is worth nothing. Cat HP is finite (4000 per cat), and
the cat component is frozen once all cats are dead.

### 1.5 Tiebreaks

The order is: points (equal only at exact equality, §1.2), then more **global** cheese (E:world/GameWorld.java:872-886;
raw cheese is not counted), then more baby rats plus kings alive (:892-906), then `Math.random()` (:958).

Points ties are nearly impossible, so the tiebreaks are worth nearly nothing. Two consequences:
- The global cheese the champion banks at the end (§3.5) has almost no value; converting it into points (§4.4) is
  better.
- A spawn surge at r1900 for "tiebreaks" (GST `src/opp_gst/RatKing.java:104`, food) trades tiebreak 2 (global cheese)
  for tiebreak 3 (rats alive). It can only matter at an exact points tie, and then it hurts.

---

## 2. Endings and the cooperation state machine

### 2.1 Endings

| Ending | Trigger | Scored by | Source |
|---|---|---|---|
| King kill | A team's last king dies (combat, cats, starvation, its own exception) | – | E:world/GameWorld.java:1179-1180, :833-845 |
| Cooperation cat ending | The last cat dies while in cooperation | Points, cooperation weights, read at that instant | :1181-1182, :852-866 |
| Round 2000 | No winner after round 2000 | Points, with the weights of the mode the game is in | :968-978 |
| Both last kings die in the same round | – | Points | :983-990 |

### 2.2 Two irreversible doors

- **Backstab** (R§2). It is opened by the first bite on an enemy rat or king (E:world/InternalRobot.java:623-625), the
  first grab of an enemy rat (:648-649), or the first time an enemy triggers a rat trap, which blames the **trap
  owner** (E:world/GameWorld.java:639-642). It needs contact. Thrown-rat collisions, formation crush and anything
  involving cats never backstab. Effects: the weights switch, the cat ending is disabled, and cat-trap placement
  changes (§2.4).
- **Cooperation cat ending.** It is opened by whoever deals the final damage to the last cat. Each cat dies to one
  team's hit or trap.

As a game: in cooperation, each team that can reach an enemy rat holds a **veto** (backstab), and each team that can
reach the last cat holds an **exit** (kill it). The team ahead on cooperation points wants the exit; the team behind
wants the veto. Which options exist depends on the map: contact gives the veto; reaching the cat gives the exit
(§2.5). The champion uses neither deliberately: it backstabs whenever it meets an enemy, and it kills cats near its
king whenever they come (§5).

### 2.3 Which weights favour me

D_coop − D_backstab = 20·(2c−1) − 20·(2k−1) = **40·(c − k)**, where c is my cat-damage share and k my king share.
- If my cat share exceeds my king share, cooperation weights are better for me.
- If my king share exceeds my cat share, backstab weights are better.

Example: 2 kings against 1 (k = 0.67) with cat damage even (c = 0.5): the backstab weights are worth 6.7 points more
to me.

This matters only against an opponent that does not backstab first, or on maps without contact. A backstab also
changes the game itself: it allows a king-kill ending and turns off the cat ending.

### 2.4 Blame and the cat-trap window

`catTrapsAllowed(team) = isCooperation || (roundsSinceBackstab ≤ 100 && backstabber ≠ team)`
(E:world/GameWorld.java:420-423).
- The team blamed for the backstab loses cat-trap placement for good.
- The victim keeps it for 100 rounds.
- Traps already placed keep working (R§2).

A cat trap costs 10 cheese for 100 damage, credited to the owner. A bite does 10 damage for one rat action. The
window is therefore worth up to the cat damage the victim's traps can collect in 100 rounds. In cooperation, the
champion's own trap code collected about 4000 HP per team per 100–150 rounds when its rats were near a cat (TheHeist:
41 traps per side, `D:opp_validation2.txt`).

No public bot places cat traps after a backstab: awubot's placement is gated on `rc.isCooperation()`
(`src/challenger/robot/rat/BabyRat.java:36`), and OBG, GST, Goobie and food do the same (§5). **The window is unused
by everyone.** Its value depends on geometry:
- Damage dealt to a cat that both teams would eventually kill only shifts timing (§1.4).
- The window pays on maps with many cats (rift 6, cheeseguardians 10, sunandmoon 20, where most cat HP survives to
  r2000), or when the traps hit cats that the enemy would otherwise kill.

### 2.5 Contact classes, and cats crossing walls

A BFS over each map, between the two king centres, for a 1×1 rat moving in 8 directions (`D:MapScan.java`,
`D:maps_local.txt`, all 150 maps in `maps/`):

| Class | Rule | Maps |
|---|---|---|
| Wall-separated | No path even through dirt | `coop`, `mining`, `peaceinourtime`, `t_minus_five_minutes`, `testpathfinding`, `testpathfinding2` |
| Dirt-separated | A path exists only by digging | 50 maps, incl. TheHeist (193 steps), far (169), averystrangespace (101), wallsofparadis (79), rift (77), minimaze (69), pipes (41), cheesefarm (19), keepout... |
| Open | A dirt-free path exists | the rest (e.g. DefaultMedium 36, groveisbeingdesecrated 70, clockwisdom 374) |

Contact is necessary to backstab, but not sufficient: the open grand-final map `groveisbeingdesecrated` ended in
cooperation at r160 because both armies stayed home dealing with their cats.

**Cats cross walls by pouncing (not in `docs/rules.md`).** `canPounce` (E:world/InternalRobot.java:882-939) checks
only that the target tile and the 4 landing tiles are on the map and passable (no cat, no king), and that the target
is within distance² 13 of the cat's centre (E:common/MapLocation.java:120-125). The path between is never checked,
so a cat in ATTACK mode jumps over walls up to about 3 tiles thick.

Observed: final-tournament replay `bc26final_323524` game 5 (`t_minus_five_minutes`). Two full wall rows (y = 9, 10)
separate the halves. Cat 13 moved (1,7) → (0,11) between r352 and r353 at 270 HP, and died in Goobie's half
(`D:CatPath.java`, `D:replays/`). Consequences for scoring:
- On wall-separated maps the cats are not confined, so their damage can be taken by the other side.
- A king on such a map is not safe from a cat on the other side.

**Kings act across thin walls (derived, not observed).** Vision is cone and radius only, with no line of sight (R§7).
A king bites and places traps up to distance² 8 from its centre (R§7-8). A king next to a 1-thick wall can therefore
bite an enemy across it (a backstab) and place cat traps on the other side.

### 2.6 What a bot can observe of the score

`RobotController` has no getter for cat damage, cheese transferred or the enemy's king count
(E:common/RobotController.java). A bot can know or infer:
- **Own king count:** from the shared array (awubot tracks it, slots 2-8).
- **Own cheese transferred:** count the king's pickups plus transfers, or read the changes in global cheese net of
  known spending.
- **Own cat damage:** count own bites and own cat-trap triggers. `getNumberCatTraps()` drops when a trap fires or is
  removed.
- **Enemy cat damage on a cat in view:** (4000 − cat HP) − my damage to that cat. `RobotInfo.health` is visible for
  cats.
- **Enemy kings:** only those seen.
- **Enemy cheese transferred:** unobservable; estimate it from map symmetry.

Any points estimator is therefore exact on my side and estimated on theirs. The decisions that matter most (kings,
end-of-game conversion) are monotone and need no estimator: more is always better.

---

## 3. What decides games now (data)

### 3.1 Ending mix (Mirror, 43 maps × 2 seats, `D:mirror_official2.txt`)

- **Kill: 32 maps.** Ends between r68 and r600 on 23 maps, and late (r995–r1846) on 9: Meow, cheeseguardians,
  closeup, hatefullattice, keepout, mercifullattice, safelycontained, toomuchcheese, cheesefarm.
- **Points: 11 maps = 22/86 games = 25.6%.** Of these, 9 maps reach r2000 after a backstab. The other 2 end in
  cooperation by cat deaths: TheHeist at r390, peaceinourtime at r451.
- **First backstab round** (one game per map, 41 maps with a backstab): median r20, 34 of 41 by r40, maximum r154.
- **Cause of the first backstab:** a bite or grab in 31 cases, an enemy walking into a rat trap (owner blamed) in 10.
  The attribution comes from the action order in the backstab turn and is approximate.
- **Quick-pool mirror** (local engine, seed override, `match_results/abtest_2026-11-18_20-20-10.json`): 38 kill and
  10 points. TheHeist and pipes went to points 4/4, sittingducks 2/4.

### 3.2 The points games and what flips them

D is the winning seat's lead, recomputed from the final totals (it reproduces the engine's winner every time). "raw"
is the raw cheese carried at the end, which does not count. The last column is the cheapest lever that would flip
the losing seat, by §1.3-1.4.

| Map (end) | Kings W/L | Cat W/L (T) | Cheese W/L | Raw W/L | D | Flips the losing seat |
|---|---|---|---|---|---|---|
| TheHeist (r390, coop) | 2/2 | 4000/4000 | 2850/2675 | 2000/420 | 0.64 | Deliver its 420 raw; or a 3rd king (+6); or not finishing its own cat first (it did at r240, handing the timing to the other seat, §3.3) |
| peaceinourtime (r451, coop) | 2/1 | 4000/4000 | 2940/1590 | 180/820 | 15.96 | 3 kings vs 2 (+6 plus cheese → +0.04; +4.0 if the 820 raw were delivered). A 2nd king alone is not enough. |
| ZeroDay (r2000) | 2/1 | 1040/420 | 22932/18718 | 1018/236 | 31.4 | Not flippable by one lever (needs 4 kings vs 2) |
| averystrangespace (r2000) | 2/1 | 640/1340 | 11283/11330 | 780/40 | 6.02 | Keep its 2nd king (formed r770, lost later) → 2v2 → the loser leads by 10.6 |
| cheesebottles (r2000) | 2/2 | 1970/1770 (3740) | 10806/11772 | 1420/800 | 0.75 | +94 HP of cat bites (cats alive); or a 3rd king |
| minimaze (r2000) | 2/2 | 4550/3450 (8000, all dead) | 24983/25434 | 460/620 | 3.94 | Kings only (3v2) |
| pipes (r2000) | 2/2 | 4000/4000 (all dead) | 21241/18817 | 90/224 | 1.22 | Kings only |
| rift (r2000) | 2/2 | 11540/7810 (19350 of 24000) | 26185/24846 | 218/120 | 6.31 | Kings (3v2) |
| sittingducks (r2000) | 2/2 | 4000/4000 (all dead) | 20912/18067 | 500/0 | 1.46 | Kings only. The loser ended with 0 global, 0 raw and 3 rats. |
| uneruesansfin (r2000) | 2/2 | 4020/3980 (all dead) | 8174/7708 | 0/0 | 0.74 | Kings; or about 600 more cheese transferred |
| wallsofparadis (r2000) | 2/2 | 1460/1230 (2690, cats alive) | 13597/14875 | 140/112 | 1.66 | +150 HP of cat bites (+75 if taken from the enemy's share); or kings |

Summary:
- **A third king that survives** flips the losing seat on 10 of 11 maps (all but ZeroDay). On peaceinourtime the
  loser has 1 king, so it needs 3 against 2.
- **Late cat bites** flip 2 (cheesebottles, wallsofparadis).
- **Raw-cheese delivery** flips 1 (TheHeist).
- In 8 of 11 games both seats ended with exactly 2 kings, and many second kings formed only after r1800
  (cheesebottles r1901/r1901, uneruesansfin r1821/r1827, wallsofparadis r1815, sittingducks r1820, ZeroDay r1924).
  That is awubot's relaxed endgame formation, which applies when a team has 1 king
  (`src/challenger/robot/king/KingFormationCoordinatorStrategy.java:194-204`, `robot/rat/FormationStrategy.java:68`).

### 3.3 Sparring bots (`D:opp_validation2.txt`, 76 games, engine 1.2.5, seed 0)

- Every game backstabbed at r10–26 (bites or rat traps), except the 4 TheHeist games against OBG and GST.
- **Those 4 stayed in cooperation and ended by cat deaths** at r152–286, with cat damage exactly 4000/4000: each team
  killed the cat on its own side. The 4000/4000 reading is genuine, not a replay artifact. Per-hit attribution
  confirms it: in the grand-final `groveisbeingdesecrated` game, awubot dealt 370 bites × 10 + 3 cat traps × 100 =
  4000 to its own cat, and GST 1500 + 2500 = 4000 to its own (`D:CatHits.java`). Cheese and kings decided them:
  - awubot won 3 (one with kings 2/1);
  - GST won one as team B by 320 to 290 cheese.

In the TheHeist mirror game, the challenger seat killed its own cat at r240. The awubot seat's cat stayed at
3730/4000 damage from r300 to r380, and awubot finished it at r390 while leading on cheese 2810 to 2595, winning by
0.64. The seat that kills second chooses when the game ends.

### 3.4 Official tournament (replay files listed in §9)

Champion games that ended on points or in cooperation:
- **Final, wins:** fullofsecrets (coop r1006, cheese 2910/1815); rose (coop r2000, kings 2/1 after a formation at
  r1901); where_are_you (kings 3/1); FreeTheCheese (kings 2/1).
- **Intl quals, wins:** isnotgold ×2, neitheroptionisgood, pipes_the_second.
- **Losses:**
  - **`groveisbeingdesecrated` (grand final, 323541#1), coop r160.** An open map (§2.5). GST killed its own cat by
    r130. awubot's rats fought its own cat near its king and dealt all 4000 of its HP (370 bites, 3 cat traps;
    `D:CatHits.java`). It had transferred 40 cheese by r110 against GST's 370, and its final blow at r160 ended the
    game at cheese 180 vs 550. **The champion ended the game itself
    while behind** (a cheese share of 0.247, −10.1 points). Holding that cat would have kept the game going while
    its economy caught up.
  - **`t_minus_five_minutes` (final vs Goobie, 323524#5), coop r362, 49.48–50.52.** awubot led on cheese (1570 vs
    1500). Cat 13, on awubot's side, pounced over the wall rows at 270 HP (§2.5). Goobie then dealt 240 of its last
    270 HP: 2 cat traps, 2 king bites and 2 rat bites (`D:CatHits.java`). That gave cat damage 7760/8240. Without
    the 240 HP, awubot wins on cheese (+0.46).
  - **sieveofriches (intl quals 302626#3), r2000.** awubot had 3 kings to 1 (+25) and still lost: cheese 30k vs 72k
    (share 0.30, −8.0) and cat damage 516 vs 2270 (share 0.185, −18.9). **A king lead is not unconditional.**
  - **sieveofriches (302663#3) vs Goobie:** Goobie formed 4 kings at r83–127 and won on kings 4/1 and cheese 91k vs
    33k.
  - **insertmeowname (302661#4):** kings 1/2, cat damage 360 vs 5440.
  - **tminusten (302646#2):** awubot's king died at r103 while still in cooperation. Only cats, starvation or thrown
    rats can do that.
- **Many-king play exists at the top.** muscallonge (V5, final 3rd) reached 5 kings on clockwisdom (formations at
  r332–933, coop win on kings 5/3), and Goobie reached 5 on seepastyourgreed. These come from the multi-game replay
  files, parsed per game into `D:official_tourney2.txt`.

### 3.5 State at r1100 in the r2000 mirror games (`D:state_at.txt`)

Both seats hold 790–2120 global cheese, 12–39 baby rats and 1–2 kings at r1100. 9 of the 18 seats still have 1
king. A third king costs 50 cheese plus 7 rats: about 400–500 cheese at a 16-rat base, where each extra rat costs
50–60 (R§6). **That is affordable in every one of these games**, and the champion stops trying at r1100
(`KingFormationCoordinatorStrategy.java:145`), 100 rounds before the engine's cutoff.

At the end of the 11 points games the seats hold a median of about 970 global cheese (range 0–2600) and up to 2000
raw (`D:pts_games.txt`). Neither counts for points.

---

## 4. The dominant behaviour, derived

### 4.1 Kings first

Kings appear in both the game-ending check and the points formula. Each extra king:
- raises D by w_king·(a−b)/(a+b);
- adds a target the enemy must kill before it can win by king kill;
- mines: a king's pickups count as transferred with no delivery delay (E:world/RobotControllerImpl.java:498-501).

Its cost is 50 cheese, 7 rats and 2 cheese of upkeep per round. Upkeep comes from global cheese and does not reduce
transferred cheese.

The rules:
- **Before r1200, own ≥ 3 kings on any map where the game is not decided early.** A 4th king keeps 3 alive after one
  loss.
- **After r1200, if down to 1 king, form a second immediately.** awubot waits until r1800 (§3.2).
- **Never trade a king late:** a king lost at 2v2 costs 16.7 points.
- **Take enemy kings that are not their last.** One kill at 2v2 gains 16.7.

The cheapest formation is **budding**: the king spawns 7 rats into a clear 3×3 next to its spawn ring, and the centre
rat calls `becomeRatKing`. No recruiting from the field, but the recruits spawn on the king's 16-tile ring (`buildRat`
distance² ≤ 8), so they step 1–2 tiles into the 3×3 (R§8 `becomeRatKing`). Goobie's 4 kings by r127 on sieveofriches show it works.

The limit (sieveofriches, §3.4): kings do not compensate for an economy at 0.3 share and cat damage at 0.19. Kings
come first, but not at any price.

### 4.2 The cooperation phase

- **The cat clock.** In cooperation each team, with cat traps and its rats near a cat, kills about 4000 HP in 100–150
  rounds (TheHeist and grove data). A 2-cat map can therefore end in cooperation by r130–400 if contact comes later.
  That is likely on wall-separated and dirt-separated maps, and on open maps when both armies stay home (grove).
- **The last-cat rule.** Do not deal the final damage to a cat that may be the last one alive unless the estimated
  D_coop > 0. While behind:
  - stop biting that cat;
  - remove own cat traps on its path (free, no cooldown, R§8);
  - keep rats out of its target list.

  Kill it when ahead. The cheap estimate is structural: with a king lead (2v1 = +10 in cooperation) the enemy needs a
  cheese share above 0.75 to come back, so finish the cat. Otherwise, wait.
- **Bank before the clock runs out.** In cooperation, transferred cheese is the whole economy term, and early totals
  are small (X ≈ 700 at r160: 36 cheese = 1 point). When visible cats are low, or own cats are dead, rats deliver at
  low thresholds. awubot's thresholds reach 105 raw (`src/challenger/robot/rat/MinerStrategy.java:181-186`), and
  awubot held 2000 raw at the TheHeist mirror ending.
- **Take cat HP from the enemy's share.** Each HP of a cat the enemy would kill is worth 2× (§1.4). Cats wander and
  pounce across walls (§2.5): when one enters your half, all nearby rats and the king hit it, and traps go on its
  path.
- **Get the king lead early on maps likely to end in cooperation.** These are wall- or dirt-separated maps with a long
  king-to-king path, and maps with no enemy seen by about r100. A budded second king before r150 is +10 points in
  cooperation and costs 150 cheese with no baby rats alive, 430 at 16 (7 rats at the rat cost curve, rules.md §5, plus 50).
- **Contact choice on dirt-separated maps.** Digging toward the enemy hands it the veto. If ahead on cooperation
  points, don't dig and run the clock. If behind, digging forces contact, and the champion then backstabs on its own,
  which cancels the cat ending.
- **Blame.** Against a bot that backstabs on contact, holding fire until it bites makes us the victim and gives us the
  100-round window (§2.4). The cost is the first exchange: its bite gives it the HP lead that grab legality depends on
  (R§8 `carryRat`). Rat traps placed in cooperation make us the backstabber when they fire, which happened in 10 of 41
  mirror backstabs. The value is uncertain; it is a test, not a rule (P6).

### 4.3 The backstab phase

- Weights 30/50/20; kings come first (§4.1).
- **Cat damage.** Cat HP is finite and frozen once every cat is dead. The share is decided by who deals the HP that
  someone will deal anyway, plus any HP still alive near r2000.
  - On maps where all cats die (pipes, sittingducks, uneruesansfin, minimaze), the cat term splits by who killed
    which cat, usually 4000/4000.
  - On maps with many cats or late cats (rift, cheesebottles, wallsofparadis), extra bites near r2000 are pure gain
    at about 100× the value of mining (§1.4).
  - Use 1–2 cheese per bite on cats: +1 or +2 damage at 1 cheese each, the best rate in the damage curve
    10 + ⌈√c⌉ (R§6). awubot forces 0 cheese on non-rat targets (`src/challenger/robot/rat/BabyRat.java:245-249`).
  - Kings bite cats within distance² 8 when no enemy is in range. awubot's king never bites cats
    (`src/challenger/robot/king/KingCombatStrategy.java.jinja2:180-195`).
- **Points-aware aggression.**
  - A late king kill is the largest swing available: 16.7 at 2v2, and a win if it was their last king.
  - awubot's late second kings form at a far mine from up to 7 rats (`FormationStrategy.java:68`,
    `KingFormationCoordinatorStrategy.java:194-204`). They are the softest target. Formation also needs a fully
    passable 3×3 (R§8), so placing dirt on the formation tiles blocks it.
  - If ahead on points near the end, turtle: keep kings away from fights and cats.

### 4.4 Endgame conversion (from about r1800, earlier for slow deliveries)

1. All rats with raw cheese go home in time. "In time" is distance-aware: about 1 tile per round when moving
   forward, slower with a large load (R§4).
2. A team with 1 king forms a second (allowed after r1200 with 1 king).
3. Spend spare global cheese on points:
   - cat bites at 1–2 cheese, wherever a live cat is reachable;
   - another king if allowed;
   - nothing else. Hoarding is worth only tiebreak 2.
4. Kings pick up any cheese within their 3×3 (it counts as transferred) and stay away from danger.

### 4.5 Best responses by opponent type

| Opponent type | Cooperation policy (code) | Typical ending | Best response on scoring |
|---|---|---|---|
| Backstab on contact, ≤ 2 kings (awubot; OBG, GST and most others) | Bites, grabs or traps any enemy in reach, no cooperation check (§5) | King kill; points on stalemate maps (25%); cooperation endings on separated maps | §4.1 kings ≥ 3 by r1200; §4.4 conversion; §4.2 last-cat rule, banking and taking cat HP on cooperation maps; P6 blame test |
| Polite: no bites or traps in cooperation (cerr: `src/opp_cerr/BabyRat.java:326,397`; it still grabs, :271-273) | Leaves the veto to us | Cooperation can last on open maps | Choose the regime: backstab early if stronger in combat (king-kill wins); otherwise pick by ΔD = 40(c−k) (§2.3); keep cooperation while leading on cat damage and end it by killing the last cat |
| Cooperation cat farmers (OBG sends all rats at cats in cooperation, `src/opp_obg/BabyRat.java:86-88,168`; GST cat micro in cooperation, `src/opp_gst/BabyRatStateUpdaters/DefectBRStateUpdater.java:61`) | Race to cat damage | Cooperation ending when cats die before contact | Out-trap them on cats they would kill; if behind on cats, force contact (dig) to veto the ending |
| Many kings (muscallonge V5, Goobie; not vendored) | – | Points at r2000 with 3–5 kings | Match the king count before r1200 (§1.3: 3v5 is −12.5); kill their kings that are not the last |
| Rushers | Early backstab, king hunt | Early king kill | Points are irrelevant; an extra king is insurance, since the game ends only when the last king dies |

---

## 5. Where the champion and the top bots deviate

| Point | awubot / challenger | OBG (`src/opp_obg`) | GST (`src/opp_gst`) | Others |
|---|---|---|---|---|
| Cooperation policy | None. Combat entry, bites, grabs and traps never check `isCooperation` (`robot/rat/combat/CombatStrategy.java.jinja2:143-206`; docs/combat.md §7). The king bites any enemy within distance² 8 (`robot/king/KingCombatStrategy.java.jinja2:180-195`). | None. `MyUnit.tryAttack` grabs or bites any enemy in range (`MyUnit.java:97-112`). | None. Micro starts on seeing an enemy (`DefectBRStateUpdater.java:56-59`). | cerr: no bites or traps in cooperation (`opp_cerr/BabyRat.java:326,397`). |
| Cat traps | Cooperation only, and only when the rat stands within distance² 9 in the cat's 90° front (`robot/rat/BabyRat.java:36,38`). **NPE** at `:42,:68` (`senseRobotAtLocation(...)` is null on an empty tile), which loses the turn in front of the cat (docs/strategy.md §7). | Cooperation only, along the cat's heading (`BabyRat.java:90-117`). | Cooperation only, needs raw ≥ 30 (`BabyRat.java:308`); removes stale cat traps to recycle the 10-trap cap (`BabyRat.java:220-223`). | Goobie and food: cooperation only. Nobody uses the victim window (§2.4). |
| Last-cat timing, banking before a cooperation ending | None (lost grove, §3.4). Go-home thresholds up to 105 raw (`robot/rat/MinerStrategy.java:181-186`). | None | None | None |
| Kings | 2nd king gated on ≥ 5 known mines and ≥ 20·kings rats (16 on small maps), against a normal cap of 16; 3rd needs ≥ 9 mines and ≥ 40 rats; aborts at t ≥ 1100 with ≥ 2 kings (`robot/king/KingFormationCoordinatorStrategy.java:13,145,194-204`). Relaxed late 2nd king (t > 1800); becomes king anywhere after r1900 (`robot/rat/FormationStrategy.java:68`). Mirror: max kings ≤ 2 on 42 of 43 maps. | 2nd king from r350 and rat cost > 40 (`RatKing.java:26,151-161`); fuses anyone from r1950 (`Constants.java:13`) | ≤ 2 kings (3 above 2500 tiles) (`RatKing.java:65`; `RatKingBehaviors/RKHeadquarter.java:144`) | cerr: backup king on panic, everyone becomes king after r1990 (`opp_cerr/BabyRat.java:208-225`). Goobie: income-driven, up to 5. |
| Cat damage in the backstab phase | Rats fight cats within distance² 36 of the king, or hunt when t > 500, cheese > 1000, ≥ 16 rats, raw 0, and id%3==0 or t ≥ 1800 (`CombatStrategy.java.jinja2:455-456`). Bites on cats always use 0 cheese (`BabyRat.java:245-249`). The king never bites cats. | Cat hunters from r1200 (`Constants.java:9`, `BabyRat.java:193`) | Cat micro after r1700 (`BabyRat.java:109`) | – |
| Endgame | No raw-cheese recall. Global cheese kept near the "very rich" line 3000 − 0.75t, i.e. about 1500 at r2000 (`robot/king/KingSpawningStrategy.java:28-29`). | Converge on the king from r1900 (`Constants.java:11`) | Spawn spam from r1900 (`RatKing.java:104`; anti-points, §1.5) | food: spawn surge at r1950 |

---

## 6. Work packages

Effects are against awubot under official conditions, as percentage points (pp) of games won. The upper bound
assumes the package flips only the seats §3.2 identifies and changes no other game. Protocols use `tools/games`
(docs/evaluation.md): paired seats, a sequential test against awubot on the quick and tune pools, then the
sparring-bot gauntlet.

The **points diagnostic** is not a tuning pool. It is the 11 mirror points maps (§3.2), both seats, official engine
and seeds, plus the holdout maps known to end on points or in cooperation (groveisbeingdesecrated,
t_minus_five_minutes, fullofsecrets, rose, clockwisdom, sieveofriches, coop, mining). Its metric is the **final D per
game** (§7).

### P1. Three kings by r1200, two after (kings for points and survival)
- **Hypothesis.** Owning ≥ 3 kings from before r1200 until r2000 wins nearly every points game (§3.2: +10 points
  flips the losing seat on 10 of 11 maps) and cuts late king-kill losses (9 maps end by king kill after r990). Method:
  - **Budding (§4.1):** the king spawns 7 rats into an adjacent clear 3×3; the centre rat calls `becomeRatKing`.
  - **Trigger:** from about r600–1000 (T1 decides the economic timing), when the game is still going, global cheese
    ≥ about 500, and fewer than 3 kings. Deadline r1190.
  - **After r1200:** with 1 king, re-bud at once.
  - **Late:** king safety, with no fights near kings when ahead.
- **Expected effect.** Upper bound +11.6 pp (10 losing seats of 86 games: every §3.2 map except ZeroDay).
  Realistic +4 to +8 pp, with an unknown sign on early king-kill games.
- **Test.**
  - The points diagnostic: final kings per game and D must rise.
  - Then the sequential test on quick and tune.
  - Watch the king-kill loss rate on maps that end before r600; it must not rise.
  - Gauntlet: OBG, GST, and many-king behaviour (no vendored bot does it; Goobie's code `src/opp_goobie` forms
    income-driven kings).
- **Risks.**
  - 7 rats taken out of the army mid-game.
  - King deaths: each costs 10–16.7 points.
  - Kings packed together can see > 25 robots, which makes `RobotTracker` throw and loses the turn. Above 43 robots
    an assert fails, and the escaping `AssertionError` **destroys the king** at the end of that turn
    (E:world/GameWorld.java:356-359; docs/architecture.md §1.6, §5.2). That is a king death: −10 to −16.7 points, or
    the game if it was the last king. Budding must keep the robot count each king sees below 25.
  - More king turns cost more wall time (1200 s per team per game, R§14).
  - A bud next to the main king shares its danger. A separate safe site costs a walk (T4 site choice).
- **Size.** Medium: about 150–250 lines (trigger, site test, spawn-into-block, bud). It overlaps T1's formation work
  and should be merged in synthesis.

### P2. Endgame points conversion
- **Hypothesis.** From about r1800:
  - rats deliver all raw cheese before r2000, with distance-aware go-home and no exploration;
  - a team with 1 king forms a second;
  - spare global cheese buys cat damage (bites at 1–2 cheese, and the king bites cats when no enemy is in range);
  - kings collect cheese within their 3×3 and avoid danger.

  This adds about +0.5 to +2 points on r2000 games and on late cooperation endings.
- **Expected effect.** It flips the losing seats with margin < 1.7 that cats or cheese can reach: cheesebottles,
  wallsofparadis, TheHeist (raw cheese), possibly uneruesansfin. That is +3 to +5 pp, nearly free.
- **Test.** The points diagnostic, primary metric mean paired ΔD (§7); then the sequential test on quick and tune;
  the gauntlet must not regress.
- **Risks.**
  - Rats walking home late can be killed; that only matters if it decides a king kill.
  - Cat bites expose rats to scratches and pounces; late rats are cheap.
  - Taking global cheese below the spawn floor could stall replacement spawns: keep a reserve against enemies in
    sight.
- **Size.** Small: about 60–120 lines (`MinerStrategy` go-home rule, an endgame branch in the combat cat gate, a 0 → 1/2
  cheese change for cat bites, a king cat bite).

### P3. Control of cooperation endings
- **Hypothesis.** While in cooperation:
  - (a) apply the last-cat rule (§4.2);
  - (b) deliver at low thresholds once visible cats are low or own cats are dead;
  - (c) bud a second king before about r150 on maps likely to end in cooperation (wall- or dirt-separated with a
    long king-to-king path, or no enemy seen by about r100);
  - (d) take cat HP: all rats and the king hit any cat in our half, including cats that pounce across walls.

  Version 0 of the last-cat rule needs no estimator: in cooperation, at most one cat seen recently, and that cat at
  HP ≤ 300 → finish it only if own kings ≥ 2; otherwise stop damaging it and remove own cat traps within 2 tiles of
  its path.
- **Evidence.** Grand-final grove (self-ended while behind), t_minus_five_minutes (240 HP taken), TheHeist mirror
  (finished its own cat first, lost by 0.64 with 420 raw carried), peaceinourtime mirror (the second king decided
  it), TheHeist against GST and OBG (all 4 games ended in cooperation).
- **Expected effect.** +2 to +3 pp on the official pool (TheHeist and peaceinourtime, 4 games). Larger on the holdout
  and on tournament-like pools: 2 of the champion's 5 final-tournament game losses were cooperation endings (§3.4).
- **Test.** The points diagnostic restricted to cooperation-prone maps, both seats: the share of cooperation endings
  won, and the D of those games. Then the sequential test on quick and tune.
- **Risks.**
  - A cat kept alive near the king can kill it (awubot's king died to a cat in cooperation on peaceinourtime in
    instrumented runs, docs/strategy.md §8).
  - The enemy may finish the cat anyway.
  - An early bud slows the opening economy.
- **Size.** Medium: version 0 about 80 lines; with a points estimator (P8) about 200.

### P4. Cat-damage share in the backstab phase
- **Hypothesis.** Hunt cats earlier and harder once a game looks like a stalemate (no enemy king contact, t ≥ about
  1300), bite them with 1–2 cheese, let kings bite them, and prioritise cats on the enemy's side or about to die to
  the enemy. At about 100× the points value of mining late (§1.4), this adds +2 to +6 points where cats are alive
  late.
- **Expected effect.** +2 to +4 pp: cheesebottles, wallsofparadis and rift seats, and the many-cat maps (rift,
  cheeseguardians, where_are_you, FreeTheCheese). About 0 where all cats die early.
- **Test.** The points diagnostic: cat share per game; then the sequential test. Check king-kill losses after r1300.
- **Risks.**
  - Rats lost to pounces and scratches.
  - Hunters away from the king weaken its defence.
  - Pulling cats toward our own king (cats target the last rat they see, R§12).
- **Size.** Small to medium: about 60–150 lines (combat cat gate, cheese for cat bites, king cat bite, a target
  preference).

### P5. Points-aware late aggression and king denial
- **Hypothesis.** At t ≥ about 1600, with a points estimate (P8):
  - if behind and the enemy has ≥ 2 kings, strike the weaker king that is not its last (+16.7);
  - deny awubot's late second-king formation (its rats gather within 25 tiles of the formation mine,
    `FormationStrategy.java:116`): attack the group, or put dirt on the 3×3;
  - if ahead, turtle.
- **Expected effect.** Uncertain, 0 to +4 pp. It overlaps T2 (micro) and T5 (opponent models).
- **Test.** The points diagnostic plus late king-kill maps; count enemy kings at r2000.
- **Risks.** It exposes our army and kings late. A failed strike costs more than it gains.
- **Size.** Medium to large.

### P6. Blame policy: be the victim, use the 100-round window
- **Hypothesis.** While in cooperation:
  - place no rat traps;
  - do not open with a bite or grab until the enemy backstabs, or until a deadline (e.g. r60);
  - then, for 100 rounds, place cat traps on cat paths.

  Gains: exclusive cat traps (100 HP for 10 cheese) at a cost of one early exchange.
- **Expected effect.** Small against awubot (−1 to +2 pp), possibly larger on many-cat maps. It is the only way to
  use a mechanic no public bot uses (§2.4).
- **Test.** quick and tune; measure cat damage by r150 and the first-exchange outcome; the points diagnostic.
- **Risks.**
  - Losing the opening fights: the first bite decides grab legality.
  - The rule must not suppress self-defence near the king.
- **Size.** Small to medium: a cooperation gate in the `CombatStrategy` entry and `TryTrap`, plus a post-backstab cat-trap
  routine (about 80–120 lines).

### P7. Fix the cat-trap NPE
- **Hypothesis.** `considerPlacingCatTrap` throws an NPE when the probed tile is empty (`BabyRat.java:42,68`). The
  rat then loses its turn while standing in front of a cat. Checking for null restores cat farming in cooperation and
  removes a source of rat deaths.
- **Expected effect.** Small and positive on cooperation maps (+0 to +1 pp).
- **Test.** Paired games on TheHeist, peaceinourtime and grove; cooperation cat damage per 100 rounds.
- **Risks.** It changes bytecode and the random stream (R§16, docs/architecture.md §7), so close games flip; judge it
  only with the tool.
- **Size.** About 2 lines.

### P8. Points estimator (infrastructure for P3 and P5)
- **Hypothesis.** Kings keep a running estimate in the shared array (slot 15 is free, docs/architecture.md §4.1):
  - own kings, exact;
  - own cheese transferred: changes in global cheese plus known spending, plus king pickups;
  - own cat damage: own bites and cat-trap triggers, reported by squeak;
  - enemy cat damage: 4000 − cat HP − ours, per cat seen;
  - enemy kings seen.

  This gives the last-cat rule and late aggression a basis better than structural proxies.
- **Expected effect.** None by itself; it only enables P3 and P5.
- **Test.** Offline: compare the estimate with the replay truth on the points diagnostic (the replay holds the real
  totals, §9).
- **Risks.** Bytecode: kings run about 13k of 20k (docs/architecture.md §8); squeak traffic.
- **Size.** Medium: about 150 lines.

**Top three by expected effect against awubot:** P1 (up to +11.6 pp, realistic +4 to +8), P2 (+3 to +5), P3 (+2 to
+3 on the official pool, more on cooperation-heavy holdouts). P2 is the cheapest and safest, so build it first. P1
has the largest upside; merge it with T1's formation work.

---

## 7. Testing notes for points packages

- **Use a continuous metric.** A points package moves D by 0.5–10 points, and most mirror margins are under 2. Win/loss
  on the 11 points maps is therefore close to noise for small packages. The final D per game (§1.1) can be computed
  from any replay (`D:Coop2.java` prints `pts=` and every component), paired by map, seed and seat. Its paired mean
  difference is much more sensitive. It is a diagnostic; acceptance stays with the `tools/games` sequential test.
- **Read the ending type.** A points package must not convert points wins into king-kill losses. Report the ending
  mix and the king-kill loss rate by ending round.
- **Official conditions matter here.** Which maps reach points depends on the seed and the engine: sittingducks went
  to points in 2 of 4 quick-pool games under the local seed override (§3.1).
- **The mirror is maximally fragile on these maps.** Any bytecode change reshuffles them (docs/architecture.md §7). A
  flip on a margin < 1 is not evidence on its own: look for a consistent shift in D.

---

## 8. Unsettled or not yet verified

1. **Cats pouncing over walls** is settled from the engine and one replay (§2.5), but it is not in `docs/rules.md`.
   The synthesis should add it to R§12 and R§16. Not measured: how often it happens on wall-separated maps.
2. **Kings acting across 1-thick walls** (bites, traps up to distance² 8) is derived from R§7-8, not observed.
3. **The value of the victim window** depends on how often rats are near cats in rounds 20–120. It is not measured;
   P6 is the experiment.
4. **King-kill outcomes with more kings** (does a third king cost early fights?) cannot be derived; P1 measures it.
5. **Backstab blame attribution** in my parser is approximate: it reads the action order in the first
   non-cooperation turn and does not resolve whether a bite targeted a cat. Exact attribution needs an engine log line.
6. **`tminusten`** (awubot's king died in cooperation at r103): settled by T5 (`docs/theory/t5-opponents.md`, A12).
   A cat stuck against the parked king scratched it to death. In scoring terms it is a king-kill loss in cooperation
   that no points play can recover; the fix is king safety (T5 P1b).
7. **Deaths by `Error` count as deaths.** An escaping `AssertionError` destroys the robot at the end of its turn
   (E:world/GameWorld.java:356-359). Every king count in this file comes from the engine's own per-round king count
   (§9), so such deaths are already included. This also covers the champion's ≥ 26-mine overflow, which destroys
   all its kings (docs/architecture.md §1.6; T5 A15).

---

## 9. Reproducing the data

All files are in `~/battlecode26-data/theory/t3/`. Compile with the classpath in `cp.txt` (the engine 1.2.5 jar from
the Gradle cache plus flatbuffers). Each tool is a single Java file with no project dependency.

| File | What it does |
|---|---|
| `Coop2.java` | One line per game (a replay file may hold several): map, packages, winner, win type, end round, cooperation or backstab round and approximate blame, recomputed points, cat count, cheese transferred, cat damage, kings (final and max), global cheese, baby rats, cat traps placed, raw cheese held at the end, king formation rounds. Kings and global cheese are decoded from `Round.teamAliveRatKings`, which the engine fills with `kings + 10·globalCheese` (E:world/GameWorld.java:1013). This is why that field "looked like nonsense" in docs/intel.md §7. |
| `mirror_official2.txt` | `Coop2` over the 86 Mirror replays |
| `opp_validation2.txt` | `Coop2` over the 76 sparring-bot validation replays (`/tmp/bc26-opponents/work/matches/`) |
| `official_tourney2.txt` | `Coop2` over the 43 official tournament replay files, 215 games (`/tmp/bc26-history/downloads/replays*/`; `/tmp` is not persistent) |
| `replays/bc26final_323524.bc26`, `replays/bc26final_323541.bc26` | Persistent copies of the two final-tournament files cited in §3.4 (`t_minus_five_minutes` is game 5, `groveisbeingdesecrated` game 1) |
| `pts_games.txt`, `state_at.txt` | The 11 points games with raw cheese held; state at r600/1000/1100/1800/1950 (`StateAt.java`) |
| `CatHits.java`, `CatPath.java` | Per-cat damage attribution (bites by unit type, cat-trap triggers by owner) and a cat's path, for game n of a replay |
| `MapScan.java`, `maps_local.txt`, `MapAscii.java` | King-to-king reachability classes for all 150 maps; ASCII rendering of a map |
| `CoopSeq.java` | Per-round cooperation flags and stats for every game in a replay file |
