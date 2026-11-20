# PM: the maps hard from both seats

Package PM of wave 6: for the maps where main loses from both seats, the loss classes on fresh seeds and the per-map
policy that wins there, as book entries (tools/book/AGENTS.md). Branch `t3/pm-per-map-tuning`; the seat-B rule on
`t3/pm-symb`. Tools: `~/battlecode26-data/scratch/pm/` (end of this file).

## 1. Result

| map | seat | entry | chosen on (seed base, cells): ours − main | confirmed on (seed base, cells): ours − main |
|---|---|---|---|---|
| TheHeist | both | `PM_SYM` 1 and `PM_COOP_HOLD` 1 | 24003, 300: A +0.31 ± 0.07, B +0.35 ± 0.07 | 24006, 300: A +0.33 ± 0.06, B +0.32 ± 0.06 (0.81 / 0.90 against 0.48 / 0.58) |
| groveisbeingdesecrated | both | `PM_COOP_HOLD` 1 | 24003, 148: A +0.08 ± 0.08, B +0.11 ± 0.08 | 24007, 300: A +0.10 ± 0.04, B +0.11 ± 0.04 (0.87 / 0.67 against 0.77 / 0.56) |
| longwayaround | both | `SPAWN_CAP` 60 | 24009, 250: A +0.09 ± 0.08, B +0.10 ± 0.08 | 24010, 300: A +0.05 ± 0.07, B +0.07 ± 0.07 (0.62 / 0.70 against 0.57 / 0.63) |
| Nofreecheese | both | `PM_SIEGE` 1 | 24012, 100: A +0.06 ± 0.10, B +0.10 ± 0.09 | 24013, 200: A +0.11 ± 0.06, B +0.13 ± 0.06 (0.90 / 0.91 against 0.79 / 0.79) |

Intervals are 95%, paired per game against main on the same (map, seed, seat). Run IDs in §6.

- `PM_SYM`: on a book map-seat the robots take `Book.symmetry` from round 1 (`CellTracker.setSymmetry`).
- `PM_SYM_B` (general, no table): seat B's king reads the symmetry from the order of its first `senseNearbyMapInfos`
  (chirality 1 reverses y on HORIZONTAL maps, x on VERTICAL, both on ROTATIONAL; docs/rules.md §7) and shares it as it
  shares an inferred one. Checked in probe games on one map of each symmetry.
- `PM_COOP_HOLD`: PC1's A2 ported to today's main (`robot/LastCat.java`): in cooperation with one king, no bite or cat
  trap that could leave a cat at or below our own damage; rats remove their own cat traps once those threaten a cat in
  view; with two kings we may end the game.
