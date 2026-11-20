# AT2: book entries on the maps after the worst

Package AT2 (wave 6, the book). The 17 maps of `goal` that each lose a little and had no entry: DefaultSmall, shell,
trapped, warhammer, dirtpassageway, cheeseysquare, hatefullattice, moonlitriverpark, AceOfSpades, directconfrontation,
streetsofnewyork, thunderdome, constrictinglattice, stareintotheabyss, Excavation, jail, holeinthewall. Main against
awubot; "seat" is ours. Data and tools: `~/battlecode26-data/scratch/at/` (`at2/runs.txt` holds every run ID;
`at2/ref_kd.jsonl` the loss readout; `at2m*.fit.txt` the screens' fits; `confstats.py RUN` a run's paired effect by
map and seat).

## 0. Entries

Each chosen on one fresh seed base by `tools/book/book fit` (best of K candidates on that map-seat), confirmed on
another against main on the map alone, both seats played (paired difference against main, 95% interval). Variants are
one-constant changes of main made by `tools/tune points`; no new switch code.

| map | seat | constant | value | chosen (K, seed base, raw / adj / p_max, run) | confirmed (seed base, main, effect, run) |
|---|---|---|---|---|---|
| warhammer | A | `SPAWN_CAP_SMALL` | 50 | K 4, 26008: +0.267 / +0.230 / 0.000, `1120-043031-ab-eee6` | 26010, `a603160`: +0.173 [+0.107, +0.240], `1120-045603-ab-5e9d` (with `SPAWN_RICH` 1500 also set: +0.173, `1120-045604-ab-ed60`) |
| warhammer | B | `SPAWN_CAP_SMALL` | 40 | K 4, 26008: +0.142 / +0.110 / 0.017, `1120-042936-ab-0aa3` | 26010, `a603160`: +0.060 [+0.007, +0.113], `1120-045603-ab-7654` |
| hatefullattice | A | `SPAWN_CAP` | 60 | K 11, 26005: +0.125 / +0.076 / 0.055, `1120-030709-ab-3e6b` | 26007, `767421c`: +0.160 [+0.088, +0.232], `1120-040910-ab-8af0` |
| dirtpassageway | A, B | `SPAWN_CAP` | 30 | K 11, 26005: A +0.125 / +0.071 / 0.12, B +0.100 / +0.048 / 0.14, `1120-031046-ab-e9c7` | 26007, `767421c`: A +0.093 [+0.012, +0.175], B +0.187 [+0.116, +0.257], `1120-040913-ab-62cc` |
| warhammer | A | `SPAWN_RICH` | 1500 | K 11, 26005: +0.138 / +0.089 / 0.017, `1120-030949-ab-3847` | 26007, `767421c`: +0.100 [+0.018, +0.182], `1120-040903-ab-b4b6`; adds nothing once `SPAWN_CAP_SMALL` is 50 (both: +0.173, the cap alone: +0.173): drop it |

Main's own runs of the screens: `1120-024047-ab-4d38` (26005, its 529 losses replayed and kept), `1120-042943-ab-6431`
(26008, 26009). SPAWN_CAP 60 is harmful on most other maps (−0.2 to −0.56 in the 26005 screen): hatefullattice A only.

## 1. Where main loses here

