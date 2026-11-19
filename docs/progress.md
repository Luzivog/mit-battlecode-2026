# Progress

Win rate of `challenger` at each merged `main` commit (games won / played, both seats). Written by the integrator
only, from `tools/games` results (docs/evaluation.md). Official conditions unless a row says otherwise.

| date | main commit | vs awubot | vs opp_* (each) | vs awubot snapshots | pool, seeds, engine | note |
|---|---|---|---|---|---|---|
| 2026-11-18 | 1ff4b32 | 50% by construction (identical code; every pair splits) | not yet measured | not yet measured | n/a | baseline: challenger = awubot |
| 2026-11-18 | c133834 | 285–285 = 50.0% (`goal`, 3 seed passes, 285 cells, every pair split) | obg 124–114 = 52.1% (`goal`, 119 cells); gst 231–115 = 66.8% (`goal`, 173 cells); on `quick`, ~32 cells each: spaark 76.6%, cerr 79.7%, parmesan 78.1%, gravy 81.7%, goobie 81.2%, powerpuff 82.8%, food 90.6% | on `quick`, ~32 cells each: quals 85.9%, us 75.0%, mystery 87.5%, sus 72.4% | engine 1.2.6, `tools/games` (runs 1118-222544-ab-6dea, 1118-225119-gauntlet-c3f9) | baseline measured: challenger = awubot; challenger's robots killed 41 times by their own uncaught Error in the 238 OBG games |
| 2026-11-19 | 3163bb8 (RB) | 197–183 = 51.8% [46.6, 57.1] (`goal`, 2 passes, 190 cells); holdout 49–55 = 47.1% [37.5, 56.7] (52 cells); teammade 33–39 = 45.8% (36 cells) | obg 204–196 = 51.0% (awubot 50.7% on the same cells, 200 cells); gst 230–118 = 66.1% (awubot 66.4%, 174 cells); on `quick`, 32 cells each: spaark 73.4%, cerr 78.1%, parmesan 82.8%, gravy 81.2%, goobie 76.6%, powerpuff 84.4%, food 89.1%; every diff vs awubot's score within its interval | quals 89.1%, us 75.0%, mystery 87.5%, sus 76.6% (`quick`, 32 cells each) | engine 1.2.6, plain mode (runs 1119-003728-ab-8d65, 1119-003729-ab-11a9, 1119-003729-ab-8a14, 1119-003730-gauntlet-afc2) | RB: king bytecode −2.3k mean, no behaviour change intended; all differences are within noise |
