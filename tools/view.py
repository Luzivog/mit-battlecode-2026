"""The remote state the tools on this machine share, read by one remote command at a time and at most every MAX_AGE_S
however many tools run: the pool's pieces, the open runs, our jobs in each scheduler's queue, and the progress and job
folders of every run and job a process here follows. No daemon: a process that needs the state reads the local
snapshot (CLIENT/view2.json) and, when it is older than MAX_AGE_S or lacks what it follows, refreshes it under a lock
(CLIENT/view2.lock), so the logins a minute don't grow with the number of runs followed.

What a process follows is its interest file (CLIENT/interest/PID-TOKEN.json, rewritten by each `get`, removed at its
exit; one silent for INTEREST_S is a dead process's): a refresh reads every live one's runs and jobs.
"""
from __future__ import annotations

import atexit
import json
import os
import re
import secrets
import time
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass, field
from pathlib import Path

from tools import backends as B
from tools import jobs as J
from tools import remote
from tools.piece import STATE_BEAT_S
from tools.remote import CONDOR, CPU_HOST, REMOTE_POOL, REMOTE_RUNS, SLURM_HOSTS, SUBMIT, Unreachable

MAX_AGE_S = 0.5 if remote.LOCAL else 5.0  # a snapshot this young is used as it is (tests: their pace)
MIN_GAP_S = 0.2 if remote.LOCAL else 2.0  # two refreshes are at least this far apart, even for a newcomer's run
INTEREST_S = 120.0  # an interest file not rewritten for this long is a dead process's
REWRITE_S = 30.0  # an unchanged interest file is rewritten this often
VIEW_S = 30.0  # a refresh's bound
# A run's folder is read again by a refresh once its last read is this old, and a reader takes it up to RUN_AGE_S old:
# followers poll every 30 s, and 30 open runs' folders read at every refresh took 2 s
RUN_REREAD_S = 0.3 if remote.LOCAL else 8.0
RUN_AGE_S = 0.6 if remote.LOCAL else 15.0
LOCK_WAIT_S = 3 * VIEW_S  # how long a reader waits for another process's refresh (tools/compute status: less)

# The snapshot and its lock, named by the snapshot's form: a tool of another form shares neither (its sections differ)
SNAP, LOCK = "view2.json", "view2.lock"
_me = f"{os.getpid()}-{secrets.token_hex(3)}"
_said: tuple[str, float] = ("", 0.0)

# Every piece's state is the name of an empty file (piece.state_name): one listing reads them all, no file opened (a
# file's content cost 16 ms a file on CephFS: 236 pieces, 3.9 s). A file named by its job id alone is an older piece's,
# its state inside (read while such pieces live). Then the names and states of our jobs in the GPU cluster's and
# Condor's queues, which say which pieces live, then the open runs.
POOL_SH = (f"mkdir -p {REMOTE_POOL}/pieces {REMOTE_POOL}/open; cd {REMOTE_POOL}/pieces && {{ echo ==pieces; ls; "
           "echo ==old; ls | grep -v , | xargs -r grep -s -H ''; }; cd /; "
           "echo ==sq; squeue -u \"$USER\" -h -o '%j %T'; echo ==sq_rc; echo $?; "
           f"echo ==cq; {CONDOR}; condor_q -name {SUBMIT} \"$USER\" -af JobBatchName JobStatus; echo ==cq_rc; echo $?; "
           f"echo ==open; ls {REMOTE_POOL}/open; ")
LIVE_S = 2 * STATE_BEAT_S + 60  # a piece whose scheduler wasn't read is live while its state is this young
DEAD_S = 3 * STATE_BEAT_S  # ... and dead, its file removed, once it is this old
STATE_FIELDS = ("job", "v", "slots", "running", "task_cpus", "task_mem_mb", "env", "deadline", "limit", "at")
PIECE_ID = r"[a-z0-9]+-\d{4}-\d{6}-[0-9a-f]{4,6}"  # a piece's job id, of any backend


def parse_state(name: str) -> dict[str, object]:
    """A piece's state from its file's name (piece.state_name); {} when the name isn't one."""
    f = name.split(",")
    if len(f) != len(STATE_FIELDS) or not re.fullmatch(PIECE_ID, f[0]):
        return {}
    out: dict[str, object] = {"job": f[0], "env": f[6]}
    try:
        for k, x in zip(STATE_FIELDS, f):
            if k in ("job", "env"):
                continue
            out[k] = None if x == "-" else int(x)
    except ValueError:
        return {}
    return out


def dead_sh(names: list[str]) -> str:
    """Dead pieces' state files removed (names checked: they reach a shell)."""
    ok = [n for n in names if re.fullmatch(rf"{PIECE_ID}(,[\w.-]+)*", n)]
    return f"(cd {REMOTE_POOL}/pieces && rm -f -- {' '.join(ok)}); " if ok else ""


