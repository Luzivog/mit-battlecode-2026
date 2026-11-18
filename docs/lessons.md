# Cross-year lessons from Battlecode postmortems (2019–2026), ranked for beating `awubot`

Sources: the 38 archived write-ups in [`references/postmortems/`](../references/postmortems/INDEX.md), cited by short key (key → file table in §10). Rules facts come from [rules.md](rules.md). Champion facts come from [architecture.md](architecture.md), [combat.md](combat.md), [strategy.md](strategy.md), [intel.md](intel.md), [history.md](history.md) and [testing.md](testing.md).

**Applicability:** **A** = directly actionable for the challenger-vs-awubot bet, **B** = useful context or a second-order gain, **C** = background.

**Champion column:** does `awubot`/`challenger` already do this? **YES** / **PARTIAL** / **NO** with a doc pointer, or **UNVERIFIED** when no doc settles it.

The goal is unusual. Our "opponent" is one fixed, known bot, and the maps are mostly known (43 official maps plus the tournament maps recovered in intel.md §7). Lessons about overfitting to one opponent therefore shift. Exploiting awubot-specific weaknesses is legitimate. Overfitting to **maps and seeds** is still the danger.

---

## 0. Top 10 for this project

| # | Lesson | Why here | Cite | Champion |
|---|---|---|---|---|
| 1 | **Accept a change only on a paired, both-sides, many-seed head-to-head against the current best, with ≥ 200 games.** Treat 41-37 as a tie and test one change at a time. | Local noise made bad changes look good for 3Mice and UnderTheC. The engine is deterministic per seed but chaotic across seeds (architecture.md §7). | [3mice26] [utc26] [tvb26] [xsq] | ABtest.py already does paired sides plus a sign test (testing.md §7) |
| 2 | **No untested changes. Fix the champion's own final-day bugs first, each A/B'd.** | awubot shipped "(untested)" final-day commits (history.md). Asserts kill every king at ≥ 26 known mines (architecture.md §1.6). SPAARK lost 2023 and 2024 to last-minute changes. | [spaark25] [thg20] [kragle25] [ivy21] | NO: shipped untested code |
| 3 | **Find hypotheses by tracing replays for degenerate stalls, not by theorising.** Watch individual units. | The 2025 #1/#2 and 2026 #2 teams all credit replay review over cleverness. Trace-first produced the accepted changes in the 2026 agent loop. | [GST26] [confused25] [jwu25] [tvb26] [food26] | n/a |
| 4 | **The economy snowballs: cut idle time and raise production when rich.** | awubot caps contested play at 16 rats while sitting on 700–1850 cheese (strategy.md §10 H1), and loses cheese-rich maps on transfer (intel.md §0 item 4). | [kragle25] [confused25] [chop21] [jwu25] | PARTIAL: cap 16; 20% exploration never ends |
| 5 | **Micro decides fights, and 2026 was a micro year. Improve the search where it is blind:** cat-aware throws, all 8 throw directions, cheese-scaled bites. | A micro gain is worth +30–50% win rate vs ~5% for macro (Gone Fishin'). GST scores throws into cats. awubot ignores cats in TryThrow (combat.md §14 H1/H3/H4). | [gf23] [GST26] [food26] [3mice26] | PARTIAL: strong search, known gaps |
| 6 | **Bytecode is the binding constraint, so reclaim it before adding features.** Measure every component with `Clock.getBytecodeNum()` deltas. | Rats average ~65% of 17.5k with a long tail, and adaptive budgets mean new code displaces old (architecture.md §8). King perception costs ~3.4k/turn. | [xsq] [lmax26] [omnom25] [dam23] | YES: heavily optimised, little headroom |
| 7 | **Use symmetry offensively:** derive the enemy king spawn and pressure it when ahead. | Symmetry is "Battlecode 101". awubot computes it but never uses it to find the enemy king (`guessEnemyLocs` unused), even though 14/16 games end by king kill (strategy.md §10 H5). | [kragle25] [gf23] [omnom25] [food26] | PARTIAL |
| 8 | **Share combat information.** In 2026 the 90° cone hides rear ratnappers. GST had every fighting rat squeak its HP and nearest enemy each turn, accepting bytecode overruns. | awubot squeaks ≤ 1–2 enemies, only with > 1000 bytecodes left, and receivers read ≤ 5 messages (architecture.md §4.2). | [GST26] [3mice26] | PARTIAL |
| 9 | **Dig aggressively.** Treat dirt as passable with a cost: it opens lanes and raises cheese throughput. | awubot allows dirt only for round-≤2 spawns or when rich (architecture.md §6). Its `testing` branch tried "always allow dirt" (history.md). | [GST26] [3mice26] | PARTIAL |
| 10 | **Maps decide matches. Hold out maps, stress edge cases, and never hard-code map names or sizes.** | 4 Musketeers lost quals as top seed to one edge-case map, and confused 2020's pathing broke on every anti-rush tournament map. awubot has large repeatable per-map losses (intel.md §3.1). UnderTheC banned map hardcoding. | [bd21] [4m23] [conf20] [chop21] [utc26] | PARTIAL: area ≤ 900 rule |

---

## T. Testing and evaluation (input for the harness design)

### T.1 What top teams actually did to decide "this is better"

| Team (year, place) | Opponents | Maps / volume | Decision rule | Cite |
|---|---|---|---|---|
| Just Woke Up (2025, 1st) | Previous versions (57 in 25 days), then scripted unranked scrims vs ~top 20 | Every map available, via camel_case's A/B script | Per-map win/loss/tie, then **watch the lost replays** (and surprising wins). A loss to an old bot is a judgement call: "worse vs your old one doesn't mean worse vs other teams" | [jwu25] |
| confused (2025, 2nd) | Ladder scrims vs top teams | n/a | Replay analysis gave a two-principle "game philosophy". Little automation | [confused25] |
| SPAARK (2025, HS 1st) | Old bots via `runmatches.py` (inherited from Producing Perfection 2022 → 4 Musketeers 2023) | Overnight runs | "Scrimmage analysis is still more important than raw win rate against old bots." The script caught a bug 5 minutes before a deadline | [spaark25] |
| 3MiceWalkIntoABar (2026, finalist) | A benchmark bot plus recent commits | Distributed runner "Nudge", ~1.2 games/s, **300–500 games per test** (5–15 min) | **Merge only via PR, when the 95% CI on win rate shows an improvement.** Bisecting recent commits by win rate found several regressions: <50% → ~70% vs benchmark | [3mice26] |
| Lorem Ipsum (2026, 15th) | Old bots, then ladder scrims | 1–3 targeted maps → ~7 custom + sprint maps | Accept if "slightly above 50%"; investigate if ~30% | [lorem26] |
| GST (2026, 2nd) | Higher-ranked ladder teams (watched Old But Gold heavily) | n/a | Copy macro that "made us better". Follow individual units in replays to improve micro | [GST26] |
| food (2026, top 12) | Old bots, scrims | Was running 20–30 maps at a time and regrets it | Prefer 3–5 behaviour-specific maps plus replays while iterating, then a broad check. Beware overfitting to a fixed baseline | [food26] |
| 5 Musketeers (2022) | Several recent bots, not one | Every map | A spreadsheet of every change vs every recent bot on every map, from day 1 | [5m22] |
| 4 Musketeers (2023, 3rd) | All previous versions | > 30 maps took hours locally, so they ran matches on GitHub Actions | Match summary vs a chosen old version | [4m23] |
| don't @ me (2023, 7th) | Old versions (all maps) + 10-game online full-set scrims | ~30 maps | Both together, to track progress and the meta | [dam23] |
| XSquare (guide; 2020 winner) | Old versions | "Largest possible map set", **both sides** | Near 50% and expected good → keep, after checking for bugs. Add one upgrade at a time. Build maps that isolate the feature | [xsq] |
| Big Red (2019) | **Re-implemented competitor bots** as local benchmarks | n/a | Scrims are rate-limited; reproductions are always available | [brb19] |
| Malott Fat Cats (2021, 4th) | Old bot | n/a | Build-order tweaks alone could not move win rate vs the old bot, so they changed the mechanism (weighted build order) | [mfc21] |
| terryvanbelle (2026, AI-agent loop) | Mirror vs last accepted snapshot; peers; old bots; 5 public tournament bots | 54–162 games per run | Instruments ranked by resolution (T.4). Many process rules. **The bot stayed far weaker than tournament bots**, so read it for pitfalls, not proof | [tvb26] |
| UnderTheC (UNSW 2026, AI-agent loop) | Parent + top-3 of own pool; generated-map families; official judge | 78-game drafts ≈ ±9 wins; 291k arena games in total | One change per child vs its parent, then a server per-map rating-adjusted check | [utc26] |

### T.2 Map pool

- **Use the widest pool, both sides** [xsq] [dam23] [jwu25]. Ours: 43 official maps (testing.md §5).
- **Add behaviour-specific maps** (micro: small/open; pathing: mazes; economy: cheese-rich) for the inner loop, because a full-pool win rate dilutes a local change [xsq] [food26] [lorem26].
- **Keep a holdout the agent never tunes on.** Tournament maps differ from ladder maps and cause upsets [chop21] [conf20] [4m23] [bd21]. UnderTheC kept 100 hidden maps for champion picking only, and generated map families exposed a centre rush that only won on known maps [utc26]. Ours: the 29 team-made maps and the recovered tournament maps (intel.md §7) can serve as holdout.
- **Stress maps for robustness**: degenerate geometry (4 Musketeers lost quals as top seed to 3 HQs in a corner [4m23]), many mines (awubot loses every king at ≥ 26, architecture.md §1.6), many cats, heavy dirt.

### T.3 Sample sizes and statistics

| n games (independent) | 95% CI half-width on a ~50% win rate |
|---|---|
| 50 | ±14% |
| 100 | ±10% |
| 200 | ±7% |
| 400 | ±5% |
| 1000 | ±3% |

- UnderTheC: "78 games is about ±9 wins: 41-37 told us nothing". A line climbed by three promotions that were all inside noise [utc26].
- 3Mice needed 300–500 games per decision [3mice26]. Most human teams used far fewer and leaned on replays.
- **The engine is deterministic per (code, map, seed)** (testing.md §4). Re-running identical inputs gives zero information [tvb26]. Variance must come from seeds × maps × sides. Any bytecode change reshuffles RNG-dependent outcomes (architecture.md §7), so small unrelated edits flip long, fragile games [tvb22].
- **Pair by (map, seed) and play both sides.** Side and turn order bias results (XSquare footnote: RNG seeds, fixed direction loops, turn order [xsq]). Fixed tie-break iteration order encoded side bias across ~60 iterations [tvb22]. ABtest's sign test on pairs already removes the side effect (testing.md §7).
- **Track win type, not just W/L.** A king kill, a points win and a coin-flip tiebreak are different strengths [tvb26]. Normalise replay counters per round, since surviving longer inflates deaths [tvb26].
- **A win-probability curve beats W/L for variance and for locating the loss.** UnderTheC's logistic eval on per-round metrics predicted winners at 70–96%, and showed 52% of their losses were decided in the opening [utc26]. A BC26 analogue: per-round king HP, rats alive, cheese transferred, cat damage share.

### T.4 Instruments: resolution vs representativeness [tvb26]

| Instrument | Typical WR | Use |
|---|---|---|
| Head-to-head vs the current best (for us: **challenger vs awubot**) | ~50% by construction | **Primary accept test.** A 4-game swing is signal |
| Older own versions | ~85–90% | Direction only; ±2 games is the noise floor. Use as a **pre-accept gate when the margin is thin** (it once reversed a +1/+1 verdict by 10 games) |
| Much stronger external bots | ~3% | Direction only, but the only instrument that sees threats your lineage never poses |
| Peers sharing your code | ~50% always | Regression check only; goes stale |

- **"The mirror can prove a feature is not paying for itself. It cannot prove a feature is unnecessary."** An ablation that won on the mirror raised early king wipes by bench bots from 16% to 26% [tvb26]. For defensive features, keep external bots in the gauntlet: GST `uravt/Battlecode26`, Old But Gold `IvanGeffner/BC26`, SPAARK, food, Lorem Ipsum (intel.md §5, INDEX.md "Not archived").
- **Do not tune against benchmark source.** Run their bots and read replays only, to keep the benchmark independent [tvb26].
- A pool made only of your own relatives "rewards beating our own habits". Add non-family bots early [utc26].
- **Fix the measurement before trusting it.** UnderTheC's simulator diverged from the official judge (an FMA float-contraction difference), and for days the league judged "a slightly different game" [utc26]. For us: local engine 1.2.5 vs official 1.2.6/1.2.7 differ in cheese spawn (rules.md §0). Validate on `--engine official` before final acceptance.

### T.5 Diagnostics that cost no extra games [tvb26]

1. **Arm identity:** if every (opponent, map, side) result matches the control, the change never executed. Three "flat dose-response" experiments were byte-identical bots.
2. **Diff runs game-by-game** keyed by (map, seed, side). Scattered mixed flips are noise. One-directional flips clustered on one map are a real effect: reproduce and trace them.
3. **Dose-response:** vary the parameter instead of re-running. Always include the zero arm. An interior peak is stronger evidence than any single point.
4. **Ablate accepted features periodically.** Of 6 features, two "failure-mode preventers" carried ~+24–28 points each, one was ~0, and one was negative.
5. **Don't pick replays from the loss list to "find the deficit"**, which is circular. Look for absolute stalls over time (e.g. rich but not building) [tvb26].
6. **Bytecode overrun monitor** from day one: compare the round number before and after a turn [tvb26]. architecture.md §8 has the measured tail.

### T.6 Pitfalls teams hit

| Pitfall | Cite |
|---|---|
| Accepting on a lopsided instrument (old bots, strong bench) while the even one dissents | [tvb26] |
| Bundling several changes, then not knowing which helped. UnderTheC reversed per-round merges | [xsq] [utc26] |
| Discarding a near-50% idea without checking for bugs: ideas often get worse before better | [xsq] [food26] |
| Goodhart: optimising the metric named in the brief (e.g. fewer self-deaths) shrank the army and lost | [utc26] |
| Overfitting to a fixed baseline or a small map set | [food26] [xsq] |
| Local runs too slow, so people test less. awubot's huge generated classes and StringBuilder use slow every spawn (testing.md §1); 3Mice called StringBuilders "extremely slow" in wall time | [3mice26] [4m23] |
| Automated parameter tuning (GRASP, a vibe-coded tuner) did not improve rank for two 2026 teams | [lmax26] [lorem26] |

### T.7 Recommendations for our harness (derived from the above)

1. **Inner loop:** challenger vs awubot on a targeted map subset for the behaviour, both sides, ~50–100 games; plus replay review of flipped pairs.
2. **Accept gate:** the full official pool × ≥ 3 seeds × both sides (≥ 258 games); sign test on pairs; report the win-type mix; reject if any map flips one-directionally without an explanation.
3. **Holdout gate** before merging a series: the team-made maps plus the recovered tournament maps, under `--engine official`.
4. **Side gauntlet** (direction only): GST, Old But Gold, SPAARK and older awubot snapshots (`awubot_sus`, `awubot_quals`, ...), so that we do not regress against styles awubot never plays.
5. **Log per game:** win type, round, and per-100-round cheese transferred, rats alive, king HP and cat damage (for stall tracing and a P(win) curve later).
6. **One change per candidate; keep a "closed directions" log with the measured reason** [tvb26].

---

## 2. Bytecode optimisation

| Lesson | Applic. | Why it matters here | Cite | Champion |
|---|---|---|---|---|
| Measure, don't guess: bracket code with `Clock.getBytecodeNum()`; keep a micro-benchmark table | A | Budget is tight; changes are adaptive (§1.5 gates) | [xsq] [lmax26] | YES (architecture.md §8 tables) |
| `static` fields over instance fields; no objects in hot paths. Om Nom: the `this` load roughly doubled micro cost | A | Every robot is a separate JVM, and all state is static anyway | [xsq] [omnom25] | YES (architecture.md §3) |
| `switch` instead of if-chains or loops (10-iteration loop 75 bytecodes vs 23 for a switch); unroll fixed loops | A | Unrolling is how the micro fits in 10–14k | [xsq] [lmax26] [omnom25] | YES (codegen.md) |
| Reverse loops `for (int i = n; --i >= 0;)` (64 vs 75 per 10 iterations); compare to 0 | B | Hand-written loops remain | [wololo21] [xsq] [lmax26] | UNVERIFIED (per-file) |
| Avoid `java.util`; use custom sets or heaps (FastIterableIntSet, linear-probing hash set) | A | `java.util` is charged as user code (rules.md §14) | [3m21] [dam23] [bd21] | YES (`util/string/*`) |
| String/StringBuilder/`toCharArray` ops are single API calls: O(1)-bytecode containers (`"\u0000…".toCharArray()` = 2 bytecodes) | A (cost: wall time) | Cheap in bytecode but slow on the wall clock, which hurts harness throughput | [xsq] [lmax26] [3mice26] | YES (heavily) |
| Bitmask BFS and bitboards (Om Nom pathing ~1.5k, symmetry ~1k; GST reachability ~300) | A | Already the pathing core; reuse it for new reachability questions (e.g. combat through walls) | [omnom25] [GST26] | YES (11×11 two-long BFS) |
| Codegen (Jinja/Python) for unrolled code; macros instead of function calls | A | All the hot code is templates; edit the templates, not the generated `.java` | [xsq] [omnom25] [lmax26] | YES (codegen.md) |
| Don't bytecode-optimise early, but late-season complexity will need it | C (we are late-season) | The bot is mature: optimisation frees room for features | [xsq] [dam23] | n/a |
| A single top-level try/catch; exceptions cost 500 bytecodes; a `isCompetitive` flag for debug-only paths | B | awubot swallows Exceptions; **`AssertionError` escapes and the engine destroys the robot** | [lmax26] | PARTIAL (architecture.md §1.6) |
| "Codegen spam" is not what wins; strategy and replay review are | B | Guards against agents sinking time into micro-optimisation | [GST26] [jwu25] [confused25] [lorem26] | n/a |

## 3. Pathfinding

| Lesson | Applic. | Why it matters here | Cite | Champion |
|---|---|---|---|---|
| **Bug-nav waypoint plus local BFS toward it** ("Optimal Bug", BugBFS, Bellman-Ford+bug) is the converged design: guaranteed progress and robust to clogging | B (already done) | Don't rewrite it. The Kragle regretted rewriting pathing they couldn't improve | [omnom25] [GST26] [confused25] [kragle25] | YES (architecture.md §6) |
| Simulate both wall-follow directions and pick the shorter; cap the simulation by bytecode | B | — | [gf23] [GST26] | YES (left/right child bugs) |
| Units are temporary obstacles: BFS around them, or remember them for ~5 ticks; randomise if stuck | B | Congestion near kings and mines | [omnom25] [3mice26] [gf23] | YES (BFS mask includes allies and king footprints) |
| Don't follow map edges; turn away from borders | A | The upstream `stable` branch has an unmerged border-bugnav fix (history.md) | [GST26] [gf23] | PARTIAL (fix exists upstream, not in final) |
| **Dirt as passable with a dig cost; aggressive digging is usually right** | A | Economy plus mobility. awubot gates dirt on cheese and round | [GST26] [3mice26] | PARTIAL (architecture.md §6, dirt policy) |
| King (3×3) pathing = 1×1 pathing on a Minkowski-expanded obstacle map | B | Mobile kings mine (strategy.md §6) | [3mice26] | YES (`isKingPassable`) |
| Target switching: switch only if the new distance < the historical minimum to the old target, or bug loops forever | B | `TraverseStrategy` re-targets on `givenUp` | [xsq] | UNVERIFIED |
| Don't assume pathing is unimportant early; greedy pathing cost a finalist a week | C | — | [chop21] | n/a |

## 4. Communication encoding

| Lesson | Applic. | Why it matters here | Cite | Champion |
|---|---|---|---|---|
| Bit-pack every message with a type header; one decoder table | B | Free header 3 / slot 15 exist (architecture.md §9) | [GST26] [dam23] | YES |
| **Combat info sharing beats bytecode thrift in 2026** (cone vision, rear ratnaps): every fighter squeaks HP and nearest enemy each turn | A | awubot drops squeaks under 1000 bytecodes left and reads ≤ 5 per turn | [GST26] [3mice26] | PARTIAL (architecture.md §4.2) |
| A local database, uploaded when in range of a writer (babies → king → array) | B | Only kings write the array | [dam23] [GST26] | YES (map diffs squeaked to the king) |
| Double-buffer transient commands by round parity so late-turn robots don't miss them | B | "Last king in turn order wins" on shared slots | [dam23] | NO (UNVERIFIED need) |
| Use spare channels for roles: the king → newborn payload (13 bits, unused) | B | Enables role specialisation (scouts, cat hunters, guards) without new comms | [GST26] (behaviour singletons) [kragle25] (goal queue) | NO (payload discarded, architecture.md §4.1) |
| Unit-test encoders and decoders; comm bugs are silent | A | awubot's panic squeak decodes as 2 phantom enemies; the king packs enemies at the wrong shift | [kragle25] ("we should write unit tests") | NO (strategy.md §10 item 9) |
| Don't over-engineer comms when the game limits them; spend effort on fundamentals | C | 2025 #2 barely used comms | [confused25] | n/a |

## 5. Micro frameworks (scoring and simulation)

| Lesson | Applic. | Why it matters here | Cite | Champion |
|---|---|---|---|---|
| XSquare micro: score all 9 moves on features, compare with an `isBetterThan` tie-break chain; attack is chosen separately | B | The baseline every 2024–2026 team used | [mfc21] [kragle25] [cout24] [GST26] [food26] | YES and beyond: (move, facing, act-before/after) search (combat.md §0) |
| **Model the opponent's best reply (one-ply minimax).** In 2026: any enemy standing cardinal-adjacent can ratnap you next turn regardless of facing | A | Ratnapping decides fights | [wololo21] [food26] | YES (CombatTracker grab-threat for every (move, facing), combat.md §4) |
| **Throws: enumerate move + turn + throw and score the endpoint** (cat > enemy > wall > ally); hold a grabbed enemy when no good throw exists yet | A | Feeding a rat to a cat kills it and sleeps the cat (rules.md §9). awubot has no cat term, checks only ±45°, and its carrying has no goal | [GST26] [3mice26] | PARTIAL (combat.md §14 H1–H3) |
| Balance passive vs aggressive: passive beats aggressive, two passives stalemate; go aggressive with a 2:1 numbers edge; invert proximity penalties during a king SOS | A | Standoffs cost map control and economy | [food26] | UNVERIFIED (see `shouldFight`, combat.md §3.3) |
| Defensive facing at end of turn (no blind spot toward enemies); move/turn order avoids the strafe cost | B | Glance uses spare turn cooldown | [food26] [3mice26] | PARTIAL (`executeGlance`) |
| Cat micro: avoid cat vision and the 2–3 tiles in front (its path), but prioritise enemies over cats | B | Cats are deterministic (rules.md §12) | [GST26] [food26] | YES (cone penalties, `autoMove` refuses the cone) |
| An "exhaust" timer to leave fights that cannot progress (enemy behind walls) | B | Rats stuck "fighting" through walls lose economy | [3mice26] [GST26] | UNVERIFIED |
| Micro gains dwarf macro gains in micro-heavy games (+30–50% vs +5%); copying a better micro is legitimate | A | Old But Gold beat awubot 15-24 in ranked (intel.md §0) | [gf23] [GST26] [cout24] | n/a |
| A sprawling if-chain micro becomes unmaintainable: keep scores in one place, constants named | B | combat.md §10 lists every constant | [cout24] | YES (named constants) |

## 6. Macro / economy

| Lesson | Applic. | Why it matters here | Cite | Champion |
|---|---|---|---|---|
| **Economy snowballs; optimise the opening and cut idle time.** The biggest jump for a top-16 team came from removing floating resources and idle units | A | awubot floats cheese at a 16-rat cap; go-home thresholds reach 105 raw cheese (×2 slowdown) | [kragle25] [confused25] [jwu25] | PARTIAL (strategy.md §10 H1, H6) |
| **Copy macro from stronger teams** (spawn counts, king formation on mines, return thresholds). It is easier to reverse-engineer than micro | A | Public bots: GST, Old But Gold | [GST26] [thg20] [chop21] [xsq] | n/a |
| More production centres (kings on mines) multiply income; GST formed kings on cheese mines | A | awubot's 2nd king failed 0/11 on cheesefarm and peaceinourtime (needs ≥ 20 rats vs a cap of 16) | [GST26] | PARTIAL (strategy.md §10 H2) |
| Prefer simple independent per-robot rules over global coordination | B | Coordination breaks under limited vision and comms | [xsq] [jbw20] [food26] | YES (fixed strategy chain) |
| Derive heuristics from one principle (e.g. "stay near a king") and test each | B | food's cheese-return logic | [food26] | n/a |
| Know the scoring path: 2026 games mostly end by backstab or king kill, but cooperation endings exist and decide matches | A | 2 of awubot's 5 final-tournament game losses were cooperation endings on points (intel.md §0) | [3mice26] (99% backstab) [GST26] | NO (no coop policy) |
| A "taxi" (rats carrying heavy-cheese rats home) was a small gain | C | Carrying is free (rules.md §16 item 8) | [GST26] | NO |

## 7. Map analysis and symmetry

| Lesson | Applic. | Why it matters here | Cite | Champion |
|---|---|---|---|---|
| Keep a per-tile map store (char or bitmask rows) and eliminate symmetry candidates from observed tiles | B | — | [gf23] [omnom25] [kragle25] [dam23] | YES (architecture.md §5.1) |
| **Never assume one symmetry** (Gone Fishin' assumed rotational and it backfired in Sprint 1); on 1.2.5, H/V maps aren't cheese-symmetric | B | 22/43 maps are H/V (testing.md §3) | [gf23] | YES |
| **Use the known symmetry to target the enemy spawn**: rush the 3 candidates, narrow as symmetries are eliminated | A | Enemy king pressure is unused | [food26] [kragle25] | NO (strategy.md §10 H5) |
| Mirror known features (mines, walls) to the unseen half for free exploration targets | B | — | [dam23] [kragle25] | YES (`apply*` mirrors) |
| Maps win games: finals maps differ, edge-case maps eliminate top seeds; build for robustness | A | Per-map weaknesses (intel.md §3.1) | [bd21] [chop21] [4m23] [conf20] [3m21] | PARTIAL |
| Map-size-dependent parameters are fine; exact-map tables are overfitting | B | — | [lmax26] [utc26] | PARTIAL (area ≤ 900 rule) |

## 8. Development process and pitfalls teams regretted

| Lesson | Applic. | Cite |
|---|---|---|
| **No last-minute untested changes.** SPAARK lost 2023 and 2024 that way; Ivy placed last in quals with a critical bug; awubot's final day was "(untested)" | A | [spaark25] [ivy21] [kragle25] [thg20] |
| **Before a deadline, make 1–2 well-built changes, not many small buggy ones** (High Ground: too cautious in 2019, too scattered in 2020) | A | [thg20] |
| Prioritise by expected win-rate gain; keep a ranked task list; even finalists ship with obvious bugs | A | [thg20] [kragle25] |
| One upgrade at a time; revert by subsets is expensive | A | [xsq] [utc26] |
| Don't throw away near-50% ideas; check for bugs first; give an idea 2–3 tries if its target moves | A | [xsq] [utc26] [food26] |
| Read the spec carefully before coding a strategy (for us: rules.md is engine truth; the spec is wrong in places) | A | [chop21] |
| Don't rewrite mature subsystems you can't beat (pathfinding); don't polish behaviours that never decide games | A | [kragle25] |
| Copy, a lot, but understand what you copy (blind bugnav → infinite target-switch loops) | B | [xsq] [jwu25] [confused25] [thg20] |
| Modular code: state → behaviour, robust primitives, so late changes are cheap | B | [xsq] [GST26] [jwu25] [brb19] [nthe23] |
| Rewrite rather than retrofit **only** if the core strategy changes. Not our case: build on the champion (UnderTheC's from-scratch bot lost the parent line's behaviours) | B | [jbw20] [utc26] |
| Hard-coded geometric assumptions break on new maps (a fixed building offset; assuming rotational symmetry) | A | [bg20] [gf23] |
| Indicators (dots, lines, strings) and debug-only flags make replay review fast | B | [thg20] [xsq] [lmax26] |
| Burnout and lost weeks hurt more than any single feature (GST and 3Mice lost a week; 3Mice dropped from top 10 to ~60) | C | [GST26] [3mice26] [jwu25] |

## 9. AI-agent improvement loops (directly relevant to the bet)

Two published post-season or parallel efforts ran LLM agents in a loop. One was on this exact game (terryvanbelle, weak result). The other was on a different 2026 game (UnderTheC, top-16 ladder peak).

| Lesson | Cite |
|---|---|
| **Separate roles.** Read-only analysts diagnose losses from replays. Strategists propose single ranked changes with evidence and a success test. Fresh workers implement **one** change each in their own worktree. The orchestrator verifies claims and never designs strategy | [utc26] |
| Short, fresh workers beat long-running agent teams, whose contexts drifted and produced ~15 verdicts inside noise | [utc26] |
| Build on the strongest line and add one behaviour; from-scratch agent bots lost accumulated behaviours | [utc26] |
| Don't steer analysts with the orchestrator's conclusions; they "dutifully confirmed" them | [utc26] |
| No map-name or exact-size hardcoding; every new threshold must name the observed cause | [utc26] |
| Verify agent-reported results; weaker models shipped partial briefs, misreported results or no-ops | [utc26] |
| Keep a "closed directions" log with the measured reason; re-open only with new evidence | [tvb26] |
| When idea generation stalls, ablate existing features: it found more real gains than invention | [tvb26] |
| "Never idle": track functional areas and leave an area after 3 consecutive rejects | [tvb26] |
| Rating gains were roughly linear in tokens spent, and stalled when agent count dropped | [utc26] |
| An agent loop with rigorous stats but a weak base bot stayed weak (7 kills for 347 losses vs tournament bots). **Process rigour does not replace a strong base**: our base is the champion | [tvb26] |

---

## 10. Source keys

| Key | File |
|---|---|
| GST26 | [`references/postmortems/2026/2026-generalized-strokes-theorem.md`][GST26] |
| food26 | [`references/postmortems/2026/2026-food.md`][food26] |
| 3mice26 | [`references/postmortems/2026/2026-3micewalkintoabar.md`][3mice26] |
| lorem26 | [`references/postmortems/2026/2026-lorem-ipsum.md`][lorem26] |
| lmax26 | [`references/postmortems/2026/2026-louis-max.md`][lmax26] |
| tvb26 | [`references/postmortems/2026/2026-terryvanbelle-vibe-training-algorithm.md`][tvb26] |
| utc26 | [`references/postmortems/2026/2026-underthec-unsw.md`][utc26] |
| jwu25 | [`references/postmortems/2025/2025-just-woke-up.md`][jwu25] |
| confused25 | [`references/postmortems/2025/2025-confused.md`][confused25] |
| omnom25 | [`references/postmortems/2025/2025-om-nom.md`][omnom25] |
| spaark25 | [`references/postmortems/2025/2025-spaark.md`][spaark25] |
| kragle25 | [`references/postmortems/2025/2025-the-kragle.md`][kragle25] |
| cout24 | [`references/postmortems/2024/2024-cout-for-clout.md`][cout24] |
| xsq | [`references/postmortems/2024/2024-xsquare-guide.md`][xsq] |
| gf23 | [`references/postmortems/2023/2023-gone-fishin.md`][gf23] |
| 4m23 | [`references/postmortems/2023/2023-4-musketeers.md`][4m23] |
| dam23 | [`references/postmortems/2023/2023-dont-at-me.md`][dam23] |
| nthe23 | [`references/postmortems/2023/2023-no-thoughts-head-empty.md`][nthe23] |
| 5m22 | [`references/postmortems/2022/2022-5-musketeers.md`][5m22] |
| tvb22 | [`references/postmortems/2022/2022-terryvanbelle-vibe-learnings.md`][tvb22] |
| bd21 | [`references/postmortems/2021/2021-baby-ducks.md`][bd21] |
| mfc21 | [`references/postmortems/2021/2021-malott-fat-cats.md`][mfc21] |
| wololo21 | [`references/postmortems/2021/2021-wololo.md`][wololo21] |
| chop21 | [`references/postmortems/2021/2021-chop-suey.md`][chop21] |
| 3m21 | [`references/postmortems/2021/2021-three-musketeers.md`][3m21] |
| ivy21 | [`references/postmortems/2021/2021-llllllll-disciple.md`][ivy21] |
| jbw20 | [`references/postmortems/2020/2020-java-best-waifu.md`][jbw20] |
| bg20 | [`references/postmortems/2020/2020-battlegaode.md`][bg20] |
| thg20 | [`references/postmortems/2020/2020-the-high-ground.md`][thg20] |
| conf20 | [`references/postmortems/2020/2020-confused.md`][conf20] |
| brb19 | [`references/postmortems/2019/2019-big-red-battlecode.md`][brb19] |

Not cited above but archived: muskellunge 2024, zhugeduck 2024, Bowl of Chowder 2020, smite 2019, NP-cgw 2019, Oak's Last Disciple 2019, Double J 2019. See the [index](../references/postmortems/INDEX.md).

[GST26]: ../references/postmortems/2026/2026-generalized-strokes-theorem.md
[food26]: ../references/postmortems/2026/2026-food.md
[3mice26]: ../references/postmortems/2026/2026-3micewalkintoabar.md
[lorem26]: ../references/postmortems/2026/2026-lorem-ipsum.md
[lmax26]: ../references/postmortems/2026/2026-louis-max.md
[tvb26]: ../references/postmortems/2026/2026-terryvanbelle-vibe-training-algorithm.md
[utc26]: ../references/postmortems/2026/2026-underthec-unsw.md
[jwu25]: ../references/postmortems/2025/2025-just-woke-up.md
[confused25]: ../references/postmortems/2025/2025-confused.md
[omnom25]: ../references/postmortems/2025/2025-om-nom.md
[spaark25]: ../references/postmortems/2025/2025-spaark.md
[kragle25]: ../references/postmortems/2025/2025-the-kragle.md
[cout24]: ../references/postmortems/2024/2024-cout-for-clout.md
[xsq]: ../references/postmortems/2024/2024-xsquare-guide.md
[gf23]: ../references/postmortems/2023/2023-gone-fishin.md
[4m23]: ../references/postmortems/2023/2023-4-musketeers.md
[dam23]: ../references/postmortems/2023/2023-dont-at-me.md
[nthe23]: ../references/postmortems/2023/2023-no-thoughts-head-empty.md
[5m22]: ../references/postmortems/2022/2022-5-musketeers.md
[tvb22]: ../references/postmortems/2022/2022-terryvanbelle-vibe-learnings.md
[bd21]: ../references/postmortems/2021/2021-baby-ducks.md
[mfc21]: ../references/postmortems/2021/2021-malott-fat-cats.md
[wololo21]: ../references/postmortems/2021/2021-wololo.md
[chop21]: ../references/postmortems/2021/2021-chop-suey.md
[3m21]: ../references/postmortems/2021/2021-three-musketeers.md
[ivy21]: ../references/postmortems/2021/2021-llllllll-disciple.md
[jbw20]: ../references/postmortems/2020/2020-java-best-waifu.md
[bg20]: ../references/postmortems/2020/2020-battlegaode.md
[thg20]: ../references/postmortems/2020/2020-the-high-ground.md
[conf20]: ../references/postmortems/2020/2020-confused.md
[brb19]: ../references/postmortems/2019/2019-big-red-battlecode.md
