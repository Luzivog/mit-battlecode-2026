# tools/tune: challenger's constants tuned by measurement

`tools/tune` builds variants of challenger that differ from a base commit only in `src/challenger/Tune.java`, plays
each in a fixed-size paired test against awubot (`tools/games ab`, main on the same cells), and fits each constant's
effect on the win rate from all of them. The method, its measured noise and cost, and the results: docs/evaluation.md
"Tuning constants".

## Commands

| what | command |
|---|---|
| the tunable constants: value, step, bounds | `tools/tune params [--rev REV]` |
| a screening design over some constants (2n variants, resolution IV) | `tools/tune screen NAME --params A,B,... [--scale S]` |
| variants at given values | `tools/tune points NAME --point LABEL:A=1,B=2 ...` |
| variants along a screen's steepest-ascent line | `tools/tune path NAME --screen S [--min-z 2] [--t 0.5,1,2]` |
| build the variants (two at once) and start each one's test as soon as it is built; **run in the background** | `tools/tune play NAME --seed-base B [--cells 285] [--pool goal] [--builds 2]` |
| wake at each run's end (background; again after each wake) | `tools/tune wait NAME --next` |
| wait for every run (background) | `tools/tune wait NAME` |
| variants, commits, runs | `tools/tune show NAME` |
| scores and effects, 95% CIs by map | `tools/tune fit NAME [--seed-base B] [--split LISTPOOL] [--json]` |
| a variant's values into this checkout's Tune.java | `tools/tune apply NAME LABEL` |
| tests | `python3 -m unittest discover -s tools/bctune -t tools -p '*_test.py'` |

Campaigns live in `~/battlecode26-data/tune/NAME/campaign.json` (`TUNE_DATA` overrides); variant commits at
`refs/tune/NAME/LABEL` (never pushed; `tools/games` reads them with `git archive`).

## Tune.java

- One line a constant: `public static final TYPE NAME = VALUE; // tune: STEP [MIN, MAX] meaning`. The tuner rewrites
  VALUE only, within [MIN, MAX]. A code line holding `// tune:` that does not parse is an error.
- A constant some book entry sets has no `final` (`tools/book/book gen` drops it; tools/book/AGENTS.md): it is a
  static field the book writes per map-seat, and a variant's value is its default off the book.
- Compile-time constants only (literals and constant expressions such as `10 * Tune.GOHOME_PCT / 100`): javac copies
  the value into every use, so main's bytecode is unchanged instruction for instruction, and a variant changes no
  instruction, only the constants. Its games equal main's up to the first decision a constant changes, with no
  bytecode noise before it (docs/evaluation.md, W0.1).
- To make a number tunable: move it into Tune.java with a step and bounds, and use `Tune.NAME` (templates:
  `challenger.Tune.NAME`). Check that main's instructions did not move: compile both packages alone and compare
  `javap -c -p` with constant-pool indices removed (docs/evaluation.md "Tuning constants" has the command).

## How a campaign runs

1. `screen` writes a two-level design: each constant at its value ± STEP·scale, 2n variants for up to n constants
   (n a power of two). Every column is balanced and orthogonal to every other and to every product of two columns, so
   a main effect is mixed with no two-constant interaction. Quadratic terms all fall into the intercept, read as the
   curvature (mean of the variants − main).
2. `play` first starts main's own run on the cells (`tools/games ab challenger@MAIN awubot --ref none --min-cells N
   --max-cells N --seed-base B --remote`, label `ref`), then makes the variants' native binaries (`tools/games
   build`, `--builds` at once, default 2: the host's two slots, translations one at a time, so a build's compile,
   link and test start overlap the next translation) and starts each variant's test as soon as its own build is
   done. Every variant plays the same N cells. `tools/tune wait NAME --next` in the background wakes you at each
   run's end (it prints the runs no earlier `--next` reported, then how many still run). Main plays once: paired runs started together each replayed main's games until the
   cache held them (23% of the games of campaign s3). Use a seed base no test of these variants or of their choice
   used. A variant whose run stopped short of its cells (failed batches) is played again by the next `play`.
3. `fit` scores each cell (games won / 2) for every variant and main, keeps the cells every one of them completed, and
   fits the coded design by least squares on the variants' mean scores. Effects are "score change for +1 step". CIs
   resample maps (as `tools/games` does), so they speak for maps in general, not only the pool's.
4. A constant whose |z| ≥ 2 moves; `path` puts points along the fitted gradient, and `fit` of that campaign fits a
   parabola along it (main at t = 0). The chosen point is then confirmed by the ordinary protocol on a fresh seed
   base: a `goal` gain test, `teammade` and the gauntlet (docs/evaluation.md "Protocol"). Never confirm on a seed
   base the search used.

## Rules

- Never tune on the holdout. Search on `goal` (or a class pool) with derived seeds; confirm on another seed base.
- A screen's z values come from one seed base: about one in twenty flat constants reads |z| ≥ 2 by chance. Treat the
  screen as a ranking and the path and confirmation as the test.
- Rebase onto origin/main and run a new campaign after merges that touch the tuned code: effects are measured
  against the main of their time.
