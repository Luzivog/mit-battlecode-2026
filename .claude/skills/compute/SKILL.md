---
name: compute
description: Use before running anything heavy (many Battlecode games, long or parallel commands, engine builds) or touching DoC's Condor lab pool, Slurm GPU cluster or cpucluster1, and to wait for a remote job or run in the background.
---

# Compute

## Run heavy work

```sh
tools/run --kit -- CMD...                          # CMD once, alone on a whole remote machine
tools/run --kit --each FILE -- CMD {}              # CMD once a line of FILE, on every free remote CPU
tools/run --kit --each FILE --chunk K -- CMD {file} {out}   # a task a K lines, {file} a file holding them
```

- Run it from your worktree. The working tree goes as it is, uncommitted changes included.
- `--kit` compiles `build/classes`, ships it, and gives each task `BC_CLASSPATH` (build/classes, then the engine's
  jars) and `BC_ENGINE_JARS`, with `java` (Temurin 21) and `python3` (3.14, standard library only) on PATH. A game is
  then plain `java -cp "$BC_CLASSPATH" ... battlecode.server.Main` (`tools/kitgame` is an example; `tools/kit.py`
  gives the same classpath here).
- Tokens: `{}` the line, `{file}` a file of the task's lines, `{out}` its OUT, `{cpus}` its CPUs. Write results to
  `$OUT`. They come back to `runs/RUN/N/out/`, the output to `runs/RUN/N/log`. `--gather NAME` concatenates every
  task's `out/NAME`, in task order, into `runs/RUN/NAME`. A day after a run's results last changed, its `N/` folders
  are packed into `runs/RUN/tasks.tgz` (`tar -xzf` it to read them) and each `result.json` is a line of
  `runs/RUN/tasks.jsonl`; the gathered files and `summary.json` stay.
- A task gets `JOB_CPUS` CPUs (`--task-cpus`, default 1) and `JOB_MEM_MB` (`--task-mem`, default 1700M: a game's JVM
  peaks at 0.5-1.6 GB). A machine runs as many tasks at once as its CPUs and its memory both hold.
- `--with PATH` ships another git-ignored file or folder. `--where condor,slurm,cpucluster1` limits the backends.
- It prints the run's id first, then waits until every task has ended; the exit code is 0 when every task ended 0,
  else the first task's code. A task whose machine was taken back runs again elsewhere, three starts at most.
- A machine with under 2 GB free where tasks write takes no task. A task that exits 75 says its machine can't
  run it: it goes to another machine and that one takes no more. A machine's tasks share `JOB_CACHE`, a folder
  for what each would otherwise copy (references/internals.md, "Room to write").
- Runs share one pool (not `--whole`, not `--max-cpus`): a live piece of any run takes tasks of every open run of the
  same `--task-cpus`, `--task-mem` and backends, the run with the fewest running tasks first, and ends as soon as
  nothing is left. A new run asks for pieces only beyond the live pieces' free slots, so its tasks often start within
  seconds. Code goes up as main once (shared by every run while main stays) and each run's own commits (a few KB);
  `--with` files once a content. A closed run's results are deleted from the workspace once they are here.
