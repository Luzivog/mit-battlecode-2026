"""tools/play: play games from a JSONL spec file with the unmodified engine, many games per JVM, N JVMs.

Spec (one JSON object per line):  {"id": "g1", "a": "challenger", "b": "awubot", "map": "DefaultSmall",
                                    "seed": 158, "engine": "1.2.6"}
  a, b    bots as team A and team B: a package under src/, or pkg@REV for a git revision of this repo
  seed    optional, default: the map's embedded seed (= the official game); any other seed is a map variant
  engine  optional, default 1.2.6 (official); 1.2.5 is the pinned dev engine
Result (one JSON object per line, in completion order): the spec's fields plus winner ("A"/"B"), wintype, rounds,
wall_ms, digest (sha256 of the replay bytes), per team {cheese_transferred, cat_damage, rats, kings, cheese, frozen},
engine_errors, cat_rng_draws, key, runner, cached, error, replay.
"""
from __future__ import annotations

import argparse
import json
import os
import queue
import subprocess
import sys
import threading
import time
from dataclasses import dataclass, field
from pathlib import Path

from . import store

# JVM flags, measured (docs/testing.md "Runner"). None of them changes a game: replays are byte-identical.
JVM_FLAGS = [
    "-Xmx1g", "-XX:+UseSerialGC",
    "-XX:+UnlockDiagnosticVMOptions", "-XX:-BytecodeVerificationRemote",  # skip verifying each robot's classes
]
OPENS = [f"--add-opens=java.base/{p}=ALL-UNNAMED" for p in
         ("jdk.internal.misc", "jdk.internal.math", "jdk.internal.util", "jdk.internal.access", "sun.security.action")]
PROPS = {
    "bc.server.websocket": "false", "bc.server.wait-for-client": "false", "bc.server.mode": "headless",
    "bc.server.robot-player-to-system-out": "false", "bc.server.debug": "false",
    "bc.engine.debug-methods": "false", "bc.engine.enable-profiler": "false",
    "bc.server.validate-maps": "true",
}
DEFAULT_TIMEOUT = 1800  # seconds per game; the slowest official games take ~150 s alone


@dataclass
class Job:
    spec: dict
    key: str
    line: str = ""          # the line sent to bcplay.Play
    replay: str = ""        # replay path ("" = none)
    meta: dict = field(default_factory=dict)


class Resolver:
    """Turns specs into jobs: builds bots and map variants once, computes cache keys."""

    def __init__(self):
        self.runner_dir, self.runner = store.runner_classes()
        self.bots: dict[str, tuple[Path, str]] = {}
        self.maps: dict[str, str] = {}
        self.lock = threading.Lock()

    def bot(self, ref: str) -> tuple[Path, str]:
        with self.lock:
            if ref not in self.bots:
                self.bots[ref] = store.bot_jar(ref)
            return self.bots[ref]

    def job(self, spec: dict, replay_dir: Path | None = None) -> Job:
        for k in ("id", "a", "b", "map"):
            if k not in spec:
                raise store.SetupError(f"spec {spec!r} has no {k!r}")
        engine = spec.setdefault("engine", store.DEFAULT_ENGINE)
        if engine not in store.ENGINES:
            raise store.SetupError(f"spec {spec['id']}: unknown engine {engine!r}")
        name = spec["map"]
        if name not in self.maps:
            self.maps[name] = store.map_hash(name)
        if spec.get("seed") is None:
            spec["seed"] = store.embedded_seed(name)
        seed = int(spec["seed"])
        (a_jar, a_hash), (b_jar, b_hash) = self.bot(spec["a"]), self.bot(spec["b"])
        a_pkg, b_pkg = store.parse_bot(spec["a"])[0], store.parse_bot(spec["b"])[0]
        key = store.game_key(self.runner, engine, a_hash, b_hash, a_pkg, b_pkg, name, self.maps[name], seed)
        replay = str(replay_dir / f"{safe(spec['id'])}.bc26") if replay_dir else ""
        line = "\t".join([str(spec["id"]), a_pkg, str(a_jar), b_pkg, str(b_jar), name,
                          str(store.map_variant(name, seed)), replay])
        meta = {"a_hash": a_hash, "b_hash": b_hash, "map_hash": self.maps[name], "key": key, "runner": self.runner}
        return Job(spec=spec, key=key, line=line, replay=replay, meta=meta)


