# opponents.md: public bc26 bots vendored as sparring partners (`src/opp_*`)

Audience: AI agents improving `src/challenger`. Purpose:
- **Anti-overfitting.** A challenger change that beats `awubot` should not lose ground against the other strong bots.
- **Idea source.** The ranked menu is in §4.

The packages are **frozen**. Do not edit them to improve them. The only edits made were package renames (§6). Provenance for each bot is in `src/opp_<slug>/SOURCE.md`. Intel on the champion's weaknesses is in [intel.md](intel.md). Champion macro is in [strategy.md](strategy.md) and combat in [combat.md](combat.md).

## 0. TL;DR
- 9 bots are vendored. Two of them beat `awubot` in local games:
  - **`opp_obg`** (Old But Gold, XSquare's team, ladder #2) won **9/14**.
  - **`opp_gst`** (GST, 2nd in the final) won **6/14**.
  - The other 7 won 1 game out of 42 between them.
- Use `opp_obg` and `opp_gst` as the main gate after `awubot`. Use the rest as a cheap regression gauntlet.
- In 12 of the 15 OBG/GST wins, they had **1.4–2× awubot's transferred cheese** at the last 100-round sample. Examples: keepout, OBG 13.2k vs 7.6k by r900; cheesefarm, GST 5.0k vs 2.8k by r400.
  - The exceptions are OBG's DefaultMedium (A) and arrows (B) wins, where the economy was even and OBG won the fights, and GST's TheHeist win on cooperation points.
  - They had **more rats alive in every non-cooperation win** (e.g. keepout 27 vs 1, cheesefarm 16 vs 2).
  - The economy gap opens **early**, while awubot is still at its 12–17-rat cap. On keepout, GST had 2.4k vs 1.6k at r200 and 14.6k vs 8.2k at r900, running 16–26 rats against awubot's steady 14–17.
  - The levers are **economy and macro** (§4 items 1–3), not exotic micro.
- Build cost: +1–3 s on a forced full compile (≈ 19 s → 21 s). Incremental builds are unchanged.

## 1. Vendored bots

Validation: every bot played `awubot` on DefaultSmall, DefaultMedium and cheesefarm, both sides, seed 0. `opp_obg` and `opp_gst` also played DefaultLarge, arrows, keepout and TheHeist. Engine 1.2.5, worktree build, 2026-11-18.

**Notation.** W/L = result for the opponent. `A` = opponent was team A. The number after the letter is the final round.

**Games are deterministic for a fixed seed.** Reruns of 4 games reproduced the same winner and end round, including bots that use `new Random()`. So vary maps, sides and `-Pseed` instead of repeating a game.

| Package | Team | Result | Source (repo @ commit, path) | License | vs awubot | Size |
|---|---|---|---|---|---|---|
| `opp_obg` | Old But Gold (XSquare, dirbaio, Zousen) | ladder **#2, 2064**. Not tournament-eligible. 24-15 vs the champion in final-week ranked. | github.com/IvanGeffner/BC26 @70b3f40, `basic51/` | none stated | **9/14** | 36.7k lines (mostly generated) |
| `opp_gst` | Generalized Stroke's Theorem | **final 2nd**, 2032 (#3) | github.com/uravt/Battlecode26 @fb5cc3c, `src/Version40/` | AGPL-3.0 | **6/14** | 37.5k |
| `opp_cerr` | cerr or despair | **final 4th**, 1912 (#7) | github.com/thewaxmango/bc26 @f8f7864, `src/masters/` | AGPL-3.0 | 0/6 | 8.7k |
| `opp_parmesan` | Parmesan | **HS champion**, 1929 (#6) | github.com/abcdezyxwv/Parmesan-bc2026 @0a0b266, `src/bestsofar/` | none stated | 0/6 | 12.6k |
| `opp_gravy` | Gravy | 1864 (#11), HS winners' semi | github.com/battlecode-archive/2026-Gravy @f9c268a, `src/testplayer/` | AGPL-3.0 | 0/6 | 4.5k |
| `opp_spaark` | SPAARK | HS 4th, 1841 (#12) | github.com/battlecode-archive/2026-SPAARK @a164312, `src/SPAARK/` | none stated | 1/6 | 12.1k |
| `opp_goobie` | Goobie Woobies | final 7–8th (team says 6th), 1817 (#18). Took the champion to 3-2 in the final. | github.com/daannte/bc26 @bf0b5d4, `src/v52_ThisGuy/` | none stated | 0/6 | 6.1k |
| `opp_powerpuff` | Powerpuff Girls | final 9–12th, 1822 (#17) | github.com/battlecode-archive/2026-Powerpuff-Girls @91fdddb, `src/Nike/` | AGPL-3.0 | 0/6 | 16.2k |
| `opp_food` | food (alext) | final 9–12th, 1760 (#25), best-postmortem award | github.com/AlexT101/battlecode26 @d12dc7d, `src/finalsbot/` | AGPL-3.0 | 0/6 | 6.0k |

**Per-map results:**
- `opp_obg`:
  - wins: DefaultMedium A367 B301, cheesefarm A402 B709, DefaultLarge A604 B958, keepout A903 B977, arrows B499;
  - losses: DefaultSmall ×2, TheHeist ×2 (points), arrows A.
- `opp_gst`:
  - wins: cheesefarm A436 B386, keepout A1205 B2000 (points), DefaultLarge A400, TheHeist B152 (**points, cooperation ended at r152**: cat damage 3030 vs 3000, cheese 200 vs 160);
  - losses: DefaultSmall ×2, DefaultMedium ×2, arrows ×2, DefaultLarge B, TheHeist A.
- `opp_spaark`: won cheesefarm A314.
- Every other game was an `awubot` king kill. All OBG/GST wins except the points games were king kills.

**Which package is the final one.** Folders in these repos are often stale.
- Replay-confirmed: the packages of GST, cerr, Goobie and Powerpuff were read from the `GameHeader.packageName` of official final-tournament replays; those of Parmesan and SPAARK from the HS-tournament replays.
  - Powerpuff's repo also has a `Finals/` folder, but they submitted **`Nike`**.
  - food submitted `peppermint`, which the author later renamed `finalsbot`.
  - Gravy submitted `temp`; the archive keeps only `testplayer`.
- OBG never played a tournament after Sprint 2, where they used `basic41`.
  - `basic51` is the only folder the author kept at HEAD. Its code is unchanged since 01-25.
  - The history before commit `4eaa548` also has `basic52`..`basic62` (last one 01-29), each 24–74 changed lines from basic51. Recover one with `git archive 4eaa548^ basic62`.

**Runtime noise seen in the logs.** None of these is a vendoring problem.
- Bots print their own caught exceptions:
  - cerr, gravy and goobie: `GameActionException`;
  - merlin: `Exception("Impossible")`;
  - gst: an NPE in `BRThrownCarried`.
- Engine 1.2.5 bug: `RobotControllerImpl.processTrapsAtLocation:869` can throw `IndexOutOfBoundsException` from inside `rc.move()`. Seen twice on cheesefarm. **`move()` can throw a non-`GameActionException`.**
- awubot's own `assert(...)` in `TryThrow` (java:151/156) killed rats (the engine destroys a robot whose `run()` ends) in **15/70** games (architecture.md §1.6). This is a free fix for challenger.

**How to run:**
```
./gradlew run -PteamA=challenger -PteamB=opp_obg -Pmaps=DefaultMedium
./gradlew run -PteamA=opp_gst -PteamB=challenger -Pmaps=keepout -Pseed=3
```
For many games, use `autoscrims/ABtest.py` (testing.md) with the opponent package as the baseline.

**Per-round stats.** To get rats alive, cheese transferred and cat damage per round from a local `.bc26`, use the flatbuffer recipe in intel.md §7: on `Event.Round` read `teamAliveBabyRats`, `teamCheeseTransferred` and `teamCatDamage`. The numbers in §0 came from this.

## 2. bc26 repos found but not vendored

Search: `gh search repos/code` (battlecode26, "battlecode 2026", bc26, "uneasy alliances", RAT_KING, becomeRatKing, …), the battlecode-archive org, the battlecode.org/past.html postmortems, and the platform ladder (`api.battlecode.org/api/team/bc26/t/?ordering=-rating`) matched to commit authors. Team packages come from replays where noted.

| Repo | Team (result, rating) | License | Why not vendored |
|---|---|---|---|
| battlecode-archive/2026-The-Complex-Merlin (= nzjt/battlecode26), `java/src/TheComplexMerlin` | The Complex Merlin (HS 2nd, 1867 #9). Submitted as `lactosetolerantalexa`. | AGPL-3.0 | Valid. It compiled and ran (0/6 vs awubot). But its generated `Symmetry.java` is **15 MB**, which adds **+30–50 s to every full compile** (2–3× baseline). Vendor it only if needed (§6). |
| quesswho/BattleCode2026 | **Mango Tango** (final 5–6th, 1839 #13). Submitted `current`. | AGPL-3.0 | `current` is jinja output that was not committed at the freeze. `java/src/current` at HEAD is a post-season cleanup (02-07/08). The newest committed pre-freeze build is `java/src/bot16` (01-27). An exact copy needs their generator run at 7d13d81. The champion went 5-0 ranked and 43-0 unranked against them. Next candidate if wanted. |
| VarunVejalla/battlecode26 | JDK? More like IDK (final 9–12th, 1725). Submitted `cat_avoidance_micro_off2` (replay). | AGPL-3.0 | Mid-pack; kept the set at 9. Easy to add. |
| HugoIngelsson/podemice-Battlecode-2026 | podemice (final 9–12th, 1729). Submitted `ZaraLarsson` (replay; folders at repo root). | none | Mid-pack. Easy to add. |
| Coderz75/BattleCode2026 | Lorem Ipsum (final 13–16th, 1634) | none | Weaker. The repo is 2.2 GB (committed replays). Postmortem: references/postmortems/2026/2026-lorem-ipsum.md. |
| outercloudstudio/battlecode-2026 (default branch `v2-fire`) | 3MiceWalkIntoABar (final 13–16th, 1625). Submitted `sprint_1`. | AGPL-3.0 | Weaker. Blog postmortem. Their open-source match runner **github.com/outercloudstudio/nudge** is tooling: don't vendor it. |
| battlecode-archive/2026-TSPAARK (= spsquared/battlecode26) | TSPAARK (1663). Final `Delta` per README. | AGPL-3.0 | Weaker sibling of SPAARK. |
| bman12345678/battlecode-26 | lollol (1801 #20) | AGPL-3.0 | No identifiable final (`goon*` series, last push 01-26). |
| justinottesen/battlecode26-bots | Anyone Can Cook (1636) | AGPL-3.0 | Weaker. |
| ask8000/battlecode26; PixelRifts/battlecode-26; Gavdingo/Battlecode-2026 | Bumchit (1710); Mushak Raj (1617); a member of Dovish Dolphin (1817) | AGPL / AGPL / none | Early-season snapshots only (last push 01-11, 01-16 and 01-13). |
| Louis-max-H/bc26 | Raclette (1339) | AGPL-3.0 | Weak. Jinja output not committed. Postmortem: references/postmortems/2026/2026-louis-max.md. |
| battlecode-archive/2026-ProofOfConcept (= r3viviFY/battlecode26_released) | unknown | none / MIT | Neural-net and evolutionary experiment (JS generator, `thread_N_teacher_M` packages). No identified final. |
| winkelmantanner/battlecode26_waffle_genetic | waffle (1551) | none | Linear-genetic-programming experiment. Weak. |
| terryvanbelle/battlecode26-vibe, battlecode26b-vibe | (AI-agent training loop) | AGPL / none | Derivative: the `bench_*` packages are copies of GST and SPAARK. Process notes: references/postmortems/2026/2026-terryvanbelle-vibe-training-algorithm.md. |
| CS50Crab/The-Blades…, OmarAyman33/…Allied-Ratster…, C0derTang/battlcod-26, XSercia/battlecode26, amcsz/battlecode-2026, nfgehrs/Battlecode26 (= obyrnesd12/Battlecode), quanticsoul4772/battlecode2026, GuzziFlipFlops/MIT-battlecode26-aditya, StuxnetStudios/Skaven, jdishihara/battlecode2026bot, Krak9n/battlecode-2026 (RISC), seru9/BattleCode2026, zdwalter/battlecode_2026_ai, Camleaf/battlecode26, WiredMind2/battlecode26, amandaxhuang/BattleCode26, luhoan/battlecode2026, battlecode/battlecode26-lectureplayer | ratings ≤ 1616 or unranked | mostly AGPL | Weak, early-season, lecture-player or LLM-generated bots. |
| eh5320, 07ahu, Banana2308, tysonking33, wfzhangtx, multiii, raineykalina, StuxnetStudios/Battlecode2026, hacker12353213, WilliamMcKeown1, danielhe13, mateo-uru, Coderz75/bc26-post, JonathanBokeunChang/BattlecodeCoderz | – | – | Scaffold or examplefuncsplayer only, empty, or no bc26 Java. |
| Cambridge/UNSW "Battlecode 2026" repos, softmax-universe ports, `BC26` IoT/Solana/etc. name clashes | – | – | Not the MIT bc26 game. |

**Top teams with no public code found:**
- glorp (#4, 2003, final 5–6th);
- muscallonge (#5, final 3rd, package `V5`);
- Sloppy Joe Pirates (#8, video postmortem only);
- walk em down (#10, final 7–8th, package `dev`);
- Git Miners, Blue Dragon, Was?, Mango Tango's exact final (see above).

## 3. What each vendored bot does differently from the champion
Paths are inside `src/opp_<slug>/`.

**opp_obg (Old But Gold).** Hand-written managers plus generated tables (`ObservationUtil`, `ThrowHelper`, `BFSAdd`, `LocalBFS`).
- **Roles by spawn index** (`BabyRat.java`):
  - `miner()` = 3 of every 4 rats (after the first 3);
  - "slaves" (id%4 ∈ {0,1}) walk to a new-king target;
  - `catHunter()` (id%4==2) is active from r1200.
- The king hands each new rat **an assigned mine, round-robin** (`RatKing.buildWrapper` → `CommunicationManager.reportTargetMine/getAssignedMine`).
- Rats carry up to 100 raw cheese, but micro treats more than 20 raw as "retreat" (`MicroManager.tooMuchCheese`).
- **King macro** (`RatKing.java`):
  - The king sits on the known mine **closest to the map centre** (`findBestMine`).
  - Spawning (`shouldBuildRat`): while rat cost ≤ 40 (30 if ≤ 4 mines known), spawn if global > 5×cost. Above that, keep a reserve that shrinks over time: spawn if `global − (2100 − round) ≥ 5×cost`.
  - **Emergency spawns** go toward a visible cat with no closer ally, or toward visible enemies (`buildEmergencyRats`).
  - The king places dirt between itself and a cat within d² ≤ 8 (`tryPlaceDirt`).
  - Spawn direction spreads rats out: distance to the edge ÷ (spawns in that direction + 1) (`getBestLoc`). GST and Goobie copied this.
- **Second king** (`shouldCreateNewKing`/`setNewKingTarget`):
  - Conditions: round ≥ 350, rat cost > 40, known mines > 4×kings (halved once symmetry is known), and global ≥ 1000 (or cost > 50 and global > 500).
  - Target: the mine closest to the centre that is ≥ √300 tiles from every base.
  - Slaves go there and `becomeRatKing`.
- **Micro** (`MicroManager.java`): the 9 squares are ranked by a safety tier:
  - tiers, worst to best: KIDNAPPED / MOVE_KIDNAPPED / VERY_SURROUNDED / SURROUNDED / FREE_ENEMY_HIT / CAT_DANGER / NO_DANGER;
  - these use the enemy's max HP within 2 and within 8 versus own HP (+11 per extra enemy), and ally counts.
  - A **rat trap is pre-placed toward an approaching enemy** at d² 3–8, roughly when that enemy has HP ≥ 50 (or we carry raw cheese) and global cheese ≥ 300–500 (`shouldPlaceTrap`, `MicroInfo.shouldPutTrap`).
- **Bite cheese breakpoints** (`MyUnit.tryAttack`): it pays 1/4/9 cheese to push an enemy at 11–13 or 21–24 HP over a threshold, or to flip the HP order. Note that 4 and 9 overpay: damage is `10+ceil(√c)`, so 2 and 5 suffice.
- **Grabs**: it grabs whenever `canCarryRat`, then throws after ≥ 2 carry turns. The `ThrowHelper` collision search covers move+turn+throw. Emergency: turn away from its own king and throw.
- **Rats in flight squeak their direction** (`SqueakManager.squeakFlying`); allies mark that line forbidden (`MovementManager.markFlying`). This dodges collateral from thrown rats.
- **Squeak policy**: kings never squeak. A rat squeaks a cat only when the cat is closer to our king than the rat is, which pulls the cat away (`canDistractCat`). Squeaked map chunks are relayed through the shared array by the first king to everyone.
- **Endgame**: from r1900 all rats converge on the king; from r1950 any rat that can `becomeRatKing` does (`Constants.java`).
- In cooperation: cat traps are placed along the cat's heading, and rats target cats.

**opp_gst (GST).** Strategy-pattern behaviours; postmortem in references/postmortems/2026/2026-generalized-strokes-theorem.md.
- **Distress answering**: the king writes inDistress plus the average enemy position. Rats within d² ≤ 25 drop everything and fight (`BabyRatStateUpdaters/DefectBRStateUpdater.java`, `Communication/Communicator.java`).
- Spawning (`RatKing.java`, `RatKingBehaviors/RKHeadquarter.java`):
  - `RAT_BUILD_THRESHOLD = 0.002·area + 6.2` is defined (`RatKing.java:83`) but never read; rat-cost threshold 40;
  - spawning continues while cheese ≥ 2000, and is spammed after r1900.
- **King relocation**:
  - Kings walk to the **closest safe mine**: mines near enemies or cats get threat levels, and the mine symmetric to ours is marked threat 127 (`BabyRat.updateMineThreatLevels`, `RatKing.updateInfo`).
  - Kings flee squeaked cats and dig through dirt when cheese > 1000.
- **Formation**: the chief king writes a formation target when rat cost > 1.8×40; maximum 2 kings (3 on > 2500 tiles).
- Bite cheese: `Utilities.bestCheeseAmount` buys a kill, else a lead in HP, else equality; it gives up when more than 15 HP behind.
- Throw enumeration over move+turn+throw with a scored path (`Utilities.attemptThrow/scoreThrowPath`).
- Rat traps and dirt go on the **enemy-facing side of mines** (`attemptTrapMine`, `placeDirtMine`).
- Cat-path avoidance (`catPath`, `attemptLeaveCatPath`).
- Bit-shift reachability in about 300 bytecodes (`Reachability.java`).
- "Rat taxi": low-cheese rats carry overloaded rats home (`BabyRatBehaviors/BRScavengeRatPickup.java`).

**opp_cerr (cerr or despair).**
- **Panic king** (`RatKing.tryPanic`, `BabyRat.executeAscend/updateMoveTarget`):
  - when the king drops below 200 HP, a shared flag goes up;
  - rats head for the closest known mine, the lowest-ID rat anchors, and they `becomeRatKing` as a backup;
  - panic is also raised after r1980, and after r1990 every rat that can becomes a king.
- A duplicate king within d² 49 disintegrates in favour of the healthier one, before r1150 or at ≤ 100 HP (`shouldDisintegrate`).
- **Rat cap by map area** (30/40/50/60), lowered by the number of known mines (8/12/16/20) while cheese ≤ 2000 (`shouldBuildRat`).
- The king stays put ("CORNER") until it is safe and knows a mine.
- Rats go home only at 100 raw cheese.
- `BugSim`: simulates bugnav a few steps ahead before choosing a side.
- `Attention`: one arbiter for turning, covering enemy, cat, cheese, move and scout.

**opp_parmesan (Parmesan).**
- `Utils/CatsAreWalls.java`: the cat's tiles and its next step are walls in pathing.
- `Roles/AntiCat.java`: rats near the king build dirt between the king and the cat, and suicide-attack it after 10 consecutive rounds.
- `Roles/Rush.java`: rats rush the symmetric images of spawn, weighted over the symmetries still possible.
- King (`King.java`):
  - when allies ≤ enemies, spawns a defender toward the enemy centroid, but outside d² 8 of any enemy (`defendRats`);
  - bites with 1/2/5/10 cheese on enemies at 11–14 HP;
  - places cat traps at a spacing.
- Spawn reserve `cost × (20 − 15t/2000)`. 12–16 rats normally; an "insane" threshold allows more.
- A rat disintegrates when it has been carried ≥ 2 rounds, has < 50 HP and hears ≥ 2 squeaks (it denies the throw).

**opp_gravy (Gravy).**
- `BabyRat.java` states: SEARCHING / RETURNING / ATTACKING / **BAITING** / AGGRESSIVE / TO_ASSIGNED_MINE / PROMOTING.
- **Bait**: a rat leads a cat to the corner farthest from its own king, or into the enemy king (`Baiting`, `getBaitLocation`).
- `KingRat.java`:
  - camps a mine (`findValidCampMine`);
  - rat-trap and cat-trap thresholds depend on king HP;
  - spawn cost cap 80 in the mid game and 1000 late;
  - clears dirt on the spawn tile.

**opp_spaark (SPAARK).**
- **Adaptive spawn budget** (`RatKing.run`, `Sustain.java`):
  - a 100-round rolling average of rat cost sets the cap;
  - the cheese threshold scales with the number of kings, the cost and `max(0, 1500 − round)`;
  - the comments record their A/B tuning.
- Spawns face the last enemy or cat seen within d² 30.
- Micro scores actions with cheese-bite tiers of 1/2/5/10/17/26/37/50/65 cheese (the minimal cost for +1..+9) (`MicroActionScore.java`, `BabyRatMicro.java`).
- It fielded about 42 rats vs the champion's 16 in Sprint 2 (intel.md §2).

**opp_goobie (Goobie Woobies).**
- **Income-driven formation** (`RatKing.trySetFormationRallyPoint`):
  - inputs: the 20-round average of global-cheese change, known mines (≥ 3 per king, counting only our side while symmetry is unknown), cheese, or more than 40 rats;
  - it is **urgent** when the king has < 150 HP, or after r1900 with fewer than 2 kings.
- Rats fill the ring around the rally point counter-clockwise (`BabyRat.getCounterclockwiseTarget`).
- Spawn caps 8/16/20 by cheese (< 400, < 1400, else); unlimited at ≥ 3000 (`trySpawn`).
- The first spawns go toward the symmetric target (`SpawnManager.spawnDefense`).
- Cheese return distance scales with map size (`Constants.java`).

**opp_powerpuff (Powerpuff Girls, `Nike`).**
- A carried rat squeaks its own position so allies come (`Rats/BabyRat.turn`).
- Rats turn toward squeaked unseen enemies (`Helpers/ClosestUnseenEnemy.java`).
- A king alarm recalls rats within d² 12 (`Managers/Global.readAlarm`).
- A rat carrying ≥ 200 raw cheese while global cheese ≤ 1400 tries to form a king on the spot (`Actions/MergeKing.buildKingOnClump`), which banks its cheese without walking home.
- A grabbed enemy is carried and then thrown at a cat > a wall > away from our king (`Actions/ThrowOutsideCombat.java`).
- Rat limits by cheese tier: 4/8/12/16/20/24 at 125/500/800/1200/1400/1700 (`Helpers/Constants.java`).
- Design notes: `INFO.txt` and `COMBAT.txt` upstream.

**opp_food (food).** Postmortem: references/postmortems/2026/2026-food.md.
- King SOS bit in the shared array; nearby rats return (`Comms.setSOSBit`, `RobotPlayer.java`).
- Spawn surge at r1950 for tiebreaks.
- Rats can self-coordinate a formation by re-squeaking.
- Rats transfer cheese to all 9 king tiles, and don't detour for < 5 cheese.
- Kings escape a cat perpendicular to its path.
- The postmortem says "many matches were won simply by who formed a rat king first", and that keeping 16 rats on small maps cost them, because trades there are fast.

## 4. Idea menu, ranked by plausibility against the champion

"Champion" facts come from strategy.md and combat.md. Try each idea in `challenger`. Validate against `awubot`, then against `opp_obg` and `opp_gst`, and the cheap gauntlet.

| # | Idea | Who does it (paths in `src/opp_*`) | Why it should hurt awubot | Cost / risk |
|---|---|---|---|---|
| 1 | **Field more rats on medium and large maps, with an adaptive spawn budget** instead of a hard cap of 16 (12 on small maps) | obg `RatKing.shouldBuildRat` (5×cost rule plus a reserve of 2100−t); spaark `RatKing.run` + `Sustain.java`; cerr `RatKing.shouldBuildRat` (cap by area and mines); gst (spawns while cheese ≥ 2000; its `RAT_BUILD_THRESHOLD` is never read); goobie `getMaxRatsForCheese` | 12/15 OBG and GST wins came with 1.4–2× awubot's transferred cheese, and all non-cooperation wins with more rats alive (§0). intel.md §0.4: the economy ceiling lost sieveofriches 30k vs 71–91k. The champion stays at 16 until `2000−t/2` (strategy.md §5.2). | Low code cost. Keep small maps lean: food lost small maps at 16 rats. |
| 2 | **Spread the miners: assign each new rat a mine round-robin, and park the king on the central mine** | obg `RatKing.buildWrapper`→`CommunicationManager.reportTargetMine`, `BabyRat.getBestMine`, `RatKing.findBestMine`; gst safe-mine relocation `RatKing.updateInfo` | The champion's mine choice is random and distance-weighted and ignores crowding (strategy.md §2.3). On cheesefarm and keepout, OBG and GST pull ahead on transferred cheese from r200–300, before any rat collapse (inference from the per-100-round stats, §0). | Medium. The payload slot (`GlobalArray` 22–31) is already free for role and mine assignment (strategy.md §4). |
| 3 | **A reachable, rule-based second king**: from r≈350, when cost > 40, mines > 4×kings and cheese ≥ 1000, form at the most central mine far from the bases. Send a fixed share of rats (2 of every 4), not "everyone within 25". | obg `RatKing.shouldCreateNewKing/setNewKingTarget` + slaves in `BabyRat.updateTarget`; goobie income-driven `trySetFormationRallyPoint` + ring fill; food "who forms first wins" | The champion's coordinator needs ≥ 20 rats, which is unreachable at cap 16, and pulls every rat within 25 tiles off mining (strategy.md §3). Points also reward living kings. | Medium. Needs #1 to have spare rats. |
| 4 | **Thrown-rat flight squeak**: a rat in flight squeaks its direction, and allies step off the line | obg `SqueakManager.squeakFlying` + `MovementManager.markFlying`; `DodgeManager.java` is a generated table, unused in basic51 | 29% of awubot's throws hit a second enemy rat for up to 42 damage each (combat.md §0.3). A champion-vs-champion mirror suffers the same. | Low. One squeak type plus a forbidden-move mask. |
| 5 | **Backup or panic king**: when the king is low (< 150–200 HP), rats regroup at a safe mine and form a second king. Also recall rats within a radius when the king is in distress. | cerr `RatKing.tryPanic` + `BabyRat.executeAscend`; goobie `urgent` rally; gst `DefectBRStateUpdater` distress answering; food `Comms.setSOSBit`; powerpuff `Global.readAlarm` | Most awubot losses are king kills (intel.md §2: tminusten r104, attack_on_cat, mincemeat …). It has no rat recall; under attack it only raises its spawn threshold and spawns toward the threat (strategy.md §5.2). | Medium. Costs 50 cheese + 7 rats; must not trigger on cat scratches alone. |
| 6 | **Points awareness in cooperation**: when cats will die before contact, maximise cat-damage share (0.5) and cheese transferred | gst won TheHeist at r152 on points (cooperation); obg cooperation cat traps along the cat heading `BabyRat.tryPlaceCatTrap`, `shouldTargetCat` | intel.md §0.2: two of the champion's five final-tournament losses were cooperation endings. | Medium. Needs a points estimator. |
| 7 | **Minimal cheese per bite at HP breakpoints**: pay 1/2/5/10… (`10+ceil(√c)`) only to kill, or to flip the HP order that decides ratnaps | gst `Utilities.bestCheeseAmount`; spaark `MicroActionScore` tiers; parmesan `King.defendRats`; obg `MyUnit.tryAttack` (overpays) | The champion always pays 2 cheese for +2 when it has ≥ 300 (combat.md §0.2). HP ordering is its whole grab logic, so landing an exact kill or a flip matters. | Low. |
| 8 | **Pre-place a rat trap on the approach tile** at d² 3–8 when the enemy has ≥ 50 HP, and traps on the enemy-facing side of contested mines | obg `MicroManager.performMovement/shouldPutTrap`; gst `attemptTrapMine`, `placeDirtMine` | A trap (50 damage + stun) flips HP order before contact, against a bot that approaches to grab. The champion traps only when cost ≥ 70 or raw ≥ 20 (strategy.md §5.1). | Low–medium. 30 cheese each, max 25. |
| 9 | **Cat handling**: cats as walls in pathing; kings escape perpendicular to the cat's path; king dirt walls toward the cat | parmesan `Utils/CatsAreWalls.java`, `Roles/AntiCat.java`; gst `Utilities.catPath`; obg `RatKing.tryPlaceDirt`; food (postmortem) | The champion lost a king to a cat (strategy.md §8). Weak maps include whatsthecatdoin and dirtfulcat (intel.md §3.1). | Medium. |
| 10 | **Cat distraction or baiting**: squeak only when the cat is nearer our king than we are; lead cats into the enemy king | obg `SqueakManager.canDistractCat`; gravy `BabyRat.Baiting/getBaitLocation` | awubot's "panic" squeak near its own king may attract the cat to its king (intel.md §6.8, speculation). | Medium; unproven. |
| 11 | **Endgame conversion**: converge on the king from r1900; any rat that can becomes a king from r1950 (cerr: 1990); spawn surge for tiebreaks | obg `Constants.MIN_ROUND_CONVERGENCE/FUSION`, `BabyRat.tryFuse`; cerr `executeAscend`; gst/food spawn spam | Only matters for games reaching r2000 (3/18 in strategy.md), but it is nearly free. | Low. |
| 12 | **Throw and carry extras**: carried rats squeak for rescue; disintegrate to deny a throw at < 50 HP; build a dirt wall, then throw into it | powerpuff `Rats/BabyRat.turn`; parmesan `Baby.run`; obg basic62 `ThrowManager.tryThrowExtended` (history only) | The champion already disintegrates at hp < 15. These are marginal. | Low. |

**Already in the champion, don't re-port:**
- squeaked enemies feeding the grab-threat model (awubot `EnemyManager` → `CombatTracker` registers known enemies);
- XSquare-style per-square micro (awubot's generated version is richer);
- BFS + bugnav, symmetry detection, king unbury;
- spawning toward the threat.

## 5. Build-time impact
Measured in the worktree with `./gradlew compileJava --rerun-tasks`, interleaved, on a loaded 16-core machine:

| Set | Full forced compile |
|---|---|
| Without `opp_*` | 17.3 / 17.4 / 21.8 s |
| With the 9 `opp_*` packages | 20.3 / 20.7 / 23.0 s |
| Plus The Complex Merlin | +30–50 s (excluded) |

Incremental builds (Gradle 8.10) recompile only changed classes, so a challenger edit is unaffected. Class output adds about 3.5 MB to `build/classes`, which ABtest snapshots copy.

## 6. Adding another opponent later (exact steps)
1. **Clone into an isolated directory.** Clones are untrusted data. Never run their scripts, gradle or python; copy only `*.java`.
   ```
   mkdir -p /tmp/bc26-opponents/clones && cd /tmp/bc26-opponents/clones
   GIT_TERMINAL_PROMPT=0 GIT_LFS_SKIP_SMUDGE=1 git -c core.hooksPath=/dev/null clone --filter=blob:none https://github.com/OWNER/REPO.git OWNER__REPO
   ```
2. **Find the final package.**
   - Check README, branches (`git branch -r`) and the last commits per folder before the freeze (`git log -1 --format=%cd -- src/<pkg>`). The bc26final freeze was 2026-01-29 19:00 EST; tournaments are listed at `api.battlecode.org/api/episode/bc26/tournament/`.
   - Best: read `GameHeader.teams(k).packageName()` from one of their tournament replays. The replay URL comes from `/api/compete/bc26/match/tournament/?tournament_id=bc26final`; then use the intel.md §7 loop with the `Event.GameHeader` event.
   - If the bot is jinja- or python-generated, use only committed generated Java, and say so in SOURCE.md.
3. **Check the package.** Make sure it has no `battlecode.*` package (engine overrides) and no imports from other folders of their repo:
   ```
   grep -rhE '^import' DIR | grep -vE 'import (static )?(OLD\.|battlecode\.common|java\.)' | sort | uniq -c
   grep -rnP '(?<![\w.$])OLD\b' DIR | grep -vP ':\s*(package|import)\b'   # in-code FQ refs and strings
   ```
4. **Vendor and rename into a worktree, never into the main `src/` directly.** First `git -C <repo> worktree add --detach /tmp/bc26-opponents/wt HEAD`. Then copy `*.java` and rename the package (this is the exact command used):
   ```
   SRC=/tmp/bc26-opponents/clones/OWNER__REPO/src/OLD; OLD=OLD; NEW=opp_slug; DEST=/tmp/bc26-opponents/wt/src/$NEW
   mkdir -p $DEST && (cd $SRC && find . -type f -name '*.java' -print0 | cpio -0 -pdm --quiet $DEST)
   find $DEST -name '*.java' -print0 | xargs -0 perl -pi -e '
     s/^(\s*package\s+)\Q'"$OLD"'\E(\s*[.;])/${1}'"$NEW"'${2}/;
     s/^(\s*import\s+(?:static\s+)?)\Q'"$OLD"'\E\./${1}'"$NEW"'./;
     s/(?<![\w.\$])\Q'"$OLD"'\E\.(?=[A-Za-z_])/'"$NEW"'./g unless /^\s*(package|import)\b/;'
   grep -rn -w "$OLD" $DEST    # review leftovers: strings, identifiers, comments
   ```
   Use absolute paths: the copy runs inside `$SRC`.
5. **Validate in the worktree.**
   - Run `./gradlew build -x test` and check the extra compile time (`compileJava --rerun-tasks`).
   - Play ≥ 3 maps × both sides vs `awubot` and grep the logs for `Exception|Error`.
   - Fix compile errors minimally, and record every edit in SOURCE.md.
6. **Write `src/opp_slug/SOURCE.md`** (copy an existing one): repo URL, commit SHA, original path, license, team, result, platform handles, edits. Skip any repo whose license forbids copies.
7. **Move it in atomically**:
   ```
   cp -r /tmp/bc26-opponents/wt/src/opp_slug <repo>/.vendor_tmp_opp_slug
   mv <repo>/.vendor_tmp_opp_slug <repo>/src/opp_slug
   ```
   Then run `./gradlew build -x test` in the main repo, `git worktree remove /tmp/bc26-opponents/wt`, and add a row to §1 or §2.
