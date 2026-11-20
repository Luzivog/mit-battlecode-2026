"""Runs: many tasks spread over every free remote CPU (`tools/run`). No daemon: the run's folder on the shared volume is
the record, pieces (jobs.py jobs running tools/piece.py) claim its tasks, and whoever follows it (`tools/run` itself,
or `tools/compute wait RUN`) fetches results, adds pieces while its tasks wait, and closes it.

A run is REMOTE_RUNS/ID (ID: run-MMDD-HHMMSS-XXXX): `run.json` (its settings), `tasks.jsonl` (a task a line:
{"argv": [...], "lines": [...]}), `commit`, `base.bundle` (main), `code.bundle` (what the commit adds to main; absent
when main holds it), `with.tgz` (hard links to REMOTE_CAS's copies, which are named by their content and shipped once
a content; hard-linked into each piece's job folder), `kit` (a kit run's
engine jars), `piece.py` (the submitter's, so a piece always speaks its follower's protocol), `pieces` (each piece's
job id, a line), `claims/`, `lost/`, `done/` (tools/piece.py), `placed` (tools/run's own placement has ended: until
then, or PLACE_S, a follower adds no piece), `replace.K` (round K of added pieces was submitted),
`closed` (every task is done, or the run was cancelled: its tasks stop). Results come back to the worktree's
runs/ID/N/ (result.json, log, out/); once its follower has them all, done/, claims/, lost/ and tasks.jsonl go.

The pool. A run of tasks of a set size (not --whole) with no --max-cpus is pooled: REMOTE_POOL/open/ID lists it while
it is open, every live pooled piece of the same task size and runtime may take its tasks (the fewest-running run
first: tools/piece.py), and its own pieces serve every other pooled run once its own tasks are all claimed. A new
pooled run asks for pieces only for the tasks the live pieces' free slots (REMOTE_POOL/pieces/) can't take; its
follower adds pieces while tasks wait unclaimed and none of its pieces is still queued.
"""
from __future__ import annotations

import hashlib
import json
import os
import re
import secrets
import shlex
import shutil
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from dataclasses import asdict, dataclass
from pathlib import Path

from tools import backends as B
from tools import jobs as J
from tools import kit, provision, remote, view
from tools import workspace as W
from tools.piece import MIN_LEFT_S, POOL_V, STALE_S
from tools.remote import (CPU_HOST, LOCAL, REMOTE_CAS, REMOTE_POOL, REMOTE_RUNS, SLURM_HOSTS, Refused, Unreachable,
                          login, login_into)

RUN_ID = rf"run{J.ID_TAIL}"
DEFAULT_TASK_MEM_MB = 1700  # a game's JVM peaks at 0.5-1.6 GB (docs/testing.md §7), plus headroom
DEFAULT_TIME_S = 4 * 3600  # a Slurm or cpucluster1 piece's --time: at it, the piece gives its tasks back and requeues
MAX_ROUNDS = 3  # rounds of replacement pieces a run may get, once all its pieces were lost
QUEUED_PIECES = 4  # Condor pieces queued for a run when no backend has a free machine for it now
# Condor pieces of a kind waiting in its queue at most, over every run, however many lab machines look free: each read
# of the free machines is a moment's, and ten runs opened in one minute queued 96, 101 and 220 pieces for machines that
# looked free; 181 of them waited at once (20 Nov)
IDLE_CAP = 32
ORDER = ("cpucluster1", "condor", "slurm", "local")  # of equal offers, the CPU-only cluster first, the GPU cluster last
GRACE_S = 20.0 if not LOCAL else 0.5  # after a placement (or the run's start), how long before pieces are added
POOL_ROUNDS = 8  # placements a pooled run's follower may make
STUCK_S = 90.0 if not LOCAL else 1.0  # a pooled run none of whose tasks moved for this long has stopped moving
QUEUED_S = 120.0  # a piece queued this long no longer holds back pieces added for its run's waiting tasks
PLACE_S = 600.0  # a follower adds pieces before tools/run's placement says it ended (`placed`) only this long after
PLACE_WAIT_S = 300.0  # how long a placement waits for another one of this machine's to end (place_pieces)
READY_S = 3600.0  # the root's mode and the runtime on the volume, checked at most this often by this machine's runs


@dataclass(frozen=True)
class Settings:
    id: str
    tasks: int
    task_cpus: int  # 0: each task takes a whole machine, alone
    task_mem_mb: int
    wheres: tuple[str, ...]
    time_s: int
    condor_time_s: int | None  # Condor pieces removed after this long (only when --time was given)
    max_cpus: int | None
    kit: bool
    cwd: str
    commit: str
    gather: tuple[str, ...]
    pool: bool = False  # pooled (the module's docstring); runs made before the pool aren't
    pool_v: int = 0  # the pool's protocol (tools/piece.py's POOL_V)
    env: str = ""  # the runtime's hash (provision.env_file()): a piece serves runs of its own runtime only
    created: float = 0.0  # epoch s: the older of two runs as short of tasks is served first
    code_key: str = ""  # the code's content (jobs.Tree.key) and the shipped files' (jobs.content_key)
    with_key: str = ""


def env_hash() -> str:
    return hashlib.sha256(provision.env_file()).hexdigest()[:16]


def new_run_id() -> str:
    return f"run-{time.strftime('%m%d-%H%M%S')}-{secrets.token_hex(3)}"


def run_dir(rid: str) -> str:
    if not re.fullmatch(RUN_ID, rid):
        raise Refused(f"{rid!r} isn't a run id (they look like run-1118-153000-3fa2)")
    return f"{REMOTE_RUNS}/{rid}"


def is_run(ident: str) -> bool:
    return ident.startswith("run-")


# ---- what each backend offers now ----
@dataclass(frozen=True)
class Offer:
    where: str
    slots: int  # tasks it runs at once
    cpus: int
    mem_mb: int
    slot: B.CpuSlot | None = None  # a GPU-cluster slot
    partition: str = ""  # a cpucluster1 partition


