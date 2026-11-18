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
  task's `out/NAME`, in task order, into `runs/RUN/NAME`.
- A task gets `JOB_CPUS` CPUs (`--task-cpus`, default 1) and `JOB_MEM_MB` (`--task-mem`, default 1700M: a game's JVM
  peaks at 0.5-1.6 GB). A machine runs as many tasks at once as its CPUs and its memory both hold.
- `--with PATH` ships another git-ignored file or folder. `--where condor,slurm,cpucluster1` limits the backends.
- It prints the run's id first, then waits until every task has ended; the exit code is 0 when every task ended 0,
  else the first task's code. A task whose machine was taken back runs again elsewhere, three starts at most.

## Wait in the background

Start the wait as a background command; its exit wakes you:

```sh
RUN=$(tools/run --kit --detach --each specs.jsonl --chunk 10 -- tools/play {file} {out} --workers 1)
tools/compute wait "$RUN" --json          # run_in_background; exit = the run's status; results in runs/RUN/
```

- `tools/compute wait ID...` takes runs and jobs, polls every 30 s, survives lost connections, fetches results as
  they come, and ends with one line an id saying where they are. Run it again after a restart: it picks up.
- Or run `tools/run ...` itself in the background: same output, same exit code.
- `tools/compute status [--json]`: what each backend can start now, in tasks at once, and our jobs.
- `tools/compute logs ID [-f]`, `tools/compute cancel ID` (a job, or a run and its pieces).

## Games at scale

`tools/play` (docs/evaluation.md) plays a JSONL of games with one JVM a game; on Imperial it runs as
`tools/run --kit --each SPECS.jsonl --chunk K --gather results.jsonl -- tools/play {file} {out} --workers 1`, and the
results come back as `runs/RUN/results.jsonl` in SPECS's order. The exact interface (what a task gets, how SPECS is
cut, what comes back, how a lost machine is handled): references/internals.md, "The games interface".

## One job on a chosen backend

```sh
tools/compute run --where condor|slurm|cpucluster1 [--cpu N] [--mem 8G] [--time 2h] [--kit] [--with PATH] -- CMD
```

It runs HEAD (commit first), prints the job id, and returns. `--time` is required on Slurm and cpucluster1 (SIGUSR1
comes 120 s before it: save to `$OUT`, exit 0, and the job requeues itself). Results: `tools/compute wait ID` pulls
`log`, `exit` and `out/` to `runs/ID/`.

## Measured (18 Nov 2026)

- Remote games equal local ones byte for byte (winner, round, replay sha256): 108 games on Condor, the GPU cluster
  and cpucluster1, none different (references/internals.md, "Verification").
- A game runs 3 to 6 times slower on DoC's CPUs than on clankerland (DefaultSmall: 20 s here, 62 s on a lab Xeon or
  a GPU-cluster node, 120 s on a busy cpucluster1 node). A JVM peaked at 1.67 GB.
- Throughput on the quick pool (48 games, mean 620 rounds), at full load: a Condor lab machine of 80 threads
  (gallifrey), 48 games at once, 222 s a game: 13 games a minute; a GPU-cluster slot of 36 CPUs (dove02), 165 s a
  game: 13 a minute; clankerland, 8 workers, 49 s a game: 9.7 a minute. About 0.3 games a minute per busy CPU on DoC.
- Free on 18 Nov at 21:13 (`status`), in game tasks at once: Condor 2,654 (266 idle lab machines, 4,884 CPUs; most
  machines hold 8 to 16 games, memory binding first), the GPU cluster 36 (its best slot; up to 112 CPUs under our
  caps), cpucluster1 148 (288 CPUs under our cap).
- A launch on a lab machine costs 1.5 ms (`/bin/true`; 0.2 ms here) and `java -version` 89 ms (14 ms here): DoC's
  auditd and process accounting log every exec. One JVM a game is fine; avoid scripts that fork per line.

## Rules

- Never SSH to a lab machine: the tools reach only the Slurm login nodes, cpucluster1's and the Condor submit host.
- Never cancel a job to resubmit it. If one fails or is held, report its id and why.
- No idle allocations: runs close themselves and cancel pieces that never started; cancel a job you no longer need.
- One partition a job; a Condor job takes its whole machine (`request_cpus = 1`). The tools enforce both.
- A first job on a backend, or of a new kind, is small and under 10 minutes (`--time 10m`).
- Our files on `/vol/bitbucket/user/bc26` only (0700, written with umask 077); never another project's folders,
  jobs or services. Ended jobs and closed runs are deleted after 7 days (`tools/compute sweep`).
- Never work around a limit. If a backend refuses, report its exact error.

## Changing the tools

Read `references/internals.md` before changing `tools/compute`, `tools/run`, `tools/job.sh` or `tools/piece.py`.
Tests: `.venv/bin/pytest tools/`.