Main `f9ebb344`, 80 cells a map (`1120-024047-ab-4d38`): 529 losses of 2,720 games (0.805). The 529 replays read by
KD's `KDStats` and CM's `classify.py` (our kings' HP lost by cause):

- **Starvation** on warhammer (26 of 26 losses), moonlitriverpark (23 of 24), cheeseysquare (28 of 40), shell (31 of
  48), jail (23 of 38), and part of dirtpassageway, hatefullattice, AceOfSpades: the bank (2,488 at round 1) falls to
  0 by r250–450 while the army holds 16 rats; the lone king then starves. Our deliveries run at about half of
  awubot's. Spending less makes it worse (§2), so it is an income loss, not over-spending.
- **Enemy bites** (COMBAT) on DefaultSmall (34 of 53), trapped (29 of 40), thunderdome (22 of 26), stareintotheabyss
  (23 of 25), streetsofnewyork (22 of 28), Excavation (15 of 20): mostly before r320.
- **Cats** on directconfrontation (22 of 26).

## 2. Screens

Every screen: `tools/book/book matrix` against main, the maps' `map:` pool, both seats; fit by `tools/book/book fit`.

| matrix | seed base | maps, cells a map | candidates (K) | what it showed |
|---|---|---|---|---|
| at2m1 | 26005 | 17, 80 | `SG_LOOK_RIGHT` 1, `PM_COOP_HOLD` 1, `PM_SIEGE` 1 and 2, `SPAWN_CAP` 30 and 60, `CM_CAT_HUNT_START` 500 and 800, `MINES_KING2` 3 and 7, `SPAWN_RICH` 1500 (11) | the entries above; the rest failed on 26007 (§3); the cat hunt, the cooperation hold and `MINES_KING2` change nothing here |
| at2m2 | 26006 | 17, 80 | `CAT_DEF_DSQ` 100 and 200, `SPAWN_FLOOR` 250, `SPAWN_CHEESE_DIV` 20, `SPAWN_CAP_SMALL` 20 (5) | nothing (every p_max ≥ 0.18); `CAT_DEF_DSQ` does nothing on these maps; `SPAWN_FLOOR` 250 and `SPAWN_CHEESE_DIV` 20 lose 0.1–0.2 on the starving maps; `SPAWN_CAP_SMALL` 20 loses 0.4–0.8 on the three 900-tile maps |
| at2m3 | 26008 | DefaultSmall, warhammer, directconfrontation, Excavation, 120 | `SPAWN_CAP_SMALL` 40 and 50, `SPAWN_POOR_CUT` 0 and 20 (4) | warhammer's entries; DefaultSmall loses with every change (−0.19 to −0.78): main's cap is its optimum |
| at2m4 | 26009 | the 8 starving maps, 100 | `EXPLORE_P` 0.2, 0.4, 0.8 (3) | no map structure (τ 0.019); its one lead failed (§3) |
| at2m5 | 26012 | 17, 80 | `CM_LANE_MEMORY` 1; with `CM_LANE_FLEE` 1 (2) | nothing (τ 0.015; 13 map-seats with no game changed; directconfrontation −0.01 to −0.03): our kings meet few cats here |

## 3. Failed on fresh seeds

| map | seat | constant | screen (26005) | confirmation (26007, `767421c`) |
|---|---|---|---|---|
| cheeseysquare | A | `SG_LOOK_RIGHT` 1 | +0.225, p_max 0.002 | −0.060 [−0.147, +0.027], `1120-040903-ab-a0e3` |
| DefaultSmall | A | `SG_LOOK_RIGHT` 1 | +0.100, p_max 0.12 | −0.040 [−0.127, +0.047], `1120-040914-ab-ba76` |
| shell | B | `SPAWN_CAP` 30 | +0.163, p_max 0.055 | +0.067 [−0.029, +0.162], `1120-040906-ab-9aa6` |
| constrictinglattice | A | `EXPLORE_P` 0.8 | 26009: +0.090, p_max 0.034 | 26011, `9f2de07`: −0.060 [−0.127, +0.007], `1120-052434-ab-1bd0` |

`p_max` is the test of one map-seat's best of K; over 34 map-seats a p_max of 0.002 arises by chance about once in
15 screens, so the confirmation decides (cheeseysquare A).

## 4. What is left

The screens hold every switch that exists today (`KING_HAND` and `KING_PATH` excepted: KR screened them on every map)
and the spawning, exploration and cat constants. What they leave:

- DefaultSmall (both seats, 53 losses of 160): every change of the small-map cap or the poor-map cut loses; the losses
  are the r175–320 fights (34 of 53). Needs reading the fights, not a constant.
- The starving maps (moonlitriverpark, shell, cheeseysquare, jail): deliveries at half awubot's; spending less loses.
  The remedy is income: the king's site and the miners' targets on those maps, as book data.
- trapped, thunderdome, stareintotheabyss, streetsofnewyork, Excavation: early fights, untouched by any screen.
- directconfrontation: cat kills, untouched by the lane memory, the cat defence radius and the cat hunt.
