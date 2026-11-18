# Progress

Win rate of `challenger` at each merged `main` commit (games won / played, both seats). Written by the integrator
only, from `tools/games` results (docs/evaluation.md). Official conditions unless a row says otherwise.

| date | main commit | vs awubot | vs opp_* (each) | vs awubot snapshots | pool, seeds, engine | note |
|---|---|---|---|---|---|---|
| 2026-11-18 | 1ff4b32 | 50% by construction (identical code; every pair splits) | not yet measured | not yet measured | n/a | baseline: challenger = awubot |
| 2026-11-18 | c133834 | 285–285 = 50.0% (`goal`, 3 seed passes, 285 cells, every pair split) | obg 124–114 = 52.1% (`goal`, 119 cells); gst 231–115 = 66.8% (`goal`, 173 cells); on `quick`, ~32 cells each: spaark 76.6%, cerr 79.7%, parmesan 78.1%, gravy 81.7%, goobie 81.2%, powerpuff 82.8%, food 90.6% | on `quick`, ~32 cells each: quals 85.9%, us 75.0%, mystery 87.5%, sus 72.4% | engine 1.2.6, `tools/games` (runs 1118-222544-ab-6dea, 1118-225119-gauntlet-c3f9) | baseline measured: challenger = awubot; challenger's robots killed 41 times by their own uncaught Error in the 238 OBG games |
