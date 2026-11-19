# RS: the shelf re-tested at ±1 pp

Every arm that read above zero and was rejected by a coarse test (δ 0.05–0.10), re-tested on main by the precision
route (docs/evaluation.md, Protocol item 5): a 1,000-cell paired screen on `goal` on a fresh seed base, then a
3,000-cell confirmation on another fresh seed base whose 95% lower bound must clear 0.

Main: `da9c83f` (TN2 merged; bot source unchanged through `236f0d4`). Seed bases 15001–15099, each used once.
Scratch: `~/battlecode26-data/scratch/rs/` (`tests.md`: every run; `results.md`; `patches/`: the ports;
`flips.py RUN`: flipped games by ending; `flipsround.py RUN 1300`: net flips by the round the pair diverged at the latest).

**Result.** One arm passes: **PT 2d, the late cat hunt** (screen +0.004, confirmation +0.010 [+0.003, +0.018]); its
own mechanism is worth about +0.5 pp (§3). Nothing else on the shelf holds a gain of 1 pp: of ten arms, five read ≤ 0
on their screen, three of the five confirmations read 0, one (PT 1) is harmful, and one (AB defdirt) misses by
0.03 pp. The coarse tests were right to reject them; their positive reads came from small samples and class pools.

## 1. Inventory

