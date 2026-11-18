# opp_parmesan: Parmesan

| Field | Value |
|---|---|
| Team (platform) | Parmesan |
| Members (platform handles) | Matthewwww, Lithanium, Anson, reechee |
| Result | High-school tournament champion; rating 1929 (ladder #6) |
| Source repo | https://github.com/abcdezyxwv/Parmesan-bc2026 |
| Commit (repo HEAD when vendored) | `0a0b266dd010d31a66f2b4942853e4445dc36ace` (branch `main`) |
| Original package path | `src/bestsofar/` (package `bestsofar`) |
| License | none stated |
| Vendored | 2026-11-18, `*.java` only |

Edits: Package renamed only: `package`/`import` lines and fully-qualified `bestsofar.` references rewritten to `opp_parmesan` (command: docs/opponents.md §6). No other edits; compiles unmodified on engine 1.2.5.

Notes: Package inferred: `bestsofar` got the last commits before the HS freeze (491f4ef, 2026-01-27 10:26 AEDT = 2026-01-26 18:26 EST, freeze 19:10 EST). Generated navigators (BellmanFord*, Reachability) come from scripts/generate_*.py; only the generated Java is vendored.

Do not edit this package to improve it: it is a frozen sparring partner. See docs/opponents.md.