- A new run is refused while the workspace holds over 40 of its 55 GB after a sweep: the message says so.
- Pieces never pile up in a queue: our Condor pieces waiting in its queue take free machines first, and at most 4
  pieces of a kind (task size and runtime) wait over every run. When every machine is busy, a new run's tasks wait
  for the pool's free slots (`... pieces of its kind wait in Condor's queue: no more`): that is not an error. However
  many machines look free, at most 32 pieces of a kind wait in Condor's queue.
- When Condor's submit host gives no answer, runs get pieces on cpucluster1 and the GPU cluster alone (`Condor's
  submit host gave no answer ... s ago: no Condor piece now`), tried again every 10 minutes: that is not an error.

## Wait in the background

Start the wait as a background command; its exit wakes you:

```sh
RUN=$(tools/run --kit --detach --each specs.jsonl --chunk 10 -- tools/play {file} {out} --workers 1)
tools/compute wait "$RUN" --json          # run_in_background; exit = the run's status; results in runs/RUN/
```

- `tools/compute wait ID...` takes runs and jobs, polls every 30 s, survives lost connections, fetches results as
  they come, and ends with one line an id saying where they are. Run it again after a restart: it picks up.
- Or run `tools/run ...` itself in the background: same output, same exit code.
- `tools/compute status [--json]`: what each backend can start now, in tasks at once, the pool, and our jobs. It
  never waits for another process's read: the pool line says how old its snapshot is (under 30 s, or older while
  another process reads it anew); a backend that can't be read is named as unreadable.
- `tools/compute logs ID [-f]`, `tools/compute cancel ID` (a job, or a run: its tasks stop, its queued pieces are
  cancelled; its running pieces go on with other runs' tasks, or end).
- When a run's last task is done its follower writes `summary.json` first, then cancels the pieces that never
  started, all in one `condor_rm` and one `scancel`: read the summary, not the follower's exit.

## Games at scale

`tools/play` (docs/evaluation.md) plays a JSONL of games: here on the native engine where it can, else one JVM a game;
on a node (no toolchain) only specs naming a native binary (`bcx`) play natively. On Imperial it runs as
`tools/run --kit --each SPECS.jsonl --chunk K --gather results.jsonl -- tools/play {file} {out} --workers 1`, and the
results come back as `runs/RUN/results.jsonl` in SPECS's order. The exact interface (what a task gets, how SPECS is
cut, what comes back, how a lost machine is handled): references/internals.md, "The games interface".
- A lab machine's disk can fill under a run. Its games then fail with `cannot write ...` or `OSError: [Errno 28] No
  space left on device`: that is the node, not the code; re-run those games.

## One job on a chosen backend

```sh
tools/compute run --where condor|slurm|cpucluster1 [--cpu N] [--mem 8G] [--time 2h] [--kit] [--with PATH] -- CMD
```

It runs HEAD (commit first), prints the job id, and returns. `--time` is required on Slurm and cpucluster1 (SIGUSR1
comes 120 s before it: save to `$OUT`, exit 0, and the job requeues itself). Results: `tools/compute wait ID` pulls
`log`, `exit` and `out/` to `runs/ID/`.

## Sizes

- Remote games equal local ones byte for byte (winner, round, replay sha256), on every backend.
- A game runs 3 to 6 times slower on DoC's CPUs than on clankerland (DefaultSmall: 20 s here, about 60 s on a lab
  Xeon or a GPU-cluster node, 120 s on a busy cpucluster1 node); a JVM peaks near 1.7 GB. About 0.3 games a minute
  per busy CPU on DoC.
- Condor has about 340 lab machines, most holding 8 to 16 game tasks (memory binds first); the GPU cluster gives one
  slot at a time within our caps (48 and 64 CPUs, 3 and 4 cards); cpucluster1 288 CPUs under our cap.
  `tools/compute status --json` gives `task_slots_now` per backend for a task size.
- A launch on a lab machine costs 1.5 ms and `java -version` 89 ms (DoC audits every exec): one JVM a game is fine;
  never fork per line or per round.

## Rules

- Never SSH to a lab machine: the tools reach only the Slurm login nodes, cpucluster1's and the Condor submit host.
- Never cancel a job to resubmit it. If one fails or is held, report its id and why.
- No idle allocations: runs close themselves and cancel pieces that never started; cancel a job you no longer need.
- One partition a job; a Condor job takes its whole machine (`request_cpus = 1`). The tools enforce both.
- A first job on a backend, or of a new kind, is small and under 10 minutes (`--time 10m`).
- Our files in our workspace (`BC26_WORKSPACE` in `.env`, `.env.example`) only (0700, written with umask 077; a 55 GB quota); never
  another project's folders, workspaces, jobs or services. Nothing on `/vol/bitbucket`.
- **No persistent data on the shared volumes.** The workspace holds only the state of open runs and caches that make
  jobs faster (the runtime, stored code, native binaries); results live on clankerland, under the checkout's `runs/`.
  Its `MANIFEST` names every top-level entry: what it is, the tool that writes it, how long an entry lives, what
  breaks if it goes. The tools write it (`tools/compute sweep`); a new kind of entry is added to `tools/workspace.py`'s
  `ENTRIES`, never put there by hand. Ended jobs and closed runs go after 6 hours (`tools/compute sweep`, hourly).
- `tools/compute empty` says what the workspace holds and what still uses it; `--yes` deletes all of it once no job
  of ours has its folder there and no open run there moved in the last hour.
- Never work around a limit. If a backend refuses, report its exact error.

## Changing the tools

Read `references/internals.md` before changing `tools/compute`, `tools/run`, `tools/job.sh` or `tools/piece.py`.
Tests: `.venv/bin/pytest tools/*_test.py` (they never reach DoC: `tools/conftest.py`) and
`.venv/bin/python -m unittest discover -s tools/bcgames -t tools -p '*_test.py'`.
