# opp_powerpuff: Powerpuff Girls

| Field | Value |
|---|---|
| Team (platform) | Powerpuff Girls |
| Members (platform handles) | Varity, Gura, jungho_p, mikhail10101 |
| Result | Final tournament 9-12th; rating 1822 (ladder #17) |
| Source repo | https://github.com/battlecode-archive/2026-Powerpuff-Girls (same code: https://github.com/AdamTan12/BattleCode2026) |
| Commit (repo HEAD when vendored) | `91fdddbb1c015a2e8cd7ebcb8eebc8319b7ce06d` (branch `main`) |
| Original package path | `src/Nike/` (package `Nike`) |
| License | AGPL-3.0 |
| Vendored | 2026-11-18, `*.java` only |

Edits: Package renamed only: `package`/`import` lines and fully-qualified `Nike.` references rewritten to `opp_powerpuff` (command: docs/opponents.md §6). No other edits; compiles unmodified on engine 1.2.5.

Notes: `Nike` is the package seen in final-tournament replays (last commit 91fdddb, 2026-01-29 04:12 PST); the repo's `Finals` folder is NOT what they submitted. Not vendored: `Codegen/generator.py`, `Codegen/Templates/*.j2` (generated Java already in Micro/Attack), INFO.txt, COMBAT.txt (design notes; read them upstream). `Helpers/Constants.java` has an unused `import com.fasterxml.jackson.core.io.ContentReference;` that compiles only because the engine jar bundles Jackson; delete that line if it ever breaks the build.

Do not edit this package to improve it: it is a frozen sparring partner. See docs/opponents.md.
