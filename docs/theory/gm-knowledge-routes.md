# GM: map knowledge and routes without the book

Track GM of the clean program (docs/program.md): the book's map knowledge (`CM_MINE_BOOK`, `AT_MAP`, `PM_SYM`) and its
per-map route switches (`KING_PATH`, `SG_LOOK_RIGHT`, `KING_HAND`) replaced by what robots observe. Seeds 34000-34999.
Data and tools: `~/battlecode26-data/scratch/gm/` (`D:`; `runs.txt` every run, `py/bymap.py` a run's per-map table
split by W4's map features, `py/symcons.py` which symmetries each map's walls, mines and dirt fit). Map features:
docs/theory/w4-map-features.md (branch `t3/w4-clean-step-back`), `~/battlecode26-data/scratch/w4/mapfeat.json`.

## 0. Summary

1. **Full mine knowledge is worth +2.4 pp, half of it in r1–150** (§1). Oracle: every baby rat knows every mine
   from round 1 on all 95 `goal` maps (CM8's book on every map): +0.024 [+0.005, +0.044] and +0.024 [+0.003, +0.046]
   on two seed bases of 1,000 cells; the same mines from round 150 only: +0.011 [−0.006, +0.029]. The class splits
   move between the bases (small maps +0.005 on one, +0.042 on the other); large maps with ≤ 6 mines gain on both
   (+0.078, +0.045).
2. **The general version: mirror the mines by the symmetry** (`Tune.GM_MINE_MIRROR`, `MineMirror`). The engine pairs
   every cheese mine with its image under the map's symmetry (docs/rules.md §5: each mine spawns cheese together with
   its pair), so once a rat knows the symmetry (main's wall check, CellTracker), each known mine's image is a mine. The
   rats add it to their known mines; kings keep what they see (as CM8). Screen: +0.015 [+0.001, +0.029] on every map;
   only on maps of more than 900 tiles (`GM_MIRROR_AREA` 900; the 73 `goal` maps that are not small): **+0.018
   [+0.005, +0.030]**, chosen as the best of eleven arms on those cells. **On 3,000 fresh cells it reads +0.005
   [−0.003, +0.013]: not confirmed** (§2). By class on the fresh cells: large +0.003, medium +0.013, small −0.004;
   large ≤ 6 mines +0.007 (the screen read +0.026 there, the oracle +0.078).
3. Closed (§3): mines mirrored for kings too; the symmetry from mines (a seen mine whose image tiles are seen and hold
   no mine rules that symmetry out); exploration toward mines' possible images; the chirality-1 king's round-1
   symmetry with the mirror (not run); the king's bug choosing its hand by the rats' two-sided lookahead (+0.000);
   the explore-only opening shortened (round W·H/16: +0.002) or ended at 2 known mines (+0.015, unconfirmed);
   `EXPLORE_P` 0.2, alone −0.027 [−0.045, −0.010] on these cells, with the mirror +0.003.

## 1. What the oracle says

`gm-oracle-mb` 173e5ebe (never merge): book entries `CM_MINES` + `CM_MINE_BOOK` 1 on every `goal` map, both seats
(MineBook: baby rats load the map's mines at their first turn; the explore-only opening ends at once). Seed base 34001,
1,000 cells, `1121-034621-ab-4308` (kept): 186 cells better, 144 worse.

| class (W4) | maps | oracle | mirror (m1) | mirror, maps > 900 tiles (m1L) |
|---|---|---|---|---|
| large (> 1,900 tiles) | 41 | +0.040 | +0.022 | |
| medium | 32 | +0.017 | +0.015 | |
| small (≤ 900) | 22 | +0.005 | −0.000 | |
| large, ≤ 6 mines | 13 | +0.078 | +0.022 | |
| large, > 6 mines | 28 | +0.022 | +0.022 | |
| < 2.5 mines per 1,000 tiles | 22 | +0.060 | +0.019 | |

Largest per map (about 10 games each): RUN +0.41, kingofthehill +0.35, jail +0.25, hatefullattice +0.25,
therearenowalls +0.18, shell +0.14. The mirror recovers the oracle's gain on the large mine-rich maps and about a
quarter of it on the large sparse ones: there both sides' mines lie far from where the rats look, so the mines nobody
has seen are what the oracle adds.

**Replicated and decomposed** (seed base 34102, 1,000 cells each, same cells): the oracle from round 1 +0.024
[+0.003, +0.046] (`1121-103755-ab-78f3`); the same mines handed over only at round 150 (`gm-oracle-mb150` eb45dec8,
`MineBook.load` at the first turn from r150) +0.011 [−0.006, +0.029] (`1121-104604-ab-fe0b`). By class, r1 / r150:
large ≤ 6 mines +0.045 / +0.012, small ≤ 6 mines +0.062 / +0.017, medium > 6 mines +0.032 / +0.026, large > 6
mines +0.014 / +0.006. About half of what mine knowledge is worth is in the first 150 rounds, and on the mine-poor
maps nearly all of it: the rounds before the rats have seen the far mines, or the symmetry that would mirror them
(on jail the first rat knows the symmetry at r53, both own mines' images at r89). Observation cannot supply mines
nobody has seen by then; the mirror supplies images of seen mines only, after the symmetry.

## 2. The candidate

Branch `t3/gm-mirror-cand` 5bf9342f: `GM_MINE_MIRROR` 1, `GM_MIRROR_AREA` 900 on `t3/clean-gm-map-knowledge` 1958f2b1.

- Mechanism: `MineMirror.sync()` from `RobotPlayer`'s pre-tick, baby rats only, maps of more than 900 tiles: when the
  symmetry is known, the image of every known mine not yet mirrored joins `Game.knownCheeseMines` (on the 2×2 grid of
  `encode2x2`: exact on even sides, at most one cell off on odd ones). The explore-only opening ends sooner (it holds
  while ≤ 2 mines are known) and trips draw over our side's and the enemy side's mines with main's distance weight.
- Where the rule holds: every `goal` map over 900 tiles where the rats learn the symmetry (73 maps over 900 tiles;
  the symmetry is resolved by walls on most of them, never on TheHeist).
- Off, it plays as main: javap of every class against main differs only in `Tune` and the new `MineMirror`.

| run | seed base | cells | ours − main |
|---|---|---|---|
| screen m1L (arm 75ffd05a) | 34001 | 1,000 | +0.018 [+0.005, +0.030] (`1121-095127-ab-75e6`) |
| confirmation (5bf9342f) | 34101 | 3,000 | **+0.005 [−0.003, +0.013]** (`1121-101938-ab-fff6`, 261 better, 235 worse): not confirmed |
| smoke (5bf9342f) | – | 8 | no robot killed by its own exception (`1121-103015-ab-d965`) |
| `teammade` (5bf9342f) | 34201 | 600 | −0.018 [−0.042, +0.003] (`1121-102944-ab-7e47`; 49 better, 66 worse) |

## 3. Closed

All on the same 1,000 `goal` cells (seed base 34001), each arm against main.

| arm | change | run | ours − main |
|---|---|---|---|
| m1 | mirror for rats, every map | `1121-034622-ab-1c15` | +0.015 [+0.001, +0.029] |
| oracle150 | the oracle's mines only from round 150 | `1121-104604-ab-fe0b` (34102) | +0.011 [−0.006, +0.029] |
| m2L | m1L for kings too (`GM_MINE_MIRROR` 2) | `1121-102101-ab-e1ed` | +0.006 [−0.010, +0.022] |
| m1Ls | m1L + `PM_SYM_B` 1 (the chirality-1 king's symmetry from its first scan) | `1121-102105-ab-f0ce` | +0.013 [−0.002, +0.029] |
| m1sm | m1 + symmetry from mines (`checkByMines`, arm only) | `1121-034622-ab-b25d` | +0.006 [−0.010, +0.023] |
| m1smx | m1sm + half of exploration trips to unseen images of known mines (arm only) | `1121-095127-ab-a7b8` | +0.004 [−0.015, +0.024] |
| kl | the king's bug picks its hand by the rats' lookahead (both 5-step bugs, then the score draw) | `1121-034623-ab-c595` | −0.002 [−0.020, +0.015] (102 better, 103 worse) |
| e16 | explore-only opening to round W·H/16 (main W·H/8) | `1121-095128-ab-9efc` | +0.002 [−0.010, +0.015] |
| e1 | explore-only opening while ≤ 1 mine is known (main ≤ 2) | `1121-095129-ab-3ddd` | +0.015 [−0.001, +0.031] |
| p2 | `EXPLORE_P` 0.2 | `1121-095129-ab-d610` | −0.027 [−0.045, −0.010] |
| m1p2 | m1 + `EXPLORE_P` 0.2 | `1121-095128-ab-e2eb` | +0.003 [−0.017, +0.021] |
| m1Le1 | m1L + e1 | `1121-101236-ab-88a3` | +0.011 [−0.009, +0.028] (below m1L alone) |

- The symmetry from mines resolves wall-free maps (therearenowalls by r108 in a local game, where main never resolves
  it), but on `goal` it reads below the plain mirror. Of the 95 maps only 7 have walls that fit two symmetries
  (`D:py/symcons.py`); main's wall check resolves the rest once rats have seen both sides of an axis.
- `EXPLORE_P` 0.2 by class (p2 / m1p2 / m1): mines > 8: −0.054 / −0.023 / +0.004; < 2.5 per 1,000 tiles: −0.004 /
  +0.030 / +0.019; large: −0.020 / +0.014 / +0.022; small: −0.031 / −0.011 / −0.000. Fewer exploring trips cost on
  mine-rich maps; with the mirror they help only on the sparse ones (track GE reads the share on its own base).
- The king's lookahead: arm `gm-arm-kl`; smoke clean (`1121-030351-ab-645b`). A fixed hand per team (KR's h7) and the
  lookahead's hand read the same on average, as KR's and SG's general rules did.
