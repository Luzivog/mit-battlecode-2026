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
- `data`: a datum declared by a `declare` line: `tiles` (`MapLocation[]`) or `ints` (`int[]`), `Book.NAME` in the bot,
  null wherever no entry sets it.
- `evidence`: the run IDs that chose and confirmed the value (docs/evaluation.md "The book"). `by`, `note`: free.
- `twins`: maps of `maps/` that look the same at round 1 (below), named to accept that the entry plays there too.
- One value per (map, seat, name).

## How it works

- **Identity.** At round 1 the first king (`Book.init`, the first call after `Game.init`) keys on its team, the map's
  size and its own location; on a key of a book map-seat it reads its r² 25 view (`senseNearbyMapInfos`) into three
  121-bit masks (walls, dirt, mines; bit `(dx + 5) * 11 + dy + 5`) and compares them and its ID with the map file's.
  A match writes the map's id (1 + its index in `pools.GOAL`) to shared slot 15; every robot after it reads the slot
  at its first turn. A map-seat without entries is never keyed, so it costs a robot only the slot read (~15
  bytecodes once); a book map-seat's king pays the scan once (measured in docs/evaluation.md "The book").
- **No false match.** A map fires an entry only if its size, the king's team, location and ID and every wall, dirt
  and mine within r² 25 of the king equal the book map's: a map outside the book matches only if it copies the book
  map's start exactly. `check` compares the views of every map in `maps/` (the sealed ones included, never named).
  Visible copies exist: the champion's catless maps (DefaultLarge, DefaultMedium, arrows) and testpathfinding,
  testpathfinding2 (whatsthecatdoin); an entry on those maps must name its twins.
- **Settable constants.** `gen` drops `final` from the Tune.java constants some entry sets (only those): their uses
  read a static field instead of an inlined constant, the same instruction count except where javac folded the
  constant into an expression. `Book.apply` writes them in the robot's first turn, before any class that reads Tune
  is loaded, so a `static final` copied from one (`KingFormationCoordinatorStrategy`) gets the book's value too. Every
  other constant stays compile-time, so a main without entries has main's instructions.
- `tools/tune` reads and rewrites both forms; a tuned value of a booked constant is its default off the book.

## Rules

- Never edit `Book.java`; never drop `final` by hand. `check` fails when either is not what the book generates.
- A switch's default is main's behaviour, instruction for instruction (javap, docs/evaluation.md "Tuning constants").
- An entry's evidence comes from fresh seed bases, never from the cells it was chosen on.
