# Code generation (Jinja → Java)

Almost all of the bot's Java (> 99% of lines) is generated. A `Foo.java.jinja2` template renders to the `Foo.java` next to it. **Both files are committed.** Javac and the engine only see the `.java`. If you edit a generated `.java` by hand, the next `jinja.py` run overwrites the change without warning.

Related docs: architecture.md (what the generated code does), testing.md (build and run matches).

## 1. Running the generator

```bash
# From the repo root. jinja.py uses FileSystemLoader("./") and relative paths "src/<pkg>".
.venv/bin/python jinja.py                       # all templates in src/challenger except MapTracker (~1 s)
.venv/bin/python jinja.py --include-maptracker  # also MapTracker (~25 s more; 6 MB output)
.venv/bin/python jinja.py --watch               # full build, then recompile templates as they change
```

| Flag | Effect (jinja.py) |
|---|---|
| `--package P` (default `challenger`) | Renders `src/P/**/*.jinja2`. It **does not rename packages**: templates hard-code `package challenger...` and `import challenger...`. |
| `--allow-frozen` | Required for `--package awubot`. Never use it, because awubot is the frozen baseline. |
| `--include-maptracker` | Without this flag `MapTracker.java.jinja2` is skipped (`compile_template`, jinja.py:244). It is opt-in because rendering takes ~25 s (7 nested template loops, 60×60×8 states). |
| `--debug` | Sets the render variable `debug=True`. **No challenger template reads it** (only `awubot_us` does). |
| `--watch` | Uses `watchdog` on `.` (the whole repo, recursive). Editing a template under `templates/` clears the cache and runs `compile_all`. Editing any other `.jinja2` recompiles only that file. MapTracker is included only if the flag was given. |

Python deps (`requirements.txt`) are `jinja2` (3.1.6 in `.venv`) and `watchdog`.

## 2. How jinja.py works

- **Environment** (`make_env`, jinja.py:109): `trim_blocks=True, lstrip_blocks=True`, plus the `debug` tag extension.
- **Python globals** injected into every template:

| Name | Value |
|---|---|
| `bitAnd bitOr bitLeftShift bitRightShift intDiv` | Integer helpers. Jinja has no bitwise operators (`\|` is the filter pipe); `intDiv` duplicates Jinja's own `//`. |
| `capitalise(s)` | Upper-cases the first character (`seen` → `Seen`). Used to build method names. |
| `radialMask(rsqr,cx,cy)` | (mask0, mask1) for a 9×9 grid. **Unused.** |
| `MAX_MAP_SIZE` | 60 |
| `directions` / `shortDirections` / `allDirections` | `"Direction.NORTH"`…`"Direction.NORTHWEST"` (ordinal order) / `N…NW` / the 8 directions + `"Direction.CENTER"`. Strings meant for pasting into Java. |
| `try_ord` | `[0,4,1,-1,3,-3,2,-2]`, the rotation offsets that define the move-ordering below. |
| `movement_table`, `get_movements(facing)` | For each facing: 9 `(dir, isWideRelated)` pairs, ordered CENTER, facing, opposite, ±1, ±3, ±2. Used by every combat `Try*` template to order candidate moves (e.g. TryThrow.java.jinja2:117). |

- **Shared template globals** (`_refresh_global_exports`, jinja.py:202): each `src/<pkg>/templates/*.jinja2` is loaded as a module, and every public macro or top-level `{% set %}` becomes a global. `templates/common.jinja2` exports these:
  - Maps: `related_map`, `wide_related_map`, `wide_related_map_center` (ordinal → list of direction strings), `dir_short_map`, `dir_ord_map`, `dir_opposite_map`, `dir_dx`, `dir_dy` (dy is +1 for NORTH), `visible_dirs`.
  - Lists: `moveDirs`, `moveShort` (the 8 directions + CENTER/`C`).
  - Macro: `can_see(facing_idx, dx, dy)`, which returns the **string** `"true"`/`"false"` for whether a baby rat facing that way has (dx,dy) in its 90° cone. It ignores distance, so callers test `dx²+dy² ≤ 20` themselves.
