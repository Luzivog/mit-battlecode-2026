# Compute internals

For changing `tools/compute`, `tools/run`, `tools/job.sh` or `tools/piece.py`. To run work, read `../SKILL.md`.

No daemon: Slurm, Condor and the folders of our workspace (`BC26_WORKSPACE` in `.env`) are the record, and what the
tools on this machine share (session slots, the view of the remote state, cached scheduler reads) is files under
`~/battlecode26-data/compute/`. A run's follower is `tools/run` itself or `tools/compute wait`, a process of the
agent's.

## Files

| file | what |
|---|---|
| `tools/remote.py` | ssh (the only hosts allowed), logins, Kerberos for Condor submits, `REMOTE_ROOT` |
| `tools/backends.py` | reading Slurm, cpucluster1 and Condor; where a job can start now; `condor_sub` |
| `tools/jobs.py` | a job: code bundle, folder, submit, state, logs, wait, pull, cancel |
| `tools/provision.py` | the runtime on the volume: Temurin 21, CPython 3.14.8, the engine's jars |
| `tools/kit.py` | the game kit's classpath, here and on a node |
| `tools/runs.py`, `tools/piece.py` | runs (client) and pieces (node); the pool |
| `tools/view.py` | the remote state every tool here shares, read by one process at a time |
| `tools/workspace.py` | the workspace's MANIFEST, `tools/compute empty` |
| `tools/compute.py`, `tools/grid.py` | the CLIs `tools/compute` and `tools/run` |
| `tools/kitgame` | a smoke game runner: the same game here and on a node |
| `tools/fixtures/` | reads captured from DoC, the verification games, `launchcost.sh` |
| `tools/conftest.py` | every test stays on this machine: a call that would reach DoC fails at once |

## Remote calls (`tools/remote.py`)

- **Bounded, both sides.** `login` gives a call `READ_S` (20 s), plus 1 s a MB it uploads; `login_into` (a bulk
  transfer) `BULK_S` (600 s); a call on the submit host `SUBMIT_S` (120 s); the sbatch probes and the sweep pass
  their own. Here ssh is killed `SLACK_S` after the bound; there the command runs in a session of its own beside a
  `sleep` of the bound, and at the bound that whole session is killed, children included (`BOUND_SH`). A call that ran
  out of time is `Unreachable`, as a lost connection is: a follower keeps following.
- **One kept connection a login node, never more sessions than it holds.** sshd allows 10 sessions a connection. Every
  process of ours on this machine takes one of `SESSIONS` (8) slots a host before a session, at most `BULK_SESSIONS`
  (3) of them for bulk transfers, and `SUBMIT_SESSIONS` (4) logins at once to the submit host: slots are lock files
  under `~/battlecode26-data/compute/ssh/`, freed when the process ends. A call waits its turn for a slot
  (`SLOT_WAIT_S`, 300 s), then is `Unreachable`.
- **No fall-off.** A client runs with `ControlMaster=no`, the connection's socket as ssh resolves it (`ssh -G`: `%C`
  hashes the jump host), and `ProxyCommand=/bin/false`: a session the master refuses ends 255 instead of opening a
  connection of its own. A refused session ("Session open refused by peer") never ran, so it is asked again with a
  backoff while the slot wait lasts. A connection that is gone is made again once (`start_master`, one process at a
  time, under a lock; `ControlPersist=10m`). `gpucluster3` is tried when `gpucluster2` can't be reached; a name whose
  connection was already tried is skipped.
- **Shared scheduler reads.** `remote.cached` keeps a read of `sinfo`/`condor_status`/our queues for `CACHE_S` (5 s)
  for every process here (`backends.read_*(max_age)`), and the sbatch probes too. A submit or a removal starts a new
  generation of reads (`forget`): a read begun before it is filed where no reader looks, so a placement always sees
  the pieces the last one queued.

## The shared view (`tools/view.py`)

