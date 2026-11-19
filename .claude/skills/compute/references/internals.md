# Compute internals

For changing `tools/compute`, `tools/run`, `tools/job.sh` or `tools/piece.py`. To run work, read `../SKILL.md`.

No daemon and no state of our own: Slurm, Condor and the folders on `/vol/bitbucket/user/bc26` are the record.
Ported from the previous project's tooling (prior-battlecode-tooling, `tools/compute`, `tools/job.sh`), trimmed
to what a Java bot repo needs: no test queue and no follower service.

## Files

| file | what |
|---|---|
| `tools/remote.py` | ssh (the only hosts allowed), logins, Kerberos for Condor submits, `REMOTE_ROOT` |
| `tools/backends.py` | reading Slurm, cpucluster1 and Condor; where a job can start now; `condor_sub` |
| `tools/jobs.py` | a job: code bundle, folder, submit, state, logs, wait, pull, cancel |
| `tools/provision.py` | the runtime on the volume: Temurin 21, CPython 3.14.8, the engine's jars |
| `tools/kit.py` | the game kit's classpath, here and on a node |
| `tools/runs.py`, `tools/piece.py` | runs (client) and pieces (node); the pool |
| `tools/compute.py`, `tools/grid.py` | the CLIs `tools/compute` and `tools/run` |
| `tools/kitgame` | a smoke game runner: the same game here and on a node |
| `tools/fixtures/` | reads captured from DoC (18 Nov 2026), the verification games, `launchcost.sh` |

## A job

`tools/compute run` ships HEAD as a git bundle of it and `main` (34 MB), `--with` paths as one gzip'd tar, and
submits `job.sh JOB_DIR BACKEND`. A job is the folder `REMOTE_ROOT/jobs/ID` (ID: `condor|slurm|cpucluster1-MMDD-HHMMSS-XXXX`);
its files are `tools/jobs.py`'s docstring's, an interface `job.sh` reads. `job.sh` clones the commit into scratch,
unpacks `with.tgz`, exports the `env` file's names (JAVA_HOME; the JDK's and Python's `bin` first on PATH) and, with
`kit`, `BC_ENGINE_JARS` and `BC_CLASSPATH`; HOME, TMPDIR and XDG_CACHE_HOME are scratch. It sets `JOB_CPUS` (nproc,
or Slurm's ask where nproc shows more) and `JOB_MEM_MB` (Slurm's allocation, else MemAvailable). The command's output
goes to `log` through a FIFO and `head -c` (64 MB at most). `exit` is written in place once the job has ended for
good; `code.bundle` and `with.tgz` go then. A setup failure ends 255, a file left in HOME 253.

Reading jobs (`read_states`): one login a cluster reads every folder (exit, backend-id, cancelled, started, memory
peak) and our whole queue (`squeue -u`, `condor_q -name SUBMIT $USER`: a list of ids fails whole on one Slurm has
forgotten). A job its backend no longer lists with no exit code is final only after 120 s (`GONE_GRACE_S`): an exit
file written on a node can stay unseen on a login node (NFS attribute cache). `wait` and `logs -f` poll every 30 s,
and a failed login is retried at the next poll.

## A run