- `PM_SIEGE` (E2b's siege spawns): with an enemy in the king's vision (1), or more enemies than our rats there (2), the
  king spawns whatever the army size.
- Every default is main's instructions (javap, only `Tune.class` differs).

## 2. Loss classes now (main, seed base 24001, 150 games a map-seat, `1119-212800-ab-9092`)

| map | win A / B | losses (A + B) |
|---|---|---|
| TheHeist | 0.54 / 0.56 | all cooperation endings (135), r150–400, kings 1 v 1 in 100, 1 v 2 in 35 |
| groveisbeingdesecrated | 0.79 / 0.50 | cooperation endings 72 (kings 1 v 1: we win 3 of 64), king kills r450–1500 28 |
| longwayaround | 0.53 / 0.59 | points at r2000 85, king kills after r1500 35, r450–1500 12; at r2000 awubot has transferred ~12,800 more and holds 56 rats to our 16 |
| therainbow | 0.59 / 0.63 | king kills before r450 (112): our alive rats 9.2 against 13.0, our king starving ~21 turns a loss (0.5 in wins) |
| Nofreecheese | 0.79 / 0.73 | king kills before r450 62, r450–1500 10 |
| DefaultSmall | 0.81 / 0.77 | king kills before r450 (63) |
| Excavation | 0.78 / 0.83 | king kills before r450 (58) |
| externalsustenance | 0.77 / 0.83 | king kills before r450 (61) |
| warhammer | 0.76 / 0.86 | king kills r450–1500 (57) |
| cheeseysquare | 0.78 / 0.83 | king kills r450–1500 21, after r1500 24, points at r2000 13 |
| insertmeowname | 0.81 / 0.91 | points at r2000 24, king kills r450–1500 16 |

The W3 pooled numbers (56 games a pair) were lower for DefaultSmall, Excavation, cheeseysquare, externalsustenance and
insertmeowname: chosen as the worst, they regressed.

## 3. TheHeist: the symmetry lets the cooperation king form

- Each team has a base strip with its king, one mine and one cat, opening on its own corridor of 8 mines; the two
  corridors meet only at the far left. Every game ends in cooperation once both cats die (the team that kills second
  ends it); at 1 king each the result is the cheese transferred (164 of 169 won with a positive margin).
- Main never resolves this map's symmetry (all three stay possible all game: walls never contradict any). KB's
  cooperation king forms only on a mine on our side under every symmetry still possible, so the corridor mines never
  qualify, and its search finds no site (probe: `nosite` from r100 to the end, 8 of 8 games).
- Given the symmetry, the site is a corridor mine: the formation starts at r155–170 in 6 of 8 probe games and the second
  king stands by r180–260 in 3 of them. 2 kings to 1 wins the cooperation ending (+10 points).
- The hold adds to it: given the symmetry, hold − no hold +0.075 ± 0.027 (24003) and +0.100 ± 0.029 (24006). Alone it
  is −0.41 ± 0.10: without a second king, a held cat runs the game on to an ending we lose.

## 4. grove: the hold

- Our king and our cat share a closed strip; the mines lie in a dirt field beyond a gap. KB's cooperation formation
  starts at r100–140 at a field mine and does not complete before both cats are dead (~r185, probe 8 of 8 lost games),
  so most endings are 1 king to 1 on cheese, where awubot leads. Under the hold the cats survive, the game backstabs or
  forms the second king: cooperation endings at 1 v 1 vanish, and the losses are king kills r900–1200.
- `PM_SYM` on grove (main resolves its symmetry by r300): no gain (+0.04, both seats pooled, 24003).

## 5. longwayaround: a larger army

- A 60×60 map with 20 mines, decided at r2000 on points or by late king kills: awubot transfers ~12,800 more cheese and
  holds 56 rats to our 16. The rat cap (`SPAWN_CAP`, 40 = 16 rats) binds. Dose (ours − main): cap 50 +0.055 ± 0.063
  (24005, 200 cells), 60 +0.096 ± 0.055 (24009), 80 +0.055 ± 0.047 (24010; A −0.02, B +0.13); the rich-spawn line
  (`SPAWN_RICH` 1000: +0.059; 500: +0.023) and cap 50 with line 1000 (+0.066) less.
- The same cap is harmful on the other hard maps (§6).

## 6. Nofreecheese: siege spawns

- An open 60×60 map with 6 mines; main loses it to early king kills (r < 450). E2b's siege spawns (no rat cap while an
  enemy is in the king's vision) closed on `goal` at zero (RS: −0.001 on 3,000 cells) but win here on both seats on two
  seed bases. Matrix `pmx2` (6 early-kill maps): externalsustenance read +0.05 and did not confirm (+0.030 ± 0.035, 24013);
  DefaultSmall loses with it (A −0.15, B −0.13: E2's mine-poor bank race).

## 7. What did not gain

| arm | maps | seed base, cells | ours − main |
|---|---|---|---|
| `PM_SYM` (symmetry from round 1, both seats) | therainbow | 24003, 300 | −0.123 ± 0.062 (A −0.15, B −0.09) |
| `PM_SYM` | longwayaround | 24003, 300 | −0.023 ± 0.065 |
| `PM_SYM` | the 8 rotational maps of §2 | 24003, 87 a map | −0.022 ± 0.039 |
| `PM_SYM` | the 27 VERTICAL `goal` maps | 24004, 10 a map | −0.031 ± 0.038 |
| `PM_SYM_B` (seat B, general) | `goal` | 24008, 1,000 | −0.002 [−0.015, +0.011]; seat B −0.004 ± 0.025; seat B where main has no symmetry by r300 −0.027 ± 0.044 |
| `PM_SYM` | the 48 ROTATIONAL `goal` maps | 24004, 10 a map | −0.014 ± 0.027 |
| `PM_COOP_HOLD` | TheHeist | 24003, 106 | −0.406 ± 0.104 |
| `DIRT_ROUND` 0 / 50 (main 100) | therainbow, Excavation, DefaultSmall | 24014, 150 a map | Excavation −0.15 / −0.13 (both seats pooled), others within ±0.08, none significant |
| `SPAWN_CAP` 60, `SPAWN_RICH` 1500, `MINES_KING2` 7 | the 8 other hard maps | 24011, 60 a map | no map-seat significant; `SPAWN_CAP` 60 down to −0.48 |
| `PM_SIEGE` 1 / 2 | therainbow, DefaultSmall, Excavation, externalsustenance, warhammer | 24012, 100 a map | within ±0.06 except DefaultSmall S1 −0.14; externalsustenance S1 +0.05 did not confirm (+0.030 ± 0.035, 24013) |
| `SPAWN_POOR_CUT` 0 / 20 (main 10) | therainbow, DefaultSmall, Excavation | 24005, 150 a map | −0.086 ± 0.041 / −0.369 ± 0.041; worse on each map |
| `PM_COOP_HOLD` | longwayaround, therainbow, 7 rotational maps besides grove | 24003 | 0 (backstab maps: the hold never binds) |

Seat A's symmetry from the book is therefore no broad lever: it helps where main never resolves the symmetry and a
choice hangs on it (TheHeist's cooperation king), and costs where it changes routes (therainbow).

## 8. Runs

| purpose | seed base | runs (main, variants) |
|---|---|---|
| baseline, loss classes, all replays (kept) | 24001 | `1119-212800-ab-9092` |
| TheHeist choose | 24003 | main `1119-214234-ab-4404`; S `-220856-ab-3f11`, H `-214946-ab-b367`, SH `-215446-ab-afd0` |
| TheHeist confirm | 24006 | main `1119-224242-ab-2475`; S `-224242-ab-8e76`, SH `-224246-ab-4682` |
| 8 rotational maps choose | 24003 | main `1119-214235-ab-9a21`; S `-220336-ab-ed1b`, H `-215149-ab-29ad`, SH `-215924-ab-f8e8` |
| grove confirm | 24007 | main `1119-225213-ab-8bfc`; H `-225213-ab-6ae7` |
| longwayaround, therainbow choose | 24003 | main `1119-214235-ab-de0c`; S `-215446-ab-acf6`, H `-214946-ab-5273`, SH `-220447-ab-c246` |
| longwayaround constants | 24005, 24009 (choose), 24010 (confirm) | campaigns `clong`, `clong2`, `clong3` (`tools/tune show NAME`); confirm main `1119-234728-ab-3677`, cap 60 `-234728-ab-8a7d` |
| constants matrix, 8 maps | 24011 | `pmx1` (`tools/book/book fit pmx1`) |
| mine-poor cut | 24005 | `cpoor` |
| siege matrix, 6 maps | 24012 | `pmx2`: main `1120-001937-ab-3694`, S1 `1120-002428-ab-3f20`, S2 `1120-002512-ab-4910` |
| siege confirm, Nofreecheese and externalsustenance | 24013 | `pmx3`: main `1120-004152-ab-9935`, S1 `1120-004152-ab-cfef` |
| `PM_SYM_B` `goal` screen | 24008 | `1119-230521-ab-e731` |
| `PM_SYM` on `goal` by symmetry class | 24004 | VERTICAL main `1119-223502-ab-3235`, S `-223502-ab-7f14`; ROTATIONAL main `-223502-ab-5dd4`, S `-223503-ab-c5fe` |

## 9. Open

- therainbow (0.59 / 0.63), still without an entry: losses are king kills before r450 in which our king starves (239 HP
  a loss, 1 in wins) and is bitten (166): the bank race at the two shared mines between the kings. The rat cap (both
  directions), digging earlier, siege spawns and the symmetry all failed; the lever would be the fight for the shared
  mines (W3's exchange), not a constant.
- DefaultSmall, Excavation, externalsustenance (early king kills) and warhammer, cheeseysquare, insertmeowname (mid
  game, points) win 0.76–0.91 on fresh seeds: no constant or closed arm tried moves them.
- Not tried: KF's site arms and E3's miner arms per map (both read zero where they were measured, on rich maps), PT's
  delivery deadline on the r2000 maps (harmful on `goal`).

## 10. PM2: the maps lost on points

From main's 3,000-cell `goal` run of 20 Nov (`1120-022339-ab-124d`, 86.9%), per map-seat: which points term awubot
leads in our points losses (docs/rules.md §1: backstab 30 cat damage, 50 kings, 20 cheese transferred; cooperation 50,
30, 20). `scratch/pm/pts.py RUN MAP...` prints it.

| map | the term awubot leads in our points losses |
|---|---|
| uneruesansfin, cheesebottles, isnotgold, rift, FreeTheCheese | cat damage (uneruesansfin B 9 of 10, isnotgold 8 of 8) |
| t_minus_five_minutes | cat damage in cooperation (12,000 to our 4,000), kings 2 to 1 ours |
| where_are_you | kings (2 to 3 in 14 of 17) |
| longwayaround, insertmeowname, cheeseguardians, grove, TheHeist A | cheese transferred |

Entries (both runs against the main of their day; ours − main, 95%):

| map | seat | entry | chosen on (seed base, cells, candidates) | confirmed on (seed base, cells) |
|---|---|---|---|---|
| t_minus_five_minutes | both | `CAT_DEF_DSQ` 100 | 24017, 200, 2: A +0.030, B +0.055 | 24019, 300: A +0.060 ± 0.047, B +0.047 ± 0.053 |
| uneruesansfin | both | `CM_CAT_HUNT_START` 500 | 24015, 150, 3: A +0.053, B +0.120 | 24024, 200: A +0.045 ± 0.045, B +0.160 ± 0.064 |
| cheesebottles | both | `CM_CAT_HUNT_START` 500 | 24015, 150, 3: A +0.093, B +0.087 | 24024, 200: A +0.135 ± 0.055, B +0.095 ± 0.047 |
| isnotgold | both | `CAT_DEF_DSQ` 100 | 24015, 150, 3: A +0.040, B +0.093 | 24024, 200: A +0.070 ± 0.045, B +0.095 ± 0.053 |
| rift | both | `CAT_DEF_DSQ` 100 | 24015, 150, 3: A +0.093, B +0.093 | 24024, 200: A +0.105 ± 0.053, B +0.105 ± 0.051 |
| where_are_you | A | `PM_SKIP_FAILED` 2 | 24022, 200, 5: A +0.060 ± 0.048 | 24026, 300: A +0.093 ± 0.048 (B +0.027 ± 0.038) |

- Cat maps: the hunt (PT 2d, from r1300 on main) or the cat defence round our king (d² 36 on main) gives our rats more
  of the cat damage. On t_minus_five_minutes every game ends in cooperation near r350 with us ahead on kings; the hunt
  never runs there (it needs global > 1,000 and 50 quiet rounds), so the defence is the lever.
- where_are_you: the lock-in's gates do not bind. The third king's formation restarts at the site that just timed out,
  up to six times in a row, at contested mid-map or far sites (probe of 8 lost seeds, all reproduced). `PM_SKIP_FAILED`
  (KF's arm T, closed as a general rule): the coordinator skips the mines whose formation timed out, 1 the last one, 2
  every one (`robot/king/FailedSites.java`). Games ending at 3 kings: A 143 → 165 of 200, B 126 → 153. The pattern
  (kings ≤ 2 from r800 with the army at its cap, awubot ahead on kings) is 11 of the 21 such losses in the 3,000 cells;
  no other map-seat has more than 2.
- RUN: on main with `KING_PATH` 1 it wins 0.970 / 0.975 (24023); no candidate moved it.
- Closed (docs/closed.md): the lock-in constants, the hunt in cooperation (`PM_HUNT_COOP`, never fires; removed),
  the formation timeout and join distance on where_are_you, longwayaround B cap 80, the cat levers on cheeseguardians
  and insertmeowname in both directions, RUN.
- Runs: matrices `pmc1` (24015), `pmk1` (24016), `pmt1` (24017), `pml1` (24018), `pmt2` (24019), `pmc2` (24020),
  `pml2` (24021), `pmk2` / `pmk3` (24022), `pmr1` (24023), `pmc3` (24024), `pmcg` (24025), `pmk4` (24026);
  `tools/tune show NAME`, `tools/book/book fit NAME`. Every run an entry cites is kept.

## Tools

`~/battlecode26-data/scratch/pm/`: `seat.py RUN` (loss classes per map-seat from results.jsonl), `pair.py REF RUN...`
(paired per map-seat differences), `coop.py RUN MAP` (endings by king counts), `ends.py RUN MAP`, `java/MapDump`
(ASCII map and its symmetry), `java/TeamTL` (per-team 10-round series from replays), `java/KingStarts`,
`goal_sym.txt`; probe worktree `wt` (branch `t3/pm-probes`, never merged: coordinator prints, played locally with the
tools/play shim that keeps robot output).