def safe(s: str) -> str:
    return "".join(c if c.isalnum() or c in "-_." else "_" for c in str(s))


class Worker:
    """One JVM running bcplay.Play; restarted after a timeout, a crash or an engine error."""

    def __init__(self, engine: str, runner_dir: Path, log: Path):
        self.cmd = (["java", *JVM_FLAGS, *OPENS, *[f"-D{k}={v}" for k, v in PROPS.items()],
                     "-cp", f"{store.engine_jar(engine)}{os.pathsep}{runner_dir}", "bcplay.Play"])
        self.log_path = log
        self.proc: subprocess.Popen | None = None
        self.lines: queue.Queue = queue.Queue()

    def start(self) -> None:
        self.log = open(self.log_path, "ab")
        self.proc = subprocess.Popen(self.cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=self.log,
                                     text=True, bufsize=1)
        lines = self.lines = queue.Queue()
        proc = self.proc
        threading.Thread(target=lambda: [lines.put(l) for l in proc.stdout] + [lines.put(None)], daemon=True).start()
        first = lines.get(timeout=120)
        if first is None or "ready" not in first:
            raise store.SetupError(f"game JVM did not start, see {self.log_path}")

    def stop(self) -> None:
        if self.proc and self.proc.poll() is None:
            self.proc.kill()
            self.proc.wait()
        self.proc = None
        if getattr(self, "log", None):
            self.log.close()

    def play(self, job: Job, timeout: float) -> dict:
        if self.proc is None or self.proc.poll() is not None:
            self.start()
        try:
            self.proc.stdin.write(job.line + "\n")
            self.proc.stdin.flush()
            out = self.lines.get(timeout=timeout)
        except queue.Empty:
            self.stop()
            return {"error": f"timeout after {timeout:.0f} s"}
        except (BrokenPipeError, OSError) as e:
            self.stop()
            return {"error": f"game JVM died: {e}"}
        if out is None:
            self.stop()
            return {"error": f"game JVM died, see {self.log_path}"}
        res = json.loads(out)
        if res.get("error"):
            self.stop()  # an engine exception may leave robot threads behind: start clean
        return res


class Pool:
    """N workers per engine pulling jobs from one queue. on_result(job, result) is called from worker threads."""

    def __init__(self, workers: int, logdir: Path, runner_dir: Path, timeout: float = DEFAULT_TIMEOUT):
        self.n, self.logdir, self.runner_dir, self.timeout = workers, logdir, runner_dir, timeout
        self.queues: dict[str, queue.Queue] = {}
        self.threads: list[threading.Thread] = []
        self.workers: list[Worker] = []
        self.closed = False
        logdir.mkdir(parents=True, exist_ok=True)

    def submit(self, job: Job, on_result) -> None:
        engine = job.spec["engine"]
        if engine not in self.queues:
            self.queues[engine] = queue.Queue()
            for i in range(self.n):
                w = Worker(engine, self.runner_dir, self.logdir / f"jvm-{engine}-{i}.log")
                self.workers.append(w)
                t = threading.Thread(target=self._loop, args=(w, self.queues[engine]), daemon=True)
                t.start()
                self.threads.append(t)
        self.queues[engine].put((job, on_result))

    def _loop(self, w: Worker, q: queue.Queue) -> None:
        while True:
            item = q.get()
            if item is None or self.closed:
                break
            job, on_result = item
            try:
                res = w.play(job, self.timeout)
            except Exception as e:  # noqa: BLE001 - reported as the game's error
                w.stop()
                res = {"error": f"runner: {e}"}
            if not self.closed:
                on_result(job, res)
        w.stop()

    def close(self, wait: bool = True) -> None:
        if not wait:
            self.closed = True
            for w in self.workers:
                w.stop()
        for q in self.queues.values():
            for _ in range(self.n):
                q.put(None)
        if wait:
            for t in self.threads:
                t.join()


def finish(job: Job, res: dict, cached: bool) -> dict:
    """The result line: the spec, then the outcome, then provenance."""
    out = dict(job.spec)
    out.update({k: v for k, v in res.items() if k != "id"})
    out.update(job.meta)
    out["cached"] = cached
    out.setdefault("error", None)
    out.setdefault("replay", None)
    return out


