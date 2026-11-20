# tools/book: the per-map book

The book switches challenger's behaviour per map and seat on the `goal` maps; on any other map, and on a goal
map-seat without entries, the bot plays exactly as main. How an entry is chosen and confirmed: docs/evaluation.md
"The book". The id and its slot: docs/architecture.md §4.1.

## Commands

| what | command |
|---|---|
| write `Book.java` and Tune.java's finality from the book | `tools/book/book gen` |
| ids unique over `maps/`, entries valid, generated code current (exit 1 on a problem) | `tools/book/book check` |
| the goal maps' ids and round-1 keys | `tools/book/book ids` |
| tests | `python3 -m unittest discover -s tools/book -p '*_test.py'` |
| candidates x maps: one run a candidate plus main's, each over every map (background) | `tools/book/book matrix NAME --point LABEL:CONST=V ... --maps M1,M2 --cells-per-map N --seed-base B` |
| per map-seat: raw, selection-adjusted and shrunk effects, p_max | `tools/book/book fit NAME [--json]` |
| the method on simulated null and planted matrices | `tools/book/book fit --simulate` |
| the book against main: goal 3,000 cells, its maps, maps outside it (3 runs) | `tools/book/book confirm --seed-base B` |
| a confirm's results | `tools/book/book confirm-show FILE` |
| then smoke and the gauntlet on the book's commit, against main | `tools/games ab challenger@REV awubot --pool smoke --remote`; `tools/games gauntlet challenger@REV --ref challenger@origin/main --remote` |

After `gen`, regenerate nothing else; run `./gradlew build -x test` and commit `book.jsonl`, `Book.java` and
`Tune.java` together.

## The book: `src/challenger/book.jsonl`

One JSON object a line (blank lines and `#` lines skipped):

```
{"map": "EscapeTheNight", "seat": "B", "set": "KING_HAND", "value": 1, "evidence": ["1120-...", "1120-..."], "by": "KR"}
{"declare": "CAT_LANE", "type": "tiles", "meaning": "tiles a cat walks past our king"}
{"map": "bed", "seat": "both", "data": "CAT_LANE", "value": [[3, 4], [3, 5]], "evidence": ["..."]}
```

- `map`: a `goal` map. `seat`: `A`, `B` or `both` (A = our king is team A's).
- `set`: a tune constant of `src/challenger/Tune.java`; `value` within its `[MIN, MAX]`, an int for an int.
- `data`: a datum declared by a `declare` line, `Book.NAME` in the bot. The shapes, and what a read costs (javap
  instructions, each 1 bytecode):

  | type | declare | value | Java | off the book | a read |
  |---|---|---|---|---|---|
  | `int` | `"default": D` | an int | `int` | D | 1 (`getstatic`) |
  | `tileset` | | `[[x, y], ...]` | `long[]` of row masks, bit x of `NAME[y]` | null | 12 inline (`NAME != null && (NAME[y] >>> x & 1) != 0`); 10 with the array in a local |
  | `tiles` | | `[[x, y], ...]` | `MapLocation[]` | null | a loop |
  | `ints` | | `[i, ...]` | `int[]` | null | 3 an element |

  Setting one costs the robot once, at its first turn on its map-seat: a scalar 2, a tile set 3 + 4 a non-empty row.
- Built in, on every map-seat the book identifies: `Book.symmetry`, the map's symmetry from its file as
  `CellTracker.Symmetry`'s ordinal (1 rotational, 2 Horizontal = x -> W-1-x, the engine's VERTICAL, 3 Vertical).
  Its use is a switch like any other (e.g. a Tune constant that calls `CellTracker.setSymmetry(Book.symmetry)`), so
  an entry of that switch on a map is what makes the map known.
- `evidence`: the run IDs that chose and confirmed the value (docs/evaluation.md "The book"). `by`, `note`: free.
- A tile set is for what kings test tile by tile (cat lanes); a scalar for what rats need too (symmetry).
- `twins`: maps of `maps/` that look the same at round 1 (below), named to accept that the entry plays there too.
- One value per (map, seat, name).

## How it works

- **Identity.** At round 1 the first king (`Book.init`, the first call after `Game.init`) keys on its team, the map's
  size and its own location; on a key of a book map-seat it reads its r² 25 view (`senseNearbyMapInfos`) into two
  121-bit masks (walls, dirt; bit `(dx + 5) * 11 + dy + 5`; mines separate no map of `maps/` further) and compares
  them and its ID with the map file's.
  A match writes the map's id (1 + its index in `pools.GOAL`) to shared slot 15; every robot after it reads the slot
  at its first turn. A map-seat without entries is never keyed, so it costs a robot only the slot read (~15
  bytecodes once); a book map-seat's king pays the scan once (measured in docs/evaluation.md "The book").
- **No false match.** A map fires an entry only if its size, the king's team, location and ID and every wall and dirt
  tile within r² 25 of the king equal the book map's: a map outside the book matches only if it copies the book
  map's start exactly. `check` compares the views of every map in `maps/` (the sealed ones included, never named).
  Visible copies exist: the champion's catless maps (DefaultLarge, DefaultMedium, arrows) and testpathfinding,
  testpathfinding2 (whatsthecatdoin); an entry on those maps must name its twins.
- **Settable constants.** `gen` drops `final` from the Tune.java constants some entry sets (only those): their uses
  read a static field instead of an inlined constant: the same instruction count where the use was a plain constant,
  more where javac folded it into an expression or a condition (`Tune.KING_HAND == 0 || !isRatKing` was `true`). Those
  extra bytecodes run on every map, so a booked constant changes a few games off its map-seats too (measured: docs/
  evaluation.md "The book"); prefer a switch whose use is a single read. `Book.apply` writes them in the robot's first turn, before any class that reads Tune
  is loaded, so a `static final` copied from one (`KingFormationCoordinatorStrategy`) gets the book's value too. Every
  other constant stays compile-time, so a main without entries has main's instructions.
- `tools/tune` reads and rewrites both forms; a tuned value of a booked constant is its default off the book.

- `confirm` tests a book against main's own book: the entries' run plays only the maps whose map-seats differ from
  main's (new, changed or removed entries), and `confirm-show`'s line (a) reads those map-seats; carried-over entries
  are main's and read 0.
- A matrix point wins over the book on the matrix's maps: a point setting a constant that an entry sets on one of those
  maps has that entry dropped from its variant (`matrix` prints which; the variant's Book.java and Tune.java are
  regenerated), so a booked value can be re-tuned there. The reference stays main, book included: the fit reads the
  point against main's entry.

## Rules

- A matrix is K + 1 runs (`tools/tune play` of a points campaign on a `map:` pool): put many maps in one matrix
  rather than one matrix a map, and keep within the integrator's limit on open runs.

- Never edit `Book.java`; never drop `final` by hand. `check` fails when either is not what the book generates.
- A switch's default is main's behaviour, instruction for instruction (javap, docs/evaluation.md "Tuning constants").
- An entry's evidence comes from fresh seed bases, never from the cells it was chosen on.
