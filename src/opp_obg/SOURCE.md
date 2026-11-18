# opp_obg: Old But Gold

| Field | Value |
|---|---|
| Team (platform) | Old But Gold |
| Members (platform handles) | XSquare, dirbaio, Zousen |
| Result | Ladder #2, rating 2064 (not tournament-eligible; champion's nemesis, 15-24 vs it in final-week ranked) |
| Source repo | https://github.com/IvanGeffner/BC26 (mirror: https://github.com/battlecode-archive/2026-Old-But-Gold, identical) |
| Commit (repo HEAD when vendored) | `70b3f40cebefcca9dc858771715d78088e7ae520` (branch `main`) |
| Original package path | `basic51/` (package `basic51`) |
| License | none stated |
| Vendored | 2026-11-18, `*.java` only |

Edits: Package renamed only: `package`/`import` lines and fully-qualified `basic51.` references rewritten to `opp_obg` (command: docs/opponents.md §6). No other edits; compiles unmodified on engine 1.2.5.

Notes: basic51 is the only bot left at HEAD (code unchanged since 2026-01-25 except comment removal in 4eaa548). Later variants basic52..basic62 (24-74 changed lines vs basic51) exist only in history: `git archive 4eaa548^ basic62`. Not vendored: `codegen/` (Java generators for BFS/ObservationUtil/ThrowHelper; their output is already in basic51).

Do not edit this package to improve it: it is a frozen sparring partner. See docs/opponents.md.