Followers, `wait`, `status` and placement read the remote state from one local snapshot
(`~/battlecode26-data/compute/view2.json`): the pool's piece states, the open runs, the names and states of our jobs in
the GPU cluster's and Condor's queues, and, for every run and job a process here follows, the run's progress (done,
claims, pieces, closed, rounds, placed) and the jobs' folders and queue states. What a process follows is its interest
file (`interest/PID-TOKEN.json`, rewritten by each read, removed at exit, ignored after 120 s). A process whose snapshot
is older than `MAX_AGE_S` (5 s), or lacks what it follows, refreshes it under `view2.lock`: one login on the GPU
cluster (and one on cpucluster1 when a cpucluster1 job is followed), at most every `MIN_GAP_S` (2 s), whatever the
number of runs. The pieces' states are one listing of `pool/pieces` (their names), and a run's folder is read again
only once its last read is `RUN_REREAD_S` (8 s) old (a reader takes it up to `RUN_AGE_S`, 15 s): at 314 pieces and 26
open runs a refresh takes 0.3 to 0.5 s. A refresh that failed is the snapshot too: every reader is `Unreachable` until
the next. `tools/compute status` never waits for another process's refresh: it shows the snapshot when it is under 30
s old or another process is refreshing it, with its age. A follower's fetch of results (`login_into`) is the one
remote call a run still makes of its own, in a bulk session slot.

## A job

`tools/compute run` ships HEAD as a git bundle of it and `main` (35 MB), `--with` paths as one gzip'd tar, and
submits `job.sh JOB_DIR BACKEND`. A job is the folder `REMOTE_ROOT/jobs/ID` (ID: `condor|slurm|cpucluster1-MMDD-HHMMSS-XXXX`);
its files are `tools/jobs.py`'s docstring's, an interface `job.sh` reads. `job.sh` clones the commit into scratch
(from `base.bundle`, then `code.bundle`, each when there),
unpacks `with.tgz`, exports the `env` file's names (JAVA_HOME; the JDK's and Python's `bin` first on PATH) and, with
`kit`, `BC_ENGINE_JARS` and `BC_CLASSPATH`; HOME, TMPDIR and XDG_CACHE_HOME are scratch. It sets `JOB_CPUS` (nproc,
or Slurm's ask where nproc shows more) and `JOB_MEM_MB` (Slurm's allocation, else MemAvailable). The command's output
goes to `log` through a FIFO and `head -c` (64 MB at most). `exit` is written in place once the job has ended for
good; the bundles and `with.tgz` go then. A setup failure ends 255, a file left in HOME 253.

Reading jobs (`states_cmd`, `parse_states`; through the shared view): each folder (exit, backend-id, cancelled,
started, memory peak) and our whole queue (`squeue -u`, `condor_q -name SUBMIT $USER`: a list of ids fails whole on one
Slurm has forgotten). A job its backend no longer lists with no exit code is final only after 120 s (`GONE_GRACE_S`):
an exit file written on a node can reach a login node late. `wait` and `logs -f` poll every 30 s, and a failed read is
retried at the next poll.

## A run

`tools/run` makes `REMOTE_ROOT/runs/RUN` (its files: `tools/runs.py`'s docstring) and submits pieces: jobs running
`python3 RUN/piece.py RUN`, the run's bundles and `with.tgz` hard-linked into each. Before anything is written, a
run is refused while the workspace holds over `ROOM_GB` (40 of its 55 GB) after a sweep (`workspace.check_room`). Each piece claims tasks by
`mkdir claims/N` (atomic on the NFS server) and runs `min(JOB_CPUS // task_cpus, JOB_MEM_MB // task_mem_mb)` at once.

Shipping, once a content: `base.bundle` is a bundle of `main` (`cas/base-MAIN.bundle`, named by main's commit:
35 MB, shared by every run made while main stays), `code.bundle` only what the run's commit adds to main
(`cas/code-KEY.bundle`, a few KB; absent when main holds the commit), `with.tgz` the shipped files
(`cas/with-KEY.tgz`); each is a hard link to its `cas/` copy. The code's KEY is its commit and `main`'s: the working
tree's snapshot commit has a fixed author, committer and date (HEAD's), so the same tree on the same HEAD is the same
commit. The files' KEY hashes their names, modes and bytes, not their times. One login finds which are there
(touching them); a missing one is made here and uploaded under a temporary name, renamed in, one upload of a blob at
a time on this machine (`ship/NAME` lock: the next one finds it there). A run of unchanged content uploads nothing.
A piece checks out from `base.bundle` then `code.bundle`, as `job.sh` does.
It touches `claims/N/beat` every 30 s; a claim not touched for 300 s (the server's clock: a fresh file's mtime) is a
dead piece's, and any piece takes it over by renaming it into `lost/`. A task has 3 starts; then it ends `-1`. A
task's result is `done/N.tar` (`result.json`: code, peak RSS, host, seconds, began, ended, slots; `log`, its last
1 MB; `out/`, at most 1 GB), written under a temporary name and renamed in. SIGTERM (cancel, eviction) or SIGUSR1
(Slurm's `--time`) stops a piece: its tasks are killed and their claims moved into `lost/`, and it exits 0 (after
SIGUSR1, `job.sh` requeues it).