`tools/run` makes `REMOTE_ROOT/runs/RUN` (its files: `tools/runs.py`'s docstring) and submits pieces: jobs running
`python3 RUN/piece.py RUN`, the run's `code.bundle` and `with.tgz` hard-linked into each. Each piece claims tasks by
`mkdir claims/N` (atomic on the NFS server) and runs `min(JOB_CPUS // task_cpus, JOB_MEM_MB // task_mem_mb)` at once.

Shipping, once a content: `code.bundle` and `with.tgz` are hard links to `REMOTE_ROOT/cas/code-KEY.bundle` and
`cas/with-KEY.tgz`. The code's KEY is its commit and `main`'s: the working tree's snapshot commit has a fixed author,
committer and date (HEAD's), so the same tree on the same HEAD is the same commit. The files' KEY hashes their names,
modes and bytes, not their times. One login finds which are there (touching them); a missing one is made here (the
bundle takes 4 s) and uploaded under a temporary name, renamed in. A run of unchanged content uploads nothing.
It touches `claims/N/beat` every 30 s; a claim not touched for 300 s (the server's clock: a fresh file's mtime) is a
dead piece's, and any piece takes it over by renaming it into `lost/`. A task has 3 starts; then it ends `-1`. A
task's result is `done/N.tar` (`result.json`: code, peak RSS, host, seconds, began, ended, slots; `log`, its last
1 MB; `out/`, at most 1 GB), written under a temporary name and renamed in. SIGTERM (cancel, eviction) or SIGUSR1
(Slurm's `--time`) stops a piece: its tasks are killed and their claims moved into `lost/`, and it exits 0 (after
SIGUSR1, `job.sh` requeues it).

Placement (`runs.offers`): each backend allowed is read at once; offers are sorted by tasks at once, then CPUs, then
cpucluster1, Condor, the GPU cluster. A Slurm or cpucluster1 piece asks only the CPUs the tasks left can use; a lab
machine is taken whole. Condor pieces are anonymous (requirements and a rank by the workers a machine holds), so a
big machine can take every task and a second piece end at once. GPU-cluster slots go one at a time, each from a read
after the last submit. When nothing is free, 4 Condor pieces wait in its queue. A piece counts once its backend took
it (appended to `pieces`).

The follower (`runs.follow`, behind `tools/run` and `tools/compute wait RUN`) polls every 30 s: fetches new
`done/` tars over one tar stream, reads the pieces' states, and when every task is done writes `closed` and cancels
pieces that never started. When no piece is left and tasks remain: a held piece stops the follower with exit 3, the
run left open (a hold is reported, never routed around); a piece that ended with a code other than 0 or 75 stops the
run (its log shown: it would fail again); otherwise a replacement round is submitted, once unclaimed tasks exist or
the dead claims are stale, at most 3 rounds (`mkdir replace.K` lets one follower submit each). A pooled run follows
the pool's rule below instead. A run no piece could be submitted for is closed at once.

## The pool

A run of tasks of a set size (not `--whole`) and no `--max-cpus` is pooled (`run.json`'s `pool`, `pool_v`): its
pieces serve every pooled run, and every pooled piece serves it.

- **Open runs.** `REMOTE_ROOT/pool/open/RUN`, an empty file, made last in the login that makes the run's folder and
  removed by `close` and `cancel` (and by a piece once every task is done; the sweep removes those of closed or deleted
  runs).
- **Claiming across runs.** A piece with a free slot lists `pool/open` (at most every `SCAN_S`, 2 s; at once after a
  task ends) and reads each new run's `run.json` once. It serves a run with the same `task_cpus`, `task_mem_mb` and
  runtime (`env`: the hash of `provision.env_file()`), whose `wheres` hold its backend, and, when it has a deadline
  (Slurm's `--time` less SIGUSR1's lead, Condor's `--time`), with at least `min(MIN_LEFT_S, limit / 2)` left (20 min,
  or half its limit) and 1.5 times the longest task of that run it ran. A run found with nothing claimable is skipped
  for `FULL_S` (30 s); its claims are read for dead pieces' at most every `STALE_CHECK_S` (120 s). Claims, beats,
  takeover, `MAX_STARTS` and results are a run's own, as above, whoever runs its tasks; a result run by another run's
  piece names that run (`piece_run`).
- **Fairness.** Each free slot goes to the run with the fewest running tasks (claims not done), the older run first
  among equals. A run with nothing running is beaten only by older runs with nothing running, which are finitely many
  and finite, so no run waits forever; a newcomer beats every run that has a task running, so a small batch behind a
  big one starts at the next free slots.
- **Another run's checkout.** A task runs in its run's checkout: the piece's own (job.sh's) for its run and runs of
  the same content and kit; else one the piece makes in its scratch from that run's `code.bundle` and `with.tgz`,
  job.sh's way (2.0 s on a lab machine, 19 Nov), kept for later runs of that content (`CHECKOUTS` unused ones at
  most). A kit run's `kit` file names its jars; the task gets that run's `BC_CLASSPATH` and `BC_ENGINE_JARS`.