- **Wrapper**: each template renders as `{% include 'src/<pkg>/templates/common.jinja2' %}\n{% include '<template>' %}` (`_wrap_with_globals`, :221). As a result every generated file **starts with two blank lines**. Together with a sibling `.jinja2`, that is how you recognise a generated file. There is no "GENERATED" header.
- **Render variables**: `directions`, `shortDirections`, `allDirections`, `debug`.
- **`{% debug %}` tag** (`DebugExtension`, :19). `{% debug ("myLoc", "time") %}` expands to `System.out.println("[<template line>] " + "myLoc = " + myLoc + ", " + "time = " + time);`. Pass a **tuple** of Java expressions as strings. Space-separated literals (`{% debug "a" "b" %}`) are concatenated by Jinja into the single expression `ab`. It is unused in challenger. Remove it before measuring bytecode, because string concatenation is expensive.

## 3. Generated files (src/challenger)

Line counts are template → output.

| Template | Output | Lines | What gets unrolled |
|---|---|---|---|
| `CellTracker.java.jinja2` | `CellTracker.java` | 546 → **436,521** (18.7 MB) | 3×60 row bitboards. A `switch(y)` accessor per mask. Visible-cell scans per facing. Symmetry check/apply per map height (20–60) × robot row. |
| `comms/MapTracker.java.jinja2` (opt-in) | `comms/MapTracker.java` | 481 → **229,189** (6.0 MB) | One method per (dir, row) with `switch(x)`: 8×60 methods × 60 cases of inline chunk-bit updates. |
| `robot/rat/combat/moves/TryThrow.java.jinja2` | `TryThrow.java` | 711 → 34,831 | One method per (move dir, throw dir) and variant. |
| `Pathfinder.java.jinja2` | `Pathfinder.java` (classes `Config`, `BugnavInstance`, `Pathfinder`) | 999 → 18,536 | 13-slot path ring buffer. 11×11 bitboard BFS unrolled 13 iterations. Bugnav/source variants. |
| `robot/rat/combat/CombatTracker.java.jinja2` | `CombatTracker.java` | 462 → 15,960 | Threat masks per (move, facing). Byte-packed counters. |
| `robot/rat/combat/moves/TryTrap.java.jinja2` | `TryTrap.java` | 459 → 5,203 | |
| `util/Util.java.jinja2` | `Util.java` | 933 → 4,072 | `canSense` / `canCatSense` / `canHardSense` lookup switches |
| `RobotTracker.java.jinja2` | `RobotTracker.java` | 191 → 3,008 | Distance sort for 0–43 robots, per team A/B |
| `robot/rat/goHome/GoHomeStrategy.java.jinja2` | `GoHomeStrategy.java` | 126 → 1,855 | 11×11 serpentine breadcrumb scan |
| `robot/rat/combat/moves/TryAttack.java.jinja2` | `TryAttack.java` | 213 → 1,345 | |
| `robot/rat/combat/moves/TryRatNap.java.jinja2` | `TryRatNap.java` | 230 → 1,332 | |
| `robot/rat/combat/moves/TryFight.java.jinja2` | `TryFight.java` | 160 → 1,209 | |
| `robot/rat/combat/moves/TryPosition.java.jinja2` | `TryPosition.java` | 96 → 987 | |
| `robot/rat/combat/CombatStrategy.java.jinja2` | `CombatStrategy.java` | 510 → 500 | (mostly hand-written code) |
| `robot/king/KingCombatStrategy.java.jinja2` | `KingCombatStrategy.java` | 198 → 499 | |
| `comms/EnemyManager.java.jinja2` | `EnemyManager.java` | 185 → 358 | Health encode/decode switches |
| `robot/king/KingMiningStrategy.java.jinja2` | `KingMiningStrategy.java` | 279 → 330 | |
| `robot/rat/goHome/AntColony.java.jinja2` | `AntColony.java` | 71 → 128 | |
| `robot/rat/combat/CombatCommon.java.jinja2` | `CombatCommon.java` | 74 → 72 | |
| `templates/common.jinja2` | (none; shared globals) | 125 | |

