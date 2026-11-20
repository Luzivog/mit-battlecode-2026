"""A piece: the node side of a run (tools/runs.py), `python3 RUN_DIR/piece.py RUN_DIR`, run by job.sh in the run's
checkout. Standard library only: it runs on any node.

It claims tasks one at a time and runs as many at once as its machine holds: JOB_CPUS // task_cpus, and no more than
JOB_MEM_MB // task_mem_mb (a whole-machine task runs alone). A claim is a folder, claims/N, made by mkdir, which the
volume's server makes atomically: two pieces never run one task at once while both live. While a task runs its piece
touches claims/N/beat every BEAT_S; a claim whose beat is older than STALE_S belongs to a piece that died (its machine
evicted it or failed), and any piece may take it over (renamed into lost/, then claimed again). A task is started at
most MAX_STARTS times; the last loss ends it with code LOST.

The pool. A pooled run (run.json's `pool`; tools/runs.py) is listed in POOL/open/ while it is open. A piece of a
pooled run serves every open pooled run it can: the same task CPUs and memory, the same runtime (`env`), its backend
among the run's `wheres`, and, when the piece has a deadline (Slurm's --time, Condor's), at least MIN_LEFT_S (or half
its limit, when shorter) left and 1.5 times the longest task of that run it has run. Each free slot goes to the run with the fewest running tasks, the
oldest first among equals: a small run submitted behind a big one gets the next free slots, and no run waits forever,
since a run's running count only rises when it is served and an older run wins every tie. Another run's tasks run in
that run's own checkout, made once a content (its commit and its shipped files) in the piece's scratch, with that
run's kit. A piece ends as soon as it runs nothing and no task it can serve is claimable anywhere: no idle grace. Its
state (slots, running, task size, runtime, deadline, its time) is the name of an empty file in POOL/pieces
(state_name), renamed when it changes (at most every STATE_S, at once when its free slots reach or leave 0) and every
STATE_BEAT_S, removed at its end:
`tools/run` counts the live pieces' free slots from it (tools/view.py: a piece its scheduler no longer lists is dead,
and its file goes).

A task's result is done/N.tar (result.json, log, out/), written whole under a temporary name and renamed in. Its
command runs with {} (its line), {file} (a file of its lines), {out} (its OUT) and {cpus} (its CPUs) replaced, in the
checkout's folder the submitter ran from, with OUT, JOB_OUT, JOB_CPUS, JOB_MEM_MB, TASK, RUN_ID and TMPDIR its own.

A host that can't hold more work takes none: a piece claims a task only while its scratch's filesystem has DISK_MIN_MB
free (its unused checkouts are pruned first). A task that ends GIVE_BACK says this host can't run it (its game had no
room to write): its claim goes back at once, for another piece, and this piece takes no more tasks. The tasks of one
piece share JOB_CACHE, a folder of its scratch (the native binaries they unpack, once a piece).

A run's `closed` stops its tasks (claims given back) within CLOSED_S; a piece whose own run is unpooled then ends.
SIGTERM (a cancel, an eviction) and SIGUSR1 (Slurm's limit) stop it: its tasks are stopped and their claims given back
at once (into lost/, so they count as a start), then it exits 0 (on Slurm after SIGUSR1, job.sh then requeues the
piece).
"""
from __future__ import annotations

import hashlib
import json
import os
import secrets
import shutil
import signal
import socket
import subprocess
import sys
import tarfile
import time
from pathlib import Path

