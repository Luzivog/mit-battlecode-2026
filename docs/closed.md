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
| 2026-11-19 | E1 | arm H = 30: reserve max(100, 2·kings·30); spawns (cost-10 exempt), rat traps, rat and king digs and the formation start leave global ≥ reserve after paying | `L-starve`, 0.08 | −0.037 [−0.075, +0.000], H0 (95 cells) | king starvation turns −0.4 a game [−2.3, +1.3]; cost-10 spawns +1.4; 5–7-mine maps −0.10 in wins. Branch `t3/e1-h30` |
| 2026-11-19 | E1 | arm H = 60 (as above) | `L-starve`, 0.08 | −0.012 [−0.059, +0.035], H0 (84 cells) | starvation turns −1.3 [−3.0, +0.3]: down on ≤ 4-mine maps (−1.6) with no more wins there; 5–7-mine maps −0.11. Branch `t3/e1-h60` |
| 2026-11-19 | E1 | arm H = 120 (as above) | `L-starve`, 0.08 | −0.078 [−0.128, −0.030] (103 cells) | the army collapses: cost-10 spawns +3.5 a game, cheese transferred −240, starvation turns +3.6 on 5–7-mine maps (−2.3 on ≤ 4). Branch `t3/e1-h120` |

Why E1 cannot gain (all three arms, paired replays): the spawn cap is already a reserve. `thresh = min(global/10 + 10, …)`
builds a cost-c rat only with global ≥ 10·(c − 10): 100 for cost 20, 200 for 30, 300 for 40. Outside the +10 to +30
raises when the king is hit or cats are near, global stays ≥ 80 after any spawn but a cost-10 one. With one king, a reserve of 2·K·H binds only on cost-20 rats at global 100–139, which
leaves a 4–7-rat army that cannot earn its way back (squaresaremodernart: 4 rats from r300 to r700, main rebuilt to 9;
the same cells lose in every arm). The trap, dig and formation gates almost never bind (traps 79 vs 79 a game). Most
remaining starvation follows a lost income share (cheddarchokehold: 4,660 transferred against awubot's 17,770), which a
reserve only delays. Diagnostics: `~/battlecode26-data/scratch/e1/` (`E1Stats.java`, `e1_diag.py`, `paired.py`).

Code of the rejected arms: the packages' revert commits and side branches (`e3-c2` holds a working spawn-payload
role switch with S2's both-or-neither write, for KB or K3 to reuse).