Every other `.java` in src/challenger is hand-written (see the module map in architecture.md), except `Book.java`: `tools/book/book gen` writes it from `book.jsonl` and sets the `final` of Tune.java's constants (tools/book/AGENTS.md); jinja.py does not touch either.

## 4. Workflow

1. Edit the `.jinja2`, never the generated `.java`. If you change `templates/common.jinja2`, `can_see` in particular, also regenerate MapTracker, because MapTracker uses `can_see`.
2. Run `.venv/bin/python jinja.py` (add `--include-maptracker` if MapTracker or `common.jinja2` changed).
3. **Read the output for red `[error] <path>: <msg>` lines.** jinja.py **exits 0 even when a template fails**, and it leaves the output `.java` **truncated**: on a syntax error the file is 2 lines long; on an undefined macro call it is cut off mid-file. Both were verified in a scratch copy.
4. Run `git diff --stat src/challenger`. Only the outputs of templates you touched should change. A large negative line count means the file was truncated.
5. Compile: `./gradlew compileJava` (or see testing.md). Gradle compiles **every** package under `src/` (awubot, the snapshots, challenger). One bot package takes ~20 s and ~1 GB RSS with a cold `javac`.
6. Run `python3 check.py` from the repo root. It ignores its argument and scans `.` for `rc.move` / `rc.turn` outside `Game.java`. **Note: it already fails on the shipped code** (`robot/king/KingSpawningStrategy.java:90` `rc.turn(...)`, in every snapshot). Compare against that baseline instead of expecting exit 0.

**Checking that regeneration is clean.** Run `jinja.py --include-maptracker` with no edits, then `git status --short src/challenger`. It must show nothing; HEAD has been verified byte-identical. To check without touching the worktree, use a scratch copy: `mkdir -p /tmp/x/src && cp -r src/challenger /tmp/x/src/ && cp jinja.py /tmp/x/ && (cd /tmp/x && $REPO/.venv/bin/python jinja.py --include-maptracker) && diff -rq src/challenger /tmp/x/src/challenger`.

**New bot copy** (for example a probe package): copy the tree, then `sed -i 's/\bchallenger\b/NEW/g'` over **both** `*.java` and `*.jinja2`, then run `jinja.py --package NEW`.

## 5. Template patterns (real instances)

| Pattern | Where | Notes |
|---|---|---|
| Unroll a loop over directions or offsets, with the visibility test done at template time | `CellTracker.java.jinja2:192-224` (`storeVisibleCells`) | Emits one `{ var l = new MapLocation(x + dx, y + dy); if (rc.onTheMap(l)) storeCell(rc.senseMapInfo(l)); }` per visible cell, inside `case <facing ordinal>`. |
| Predicate → nested `switch` lookup table | `util/Util.java.jinja2:10-39` (helper macros returning 0/1) and `:155-188` (`canSense(dx,dy,dir)`) | Uses `{% set ns = namespace(dys=[]) %}` to collect the true `dy`s, then `case 2, 3, 4 -> return true;`. |
| Hand-flattened key switch | `util/Util.java.jinja2:276-366` | `key = dx + 20*dy`, with one `case` list per facing. |
| Variable naming instead of arrays (fields as registers) | `CellTracker.java.jinja2:39-77` (`static long seen0..seen59`, `passable*`, `dirt*`, plus `switch(loc.y)` accessors); `MapTracker.java.jinja2:136-140` (`chunk00..chunk33`); `Pathfinder.java.jinja2:63-136` (`path0..path12` ring buffer with `switch(nextPathIndex)`) | |
| Parameterised macro that emits whole code variants | `Pathfinder.java.jinja2:392-464` (`tryDirs("Left","Right")` → `tryDirsLeft()`/`tryDirsRight()`); `:315-329` (`go(where)`); `:285-302` (`updateBottomDir`) | |
| `{% for mode in [...] %}` around a whole method, to emit specialised copies | `Pathfinder.java.jinja2:633-921` (`doBugnavBfs` and `doBfs`, with `{% if mode == ... %}` inside) | |
| Split one huge switch into one method per case, so each method stays under 64 KB | `MapTracker.java.jinja2:445-478` (`updateVisibleUnitsDir{d}Row{y}`); `CellTracker.java.jinja2:395-453` (`checkVer{{height}}` per map height); `TryThrow.java.jinja2:297+` (`tryThrow{MOVE}{THROW}()`) | |
| Template-time constants | `Pathfinder.java.jinja2:14-15` (`MAX_BUG_ITERS=5`, `MAX_PATH=13`); `RobotTracker.java.jinja2:40-41` (`OFF`, `MAX_DIST=42`) | |
| Template-time bit arithmetic | `CombatTracker.java.jinja2:30-75` (`threat_expr`, `enemy_mask`, which use `bitOr`/`bitLeftShift` and `long_hex`) | |
| Serpentine scan with a running offset tracked in `namespace` | `GoHomeStrategy.java.jinja2:60-107` (`ns.off` = bitboard bit for the current cell) | |
| Fall-through `switch` as a bucketing function | `EnemyManager.java.jinja2:174-184` (`encodeHealth`) | Cases with no `return` fall through, so values round **up** to the next table entry. |
| `caller()` macros | `CombatStrategy.java.jinja2:37-61` (`for_related`, `for_wide_related`) | **Defined but never `{% call %}`ed (dead code).** |

