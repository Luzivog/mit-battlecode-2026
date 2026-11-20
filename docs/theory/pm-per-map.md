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
| groveisbeingdesecrated | A | `SPAWN_CAP` 60 | 24028, 200, 3: A +0.070 ± 0.051 | 24031, 300: A +0.053 ± 0.041 (0.94 vs 0.89) |
| groveisbeingdesecrated | B | `SPAWN_CAP` 80 | 24028, 200, 3: B +0.160 ± 0.071 | 24031, 300: B +0.123 ± 0.056 (0.87 vs 0.75) |

- Cat maps: the hunt (PT 2d, from r1300 on main) or the cat defence round our king (d² 36 on main) gives our rats more
  of the cat damage. On t_minus_five_minutes every game ends in cooperation near r350 with us ahead on kings; the hunt
  never runs there (it needs global > 1,000 and 50 quiet rounds), so the defence is the lever.
- where_are_you: the lock-in's gates do not bind. The third king's formation restarts at the site that just timed out,
  up to six times in a row, at contested mid-map or far sites (probe of 8 lost seeds, all reproduced). `PM_SKIP_FAILED`
  (KF's arm T, closed as a general rule): the coordinator skips the mines whose formation timed out, 1 the last one, 2
  every one (`robot/king/FailedSites.java`). Games ending at 3 kings: A 143 → 165 of 200, B 126 → 153. The pattern
  (kings ≤ 2 from r800 with the army at its cap, awubot ahead on kings) is 11 of the 21 such losses in the 3,000 cells;
  no other map-seat has more than 2.
- grove (on top of the hold): seat B's king-kill losses hold our army at the cap (16 rats) for hundreds of rounds while
  awubot's grows to 80–120 from the field mines; a higher cap is the lever (A 60, B 80).
- Stopped, the next step costing more than the map-seat returns: longwayaround (the cheese term from r500 as our army
  shrinks 44 → 22 rats against awubot's 46 → 51, awubot's third king first; cap, rich-spawn line, lock-in and king
  target all flat: what is left is the rat exchange), where_are_you B (0.91 with the site skip; the rest is the same
  missing third king), TheHeist A (holding to a third king moves the losses to r2000), insertmeowname (8 losses of
  64; the second king's mine gate does not bind).
- RUN: on main with `KING_PATH` 1 it wins 0.970 / 0.975 (24023); no candidate moved it.
- Closed (docs/closed.md): the lock-in constants, the hunt in cooperation (`PM_HUNT_COOP`, never fires; removed),
  the formation timeout and join distance on where_are_you, longwayaround B cap 80, the cat levers on cheeseguardians
  and insertmeowname in both directions, RUN.
- Runs: matrices `pmc1` (24015), `pmk1` (24016), `pmt1` (24017), `pml1` (24018), `pmt2` (24019), `pmc2` (24020),
  `pml2` (24021), `pmk2` / `pmk3` (24022), `pmr1` (24023), `pmc3` (24024), `pmcg` (24025), `pmk4` (24026), `pml3` (24027),
  `pmg1` (24028), `pmh2` (24029), `pmi1` (24030), `pmg2` (24031);
  `tools/tune show NAME`, `tools/book/book fit NAME`. Every run an entry cites is kept.

## 11. PM3: houseofcards B, diamond B, EscapeTheNight B, mushroom A

Main on seed base 24032, 60 cells a map, every replay kept (`1120-074134-ab-629e`); replay readers
`scratch/pm/java/Opening.java` (the first kings' positions every 25 rounds to r300, first hit, first crowd of 3 enemy
rats within d² 36, alive rats), W1's `MineControl` (cheese picked per mine and window), `TeamTL` (per-team series),
`KingBc` (the first kings' bytecode in rounds 1–3); `scratch/pm/pm3sum.py` summarises them.

| map-seat (win A / B) | how the worse seat loses | entry |
|---|---|---|
| houseofcards B (0.58 / 0.40) | 30×30, 2 mines: E2's mine-poor cut and the small-map cap leave us 8 rats against awubot's 12; by r100 we have delivered 35–64 against 118–208, the bank is empty by r200, the king falls before r450 to bites, throws and starvation | `SPAWN_CAP_SMALL` 40 (12 rats) and the first king at the far mine (11, 4) |
| diamond B (0.93 / 0.62) | even to r500; the losses keep one king all game and the bank drains after r500 (starvation 341 HP a loss); in wins our second king stands by r500 | the first king at the inner mine (28, 12) |
| EscapeTheNight B (1.00 / 0.65) | a race of the two first kings for the centre column: in our wins ours sits at x 22–26 by r300 and awubot's stays home, in our losses awubot's is there first and ours stops at x 29–35; our deliveries start late (0 by r100), the bank falls to 175 by r300 | none |
| mushroom A (0.58 / 0.78) | the bank falls faster from the start (888 against 1,180 at r100, 221 against 642 at r200), stays near 240, the king starves late (528 HP a loss) | none |

Entries (ours − main on the same (map, seed, seat), 95%):

| map | seat | entry | chosen on (seed base, cells, candidates) | confirmed on (seed base, cells) |
|---|---|---|---|---|
| houseofcards | B | `SPAWN_CAP_SMALL` 40 | 24033, 100, 3: +0.200 ± 0.131 | 24037, 200: +0.175 ± 0.095 (0.53 vs 0.35) |
| houseofcards | B | `KING1_SITE` (11, 4), `CM_KING1_SITE` 1 | 24039, 100, 3: +0.120 [−0.012, +0.252] | 24041, 150, against the cap-40 variant: +0.160 ± 0.108 (0.69 vs 0.53); with the cap, against main +0.407 ± 0.100 |
| diamond | B | `KING1_SITE` (28, 12), `CM_KING1_SITE` 1 | 24039, 100, 3: +0.180 [+0.061, +0.299] | 24040, 120: +0.125 ± 0.100 (0.70 vs 0.57) |

- The king sites are CM's `KING1_SITE` (branch `t3/cm4`): the first king records its site in round 1 and walks from
  round 2. Chosen on CM's first code (which overran round 1), confirmed on its fixed code (`a3c32f99`); the first king's
  turns in rounds 1–3 on the fixed code: diamond B 13,053 / 11,330 / 11,988, houseofcards B 14,658 / 12,271 / ≤ 13,633.
- Failed (docs/closed.md): EscapeTheNight B `KING1_SITE` (22, 17) +0.033 ± 0.109 on 24040 after +0.170 on 24039, siege
  spawns, cap 60, exploration 0.2; mushroom A `KING1_SITE` (27, 17) −0.108 ± 0.111 after +0.100, cap 30, cheese-free
  bites, fewer traps; diamond's earlier second king, its mine gate 3, the site skip; houseofcards A every arm (the
  bigger army −0.20, the sites −0.05).
- Matrices: `pmh3` (24033), `pme1` (24034), `pmm1` (24035), `pmd1` (24036), `pmh4` (24037), `pmsk` (24038), `pmk1s`
  (24039), `pmkc` (24040), `pmkh` (24041); the king-site candidates on branch `t3/pm3-trial` (never merged).

## 12. PM4: twelve map-seats lost to king kills, no entry, no owner

Main on seed base 24042, 60 cells a map, every replay kept (`1120-114652-ab-105c`); `scratch/pm/deep.sh RUN TAG` runs
the replay readers, `pm3sum.py RUN TAG` and `tlsum2.py TAG` summarise them. Every loss is a king kill, most before
r450. What our first king loses its HP to in the worse seats' losses (KingLedger, HP a loss):

| map-seat (win) | cat scratches | bites | starvation | throws | the rest of the picture |
|---|---|---|---|---|---|
| directconfrontation B (0.717) | 322 | 112 | 49 | 69 | four corner cats round a central box; scratches in wins too (A 151, B 260) |
| closeup A (0.617) | 370 | 50 | 164 | 9 | closeup B wins 60 of 60 with CM's lane memory and flee |
| externalsustenance A / B (0.633 / 0.750) | 343 / 264 | 109 / 139 | 105 / 93 | 16 / 54 | 24 mines in a ring, cats |
| hellofromtheotherside A (0.767) | 143 | 76 | 295 | 32 | |
| DefaultSmall A / B (0.867 / 0.783) | 2 / 0 | 121 / 112 | 194 / 205 | 158 / 204 | 7–8 rats against 11–12 (small-map cap and mine-poor cut) |
| cheesefarm, constrictinglattice, streetsofnewyork, trapped, DefaultMedium, pocketsofcheese (0.72–0.88) | ≤ 45 | 147–361 | 69–383 | 48–183 | even armies; the fight reaches our king first |
| Meow A / B (0.900 / 0.750) | 0 | 169–177 | 387–520 | 13–71 | the bank runs dry late |

Levers tried (choosing screens, then confirmations against today's main on fresh seeds):

| arm | maps | choose (seed base, cells) | confirm (seed base, cells) |
|---|---|---|---|
| `CAT_DEF_DSQ` 100 / 200, `CM_LANE_MEMORY` + `CM_LANE_FLEE` | directconfrontation, closeup, externalsustenance, hellofromtheotherside | `pmdc` 24043, `pmcl` 24044, `pmcx` 24046 (100): best directconfrontation A +0.06 (p 0.18); defence −0.05 to −0.34 on the other three | none qualified |
| closeup A: seat B's `CAT_LANE` tiles + lane memory and flee | closeup | — (`pmt4b`, 24048, 100) | A −0.050 ± 0.140 |
| `PM_SIEGE` 1, `KING_DEF_DSQ` 100 | the ten others | `pmx5` 24045 (60): constrictinglattice A siege +0.133 (p 0.038); siege +0.03 on both seats of DefaultMedium, trapped, externalsustenance; DefaultSmall defence +0.02 / +0.05 | `pmc5` 24047 (100): constrictinglattice A 0.000, DefaultMedium −0.02 / 0.00, trapped +0.05 / −0.01, externalsustenance A +0.02, DefaultSmall defence +0.02 / +0.04 (all across 0); externalsustenance B on top of CM's site (`pmt4a`, 24049) 0.000 |

The first king's site was screened on all twelve maps by CM (cm4, cm5: four site types; only externalsustenance B
passed, CM's line), the army caps on trapped (WS2), the hands and orders on Meow and knifefight (SG3), the spawn and
lock-in constants on the top-50 maps (WS3, WS4). What is left on these map-seats is the rat exchange at our king (bites
and throws first, starvation after) and, on the cat maps, a king caught between cats and rats that neither the cat
defence nor the lane memory frees: no switch reaches either. Closed rows in docs/closed.md.

## 13. PM5: where each switch has been screened

Every `tools/tune` campaign records its variants' values and its runs' pools (`~/battlecode26-data/tune/*/campaign.json`):
`scratch/pm/coverage.py WORKTREE` writes, per Tune constant and map, the most cells one campaign played a variant setting
that constant on that map (`scratch/pm/coverage.json`, with the campaigns). A map counts as screened from 20 cells (both
seats). Runs made outside campaigns (branch arms with `tools/games ab`: KR's first hands, MS's planner on `goal`) are not
in it; they played `goal` at 10–30 cells a map. `PM_SYM` acts only on a book map-seat (`Book.symmetry` is 0 elsewhere),
so a matrix point alone does not play it: its screens need trial entries (`t3/pm5-sym`). `SPAWN_CAP_SMALL` acts only on
maps of at most 900 tiles. Table of 20 Nov (115 campaigns), gaps among the 43 maps of the top 60 map-seats of
`rank-book6.json`:

| switch | maps played with ≥ 20 cells (of 95) | campaigns (examples) | gaps among the top-60 map-seats' 43 maps |
|---|---|---|---|
| `KING_PATH` | 76 | WS, WSALL, at5m, cm2a, cm6b, kr_m2a … | none |
| `KING_HAND` | 87 | SGK, SGM, SGN, cm2c, kr2_rotb, kr_m2a … | DefaultMedium, holeinthewall, shell |
| `SG_LOOK_RIGHT` | 52 | SGL, SGM, SGN, WS, WSALL, at2m1 … | DefaultMedium, TheHeist, averyfineline, cheesefarm, gridlock, mincemeat |
| `PM_SYM` | 11 | gshor, gsrot, gsver, hcf, pmhor, pmrot … | 35 maps |
| `PM_COOP_HOLD` | 51 | WS, WSALL, at2m1, gcf, hcf, pmh2 … | DefaultMedium, averyfineline, cheesefarm, gridlock, mincemeat, pocketsofcheese, popthecork |
| `PM_SKIP_FAILED` | 50 | WS, WSALL, pmk3, pmk4, pmsk | DefaultMedium, TheHeist, averyfineline, cheesefarm, gridlock, mincemeat, pocketsofcheese, popthecork |
| `PM_SIEGE` | 34 | at2m1, cm2a, cm6b, pmc5, pme1, pmt4a … | 15 maps |
| `CM_CAT_HUNT_START` | 52 | WS, WSALL, at2m1, cm3a, pmc1, pmc2 … | DefaultMedium, TheHeist, averyfineline, cheesefarm, gridlock, mincemeat, pocketsofcheese, popthecork |
| `CAT_DEF_DSQ` | 53 | WS, WSALL, at2m2, cm2f, pm1, pmc1 … | DefaultMedium, averyfineline, cheesefarm, gridlock, mincemeat, pocketsofcheese, popthecork |
| `CM_LANE_MEMORY` | 27 | at2m5, cm2a, pmcl, pmcx, pmdc, pmt4b | 22 maps |
| `CM_LANE_FLEE` | 27 | at2m5, cm2a, pmcl, pmcx, pmdc, pmt4b | 22 maps |
| `DIRT_ROUND` | 50 | WS6, pmx4, ws4 | none |
| `IDLE_GOHOME` | 53 | WS6, pm1, s1, s2, ws4 | none |
| `KING_DEF_DSQ` | 10 | fls1, pmc5, pmx5 | 33 maps |
| `CM_KING1_SITE` | 53 | cm4a, cm4b, cm4c, cm5a, cm5b, cm6a … | none |
| `CM_KING2_ROUND` | 61 | WS, WSALL, cm2a, cm6b, pmd1, ws4 | none |
| `SPAWN_CAP` | 52 | WS2, WS3, WS6, WSALL, at2m1, clong … | DefaultMedium, averyfineline, cheesefarm, mincemeat, pocketsofcheese, popthecork |
| `SPAWN_CAP_SMALL` | 25 | at2m2, at2m3, pm1, pmh3, pmh4, pmkh | 24 maps |
| `SPAWN_RICH` | 55 | WS5, WS6, at2m1, clong, clong2, cm2b … | none |
| `MINES_KING2` | 55 | WS5, WS6, at2m1, cm2a, cm2b, cm6b … | none |

**The gap screens** (reference `origin/t3/book-8` at `667b0472`, 40 cells a map; `scratch/pm/matrix_ref.py` runs
`tools/book/book matrix` with that reference): `pm5a` (24050: `KING_HAND` 0 / 1, `SG_LOOK_RIGHT` 1, `PM_SKIP_FAILED` 2,
`CM_CAT_HUNT_START` 500, `CAT_DEF_DSQ` 100, `PM_COOP_HOLD` 1 on the 10 maps of those gaps), `pm5b` (24051: `PM_SIEGE` 1,
`CM_LANE_MEMORY` + `CM_LANE_FLEE` on the 22 lane gaps), `pm5s` (24052: `PM_SYM` 1 entries on the 35 symmetry gaps, branch
`t3/pm5-sym`). Each pick read only on its switch's gap maps; 12 qualified (p_max < 0.05 or positive on both seats), one
confirmed (`pm5c` 24053, `pm5t` 24054, 100 cells a map):

| map | seat | entry | chosen on (seed base, cells, candidates) | confirmed on (seed base, cells) |
|---|---|---|---|---|
| longwayaround | A | `PM_SIEGE` 1 | 24051, 40, 2: A +0.175 ± 0.119, p_max 0.008 (B +0.050) | 24053, 100: A +0.260 ± 0.086 (0.92 vs 0.66; B +0.020 ± 0.028) |

Against `book-8` (longwayaround's `SPAWN_CAP` 60 and seat A's `MINES_KING2` 7 in the reference). `PM_SIEGE` is already
settable (Nofreecheese): no new code, no king bytecode change. Failed confirmations: averyfineline `SG_LOOK_RIGHT` 1
(+0.010 / −0.040), popthecork `CAT_DEF_DSQ` 100 (+0.020 / 0.000) and `PM_SIEGE` 1 (0.000 / +0.060 ± 0.092),
pocketsofcheese and where_are_you lane memory (+0.010 / +0.030, −0.020 / −0.030), squaresaremodernart `PM_SIEGE` 1
(0.000 / 0.000), `PM_SYM` on QueenOfHearts, constrictinglattice, gridlock, mincemeat, squaresaremodernart (−0.030 to
+0.040, all across 0).

**The table at the end of PM6** (all 20 Nov campaigns, PM6's included; every `goal` map): `PM_SYM` counts only matrix
points, so its trial-entry screens (`pm5s`: 35 maps at 40 cells; `gs*`: every map at 10) are not in its row.

| switch | `goal` maps played with ≥ 20 cells (of 95) | not yet played there |
|---|---|---|
| `KING_PATH` | 84 | DefaultLarge, evileye, arrows, rift, ZeroDay, mercifullattice, toomuchcheese, whereisthecheese, attack_on_cat, fullofsecrets, learn_your_lesson |
| `KING_HAND` | 90 | gelcatinouscube, groveisbeingdesecrated, isnotgold, seepastyourgreed, superioristhmus |
| `SG_LOOK_RIGHT` | 58 | 37 maps |
| `PM_SYM` | 11 | 84 maps |
| `PM_COOP_HOLD` | 58 | 37 maps |
| `PM_SKIP_FAILED` | 58 | 37 maps |
| `PM_SIEGE` | 68 | 27 maps |
| `CM_CAT_HUNT_START` | 65 | 30 maps |
| `CAT_DEF_DSQ` | 65 | 30 maps |
| `CM_LANE_MEMORY` | 49 | 46 maps |
| `CM_LANE_FLEE` | 49 | 46 maps |
| `DIRT_ROUND` | 70 | 25 maps |
| `IDLE_GOHOME` | 53 | 42 maps |
| `KING_DEF_DSQ` | 10 | 85 maps |
| `CM_KING1_SITE` | 70 | 25 maps |
| `CM_KING2_ROUND` | 72 | 23 maps |
| `SPAWN_CAP` | 65 | 30 maps |
| `SPAWN_CAP_SMALL` | 25 | 70 maps |
| `SPAWN_RICH` | 55 | 40 maps |
| `MINES_KING2` | 72 | 23 maps |


## 14. PM6: the next tier of map-seats

Map-seats outside the 50 maps screened since book 6 with at least 6 losses in the pooled runs of books 6–8
(`rank-pooled.json`, about 95 games each): 29 on 20 maps. Levers (40 cells a map, against main plus the book 9 and 10
lines, `t3/pm6-ref` `c3a25967`, which sets nothing on these maps): `PM_SIEGE` 1, `KING_PATH` 1, `DIRT_ROUND` 0,
`SPAWN_CAP` 30 / 60, `MINES_KING2` 3 / 7 (seed base 24055), and on the map-seats lost on points `CM_CAT_HUNT_START` 500
and `CAT_DEF_DSQ` 100; the first king's seat over CM's four site types and every reachable mine (`king1.py`,
`king1all.py`; 2–8 candidates a map-seat, `pm6k1`, 24056); an early second king beside our nearest free mine at r30 and
r80 and beside the next at r30 (SG's `k2cand.py`; `pm6k2a` 24057, `pm6k2b` 24058). Dropped where a screen had already
played the value with at least 20 cells: `KING_PATH` on 12 of the 20 maps, `SPAWN_CAP` on 7, `MINES_KING2` on 3,
`PM_SIEGE` on dirtpassageway, the cat pair on 5; sunandmoon's second king (CM books one). The first runs stalled with
Condor's submit host (the tools' fix `93dde2c0`); the matrices were played again unchanged.

Selection: the map-seat's best at p_max < 0.05, or the same candidate positive on both seats with the two together at
+0.10 or more; a read above the map-seat's loss rate (or leaning on a partner seat's such read) is chance. Eight picks
on nine map-seats; one confirmation, all in one paired run against `origin/main` (`7b095ab7`), seed base 24059, 120
cells a map (`1120-182050-ab-4e46`, arm `t3/pm6-arm` `1ac041f2`):

| map-seat | pick (screen) | screen (seat / other) | confirmation | |
|---|---|---|---|---|
| groveisbeingdesecrated A | `CM_KING2_ROUND` 30, `KING2_SITE` (48, 40) by (50, 40) (`pm6k2a`) | +0.075 / +0.100 | **+0.092 [+0.040, +0.144]** (120 / 109) | confirmed |
| groveisbeingdesecrated B | `KING1_SITE` (5, 4) (`pm6k1`, site 2) | +0.125 / +0.050 | +0.058 [−0.016, +0.133] | failed |
| sunandmoon A | `KING1_SITE` (19, 30) (site 2) | +0.125 / +0.025 | 0.000 [−0.093, +0.093] | failed |
| sunandmoon B | `KING1_SITE` (19, 30) (site 4) | +0.050 / +0.100 | 0.000 [−0.066, +0.066] | failed |
| UncomfortablyClose A / B | `KING1_SITE` (1, 7) / (14, 12) (site 2) | +0.025 / +0.100 | +0.017 / −0.017 | failed |
| averystrangespace A | `MINES_KING2` 7 (`pm6mk`) | +0.050 / +0.075 | +0.008 [−0.041, +0.058] | failed |
| bed B | `DIRT_ROUND` 0 (`pm6dr`) | +0.075 / +0.025 | −0.017 [−0.090, +0.057] | failed |
| canyoudig B | `PM_SIEGE` 1 (`pm6sg`) | +0.050 / +0.050 | −0.025 [−0.107, +0.057] | failed |

Read as chance (above the loss rate): groveisbeingdesecrated A `DIRT_ROUND` 0 (+0.125), its second king at rank 2
(A +0.100, B +0.200), insertmeowname A's sites and second king (+0.150 to +0.175), wallsofparadis B's site (+0.125),
seepastyourgreed B's second king (+0.100); and insertmeowname B, wallsofparadis A, seepastyourgreed A, whose both-seat
reads leaned on those. The other levers read within ±0.075 on every map-seat (`KING_PATH` −0.175 on seepastyourgreed B).

Runs (kept): references `pm6sg` `1120-153100-ab-e7ad`, `pm6kp` `-153105-ab-b7c8`, `pm6dr` `-153108-ab-15a8`, `pm6cap`
`-153112-ab-9bea`, `pm6mk` `-153116-ab-f8eb`, `pm6cat` `-153120-ab-a169`, `pm6k1` `-153124-ab-e5e1`, `pm6k2a`
`-153128-ab-501b`, `pm6k2b` `-153133-ab-8d71`; picks: `pm6k2a` R30 `1120-153128-ab-78d9`, `pm6k1` site 2
`-153125-ab-131a`, site 4 `-153126-ab-2c44`, `pm6mk` MK7 `-153117-ab-b644`, `pm6dr` `-153108-ab-987b`, `pm6sg`
`-153100-ab-3bdf`; trial branches `t3/pm6-ref`, `t3/pm6-k1`, `t3/pm6-k2r1`, `t3/pm6-k2r2`, `t3/pm6-arm` (never merged).

## Tools

`~/battlecode26-data/scratch/pm/`: `seat.py RUN` (loss classes per map-seat from results.jsonl), `pair.py REF RUN...`
(paired per map-seat differences), `coop.py RUN MAP` (endings by king counts), `ends.py RUN MAP`, `java/MapDump`
(ASCII map and its symmetry), `java/TeamTL` (per-team 10-round series from replays), `java/KingStarts`,
`goal_sym.txt`; probe worktree `wt` (branch `t3/pm-probes`, never merged: coordinator prints, played locally with the
tools/play shim that keeps robot output).
