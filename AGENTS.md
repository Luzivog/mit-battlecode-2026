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
- `maps/`: the official, tournament and team-made maps (all valid). `test_maps/`: scenario maps.
- `jinja.py`: the code generator. `tools/`, `autoscrims/`, `scripts/`: tooling, each tool with `--help`.
- `references/postmortems/`: every public Battlecode postmortem, as text (`INDEX.md`).
- Remote: `origin` only (ours, private). The champion's ~30 experiment branches are archived in it under
  `refs/champion/*`: `git fetch origin '+refs/champion/*:refs/remotes/champion/*'` makes them `champion/<branch>`.
- `~/battlecode26-data/`: data, caches and scratch. Never `/tmp` for anything worth keeping.

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
10. Keep instructions short and plain: no history, no quotes, no README files. Never estimate how long work will take.

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
```

Judging a change: `docs/evaluation.md` (`tools/games`). Until it lands, `docs/testing.md` §0 (`autoscrims/ABtest.py`).
