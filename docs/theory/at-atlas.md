# AT: the atlas of shelf arms by map-seat

Package AT (wave 6): which measured-and-rejected arms win on particular `goal` maps and seats, from the games already
played, confirmed on fresh seeds; and main's map-seat table now. Tools and data: `~/battlecode26-data/scratch/at/`
(§5). "Seat" is our seat. An effect is a paired difference (arm − its own base main, both against awubot) in win
rate on that map-seat; on `goal` one map-seat weighs 1/190, so +0.19 there is +0.1 pp of `goal`.

## 1. Method

- **Data.** Every `results.jsonl` under `games/runs/` (903 runs, 993k games, snapshot of 19 Nov 21:28), engine
  1.2.6, against awubot, `goal` maps. Two traps:
  - a record's `a` is seat A's package: when awubot holds seat A, `a` is awubot, `b` is ours and `seat` is B (our
    seat); `winner_pkg` must be read against our package, not `a`;
  - cells taken from main's losses (the list pools `L-*`, `L1-*`, `L2-*`, `E3-ge8`, `MP-combat` and `file:` pools,
    played at seed base 0) bias every arm upward (+0.17 to +0.32 on L1 pools): runs on them at seed base 0 are
    dropped. A fresh seed base re-derives every seed, so those runs stay.
- **Pairing.** Games are deterministic per (class hash, map, seed, seat), so an arm's games pair with its base's games
  from any run (run.json `bots` gives the hash; tune screens s5 and fls1 played their variants beside a separate main
  run). An arm is one patch (patch-id of the `src/challenger` diff base → arm) on one base main; the same patch on
  several bases also gets a pooled arm (`PID@all`). Tune screens give one pseudo-arm per constant (y = x_c · d over
  the variants; effect of +1 step) and, where s1, s2 and s5 screened a constant at the same value and step,
  `tune:ALL:NAME`.
- **Model.** Per arm and map-seat: n paired games, m discordant (arm ≠ base), k of them won by the arm; effect
  θ = f (2π − 1), f = m/n shrunk to the arm's rate × the map-seat's flip factor (8 pseudo-games), π ~ Beta across the
  arm's map-seats with mean and concentration on a grid, posterior averaged over the grid. Concentration ∞ is "no
  map-seat effects". A normal random-effects model on the map-seat means fails here: arms that act on few maps leave
  many map-seats with no discordant game, which pin the variance to 0 (KP3: split-half r +0.37, normal-model τ̂ 0).
- **Nulls.** Sign flip of every paired game (no effect anywhere), 30 replicates; shuffle of map-seat labels within
  an arm (no heterogeneity), 8; split-half: choose map-seats on one seed half, score on the other.
- **Confirmation.** The arm's own commit against its own base on one map, fresh seed base, fixed cells:
  `tools/games ab challenger@ARM awubot --ref challenger@BASE --pool map:NAME --seed-base N --min-cells C --max-cells C
  --remote`. Constants: one-constant variants of main from `tools/tune points at1` (base `de6e5b8`).

## 2. Confirmed entries

| arm (branch) | map | seat | effect [95%] fresh seeds | main there | run |
|---|---|---|---|---|---|
| KP3, the king plans its route (`t3/ms-arm-kp3`, `2dbed577` vs main `419fa32`) | EscapeTheNight | B | +0.727 [+0.655, +0.798] (150 cells) | 0.007 | `1119-222443-ab-30b0` |
| KP3 | EscapeTheNight | A | +0.007 [−0.006, +0.020] | 0.993 | same |

## 3. Main's map-seat table now

`~/battlecode26-data/scratch/at/mapseat_p.json`, the format of W3's (`"map|seat": [shrunk p, games, raw p]`): 21,279
games of the current bot (class hash `b9f344664968e099`, main since 93e8311) against awubot on `goal`, from 9 runs,
102–160 games a pair, outcome-selected cells excluded; beta-binomial shrinkage as W3's `mapvar.py`. Mean 0.830,
true map-seat SD 0.153. All 190 pairs with intervals: `mapseat_main.tsv`.

| map | seat | won / games | shrunk p [95%] | W3 table (56 games) |
|---|---|---|---|---|
| EscapeTheNight | B | 2 / 105 | 0.06 [0.02, 0.11] | 0.02 |
| houseofcards | B | 28 / 106 | 0.29 [0.21, 0.38] | 0.29 |
| popthecork | B | 52 / 153 | 0.36 [0.28, 0.43] | 0.32 |
| bed | B | 56 / 159 | 0.37 [0.29, 0.44] | 0.39 |
| TheHeist | A | 44 / 104 | 0.44 [0.35, 0.54] | 0.57 |
| peaceinourtime | A | 72 / 156 | 0.47 [0.40, 0.55] | 0.39 |
| cheddarchokehold | B | 49 / 103 | 0.49 [0.40, 0.59] | 0.52 |
| asabovesobelow | B | 52 / 104 | 0.52 [0.42, 0.61] | 0.36 |
| diamond | B | 82 / 151 | 0.55 [0.47, 0.63] | 0.64 |
| groveisbeingdesecrated | B | 57 / 105 | 0.56 [0.46, 0.65] | 0.71 |
| tminusten | A | 88 / 160 | 0.56 [0.48, 0.63] | 0.61 |
| TheHeist | B | 57 / 104 | 0.56 [0.47, 0.65] | 0.57 |
| closeup | A | 90 / 154 | 0.59 [0.52, 0.67] | 0.59 |
| longwayaround | B | 63 / 103 | 0.62 [0.53, 0.71] | 0.61 |
| kingofthehill | B | 65 / 105 | 0.63 [0.54, 0.72] | 0.50 |
| longwayaround | A | 64 / 103 | 0.63 [0.54, 0.72] | 0.55 |
| sunandmoon | B | 98 / 155 | 0.64 [0.56, 0.71] | 0.54 |
| AceOfSpades | B | 67 / 104 | 0.65 [0.56, 0.74] | 0.59 |
| pocketsofcheese | B | 99 / 153 | 0.65 [0.58, 0.72] | 0.54 |
| QueenOfHearts | B | 68 / 104 | 0.66 [0.57, 0.75] | 0.73 |

Moved since W3's table (two-proportion z on the raw rates): 19 pairs at |z| ≥ 2 where chance gives 9; only bed A
clears |z| 3 (0.66 → 0.86). Down: Meow A 0.98 → 0.86, grove B 0.71 → 0.54, isnotgold B 0.89 → 0.76. Up: insertmeowname
A 0.71 → 0.89, Nofreecheese A 0.68 → 0.87, mushroom B 0.71 → 0.89, Excavation B 0.71 → 0.89, DefaultSmall A 0.57 →
0.74, knifefight A 0.61 → 0.75, worldshardestgame A 0.61 → 0.76, and eight more at z +2.0 to +2.4.

## 5. Tools

In `~/battlecode26-data/scratch/at/` (Python: `venv/bin/python`, numpy, scipy, pandas): `extract.py` (runs →
`games.tsv`, then seat-normalised `games.pkl`), `units.py` (arm × base pairs, patch-ids), `atlas.py` (arms, the
model `fit2`), `armclass.py`, `inmain.py`, `merged_check.py` and `excluded.txt` (arms main already holds),
`fitall2.py` (fits, nulls, split-half), `shelf.py` (out-of-sample shelf gating), `shortlist.py`, `confirm.py`,
`confstats.py RUN` (a confirmation's paired effect per map and seat), `mainmap.py` (§3), `runs.txt` (run IDs).