class Runner:
    """Plays jobs through the cache and a pool. Results go to callbacks; finished games go to the cache."""

    def __init__(self, workers: int, outdir: Path, timeout: float = DEFAULT_TIMEOUT, use_cache: bool = True):
        self.resolver = Resolver()
        self.cache = store.Cache() if use_cache else None
        self.cache_lock = threading.Lock()
        self.pool = Pool(workers, outdir / "logs", self.resolver.runner_dir, timeout)

    def cached(self, job: Job) -> dict | None:
        if self.cache is None or job.replay:
            return None
        with self.cache_lock:
            hit = self.cache.get(job.key)
        return finish(job, hit, True) if hit else None

    def submit(self, job: Job, callback) -> None:
        def done(job: Job, res: dict) -> None:
            if job.replay and res.get("error") is None:
                res["replay"] = job.replay
            if self.cache is not None and res.get("error") is None:
                keep = {k: v for k, v in res.items() if k not in ("id", "replay")}
                with self.cache_lock:
                    self.cache.put(job.key, keep)
            callback(finish(job, res, False))
        self.pool.submit(job, done)

    def close(self, wait: bool = True) -> None:
        self.pool.close(wait)


def describe(r: dict) -> str:
    if r.get("error"):
        return f"{r['id']}: ERROR {r['error']}"
    win = r["a"] if r["winner"] == "A" else r["b"]
    src = "cache" if r["cached"] else f"{r['wall_ms'] / 1000:.1f}s"
    return (f"{r['id']}: {r['map']} s{r['seed']} {r['a']}(A) vs {r['b']}(B) -> {win} "
            f"{r['wintype']} r{r['rounds']} [{src}]")


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(prog="tools/play", description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("specs", help="JSONL spec file ('-' = stdin)")
    ap.add_argument("outdir", help="writes results.jsonl, logs/ and replays/ here")
    ap.add_argument("--workers", type=int, default=8, help="parallel JVMs (default 8: the shared-box cap)")
    ap.add_argument("--engine", choices=sorted(store.ENGINES), help="engine for specs that don't name one")
    ap.add_argument("--replays", choices=["none", "losses", "all"], default="none",
                    help="write replays (losses = games team B won: put the bot you study as A)")
    ap.add_argument("--timeout", type=float, default=DEFAULT_TIMEOUT, help="seconds per game")
    ap.add_argument("--no-cache", action="store_true", help="play every game even if its result is cached")
    args = ap.parse_args(argv)

    t0 = time.time()
    lines = sys.stdin if args.specs == "-" else open(args.specs)
    specs = [json.loads(l) for l in lines if l.strip()]
    outdir = Path(args.outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    replay_dir = outdir / "replays" if args.replays != "none" else None
    if replay_dir:
        replay_dir.mkdir(exist_ok=True)
    try:
        runner = Runner(args.workers, outdir, args.timeout, use_cache=not args.no_cache)
        jobs = []
        for s in specs:
            if args.engine and "engine" not in s:
                s["engine"] = args.engine
            jobs.append(runner.resolver.job(s, replay_dir))
    except store.SetupError as e:
        print(f"tools/play: {e}", file=sys.stderr)
        return 1

    out = open(outdir / "results.jsonl", "a")
    lock = threading.Lock()
    done = threading.Event()
    counts = {"played": 0, "cached": 0, "errors": 0, "left": len(jobs)}

    def record(r: dict) -> None:
        with lock:
            if r.get("replay") and args.replays == "losses" and r.get("winner") != "B":
                os.unlink(r["replay"])
                r["replay"] = None
            out.write(json.dumps(r) + "\n")
            out.flush()
            counts["errors" if r.get("error") else "cached" if r["cached"] else "played"] += 1
            counts["left"] -= 1
            n = len(jobs) - counts["left"]
            print(f"[{n}/{len(jobs)}] {describe(r)}", file=sys.stderr, flush=True)
            if counts["left"] == 0:
                done.set()

    for j in jobs:
        hit = runner.cached(j)
        if hit:
            record(hit)
        else:
            runner.submit(j, record)
    if jobs:
        done.wait()
    runner.close()
    wall = time.time() - t0
    rate = counts["played"] / wall * 60 if wall > 0 else 0
    print(f"tools/play: {counts['played']} played, {counts['cached']} cached, {counts['errors']} errors in "
          f"{wall:.0f} s ({rate:.1f} games/min, {args.workers} workers) -> {outdir / 'results.jsonl'}",
          file=sys.stderr)
    return 2 if counts["errors"] else 0
