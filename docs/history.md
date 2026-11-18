# History & provenance of the champion bot

Audience: AI agents improving `src/challenger` against `src/awubot`. Dense on purpose. All times UTC unless noted.
Competitive intel (results, scrims, attack angles) lives in [intel.md](intel.md).

## 1. Provenance

| Item | Value |
|---|---|
| Upstream | https://github.com/awu7/battlecode-2026, default branch `final` |
| Champion commit | `a70328e` "fix glorp trolling" (2026-01-29 22:12), tagged locally `caterpillow-final` (annotated tag) = `champion/final` |
| Local `main` | `champion/final` + 2 local setup commits: `8e72610` (pin Python deps, parametrize `jinja.py`, default match challenger vs awubot) and `1ff4b32` (`src/challenger` = copy of awubot with package renamed) |
| Team | "subscribe to caterpillow", team id 2067, episode `bc26`. Platform members: `awu` (Cambridge), `caterpillow`, `lmkae`, `horiseun` (UNSW) ([API](https://api.battlecode.org/api/team/bc26/t/2067/)) |
| People | [battlecode.org/past.html](https://battlecode.org/past.html) lists the winners as Naman Doshi, Alexander Wang, Andy Wu, Yiheng You |
| Git authors on `final` | `caterpillow` (Alexander Wang): 159 commits. Combat micro (Jinja codegen), comms/squeaks, combat tracker, throwing, traps, autoscrims. `awu`/`awu7` (Andy Wu): 150. Pathfinding (BFS/bugnav), Cell/Robot trackers, king pathing, spawning/econ, final-day tweaks. `naman-doshi`/`Naman Doshi`: 56. King formation, mining, defense, traps, testing scripts. `Yiheng You`: 7. Early cheese farming/roaming. Which platform user is which of Naman and Yiheng is not known. |
| AI use by authors | Every package has a `CLAUDE.md`. Commit messages include "MASSIVE CLEANUP AND REFACTOR AI SLOP BEGONE" and "logically equivalent ai slop refactor". |
| License | **AGPL-3.0** (`LICENSE`, from upstream's initial commit `d822dae`). Code in this repo is a derivative work. Using it privately has no obligations. If you **distribute** it (a public repo, shared zips, arguably a platform submission), you must license it AGPL-3.0, keep the copyright and license notices, and provide the full source. AGPL §13 adds the same duty for modified versions that users interact with over a network. This is not legal advice. |
| Engine/client | The repo pins engine **1.2.5** / client **1.0.7** (`engine_version.txt`, `client_version.txt`; last bump `53e6d55`, 01-27). Pin history: 1.1.7 (01-18), 1.2.0 (01-21), 1.2.1/1.2.2 (01-23), 1.2.3 (01-24), 1.2.4 (01-25), 1.2.5 (01-27). The official public release was 1.2.6. The tournament servers ran **1.2.7.private2** (`release_version_saturn` in the [episode API](https://api.battlecode.org/api/episode/e/bc26/)). Per the rules agent, that plays like 1.2.6, and the only gameplay change from 1.2.5 is cheese spawning. Java 21. |

## 2. Timeline vs the 2026 calendar

Freeze times come from the [tournament API](https://api.battlecode.org/api/episode/bc26/tournament/), and results from the tournament match API (see [intel.md §2](intel.md)). Submitted package names are read from the official replays.

| Date (2026) | Event | Bot state / submission | Result |
|---|---|---|---|
| 01-05 19:00 EST | Game release | First commit `d822dae` on 01-06 | |
| 01-06 → 01-11 | | awu builds pathfinding first: 11x11 BFS, checkerboard-resilient bugnav with lookahead, Dijkstra through dirt, symmetry checker. caterpillow adds comms (StringBuilder "string tech") and cheese-mine tracking. | Ladder rating 1357 on 01-11 |
| **01-12 00:00** freeze | **Sprint 1** (single elimination) | Package `experiment` (`715ccba`, Naman, 01-11; deleted in `e1414fa`) | **Lost round 1, 1-4 to X7124**: 4 king deaths in rounds 105-125, i.e. rushed. Winner: Super Cow Powers. |
| 01-12 → 01-18 | | caterpillow's combat micro v1 (`48b06a5`), king defense, big refactor (`834a8df`), defensive/adaptive spawning, non-adjacent traps (Jinja), rat throwing, combat BFS, autoscrims tester | 1750 on 01-17 |
| **01-19 02:00** freeze | **Sprint 2** (single elimination) | `awubot_fixratthrowing_old` (`b52c072` "commit old version of fixratthrowing (much better than the new one?)", 01:20) | Won 5-0 Slam Duck, 5-0 BFS Enjoyers, 4-1 SPAARK. **Lost round 5, 1-4 to Old But Gold** (top 16). Winner: glorp. |
| 01-19 → 01-23 | | "998244353" micro rewrite, early exits, combat tracker. King formation written but disabled. The "mystery" bot was pushed as the good one (`81fd3aa`), and awubot was **restarted from it** (`aece3fa`, 01-22). Pathfinding cat avoidance. | 1900 on 01-21 |
| **01-24 02:00** freeze | **International Qualifiers** (double elimination) | `awubot` at `0b0427d` (= branch `submission-quals` = `src/awubot_quals`) | **1st**: lost the winners-bracket final 2-3 to Goobie Woobies, beat Mango Tango 3-2, then beat Goobie Woobies 4-1 and 3-2 |
| 01-24 → 01-25 | | Cat-fighting rewrite, rat counter, king position comms, "cheese teleportation" (transfer to king over walls, `ce53442`), rat indexing and king→spawn comms, BFS through dirt, traps vs full-HP enemies | 2034 on 01-25 |
| **01-26 02:00** freeze | US Qualifiers (team not eligible) | `src/awubot_us` = awubot at `742035f` (01:17), the ladder bot at that freeze | GST won |
| 01-25 → 01-27 | | Econ retune ("5-5 xsquare on micro maps", `f1d2961`), more aggressive spawning, king micro, king mining (`2bea54a`), king digging/gap passing, combat early exit, "ban comming enemies you cant see". Many parallel micro branches (§4). | 2056 on 01-26 |
| 01-27 → 01-29 | | Ant-colony go-home (`bdfe4b7`), MapTracker/MapManager map sharing over squeaks (`673d652`), mine bandwidth, 1-wide corridor handling, much more conservative defensive spawning, "keep up rat production + remove taboo zone", panic no-op squeaks to pull cats off the king, "fix baby rat attack!!!!!" (`6bb1242`) | 2033 on 01-28 |
| 01-29 (last ~6 h) | | Several commits are **untested or late**: cat trapping "(untested)" `a80b25b`, king formation anywhere at round 1900, king digging to escape, first rat goes to centre, remove profiler/indicators, "more aggressive spawning when rich (untested)" `e0bd796`, ally throwing at ≤22 HP `aaba1a7`, "trap when many rats" `5b8f178`, anti-glorp `271c868` + `a70328e` | **2095, #1 on the ladder** |
| **01-30 00:00:30** freeze | **Final Tournament** (16 teams, double elimination, live 01-31) | `awubot` = `final` (`a70328e`). The authors label `final` as the submission. Upstream `stable` has 3 more commits made before the freeze (§4), so which one ran is unverified. | **Champion, no series lost**: 5-0 3MiceWalkIntoABar, 3-2 Goobie Woobies, 4-1 cerr or despair, 5-0 GST, 3-2 GST (grand final) |
| 02-02 | Episode archived | Ladder scrims continued into March. Rating frozen at 2095.5. | |

## 3. Package lineage (`src/`)

Snapshots were matched to `src/awubot` history by diffing the hand-written files and templates against every commit (`git show <c>:src/awubot/...`). "Δ lines" is the number of lines that differ at the best-matching commit.

| Package | Frozen as | Best match (Δ lines) | What it is / main differences vs final `awubot` |
|---|---|---|---|
| `awubot` | `final` `a70328e` | n/a | Champion submission. **Edit only via `.jinja2` templates + `jinja.py`** (generated `.java` files are huge, e.g. `CellTracker.java` 436k lines). |
| `challenger` | local `1ff4b32` | identical except the package name | The dev copy you are meant to improve |
| `awubot_sus` | `94e1b8b` 01-28 "snapshot of current version for testing" | `a1bc5ea` 01-27 23:11 (0) | **Closest older version, about 2 days before final.** Baseline for the `sus-changes`/`main` econ experiment. Missing Jan 28-29 work: king tweaks/optimisations, 1-wide handling, conservative defensive spawning, keep-up rat production, map-tracker exploration, panic squeaks, the baby-rat attack fix, cat trapping, ally throwing, trap-when-many-rats, anti-glorp, first rat to centre, rich spawning. King mining is still hand-written `KingMiningStrategy.java`, not templated. Good A/B "previous version" opponent. |
| `awubot_us` | `0ababcc`/`079dd3d` 01-26 "add us quals bot" | `742035f` 01-26 01:17 (0) | Ladder bot at the US-quals freeze. Has the econ retune and aggressive spawning. Missing the king-mining rework (`2bea54a` "king mining (stable)" and later king tweaks; its `KingMiningStrategy` is the older version), ant colony, MapTracker, combat early exit, unseen-enemy comm ban, and everything listed for `awubot_sus`. Still has the old flat `GoHomeStrategy` and dead strategy classes (Rush, CatLuring, AggroRoam, KingDefense/ThreatContext, none instantiated). |
| `awubot_quals` | `040bbd1` 01-24 "push quals bot into repo" | `0b0427d` 01-24 00:37 (10) | **International Qualifiers submission** (won that tournament). Spawning was simpler: threshold `min(cheese/20+10, 40)`, flood only at ≥5000/≥3000 cheese, vs final's `min(cheese/10+10, 40)` with time-decaying flood thresholds `3000-0.75t` / `2000-t/2`. Lacks everything listed for `awubot_us` plus cheese teleportation, the rat-indexing/king→spawn comms fix, dirt BFS, traps vs full-HP enemies. |
| `awubot_mystery` | `81fd3aa` 01-22 "push good bot", touched by `99ab7c2` 01-24 (TryTrap rewrite) | ≈`67cb9be` 01-22 (408) | The "mystery" line that awubot was restarted from on 01-22/23 (`aece3fa`). Oldest snapshot. Contains an unused `TryAllCombat.java.jinja2` (an attempt to unify all micro moves). |
| `afk`, `suicide` | 01-17 / 01-18 | n/a | Trivial sparring bots: one only yields, the other calls `disintegrate()` |

The sprint submissions (`experiment`, `awubot_fixratthrowing_old`, `awubot_sprint2`, `awubot_1`, …) were deleted in clean-up commits `e1414fa`, `5dc752f`, `f1aee7a`, `0ababcc`. Recover them with `git show <commit>^:src/<pkg>/…`.

## 4. Upstream branch atlas

How to read this: "ahead" = commits not in `final` (`git rev-list --count champion/final..B`). "Merged?" is judged from `git cherry` plus matching commit subjects. Many branches were merged by re-applying patches rather than by merge commits, and none of the unmerged diffs reverse-apply cleanly onto final, so expect to port them by hand. Commit-message evidence is quoted.

| Branch | When / who | What it tried | In final? | Evidence / notes |
|---|---|---|---|---|
| `final` (= `HEAD`) | 01-29 | Champion | yes | default branch |
| `stable` (+3) | 01-29 22:26-23:49, awu + caterpillow | Bugnav treats 1-thick border walls as map edge (`05e7205`). In combat, if no enemy is visible but a squeaked enemy is within d²≤36, turn to face it. Throw-targeting edge-case fix (`794faa7`). "remove sus code" (`7f1ccc0`). | **no** | Committed before the freeze, but the authors named `final` as the submission. Small, low-risk patches worth A/B testing. |
| `ally-throwing` (+6) | 01-29/30, caterpillow | Ally throwing at ≤11 HP (final uses ≤22). `c352477` fixes the ratnap checker "-100 cost … stumped by tryThrow bug where score goes up to 102". Trap when ≥28 rats and ≥1500 cheese. Targeting edge-case fix. | partial | `c352477` (**known scoring bug in final's throw/ratnap code**) and `71003a2` are not in final |
| `kingmove` (+2) | 01-30, Naman | King "progressive mine switch" toward map centre and mine de-dup across kings, with debug prints | no | message "idk"; unfinished |
| `main` (+14), `sus-changes` (+12) | 01-28/29, caterpillow + awu | `CheeseTracker` sets spawn thresholds from the cheese *gain rate*. Kings squeak mines too. Globally known mines weighted ×10. Go home at ≥20 raw cheese. Spawn threshold floor 20. Assorted king tweaks. | no | `awubot_sus` is their baseline. The result isn't recorded, and the authors shipped without it. |
| `econ-testing-with-newer-version` (+10) | 01-28/29 | Keep up rat production, remove taboo zone, map-tracker exploration, panic squeaks, mine ordering | yes | all subjects present in final |
| `more-econ-testing` (+13) | 01-28 | King mining + MapTracker + mine bandwidth | yes (mostly) | plus "commit old for comparison" snapshots |
| `micro-stronk` (+17) | 01-27/28 | Trap weights, bite-damage tweak, prefer ratnapping enemies that carry rats | partial | "**undo tghe damasge change wtf why is it so abd**". The bite-damage tweak was harmful. |
| `time-for-another-micro-branch` (+15) | 01-27/28 | Same base plus enemy-density heuristics for throwing (`c1c4283`, `cc7dfc7`) | no (heuristics) | untested |
| `micro-but-good`, `micro-but-good-fr-this-time` (+6/+15) | 01-27 | Move-ordering optimisation (merged as `3759e66`), trap tweaks, trapping avoids standing next to enemies | partial | snapshots and CE fixes |
| `micro` (+23) | 01-26/27 | "ONLY XSQUARE SCRIMS", more traps, traps consider ratnap, **combat-mode spawning** (detects combat from a rat-counter drop and raises the spawn threshold), passive trapping, 12 rats, "untested dynamic damage", combat tracker accuracy | mostly no | "fully lost the plot (bug fixes and stuff)". "12 rats on small maps" landed separately (`ba7c45f`). |
| `micro-allythrowing` (+12) | 01-25/26 | Ally throwing behind a kill switch. Disabled ("gatekeep"), then re-enabled with attacks disabled while carrying. | superseded | final re-implemented it on 01-29 |
| `micro-guarding` (+9) | 01-26 | CombatTracker treats cells inside allies' vision cones as "guarded" ("mystery change"). Low-HP mode. | no | untested idea |
| `smarter-micro` (+1) | 01-25 | Ratnap checker accounts for visible dirt (+ `dirtworld` map) | no | |
| `testing` (+7) | 01-23/24 | "always allow dirt" in pathing, "reduce attack cheese usage" (fewer cheese-boosted bites) | no | |
| `mining-changes`, `temporary-attempt-at-trying-to-fix-rat-indexing` (+4) | 01-24 | "bullshit new mining heuristics", uniform mine distribution, "use law of large numbers" (random mine assignment), drop `myIndex` | no | final fixed indexing differently (`2b06ad5`) |
| `avoid-cat-vision` (+8) | 01-24 | Cat-fighting rewrite (cat can sense, conservative fighting) | yes, except `73ec115` "if we die fighting cat" | |
| `econ-testing`, `dirt-changes`, `ant-colony-real` | 01-24 → 01-27 | King spawn tweaks, BFS through dirt / first rat digs, ant-colony go-home | yes | |
| `exploration` (+1) | 01-25, awu | MinerStrategy exploration rewrite | no | "**(untested)**" |
| `king-can-move` (+1) | 01-25, Naman | King walks to visible cheese near its mine | no as-is | final's king mining covers similar ground |
| `bricked-code` (+1) | 01-27, awu | A version of "better king digging" | no | name says it all. final has a different `c171662`. |
| `mystery` (+27) | 01-22 → 01-24 | Econ experiments: rat spawning cheese cutoffs, "funny econ change" (target cheese `2600-1.05t`, else flood) then "rollback econ change", "formation v1", autoscrim utils | mostly superseded | |
| `mystery-pathfinding` (+12), `pathfinding` (+10) | 01-22/23, awu | **Instanced** Pathfinder refactor and the first ant-colony attempt | no | final kept the static `Pathfinder`. Ant colony was redone on 01-27. |
| `formation` (+1) | 01-17, Yiheng | First king-formation strategy | no | Naman's later formation logic shipped instead |
| `smarter-kings`, `submission-quals` | 01-25 / 01-24 | Pointers only. `submission-quals` = the intl-quals submission (`0b0427d`). | yes | |

Signals for us:
- Things the authors tried and **backed out**: bite-damage tweaks, dynamic damage, the "funny econ" cheese target, instanced pathfinding, the new mining heuristics.
- Things they **never finished or validated**: combat-mode spawning, guarding cells, enemy-density throw heuristics, rate-based spawning (`CheeseTracker`), exploration rewrite, progressive king mine switching, throw-score bug fix, `stable` fixes.
- The **untested** commits that did ship on the final day are the most likely places for bugs (see [intel.md §6](intel.md)).

## 5. Inspecting upstream later

```bash
git fetch origin '+refs/champion/*:refs/remotes/champion/*'   # the champion's branches, archived in our repo, land as champion/*
git branch -r | grep champion/
git log --oneline champion/final..champion/stable    # commits not in final
git cherry -v champion/final champion/<branch>       # '-' = patch-equivalent already in final
git diff --stat champion/final champion/<branch> -- 'src/awubot/**/*.jinja2'   # templates only; generated .java diffs are huge
git show champion/<branch>:src/awubot/robot/rat/combat/moves/TryThrow.java.jinja2 | less
git worktree add --detach /tmp/wt-<name> champion/<branch> && … && git worktree remove /tmp/wt-<name>
```

Diff only the `*.jinja2` files and hand-written `.java` files (those without a `.jinja2` sibling). Regenerate code with `jinja.py` (see [codegen.md](codegen.md)).
