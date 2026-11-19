# RS leads: the E3 decoder and the pinned-king dirt

The two leads `docs/theory/rs-shelf.md` §5 left: why fixing the mine-squeak decoder read −1.2 pp, and AB defdirt with
the dirt kept while a cat pins the king. Main `99ef794` (PT 2d merged). Seed bases 15101–15104. Scratch:
`~/battlecode26-data/scratch/rs/e3/` (probe reader, analysis), `~/battlecode26-data/scratch/rs/wt/` (probe packages
and runner, never merged), `~/battlecode26-data/scratch/rs/tests.md` (every run).

**Result.** Neither lead holds a gain; nothing to merge.
- **The E3 decoder.** Fixing it changes nothing a rat or king knows or chooses: the kings' shared array spreads every
  mine whatever the squeaks decode. There is no gossip lever, so no arm was built. Its −1.2 pp screen was chance: a
  3,000-cell re-test on fresh seeds reads −0.0018 [−0.0068, +0.0028]. The bug is neither load-bearing nor worth fixing.
- **DP**, the defensive dirt kept only while a cat pins the king: screen +1.35 pp [+0.0, +2.7], confirmation +0.23 pp
  [−0.48, +0.93]: fails.

## 1. The E3 decoder: no mechanism

`Squeaker.processMessage` registers only the first of the three mines a gossip squeak packs (`squeakMine`); the fix
(`t3/rs-e3d`) reads all three. Six decisions read the known mines: the miners' early-explore rule (`knownMines <= 2 &&
time <= area/8`) and their mine lottery (`pickSeenMine`, weight = distance), the kings' mine choice, the formation
coordinator's known-mine gate (`MINE_THRESHOLDS`), E2's mine-poor cap (`isRichMap`) and CT's band-trap gate.

**Where the screen's loss sat** (`1119-173807-ab-915a`, seed base 15008, per-game series in `results.jsonl`):
≥ 8-mine maps −2.05 pp of wins (1,074 games), ≤ 4 mines −0.6, 5–7 +0.3: E2's cap (mine-poor maps only) is out.

**The probe.** `rs_e3m` (main) and `rs_e3f` (main + the fix) are main's generated sources under another package with
one addition, `Probe.mark()`, after the turn's last decision (in `RobotPlayer`'s `finally`, before `Clock.yield`):
each rat's known-mine count and current trip (to a known mine, exploring, none, with its target), each king's
known-mine count, as indicator strings. Skipped on a turn that ran over, and with < 2,000 bytecodes left. Played
natively with indicators kept on the screen's 1,000 cells, both seats (`rsplay.py`, 2,000 games a probe, runs
`run-1119-202009-bf590d`, `run-1119-202029-f7a770`); `E3Probe.java` reads each replay on the node.
- A probe game equals the recorded game until the first turn that overran (a short game: winner, round and cheese
  identical); after it the mark's round check spends a few bytecodes of the next turn and a bytecode-gated choice
  can flip (FirstDiff on `isnotgold` B: r547, a turn). 63 of 152 changed-cell games stayed identical. So the probes
  are a paired comparison of their own, with the same instrumentation on both sides, not the screen's games.

**What the fix changes** (2,000 game pairs; ≥ 8-mine maps, main / fix):

| | r0–50 | r50–100 | r100–200 | r200–300 | r400–600 |
|---|---|---|---|---|---|
| rats' known mines | 1.4 / 1.4 | 3.1 / 3.1 | 4.9 / 4.9 | 6.4 / 6.4 | 8.8 / 8.6 |
| rat turns under the early-explore rule | 82.5% / 82.3% | 45.8% / 45.4% | 15.8% / 15.6% | 2.4% / 2.4% | 0 / 0 |
| rat turns exploring | 97.3% / 97.3% | 90.9% / 90.8% | 77.5% / 77.4% | 66.6% / 66.6% | 60.0% / 60.1% |
| mine trips started a game | 0.9 / 0.9 | 2.8 / 2.8 | 8.1 / 8.2 | 8.8 / 8.7 | 17.3 / 17.1 |
| mine trips toward the enemy start | 2.9% / 3.0% | 5.2% / 5.2% | 14.5% / 14.6% | 24.5% / 24.3% | 32.6% / 31.9% |
| kings' known mines | 0.8 / 0.8 | 2.5 / 2.5 | 4.4 / 4.4 | 6.2 / 6.2 | 8.2 / 8.1 |

Rats' median first round with ≥ 3 known mines 254.5 / 252.3; formations by r300 0.29 / 0.30, by r600 0.51 / 0.51,
first formation median r311 / r298. The 4–7- and ≤ 4-mine maps are the same to the decimal. Bytecode, from the
screen's 296 changed-cell replays (PairBc on synced turns): rats +0.84 a turn, overruns 661 / 660; kings +0.3, 2 / 2.

- **Why nothing moves.** A mine reaches every robot through the kings' shared array: a king registers a mine it
  sees or hears and writes its queue every turn (`GlobalArray.postTick` → `writeCheeseMines`), and every robot reads
  it every turn (`preTick` → `readCheeseMines`). The decoded first slot is enough: `squeakMine` starts each squeak at
  another pending mine (`time % len`), so every mine leads some squeak, and it drops mines already in
  `globalCheeseMines`. Gossip decoding is redundant with
  the shared array, so neither "less gossip" nor "exploration that ends later where gossip arrives early" has a
  mechanism to act on; EXPLORE_P and the early rule stay as TN and TN2 left them. No arm built.
- **What the screen's loss was.** The fix changes 27% of the probe games at all (1,450 of 2,000 pairs identical);
  where it does, it is through a few bytecodes or a mine known a few rounds sooner, which diverges a game without a
  direction. On the probe pair −0.0105 [−0.0205, −0.0015] (25 better, 46 worse), but those are mostly the screen's
  own games, so not an independent read. The fresh-seed re-test decides (§1.1).

### 1.1 The re-test

`tools/games ab challenger@2fe95b8 awubot --pool goal --min-cells 3000 --max-cells 3000 --seed-base 15104 --remote`
(the fix rebased onto `99ef794`), `1119-202327-ab-975f`: **−0.0018 [−0.0068, +0.0028]** (104 better, 117 worse,
2,779 unchanged; main 0.834 on the cells). By richness: ≤ 4 mines −0.38 pp of wins, 5–7 −0.33, ≥ 8 −0.03, where the
screen's −2.05 sat. The decoder fix is neutral: neither the gain E3's three harm reads suggested nor the harm the RS
screen did. docs/closed.md's row is corrected.

## 2. DP: defensive dirt only while pinned

DD (AB's deletion of `placeDefensiveDirt`) read +0.018, +0.001, +0.0077 [−0.0003, +0.0165] on three `goal` seed bases
and lost `bed` in both RS runs, where the dirt shields a king a cat pins. DP (`t3/rs-dp` `e20c33c`) keeps it there
only: the king places defensive dirt toward a cat only while pinned in CL's sense (docs/theory/cl-catlane.md §5:
≥ 3 block scratches, each within 6 rounds of the last, at room ≤ 13). In the bot, `KingCombatStrategy.trackPin`
counts the king's turns that start with HP lost while a cat touching its 3×3 holds it in its forward lane
(`laneCross`), and `isPinned` counts free king centres in the 5×5 box (`CellTracker.kingCentres`, CL V's helper).
A pinned EXPLORE cat digs dirt on its front tiles before it scratches (rules §12), so each dirt tile it meets there
costs it a scratch.

| test | seed base | cells | diff [95%] | better / worse | run |
|---|---|---|---|---|---|
| firing check: bed, tminusten, sunandmoon, externalsustenance, peaceinourtime, RUN | 15101 | 12 | −0.125 [−0.417, +0.083] | 1 / 3 | `1119-194847-ab-16f3` |
| screen | 15102 | 1,000 | **+0.0135 [+0.000, +0.027]** | 76 / 52 | `1119-200228-ab-d976` |
| confirmation | 15103 | 3,000 | +0.0023 [−0.0048, +0.0093] | 195 / 188 | `1119-200804-ab-326e` |

- The firing check (CcStats on its 48 replays): our kings placed 102 dirt tiles against main's 497 in 24 games; 0
  against 296 on RUN, 5 against 63 on sunandmoon, about as many as main on tminusten (30 / 23) and peaceinourtime
  (33 / 35), the pinned maps; on bed 29 against 58 and 1 of 4 won against 4 of 4.
- bed: screen 6.5 against main's 7.0 of 11 cells, confirmation 18.5 against 18.0 of 31: DD's loss there is gone.
- DP and DD both read about +0.5 pp over their runs (DP +1.35 and +0.23; DD +0.1 and +0.77). If that is real it is
  under what a 3,000-cell confirmation resolves (±0.7 pp), and neither passed. Not recommended; no smoke or gauntlet.

## 3. Data and tools

- `~/battlecode26-data/scratch/rs/wt/` (a detached worktree of `99ef794`): `src/rs_e3m`, `src/rs_e3f` (the probe
  packages: `Probe.java`, its call in `RobotPlayer`'s `finally`, `MinerStrategy.probeTarget`), `rsprobe/rsplay.py`
  (`tools/run --kit --with rsprobe --each SPECS --chunk 4 --gather e3.jsonl --gather results.jsonl -- python3
  rsprobe/rsplay.py {file} {out} BCX_GZ rsprobe/cls`: plays with indicators, reads each replay on the node, keeps one
  line a game), `rsprobe/specs_*.jsonl`.
- `~/battlecode26-data/scratch/rs/e3/`: `E3Probe.java` (the reader), `e3an.py E3_M E3_F RES_M RES_F` (§1's table),
  `mkspecs.py RUN OUT [--changed]` (probe specs for a run's cells), `series.py RUN` (a paired run's per-game series by
  richness), `kfpair.py` (KFStats rows, arm against ref), `exact.py RUN PROBE_RESULTS ROLE` (does a probe reproduce a
  run's games), `DivergeNoInd.java` (CB's Diverge, indicator actions ignored, with team and position).
- A probe that only adds indicator strings after the turn's last decision still diverges from the bot at the first
  turn that ran over the bytecode limit: compare a probe with its own twin, never with the bot's games.
