"""The Imperial backend of tools/games: plays jobs on DoC's machines through track C's tools/run (the compute skill,
.claude/skills/compute/references/internals.md "The games interface").

Each batch of jobs becomes a spec file and one `tools/run --each SPECS --chunk K -- tools/play {file} {out}`:
- it ships `.bcgames-ship/` (git-ignored): the engine jar, the bot jars this run plays and the compiled runner;
- each task plays its K games with `BCGAMES_DATA=.bcgames-ship`, one worker, no cache;
- every spec names its bots' jars (`classes_a`, `classes_b`), so a node plays exactly the classes built here.

Native games (the default where the native engine can play them, native.py) go in batches of their own: their binary
is built here, uploaded once to the volume (RUNTIME/bcx/<key>.gz; a binary unused for REMOTE_KEEP_DAYS is removed)
and named by each spec (`bcx`). A guarded game goes out twice: as itself, whose result the test uses, and as
`ID~guard` with `guard: true`, a task that plays it on both engines on one machine and returns only the guard's
verdict (play.Runner.guard). So a Java twin never holds up a cell; the run waits for the verdicts (guards_pending,
on_guard) before it concludes.

Results are read from runs/RUN/N/out/results.jsonl as the tasks end, so a test can stop before its batch does. A
result enters the local cache only when the node computed the same game key (same runner, JDK fingerprint, engine,
classes, map, seed).
"""
from __future__ import annotations

import gzip
import json
import os
import shutil
import subprocess
import sys
import threading
import time
from pathlib import Path

from . import native, play, store

SHIP = store.ROOT / ".bcgames-ship"
FLUSH_S = 30          # a batch every 30 s at most
POLL_S = 15           # reading finished tasks' results
CHUNK = 1             # games a task: one, so a batch ends with its longest game (a task starts in 2 s, median)
REMOTE_KEEP_DAYS = 3


def ship_binary(key: str, path: Path) -> str:
    """The binary's path on the volume, gzip'd (30 MB, not 100), uploaded when missing and touched when there, so it
    stays. A node unpacks it to its own disk once (native.local_binary): run from the volume, every task paid about
    70 s of page faults over NFS."""
    if str(store.ROOT) not in sys.path:
        sys.path.insert(0, str(store.ROOT))
    from tools import remote  # track C's login (ssh to a GPU-cluster login node; the volume is shared)
    d = f"{remote.RUNTIME}/bcx"
    dest = f"{d}/{key}.gz"
    if remote.login(f"test -s {dest} && touch {dest} && echo yes; true").strip() == "yes":
        return dest
    data = gzip.compress(path.read_bytes(), 1)
    remote.login(f'mkdir -p {d} && t=$(mktemp {dest}.XXXXXX) && cat > "$t" && mv -f "$t" {dest} && '
                 f'find {d} -maxdepth 1 -type f -mtime +{REMOTE_KEEP_DAYS} -delete', data, stream=2)
    return dest
    data = gzip.compress(path.read_bytes(), 1)
    remote.login(f'mkdir -p {d} && t=$(mktemp {dest}.XXXXXX) && gunzip -c > "$t" && chmod 700 "$t" && '
                 f'mv -f "$t" {dest} && find {d} -maxdepth 1 -type f -mtime +{REMOTE_KEEP_DAYS} -delete',
                 data, stream=2)
    return dest


