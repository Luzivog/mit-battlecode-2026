"""The Imperial backend of tools/games: plays jobs on DoC's machines through track C's tools/run (the compute skill,
.claude/skills/compute/references/internals.md "The games interface").

Each batch of jobs becomes a spec file and one `tools/run --each SPECS --chunk K -- tools/play {file} {out}`:
- it ships `.bcgames-ship/` (git-ignored): the engine jar, the bot jars this run plays and the compiled runner;
- each task plays its K games with `BCGAMES_DATA=.bcgames-ship`, one worker, no cache;
- every spec names its bots' jars (`classes_a`, `classes_b`), so a node plays exactly the classes built here.

Results are read from runs/RUN/N/out/results.jsonl as the tasks end, so a test can stop before its batch does. A
result enters the local cache only when the node computed the same game key (same runner, JDK fingerprint, engine,
classes, map, seed).
"""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import threading
import time
from pathlib import Path

from . import play, store

SHIP = store.ROOT / ".bcgames-ship"
FLUSH_S = 30          # a batch every 30 s at most
POLL_S = 15           # reading finished tasks' results
CHUNK = 3             # games a task: a few minutes on DoC, so a lost machine costs little


class ImperialRunner:
    """Same interface as play.Runner: resolver, cached(job), submit(job, callback), close(wait)."""

    def __init__(self, outdir: Path, where: str | None, timeout: float):
        self.resolver = play.Resolver()
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
        with self.cache_lock:
            hit = self.cache.get(job.key)
        return play.finish(job, hit, True) if hit else None

    def submit(self, job: play.Job, callback) -> None:
        with self.lock:
            self.pending.append((job, callback))

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
            with self.lock:
                batch, self.pending = self.pending, []
            if batch:
                try:
                    self._launch(batch)
                except Exception as e:  # noqa: BLE001 - every game of the batch fails with the reason
                    self._say(f"batch failed to start: {e}")
                    for job, cb in batch:
                        cb(play.finish(job, {"error": f"imperial: {e}"}, False))

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

    def _launch(self, batch: list[tuple[play.Job, object]]) -> None:
        jars = self._ship([j for j, _ in batch])
        n = len(self.batches)
        specs = self.outdir / f"batch-{n}.jsonl"
        with open(specs, "w") as f:
            for job, _ in batch:
                spec = dict(job.spec)
                (a_jar, _), (b_jar, _) = self.resolver.bot(spec["a"]), self.resolver.bot(spec["b"])
                spec["classes_a"], spec["classes_b"] = jars[str(a_jar)], jars[str(b_jar)]
                f.write(json.dumps(spec) + "\n")
        cmd = [str(store.ROOT / "tools" / "run"), "--detach", "--each", str(specs), "--chunk", str(CHUNK),
               "--gather", "results.jsonl", "--with", str(SHIP.relative_to(store.ROOT))]
        if self.where:
            cmd += ["--where", self.where]
        cmd += ["--", "env", f"BCGAMES_DATA={SHIP.relative_to(store.ROOT)}", "BCGAMES_SLOTS=0", "tools/play",
                "{file}", "{out}", "--workers", "1", "--no-cache", "--timeout", str(int(self.timeout))]
        proc = subprocess.run(cmd, cwd=store.ROOT, capture_output=True, text=True)
        self.log.write(proc.stderr)
        lines = proc.stdout.split()
        if proc.returncode != 0 or not lines:
            raise store.SetupError(f"tools/run exit {proc.returncode}: {proc.stderr.strip()[-500:]}")
        run = lines[0]
        b = {"run": run, "jobs": {j.spec["id"]: (j, cb) for j, cb in batch}, "done": set(), "ended": False}
        b["proc"] = subprocess.Popen([str(store.ROOT / "tools" / "compute"), "wait", run], cwd=store.ROOT,
                                     stdout=self.log, stderr=self.log)
        self.batches.append(b)
        self._say(f"batch {n}: {len(batch)} games as run {run}")
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

    def _deliver(self, b: dict, r: dict) -> None:
        gid = r.get("id")
        if gid not in b["jobs"] or gid in b["done"]:
            return
        b["done"].add(gid)
        job, cb = b["jobs"][gid]
        res = {k: v for k, v in r.items() if k not in job.spec and k not in (
            "key", "runner", "cached", "a_hash", "b_hash", "map_hash", "backend", "classes_a", "classes_b")}
        res["remote"] = b["run"]
        if r.get("error") is None:
            if r.get("key") == job.key:
                keep = {k: v for k, v in res.items() if k not in ("replay", "remote")}
                with self.cache_lock:
                    self.cache.put(job.key, keep)
            else:
                res["key_mismatch"] = r.get("key")
                self._say(f"{gid}: node key {r.get('key')} != local {job.key} (runner {r.get('runner')}); not cached")
        cb(play.finish(job, res, False))
