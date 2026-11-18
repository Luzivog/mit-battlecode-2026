# Competitive intel: how to beat `awubot`

Goal: find where the 2026 champion (`src/awubot`, "subscribe to caterpillow") is beatable. Provenance and timeline are in [history.md](history.md).

How to read this: "evidence" means official replays, API data, or code. **Speculation is labelled.** Per-game numbers come from parsing the official `.bc26` replays with the engine's flatbuffer schema (§7). Fields: win type, rounds, cheese transferred to kings (A/B), cat damage (A/B). The cat-damage field sometimes reads exactly 4000/4000, so treat it as approximate.

## 0. TL;DR, ranked
1. **Old But Gold (XSquare's team) is the real nemesis, not any tournament team.** Head-to-head in the last week was 15-24 in ranked matches. Post-season it was a coin flip (games 296-298). OBG was ineligible for tournaments. Their code is public (§5).
2. **The bot has no cooperation/backstab policy.** It fights on sight. `rc.isCooperation()` is used only to gate cat traps (`BabyRat.considerPlacingCatTrap`, added "(untested)" on the final day). Two of its five final-tournament game losses were **cooperation endings decided on points**: `t_minus_five_minutes` vs Goobie Woobies and `groveisbeingdesecrated` vs GST (ended at round 160). In cooperation, points = 0.5·cat-damage share + 0.3·king share + 0.2·cheese share.
3. **Map-specific weaknesses are large and repeatable** (§3.1): `whatsthecatdoin`, `corridorofdoomanddespair`, `safelycontained`, `jail`, `keepout`, `dirtfulcat`, plus `tminusten` (lost by about round 104 to two different teams), `sieveofriches` and `pocketsofcheese` in quals.
4. **There is an economy ceiling on cheese-rich maps.** The quals version lost `sieveofriches` three times on cheese transferred (30k vs 71-91k). Spawning holds about 12-16 rats until global cheese reaches roughly 3000-0.75t. The final-day "more aggressive spawning when rich" was "(untested)".
5. **Final-day code is untested and has known bugs.** Upstream branches `ally-throwing` and `stable` contain fixes for a throw/ratnap scoring bug, a throw-targeting edge case and border bugnav. Those fixes are not in `final`.

## 1. Data sources (public, no auth) and which API endpoints work
Base: `https://api.battlecode.org/api`. Responses are paginated with `?page=N` (10 per page).

| Endpoint | Works? | Notes |
|---|---|---|
| `/episode/e/bc26/` | ✅ | versions (`release_version_public` 1.2.6, `release_version_saturn` 1.2.7.private2), dates |
| `/episode/bc26/tournament/` | ✅ | all 7 tournaments with freeze times. `/compete/bc26/tournament/` → **404** |
| `/episode/bc26/tournament/{bc26final\|bc26intlquals\|bc26usquals2\|bc26sprint1\|bc26sprint2}/round/` | ✅ | rounds with map **ids**. Tournament maps aren't in `/episode/bc26/map/`, which lists only 42 public maps. |
| `/compete/bc26/match/tournament/?tournament_id=bc26final[&team_id=2067]` | ✅ | series scores, map names, **`replay_url`** |
| `replay_url` (storage.googleapis.com/mitbattlecode-production-secure/…) | ✅ | Public GET of gzip flatbuffer replays, 1-6 MB each. Contains per-game winner, win type, rounds, per-round team stats, and the full map. |
| `/compete/bc26/match/scrimmage/?team_id=2067` | ✅ | 3527 matches (353 pages). Scores and 3-5 map names. `replay_url` is null for scrims. |
| `/compete/bc26/match/scrimmaging_record/?team_id=2067` | ✅ | Ranked 949-526, unranked 1002-556-138 (all time) |
| `/compete/bc26/match/historical_rating/?team_id=2067` | ✅ | 667 rating points, frozen 2026-01-29 at 2095.5 |
| `/compete/bc26/match/historical_rating_topN/?N=10` | ✅ (200) | not inspected |
| `/compete/bc26/match/{id}/` | ✅ | single match |
| `/team/bc26/t/2067/`, `/team/bc26/t/?search=…`, `/team/bc26/t/?ordering=-rating` | ✅ | team info and leaderboard |
| `/compete/bc26/match/?team_id=2067` | ⚠️ | **filter ignored**, returns all 220k matches |

Final leaderboard (frozen 01-29): 1 caterpillow 2095, **2 Old But Gold 2064** (XSquare, dirbaio, Zousen, "Oak's Disciples Returned!", not student-eligible), 3 GST 2032, 4 glorp 2003, 5 muscallonge 1950, 6 Parmesan 1929, 7 cerr or despair 1912, 8 Sloppy Joe Pirates 1899, 9 The Complex Merlin 1867, 10 walk em down 1866.

## 2. Tournament results ("subscribe to caterpillow")
Final bracket (double elimination, 16 teams): **1 caterpillow, 2 GST, 3 muscallonge, 4 cerr or despair, 5-6 Mango Tango and glorp, 7-8 Goobie Woobies and walk em down** (by elimination round; a [UCalgary article](https://news.ucalgary.ca/news/ucalgary-students-win-big-mits-premier-programming-competition) reportedly says Goobie Woobies were 6th). food was 9-12. 3MiceWalkIntoABar and Lorem Ipsum were 13-16.

Every game the bot **lost** in official tournaments. The submitted package is in brackets. The opponent's package name is taken from the replay.

| Tournament / series | Lost games: map (size) → how |
|---|---|
| Sprint 1 R1 vs X7124, **1-4** [`experiment`] | sittingducks r105, dirtfulcat r114, wallsofparadis r125, trapped r105: all **king killed early (rushed)**. Won pipes on points. |
| Sprint 2 R5 vs **Old But Gold** [`basic41`], **1-4** [`awubot_fixratthrowing_old`] | knifefight r186 king, whatsthecatdoin r566 king, toomuchcheese r2000 points (cat damage 6190 vs 1450), uneruesansfin r2000 points (cheese 9.8k vs 4.6k, 28 vs 7 rats). Won TheHeist. |
| Sprint 2 R4 vs SPAARK, 4-1 | safelycontained r2000 points (cheese 37k vs 22k; SPAARK kept 42 rats vs 16) |
| Intl quals vs dumb_team, 4-1 [`awubot`=`awubot_quals`] | sieveofriches (60x20) r2000 points: cheese **71.6k vs 30.4k** |
| Intl quals vs confused [`nomv3`], **3-2** | **tminusten (60x50) r103 king**; sieveofriches r1770 king (econ war: 60 rats vs 3) |
| Intl quals vs Mango Tango, **3-2** and **3-2** | asabovesobelow r159 king; kingofthehill r1033 king; moonlitriverpark r1640 king; pocketsofcheese r270 king |
| Intl quals vs **Goobie Woobies** [`v45_ChickenJockey`]: 2-3 (WB final), then 4-1 and **3-2** | **tminusten r104 king (twice)**; insertmeowname r2000 points (cheese 20.4k vs 8.1k, cat damage 5440 vs 360); hellofromtheotherside r471 king; sieveofriches r2000 points (**90.6k vs 32.6k**); pocketsofcheese r293 king |
| **Final** R2 vs Goobie Woobies [`v52_ThisGuy`], **3-2** | learn_your_lesson (40x40) r851 king (cheese 8.1k vs 1.9k); **t_minus_five_minutes (20x20) r362 points in cooperation** (forced-cooperation map; cat damage 8240 vs 7760, cheese about equal) |
| Final R3 vs cerr or despair [`masters`], 4-1 | mincemeat (31x31) r442 king |
| **Final grand final** vs GST [`Version40`], **3-2** | **groveisbeingdesecrated (60x60) r160 points**: cats died in cooperation and GST led on cheese 550 vs 180. **attack_on_cat (50x50) r452 king** (cheese 2.7k vs 0.8k). (The winners-bracket final vs GST was 5-0.) |

Wins worth noting: fullofsecrets vs cerr ended in cooperation at r1006 and was won on cheese. rose and where_are_you vs GST went to r2000 and were won on points.

## 3. Ladder history
- **Rating** (end of day): 01-11 1357, 01-14 1588, 01-17 1750, 01-21 1900, 01-24 1974, 01-26 2056, 01-28 2033, **01-29 2095 (#1)**.
- **Season ranked record**: 370-289 (matches). This includes early weak versions.
- **Ranked 01-25..29 vs top teams**: Old But Gold **15-24**, glorp 22-4, GST 19-7, Sloppy Joe Pirates 4-2, Parmesan 8-0, Mango Tango 5-0, muscallonge 5-0.
- **Unranked on the final day** (likely mostly the team testing its own versions, so noisy): OBG 16-46, muscallonge 45-3, Mango Tango 43-0, Goobie 30-0.
- **Who they feared**: `autoscrims/scrim_script.py` `SCRIM_TEAMS` = Old But Gold, muscallonge, Mango Tango, Goobie Woobies, The Complex Merlin. Branch `micro` starts with "ONLY XSQUARE SCRIMS", and commit `f1d2961` is "improve econ (5-5 xsquare on micro maps)". `271c868`/`a70328e` were added specifically against **glorp** ("anti-glorp", "fix glorp trolling"). `be030d6` "exclude muscallonge from scrims".
- **Post-season (02-01..03-07)**: 816 ranked 3-map matches against the then-current opponent bots (versions unknown). Game totals: vs **OBG 296-298 (50%)**, GST 430-203 (68%), glorp 446-175 (72%), muscallonge 410-190 (68%).

### 3.1 Estimated per-map game win rate post-season (top-4 opponents pooled)
Scrim replays aren't public. Each match only gives `(maps[3], score)`, so the per-map rates were fit by bounded least squares (`score = Σ p_map`), with n ≈ 50-85 games per map pooled and 6-29 per opponent. **These are noisy estimates (±0.2). Use them as a guide for where to test, not as truth.**

| Map (all in `maps/`) | pooled | vs OBG | vs GST | vs glorp | vs musc |
|---|---|---|---|---|---|
| whatsthecatdoin | **0.00** | 0.02 | 0.37 | 0.11 | 0.18 |
| corridorofdoomanddespair | **0.15** | 0.00 | 0.89 | 0.00 | 0.05 |
| safelycontained | **0.25** | 0.11 | 0.45 | 0.95 | 0.03 |
| jail | **0.29** | 0.00 | 0.00 | 0.71 | 1.00 |
| keepout | **0.29** | 0.08 | 0.40 | 0.44 | 0.38 |
| dirtfulcat | 0.38 | 0.17 | 1.00 | 0.00 | 0.93 |
| TheHeist | 0.40 | 1.00 | 0.32 | 0.00 | 0.82 |
| cheesebottles | 0.43 | 0.40 | 0.47 | 0.41 | 0.46 |
| whereisthecheese / Nofreecheese / trapped | 0.47-0.49 | 0.29 / 0.42 / 0.11 | | | |
| *strong (≥0.9)*: averystrangespace, hatefullattice, minimaze, streetsofnewyork, dirtpassageway, toomuchcheese, closeup, arrows | | | | | |

Against OBG specifically, the bot is also weak on Meow (0.10), pipes (0.10), ZeroDay (0.05), wallsofparadis (0.11), sittingducks, knifefight and jail (all near 0). Raw data and the fit script are under `/tmp/bc26-history/` (scratch, not persistent). They can be regenerated from the scrimmage endpoint.

## 4. Postmortems: the 2026 meta
Sources, with their local Markdown copies (grep these; cross-year lessons are in [lessons.md](lessons.md)):
- [GST, 2nd](https://battlecode.org/assets/files/postmortem-2026-generalized-strokes-theorem.pdf): `references/postmortems/2026/2026-generalized-strokes-theorem.md`
- [food, 9-12](https://www.alext.app/Battlecode_Postmortem_2026.pdf): `references/postmortems/2026/2026-food.md`
- [3MiceWalkIntoABar](https://www.outercloud.dev/blogs/battlecode-2026/): `references/postmortems/2026/2026-3micewalkintoabar.md`
- [Lorem Ipsum](https://battlecode.org/assets/files/postmortem-2026-lorem-ipsum.pdf): `references/postmortems/2026/2026-lorem-ipsum.md`
- [Sloppy Joe Pirates](https://youtu.be/8PEjZTzAjSA): video only. Chapters: Components, Bytecode Optimization, Jinja, Winning. Expected local path if archived: `references/postmortems/2026/2026-sloppy-joe-pirates.md`.
- [XSquare's general guide](https://battlecode.org/assets/files/battlecode-guide-xsquare.pdf): not a 2026 postmortem.

Below, only the points that bear on the champion and how to beat it.

- **Backstab dominates.** Most games end in backstab. Cooperation endings happen on maps where teams can't reach each other; food names `t_minus_five_minutes`, which has a wall row. food regrets not farming cat damage "for tie breakers".
- **Micro is king, and it is XSquare-style.** Each of the 9 squares is scored (passability, ally/enemy/cat proximity) and compared with an if/else chain (GST, food). GST says it "watched so much Old But Gold".
- **Throwing matters a lot.** GST enumerates every move + turn + throw combination and scores the throw path (hits cat, enemy, wall or ally). "Move forward + face same + throw" was usually bad.
- **Ratnap geometry.** An enemy standing cardinal-adjacent can always reposition and ratnap you next turn, because a vision cone covers only 3 of its 5 reachable tiles (food). Ratnapping by position should outrank ratnapping by HP. Free allies that are being carried.
- **Information.** GST squeaks its own HP and nearest-enemy info on every micro turn to "see behind", accepting bytecode overruns. Squeaks attract cats, which locate the source. Alternate move/turn order to glance sideways.
- **Traps.** 30 cheese, 50 damage plus stun, "almost always guarantee the kill" (food). A passive player beats an aggressive one, and two passive players produce long standoffs.
- **Cheese-boosted bites.** Spend just enough to finish a rat or to equalise HP (food).
- **Macro.** About 12-16 rats is typical (spawn cost 10+10·⌊n/4⌋). Keep **fewer rats on small maps**, where trades are fast (food lost small maps after raising the minimum to 16). "Many matches were won simply by who formed a rat king first" (food). Kings camping mines, and mobile kings, became meta late. Late-game spawn surges help tiebreakers.
- **Pathing.** Dirt is the key problem. Aggressive digging is usually right (GST). Bugnav with BFS-progress fallback is standard. Cats moved in straight lines between waypoints late in the season, so kings should dodge perpendicular to the cat's path.
- **Tooling.** 3Mice built and open-sourced the **Nudge** runner (github.com/outercloudstudio/nudge) and gated merges on a 95% confidence interval of win rate. The champion used its own `autoscrims/` (ladder scrims) and `ABtest.py`.
- **About the champion.** No caterpillow or teammate write-up was found. The [@caterpillow YouTube RSS](https://www.youtube.com/feeds/videos.xml?channel_id=UCgJFj-dRnKCRTOQLIY-tdgA) has no Battlecode videos (latest upload 2022). [Final tournament video](https://youtu.be/HVhT7MgSyUE) chapters: winners' octo-finals at ~0:21, winners' semis ~1:28, grand finals ~2:23. No captions are available. GST: "Congrats to subscribe to caterpillow for their well deserved win".

## 5. Public bots of other teams
See [opponents.md](opponents.md). The most important one is **Old But Gold = github.com/IvanGeffner/BC26** (pushed 2026-02-03, folder `basic51`; their Sprint 2 package was `basic41`). GST's tournament package `Version40` is in github.com/uravt/Battlecode26.

## 6. Attack angles, ranked

| # | Angle | Evidence | Concrete experiment |
|---|---|---|---|
| 1 | **Exploit the bot's weak maps.** Add these maps to every A/B eval with weight, because the bot loses them to multiple styles. | §3.1 estimates; tournament losses on tminusten (×3), sieveofriches (×3), pocketsofcheese (×2), learn_your_lesson, attack_on_cat, mincemeat | Run challenger vs awubot on whatsthecatdoin, corridorofdoomanddespair, safelycontained, jail, keepout, dirtfulcat, TheHeist and cheesebottles, plus the extracted quals/final maps (§7). Watch why awubot loses. |
| 2 | **Play the cooperation game deliberately.** If contact is late or impossible, maximise cat damage (weight 0.5) and cheese transferred. Avoid giving a backstab when ahead on cooperation points. Kill cats early when leading. | Final losses on t_minus_five_minutes and groveisbeingdesecrated were both cooperation endings. Spec Eq. 1. awubot only checks `isCooperation()` for cat traps, and that cat-trap code was "(untested)" (`a80b25b`). | Implement a points estimator (cooperation vs backstab) and a cat-focus mode. Test on t_minus_five_minutes, groveisbeingdesecrated, fullofsecrets, and maps with walled-off halves. |
| 3 | **Out-scale its economy on cheese-rich and large maps.** | sieveofriches cheese 30k vs 71-91k. Spawning sits at a threshold of 40 (about 12-16 rats) until cheese ≥ `2000-t/2` or `3000-0.75t` (`KingSpawningStrategy.java`). Rich spawning is "(untested)". food/SPAARK/Goobie fielded 40-100 rats. | Rate-based spawning (cf. the unfinished upstream `CheeseTracker`), earlier second and third kings, kings on mines. Measure cheese transferred at r2000 on sieveofriches, isnotgold, neitheroptionisgood, where_are_you, FreeTheCheese. |
| 4 | **Out-micro it the way OBG does.** | OBG is ~50% post-season and 24-15 in ranked matches in the final week; OBG won Sprint 2 4-1, 2 of the 4 lost games being king kills | Use OBG's public bot as a sparring partner (opponents.md). Study its combat decisions and port the ideas. |
| 5 | **Pressure the king early on specific maps.** | Lost by ≈r104 on tminusten to two different teams; asabovesobelow r159; attack_on_cat r452 and mincemeat r442 king kills in the finals. *Speculation*: the opponents' cat damage on tminusten (1580-3216 vs 0) hints the cat played a role. | Replay awubot vs awubot and vs opponent bots on these maps (extracted, §7) and look at what kills its king. |
| 6 | **Race to form the second king.** | food: "won simply by who formed a rat king first". awubot's formation is threshold-gated (round-1900 fallback `0ba63d2`). *Speculation*: it may be slow to form on maps with a trapped spawn. | Log the first-new-king round for both sides across maps. |
| 7 | **Hit the late, untested code paths.** | Final-day "(untested)" commits. Upstream `ally-throwing` `c352477` fixes a tryThrow score >100 that breaks the ratnap checker. `stable` fixes border bugnav and throw targeting. Anti-glorp makes rats with `id%10==7` hold position while an unreachable enemy can see them. | Create situations with unreachable enemies across walls or dirt, and carried/throw situations. Check whether awubot rats idle or misthrow. |
| 8 | **Abuse its squeaking.** *Speculation.* | awubot squeaks map info (MapTracker) and "panic" no-op squeaks near its king to pull cats (`25af0f0`). Cats hunt squeak sources. | Measure cat-caused deaths on both sides. Consider luring cats toward its squeakers or its king. |

Avoid overfitting to the mirror match. Validate gains against the snapshots `awubot_sus`/`awubot_us`/`awubot_quals` and the public opponent bots as well (see [testing.md](testing.md)).

## 7. Tournament maps recovered from replays
47 official tournament maps are **not in the public pool** (`maps/` or [battlecode/battlecode26](https://github.com/battlecode/battlecode26), which has the final-tournament maps since commit 103abf6b): all intl-quals maps (tminusten, sieveofriches, pocketsofcheese, isnotgold, gelcatinouscube, asabovesobelow, kingofthehill, moonlitriverpark, neitheroptionisgood, insertmeowname, hellofromtheotherside, holeinthewall, dangerouscrossing, directconfrontation, doyoudareenter, clockwork, cheeseysquare, thehallway, pipes_the_second), all US-quals/HS/novice maps (squaresaremodernart, winter, warhammer, mushroom, stuckinthemiddle, worldshardestgame, …), and the final-tournament map AceOfSpades.

Every replay embeds its `GameMap`. Extracting from 43 replays gave **114 tournament maps, all of which pass `LiveMap.assertIsValid()` on engine 1.2.5**. Spot checks against known maps show identical walls, dirt, cheese, mines, seed and symmetry.

Recipe, compiled against the gradle-cached engine jar `~/.gradle/caches/modules-2/files-2.1/org.battlecode/battlecode26-java/1.2.5/*/battlecode26-java-1.2.5.jar`:
```java
import battlecode.schema.*; import battlecode.world.*; import java.io.*; import java.nio.*; import java.util.zip.GZIPInputStream;
public class MapExtract { // java -cp $JAR:. MapExtract <outDir> replay.bc26...
  public static void main(String[] a) throws Exception {
    File out = new File(a[0]); out.mkdirs();
    for (int k = 1; k < a.length; k++) {
      byte[] raw; try (InputStream in = new GZIPInputStream(new FileInputStream(a[k]))) { raw = in.readAllBytes(); }
      GameWrapper gw = GameWrapper.getRootAsGameWrapper(ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN));
      for (int i = 0; i < gw.eventsLength(); i++) {
        EventWrapper ew = gw.events(i);
        if (ew.eType() != Event.MatchHeader) continue;
        LiveMap lm = GameMapIO.Serial.deserialize(((MatchHeader) ew.e(new MatchHeader())).map(), false);
        if (!new File(out, lm.getMapName() + ".map26").exists()) GameMapIO.writeMap(lm, out);
      } } } }
```
Per-game results use the same loop. On `Event.MatchFooter`, read `winner()`, `WinType.name(winType())` and `totalRounds()`. On `Event.Round`, read `teamCheeseTransferred(i)`, `teamCatDamage(i)` and `teamAliveBabyRats(i)`, indexed by `teamIds(i)`. The `GameHeader` gives `teams(k).name()`/`packageName()`. The `teamAliveRatKings` field returns nonsense in tournament replays, so don't trust it. To get replay URLs, take any tournament match from the endpoint in §1. This also works for local replays from `./gradlew run`.