def run_sh(rid: str) -> str:
    """RID's folder as read_progress reads it, in sections `run:RID:NAME`; none when the folder is gone."""
    d = f"{REMOTE_RUNS}/{rid}"
    k = f"==run:{rid}"
    return (f"if cd {d} 2>/dev/null; then echo {k}:done; ls done; echo {k}:claims; ls claims; echo {k}:pieces; "
            f"cat pieces; echo {k}:closed; ls closed 2>/dev/null; echo {k}:rounds; ls -d replace.* 2>/dev/null; "
            f"echo {k}:placed; ls placed 2>/dev/null; fi; cd /; ")


def _interest_dir() -> Path:
    return remote.CLIENT / "interest"


def _drop() -> None:
    (_interest_dir() / f"{_me}.json").unlink(missing_ok=True)


atexit.register(_drop)


def _want(runs: list[str], jobs: list[str]) -> None:
    """This process's interest file says RUNS and JOBS (rewritten when it changes, and every REWRITE_S)."""
    global _said
    said = json.dumps({"runs": sorted(set(runs)), "jobs": sorted(set(jobs))})
    if said == _said[0] and time.time() - _said[1] < REWRITE_S:
        return
    d = _interest_dir()
    d.mkdir(parents=True, exist_ok=True)
    tmp = d / f".{_me}.tmp"
    tmp.write_text(said)
    os.replace(tmp, d / f"{_me}.json")
    _said = (said, time.time())


def _interests() -> tuple[set[str], set[str]]:
    """Every live process's runs and jobs (dead ones' files removed)."""
    runs: set[str] = set()
    jobs: set[str] = set()
    now = time.time()
    for f in _interest_dir().glob("*.json"):
        try:
            if now - f.stat().st_mtime > INTEREST_S:
                f.unlink(missing_ok=True)
                continue
            d = json.loads(f.read_text())
        except (OSError, ValueError):
            continue
        runs |= {r for r in d.get("runs", []) if isinstance(r, str)}
        jobs |= {j for j in d.get("jobs", []) if isinstance(j, str)}
    return runs, jobs


@dataclass
class Snapshot:
    at: float
    runs: list[str]
    jobs: list[str]
    gpu: dict[str, list[str]]
    cpu: dict[str, list[str]] | None  # None: no cpucluster1 job followed
    error: str = ""  # the GPU-cluster login failed: every reader of this snapshot is Unreachable
    cpu_error: str = ""
    run_at: dict[str, float] = field(default_factory=dict)  # when each run's folder was read (RUN_REREAD_S)

    def covers(self, runs: list[str], jobs: list[str]) -> bool:
        now = time.time()
        return (all(now - self.run_at.get(r, 0.0) <= RUN_AGE_S for r in runs) and set(runs) <= set(self.runs)
                and set(jobs) <= set(self.jobs))

    def run(self, rid: str) -> dict[str, list[str]] | None:
        """RID's sections (done, claims, pieces, closed, rounds, placed), None when its folder is gone."""
        pre = f"run:{rid}:"
        got = {k[len(pre):]: v for k, v in self.gpu.items() if k.startswith(pre)}
        return got or None

    def states(self, ids: list[str]) -> dict[str, J.State]:
        """Each job of IDS this snapshot read (one it didn't: unknown, read at the next refresh)."""
        out: dict[str, J.State] = {i: J.State("unknown", "not read yet") for i in ids}
        js = [J.job(i) for i in ids if i in self.jobs]
        gpu = [j for j in js if J.hosts_of(j.where) == SLURM_HOSTS]
        cpu = [j for j in js if J.hosts_of(j.where) != SLURM_HOSTS]
        out |= J.parse_states(gpu, self.gpu)
        if cpu and self.cpu is not None:
            out |= J.parse_states(cpu, self.cpu)
        elif cpu:
            out |= {j.id: J.State("unknown", f"cpucluster1 unread: {self.cpu_error}") for j in cpu}
        return out

    def pool(self) -> tuple[list[dict[str, object]], float]:
        """Every piece's state file, each with its `name`, `age` (by its own time, against this snapshot's) and `live`:
        its scheduler lists it running (when the scheduler was read; else: its state younger than LIVE_S); the
        snapshot's time."""
        running: set[str] = set()
        read: set[str] = set()
        if self.gpu.get("sq_rc") == ["0"]:
            read.add("slurm")
            running |= {f[0].removeprefix("bc26-") for f in (x.split() for x in self.gpu.get("sq", []))
                        if len(f) == 2 and f[1] in ("RUNNING", "COMPLETING", "CONFIGURING")}
        if self.gpu.get("cq_rc") == ["0"]:
            read.add("condor")
            running |= {f[0].removeprefix("bc26-") for f in (x.split() for x in self.gpu.get("cq", []))
                        if len(f) == 2 and f[1] == "2"}
        pieces = []
        old = {n: said for n, _, said in (x.partition(":") for x in self.gpu.get("old", []))}
        for name in (x for x in self.gpu.get("pieces", []) if x.strip()):
            if "," in name:
                d = parse_state(name)
            else:  # an older piece's file: its state inside
                try:
                    d = json.loads(old.get(name, ""))
                    assert isinstance(d, dict)
                except (ValueError, AssertionError):
                    d = {}
            try:
                age: float | None = self.at - float(str(d["at"]))
            except (KeyError, TypeError, ValueError):
                age = None  # torn, or of an older protocol: never judged dead by its age
            job = name.split(",")[0]
            d |= {"name": name, "age": age, "backend": job.split("-")[0]}
            if d["backend"] in read:
                d["live"] = job in running
                d["dead"] = not d["live"]
            else:
                d["live"] = age is not None and age <= LIVE_S
                d["dead"] = age is not None and age > DEAD_S
            pieces.append(d)
        return pieces, self.at

    def open_runs(self) -> list[str]:
        return [x for x in self.gpu.get("open", []) if x.strip()]