BEAT_S = 30.0  # between two touches of a running task's claim
STALE_S = 300.0  # a claim not touched for this long is a dead piece's (well past NFS's attribute cache, 60 s)
MAX_STARTS = 3  # a task's starts, the first included
LOG_BYTES = 1 << 20  # the end of a task's output kept
OUT_CAP = 1 << 30  # the most a task's OUT may hold: a run's writes on the shared volume are bounded
LOST = -1  # the code of a task whose every start was lost
# The pool's protocol: a piece serves only runs that speak its version. 4: a run's code is base.bundle (main) and a
# thin code.bundle, which a version-3 piece can't check out. 3: the state file is written seldom and says its time.
POOL_V = 4
SCAN_S = 2.0  # between two looks for work while a slot is free and the last look found none
FULL_S = 60.0  # a run found with nothing claimable is looked at again after this long (its claims come back only
# from a dead piece's, after STALE_S, or a stopped task's)
STALE_CHECK_S = 120.0  # a run's claims are read for dead pieces' at most this often
CLOSED_S = 5.0  # a run whose task runs here is checked for `closed` this often
# The piece's state, the name of an empty file (state_name): a change is renamed in at most every STATE_S (at once when
# it starts, or when its free slots reach or leave 0), an unchanged state every STATE_BEAT_S. A state in a file's
# content cost a reader 16 ms a file on CephFS (236 pieces: 3.9 s), a listing of the folder 0.2 s.
STATE_S = 30.0
STATE_BEAT_S = 600.0
MIN_LEFT_S = 1200.0  # a piece with a deadline takes another run's task only with this long left (or half its limit)
CHECKOUTS = 4  # other runs' checkouts kept unused in scratch (beyond those tasks run in)
# a piece claims a task only with this much free where its tasks write (a checkout is ~350 MB, a binary 100 MB)
DISK_MIN_MB = int(os.environ.get("BC26_DISK_MIN_MB", "2048"))
GIVE_BACK = 75  # a task's code saying its host can't run it (EX_TEMPFAIL): its claim goes back, the piece stops
USR1_LEAD_S = 120  # Slurm's SIGUSR1 comes this long before --time (tools/backends.py)


def state_name(job: str, slots: int, running: int, task_cpus: int, task_mem_mb: int, env: str,
               deadline: float | None, limit: float | None, at: float) -> str:
    """A pooled piece's state as a file name, fields joined by commas: its job id, POOL_V, slots, running, task CPUs,
    task memory, runtime, deadline and limit (epoch s and s, `-` for none), the time it was written (epoch s)."""
    def num(x: float | None) -> str:
        return "-" if x is None else str(int(x))
    return ",".join([job, str(POOL_V), str(slots), str(running), str(task_cpus), str(task_mem_mb), env, num(deadline),
                     num(limit), num(at)])


def now_on(volume: Path, me: str) -> float:
    """The volume server's clock, as a freshly touched file's mtime says: claims' beats are its times, not ours."""
    f = volume / "clock" / me
    f.parent.mkdir(exist_ok=True)
    f.touch()
    return f.stat().st_mtime


def ids(folder: Path, suffix: str = "") -> set[int]:
    """The task numbers named in FOLDER (with SUFFIX)."""
    out = set()
    for x in os.listdir(folder):
        if x.endswith(suffix):
            stem = x[:len(x) - len(suffix)]
            if stem.isdigit():
                out.add(int(stem))
    return out


class Run:
    """A run this piece has looked at: its settings, read once, and what the piece knows of it."""

    def __init__(self, d: Path, own: bool) -> None:
        self.dir = d
        self.own = own
        self.meta = json.loads((d / "run.json").read_text())
        self.id = str(self.meta["id"])
        self.n = int(self.meta["tasks"])
        self.created = float(self.meta.get("created", 0.0))
        self._tasks: list[dict[str, object]] | None = None
        self.skip_until = 0.0  # monotonic: nothing claimable was found; looked at again after it
        self.stale_at = 0.0  # monotonic: when its claims may next be read for dead pieces'
        self.closed_at = 0.0  # monotonic: when `closed` was last checked
        self.closed = False
        self.longest = 0.0  # the longest task of it this piece ran (s)
        self.bad = ""  # why this piece can't serve it (its checkout failed)
        self.home: Path | None = None  # its checkout here
        self.env: dict[str, str] | None = None  # its kit's names (None: the job's own)
        self.first_stale = True

    @property
    def tasks(self) -> list[dict[str, object]]:
        if self._tasks is None:
            self._tasks = [json.loads(x) for x in (self.dir / "tasks.jsonl").read_text().splitlines() if x.strip()]
        return self._tasks

    def is_closed(self) -> bool:
        self.closed = self.closed or (self.dir / "closed").exists()
        return self.closed