- **The exit rule.** A piece ends as soon as it runs nothing and a last look at every run (skips ignored) finds
  nothing it may claim: no idle grace. A run's `closed` stops that run's tasks on every piece (checked every
  `CLOSED_S`, 5 s), and the piece goes on with other runs' tasks.
- **Live capacity.** `pool/pieces/JOB` is a pooled piece's state (slots, running, task size, runtime, backend,
  deadline, limit), rewritten through a rename as it changes (at most every 5 s, at least every 30 s) and removed at
  its end; one older than 90 s (by the volume's clock) is a dead piece's.
- **Placement.** A new pooled run asks for pieces for its tasks less the free slots of the live pieces that would
  serve it (`runs.place`); `tools/run` says so (`2 live pieces have 9 free slots: no new piece`).
- **Following.** The follower adds pieces (`replace.K`, `POOL_ROUNDS` 8 at most) when tasks wait (unclaimed, or
  claimed by dead pieces while none of its own pieces runs) beyond the live pieces' free slots, never while one of its
  pieces is queued or held, or just started, and at most every `GRACE_S` (20 s). Its own pieces' end stops a pooled
  run only once none of its tasks moved for 90 s. The summary adds `pieces_requested` and `served_by_others`.
- **Cancel.** `tools/compute cancel RUN` closes a pooled run and cancels its pieces that are queued or held; its
  running pieces stop its tasks and go on with other runs' tasks, or end at once.
- **Compatibility.** Runs made before the pool have no `pool` in `run.json`: no pooled piece touches them, and their
  pieces serve them alone, as before. A piece serves only runs of its own `POOL_V`: a change to the protocol bumps it.

## The games interface (track E)

`tools/play SPECS.jsonl OUTDIR --workers N` plays every game of SPECS one JVM a game and writes
`OUTDIR/results.jsonl`. On Imperial:

```sh
tools/run --kit --detach --each SPECS.jsonl --chunk K --gather results.jsonl -- tools/play {file} {out} --workers 1
tools/compute wait RUN --json      # in the background; exit 0 when every task ended 0
```

- SPECS is cut into tasks of K consecutive lines, in order; task N holds lines N*K to N*K+K-1. `{file}` is a file of
  them, `{out}` the task's OUT. A task runs in the run's checkout, in the folder `tools/run` was started from, with
  `JOB_CPUS` (`--task-cpus`, default 1), `JOB_MEM_MB` (`--task-mem`, default 1700), `BC_CLASSPATH`,
  `BC_ENGINE_JARS`, `JAVA_HOME`, `java` and `python3` (CPython 3.14.8, standard library only: no `.venv` packages on
  nodes) on PATH, `TASK`, `RUN_ID`, `TMPDIR`.
- Every core: a machine runs `min(CPUs // task_cpus, MemAvailable_MB // task_mem_mb)` tasks at once. With
  `--task-cpus 1`, `tools/play --workers 1` plays its chunk one game after another; with `--task-cpus N`, a task may
  run N workers (`--workers {cpus}`) and needs `--task-mem` for all of them.
- Gathered: `runs/RUN/results.jsonl` is every task's `out/results.jsonl` concatenated in task order, so in SPECS's
  order when `tools/play` writes its chunk's lines in order. Each task's own files are `runs/RUN/N/out/`, its output
  `runs/RUN/N/log`, its record `runs/RUN/N/result.json`; `runs/RUN/summary.json` is the `--json` summary
  (`ok`, `failed`: task numbers, `codes`, `task_s_total`, `task_peak_max`, `hosts`, `exit`).
- A failed task (any non-zero code) is reported, not retried. A task whose machine was taken back (Condor eviction,
  a lab machine's owner, a node failure) runs again elsewhere from the start, with an empty OUT: it needs no
  checkpoint. Keep a task to a few minutes (K games), so a loss costs little and the run's tail is short.
- Sizing: `tools/compute status --json` gives `task_slots_now` per backend for a task size
  (`--task-cpus`, `--task-mem`).
- Launches: one JVM a game is one `java` launch a game, plus one `tools/play` a chunk. At 1.5 ms of exec auditing a
  launch on a lab machine (measured) and one game a minute or so per CPU, that is noise; DoC's process accounting logs
  each launch, so never fork per line or per round.
- The same command plays the same game here: `tools/kit.py`'s `classpath()` is `BC_CLASSPATH` on a node and
  `build/classes` plus the Gradle cache's jars here.

## Backends

- **Condor** (lab machines, `--where condor`): submit host `ash04.doc.ic.ac.uk`, the only lab machine we ssh to, one
  login a call, no kept connection. Reads go through a GPU-cluster login node. Submits and removals need a Kerberos
  ticket there: `kerberos_login` runs kinit with Bitwarden's password (item "Imperial College"), never twice with a
  password the server refused (its sha256 in `~/battlecode26-data/kinit-refused`; the previous project's record is
  read too), never an empty one. ash04 timed out during ssh's banner exchange at times (18 Nov): a login to it that
  failed before connecting is tried again (3 tries, 10 s apart); one that may have run its command never is. A
  condor_submit whose answer was lost is reconciled with the queue (read through a login node): a job Condor holds
  gets its backend-id, any other folder is removed. `request_cpus = 1` takes the machine's one static slot, so every core; requirements
  are our releases (Ubuntu 18.04 to 26.04), the task's CPUs and memory; rank by the tasks the machine holds, the
  machine of the last start last. No file transfer: everything is on `/vol/bitbucket`. `--time` becomes
  `periodic_remove`. An eviction's SIGTERM leaves no exit file, and Condor runs the job again. The one hold Condor
  releases itself (HoldReasonCode 7, subcode 2: the first machine couldn't see the new folder yet) is waited through.
- **Slurm** (the GPU cluster's CPUs, `--where slurm`): login nodes `gpucluster2`, `gpucluster3`. A CPU job holds one
  card (the weakest free one), one partition, `--nodelist` of the slot's node. Slots are proposed from `sinfo`,
  `scontrol show assoc_mgr` (our QoS caps: multigpu 48 CPUs, 3 cards, 8 jobs, 32 CPUs a node; tempgpu 64 CPUs, 4
  cards) and our pending jobs, then put to `sbatch --test-only`: a slot counts only if Slurm starts it now. DoC's
  job_submit.lua gives an ask of up to 9 CPUs whole and rewrites a larger one to tasks of 6 (`whole_ask`). Every sbatch
  is tested first and isn't queued when Slurm says it would never start.
- **cpucluster1** (`--where cpucluster1`): login node `cpucluster1.doc.ic.ac.uk`, its own Slurm (partitions amd24,
  amd48, amd96; 7 AMD EPYC nodes). Our QoS: 288 CPUs, 512,000 MB, 32 CPUs and 192,000 MB a job a node, 200 jobs.
  Its lua wants `--ntasks=1` stated. A job asks `--mem-per-cpu` (the task's memory), not the 4,000 MB default, so our
  memory cap doesn't bind first.
  - **It counts only while it can take jobs.** Its login node is its Slurm controller, which saves every batch job's
    script and environment into its StateSaveLocation (`/var/spool/slurm/shared`); with that filesystem full, every
    sbatch fails "Batch job submission failed: I/O error writing script/environment to file" (Slurm 25.05.1's
    `job_mgr.c`, `_copy_job_desc_to_file`, which `--test-only` skips, so the probe passes and the submit fails). On
    18 Nov 2026 `/var` was full for hours: 53, 15 and 0 MB free (bytes, not inodes: 38% used). So cpucluster1's read
    (`backends.CPU_READ`, the same login) ends with this host, the controller's, the StateSaveLocation and its free KB
    (`df -Pk`), and `takes_jobs` is false while the controller is this host and has under 1 GB free there
    (`STATE_FREE_MIN_KB`). Then `status` (text and JSON) and placement leave it out without a word: no row, no note,
    `task_slots_now` 0. An unreachable login node is left out the same way. When DoC clears it, the next read counts
    it again. Asked for alone by name (`tools/run --where cpucluster1`, `tools/compute run --where cpucluster1`), it
    is not gated: the submit goes to sbatch, whose own refusal the agent reads.
  - A job without a batch script (`srun`, `salloc`) skips that write, but cpucluster1's job_submit.lua answers every
    such job "Interactive jobs restricted to one hour, use sbatch afte testing your scripts": DoC's rule, so its jobs
    go by `sbatch` only.
- **local** (tests only, when `BC26_LOCAL_REMOTE` is set): every login runs here with bash in that folder, and a job
  is `job.sh` run here under `taskset -c 0,1`.

## DoC rules

Enforced at submit time: ssh only to the Slurm login nodes, cpucluster1's and the Condor submit host
(`remote.ssh_argv`); one partition a job (`jobs.sbatch`); a card held by every GPU-cluster job; `request_cpus = 1` for
Condor; `--test-only` before every sbatch; the root 0700 (`private_root`) and every remote command under `umask
077`. It never cancels and resubmits a job: a run's replacement pieces are new pieces for tasks whose pieces were lost,
three rounds at most. These are yours:

- **No SSH to lab machines, ever**, not even a probe or an idle session: any process started over SSH closes the whole
  machine to everyone's Condor jobs (CSG's rule). Reach them only through `tools/compute`.
- **No cancel-and-resubmit loops.** Don't cancel a pending job to try another partition: pick it once, from `status`.
  Never cancel a held job or route around it with `--where`: report the job id and its hold reason.
- **Within Slurm's normal limits.** Never work around a limit, never hold an idle allocation, and cancel a job you no
  longer need.
- **Few launches per machine**: DoC logs every program launch on a lab machine (auditd and process accounting are
  active: measured below), so a job starts few processes per game.
- **Bounded writes on /vol/bitbucket**, DoC's shared NFS scratch with no per-user quota (99% full, 2.9 TB free on 18
  Nov). A job's log stops at 64 MB, a task's log at 1 MB and its OUT at 1 GB; ended jobs and closed runs are deleted
  after 7 days, a run nothing touched for 30 days too, and a closed run's code bundle at once (`tools/compute
  sweep`, run by `tools/run` and `tools/compute run` at most once a day). A stored bundle or shipped file in `cas/`
  that no run or job links any more goes 2 days after its last use; `pool/pieces` files silent for a day go. Never
  `chmod` anything open.
- **Quotas are facts.** The NFS home has a quota; nothing of ours writes to it (HOME is scratch in a job).
- **First jobs small**: a first job on a backend, or of a new kind, runs under 10 minutes.

## Verification (18 Nov 2026)

| backend | job / run | what | result |
|---|---|---|---|
| cpucluster1 | `cpucluster1-1118-204651-e9ae` (Slurm 145726, auklet01) | trivial job | Temurin 21.0.13, Python 3.14.8, `out/` pulled |
| Slurm | `slurm-1118-204728-1019` (Slurm 1175603, dove01) | trivial job | the same |
| Condor | `condor-1118-204748-71d0` (gallifrey) | trivial job, first Kerberos login | the same |
| Slurm | `run-1118-205020-50c4` (dove01) | 6 games: challenger v awubot, both seats, DefaultSmall, evileye, tiny | all 6 equal to local (replay sha256) |
| Condor | `run-1118-205022-b94d` (gallifrey) | the same 6 games | all 6 equal to local |
| cpucluster1 | `run-1118-205018-da1b` | the same 6 games | refused: the controller's I/O error above |
| cpucluster1 | `run-1118-210912-9a55` (auklet03) | the same 6 games, once it took jobs again | all 6 equal to local |
| Slurm | `run-1118-205600-9944` (dove02 36 CPUs, dove01 12) | 48 games: quick pool, both seats, embedded seeds and seed 1 | all 48 equal to local |
| Condor | `run-1118-205557-957e` (gallifrey; a second piece on hedwig found every task claimed and ended) | the same 48 games | all 48 equal to local |
| cpucluster1 | `run-1118-211244-d5da` | the same 48 games | refused again (the controller's I/O error); closed |
| both | `condor-1118-205433-e3d8` (suria), `slurm-1118-205442-8a4b` (dove01) | `tools/fixtures/launchcost.sh` | below |
| Condor | `run-1118-211953-c86f` (gallifrey) | `tools/run -- CMD`: one task on a whole machine, exit 3 | JOB_CPUS 80, its output and exit 3 back |

Launch cost (`launchcost.sh`): clankerland `/bin/true` 0.2 ms, `java -version` 14 ms; suria (lab, i9-13900K, auditd
and acct active) 1.5 ms and 89 ms; dove01 (GPU-cluster VM, auditd and acct active) 0.9 ms and 60 ms.

Throughput, the 48 quick-pool games (both seats, 12 maps, two seeds; 29,816 rounds), all at once:

| machine | slots (tasks at once) | mean game | games a minute at full load | peak RSS a game |
|---|---|---|---|---|
| gallifrey (Condor, Xeon Gold 6230, 80 threads, 187 GB) | 80 (48 used) | 222 s | 13 (48 running); 21.7 if all 80 run | 1.65 GB |
| dove02 (GPU cluster, t4 node, 36 of 48 Skylake vCPUs) | 36 | 165 s | 13.1 | 1.67 GB |
| dove01 (GPU cluster, 12 CPUs) | 12 | 162 s | 4.4 | 1.28 GB |
| auklet03 (cpucluster1, EPYC 7402P, 6 CPUs; the 6-game set) | 6 | 131 s | 2.7 | 0.81 GB |
| clankerland (Ryzen 7 9700X, 8 workers, shared) | 8 | 49 s | 9.7 | |

A machine runs `min(threads, MemAvailable / 1700 MB)` games: most lab machines have 16 or 24 threads and 14 to 30 GB,
so 8 to 16 games. Small runs land on one big machine (Condor's rank prefers the most workers), and a piece that starts
after every task was claimed ends at once.

## The pool, measured (19 Nov 2026)

Four `tools/games --remote` tests at once on each side, in the same window: one of 380 games (`goal`, 190 cells
forced) and three sequential ones (`quick`, 16 to 64 cells), main's tools (1119-053726-ab-3948, 1119-053727-ab-a23f,
1119-053728-ab-6ec1, 1119-053729-ab-5e77) against the pool's (1119-053726-ab-ef14, 1119-053727-ab-ee6f,
1119-053727-ab-ff1e, 1119-053728-ab-738d). `tools/games` unchanged (batches every 30 s, 3 games a task).

| | main's tools | the pool |
|---|---|---|
| batch runs: submission to first task start | median 20 s, p90 47 s, max 1,043 s (24 runs) | median 3 s, p90 16 s, max 16 s (16 runs) |
| a batch's end beyond its longest task | median 23 s, mean 60 s | median 3 s, mean 16 s |
| sequential tests' wall time | 809, 1,636, 876 s | 413, 384, 384 s |
| the 380-game test's wall time | 1,302 s | 916 s |
| pieces requested, the four tests | 34 | 21 (refill batches: none; 64% of tasks ran on another run's piece) |

- A refill batch found free slots on live pieces every time and started in 1.3 to 13.4 s.
- No idle grace: in both pooled experiments no run asked for a piece within 300 s after a piece ended (0 of 4 and
  0 of 13 piece requests); every request came while the pool was full.
- Fairness under contention, with the real `piece.py` on a fixed 6 slots (three pieces) busy with a 60-task run (20 s
  of backlog): five 2-task runs listed every 3 s each started both tasks within 0.1 to 1.1 s, where first-come
  first-served would have made them wait 10 to 22 s; each overtook 27 to 48 queued tasks of the big run, which still
  ended (its last start 25 s in, against 20 s alone).
- A warm submission (`tools/run --detach` with nothing to upload and free slots live) is six logins of 0.2 s each;
  the code bundle takes 4 s to make and 34 MB to upload once a content; placing a Condor piece takes 8 to 10 s.
- First real runs of the kind: `run-1119-051724-890a` (6 tasks, its piece on rowan05 with 7 slots),
  `run-1119-051819-4db6` (its 3 tasks on that piece's free slot, the first 3.2 s after submission),
  `run-1119-051830-518b` (another content: its checkout made on rowan01 in 2.0 s, by run-1119-051819-4db6's piece).