def _load() -> Snapshot | None:
    try:
        d = json.loads((remote.CLIENT / SNAP).read_text())
        return Snapshot(**d)
    except (OSError, ValueError, TypeError):
        return None


def _store(s: Snapshot) -> None:
    tmp = remote.CLIENT / f".{SNAP}.{_me}"
    tmp.write_text(json.dumps(s.__dict__))
    os.replace(tmp, remote.CLIENT / SNAP)


def refresh() -> Snapshot:
    """One read of every live interest: a login on the GPU cluster (the pool, the runs, our Slurm and Condor queues,
    their jobs' folders) and, when a cpucluster1 job is followed, one there, at once."""
    runs, jobs = _interests()
    js = [J.job(i) for i in sorted(jobs)]
    gpu_js = [j for j in js if J.hosts_of(j.where) == SLURM_HOSTS]
    cpu_js = [j for j in js if J.hosts_of(j.where) != SLURM_HOSTS]
    last = _load()
    ok = last is not None and not last.error
    dead = [str(p["name"]) for p in last.pool()[0] if p["dead"]] if last is not None and ok else []
    now = time.time()
    keep = {r for r in runs if ok and last is not None and now - last.run_at.get(r, 0.0) < RUN_REREAD_S}
    due = sorted(runs - keep)
    gpu_cmd = (dead_sh(dead) + POOL_SH + "".join(run_sh(r) for r in due) + J.states_cmd(gpu_js, queues=True)
               + "true")
    with ThreadPoolExecutor(2) as ex:
        fg = ex.submit(remote.login, gpu_cmd, None, SLURM_HOSTS, 0, VIEW_S)
        fc = ex.submit(remote.login, J.states_cmd(cpu_js) + "true", None, (CPU_HOST,), 0, VIEW_S) if cpu_js else None
        snap = Snapshot(time.time(), sorted(runs), sorted(jobs), {}, None)
        try:
            snap.gpu = B.sections(fg.result())
            read_at = time.time()
            snap.run_at = {r: read_at for r in due}
            if last is not None:  # the runs not due: their last read, as it was
                snap.run_at |= {r: last.run_at[r] for r in keep}
                snap.gpu |= {k: v for k, v in last.gpu.items() if k.startswith("run:")
                             and k.split(":")[1] in keep}
        except remote.Refused as e:
            snap.error = str(e)
        if fc is not None:
            try:
                snap.cpu = B.sections(fc.result())
            except remote.Refused as e:
                snap.cpu_error = str(e)
    snap.at = time.time()
    return snap


def get(runs: list[str] | None = None, jobs: list[str] | None = None, wait: bool = True,
        max_age: float = MAX_AGE_S) -> Snapshot:
    """The shared state, with RUNS and JOBS in it (they become this process's interest): the snapshot when it is
    fresh enough and has them, else a refresh (one process at a time, MIN_GAP_S apart). Unreachable when the last
    refresh couldn't read the GPU cluster. Not WAIT (tools/compute status): the last snapshot, however old, while
    another process refreshes it. MAX_AGE: the oldest snapshot taken as it is."""
    runs, jobs = list(runs or []), list(jobs or [])
    _want(runs, jobs)
    s = _load()
    if s is not None and not wait and time.time() - s.at > max_age and remote.held(remote.CLIENT / LOCK):
        if s.error:
            raise Unreachable(s.error)
        return s
    if s is None or time.time() - s.at > max_age or not s.covers(runs, jobs):
        with remote.locked(remote.CLIENT / LOCK, LOCK_WAIT_S):
            s = _load()
            if s is None or time.time() - s.at > max_age or not s.covers(runs, jobs):
                if s is not None and time.time() - s.at < MIN_GAP_S:
                    time.sleep(MIN_GAP_S - (time.time() - s.at))
                s = refresh()
                _store(s)
    if s.error:
        raise Unreachable(s.error)
    return s