Condensed example (the real one is `Util.java.jinja2:155-188`):
```jinja
{% for i in range(8) %}{% set dir = directions[i] %}
case {{i}} -> { switch(dx) {
  {% for x in range(-4, 5) %}{% set ns = namespace(dys=[]) %}
  {% for y in range(-4, 5) %}{% if (can_sense_distance(x, y)|int) and (can_sense_angle(dir, x, y)|int) %}{% set ns.dys = ns.dys + [y] %}{% endif %}{% endfor %}
  {% if ns.dys %}case {{x}} -> { switch(dy) { case {{ns.dys|join(", ")}} -> { return true; } } }{% endif %}
  {% endfor %} } return false; }
{% endfor %}
```

## 6. Bytecode techniques the code relies on

Costs below come from the engine's `MethodCosts.txt`. Plain JVM opcodes cost about 1 each.

| Technique | Why it pays | Instance |
|---|---|---|
| Unroll everything static | Removes loop counters, bounds checks and array loads | All templates; `CellTracker.storeVisibleCells` |
| `switch` on small integer domains instead of computing | A tableswitch is ~1 dispatch plus the body | `Util.canSense`, `EnemyManager.decodeHealth` |
| Switch on `enum.ordinal()`, never on the enum | An enum switch goes through a synthetic `$SwitchMap` array lookup | `Util.cw/ccw/relatedDirections` (`Util.java.jinja2:372-523`). Counter-example: `switch(robot.type)` at `KingCombatStrategy.java.jinja2:121` |
| Static fields instead of arrays or locals passed as params | No `aload` + index + bounds check. Methods split from a big switch need no arguments | `CellTracker.seen*`, `CombatTracker` `t_*` booleans, `TryThrow` per-case methods |
| Bitboards (row `long`s, an 11×11 window packed into 2 longs) | One shift/and processes a whole row or neighbourhood | `CellTracker.java.jinja2:269-312` (`computeLocalPassable`: bits 0–60 → `*0`, 61–120 → `*1`, centre = bit 60); `Pathfinder.java.jinja2:573-606` (`writePos`, `bfsIteration`) |
| SWAR: 8 byte-wide counters in one `long` (one byte per neighbour direction) | Updates 8 directions with one add | `CombatTracker.java.jinja2:84-100` (`low*`, `high*`, `moveThreat*`) |
| `StringBuilder`/`String` as a set, map or sorter | **`indexOf`, `insert`, `deleteCharAt`, `append`, `setCharAt` and `charAt` cost 1 bytecode each regardless of length** | `util/string/FastIndexedIntSet.java` (chars as ints); `RobotTracker.java.jinja2:79-112` (counting sort by distance using marker chars `\u0FFF+d`); `MapTracker` unseen-unit bag; `AntColony.mark` (5×60 StringBuilder rows); `CellTracker.mapData` |
| `Math.sqrt`/`Math.pow` are 1 bytecode | Floating point is cheap | `Pathfinder` scores (`Math.pow(score, 15)`) |
| Bytecode-budgeted search | Check `Clock.getBytecodeNum() - start > budget` between unrolled candidates. `Clock.*` calls are free | `TryThrow.java.jinja2:112-134` and all `Try*.run` |
| Field access over getters | `info.location` is cheaper than `info.getLocation()` (2) | `bytecode_notes.txt` |
| Avoid expensive engine calls | `senseNearbyRobots`/`senseNearbyMapInfos` cost 100, `writeSharedArray`/`squeak` 50, `senseRobotAtLocation` 15, `readSharedArray`/`readSqueaks`/`canMove` 10, `senseMapInfo`/`onTheMap`/`canSenseLocation` 5, a thrown exception 500, array creation = its length | Rats call `senseNearbyRobots` once in preTick plus once per mid-turn move/turn |