class Piece:
    def __init__(self, run: Path) -> None:
        self.own = Run(run, True)
        self.run = run
        self.meta = self.own.meta
        self.root = run.resolve().parent.parent  # REMOTE_ROOT: runs/RUN's grandparent
        self.pool = self.root / "pool"
        self.me = f"{os.environ.get('JOB_ID', 'piece')}"
        self.backend = self.me.split("-")[0]
        self.host = socket.gethostname().split(".")[0]
        self.scratch = Path(os.environ.get("TMPDIR", "/tmp")) / f"piece-{secrets.token_hex(4)}"
        self.scratch.mkdir(parents=True)
        cpus = int(os.environ.get("JOB_CPUS", "1"))
        mem = int(os.environ.get("JOB_MEM_MB", "0")) or 1 << 30
        self.whole = self.meta["task_cpus"] == 0
        if self.whole:
            self.slots, self.task_cpus, self.task_mem = 1, cpus, mem
        else:
            self.task_cpus, self.task_mem = self.meta["task_cpus"], self.meta["task_mem_mb"]
            self.slots = max(1, min(cpus // self.task_cpus, mem // self.task_mem))
        self.pooled = (not self.whole and bool(self.meta.get("pool")) and self.meta.get("pool_v") == POOL_V)
        self.started = time.time()
        self.deadline = self.deadline_of()
        self.runs: dict[str, Run] = {self.own.id: self.own}
        # (run id, task) -> (its run, the process, monotonic start, its folder, wall start)
        self.running: dict[tuple[str, int], tuple[Run, subprocess.Popen[bytes], float, Path, float]] = {}
        self.checkouts: dict[str, tuple[Path, float]] = {}  # content -> (folder, last used)
        self.next_scan = 0.0
        self.state_at = 0.0
        self.state_said = ""
        self.state_free = True
        self.state_file = ""  # its state's current name in POOL/pieces
        self.stopping = ""
        self.refusing = ""  # why this piece takes no more tasks (its host can't run them), once a task said so
        self.low_disk = False
        self.cache = self.scratch / "cache"  # JOB_CACHE: shared by its tasks
        self.cache.mkdir()
        for d in ("claims", "done", "lost"):
            (run / d).mkdir(exist_ok=True)
        self.say(f"piece {self.me} on {self.host}: {self.slots} at once, {self.task_cpus} CPUs and "
                 f"{self.task_mem} MB a task, {len(self.own.tasks)} tasks in the run"
                 + (", serving the pool" if self.pooled else ""))

    def say(self, msg: str) -> None:
        print(f"[{time.strftime('%H:%M:%S')}] {msg}", flush=True)

    def deadline_of(self) -> float | None:
        """When this piece's allocation ends (epoch s), or None: Slurm's and cpucluster1's --time (less the SIGUSR1
        lead), Condor's removal when --time was given."""
        self.limit = None
        if self.backend in ("slurm", "cpucluster1"):
            self.limit = float(self.meta.get("time_s", 0)) - USR1_LEAD_S
        elif self.backend == "condor" and self.meta.get("condor_time_s"):
            self.limit = float(self.meta["condor_time_s"])
        return None if self.limit is None else self.started + self.limit

    def name(self, r: Run, n: int) -> str:
        return f"task {n}" if r.own else f"task {r.id}/{n}"

    # ---- which runs ----
    def serves(self, r: Run) -> bool:
        """R's tasks fit this piece: its own run's always; another run's when both are pooled, of one protocol, task
        size and runtime, and this piece's backend is one R allows."""
        if r.own:
            return True
        m = r.meta
        return (self.pooled and not r.bad and bool(m.get("pool")) and m.get("pool_v") == POOL_V
                and m.get("task_cpus") == self.meta["task_cpus"] and m.get("task_mem_mb") == self.meta["task_mem_mb"]
                and m.get("env") == self.meta.get("env") and self.backend in m.get("wheres", ()))

    def fits(self, r: Run) -> bool:
        """Time enough left for one of R's tasks: always without a deadline or for its own run."""
        if r.own or self.deadline is None or self.limit is None:
            return True
        return self.deadline - time.time() >= max(min(MIN_LEFT_S, self.limit / 2), 1.5 * r.longest)

    def open_runs(self) -> list[Run]:
        """The runs this piece may take a task from now: its own while open, and, pooled, every open pooled run it
        serves (POOL/open, read now)."""
        listed: set[str] = set()
        if self.pooled:
            try:
                listed = set(os.listdir(self.pool / "open"))
            except OSError:
                listed = set()
            for rid in sorted(listed - set(self.runs)):
                try:
                    self.runs[rid] = Run(self.root / "runs" / rid, False)
                except (OSError, ValueError, KeyError):  # not visible here yet, or gone: looked for again later
                    continue
            for rid in [k for k, r in self.runs.items() if not r.own and k not in listed]:
                if not any(key[0] == rid for key in self.running):
                    self.runs.pop(rid)
        out = []
        for r in self.runs.values():
            if r.own:
                if not r.is_closed():
                    out.append(r)
            elif r.id in listed and not r.closed and self.serves(r):
                out.append(r)
        return out

    # ---- claims ----
    def claim(self, r: Run, n: int) -> bool:
        try:
            (r.dir / "claims" / str(n)).mkdir()
        except FileExistsError:
            return False
        except OSError:  # the run's folder went (swept): not claimable
            return False
        (r.dir / "claims" / str(n) / "by").write_text(f"{self.me} {self.host} {os.getpid()}\n")
        return True

    def give_back(self, r: Run, n: int, why: str) -> None:
        """Task N's claim moved into lost/ (a rename, atomic on the server: only one piece's move wins)."""
        try:
            os.rename(r.dir / "claims" / str(n), r.dir / "lost" / f"{n}.{secrets.token_hex(4)}")
            self.say(f"{self.name(r, n)}: claim given up ({why})")
        except OSError:
            pass

    def release(self, r: Run, n: int, why: str) -> None:
        """Task N's claim removed unrun, not counted as a start: this piece can't run it (its checkout failed here), any
        other piece may. Counted as a start, three pieces of an older protocol that can't read a run's bundles ended
        its tasks LOST."""
        try:
            shutil.rmtree(r.dir / "claims" / str(n))
            self.say(f"{self.name(r, n)}: claim released ({why})")
        except OSError:
            pass

    def starts(self, r: Run, n: int) -> int:
        """Task N's starts lost so far (its claims taken over or given back), plus the one it holds."""
        return sum(1 for x in os.listdir(r.dir / "lost") if x.split(".")[0] == str(n))

    def take_stale(self, r: Run, claims: set[int], done: set[int]) -> list[int]:
        """R's claims of dead pieces (no beat for STALE_S, or an earlier attempt of this piece's), given back: now
        claimable."""
        clock = now_on(r.dir, self.me)
        mine_running = {k[1] for k in self.running if k[0] == r.id}
        out = []
        for n in sorted(claims - done - mine_running):
            d = r.dir / "claims" / str(n)
            by: list[str] = []
            if r.first_stale and r.own:  # an earlier attempt of this piece (a requeued job) left its claims
                try:
                    by = (d / "by").read_text().split()
                except OSError:
                    by = []
            try:
                beat = max(d.stat().st_mtime, (d / "beat").stat().st_mtime if (d / "beat").exists() else 0.0)
            except OSError:
                continue
            mine = bool(by) and by[0] == self.me  # an earlier attempt of this piece: dead, since this one runs
            if mine or clock - beat > STALE_S:
                self.give_back(r, n, "an earlier attempt of this piece" if mine else f"no beat for {clock - beat:.0f} s")
                out.append(n)
        r.first_stale = False
        return out

    def look(self, r: Run, now: float) -> tuple[int, list[int]] | None:
        """R's running tasks and its claimable ones (dead pieces' claims given back first, when due); None when it
        has none."""
        try:
            claims, done = ids(r.dir / "claims"), ids(r.dir / "done", ".tar")
        except OSError:
            return None
        if len(done) >= r.n:
            if self.pooled:
                try:  # every task done: no piece needs to look at it again
                    os.unlink(self.pool / "open" / r.id)
                except OSError:
                    pass
            r.skip_until = now + FULL_S
            return None
        free = [n for n in range(r.n) if n not in claims and n not in done]
        if not free and now >= r.stale_at:
            r.stale_at = now + STALE_CHECK_S
            try:
                free = self.take_stale(r, claims, done)
            except OSError:
                free = []
            claims -= set(free)
        if not free:
            r.skip_until = now + FULL_S
            return None
        return len(claims - done), free

    def fill(self, fresh: bool = False) -> int:
        """Free slots filled from every run served, the run with the fewest running tasks first (the oldest among
        equals); FRESH looks at runs recently found full too. The tasks started."""
        free = self.slots - len(self.running)
        now = time.monotonic()
        if free <= 0 or self.refusing or not self.room():
            self.next_scan = now + SCAN_S
            return 0
        cand: dict[str, tuple[Run, list[int], list[int]]] = {}  # id -> (run, [running], claimable)
        for r in self.open_runs():
            if (r.skip_until > now and not fresh) or not self.fits(r):
                continue
            got = self.look(r, now)
            if got is not None:
                cand[r.id] = (r, [got[0]], got[1])
        started = 0
        while free > 0 and cand:
            rid = min(cand, key=lambda k: (cand[k][1][0], cand[k][0].created, k))
            r, count, todo = cand[rid]
            n = todo.pop(0)
            if not todo:
                del cand[rid]
            # room first (it may prune unused checkouts), then the claim, then the checkout: one made before a claim
            # that failed, or pruned by the room check before its task started, was made again for every claim
            # (a piece remade one run's checkout every 5 s for 4 minutes on 19 Nov)
            if not self.room():
                break
            if not self.claim(r, n):
                continue
            if self.starts(r, n) >= MAX_STARTS:
                self.lost(r, n)
                continue
            if not self.ready(r):
                self.release(r, n, "its checkout failed here")
                cand.pop(rid, None)
                continue
            self.start(r, n)
            count[0] += 1
            free -= 1
            started += 1
        self.next_scan = now + (0.0 if started else SCAN_S)
        return started

    # ---- another run's checkout ----
    def content(self, r: Run) -> str:
        return f"{r.meta.get('code_key') or r.meta.get('commit')}-{r.meta.get('with_key', '')}"

    def ready(self, r: Run) -> bool:
        """R's checkout and kit here: the job's own for its run and any run of the same content, else one made in
        scratch from R's bundles and with.tgz (kept for later runs of that content). False, R set aside, when it
        can't be made."""
        if r.home is not None:
            if r.home.exists():
                return True
            r.home = None
        if r.own or (self.content(r) == self.content(self.own) and self.own.meta.get("kit") == r.meta.get("kit")
                     and r.meta.get("code_key")):
            r.home, r.env = Path.cwd(), None
            return True
        key = hashlib.sha256(self.content(r).encode()).hexdigest()[:16]
        try:
            env = self.kit_env(r)
            if key in self.checkouts:
                home = self.checkouts[key][0]
            else:
                home = self.make_checkout(r, key)
        except (OSError, subprocess.SubprocessError, ValueError) as e:
            r.bad = f"its checkout failed: {e}"
            self.say(f"run {r.id} set aside: {r.bad}")
            return False
        self.checkouts[key] = (home, time.monotonic())
        r.home, r.env = home, env
        self.prune_checkouts(keep=home)
        return True

    def kit_env(self, r: Run) -> dict[str, str]:
        """R's kit names (BC_CLASSPATH, BC_ENGINE_JARS: its own checkout's classes and jars), or none."""
        if not r.meta.get("kit"):
            return {}
        jars = [x for x in (r.dir / "kit").read_text().split() if x]
        for j in jars:
            if not os.path.getsize(j):
                raise ValueError(f"the engine jar {j} is empty")
        return {"BC_ENGINE_JARS": ":".join(jars), "BC_CLASSPATH": ":".join(["{home}/build/classes", *jars])}

    def make_checkout(self, r: Run, key: str) -> Path:
        """R's commit checked out from its base.bundle and code.bundle into scratch (job.sh's way), its with.tgz
        unpacked into it."""
        final = self.scratch / "co" / key
        tmp = self.scratch / "co" / f".{key}.{secrets.token_hex(3)}"
        tmp.parent.mkdir(exist_ok=True)
        t0 = time.monotonic()

        def git(*args: str) -> str:
            p = subprocess.run(["git", *args], capture_output=True, timeout=900, stdin=subprocess.DEVNULL)
            if p.returncode:
                raise ValueError(f"git {args[0]}: exit {p.returncode}: {p.stderr.decode(errors='replace')[-300:]}")
            return p.stdout.decode(errors="replace")
        try:
            git("-c", "init.defaultBranch=job", "init", "-q", str(tmp))
            for name in ("base.bundle", "code.bundle"):  # main first: code.bundle holds only what the commit adds
                b = r.dir / name
                if not (b.exists() and b.stat().st_size):
                    continue
                heads = [x.split() for x in git("-C", str(tmp), "bundle", "list-heads", str(b)).splitlines()
                         if x.strip()]
                git("-C", str(tmp), "fetch", "-q", str(b), *sorted({h[0] for h in heads}))
                main = [h[0] for h in heads if len(h) > 1 and h[1] == "refs/heads/main"]
                if main:
                    git("-C", str(tmp), "update-ref", "refs/heads/main", main[0])
            git("-C", str(tmp), "-c", "advice.detachedHead=false", "checkout", "-q", "--detach", str(r.meta["commit"]))
            w = r.dir / "with.tgz"
            if w.exists() and w.stat().st_size:
                p = subprocess.run(["tar", "-xzf", str(w), "-C", str(tmp)], capture_output=True, timeout=900)
                if p.returncode:
                    raise ValueError(f"unpacking with.tgz: {p.stderr.decode(errors='replace')[-300:]}")
            shutil.rmtree(final, ignore_errors=True)  # a pruned one's leftovers
            os.rename(tmp, final)
        finally:
            shutil.rmtree(tmp, ignore_errors=True)
        self.say(f"run {r.id}: its checkout made in {time.monotonic() - t0:.1f} s")
        return final

    def prune_checkouts(self, keep: Path | None = None, idle_kept: int = CHECKOUTS) -> None:
        """Other runs' unused checkouts beyond IDLE_KEPT, unused longest first, deleted: never one a task runs in, nor
        KEEP (the one a task is about to start in). Counting every checkout instead deleted the one just made whenever
        more than CHECKOUTS were busy, so each claim made it again (2 s each) and the main loop stalled for minutes:
        no beats, its tasks' claims taken over as dead (19 Nov)."""
        busy = {r.home for r, _, _, _, _ in self.running.values()} | {keep}
        idle = sorted((used, key) for key, (home, used) in self.checkouts.items() if home not in busy)
        for _, key in idle[:max(0, len(idle) - idle_kept)]:
            home = self.checkouts.pop(key)[0]
            for r in self.runs.values():
                if r.home == home:
                    r.home = None
            shutil.rmtree(home, ignore_errors=True)

    def room(self) -> bool:
        """DISK_MIN_MB free where this piece's tasks write, after pruning its unused checkouts if short; said once
        when it runs short and once when it recovers."""
        def free_mb() -> float:
            try:
                st = os.statvfs(self.scratch)
            except OSError:
                return 0.0
            return st.f_bavail * st.f_frsize / (1 << 20)
        mb = free_mb()
        if mb < DISK_MIN_MB and self.checkouts:
            self.prune_checkouts(idle_kept=0)
            mb = free_mb()
        low = mb < DISK_MIN_MB
        if low != self.low_disk:
            self.low_disk = low
            self.say(f"{mb:.0f} MB free under {self.scratch}: " + ("taking no task" if low else "taking tasks again"))
        return not low

    # ---- tasks ----
    def start(self, r: Run, n: int) -> None:
        t = r.tasks[n]
        here = self.scratch / "t" / f"{r.id}.{n}"
        shutil.rmtree(here, ignore_errors=True)
        (here / "out").mkdir(parents=True)
        (here / "tmp").mkdir()
        lines = list(t.get("lines", []))  # type: ignore[call-overload]
        (here / "input").write_text("".join(f"{x}\n" for x in lines))
        subs = {"{}": lines[0] if len(lines) == 1 else "", "{file}": str(here / "input"), "{out}": str(here / "out"),
                "{cpus}": str(self.task_cpus)}
        argv = []
        for a in t["argv"]:  # type: ignore[union-attr]
            for k, v in subs.items():
                a = a.replace(k, v)
            argv.append(a)
        assert r.home is not None
        env = dict(os.environ)
        if r.env is not None:  # another content's checkout: its kit, or none
            env.pop("BC_ENGINE_JARS", None)
            env.pop("BC_CLASSPATH", None)
            env |= {k: v.replace("{home}", str(r.home)) for k, v in r.env.items()}
        env |= dict(OUT=str(here / "out"), JOB_OUT=str(here / "out"), JOB_CPUS=str(self.task_cpus),
                    JOB_MEM_MB=str(self.task_mem), TASK=str(n), RUN_ID=r.id, TMPDIR=str(here / "tmp"),
                    JOB_CACHE=str(self.cache))
        cwd = r.home / str(r.meta.get("cwd", "."))
        log = open(here / "log", "wb")
        try:
            p = subprocess.Popen(argv, cwd=cwd, env=env, stdin=subprocess.DEVNULL, stdout=log,
                                 stderr=subprocess.STDOUT, start_new_session=True)
        except OSError as e:
            log.write(f"piece: {e}\n".encode())
            log.close()
            self.finish(r, n, 127, 0, 0.0, here, time.time())
            return
        log.close()
        self.running[(r.id, n)] = (r, p, time.monotonic(), here, time.time())
        self.say(f"{self.name(r, n)}: started (start {self.starts(r, n) + 1})")

    def finish(self, r: Run, n: int, code: int, peak: int, secs: float, here: Path, began: float) -> None:
        log = here / "log"
        data = log.read_bytes() if log.exists() else b""
        if len(data) > LOG_BYTES:
            log.write_bytes(b"[... its first %d bytes cut]\n" % (len(data) - LOG_BYTES) + data[-LOG_BYTES:])
        size = sum(f.stat().st_size for f in (here / "out").rglob("*") if f.is_file())
        if size > OUT_CAP:
            shutil.rmtree(here / "out")
            (here / "out").mkdir()
            with open(log, "ab") as f:
                f.write(b"\npiece: its OUT held %d bytes, over the %d a task may send back: dropped\n" % (size, OUT_CAP))
            code = code or 254
        result = {"task": n, "code": code, "peak": peak, "host": self.host, "s": round(secs, 3), "piece": self.me,
                  "start": self.starts(r, n) + 1, "began": round(began, 3), "ended": round(time.time(), 3),
                  "slots": self.slots}
        if not r.own:
            result["piece_run"] = self.own.id  # a piece of another run served it
        (here / "result.json").write_text(json.dumps(result) + "\n")
        tmp = self.scratch / f"{r.id}.{n}.tar"
        with tarfile.open(tmp, "w") as tar:
            for name in ("result.json", "log", "out"):
                tar.add(here / name, name)
        try:
            dest = r.dir / "done" / f".{n}.tar.{secrets.token_hex(4)}"
            shutil.copyfile(tmp, dest)
            os.rename(dest, r.dir / "done" / f"{n}.tar")
        except OSError as e:  # its run's folder went (swept): nothing to give it
            self.say(f"{self.name(r, n)}: its result has nowhere to go ({e})")
        tmp.unlink()
        shutil.rmtree(here, ignore_errors=True)
        r.longest = max(r.longest, secs)
        self.say(f"{self.name(r, n)}: ended {code} in {secs:.1f} s")

    def lost(self, r: Run, n: int) -> None:
        """Task N, claimed again after MAX_STARTS starts that were all lost: ended LOST without running."""
        here = self.scratch / "t" / f"{r.id}.{n}"
        (here / "out").mkdir(parents=True, exist_ok=True)
        (here / "log").write_text(f"piece: task {n} lost its machine at each of its {MAX_STARTS} starts\n")
        self.finish(r, n, LOST, 0, 0.0, here, time.time())

    def reap(self) -> int:
        ended = 0
        for key, (r, p, began, here, wall) in list(self.running.items()):
            got = os.wait4(p.pid, os.WNOHANG)
            if got[0] == 0:
                continue
            del self.running[key]
            p.returncode = os.waitstatus_to_exitcode(got[1])
            ended += 1
            if p.returncode == GIVE_BACK:
                tail = (here / "log").read_bytes()[-300:].decode(errors="replace").strip().splitlines()
                self.refusing = tail[-1] if tail else "its task ended GIVE_BACK"
                self.say(f"{self.name(r, key[1])}: its host can't run it ({self.refusing}): taking no more tasks")
                shutil.rmtree(here, ignore_errors=True)
                self.give_back(r, key[1], "its host can't run it")
                continue
            self.finish(r, key[1], p.returncode, got[2].ru_maxrss * 1024, time.monotonic() - began, here, wall)
        return ended

    def halt(self, keys: list[tuple[str, int]], why: str) -> None:
        """The tasks of KEYS stopped (SIGTERM, SIGKILL 10 s later) and their claims given back."""
        for key in keys:
            try:
                os.killpg(self.running[key][1].pid, signal.SIGTERM)
            except ProcessLookupError:
                pass
        end = time.monotonic() + 10
        while any(self.running[k][1].poll() is None for k in keys) and time.monotonic() < end:
            time.sleep(0.2)
        for key in keys:
            r, p, _, here, _ = self.running.pop(key)
            try:
                os.killpg(p.pid, signal.SIGKILL)
            except ProcessLookupError:
                pass
            p.wait()
            shutil.rmtree(here, ignore_errors=True)
            self.give_back(r, key[1], why)

    def check_closed(self) -> None:
        """The tasks of runs closed since they started stopped (every CLOSED_S a run)."""
        now = time.monotonic()
        for rid in sorted({k[0] for k in self.running}):
            r = self.runs[rid]
            if now - r.closed_at >= CLOSED_S:
                r.closed_at = now
                if r.is_closed():
                    keys = [k for k in self.running if k[0] == rid]
                    self.say(f"run {rid} is closed: {len(keys)} of its tasks stopped")
                    self.halt(keys, "its run is closed")

    def beat(self) -> None:
        for (_, n), (r, _, _, _, _) in self.running.items():
            try:
                (r.dir / "claims" / str(n) / "beat").touch()
            except OSError:
                pass

    def write_state(self, force: bool = False) -> None:
        """POOL/pieces/STATE: what this piece holds, for tools/run's count of free slots (pooled pieces only), in the
        name of an empty file (state_name), renamed as it changes: a reader lists the folder and opens no file."""
        if not self.pooled:
            return
        said = state_name(self.me, self.slots, len(self.running), self.task_cpus, self.task_mem,
                          str(self.meta.get("env") or ""), self.deadline, self.limit, 0)
        now = time.monotonic()
        free = len(self.running) < self.slots
        due = (not self.state_at or free != self.state_free or now - self.state_at >= STATE_BEAT_S
               or (said != self.state_said and now - self.state_at >= STATE_S))
        if not (force or due):
            return
        name = state_name(self.me, self.slots, len(self.running), self.task_cpus, self.task_mem,
                          str(self.meta.get("env") or ""), self.deadline, self.limit, time.time())
        d = self.pool / "pieces"
        try:
            d.mkdir(parents=True, exist_ok=True)
            try:
                if not self.state_file:
                    raise FileNotFoundError
                os.rename(d / self.state_file, d / name)
                os.utime(d / name)  # a rename keeps the old time: the sweep takes a file silent for an hour
            except FileNotFoundError:  # its first, or one removed (taken for a dead piece's)
                (d / name).touch()
        except OSError:
            return
        self.state_file, self.state_at, self.state_said, self.state_free = name, now, said, free

    def drop_state(self) -> None:
        if self.pooled and self.state_file:
            try:
                os.unlink(self.pool / "pieces" / self.state_file)
            except OSError:
                pass

    def stop(self, sig: int, _frame: object) -> None:
        self.stopping = signal.Signals(sig).name

    def main(self) -> int:
        signal.signal(signal.SIGTERM, self.stop)
        signal.signal(signal.SIGUSR1, self.stop)
        last_beat = 0.0
        while not self.stopping:
            self.check_closed()
            if self.own.closed and not self.pooled:
                break
            if len(self.running) < self.slots and time.monotonic() >= self.next_scan:
                self.fill()
            if not self.running and not self.stopping:
                if self.fill(fresh=True) == 0:  # one last look everywhere, then the piece ends: no idle allocation
                    break
            self.write_state()
            time.sleep(1)
            if self.reap():
                self.next_scan = 0.0
            if time.monotonic() - last_beat >= BEAT_S:
                self.beat()
                last_beat = time.monotonic()
        if self.running:
            why = self.stopping or "the run is closed"
            self.say(f"stopping ({why}): {len(self.running)} tasks stopped")
            self.halt(list(self.running), why)
        self.drop_state()
        shutil.rmtree(self.scratch, ignore_errors=True)
        self.say("piece done")
        return 0


if __name__ == "__main__":
    sys.exit(Piece(Path(sys.argv[1])).main())