Placement (`runs.place_pieces`): one placement of this machine's at a time (`place.lock`), so each sees the queues
the last one's submits changed. Each backend allowed is read (shared reads, `CACHE_S`); offers are sorted by tasks at
once, then CPUs, then cpucluster1, Condor, the GPU cluster. A Slurm or cpucluster1 piece asks only the CPUs the tasks
left can use, and our pending jobs count against our caps; a lab machine is taken whole. Condor's offers leave out
as many of the best free machines as we have Condor jobs idle in its queue (they take free machines first). Condor
pieces are anonymous (requirements and a rank by the workers a machine holds), so a big machine can take every task
and a second piece end at once; a piece says its kind (`+BC26Kind`: task CPUs, memory, runtime and `POOL_V` for a
pooled run, the run's id otherwise). GPU-cluster slots go one at a time, each from a read after the last submit. When
nothing is free, Condor pieces wait in its queue: `QUEUED_PIECES` (4) of a kind at most, counted over every run's
idle pieces; a run whose kind has them gets none (its tasks wait for the pool) and isn't refused. However many lab
machines look free, a placement adds Condor pieces only while fewer than `IDLE_CAP` (32) of its kind wait in the
queue: a read of free machines is a moment's, and a piece waits until Condor matches it. A placement that
can't take the lock in `PLACE_WAIT_S`, or whose pool read failed, adds nothing: the follower places later. A piece
counts once its backend took it (appended to `pieces`). Condor requirements leave out machines that don't mount the
workspace (`CONDOR_EXCLUDE`) and machines with under 4 GiB free to execute in (`CONDOR_DISK_KB`): a piece there
takes no task and ends, so the machine stays free and draws piece after piece. A submit host that gives no answer to
the ticket check (`remote.submit_host`: ssh's 255) is noted (`CLIENT/submit-down`): no Condor job was submitted,
no password is sent, and for `SUBMIT_DOWN_S` (10 min) placements leave Condor out (`remote.submit_down`), so the
other backends get the pieces; a pooled run that gets none waits for the pool.

The follower (`runs.follow`, behind `tools/run` and `tools/compute wait RUN`) polls every 30 s: fetches new
`done/` tars over one tar stream (each unpacked aside and renamed into `N/`, so a reader sees a task's folder whole),
reads the pieces' states, and when every task is done writes `closed`, then `summary.json`, then cancels the pieces
that never started, all at once (`jobs.cancel_many`: one login a cluster, one `condor_rm` for every Condor cluster,
one `scancel` for every Slurm job; a cluster `condor_rm` can't find has left the queue and counts as done). When no
piece is left and tasks remain: a held piece stops the follower with exit 3, the
run left open (a hold is reported, never routed around); a piece that ended with a code other than 0 or 75 stops the
run (its log shown: it would fail again); otherwise a replacement round is submitted, once unclaimed tasks exist or
the dead claims are stale, at most 3 rounds (`mkdir replace.K` lets one follower submit each). A pooled run follows
the pool's rule below instead. A run no piece could be submitted for is closed at once. Once every result is
here, the follower deletes the run's `done/`, `claims/`, `lost/` and `tasks.jsonl` (`drop_results`): results live on
clankerland.

## Local results

`runs.compact_local` (from `sweep_daily`, so at most hourly when `tools/run` or `tools/compute run` starts) packs
each run under the checkout's `runs/` that has not changed for a day (`COMPACT_S`): its per-task folders into
`tasks.tgz`, their `result.json` into `tasks.jsonl`, then removes them: a run's thousands of tiny files take ten
times their size on disk.

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
  for `FULL_S` (60 s); its claims are read for dead pieces' at most every `STALE_CHECK_S` (120 s). Claims, beats,
  takeover, `MAX_STARTS` and results are a run's own, as above, whoever runs its tasks; a result run by another run's
  piece names that run (`piece_run`).
- **Fairness.** Each free slot goes to the run with the fewest running tasks (claims not done), the older run first
  among equals. A run with nothing running is beaten only by older runs with nothing running, which are finitely many
  and finite, so no run waits forever; a newcomer beats every run that has a task running, so a small batch behind a
  big one starts at the next free slots.
- **Another run's checkout.** A piece makes a checkout only for a task it has claimed (the room check, the claim,
  then the checkout), so a failed claim or the room check's prune never costs a checkout. A task runs in its run's
  checkout: the piece's own (job.sh's) for its run and runs of the same content and kit; else one the piece makes in
  its scratch from that run's `code.bundle` and `with.tgz`, job.sh's way (2 s on a lab machine), kept for later runs
  of that content (`CHECKOUTS` unused ones at most, besides those tasks run in, which are never pruned; one is
  ~350 MB). A kit run's `kit` file names its jars; the task gets that run's `BC_CLASSPATH` and `BC_ENGINE_JARS`.
- **Room to write.** A piece claims a task only while its scratch's filesystem has `DISK_MIN_MB` (2 GB) free, after
  pruning its unused checkouts when short (a lab machine's execute folder is on its root disk). A task that ends `GIVE_BACK` (75: `tools/play --give-back`, a game that found no room to write) has its claim
  given back at once, unrun, for another piece, and its piece takes no more tasks. The tasks of a piece share
  `JOB_CACHE` (its scratch's `cache/`): a native binary is unpacked there once a piece, not 100 MB a task.
- **The exit rule.** A piece ends as soon as it runs nothing and a last look at every run (skips ignored) finds
  nothing it may claim: no idle grace. A run's `closed` stops that run's tasks on every piece (checked every
  `CLOSED_S`, 5 s), and the piece goes on with other runs' tasks.
- **Live capacity.** A pooled piece's state is the name of an empty file in `pool/pieces`
  (`JOB,POOL_V,slots,running,task CPUs,task MB,runtime,deadline,limit,at`: `piece.state_name`), renamed (and
  touched) when it starts, when its free slots reach or leave 0, on any other change at most every `STATE_S` (30 s),
  and unchanged every `STATE_BEAT_S` (10 min); removed at its end. One listing reads every state: a state in a file's
  content cost 16 ms a file on CephFS (236 pieces: 3.9 s). A file named by a job id alone is an older piece's, its
  state inside, read while such pieces live. A piece is live while its scheduler lists it running (the view reads both
  queues); where that queue wasn't read (cpucluster1), while its `at` is under 21 minutes old. The view removes a dead
  piece's file at its next refresh.
- **Placement.** A new pooled run asks for pieces for its tasks less the free slots of the live pieces that would
  serve it (`runs.place`); `tools/run` says so (`2 live pieces have 9 free slots: no new piece`).
- **Placement beside the follower.** `tools/run` prints the run's id before it places pieces and touches `placed`
  after; a follower started at the id (`tools/games` follows each batch at once) adds no piece before `placed`, or
  `PLACE_S` (10 min) after the run's creation.
- **Following.** The follower adds pieces (`replace.K`, `POOL_ROUNDS` 8 at most) when tasks wait (unclaimed, or
  claimed by dead pieces while none of its own pieces runs) beyond the live pieces' free slots, never while one of its
  pieces is queued or held, or just started, and at most every `GRACE_S` (20 s); a round that added nothing (the
  backends full, its kind's pieces queued) is given back and doesn't count. Its own pieces' end stops a pooled run
  only once none of its tasks moved for 90 s. The summary adds `pieces_requested` and `served_by_others`.
- **Cancel.** `tools/compute cancel RUN` closes a pooled run and cancels its pieces that are queued or held; its
  running pieces stop its tasks and go on with other runs' tasks, or end at once.
- **Protocol.** A piece serves only runs of its own `POOL_V` (4), and a follower counts only pieces of it: a change to
  the protocol bumps it. A run with no `pool` in `run.json` (`--whole`, `--max-cpus`) is served by its own pieces
  alone.

## The games interface

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
  launch on a lab machine and one game a minute or so per CPU, that is noise; DoC's process accounting logs
  each launch, so never fork per line or per round.
- The same command plays the same game here: `tools/kit.py`'s `classpath()` is `BC_CLASSPATH` on a node and
  `build/classes` plus the Gradle cache's jars here.

## Backends

- **Condor** (lab machines, `--where condor`): submit host `ash04.doc.ic.ac.uk`, the only lab machine we ssh to, one
  login a call, no kept connection. Reads go through a GPU-cluster login node. Submits and removals need a Kerberos
  ticket there: `kerberos_login` runs kinit with Bitwarden's password (the item `BC26_BW_ITEM` in `.env` names), never twice with a
  password the server refused (its sha256 in `~/battlecode26-data/kinit-refused`; the previous project's record is
  read too), never an empty one. ash04 times out during ssh's banner exchange at times: a login to it that failed
  before connecting is tried again (3 tries, 10 s apart); one that may have run its command never is. A
  condor_submit whose answer was lost is reconciled with the queue (read through a login node): a job Condor holds
  gets its backend-id, any other folder is removed. `request_cpus = 1` takes the machine's one static slot, so every
  core; requirements are our releases (Ubuntu 18.04 to 26.04), the task's CPUs and memory, 4 GiB free to execute in,
  and not a machine of `CONDOR_EXCLUDE`; rank by the tasks the machine holds, the machine of the last start last. No file transfer: everything is on the shared volume. `--time` becomes
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
    `job_mgr.c`, `_copy_job_desc_to_file`, which `--test-only` skips, so the probe passes and the submit fails). Its
    `/var` has filled for hours at a time. So cpucluster1's read
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
- **Bounded writes in our workspace on /vol/gpudata** (`BC26_WORKSPACE` in `.env`, from `ws_allocate bc26 365` on
  gpucluster2: CephFS, mounted on the GPU cluster's nodes, cpucluster1 and the lab machines but melchett; a 55 GB
  quota, read without a walk by `getfattr -n ceph.dir.rbytes`). Nothing on /vol/bitbucket (full, and a small file
  operation there takes 250 ms and more). A job's log stops at 64 MB, a task's log at 1 MB and its OUT at 1 GB; ended
  jobs and closed runs are deleted after 6 hours (never a job still in one of our queues: the sweep reads them
  first, and keeps every job folder when one can't be read), a run nothing touched for 2 days too, and a closed run's code
  bundle at once (`tools/compute sweep`, run by `tools/run` and `tools/compute run` at most once an hour). A stored
  bundle or shipped file in `cas/` that no run or job links any more goes 6 hours after its last use; `pool/pieces`
  files silent for an hour go. Never `chmod` anything open.
- **No persistent data there.** Every entry is an open run's state or a cache (`tools/workspace.py`'s `ENTRIES`,
  written to `MANIFEST` by every sweep, which names any entry it doesn't list). `tools/compute empty --yes` deletes
  the whole workspace once no job of ours
  (queued, running or held) has its folder there and no open pooled run there moved in the last hour (`busy`), in
  bounded steps (`STEP_S`, 400 entries of each folder a step).
- **Quotas are facts.** The NFS home has a quota; nothing of ours writes to it (HOME is scratch in a job).
- **First jobs small**: a first job on a backend, or of a new kind, runs under 10 minutes.