Every arm in docs/closed.md, docs/theory/*.md and the tuning tables with a positive point estimate. "Biased" marks a
read on a pool of a main's own losses at seed base 0 (docs/evaluation.md, "A pool of a main's own losses is biased at
seed base 0"); those are left out. The `L-*` pools come from the W0.3 mirror (every cell split), so they are not
selected on main's losses; the `L1-*` and `L2-*` pools are, and count only at a fresh seed base.

| arm | change | unbiased reads: diff [95%], pool, seed base, cells | branch | mechanism on main | picked |
|---|---|---|---|---|---|
| E2b SR | no rat cap while an enemy is in the king's vision, off E2's mine-poor maps | +0.013 [−0.013, +0.040], `goal`, 52002, 274; S (the same on every map) on its non-capped maps 0.71 vs 0.66, `L1-early` ∪ `mines-le4`, 52001, 52 | `t3/e2b-siege-rich` | yes (spawn cap block) | **yes** (two reads) |
| E2b SO | no rat cap while the enemies in the king's vision outnumber our rats there | +0.023 [−0.023, +0.070], `L1-early` ∪ `mines-le4`, 52001, 153 (117 unchanged) | `t3/e2b-siege-out` | yes | no: SR is the same idea read on `goal` |
| AB centre | delete the first rat's centre rush (round-2 target and dirt pathing) | +0.020 [−0.027, +0.068], `goal`, 3100, 222; +0.012 [−0.046, +0.069], `AB-small`, 0 (chosen by firing), 85; OBG +0.004 | `t3/ab-centre` | yes | **yes** (two reads, a deletion) |
| K2 D' | kings remember cat footprints, park and chase cheese off them, no mining step into a known lane | +0.021 [−0.038, +0.075], `L-cat`, 20261131, 192; `goal` +0.009 [−0.035, +0.050], 20261132, 118; `goal` −0.002 [−0.036, +0.028], 20261137, 210 | `t3/k2-cat-lane-king-site` | yes (king mining; CL's open lead for bed) | **yes** |
| KS pocket | a king proven in a pocket targets only mines it can cover, else parks | +0.015 [−0.045, +0.083], `KS-pocket`, 0 (chosen by geometry), 100; `goal` −0.003 [−0.040, +0.034], 20261170, 191; king overruns −70% | in main's history (01a8280, reverted 7158b2c) | yes | **yes** (bytecode gain) |
| PT 2d | from r1300 in a backstab, rats with no raw and no enemy seen or heard of for 50 rounds hunt cats | +0.027 [+0.003, +0.053], `L-points`, 0, 188; +0.006 [−0.016, +0.027], `L-points`, 3, 427; `goal` harm +0.005, 4, 95 | `t3/pt-hunt` | yes | **yes** |
| PT 1 | raw cheese goes home by the r2000 deadline; no exploration after r1800 | +0.024 [+0.005, +0.052], `L-points`, 0, 104 (97 unchanged); 1b +0.020 [−0.011, +0.057], same cells, 101 | `t3/pt-delivery` | yes (exploration share now 0.6, was 0.2) | **yes** |
| CL V | with a cat in view and the king outside its lane, no king step from open room into a narrow centre | +0.014 [−0.037, +0.063], `L2-cat`, 13101, 71 (63 unchanged) | `t3/cl-arm-v` | yes | **yes** (fires seldom) |
| CD (a) | miners in a centre box trap the two tiles toward the enemy start, r100–600 | +0.017 [−0.021, +0.060], `L2-rich-long`, 93101, 118; its diagnostic moved −2.5% [−7.6, +2.1] | `t3/cd-a-traps` | yes | **yes** |
| E3 change 0 | the mine squeak decoder reads all 3 mines (a bug fix) | `goal` harm +0.032 [−0.016, +0.079] (0, 95, c12b57b), +0.022 [−0.013, +0.057] (0, 116, cb204c7), +0.021 [−0.026, +0.068] (0, 95, f363820), −0.005 [−0.024, +0.016] (190, CW main); OBG −0.014, −0.026 | `t3/e3-territory-miners-deficit` | yes (`Squeaker` case 0) | **yes** (four reads, three on the same cells) |
| AB defdirt | delete the king's defensive dirt toward a cat within d² 20 | `goal` +0.018 [−0.022, +0.060], 3100, 109; `AB-defdirt` −0.011, 0, 45; OBG −0.016 | `t3/ab-defdirt` | yes | **yes** (a deletion) |
| CX A1 | graded grab threat (−100, −50 per further grab pair) | +0.022 [−0.017, +0.063], `L-combat`, 0, 337; A (A1 + split) on `goal` −0.007 [−0.041, +0.028], 7101, 272 | `t3/cx-a-graded-only` | yes (TryFight, threat masks) | no: its `goal` read is negative and TN2 found every threat penalty above ~50 makes the same choices |
| CX A | A1 + bite-then-grab split | +0.022 [−0.012, +0.057], `L-combat`, 0, 340; `goal` −0.007 | `t3/cx-a-graded-l4` | yes | no (as A1) |
| KD N | danger formation beside the lone king below 100 HP | +0.008, L-combat ∪ L-cat, 8301, 196; `goal` +0.008, 8302, 183; `goal` −0.022, 8201, 268 | `t3/kd-danger-second-king` history | yes | no: pooled −0.004; its oracle ceiling ≈ 0 for COMBAT |
| KD CAT2 | cat danger forms a second king at a far mine | +0.022 [−0.058, +0.113], cat pools, 8313, 116; CAT (its parent) −0.028 and `goal` −0.009 | `t3/kd-arm-cat2` | yes | no: oracle ceiling +0.9 pp with perfect foresight |
| KF T | skip the site of the last timed-out formation | +0.000, `mines-ge8`, 7101, 83; +0.013 on where_are_you, 7104, 40 | `t3/kf-arm-t` | yes | no: zero on its own pool |
| CB king 12 / finishing | kings bitten at 12, finishing bites | +0.025 / +0.020, `L-combat`, 0 | `t3/minimal-cheese-bites-arms-kings` | yes | no: kills come no faster (its diagnostic), one read |
| CB cap 13 | bite damage capped at 13 | +0.027, `L-combat`; cap 16p on `goal` −0.002 | `t3/minimal-cheese-bites-arms-damage` | yes | no: duels are ~0.5 a game |
| CC A6, A1, A4c, A3+A5 | spawn toward a carrier; keep ring dirt; assault; carrier rules | +0.011, +0.006, +0.008, +0.021 (stopped), `faceoff` ∪ `L-combat`; the full bundle on `goal` +0.001 [−0.033, +0.034], 2742, 345 | `t3/cc-*` | yes | no: the bundle of the positive ones reads 0 on `goal` |
| AB cattrap | delete the cooperation cat traps | `goal` +0.009, 111; `AB-cattrap` −0.036, 55 | `t3/ab-cattrap` | yes | no: pooled ≈ −0.004 |
| AB trap70 | delete trap-when-many-rats | OBG +0.026; `goal` −0.007, `AB-trap70` −0.026 | `t3/ab-trap70` | yes | no: negative against awubot |
| E2b del-cat | delete the cat spawn boost | +0.010, cat pools; `goal` harm −0.011, 232 | `t3/e2b-del-cat` | yes | no: pooled ≈ 0 |
| K2 B' | king silent while a cat hears it | +0.014 at 36 cells (stopped); B'' −0.033 | `t3/k2-squeak` | yes | no: steering is not what scratches kings |
| E2 cap −4 everywhere | | +0.014, `goal`; classes cancel | merged as arm 1a (mine-poor only) | merged | no |
| PC1 A | last-cat hold | +0.034, `L1-coop`, 8; after MC −0.186 | `t3/pc1-last-cat` | changed by MC | no: negative on today's mechanism |
| MC arm F | revisit known mines instead of exploring | −0.052 | `t3/mc-fallback` | | no: negative |
| E3 C1, C2, R1, E5 | miner targeting | −0.116 to −0.037 | | | no: negative |
| TN constants | `MINES_KING2` 7 (+0.005, s7), `EXPLORE_P_RICH` 0.8 (+0.011 s6, −0.002 s7), `SPAWN_FLOOR` 50 (+0.001), `IDLE_GOHOME`, `GOHOME_PCT`, `LOCK_IN_MIN_CHEESE` (flat) | TN, TN2 | Tune.java | | no: TN2's, each already read on 760 fresh cells |
| W2, XA, KD oracles | cheat-knowledge copies | | | | no: not buildable |

## 2. Screens

`tools/games ab challenger@ARM awubot --ref challenger@origin/main --pool goal --min-cells 1000 --max-cells 1000
--seed-base N --remote`, one seed base each, 2,000 games per side. Each arm is one commit on main `da9c83f` (CL V two),
on branch `t3/rs-<arm>`. Diff = arm − main on the same cells, 95% CI by map; "unchanged" = cells whose score equals
main's.

| arm | commit | seed base | diff [95%] | better / worse | unchanged | main on the cells | run |
|---|---|---|---|---|---|---|---|
| SR | f3fcb45 | 15001 | +0.001 [−0.009, +0.012] | 62 / 61 | 877 | 0.838 | `1119-173307-ab-fcdc` |
| CR (AB centre) | 0180dfa | 15002 | −0.015 [−0.043, +0.009] | 175 / 189 | 636 | 0.818 | `1119-173321-ab-d256` |
| K2D (K2 D') | bed3294 | 15003 | −0.006 [−0.020, +0.009] | 51 / 64 | 885 | 0.831 | `1119-173410-ab-714d` |
| KS (pocket) | 2b6fe72 | 15004 | +0.000 [−0.010, +0.009] | 33 / 33 | 934 | 0.821 | `1119-173509-ab-4935` |
| PT2D | 8280af4 | 15005 | +0.004 [−0.006, +0.016] | 66 / 57 | 877 | 0.808 | `1119-173601-ab-2766` |
| CLV (CL V) | efd8048 | 15006 | −0.002 [−0.010, +0.006] | 22 / 25 | 953 | 0.811 | `1119-173639-ab-21a1` |
| CDA (CD (a)) | 2fa6b9f | 15007 | +0.001 [−0.015, +0.017] | 109 / 106 | 785 | 0.825 | `1119-173714-ab-0c93` |
| E3D (decoder) | 963519a | 15008 | **−0.012 [−0.023, −0.002]** | 26 / 48 | 926 | 0.828 | `1119-173807-ab-915a` |
| DD (AB defdirt) | b3fb724 | 15009 | +0.001 [−0.008, +0.011] | 52 / 51 | 897 | 0.818 | `1119-173844-ab-a70e` |
| PT1 | daf7ee2 | 15010 | +0.001 [−0.004, +0.004] | 10 / 9 | 981 | 0.821 | `1119-173923-ab-2335` |

- **No arm reads a gain of 1 pp.** The best point estimate is PT2D's +0.4 pp; the highest upper bound CDA's +1.7.
  Most arms leave 85–98% of cells unchanged: they act in few games, so their effect on `goal` is capped by how often
  they fire, whatever they do there. The ten sum to −2.7 pp.
- **The coarse reads were the pools' selection, not hidden gains.** SR's +0.013 on 274 cells, AB centre's +0.020,
  K2 D''s +0.021 on `L-cat`, CL V's +0.014 and CD (a)'s +0.017 on the class pools all sit inside their old intervals
  and regress to 0 on 1,000 fresh `goal` cells. A class pool concentrates the cells where an arm fires, so its effect
  there is diluted on `goal` by the share of games it touches (CL V: 47 of 1,000 cells changed).
- **The E3 decoder fix read −1.2 pp [−2.3, −0.2]**, and its three earlier positive harm reads (+0.032, +0.022, +0.021)
  were the same 95 official cells (seed base 0, pass 0) on three mains: one read, not three. A 3,000-cell re-test on
  fresh seeds read −0.0018 [−0.0068, +0.0028]: the screen's loss was chance, and the fix changes nothing a robot
  knows or chooses (docs/theory/rs-leads.md §1).
- Five arms read above 0 (SR, PT2D, CDA, DD, PT1) and go to confirmation; their stack is screened too (§4).

## 3. Confirmations

3,000 cells (6,000 games a side), each on a seed base no earlier run used; the arm passes if its 95% lower bound > 0.
Each branch was rebased onto main `d58ad4a` first (bot source unchanged).

| arm | commit | seed base | diff [95%] | better / worse | unchanged | main | verdict | run |
|---|---|---|---|---|---|---|---|---|
| SR | cb81805 | 15051 | −0.001 [−0.007, +0.004] | 187 / 195 | 2,618 | 0.828 | fail | `1119-180558-ab-b694` |
| **PT2D** | 21af908 | 15052 | **+0.010 [+0.003, +0.018]** | 206 / 149 | 2,645 | 0.817 | **pass** | `1119-180558-ab-2c02` |
| CDA | 1a44b52 | 15053 | +0.003 [−0.006, +0.011] | 251 / 234 | 2,515 | 0.822 | fail | `1119-180559-ab-dc10` (2 lost games replayed by `retry`) |
| DD | 607c30d | 15054 | +0.0077 [−0.0003, +0.0165] | 208 / 173 | 2,619 | 0.820 | fail (by 0.03 pp) | `1119-180600-ab-95f0` |
| PT1 | 9c94081 | 15055 | **−0.004 [−0.007, −0.002]** | 17 / 42 | 2,941 | 0.826 | fail, harmful | `1119-180601-ab-19ef` |

**Where PT 2d's gain is.** The hunt acts only from r1300 (`isCatHuntOn`), so before r1300 the arm differs from main
only in bytecode, and a pair of games that diverged before r1300 flipped by chaos (W0.1), mean zero. Net flipped games
(arm won − main won) by the earlier of the pair's two end rounds:

| run | seed base | net flips, end < r1300 | net flips, end ≥ r1300 | ≥ r1300, pp of games |
|---|---|---|---|---|
| PT2D screen | 15005 | +5 | +3 | +0.15 |
| PT2D confirmation | 15052 | +29 | +31 | +0.52 |
| B2 screen (PT2D + DD) | 15012 | +10 | +12 | +0.60 |
| B2 confirmation (PT2D + DD) | 15057 | +1 | +35 | +0.58 |
| bundle 5 confirmation | 15056 | +60 | +32 | +0.53 |
| bundle 5 screen | 15011 | +2 | +9 | +0.45 |
| arms without the hunt (SR, CDA, PT1 confirmations) | | 0, +21, −8 | −8, −5, −17 | |

The after-r1300 part is the hunt: +0.51 pp over the six runs that carry it (122 net games of 24,000), steady from
seed base to seed base, while the before-r1300 part swings from +1 to +60. So PT 2d is real and worth about +0.5 pp;
half of its confirmation's +1.0 is chaos in its favour. In its confirmation the late flips are mostly points endings
at r2000 (73 lost → won, 49 won → lost): the cat-damage term of the score (PT's own confirmation of 2d: cat term of ΔD +1.07 a game, kings −0.70).

## 4. The bundle

- **Bundle 5** (SR + PT2D + CDA + DD + PT1, the five screen-positive arms, branch `t3/rs-bundle5` `08f201f`): screen
  +0.005 [−0.012, +0.023] (15011, 1,000 cells, `1119-180624-ab-045d`); confirmation **+0.015 [+0.005, +0.027]**
  (15056, 3,000 cells, 438 / 352, `1119-182220-ab-6b70`). It passes the route as one change, but not on its parts' merit:
  its gain after r1300 equals PT2D's alone (+32 net games), the +60 before r1300 is chaos, and it carries PT 1, harmful
  on its own confirmation. Not recommended.
- **B2** (PT2D + DD, chosen after the confirmations, so a new arm; branch `t3/rs-b2` `803c52e`): confirmation +0.006
  [−0.004, +0.016] (15057, 3,000 cells, 299 / 273, `1119-183542-ab-7905`): fails. Its screen, launched in parallel
  with a fixed rule (screen > 0 and confirmation lower bound > 0), read +0.011 [−0.004, +0.025] (15012, 1,000 cells,
  `1119-183538-ab-223e`).
  DD adds nothing measurable to PT2D. Its smoke (`1119-183601-ab-17a4`) and gauntlet (`1119-183601-gauntlet-c48b`:
  OBG −0.006, GST −0.018 [−0.036, +0.000], gravy, goobie, awubot_sus inconclusive at the 32-cell cap) passed.
- **The confirmed stack is PT 2d alone**: branch `t3/rs-shelf`, one bot commit (`3e0cd24`, the same source as
  `t3/rs-pt2d` `21af908`). Smoke `1119-184456-ab-e00b`: no robot of ours killed by its own exception (awubot 2).
  Gauntlet `1119-184455-gauntlet-4edc` (seed base 15092): no harm. OBG +0.004 [−0.035, +0.044] (112 cells), GST −0.014
  [−0.036, +0.008] (175 cells, 5 better / 10 worse), nine `quick` opponents pass; awubot_us −0.031 [−0.094, +0.031]
  (1 / 3) and awubot_sus 0.000 end at the 32-cell cap. GST leans negative in both gauntlets that carry the hunt
  (B2's −0.018): about −1.5 pp, inside both intervals.

## 5. What is left

- **PT 2d at about +0.5 pp** is the only small gain on the shelf. 90% needs about +6 pp: the shelf does not hold it.
- **The E3 decoder**: neutral, not load-bearing (re-test and probes, docs/theory/rs-leads.md §1).
- **AB defdirt** read +0.018, +0.001, +0.008 on three `goal` seed bases; it costs `bed` (a pinned king's shield) in both
  RS runs. A version that keeps the dirt while a cat pins the king would be a new arm, shaped by these runs: screen
  and confirm it on fresh seed bases, never on these cells.
- **Arms that fire in few games cannot be confirmed by any test of their own**: CL V changes 5% of cells, PT 1 2%,
  KS 7%. Their effect on `goal` is capped by that share; class pools overstate them for that reason.
- Two games of CDA's confirmation and four of PT2D's never came back from Imperial (`no result`; `run-1119-180805-8b01`);
  PT2D's came back after a long wait, CDA's were replayed with `tools/games retry`. Reported, not patched.
