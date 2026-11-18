# tools/replays

Small Java utilities for official `.bc26` replays (gzip flatbuffers) and `.map26` files. Compile against the engine
jar and flatbuffers (both in the Gradle cache), run with `java -cp "$JAR:$FB:."`:

| file | usage | what it prints |
|---|---|---|
| `ReplaySummary.java` | `ReplaySummary replay.bc26...` | per game: map, size, winner, win type, rounds, end-of-game cheese transferred / cat damage / rats |
| `MapExtract.java` | `MapExtract OUTDIR replay.bc26...` | writes every map embedded in the replays as `OUTDIR/<name>.map26` |
| `MapValidate.java` | `MapValidate DIR` | runs the engine's `LiveMap.assertIsValid()` on every `.map26` in DIR |
| `MapCmp.java` | `MapCmp a.map26 b.map26` | field-by-field equality (walls, dirt, mines, cheese, seed, symmetry, bodies) |

```sh
JAR=$(ls ~/.gradle/caches/modules-2/files-2.1/org.battlecode/battlecode26-java/1.2.5/*/battlecode26-java-1.2.5.jar)
FB=$(find ~/.gradle/caches/modules-2 -name 'flatbuffers-java*.jar' | head -1)
mkdir -p /tmp/replaytools && javac -cp "$JAR:$FB" -d /tmp/replaytools tools/replays/*.java
java -cp "$JAR:$FB:/tmp/replaytools" MapValidate maps
```

Official tournament replays are public: see docs/intel.md §1 (API) and §7 (how the tournament maps in `maps/` were
recovered). `references/tournament/bc26-champion-replay-summary.txt` is ReplaySummary's output over every official
tournament game the champion played.