The original authors' rules are in `how_to_optimise_bytecode.txt`: statically compute everything, switch tables, ordinal switches, inline single-use temporaries, factor repeated expressions, globals-as-locals with per-case functions, and "write natural Python-ish code inside Jinja to precompute; don't unroll the unrolling code".

## 7. Pitfalls

- **The generated `.java` is overwritten.** Always edit the template. To find the template for `X.java`, look for `X.java.jinja2` in the same directory.
- **jinja.py exits 0 on errors and truncates the output** (§4.3). Grep its output for `[error]`.
- **Undefined names render as `""`** under the default `jinja2.Undefined`, so a typo such as `{{ dirr }}` silently emits nothing. Diff the output.
- **Macro output is a string.** Compare it with `|int`, `|trim` or `== "true"` (`Util.java.jinja2:164`, `CombatTracker.java.jinja2:71`, `MapTracker.java.jinja2:72`).
- **`{% set %}` inside a `for` does not leak out of the loop.** Use `namespace()`.
- **Whitespace.** `trim_blocks`/`lstrip_blocks` are on, so tags on their own line vanish. Inline `{% if %}`s leave long runs of spaces (harmless). Use `{%- -%}` inside value-returning macros.
- **MapTracker is opt-in.** If you change `common.jinja2` and forget `--include-maptracker`, MapTracker silently keeps the old logic.
- **Run from the repo root.** Template names are cwd-relative.
- **Java limits.** Method bytecode must be ≤ 65,535 B (javac fails with "code too large"). Current largest methods, measured from `build/classes`:

  | Method | Size | Share of limit |
  |---|---|---|
  | `CellTracker.computeLocalPassable` / `computeLocalDirt` | 41,295 B | 63% |
  | `CombatTracker.enemyMaskForMove` | 32,654 B | 50% |
  | `CellTracker.applyRot60` | 31,649 B | 48% |
  | `Pathfinder.doBugnavBfs` | 31,169 B | 48% |
  | `Pathfinder.doBfs` | 28,775 B | 44% |
  | `MapTracker.mergeChunkMask` | 15,375 B | 23% |

  `doBugnavBfs` grows roughly linearly with `MAX_PATH`, so doubling it would hit the limit. Constant pool, fields and methods are each capped at 65,535 per class; CellTracker uses 1,282 cp entries, 189 fields and 196 methods, and MapTracker uses 1,719 cp entries and 511 methods, so there is plenty of room. If a method gets too big, split it per case, as MapTracker does.
- **Wall clock.** HotSpot does not JIT methods over 8,000 B (`DontCompileHugeMethods`), so the huge unrolled methods run interpreted. This does not affect Battlecode bytecode accounting, but it does slow local matches and counts against the engine's per-team execution-time limit.
- **Compile cost.** The sources are huge (CellTracker.java is 18.7 MB), so expect ~20 s and ~1 GB per bot package in `javac`.
- **Duplicate `case` labels** (two template iterations producing the same constant) cause a compile error. The arrow form `case a, b ->` is used throughout.
- **Assertions are ON in the engine** (see architecture.md §1.6). Do not put side effects inside `assert`, and do not generate `assert(false)` defaults that are reachable.
- `check.py` also greps templates. Never write `rc.move` or `rc.turn` (or `rc.moveForward`, which contains `rc.move`) anywhere except `Game.java`.
