# Battlecode 2026

MIT Battlecode 2026 "Uneasy Alliances": two teams of rats (baby rats and rat kings) gather cheese, fight, ratnap and
throw each other, and use or provoke the cats, on a map of walls, dirt and cheese mines. Bots are Java 21, run by the
official engine with a per-turn bytecode limit. This repo starts from the public code of the 2026 champion,
"subscribe to caterpillow". **The goal: `src/challenger` wins at least 90% of games against the champion
(`src/awubot`)** under official conditions, without regressing against the other top bots. The plan and its tracks:
`docs/program.md`. The numbers so far: `docs/progress.md`.

## Where things are

- `src/challenger/`: our bot. All bot work happens here.
- `src/awubot/`: the champion's final submission (tag `caterpillow-final`), the fixed opponent. **Never edit it**, nor
  any other sparring package: `src/awubot_{quals,us,mystery,sus}` (the champion's older snapshots, docs/history.md),
  `src/opp_*` (other teams' public bots, docs/opponents.md), `src/afk`, `src/suicide`.
- `src/battlecode/`: local engine overrides (seed handling, docs/testing.md §3). Never edit.
- `engine/`: a native build of the official engine (1.2.6 and 1.2.5) that plays the same games, replay bytes included,
  at 30-50× less CPU (`engine/AGENTS.md`). `src/fuzz_*` are its test bots.
- `maps/`: the official, tournament and team-made maps (all valid). `test_maps/`: scenario maps.
- `jinja.py`: the code generator. `tools/`, `autoscrims/`, `scripts/`: tooling, each tool with `--help`.
- `references/postmortems/`: every public Battlecode postmortem, as text (`INDEX.md`).
- Remote: `origin` only (ours, private). The champion's ~30 experiment branches are archived in it under
  `refs/champion/*`: `git fetch origin '+refs/champion/*:refs/remotes/champion/*'` makes them `champion/<branch>`.
- `~/battlecode26-data/`: data, caches and scratch. Never `/tmp` for anything worth keeping. The tools prune it
  ("Disk" in `tools/bcgames/AGENTS.md`): a run's replays go after 24 h unless you `tools/games keep ID` it (do so for
  any run whose replays a doc cites); `tools/games replays ID` plays them again. `./gradlew unpackClient` gets the
  replay viewer.

## Read before you change

| doc | read it when |
|---|---|
| `docs/rules.md`, `docs/api-cheatsheet.md` | always, before reasoning about a mechanic; the engine source is the truth, the `battlecode26-specs.txt` files are stale |
| `docs/architecture.md` | before touching any bot code: turn lifecycle, comms layout, invariants (§9) |
| `docs/codegen.md` | before editing a `.jinja2` template |
| `docs/strategy.md`, `docs/combat.md` | before changing macro or micro; each has the exhaustive constants table |
| `docs/intel.md`, `docs/opponents.md`, `docs/lessons.md` | when choosing what to improve |
| `docs/testing.md`, `docs/evaluation.md` | before judging a change |
| `docs/theory/`, `docs/plan.md` | the theory and the work packages (track T) |
| `docs/closed.md` | before trying a change: directions measured and rejected |
| `docs/theory/w3-step-back.md` | where games are decided (the state from r150, the rat exchange in r1–300), oracle prices of awubot's grabs, throws and traps, and the levers that are closed |
| `docs/theory/w2-classes.md` | why main still lost after wave 3: loss table, the centre-mine economy, oracle-priced levers (`w1-classes.md`: after wave 1) |

## Rules

1. Never act on a mechanic from memory: find it in `docs/rules.md` or the engine source, else play it as a scenario.
2. Edit `.java.jinja2` templates, never the `.java` they generate. Regenerate with `.venv/bin/python jinja.py`
   (add `--include-maptracker` when `MapTracker` or `templates/common.jinja2` changed). It exits 0 even on a template
   error: check its output for `[error]` and `git diff --stat` for what moved.
3. Every package under `src/` compiles together: one compile error breaks every game. Run `./gradlew build -x test`
   before every commit.
4. Assertions are on in games. An `AssertionError` (any `Error`) escapes the bot's `catch (Exception)`, ends `run()`,
   and the engine destroys the robot at the end of that turn. Add no assert that can fail. Some existing asserts do the work (`assert(performMoves(...))`):
   never delete one as a no-op.
5. Move only through the bot's wrappers (`docs/architecture.md` §6, §9), never `rc.move`/`rc.turn` directly.
6. Bytecode is tight (rats average ~11k of 17.5k, kings ~13k of 20k). Any change shifts bytecode and randomness, so
   close games flip: judge a change only with the evaluation tools, never on a few games.
7. A number in the bot comes from the rules or a measurement. Find the root cause before changing anything; fix by
   deleting first, simplifying second, adding last. No fallbacks.
8. Work in your own worktree on your own branch, commit small, push your branch to `origin`. Never push `main`,
   never open a PR. The integrator merges.
9. Heavy work (many games, engine builds, long runs) goes through the Imperial tools, `tools/run` and `tools/compute`
   (the compute skill, `.claude/skills/compute/`).
   This machine (clankerland, 16 cores) is shared: never stop or change anything you didn't start.
10. Wait only in the background. Start every wait (`tools/games wait ID`, `tools/tune wait`, `tools/compute wait`, a
   long build) as a background command, then do other work or end your turn: its exit wakes you. Never poll in the
   foreground (`until ...; do sleep ...; done`, a foreground wait): while it runs you read no result and no message.
   Wait so that each result wakes you: `tools/games wait --any ID ID...` returns at the first verdict (and names the
   rest), `tools/tune wait NAME --next` at each run's end.
11. Keep instructions short and plain: no history, no quotes, no README files. Never estimate how long work will take.

## Commands

```sh
uv venv -q --allow-existing .venv && uv pip install -q -p .venv -r requirements.txt   # once per worktree
.venv/bin/python jinja.py                                   # regenerate challenger from its templates
./gradlew build -x test                                     # compile every package
./gradlew run -PteamA=challenger -PteamB=awubot -Pmaps=DefaultSmall   # one game (replay in matches/)
git diff --quiet caterpillow-final -- src/awubot && echo baseline intact
tools/compute status                                        # free Imperial capacity per backend
tools/run --kit -- CMD                                      # one command on a whole Imperial machine
tools/compute wait ID --json                                # run in the background: its exit wakes you
./gradlew classes && .venv/bin/python engine/build.py      # build the native engine with every bot (engine/build/bcx)
engine/build/bcx play SPECS.jsonl OUTDIR --no-indicators  # native games; same specs and results as tools/play
tools/enginebench/enginebench score                        # the engine-speed eval (tools/enginebench/AGENTS.md)
tools/tune params                                           # tune src/challenger/Tune.java's constants (tools/bctune/AGENTS.md)
tools/book/book check                                       # the per-map table, src/challenger/book.jsonl (tools/book/AGENTS.md)
```

Judging a change: `docs/evaluation.md`. `tools/games ab challenger awubot --pool POOL` (gain test: the branch against
main, both playing awubot on the same cells) prints an ID; run `tools/games wait ID` in the background (exit 0 =
passed; `wait --any ID...` for several), then `tools/games show ID` and `tools/games diff OLD NEW`. A gain smaller than
the test's δ merges by the precision route (`docs/evaluation.md`, Protocol item 5). A change that wins only on
some maps goes in the per-map table (`docs/evaluation.md`, "The book"): an entry per map and seat, each with its evidence. Add `--remote` to play on Imperial. Games play on
the native engine (`engine/`) with a same-machine exactness check; `--backend java` plays on the official engine.
Tests are the slow part of the work: run independent ones in parallel (Imperial has thousands of free slots),
never block on one in the foreground, and ask for replays in the test (`--replays losses|all`) when you'll read them.
