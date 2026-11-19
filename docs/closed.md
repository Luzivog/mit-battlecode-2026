# Closed directions

Changes measured and rejected, with the reason. Reopen one only with new evidence, and say what it is. Each package's
agent adds its H0s here; the integrator adds them when an agent did not. Diffs are paired against the main of the time
(score of the branch minus main's, both against awubot), with 95% intervals.

| date | package | change | pool, δ | result | why it failed (measured) |
|---|---|---|---|---|---|
| 2026-11-18 | E2 | rat cap −4 on every map | `goal`, 0.10 | +0.014 [−0.042, +0.075], H0 | classes cancel: ≤ 4 mines +0.23, 10–12 mines −0.18, ≥ 13 mines −0.13 |
| 2026-11-18 | E2 | arm 1b: cap +4 on ≥ 10-mine maps | `mines-ge8`, 0.10 | −0.029 [−0.098, +0.039], H0 | the mine estimate latches too late on rich maps; needs an income estimate |
| 2026-11-19 | E2 | arm 1c: cap −8 on mine-poor maps | `mines-le4`, 0.10 | −0.455 [−0.629, −0.258] | far past the optimum; −4 is the dose |
| 2026-11-18 | KL | lock-in from r1000 (K_target 3) | `L-late` ∪ `L-points`, 0.10 | +0.000, H0 | too late to complete before r1200 |
| 2026-11-18 | KL | K_target 5 (T_lock 800 or 1000) | `L-late` ∪ `L-points`, 0.10 | −0.061 / −0.049 | extra kings starve |
| 2026-11-18 | KL | new king at the known mine nearest our kings | `L-late` ∪ `L-points`, 0.10 | −0.062 | shares income with existing kings |
| 2026-11-19 | KL | new king on a clear 3×3 beside the coordinator | `L-late` ∪ `L-points`, 0.10 | −0.140 | completes 77% of attempts, but kings sharing one mine starve |
| 2026-11-19 | KL | arm B (budding) | not built | | forms beside the first king: the same starvation as above |
| 2026-11-19 | E3 | miners skip enemy-side mines | `E3-ge8`, 0.10 | −0.116 [−0.216, −0.029] | far trips hold the front: awubot's pickups on our side 74 → 106 |
| 2026-11-19 | E3 | king assigns mines round-robin (payload role 1) | `E3-ge8`, 0.10 | −0.081 [−0.151, −0.013] | fired as designed (149 of 166 newborns); awubot's distance lottery is near a local optimum |
| 2026-11-19 | E3 | miner lottery weight d²/16 instead of d | `E3-ge8`, 0.10 | −0.037 [−0.132, +0.057] | |
| 2026-11-19 | E3 | go-home loads near home 10→25, 25→40 | `E3-ge8`, 0.10 | −0.051 [−0.114, +0.009] | |
| 2026-11-19 | E3 | exploration ends once our side is seen | dropped before test | | the unseen-unit bag never empties; restricting to our side cuts presence as above |
| 2026-11-19 | CW | C1 alone: throws scored by engine damage, cats as feeds | `L-combat`, 0.08 | +0.009 [−0.043, +0.064], H0 | kept only as the base of C2–C4, which gained |
| 2026-11-19 | K2 | A: kings remember cat footprints; mine and cheese choice off them | `L-cat`, 0.08 | −0.053 [−0.119, +0.000], H0 (38 cells) | parked kings are rarely scratched (82 HP per 1000 king-rounds vs 379–557 at 2–8 tiles from a mine), so site choice alone moves little |
| 2026-11-19 | K2 | B'+B'': kings silent while a cat hears them; cats near a king broadcast (slots 38–41), rats near one silent | `L-cat`, 0.08 | −0.033 [−0.097, +0.036], H0 | cat turns toward our squeaks that leave our king in front −80%, cone scratch only −7%, wins 92 vs 97: steering is not what scratches kings, silence costs information |
| 2026-11-19 | K2 | D': A plus no mining step into a known lane | `L-cat`, 0.08, fresh seeds | +0.021 [−0.038, +0.075], H0 | cat-involved king deaths −30% but all king deaths −1%: enemies kill the kings the cats no longer do; one clear cluster (bed 12 better, 1 worse). `goal` harm passed twice; kept on the branch, not accepted |
| 2026-11-19 | K2 | D2: D' plus marking 4 steps along a seen cat's facing | `L-cat`, 0.08, fresh seeds | −0.051 [−0.157, +0.042], H0 (39 cells) | cone scratch −27% (D': −12%) with no more wins |
| 2026-11-19 | E3 | change 0: the mine squeak decoder reads all 3 mines (a bug fix) | `goal` harm, 0.05, on CW main; and vs `opp_obg` | vs awubot −0.005 [−0.024, +0.016] (14 of 190 cells changed); vs OBG −0.014 [−0.030, +0.001] (393 cells), and −0.026 [−0.052, +0.000] in E3's gauntlet | kings learn mines from their own vision, so their knowledge barely moves (same on 14 of 18 maps); only rat gossip speeds up, and it leans against us vs OBG twice. Not merged; branch `t3/e3-territory-miners-deficit` |
| 2026-11-19 | CB | CB1: skip the cheese transfer so the planned bite happens; "correct" B5 (next-best move); B6 | `goal` harm, 0.05 | +0.041 first, then −0.025 [−0.047, −0.003] over 539 cells on the E2 main | changes game actions at out-of-cone events; not certain-sign |
| 2026-11-19 | CB | CB1b: transfer keeps priority, "correct" B5 | `goal` harm, 0.05 | 7 better / 15 worse, −0.017 | as above |
| 2026-11-19 | CB | bite damage capped at 13 (kill now, flip HP order, win 1v1 races) | `L-combat`, 0.05 | +0.027, H0 | pure 1v1 duels happen ~0.5 a game: winning them does not move the score |
| 2026-11-19 | CB | damage cap 16 | `L-combat`, 0.05 | −0.048, H0 | extra ~67 cheese a game spent on bites |
| 2026-11-19 | CB | cap 16 with main's target choice (16p) | `L-combat`, then `goal` confirmatory | H0; confirmatory −0.002 | duels won 0.74 vs 0.49, no score; widening TryAttack's one-tap weight to 13–16 HP pulls rats off kings |
| 2026-11-19 | CB | kings bitten at 12; plus finishing bites | `L-combat`, 0.05 | +0.025 / +0.020, H0 | kings take ~18 bites a game, +2 damage each kills no faster |
| 2026-11-19 | CK | A2: the king bites with the fewest cheese that kills ((h−11)²+1, at most a rat's cost, global ≥ 300) | `faceoff` ∪ `L-combat`, 0.05 | +0.000 [−0.019, +0.017], H0 | 107 of 111 cells identical: a killable enemy in the king's ring with the cheese at hand is rare |
| 2026-11-19 | CK | A7: with an enemy in view the king spawns on the ring tile farthest from it (T4 WP4) | `faceoff` ∪ `L-combat`, 0.05 | −0.100 [−0.170, −0.025], H0 | the mechanism worked: our rats grabbed ≤ 10 rounds after spawning 16.0 → 8.6 a game, throws into our king flying such a rat 2.45 → 0.85; but bites on our king 13.2 → 25.7: near-side spawns hold the ring tiles enemies bite the king from |
| 2026-11-19 | CK | A1: the king keeps its ring dirt while enemies are in view (`digSurroundingDirt` deleted) | `faceoff` ∪ `L-combat`, 0.05 | +0.006 [−0.029, +0.038], H0 | king digs 10.4 → 8.6 a game (the rest are path and unbury digs), throw HP into our king 128 → 120 |
| 2026-11-19 | CK | A6: the king spawns toward a visible enemy carrier (blocks its step-1 ring tile) | `faceoff` ∪ `L-combat`, 0.05 | +0.011 [−0.018, +0.041], H0 | throws into our king 3.39 → 3.26 a game |
| 2026-11-19 | CK | A4: carry the victim toward a visible enemy king | `faceoff` ∪ `L-combat`, stopped | | never fired: a rat sees r² 20 in a 90° cone, so a king 5+ tiles away is out of sight (hold and throw distances unchanged) |
| 2026-11-19 | CK | A4b/A4c: carry toward the last enemy king seen (8 rounds), throw from Chebyshev 3 | `faceoff` ∪ `L-combat`, 0.05 | +0.002 [−0.040, +0.041] / +0.008 [−0.026, +0.042], H0 | throw HP into their king 134 → 170 / 135 → 162 a game, but our bites on it 22.8 → 19.4 / 22.6 → 20.5: their king's HP lost unchanged (544 vs 552) |
| 2026-11-19 | CC | A3: TryFight −42 on a visible enemy carrier's step-2 tiles | `faceoff` ∪ `L-combat`, 0.05 | −0.002 [−0.052, +0.050], H0 | awubot's double hits on our rats 14.9 → 13.0 a game, but our rescues 10.1 → 9.1 and throw HP into our king +16: near our king a rat on the line takes the hit in the king's place |
| 2026-11-19 | CC | A5: TryFight targets the nearest enemy carrying one of ours (Chebyshev ≤ 4) | `faceoff` ∪ `L-combat`, 0.05, stopped at 233 cells | +0.000 [−0.049, +0.051] | rescues unchanged (10.0 vs 9.9 a game): most rescues happen in the 2 rounds after the grab, and the grab legality is TryRatNap's |
| 2026-11-19 | CC | A3 + A5 | `faceoff` ∪ `L-combat`, 0.05, stopped at 186 cells | +0.021 [−0.032, +0.083] | A3's mechanism and cost (double hits −2.3, rescues −1.1, throw HP into our king +13); replaced by A35r, which skips the rule within d² 36 of our king |
| 2026-11-19 | CC | A35r: A3 + A5, the step-2 rule skipped within d² 36 of our king | `faceoff` ∪ `L-combat`, 0.05 | −0.005 [−0.049, +0.041], H0 | |
| 2026-11-19 | CK | A10: rats near our king hold its ring tiles on the enemy's side (TryFight +30) | `faceoff` ∪ `L-combat`, 0.05 | −0.003 [−0.039, +0.035], H0 | bites on our king 12.5 → 11.2 a game in the first 150 games, no more wins |
| 2026-11-19 | CK | CK bundle: A1 + A4c + A6 | `faceoff` ∪ `L-combat`, 0.05, fresh seeds | −0.012 [−0.057, +0.034], H0 | |
| 2026-11-19 | CC+CK | full bundle: A1 + A4c + A6 + A35r | `goal`, 0.05, fresh seeds | +0.001 [−0.033, +0.034], H0 (345 cells) | every diagnostic moved as meant (our king's deaths 0.45 → 0.34 a game, its HP lost 372 → 324, throw HP into their king 78 → 108, double hits on our rats 12.8 → 11.4), wins did not; gauntlet no harm (OBG −0.028, GST −0.004) |

Code of the rejected arms: the packages' revert commits and side branches (`e3-c2` holds a working spawn-payload
role switch with S2's both-or-neither write, for KB or K3 to reuse; `t3/cc-a*` hold CC+CK's arms and `t3/cc-bundle` its
bundle).
