# tools/fightlab: the fight lab

Measures the micro on the fights of real games: a game of the lab copy against awubot is forked at a fight, and from
the fork on each arm plays its own values of the micro's constants; every arm shares the fork's state. Why and how
it predicts whole games: docs/theory/fl-fight-lab.md.

## Parts

| file | what |
|---|---|
| `mkcopy.py` | makes `src/flch` (never committed): challenger with Tune's constants as fields, set from the round `bc.testing.fl.r0` by `bc.testing.fl.set`; `--patch patches/codearms.py` adds past packages' code arms behind lab constants |
| `FightLab.java` | the runner: plays a JSONL of jobs on the official 1.2.6 jar in one JVM (each game as `bcplay.Play` plays it), records both teams' state at given rounds, stops at a round or plays to the end, and scans a base game for fights |
| `task.sh` | one remote task: the runner on a chunk of jobs |
| `lab.py` | the driver: `scan`, `forks`, `arms`, `fetch`, `score` (its `--help`) |

## Use

```sh
.venv/bin/python tools/fightlab/mkcopy.py --patch tools/fightlab/patches/codearms.py
(cd tools/fightlab && mkdir -p cls lib && cp ~/battlecode26-data/engine/jars/battlecode26-java-1.2.6.jar lib/ &&
 javac -source 21 -target 21 -cp lib/battlecode26-java-1.2.6.jar -d cls FightLab.java)
.venv/bin/python tools/fightlab/lab.py scan NAME --pool goal --cells 300 --seed-base B   # prints the run id
tools/compute wait RUN --json                                    # in the background
.venv/bin/python tools/fightlab/lab.py fetch NAME scan && .venv/bin/python tools/fightlab/lab.py forks NAME
.venv/bin/python tools/fightlab/lab.py arms NAME SET --arm hold0=THROW_HOLD:-1000 --arm ... [--to-end]
.venv/bin/python tools/fightlab/lab.py fetch NAME SET && .venv/bin/python tools/fightlab/lab.py score NAME SET
```

- An arm is `LABEL=NAME:V,NAME:V`; `null` (main's values) is always played. Each arm plays every fork with each salt
  (`--salts`, default 2); an arm is read against the null of the same salt.
- Games play on Java (the native engine cannot read the properties): about 70 CPU-s a whole game on DoC, less for a
  fork stopped at r0 + 80. Every stage goes to Imperial with `tools/run --kit`; never run one here.
- The lab copy differs from challenger in bytecode (fields, not constants), so its games are not main's; its base
  games are its own (the scan), and it scores like main against awubot.
- `src/flch` must be regenerated after any change to challenger, and is never committed (`git add` paths, not `-A`).