class ImperialRunner:
    """Same interface as play.Runner: resolver, cached(job), submit(job, callback), close(wait), mismatches."""

    def __init__(self, outdir: Path, where: str | None, timeout: float, backend: str = "native",
                 guard: float | None = None, on_mismatch=None, use_cache: bool = True, on_guard=None):
        self.resolver = play.Resolver()
        self.use_cache = use_cache
        self.planner = native.Planner(self.resolver) if backend == "native" else None
        self.binaries: dict[native.Plan, dict] = {}  # plan -> {state: building|ready|failed, key, remote, error}
        self.guard_rate = native.GUARD_RATE if guard is None else guard
        self.guarded: set[str] = set()
        self.on_mismatch, self.on_guard = on_mismatch, on_guard
        self.guards_pending = 0
        self.mismatches: list[dict] = []
        self.cache = store.Cache()
        self.cache_lock = threading.Lock()
        self.outdir = outdir / "imperial"
        self.outdir.mkdir(parents=True, exist_ok=True)
        self.where, self.timeout = where, timeout
        self.pending: list[tuple[play.Job, object]] = []
        self.batches: list[dict] = []
        self.lock = threading.Lock()
        self.stopping = threading.Event()
        self.log = open(self.outdir / "imperial.log", "a")
        threading.Thread(target=self._flusher, daemon=True).start()

    def cached(self, job: play.Job) -> dict | None:
        if job.replay or not self.use_cache:  # a replay is asked for: play the game
            return None
        with self.cache_lock:
            hit = self.cache.get(job.key)
        return play.finish(job, hit, True) if hit else None

    def submit(self, job: play.Job, callback) -> None:
        plan = None
        if self.planner is not None:
            try:
                plan = self.planner.plan(job)
            except (store.SetupError, native.BuildError) as e:
                self._say(f"{job.spec.get('id')}: not native: {e}")
        job.plan = plan
        with self.lock:
            if plan is not None and plan not in self.binaries:
                self.binaries[plan] = {"state": "building"}
                threading.Thread(target=self._prepare, args=(plan,), daemon=True).start()
            self.pending.append((job, callback))

    def _prepare(self, plan: native.Plan) -> None:
        """Builds the plan's binary here and puts it on the volume."""
        try:
            key, path = native.build(plan)
            st = {"state": "ready", "key": key, "remote": ship_binary(key, path)}
            self._say(f"binary {key} on the volume: {st['remote']}")
        except Exception as e:  # noqa: BLE001 - its games go to Java with the reason
            st = {"state": "failed", "error": f"{type(e).__name__}: {str(e)[-1500:]}"}
            self._say(f"native binary failed, its games go to Java: {st['error']}")
        with self.lock:
            self.binaries[plan] = st

    def close(self, wait: bool = True) -> None:
        self.stopping.set()
        for b in self.batches:
            if b.get("proc") and b["proc"].poll() is None:
                b["proc"].kill()
            if b.get("run") and not b.get("ended"):
                subprocess.run([str(store.ROOT / "tools" / "compute"), "cancel", b["run"]], cwd=store.ROOT,
                               stdout=self.log, stderr=self.log)

    # ------------------------------------------------------------------------------------------------------ batches

    def _say(self, msg: str) -> None:
        self.log.write(f"{time.strftime('%H:%M:%S')} {msg}\n")
        self.log.flush()

    def _flusher(self) -> None:
        while not self.stopping.wait(FLUSH_S):
            java, nat, wait = [], [], []
            with self.lock:
                for job, cb in self.pending:
                    st = self.binaries.get(job.plan) if job.plan is not None else None
                    if st is None or st["state"] == "failed":
                        job.native_error = st["error"] if st else None
                        java.append((job, cb))
                    elif st["state"] == "ready":
                        nat.append((job, cb))
                    else:
                        wait.append((job, cb))
                self.pending = wait
            for batch, is_native in ((java, False), (nat, True)):
                if not batch:
                    continue
                try:
                    self._launch(batch, is_native)
                except Exception as e:  # noqa: BLE001 - every game of the batch fails with the reason
                    self._say(f"batch failed to start: {e}")
                    for job, cb in batch:
                        cb(play.finish(job, {"error": f"imperial: {e}"}, False))

    def _wants_guard(self, job: play.Job, key: str) -> bool:
        if self.guard_rate <= 0:
            return False
        first = key not in self.guarded
        self.guarded.add(key)
        return first or native.sampled(job.key, self.guard_rate)

    def _ship(self, jobs: list[play.Job]) -> dict[str, str]:
        """Copies what the nodes need into .bcgames-ship; returns jar path -> its path in the ship."""
        (SHIP / "engines").mkdir(parents=True, exist_ok=True)
        (SHIP / "bots").mkdir(exist_ok=True)
        for v in {j.spec["engine"] for j in jobs}:
            src = store.engine_jar(v)
            if not (SHIP / "engines" / src.name).exists():
                shutil.copy2(src, SHIP / "engines" / src.name)
        runner = self.resolver.runner_dir
        if not (SHIP / "runner" / runner.name).exists():
            shutil.copytree(runner, SHIP / "runner" / runner.name)
        jars = {}
        for jar, _ in self.resolver.bots.values():
            dest = SHIP / "bots" / Path(jar).name
            if not dest.exists():
                shutil.copy2(jar, dest)
            jars[str(jar)] = str(dest.relative_to(store.ROOT))
        return jars

    def _launch(self, batch: list[tuple[play.Job, object]], is_native: bool) -> None:
        jars = self._ship([j for j, _ in batch])
        n = len(self.batches)
        specs = self.outdir / f"batch-{n}.jsonl"
        guards = {}
        with open(specs, "w") as f:
            for job, _ in batch:
                spec = dict(job.spec)
                (a_jar, _), (b_jar, _) = self.resolver.bot(spec["a"]), self.resolver.bot(spec["b"])
                spec["classes_a"], spec["classes_b"] = jars[str(a_jar)], jars[str(b_jar)]
                if job.replay:
                    spec["replay"] = True  # the task writes it to OUT/replays/, it comes back with the results
                if is_native:
                    st = self.binaries[job.plan]
                    spec["bcx"] = st["remote"]
                    if self._wants_guard(job, st["key"]):
                        gid = f"{spec['id']}~guard"
                        guards[gid] = job
                        f.write(json.dumps(dict(spec, id=gid, guard=True, replay=False)) + "\n")
                f.write(json.dumps(spec) + "\n")
        cmd = [str(store.ROOT / "tools" / "run"), "--detach", "--each", str(specs), "--chunk",
               str(CHUNK), "--gather", "results.jsonl",
               "--with", str(SHIP.relative_to(store.ROOT))]
        if self.where:
            cmd += ["--where", self.where]
        cmd += ["--", "env", f"BCGAMES_DATA={SHIP.relative_to(store.ROOT)}", "BCGAMES_SLOTS=0", "tools/play",
                "{file}", "{out}", "--workers", "1", "--no-cache", "--timeout", str(int(self.timeout)),
                "--backend", "native" if is_native else "java", "--guard", "0"]
        proc = subprocess.run(cmd, cwd=store.ROOT, capture_output=True, text=True)
        self.log.write(proc.stderr)
        lines = proc.stdout.split()
        if proc.returncode != 0 or not lines:
            raise store.SetupError(f"tools/run exit {proc.returncode}: {proc.stderr.strip()[-500:]}")
        run = lines[0]
        b = {"run": run, "jobs": {j.spec["id"]: (j, cb) for j, cb in batch}, "guards": guards, "done": set(),
             "ended": False}
        with self.lock:
            self.guards_pending += len(guards)
        b["proc"] = subprocess.Popen([str(store.ROOT / "tools" / "compute"), "wait", run], cwd=store.ROOT,
                                     stdout=self.log, stderr=self.log)
        self.batches.append(b)
        self._say(f"batch {n}: {len(batch)} {'native' if is_native else 'java'} games as run {run}")
        threading.Thread(target=self._follow, args=(b,), daemon=True).start()

    def _follow(self, b: dict) -> None:
        local = store.ROOT / "runs" / b["run"]
        while not self.stopping.is_set():
            ended = b["proc"].poll() is not None
            for f in sorted(local.glob("*/out/results.jsonl")):
                for line in f.read_text().splitlines():
                    self._deliver(b, json.loads(line))
            if ended:
                break
            self.stopping.wait(POLL_S)
        b["ended"] = True
        for gid, (job, cb) in b["jobs"].items():
            if gid not in b["done"] and not self.stopping.is_set():
                b["done"].add(gid)
                cb(play.finish(job, {"error": f"imperial run {b['run']}: no result (runs/{b['run']}/*/log)"}, False))
        for gid in b["guards"]:
            if gid not in b["done"] and not self.stopping.is_set():
                self._deliver(b, {"id": gid, "guard": {"verdict": "java-error", "error": "no result"}})

    def _guard_result(self, b: dict, gid: str, r: dict) -> None:
        """A guard task's verdict (its game's result came from its own task)."""
        job = b["guards"][gid]
        g = r.get("guard") if isinstance(r.get("guard"), dict) else {
            "verdict": "java-error", "error": r.get("error") or "the guard task played no Java game"}
        g = dict(g, remote=b["run"])
        if g.get("verdict") == "mismatch":
            self._mismatch(job, g)
        with self.lock:
            self.guards_pending -= 1
        if self.on_guard:
            self.on_guard(job, g)

    def _deliver(self, b: dict, r: dict) -> None:
        gid = r.get("id")
        if gid in b["guards"] and gid not in b["done"]:
            b["done"].add(gid)
            self._guard_result(b, gid, r)
            return
        if gid not in b["jobs"] or gid in b["done"]:
            return
        b["done"].add(gid)
        job, cb = b["jobs"][gid]
        res = {k: v for k, v in r.items() if k not in job.spec and k not in (
            "key", "runner", "cached", "a_hash", "b_hash", "map_hash", "classes_a", "classes_b", "replay", "bcx")}
        if not isinstance(res.get("guard"), dict):
            res.pop("guard", None)
        if getattr(job, "native_error", None) and not res.get("native_error"):
            res["native_error"] = job.native_error
        res["remote"] = b["run"]
        res["replay"] = None
        if job.replay and r.get("error") is None:
            found = sorted((store.ROOT / "runs" / b["run"]).glob(f"*/out/replays/{play.safe(gid)}.bc26"))
            if found:
                Path(job.replay).parent.mkdir(parents=True, exist_ok=True)
                shutil.move(str(found[0]), job.replay)
                res["replay"] = job.replay
        guard = res.get("guard") or {}
        if guard.get("verdict") == "mismatch":
            self._mismatch(job, guard)
        elif r.get("error") is None:
            if r.get("key") == job.key:
                keep = {k: v for k, v in res.items() if k not in play.NOT_CACHED + ("remote",)}
                with self.cache_lock:
                    self.cache.put(job.key, keep)
            else:
                res["key_mismatch"] = r.get("key")
                self._say(f"{gid}: node key {r.get('key')} != local {job.key} (runner {r.get('runner')}); not cached")
        cb(play.finish(job, res, False))

    def _mismatch(self, job: play.Job, g: dict) -> None:
        """As play.Runner.mismatch: the binary is never used again, its results leave the cache."""
        key = g.get("native_build")
        if key:
            native.mark_bad(key, f"game {job.spec.get('id')} ({job.key}) on {g.get('host')}: {json.dumps(g)}")
            with self.cache_lock:
                g["purged"] = self.cache.purge_native(key)
        self.mismatches.append(dict(g, id=job.spec.get("id"), key=job.key))
        self._say(f"EXACTNESS GUARD: game {job.spec.get('id')} differs between the engines: {json.dumps(g)}")
        if self.on_mismatch:
            self.on_mismatch(job, g)
