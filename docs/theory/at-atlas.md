# AT: the atlas of shelf arms by map-seat

Package AT (wave 6): which measured-and-rejected arms win on particular `goal` maps and seats, from the games already
played, confirmed on fresh seeds; and main's map-seat table now. Tools and data: `~/battlecode26-data/scratch/at/`
(§5). "Seat" is our seat. An effect is a paired difference (arm − its own base main, both against awubot) in win
rate on that map-seat; on `goal` one map-seat weighs 1/190, so +0.19 there is +0.1 pp of `goal`.

## 0. Summary

1. **An effect measured on an old base does not carry to today's main.** Over 25 tested predictions the fresh-seed
   effect on today's main was 0.72 of the atlas's (r +0.46). PC1's last-cat hold on TheHeist reversed (+0.26 in the
   atlas from S4-era bases, −0.39 on the post-MC base); `SPAWN_RICH` 2500 on uneruesansfin (+0.12 in two screens) and
   `MINES_KING2` 7 on RUN read 0 on today's main. Only a test against today's main (or the arm's newest base) counts.
2. **Confirmed entries: about +1.3 pp of `goal`** (round-2 effects only, so no selection bonus), §2:
   KP3 king route planner on EscapeTheNight B (+0.38 pp), RUN both seats (+0.19), pocketsofcheese B (+0.17),
   houseofcards B (+0.04); `MINES_KING2` 7 on toomuchcheese (+0.15), wallsofparadis (+0.08), pipes (+0.03), 3 on
   cheddarchokehold (+0.11); `SPAWN_RICH` 1500 on t_minus_five_minutes (+0.08), TheHeist (+0.05), safelycontained
   (+0.04). K2 D' on bed against its own stale
   base: B +0.57, A +0.19 (not yet on today's main).
3. **Map-seat effects are real for a few structural arms and constants, absent for the rest.** Of 261 per-base arms,
   35 show heterogeneity beyond the sign-flip null at p < 1/31 (9 expected by chance); the strong ones act on kings,
   cooperation or spawning (KP3, PC1's last-cat hold, `MINES_KING2`, `SPAWN_RICH`). Two constants replicate per map
   across independent tune screens (`MINES_KING2` r +0.66 to +0.74 over s1, s2, s5; `SPAWN_RICH` +0.67 over s1, s5);
   the other eight shared constants do not (−0.24 to +0.35, mean ≈ 0).
4. **The whole shelf, gated, is worth about +1.2 to +1.4 pp out of sample** (§4.3): split-half +1.7 to +2.0 pp
   within the recorded bases, times the 0.72 transfer. The confirmed entries already hold most of it.
5. Main's map-seat table now (§3): mean 0.830; worst EscapeTheNight B 0.06, houseofcards B 0.29, popthecork B 0.36,
   bed B 0.37. Only bed A moved clearly since W3's table (0.66 → 0.86).

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

Book form (map, seat, switch or constant, value). Effects are paired against the base on the one map, fresh seeds,
95% intervals; "both" = both seats pooled. Seed bases: 26001 (arms, round 1), 26002 (constants, round 1), 26003
(round 2), 26004 (round 3). KP3 runs: `2dbed577` (`t3/ms-arm-kp3`) against main `419fa32`; constants: one-constant
variants of main `de6e5b8` (`tools/tune points at1`: mk7 `92e8328`, mk3 `a89d404`, sr15 `effdd2e`, sr25 `e44a3d2`).

**Confirmed on two fresh seed bases (lower bound > 0 in both):**

| map | seat | switch or constant | value | round 1 | round 2 | runs |
|---|---|---|---|---|---|---|
| EscapeTheNight | B | KP3 king route planner | on | +0.727 [+0.655, +0.798] | +0.720 [+0.632, +0.808] | `1119-222443-ab-30b0`, `1119-225348-ab-7ccf` |
| pocketsofcheese | B | KP3 king route planner | on | +0.300 [+0.220, +0.380] | +0.327 [+0.235, +0.418] | `1119-222444-ab-963d`, `1119-225715-ab-746c` |
| toomuchcheese | both | `MINES_KING2` | 7 | +0.205 [+0.161, +0.249] | +0.147 [+0.104, +0.191] | `1119-222609-ab-f5d6`, `1119-231244-ab-111b` |
| wallsofparadis | both | `MINES_KING2` | 7 | +0.075 [+0.046, +0.104] | +0.075 [+0.047, +0.103] | same |
| cheddarchokehold | both | `MINES_KING2` | 3 | +0.165 [+0.124, +0.206] | +0.102 [+0.066, +0.139] | `1119-222628-ab-3827`, `1119-231244-ab-ca9c` |
| RUN | both | KP3 king route planner | on | +0.190 [+0.137, +0.243] | +0.182 [+0.138, +0.227] | `1119-231244-ab-b1f2`, `1120-003846-ab-59f9` |
| houseofcards | B | KP3 king route planner | on | +0.150 [+0.064, +0.236] | +0.073 [+0.002, +0.145] (third base; the second read +0.040 [−0.035, +0.115]) | `1119-222443-ab-c11a`, `1120-003847-ab-58a8` (`1119-225701-ab-f186`) |
| pipes | both | `MINES_KING2` | 7 | +0.047 [+0.023, +0.071] | +0.030 [+0.014, +0.046] | `1119-231245-ab-1aa0`, `1120-003847-ab-05f4` |
| t_minus_five_minutes | both | `SPAWN_RICH` | 1500 | +0.068 [+0.027, +0.108] | +0.078 [+0.042, +0.115] | `1119-222650-ab-9629`, `1119-225539-ab-8994` |
| safelycontained | both | `SPAWN_RICH` | 1500 | +0.023 [+0.002, +0.045] | +0.037 [+0.016, +0.059] | `1119-231245-ab-66da`, `1120-003847-ab-ef4d` |
| TheHeist | both | `SPAWN_RICH` | 1500 | +0.050 [+0.001, +0.099] | +0.043 [+0.002, +0.083] | same |

By seat: toomuchcheese A +0.240/+0.190, B +0.170/+0.105; cheddarchokehold A +0.110/+0.055, B +0.220/+0.150;
wallsofparadis and t_minus_five_minutes level. KP3 leaves seat A of EscapeTheNight and pocketsofcheese unchanged
(+0.007, +0.000/+0.013).

RUN by seat: A +0.267/+0.205, B +0.113/+0.160; houseofcards by seat: A +0.115/+0.064/+0.010, B +0.150/+0.040/+0.073.
Every run here is against plain main before `KING_HAND` 7 (6017078): KP3 against `419fa32`, constants against
`de6e5b8`, never on top of a book entry. On EscapeTheNight B, pocketsofcheese B and the other seat-B VERTICAL maps the
new king hand acts on the same mechanism: those KP3 entries need re-measuring on top of it.

**Against the arm's own (stale) base only:** K2 D' (`t3/k2-proj`, `502c01d9` against `71402d9`, 34 merges behind
main): bed B +0.505 [+0.422, +0.588] and +0.573 [+0.486, +0.661], bed A +0.155 [+0.084, +0.226] and +0.193 [+0.118,
+0.269] (`1120-004703-ab-2c89`, `1120-005256-ab-2bef`). Today's main already wins bed A 0.86 (that base: 0.76), B 0.37.

**`SPAWN_RICH` 1500, second base (`1120-003847-ab-ef4d`, against plain main):** safelycontained +0.037 [+0.016, +0.059]
(first +0.023 [+0.002, +0.045]) and TheHeist both seats +0.043 [+0.002, +0.083] (first +0.050 [+0.001, +0.099]; seat A
+0.090 [+0.034, +0.146], B −0.005) hold; warhammer +0.030 [−0.014, +0.074] (first +0.063) and peaceinourtime A +0.025
[−0.069, +0.119] (first +0.120) do not. TheHeist's books 1 and 2 entries were not under these runs: the entry needs a
measurement on top of them.

**Failed on fresh seeds:** PC1's cooperation last-cat hold on TheHeist against its post-MC base, −0.390 [−0.449,
−0.331] (`1119-222444-ab-1b90`; +0.31 in the atlas, from older bases); `SPAWN_RICH` 2500 on uneruesansfin −0.018 and
rift +0.022 (`1119-222641-ab-d100`; +0.12 and +0.08 in the screens, which ran on older mains); `MINES_KING2` 7 on RUN
(+0.000), constrictinglattice and neitheroptionisgood (no game changed), diamond, where_are_you; KP3 on diamond, gelcatinouscube,
closeup (+0.04 to +0.06, intervals across 0).

## 3. Main's map-seat table now

`~/battlecode26-data/scratch/at/mapseat_p.json` (the bot before `KING_HAND` 7), the format of W3's (`"map|seat": [shrunk p, games, raw p]`): 21,279
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

## 4. The atlas, the shortlist and the shelf

### 4.1 The atlas

282 arms with ≥ 200 paired goal games (261 per base, 21 pooled over bases; 2.03 M paired games counting tune
pseudo-arms), of which 168 are eligible switches (not in main, a commit to play, or a screened constant). Per arm:
`fits2.pkl` (heterogeneity LR, its sign-flip and shuffle p, split-half gain and r); per arm × map-seat and arm × map:
`cands2.pkl` (θ, sd, P(θ > 0), n, raw mean). The arms with the strongest map-seat structure (LR, both nulls at their
floor): PC1's last-cat hold (LR 111, concentration 0.9: an all-or-nothing effect by map), KP3 (45), `tune:ALL:
MINES_KING2` (31), `tune:ALL:SPAWN_RICH` (26); then MS's map-knowledge oracles (worktree builds, not playable) and
the EXPLORE_P variants (already merged). Within one screen a constant's pseudo-arm can look heterogeneous from the
other constants' per-map effects (`GOHOME_PCT`: LR 19 in s1, 17 in s2, but s1-against-s2 r +0.05): for constants the
test is replication across screens.

### 4.2 The shortlist

Score = shrunk θ × P(θ > 0), over eligible arms, both seat and map level, at ≥ 20 games: 23,560 candidates.
Correction: the sign-flip null's maximum score over all of them (100 replicates, `null20.py`): median 0.090, 95%
0.154, max 0.190. Above it:

| arm (base) | map | seat | θ ± sd | games | family-wise p | fresh seeds |
|---|---|---|---|---|---|---|
| KP3 (`21af908`) | EscapeTheNight | B | +0.371 ± 0.075 | 42 | 0.01 | +0.72, twice |
| PC1 last-cat hold (`3762a45`, `25338f1`) | TheHeist | A, B, both | +0.21 to +0.34 | 30–179 | 0.01–0.02 | −0.39 on the post-MC base |
| KP3 | EscapeTheNight | both | +0.197 ± 0.040 | 84 | 0.01 | as above |
| K2 D' (`076be2a`) | bed | B | +0.187 ± 0.119 | 21 | 0.02 | §2 |

Next (family-wise p 0.08–0.24): KP3 houseofcards, pocketsofcheese, gelcatinouscube; PC1 on grove and peaceinourtime
A; `SPAWN_RICH` on uneruesansfin; `GOHOME_PCT` (s1 alone) on popthecork B and stareintotheabyss B. The constant
entries of §2 came from the cross-screen replication (§0.2), not from this score: their per-map θ (0.05–0.14) sits
below the family-wise bar, but three screens agreeing is its own replication.

### 4.3 What gating the whole shelf adds

`shelf.py`: on one seed half, fit every eligible arm and give each goal map-seat the arm with the largest θ above a
threshold; score that choice on the other half's raw paired mean; both ways. Null: the same with every game's sign
flipped (10 replicates).

| threshold on θ | map-seats gated | gain, pp of `goal` | null mean ± sd |
|---|---|---|---|
| 0 | 190 | +1.1 | −0.3 ± 1.1 |
| 0.05 | 94 | +1.7 | −0.4 ± 1.0 |
| 0.08 | 42 | +2.0 | +0.0 ± 1.3 |

This is within the arms' own bases. On today's main the fresh-seed effects were 0.72 of the atlas's (§0.3), so the
whole shelf is worth about +1.2 to +1.4 pp; the confirmed entries hold +1.3. Each half sees half the games, so this
reads low for the arms with few games, and the 1-sd null band is as wide as the gain.

### 4.4 Arms worth porting as switches, by confirmed gain

| arm | branch, size | confirmed entries (pp of `goal`) | maps by agent |
|---|---|---|---|
| KP3: the king plans its route over its own map | `t3/ms-arm-kp3` (`2dbed577`, on main `419fa32`): 5 files, +568 −6 (`KingPath.java`, `CellTracker`, `KingMiningStrategy`) | EscapeTheNight B +0.38, RUN both +0.19, pocketsofcheese B +0.17, houseofcards B +0.04 | KR: EscapeTheNight, houseofcards (KR's `KING_HAND` 1 is the cheap alternative on EscapeTheNight B); SG: pocketsofcheese; CM: RUN |
| `MINES_KING2` per map | `Tune.java`, needs a book-settable constant | toomuchcheese 7 +0.15, wallsofparadis 7 +0.08, cheddarchokehold 3 +0.11, pipes 7 +0.03 | KR: cheddarchokehold; toomuchcheese, wallsofparadis, pipes: no agent's list |
| K2 D': kings remember cat footprints, no mining step into a known lane (closed.md K2 D') | `t3/k2-proj` (`502c01d9`), 34 merges behind main: rebase and test on today's main first | bed B +0.30, A +0.10 against its own base | KR: bed |
| `SPAWN_RICH` per map | `Tune.java`, book-settable constant | t_minus_five_minutes 1500 +0.08, TheHeist 1500 +0.05 (on plain main), safelycontained 1500 +0.04; warhammer and peaceinourtime A failed round 2 | PM: TheHeist, warhammer; CM: peaceinourtime; t_minus_five_minutes, safelycontained: no agent's list |

Not worth porting from this data: PC1's last-cat hold (reversed on the newer base), the E2 rat-cap variants and the
other arms main already holds (`excluded.txt`), and every micro or fight-weight arm (no map-seat structure).

## 5. Tools

**Rebuild main's table** (after a merge that changes the bot; the reference games of every run of the new main
count, so a fresh `goal` run of main gives ~100 games a pair per 10,000 games):

```sh
cd ~/battlecode26-data/scratch/at
python3 extract.py && venv/bin/python prep.py            # all runs -> games.pkl, seats normalised
venv/bin/python mainmap.py HASH mapseat_p.json OLD.json   # HASH: main's challenger class hash, run.json "bots"
```

`mainmap.py` keeps only games of that hash against awubot on `goal` maps, drops the outcome-selected list pools at
seed base 0, writes the table (same path and format) and `mapseat_main.tsv`, and prints the pairs that moved against
`OLD.json` (copy the current table there first). Today's table: hash `b9f344664968e099`.


In `~/battlecode26-data/scratch/at/` (Python: `venv/bin/python`, numpy, scipy, pandas): `extract.py` (runs →
`games.tsv`), `prep.py` (seat-normalised `games.pkl`), `units.py` (arm × base pairs, patch-ids), `atlas.py` (arms, the
model `fit2`), `armclass.py`, `inmain.py`, `merged_check.py` and `excluded.txt` (arms main already holds),
`fitall2.py` (fits, nulls, split-half), `shelf.py` (out-of-sample shelf gating), `shortlist.py`, `confirm.py`,
`confstats.py RUN` (a confirmation's paired effect per map and seat), `mainmap.py` (§3), `runs.txt` (run IDs).