def slots_of(cpus: int, mem_mb: int, task_cpus: int, task_mem_mb: int) -> int:
    return 1 if task_cpus == 0 else min(cpus // task_cpus, mem_mb // task_mem_mb)


def offers(wheres: tuple[str, ...], task_cpus: int, task_mem_mb: int, time_s: int, notes: list[str],
           need: int | None = None, named: bool = False, max_age: float = 0.0) -> list[Offer]:
    """What each backend of WHERES could start for this run now, read at once, best first (most slots, then ORDER).
    NEED: the tasks to run at once, the most a Slurm or cpucluster1 offer asks for (a lab machine is taken whole). A
    backend that can't be read is left out, its reason in NOTES. cpucluster1 counts only while it can take jobs (its
    read's takes_jobs), and is left out without a word otherwise, unless NAMED: asked for alone by name, it is offered
    as read, and sbatch says why it refuses. Our Condor jobs waiting in its queue will take free machines first: as
    many of the best free machines are left out. MAX_AGE: scheduler reads of this machine's tools that recent do."""
    tc, mb_cpu = max(task_cpus, 1), max(1, -(-task_mem_mb // max(task_cpus, 1)))

    def condor() -> list[Offer]:
        c = B.read_condor(max_age)
        idle = sum(1 for j in c.jobs if j.state == "idle")
        free = sorted(((slots_of(m.cpus, m.memory_mb, task_cpus, task_mem_mb), m) for m in c.free(tc, task_mem_mb)),
                      key=lambda x: (-x[0], -x[1].cpus, x[1].name))[idle:]
        return [Offer("condor", n, m.cpus, m.memory_mb) for n, m in free if n >= 1]

    def cpucluster() -> list[Offer]:
        try:
            c = B.read_cpucluster(max_age)
        except Refused:
            if named:
                raise
            return []
        if not (c.takes_jobs or named):
            return []
        out: list[Offer] = []
        # our pending jobs will take CPUs of our cap when they start: the cap counts only running ones, so without
        # them each new run sees the same free CPUs and queues one more job behind the last
        pending = sum(j.cpus for j in c.jobs if j.state == "PENDING")
        cap = c.cap.cap - c.cap.used - pending if c.cap.cap is not None else None
        mem = c.mem.cap - c.mem.used - pending * mb_cpu if c.mem.cap is not None else None
        left = need
        for name, n in sorted(c.nodes.items(), key=lambda x: -x[1].idle):
            if not n.usable or (left is not None and left <= 0):
                continue
            part = next((p for p in n.partitions if p in c.partitions and (
                c.partitions[p].max_time_s is None or time_s <= (c.partitions[p].max_time_s or 0))), None)
            if part is None:
                continue
            free = B.cpu_free(c, n, mb_cpu)
            free = min(free, *(() if cap is None else (cap,)), *(() if mem is None else (mem // mb_cpu,)))
            if left is not None and task_cpus:
                free = min(free, left * tc)
            free -= free % tc
            if free < tc:
                continue
            out.append(Offer("cpucluster1", slots_of(free, free * mb_cpu, task_cpus, task_mem_mb), free,
                             free * mb_cpu, partition=part))
            left = None if left is None else left - out[-1].slots
            cap = None if cap is None else cap - free
            mem = None if mem is None else mem - free * mb_cpu
        return out

    def slurm() -> list[Offer]:
        units = None if need is None or not task_cpus else need * tc
        slots = B.slurm_starts_now(B.read_slurm(max_age), tc, units, time_s, max(B.CPU_JOB_MIN_MEM_MB, mb_cpu))
        seen: set[str] = set()
        out: list[Offer] = []
        for x in slots:  # one a node: slots on one node's several partitions are the same CPUs
            if x.node not in seen:
                seen.add(x.node)
                out.append(Offer("slurm", slots_of(x.cpus, x.mem_mb, task_cpus, task_mem_mb), x.cpus, x.mem_mb, x))
        return out[:1]  # each pick changes what the caps leave: one now, the next from a fresh read (place_pieces)

    def local() -> list[Offer]:  # tests: three machines of 2 CPUs
        return [Offer("local", slots_of(2, 4096, task_cpus, task_mem_mb), 2, 4096)] * 3

    readers = {"condor": condor, "cpucluster1": cpucluster, "slurm": slurm, "local": local}
    out: list[Offer] = []
    with ThreadPoolExecutor(len(wheres)) as pool:
        futures = {w: pool.submit(readers[w]) for w in wheres}
        for w, f in futures.items():
            try:
                out += f.result()
            except Refused as e:
                notes.append(f"{w}: unreadable: {e}")
    return sorted([o for o in out if o.slots >= 1], key=lambda o: (-o.slots, -o.cpus, ORDER.index(o.where)))


def choose(found: list[Offer], need: int, max_cpus: int | None) -> list[Offer]:
    """The offers to take: best first until their slots hold NEED tasks at once, or MAX_CPUS is reached."""
    took: list[Offer] = []
    slots = cpus = 0
    for o in found:
        if slots >= need or (max_cpus is not None and cpus + o.cpus > max_cpus):
            break
        took.append(o)
        slots += o.slots
        cpus += o.cpus
    return took


# ---- creating a run ----
@dataclass(frozen=True)
class Task:
    argv: list[str]
    lines: list[str]


def tasks_of(cmd: list[str], lines: list[str] | None, chunk: int) -> list[Task]:
    """One task (CMD) when LINES is None; else CMD once a CHUNK of LINES. {} is the line (CHUNK 1 only), {file} a file
    of the task's lines, {out} its OUT, {cpus} its CPUs; with none of {} and {file}, the line's words are appended."""
    if lines is None:
        return [Task(cmd, [])]
    if chunk < 1:
        raise Refused("--chunk: at least 1")
    uses = any("{}" in a for a in cmd)
    if uses and chunk > 1:
        raise Refused("{} is one line: with --chunk, name the task's lines as {file}")
    tasks: list[Task] = []
    for i in range(0, len(lines), chunk):
        part = lines[i:i + chunk]
        if uses or any("{file}" in a for a in cmd):
            tasks.append(Task(list(cmd), part))
        else:
            tasks.append(Task([*cmd, *shlex.split(part[0])] if chunk == 1 else [*cmd, "{file}"], part))
    return tasks


def create(tasks: list[Task], task_cpus: int, task_mem_mb: int, wheres: tuple[str, ...], time_s: int | None,
           max_cpus: int | None, with_kit: bool, ships: list[str], gather: tuple[str, ...], build: bool = True,
           repo: Path = J.REPO, cwd: Path | None = None) -> Settings:
    """The run's folder made and filled (one login after the uploads); its settings. Nothing is submitted yet."""
    if not tasks:
        raise Refused("no tasks: the file has no lines")
    here = (cwd or Path.cwd()).resolve()
    if with_kit and build:
        kit.compile_classes(repo)
    tree = J.tree_commit(repo)
    paths = J.ship(ships, repo, here) + ([kit.CLASSES] if with_kit and kit.CLASSES not in ships else [])
    wkey = J.content_key(repo, paths) if paths else ""
    rid = new_run_id()
    pooled = task_cpus > 0 and max_cpus is None
    s = Settings(rid, len(tasks), task_cpus, task_mem_mb, wheres, time_s or DEFAULT_TIME_S, time_s, max_cpus, with_kit,
                 str(here.relative_to(repo)) if here.is_relative_to(repo) else ".", tree.commit, gather, pooled,
                 POOL_V if pooled else 0, env_hash(), time.time(), tree.key, wkey)
    hosts = SLURM_HOSTS
    kit_key = hashlib.sha256(provision.kit_file(repo)).hexdigest()[:12] if with_kit else "none"
    ready = f"ready-{env_hash()}-{kit_key}-{'cpu' if 'cpucluster1' in wheres else 'gpu'}"
    if not LOCAL and not remote.fresh(ready, READY_S):  # the root's mode and the runtime: checked once an hour here
        J.private_root(hosts)
        if "cpucluster1" in wheres:
            J.private_root((CPU_HOST,))
        provision.ensure(hosts, repo, with_kit)
        remote.mark(ready)
    W.check_room()
    # main once (base, 35 MB, shared by every run while main stays), then only what the commit adds to it (KB)
    base_blob = f"base-{tree.main}.bundle" if tree.main else ""
    code_blob = "" if tree.in_main else f"code-{tree.key}.bundle"
    with_blob = f"with-{wkey}.tgz" if paths else ""
    size: dict[str, int] = {}

    def made(name: str, data: bytes) -> bytes:
        size[name] = len(data)
        return data
    makers = {base_blob: lambda: made(base_blob, tree.base()),
              code_blob: lambda: made(code_blob, tree.thin() if tree.main else tree.bundle()),
              with_blob: lambda: made(with_blob, provision.tar_paths(repo, paths))}
    sent = J.ship_cas({k: v for k, v in makers.items() if k}, hosts)
    files = {"run.json": json.dumps(asdict(s)).encode(), "commit": f"{tree.commit}\n".encode(),
             "piece.py": (Path(__file__).parent / "piece.py").read_bytes(),
             "tasks.jsonl": "".join(json.dumps({"argv": t.argv, "lines": t.lines}) + "\n" for t in tasks).encode(),
             "pieces": b""} | ({"kit": provision.kit_file(repo)} if with_kit else {})
    d = run_dir(rid)
    links = "".join(f" && ln {REMOTE_CAS}/{blob} {d}/{name}" for blob, name in
                    ((base_blob, "base.bundle"), (code_blob, "code.bundle"), (with_blob, "with.tgz")) if blob)
    opened = f" && mkdir -p {REMOTE_POOL}/open && : > {REMOTE_POOL}/open/{rid}" if pooled else ""
    login(f"mkdir -p {REMOTE_RUNS} && mkdir -m 700 {d} && tar -xf - -C {d} && mkdir {d}/claims {d}/done {d}/lost"
          f"{links}{opened}", J.folder_tar(files), hosts)
    up = [b for b in (base_blob, code_blob) if b in sent]
    shipped = [f"its code ({sum(size[b] for b in up) / 1e6:.1f} MB)" if up else "its code (on the volume)"]
    if paths:
        shipped.append(f"{', '.join(paths)} ({size[with_blob] / 1e6:.1f} MB)" if with_blob in sent
                       else f"{', '.join(paths)} (on the volume)")
    J.say(f"run {rid}: {len(tasks)} task{'s' * (len(tasks) != 1)} of commit {tree.commit[:12]} ({tree.subject}), "
          f"shipping {' and '.join(shipped)}" + (", pooled" if pooled else ""))
    return s


def read_settings(rid: str) -> Settings:
    raw = json.loads(login(f"cat {run_dir(rid)}/run.json"))
    raw["wheres"], raw["gather"] = tuple(raw["wheres"]), tuple(raw["gather"])
    return Settings(**raw)


# ---- pieces ----
def piece_files(s: Settings, inputs: list[str]) -> tuple[dict[str, bytes], dict[str, str]]:
    """A piece's job files, and the run's INPUTS (its bundles and with.tgz there) hard-linked into its folder
    (name -> path)."""
    d = run_dir(s.id)
    files = {"job.sh": (Path(__file__).parent / "job.sh").read_bytes(), "commit": f"{s.commit}\n".encode(),
             "argv": b"".join(a.encode() + b"\0" for a in ["python3", f"{d}/piece.py", d]),
             "env": provision.env_file(), "exit": b"", "log": b"", "run": f"{s.id}\n".encode()}
    if s.kit:
        files["kit"] = provision.kit_file(J.REPO)
    return files, {name: f"{d}/{name}" for name in inputs}


def submit_pieces(s: Settings, took: list[Offer], queued_condor: int = 0) -> list[tuple[str, int]]:
    """A piece for each offer of TOOK (QUEUED_CONDOR more Condor pieces to wait for machines); (job id, its tasks at
    once) of each one submitted, appended to the run's `pieces` once its backend took it. One whose submit failed is
    named, and left out."""
    d = run_dir(s.id)
    inputs = login(f"cd {d} && for f in {' '.join(J.INPUTS)}; do [ -e $f ] && echo $f; done; true").split()
    files, shared = piece_files(s, inputs)
    got: list[tuple[str, int]] = []
    condor = [o for o in took if o.where == "condor"]
    n_condor = len(condor) + queued_condor
    if n_condor:
        mem = min([o.mem_mb for o in condor] or [s.task_mem_mb])
        js: list[J.Job] = []
        while len(js) < n_condor:
            j = J.job(J.new_id("condor"))
            if j.id not in {x.id for x in js}:
                js.append(j)
        subs = [(j, files, B.condor_sub(j.dir, j.id, max(s.task_cpus, 1), min(mem, s.task_mem_mb),
                                        s.task_mem_mb if s.task_cpus else None, s.condor_time_s, kind_of(s)))
                for j in js]
        clusters = J.submit_condor(subs, shared)
        slots = [o.slots for o in condor] + [1] * queued_condor
        got += [(j.id, n) for j, c, n in zip(js, clusters, slots, strict=True) if c is not None]
        J.say(f"run {s.id}: {sum(c is not None for c in clusters)} Condor piece(s), a lab machine each"
              + (f" ({queued_condor} to wait for a machine)" if queued_condor else ""))
    for o in took:
        if o.where == "condor":
            continue
        j = J.job(J.new_id(o.where))
        pre = "".join(f" && ln {src} {j.dir}/{name}" for name, src in shared.items())
        try:
            if o.where == "slurm":
                assert o.slot is not None
                J.sbatch(j, files, B.cpu_slot_args(o.slot, s.time_s), SLURM_HOSTS, pre=pre)
            elif o.where == "cpucluster1":
                # --ntasks=1: cpucluster1's job_submit.lua refuses a job that leaves its task count unset
                J.sbatch(j, files, ["-p", o.partition, "--ntasks=1", f"--cpus-per-task={o.cpus}",
                                    f"--mem-per-cpu={o.mem_mb // o.cpus}M", *B.checkpointed(s.time_s)],
                         (CPU_HOST,), pre=pre)
            else:
                J.submit_local(j, files, shared)
        except Refused as e:
            J.say(f"run {s.id}: piece {j.id} not submitted: {e}")
            continue
        got.append((j.id, o.slots))
        J.say(f"run {s.id}: piece {j.id}: {o.cpus} CPUs, {o.mem_mb} MB, {o.slots} tasks at once"
              + (f" ({o.slot.node}, {o.slot.partition})" if o.slot else f" ({o.partition})" if o.partition else ""))
    if got:
        remote.forget()  # the queues changed: the next placement reads them again
        login(f"printf '%s\\n' {' '.join(i for i, _ in got)} >> {d}/pieces")
    return got


def kind_of(s: Settings) -> str:
    """The runs a piece of S serves, as a Condor attribute (BC26Kind): a pooled run's task size, runtime and protocol;
    an unpooled run's own id."""
    return f"{s.task_cpus}-{s.task_mem_mb}-{s.env}-v{POOL_V}" if s.pool else s.id


def place_pieces(s: Settings, need: int) -> list[str]:
    """Pieces for NEED tasks at once on what the backends offer now, best first, one placement of this machine's at a
    time (a lock: each reads the queues the last one's submits changed). The GPU cluster's slots go one at a time,
    each from a read made after the last one's submit (each changes what our caps leave). When nothing is free, Condor
    pieces wait in its queue, QUEUED_PIECES of S's kind at most over every run: our pieces queued and running are
    bounded by what the backends can start. Refused when no piece could be submitted and none of S's kind waits."""
    try:
        with remote.locked(remote.CLIENT / "place.lock", PLACE_WAIT_S):
            return _place(s, need)
    except Unreachable as e:
        if not s.pool:
            raise
        J.say(f"run {s.id}: no placement now ({e}): its follower places pieces later")
        return []


def _place(s: Settings, need: int) -> list[str]:
    notes: list[str] = []
    wheres = s.wheres
    down = remote.submit_down() if "condor" in wheres else None
    if down is not None:  # Condor left out: the other backends still get pieces
        wheres = tuple(w for w in wheres if w != "condor")
        J.say(f"run {s.id}: Condor's submit host gave no answer {down:.0f} s ago: no Condor piece now")
    found = offers(wheres, s.task_cpus, s.task_mem_mb, s.time_s, notes, need, named=s.wheres == ("cpucluster1",),
                   max_age=remote.CACHE_S)
    took = choose(found, need, s.max_cpus)
    for n in notes:
        J.say(f"run {s.id}: {n}")
    waiting = queued_of_kind(kind_of(s)) if "condor" in wheres else 0
    condor = [o for o in took if o.where == "condor"]
    room = max(0, IDLE_CAP - waiting)  # a Condor piece waits in the queue until matched, even for a free machine
    if len(condor) > room:
        J.say(f"run {s.id}: {len(condor)} lab machines look free, {waiting} pieces of its kind wait in Condor's queue: "
              f"{room} more at most ({IDLE_CAP} waiting of a kind)")
        drop = condor[room:]
        took = [o for o in took if not any(o is x for x in drop)]
    got = submit_pieces(s, [o for o in took if o.where != "slurm"]) if any(o.where != "slurm" for o in took) else []
    left, cpus = need - sum(n for _, n in got), sum(o.cpus for o in took if o.where != "slurm")
    slurm = [o for o in took if o.where == "slurm"]
    for _ in range(8 if slurm else 0):  # at most 8 GPU-cluster pieces (multigpu allows 8 jobs)
        if left <= 0 or not slurm:
            break
        o = slurm.pop()
        if s.max_cpus is not None and cpus + o.cpus > s.max_cpus:
            break
        one = submit_pieces(s, [o])
        got += one
        left -= sum(n for _, n in one)
        cpus += o.cpus
        if one and left > 0:
            slurm = offers(("slurm",), s.task_cpus, s.task_mem_mb, s.time_s, [], left)[:1]
    if got:
        return [i for i, _ in got]
    if "condor" not in wheres:
        if down is not None and s.pool:
            J.say(f"run {s.id}: no piece added now: its tasks wait for the pool's free slots")
            return []
        raise Refused(f"no piece could start on {', '.join(s.wheres)} now (`tools/compute status`)")
    queued = min(QUEUED_PIECES - waiting, need)
    if queued <= 0:
        J.say(f"run {s.id}: no piece added now, and {waiting} pieces of its kind wait in Condor's queue: no more")
        return []
    J.say(f"run {s.id}: no backend has room now: {queued} Condor piece(s) wait in its queue"
          + (f" ({waiting} of its kind already do)" if waiting else ""))
    got = submit_pieces(s, [], queued)
    if not got and not waiting:
        raise Refused("no piece could be submitted (the reasons are above)")
    return [i for i, _ in got]


def queued_of_kind(kind: str) -> int:
    """Our Condor jobs of KIND waiting in its queue (every run's), from a read at most CACHE_S old."""
    return sum(1 for j in B.read_condor(remote.CACHE_S).jobs if j.state == "idle" and j.kind == kind)


# ---- the pool ----
def read_pool() -> tuple[list[dict[str, object]], float]:
    """Every piece's state, each with its `live` (view.Snapshot.pool); the snapshot's time (the shared view)."""
    return view.get().pool()


def serving(s: Settings, pieces: list[dict[str, object]], clock: float) -> list[dict[str, object]]:
    """The live pieces of PIECES that would take S's tasks (tools/piece.py's `serves` and `fits`)."""
    out = []
    for p in pieces:
        if not p.get("live") or p.get("v") != POOL_V or not isinstance(p.get("slots"), int) \
                or not isinstance(p.get("running"), int):
            continue
        if (p.get("task_cpus"), p.get("task_mem_mb"), p.get("env")) != (s.task_cpus, s.task_mem_mb, s.env):
            continue
        if p.get("backend") not in s.wheres:
            continue
        if p.get("deadline") is not None and float(str(p["deadline"])) - clock < min(
                MIN_LEFT_S, float(str(p.get("limit") or MIN_LEFT_S * 2)) / 2):
            continue
        out.append(p)
    return out


def pool_free(s: Settings) -> tuple[int, int]:
    """The free slots of the live pieces that would take S's tasks now, and those pieces' count."""
    pieces, clock = read_pool()
    live = serving(s, pieces, clock)
    return sum(max(0, int(str(p["slots"])) - int(str(p["running"]))) for p in live), len(live)


def place(s: Settings, need: int) -> list[str]:
    """Pieces for NEED tasks at once (place_pieces); for a pooled run, only for those the live pieces' free slots
    can't take (they take its tasks within seconds). Refused when a piece was needed and none could be submitted."""
    if not s.pool:
        return place_pieces(s, need)
    try:
        free, live = pool_free(s)
    except Refused as e:  # unread is not empty: a piece added for it could be one too many
        J.say(f"run {s.id}: the pool's state unread ({e}): its follower places pieces later")
        return []
    if free:
        J.say(f"run {s.id}: {live} live piece{'s' * (live != 1)} {'have' if live != 1 else 'has'} {free} free slot"
              f"{'s' * (free != 1)}: " + ("no new piece" if free >= need else f"pieces for the other {need - free}"))
    if free >= need:
        return []
    try:
        return place_pieces(s, need - free)
    except Refused as e:
        if not free:
            raise
        J.say(f"run {s.id}: {e}")
        return []


def stale_claims(rid: str) -> int:
    """RID's claims no piece has touched for STALE_S (by the volume's clock): a dead piece's, any piece takes them."""
    d = run_dir(rid)
    out = login(f"cd {d} && c=$(mktemp .clock.XXXXXX) && stat -c %Y \"$c\" && rm -f \"$c\" && "
                "find claims -mindepth 1 -maxdepth 2 -printf '%P %T@\\n'").splitlines()
    clock, last = float(out[0]), dict[str, float]()
    for line in out[1:]:
        path, _, t = line.rpartition(" ")
        n = path.split("/")[0]
        if n.isdigit():
            last[n] = max(last.get(n, 0.0), float(t))
    return sum(1 for t in last.values() if clock - t > STALE_S)


# ---- following a run ----
@dataclass
class Progress:
    done: set[int]
    claimed: set[int]
    pieces: list[str]
    closed: bool
    rounds: int
    placed: bool = True


def read_progress(rid: str) -> Progress:
    """RID's progress, read now (a follower reads it through the shared view)."""
    run_dir(rid)
    got = view.Snapshot(0, [], [], B.sections(login(view.run_sh(rid) + "true")), None).run(rid)
    if got is None:
        raise Refused(f"run {rid}: no such run folder")
    return progress_of(got)


def progress_of(s: dict[str, list[str]]) -> Progress:
    def nums(key: str, suf: str = "") -> set[int]:
        stems = (x[:len(x) - len(suf)] for x in s.get(key, []) if x.endswith(suf))
        return {int(x) for x in stems if x.isdigit()}
    return Progress(nums("done", ".tar"), nums("claims"), [x for x in s.get("pieces", []) if x.strip()],
                    bool(s.get("closed")), len(s.get("rounds", [])), bool(s.get("placed")))


def mark_placed(rid: str) -> None:
    """tools/run's placement has ended: its follower (started beside it, as tools/games does) may add pieces now."""
    login(f"touch {run_dir(rid)}/placed")


def fetch(rid: str, names: list[int], out: Path) -> None:
    """Tasks NAMES' done tars, each unpacked aside and renamed into OUT/N/ whole: a reader that sees N/ sees all of it
    (tools/games reads a failed task's folder as its games' failure)."""
    import tarfile
    for i in range(0, len(names), 200):
        part = names[i:i + 200]
        tmp = out / ".done"
        login_into(f"tar -C {run_dir(rid)}/done -cf - {' '.join(f'{n}.tar' for n in part)}", tmp)
        for n in part:
            dest, aside = out / str(n), out / f".{n}.part"
            shutil.rmtree(aside, ignore_errors=True)
            aside.mkdir(parents=True)
            with tarfile.open(tmp / f"{n}.tar") as tar:
                tar.extractall(aside, filter="data")
            (tmp / f"{n}.tar").unlink()
            if dest.exists():  # fetched again (a follower started anew): the new one replaces it
                shutil.rmtree(dest)
            os.rename(aside, dest)


def result_of(out: Path, n: int) -> dict[str, object]:
    try:
        r: dict[str, object] = json.loads((out / str(n) / "result.json").read_text())
        return r
    except (OSError, ValueError):
        return {"task": n, "code": None}


def close(rid: str, pieces: list[str]) -> None:
    """The run closed (pieces stop claiming; it leaves the pool), its inputs deleted (a piece still running holds its
    own links), and its pieces that never started cancelled: no machine is held for nothing."""
    mark_closed(rid)
    cancel_unstarted(rid, pieces)


def drop_results(rid: str) -> None:
    """A closed run's results, claims and tasks deleted from the workspace once its follower has them here: results
    live on clankerland (runs/RUN/), and the workspace holds only open runs' state. A failure leaves them to the sweep."""
    d = run_dir(rid)
    try:
        login(f"rm -rf {d}/done {d}/claims {d}/lost {d}/tasks.jsonl")
    except Refused as e:
        J.say(f"run {rid}: its results stay on the volume for the sweep ({e})")


def mark_closed(rid: str) -> None:
    d = run_dir(rid)
    login(f"touch {d}/closed && rm -f {' '.join(f'{d}/{n}' for n in J.INPUTS)} {REMOTE_POOL}/open/{rid}")


def cancel_unstarted(rid: str, pieces: list[str]) -> None:
    """RID's pieces that never started cancelled, all at once (jobs.cancel_many): one condor_rm for every cluster,
    where one a piece took 4 logins (150 pieces: 33 min on 19 Nov)."""
    if not pieces:
        return
    states = J.read_states(pieces)
    unstarted = [pid for pid, st in states.items() if st.phase in ("queued", "held") and "started" not in st.flags]
    if not unstarted:
        return
    try:
        said = J.cancel_many(unstarted)
    except Refused as e:
        J.say(f"run {rid}: cancelling {len(unstarted)} pieces that never started: {e}")
        return
    odd = [x for x in said.values() if "condor_rm said" in x]
    for x in odd[:3]:
        J.say(f"run {rid}: {x}")
    J.say(f"run {rid}: {len(unstarted)} pieces that never started cancelled at once")


def follow(rid: str, poll_s: float = J.POLL_S, out_root: Path = J.LOCAL_RUNS) -> dict[str, object]:
    """RID followed until every task is done, or its pieces were lost MAX_ROUNDS times: results fetched as they come,
    pieces replaced when every one is gone and tasks are left. Its summary (also runs/ID/summary.json)."""
    s = read_settings(rid)
    out = out_root / rid
    out.mkdir(parents=True, exist_ok=True)
    (out / "run.json").write_text(json.dumps(asdict(s), indent=1) + "\n")
    have = {int(p.name) for p in out.iterdir() if p.name.isdigit() and (p / "result.json").exists()}
    shown, dead_since, w = "", None, J.Waiter()
    wanted: list[str] = []  # the pieces whose states the next read brings
    finals: dict[str, J.State] = {}
    placed_at, seen_running, progress, moved_at = s.created or time.time(), set[str](), (-1, -1), time.time()
    tick = time.time()

    def pause() -> None:  # a read every POLL_S from the last one's start: the reads' own time isn't added to it
        nonlocal tick
        time.sleep(max(0.0, poll_s - (time.time() - tick)))
        tick = time.time()
    while True:
        try:
            snap = view.get(runs=[rid], jobs=wanted)
            sec = snap.run(rid)
            if sec is None:
                raise Refused(f"run {rid}: its folder is gone from the volume")
            pr = progress_of(sec)
            new = sorted(pr.done - have)
            if new:
                fetch(rid, new, out)
                have |= set(new)
            live = [p for p in pr.pieces if p not in finals]
            wanted = live
            states = snap.states(live) if live else {}
        except Unreachable as e:  # a lost connection says nothing about the run: keep following
            J.say(f"run {rid}: {e}; trying again in {poll_s:.0f} s")
            pause()
            continue
        now = time.time()
        for pid, st in states.items():
            if (f := w.settle(pid, st, now)) is not None:
                finals[pid] = f
                if f.phase == "held":
                    J.say(f"run {rid}: piece {J.describe(pid, f)}")
        running = sum(1 for st in states.values() if st.phase == "running")
        line = (f"run {rid}: {len(have)} of {s.tasks} tasks done, {len(pr.claimed - have)} running; pieces: "
                f"{running} running, {sum(1 for p in pr.pieces if p not in finals) - running} waiting, "
                f"{len(finals)} ended")
        if line != shown:
            J.say(line)
            shown = line
        if len(have) >= s.tasks or pr.closed:
            if not pr.closed:
                mark_closed(rid)
            summary = summarize(s, out, finals, pr.pieces)  # before the cancels: the run reads done at once
            if not pr.closed:
                cancel_unstarted(rid, [p for p in pr.pieces if p not in finals])
            drop_results(rid)
            return summary
        if not pr.placed and now - (s.created or now) < PLACE_S:  # tools/run still places its pieces
            pause()
            continue
        alive = [p for p in pr.pieces if p not in finals]
        held = [p for p, f in finals.items() if f.phase == "held"]
        if (len(pr.done), len(pr.claimed)) != progress:
            progress, moved_at = (len(pr.done), len(pr.claimed)), now
        # a pooled run goes on on other runs' pieces: its own pieces' end stops it only while tasks wait unclaimed and
        # none moved for STUCK_S
        stuck = not s.pool or (now - moved_at > STUCK_S and s.tasks > len(pr.done | pr.claimed))
        if held and not alive and stuck:  # never routed around: the run stays open, and its follower stops, saying why
            J.say(f"run {rid}: no piece runs and {len(held)} held: " + "; ".join(J.describe(p, finals[p]) for p in held)
                  + f". Report it; `tools/compute wait {rid}` follows the run again")
            summary = summarize(s, out, finals, pr.pieces)
            summary["exit"] = J.HELD
            return summary
        broken = [p for p, f in finals.items() if f.exit not in (None, 0, 75) and f.phase != "held"]
        if broken and not alive and stuck:  # a piece that failed (not one lost) would fail again: the run stops
            p = broken[0]
            tail = J.read_log(J.job(p), 0).decode(errors="replace").splitlines()[-15:]
            J.say(f"run {rid}: piece {J.describe(p, finals[p])}, so no piece is started again; its log ends:\n  "
                  + "\n  ".join(tail))
            close(rid, [])
            return summarize(s, out, finals, pr.pieces)
        if s.pool:
            starting = {p for p in alive if states.get(p) and states[p].phase == "running"} - seen_running
            seen_running |= starting
            try:
                looked = follow_pool(s, pr, alive, states, bool(held), bool(starting), now, placed_at, moved_at)
            except Refused as e:  # a login that failed says nothing about the run: the next poll looks again
                J.say(f"run {rid}: {e}")
                looked = False
            if looked:
                placed_at = now
                if pr.rounds >= POOL_ROUNDS and not alive and now - moved_at > STALE_S + 60:
                    J.say(f"run {rid}: no piece took its tasks for {now - moved_at:.0f} s, after {pr.rounds} rounds "
                          f"of pieces: closing it with {s.tasks - len(have)} tasks left")
                    close(rid, [])
                    return summarize(s, out, finals, pr.pieces)
            pause()
            continue
        if alive:
            dead_since = None
        else:
            dead_since = dead_since or now
            left = s.tasks - len(have)
            unclaimed = s.tasks - len(pr.done | pr.claimed)
            if unclaimed > 0 or now - dead_since > (0 if LOCAL else STALE_S + 60):
                if pr.rounds >= MAX_ROUNDS:
                    J.say(f"run {rid}: every piece ended with {left} tasks left, after {pr.rounds} rounds of "
                          "replacements: closing it")
                    close(rid, [])
                    return summarize(s, out, finals, pr.pieces)
                k = pr.rounds + 1
                try:
                    login(f"mkdir {run_dir(rid)}/replace.{k}")  # one follower submits round K
                except Refused:
                    pass
                else:
                    J.say(f"run {rid}: every piece has ended with {left} tasks left: replacement round {k}")
                    place_pieces(s, left)
                    dead_since = None
        pause()


def follow_pool(s: Settings, pr: Progress, alive: list[str], states: dict[str, J.State], held: bool, starting: bool,
                now: float, placed_at: float, moved_at: float) -> bool:
    """A pooled run's pieces added, when its tasks wait: unclaimed, or claimed by dead pieces while none of its own
    runs, beyond what live pieces have free. Never while one of its pieces is held (a hold is reported, not routed
    around), queued for under QUEUED_S, or just started (its claims are coming), nor within GRACE_S of the last
    placement. True when it looked (the next look waits GRACE_S again)."""
    if held or starting or now - placed_at < GRACE_S:
        return False
    if any(states.get(p) is None or (states[p].phase in ("queued", "unknown") and now - submitted(p) < QUEUED_S)
           for p in alive):
        return False
    waiting = s.tasks - len(pr.done | pr.claimed)
    if not waiting and not alive and pr.claimed - pr.done and now - moved_at > STALE_S:
        waiting = stale_claims(s.id)
    if not waiting:
        return False
    if pr.rounds >= POOL_ROUNDS:
        return True
    free, live = pool_free(s)
    if waiting <= free:
        return True
    k = pr.rounds + 1
    try:
        login(f"mkdir {run_dir(s.id)}/replace.{k}")  # one follower submits round K
    except Refused:
        return True
    J.say(f"run {s.id}: {waiting} tasks wait, {free} free slots on {live} live pieces: adding pieces (round {k})")
    got: list[str] = []
    try:
        got = place_pieces(s, waiting - free)
    except Refused as e:
        J.say(f"run {s.id}: {e}")
    if not got:  # a round that added nothing (the backends full, its kind's pieces queued) isn't one of POOL_ROUNDS
        try:
            login(f"rmdir {run_dir(s.id)}/replace.{k}")
        except Refused:
            pass
    return True


def submitted(jid: str) -> float:
    """When a job was submitted (its id's local time, this year)."""
    m = re.fullmatch(rf"[a-z0-9]+-(\d\d)(\d\d)-(\d\d)(\d\d)(\d\d)-[0-9a-f]{{4,6}}", jid)
    if not m:
        return 0.0
    return time.mktime((time.localtime().tm_year, *(int(x) for x in m.groups()), 0, 0, -1))


def summarize(s: Settings, out: Path, finals: dict[str, J.State], pieces: list[str] | None = None
              ) -> dict[str, object]:
    results = [result_of(out, n) for n in range(s.tasks)]
    codes = [r.get("code") for r in results]
    failed = [n for n, c in enumerate(codes) if c != 0]
    gathered: dict[str, str] = {}
    for name in s.gather:
        dest = out / name
        with open(dest, "wb") as f:
            for n in range(s.tasks):
                part = out / str(n) / "out" / name
                if part.is_file():
                    f.write(part.read_bytes())
        gathered[name] = str(dest)
    secs = [float(str(r["s"])) for r in results if isinstance(r.get("s"), (int, float))]
    peaks = [int(str(r["peak"])) for r in results if isinstance(r.get("peak"), int)]
    summary: dict[str, object] = {
        "id": s.id, "tasks": s.tasks, "done": sum(c is not None for c in codes), "ok": codes.count(0),
        "failed": failed, "codes": {str(n): codes[n] for n in failed}, "results": str(out), "gathered": gathered,
        "task_s_total": round(sum(secs), 1), "task_peak_max": max(peaks, default=0),
        "hosts": sorted({str(r.get("host")) for r in results if r.get("host")}),
        "pieces": {p: J.describe(p, st) for p, st in finals.items()},
        "pieces_requested": len(pieces) if pieces is not None else len(finals),
        "served_by_others": sum(1 for r in results if r.get("piece_run")),
        "exit": exit_code(codes)}
    (out / "summary.json").write_text(json.dumps(summary, indent=1) + "\n")
    return summary


COMPACT_S = 24 * 3600  # a run's results, a day after they last changed, are packed: their per-task folders in one file
COMPACT_EVERY_S = 3600


def compact_local(out_root: Path = J.LOCAL_RUNS, dry: bool = False, force: bool = False) -> list[tuple[str, int]]:
    """Every run under OUT_ROOT unchanged for COMPACT_S (its summary, its newest task; tools/games stops following its
    batches without a summary): its per-task folders (N/: result.json, log, out/)
    packed into tasks.tgz, each task's result.json a line of tasks.jsonl, the folders removed; summary.json, run.json
    and gathered files stay. A run of 800 tasks held 2,400 small files and 3,200 folders: ~2.6 MB on disk for ~0.3 MB
    of data. At most every COMPACT_EVERY_S unless FORCE. Returns (run, bytes freed on disk)."""
    import tarfile
    stamp = out_root / ".compacted"
    now = time.time()
    try:
        if not force and now - stamp.stat().st_mtime < COMPACT_EVERY_S:
            return []
    except OSError:
        pass
    if not out_root.is_dir():
        return []
    if not dry:
        stamp.touch()
    out = []
    for d in sorted(out_root.iterdir()):
        if not (d.is_dir() and re.fullmatch(RUN_ID, d.name)):
            continue
        tasks = sorted((x for x in d.iterdir() if x.name.isdigit() and x.is_dir()), key=lambda x: int(x.name))
        if not tasks:
            continue
        last = max([x.stat().st_mtime for x in tasks] + [(d / f).stat().st_mtime for f in ("summary.json", "run.json")
                                                          if (d / f).exists()])
        if now - last < COMPACT_S:
            continue
        used = sum(os.lstat(os.path.join(p, f)).st_blocks * 512 for x in tasks for p, _, fs in os.walk(x) for f in fs) \
            + sum(os.lstat(p).st_blocks * 512 for x in tasks for p, _, _ in os.walk(x))
        if dry:
            out.append((d.name, used))
            continue
        tmp = d / ".tasks.tgz.tmp"
        with tarfile.open(tmp, "w:gz") as tar:
            for x in tasks:
                tar.add(x, x.name)
        with open(d / ".tasks.jsonl.tmp", "w") as f:
            for x in tasks:
                f.write(json.dumps(result_of(d, int(x.name))) + "\n")
        os.replace(d / ".tasks.jsonl.tmp", d / "tasks.jsonl")
        os.replace(tmp, d / "tasks.tgz")
        for x in tasks:
            shutil.rmtree(x, ignore_errors=True)
        out.append((d.name, used - (d / "tasks.tgz").stat().st_blocks * 512 - (d / "tasks.jsonl").stat().st_blocks * 512))
    return out


def exit_code(codes: list[object]) -> int:
    """0 when every task ended 0; else the first positive code; else 1 (a task lost, killed or never run)."""
    bad = [c for c in codes if c != 0]
    if not bad:
        return 0
    return next((c for c in bad if isinstance(c, int) and 0 < c < 256), 1)


def cancel(rid: str) -> str:
    """RID closed: its tasks stop within seconds wherever they run. Its pieces are cancelled, but a pooled run's
    running pieces: they may run other runs' tasks, so they stop its tasks and go on with those, or end at once."""
    pr = read_progress(rid)
    pooled = read_settings(rid).pool
    d = run_dir(rid)
    login(f"touch {d}/closed && rm -f {' '.join(f'{d}/{n}' for n in J.INPUTS)} {REMOTE_POOL}/open/{rid}")
    said, kept, ended = [], 0, []
    for pid, st in J.read_states(pr.pieces).items():
        if pooled and st.phase == "running":
            kept += 1
        elif st.phase in ("queued", "running", "held", "unknown"):
            ended.append(pid)
    if ended:
        said += J.cancel_many(ended).values()
    if kept:
        said.append(f"{kept} running piece{'s' * (kept != 1)} stop its tasks and go on with other runs' or end")
    return f"run {rid} closed; " + ("; ".join(said) if said else "no piece was live")


def report(summary: dict[str, object], out: Path) -> None:
    """The end of a run on stderr: its failed tasks' log ends, then a line."""
    failed = summary["failed"]
    assert isinstance(failed, list)
    for n in failed[:5]:
        log = out / str(n) / "log"
        tail = "\n".join(log.read_text(errors="replace").splitlines()[-20:]) if log.exists() else "(no log)"
        r = result_of(out, n)
        print(f"--- task {n} ended {r.get('code')} on {r.get('host') or 'no machine'}:\n{tail}", file=sys.stderr)
    print(f"run {summary['id']}: {summary['ok']} of {summary['tasks']} tasks ended 0; results in {summary['results']}",
          file=sys.stderr)
