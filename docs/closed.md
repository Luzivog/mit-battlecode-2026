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

Code of the rejected arms: the packages' revert commits and side branches (`e3-c2` holds a working spawn-payload
role switch with S2's both-or-neither write, for KB or K3 to reuse).
