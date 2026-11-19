"""tools/play: play games from a JSONL spec file: on the native engine (engine/, the default) where it can play the
game, else on the unmodified official engine (many games per JVM, N JVMs). Both play the same games: the native engine
reproduces the official one exactly, and an exactness guard plays a sample of the native games on both and stops at
any difference the known machine effects don't explain (docs/evaluation.md "Backends").

Spec (one JSON object per line; the fields of engine/AGENTS.md):
  {"id": "g1", "a": "challenger", "b": "awubot", "map": "DefaultSmall", "seed": 158, "engine": "1.2.6"}
  a, b        team A's and team B's bot: a package under src/ (compiled alone, like a submission), or pkg@REV
              for that package at a git revision of this repo
  classes, classes_a, classes_b
              optional: play prebuilt classes (a directory or jar holding <pkg>/...) instead of compiling src/
  seed        optional, default: the map's embedded seed (= the official game); another seed is a map variant
  replay      optional: true writes this game's replay to OUTDIR/replays/ (as --replays all does for every game)
  engine      optional, default 1.2.6 (official); 1.2.5 is the pinned dev engine
  guard       optional: true plays the game on both engines and compares them (the exactness guard)
  bcx         optional: the native binary to play it with (remote tasks; else one is built for the game's bots)
  other fields are copied to the result
Result (one JSON object per line, in completion order): the spec's fields plus winner ("A"/"B"), winner_pkg,
win_type, round, wall_ms, cpu_s, digest (sha256 of the replay bytes), per team A/B {cheese_transferred, cat_damage,
rats, kings, cheese, crashed}, cat_rng_draws, series (each team's [cheese_transferred, rats, kings, cat_damage] every
100 rounds and at the end), backend ("native" or "java"), key, runner, cached, error, replay; Java games add
engine_errors, native games hash (the rounds' state hash chain) and native_build (the binary), a guarded game guard
({verdict, digests, ...}), a game the native engine failed and Java played native_error.
"crashed": robots the engine destroyed because their code threw (an uncaught Error) or returned from run(). The Java
runner attributes a thrown crash by the package of the trace's first frame, so in a mirror (the same package on both
sides) every thrown crash counts for A. The native engine counts per robot and has no such limit.
"""
from __future__ import annotations

import argparse
import fcntl
import json
import os
import queue
import random
import socket
import subprocess
import sys
import threading
import time
from dataclasses import dataclass, field
from pathlib import Path

from . import native, store

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
GIVE_BACK = 75  # tools/play --give-back's exit when this host can't write its games (tools/piece.py GIVE_BACK)
DEFAULT_TIMEOUT = 1800  # seconds per game; the slowest official games take ~150 s alone
IDLE_S = 60  # a game JVM idle this long exits (a fresh JVM plays the same games)


class Slots:
    """A machine-wide cap on games played at once, shared by every tools/play and tools/games process of this host
    (lock files under the data dir, one per slot). BCGAMES_SLOTS sets the cap (default 8, this shared box's share);
    0 turns it off."""

    def __init__(self, n: int | None = None):
        self.n = int(os.environ.get("BCGAMES_SLOTS", "8")) if n is None else n
        self.dir = store.DATA / "slots" / socket.gethostname()
        if self.n:
            self.dir.mkdir(parents=True, exist_ok=True)

    def acquire(self, stop: threading.Event):
        if not self.n:
            return None
        while not stop.is_set():
            for i in random.sample(range(self.n), self.n):
                f = open(self.dir / f"slot-{i}", "w")
                try:
                    fcntl.flock(f, fcntl.LOCK_EX | fcntl.LOCK_NB)
                    return f
                except OSError:
                    f.close()
            stop.wait(0.5)
        return None

    @staticmethod
    def release(f) -> None:
        if f is not None:
            fcntl.flock(f, fcntl.LOCK_UN)
            f.close()


class Gate:
    """What a game takes before it starts, whichever engine plays it: one of the runner's N workers and one of the
    host's slots."""

    def __init__(self, n: int, slots: Slots):
        self.sem = threading.Semaphore(n)
        self.slots = slots
        self.stopping = threading.Event()

    def acquire(self):
        self.sem.acquire()
        return self.slots.acquire(self.stopping)

    def release(self, token) -> None:
        Slots.release(token)
        self.sem.release()


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
        self.runner_dir, runner = store.runner_classes()
        self.runner = f"{runner}/{store.jdk_fingerprint()}"
        self.bots: dict[str, tuple[Path, str]] = {}
        self.maps: dict[str, str] = {}
        self.lock = threading.Lock()

    def bot(self, ref: str, classes: str | None = None) -> tuple[Path, str]:
        key = ref if classes is None else f"{ref}|{classes}"
        with self.lock:
            if key not in self.bots:
                self.bots[key] = (store.bot_jar(ref) if classes is None else
                                  store.bot_classes(store.parse_bot(ref)[0], classes))
            return self.bots[key]

    def job(self, spec: dict, replay: Path | None = None) -> Job:
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
        a_jar, a_hash = self.bot(spec["a"], spec.get("classes_a", spec.get("classes")))
        b_jar, b_hash = self.bot(spec["b"], spec.get("classes_b", spec.get("classes")))
        a_pkg, b_pkg = store.parse_bot(spec["a"])[0], store.parse_bot(spec["b"])[0]
        key = store.game_key(self.runner, engine, a_hash, b_hash, a_pkg, b_pkg, name, self.maps[name], seed)
        line = "\t".join([str(spec["id"]), a_pkg, str(a_jar), b_pkg, str(b_jar), name,
                          str(store.map_variant(name, seed)), str(replay or "")])
        meta = {"a_hash": a_hash, "b_hash": b_hash, "map_hash": self.maps[name], "key": key, "runner": self.runner}
        return Job(spec=spec, key=key, line=line, replay=str(replay or ""), meta=meta)


def safe(s: str) -> str:
    return "".join(c if c.isalnum() or c in "-_." else "_" for c in str(s))


class Worker:
    """One JVM running bcplay.Play; restarted after a timeout, a crash or an engine error."""

    def __init__(self, engine: str, runner_dir: Path, log: Path):
        flags = os.environ["BCGAMES_JVM"].split() if os.environ.get("BCGAMES_JVM") else JVM_FLAGS
        self.cmd = (["java", *flags, *OPENS, *[f"-D{k}={v}" for k, v in PROPS.items()],
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
    """N JVM workers per engine pulling jobs from one queue, each game through the gate. on_result(job, result) is
    called from worker threads."""

    def __init__(self, workers: int, logdir: Path, runner_dir: Path, timeout: float = DEFAULT_TIMEOUT,
                 slots: bool = True, gate: Gate | None = None):
        self.n, self.logdir, self.runner_dir, self.timeout = workers, logdir, runner_dir, timeout
        self.queues: dict[str, queue.Queue] = {}
        self.threads: list[threading.Thread] = []
        self.workers: list[Worker] = []
        self.closed = False
        self.gate = gate or Gate(workers, Slots() if slots else Slots(0))
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
            try:
                item = q.get(timeout=IDLE_S)
            except queue.Empty:  # a native run's Java pool is mostly idle: don't keep its JVMs
                w.stop()
                continue
            if item is None or self.closed:
                break
            job, on_result = item
            slot = self.gate.acquire()
            if self.closed:
                self.gate.release(slot)
                break
            try:
                res = w.play(job, self.timeout)
            except Exception as e:  # noqa: BLE001 - reported as the game's error
                w.stop()
                res = {"error": f"runner: {e}"}
            finally:
                self.gate.release(slot)
            if not res.get("error"):
                res["backend"] = "java"
            if not self.closed:
                on_result(job, res)
        w.stop()

    def close(self, wait: bool = True) -> None:
        if not wait:
            self.closed = True
            self.gate.stopping.set()
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
    out.setdefault("backend", "java")  # results cached before the native backend are the Java engine's
    out.setdefault("error", None)
    out.setdefault("replay", None)
    return out


# Not part of a game's cached result: where this run put it, and this run's own guard check.
NOT_CACHED = ("id", "replay", "guard")


class Runner:
    """Plays jobs through the cache, the native engine where it can play them (backend "native") and the Java pool.
    Results go to callbacks; finished games go to the cache, whichever engine played them (they are the same games).

    The exactness guard plays a native game on Java too when its spec asks (guard: true), when it is the runner's first
    game of its binary, or when sampled (GUARD rate, by game key): see guard(). On a mismatch the Java engine's own
    nondeterminism doesn't explain (native.verdict), the binary is marked bad, its results leave the cache and
    on_mismatch(job, guard) is called."""

    def __init__(self, workers: int, outdir: Path, timeout: float = DEFAULT_TIMEOUT, use_cache: bool = True,
                 backend: str = "native", guard: float | None = None, on_mismatch=None, slots: bool = True,
                 on_guard=None, no_room=None):
        """NO_ROOM(job, error): a native failure for want of disk where games write (native.no_room) goes there,
        not to Java (remote tasks: tools/play --give-back)."""
        self.no_room = no_room
        self.outdir = outdir
        self.resolver = Resolver()
        self.cache = store.Cache() if use_cache else None
        self.cache_lock = threading.Lock()
        self.gate = Gate(workers, Slots() if slots else Slots(0))
        self.pool = Pool(workers, outdir / "logs", self.resolver.runner_dir, timeout, gate=self.gate)
        self.native = (native.Backend(self.resolver, workers, outdir / "logs" / "native", timeout, self.gate)
                       if backend == "native" else None)
        self.guard_rate = native.GUARD_RATE if guard is None else guard
        self.on_mismatch, self.on_guard = on_mismatch, on_guard
        self.guards_pending = 0        # guard checks not yet resolved (on_guard mode)
        self.first: set[str] = set()   # binaries this runner has guarded a game of
        self.mismatches: list[dict] = []
        self.lock = threading.Lock()

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
            if (self.cache is not None and res.get("error") is None
                    and (res.get("guard") or {}).get("verdict") != "mismatch"):
                keep = {k: v for k, v in res.items() if k not in NOT_CACHED}
                with self.cache_lock:
                    self.cache.put(job.key, keep)
            callback(finish(job, res, False))

        def native_done(job: Job, res: dict) -> None:
            if res.get("error"):  # the native engine couldn't play it: the Java engine does
                err = res["error"]
                if self.no_room is not None and native.no_room(err, self.outdir):
                    self.no_room(job, err)
                    return
                self.pool.submit(job, lambda j, r: done(j, dict(r, native_error=err)))
            elif self.wants_guard(job, res):
                self.guard(job, res, done)
            else:
                done(job, res)

        if self.native is None or not self.native.submit(job, native_done):
            self.pool.submit(job, done)

    # -------------------------------------------------------------------------------------------- exactness guard

    def wants_guard(self, job: Job, res: dict) -> bool:
        if job.spec.get("guard"):
            return True
        if self.guard_rate <= 0:
            return False
        with self.lock:
            first = res["native_build"] not in self.first
            self.first.add(res["native_build"])
        return first or native.sampled(job.key, self.guard_rate)

    def guard(self, job: Job, nres: dict, done) -> None:
        """Plays JOB on Java too (twice if the first run differs). Without on_guard the outcome is delivered once
        compared, with its verdict. With on_guard the native outcome is delivered now (guard verdict "pending") and
        on_guard(job, guard) gets the verdict later; the caller must not conclude while guards_pending is above 0."""
        twin = self.resolver.job(dict(job.spec), None)
        mirror = store.parse_bot(job.spec["a"])[0] == store.parse_bot(job.spec["b"])[0]
        g = {"native_digest": nres.get("digest"), "native_build": nres.get("native_build"),
             "host": socket.gethostname()}
        if self.on_guard is not None:
            with self.lock:
                self.guards_pending += 1
            done(job, dict(nres, guard=dict(g, verdict="pending")))

        def resolve(res: dict) -> None:
            if g["verdict"] == "mismatch":
                self.mismatch(job, g)
            if self.on_guard is None:
                done(job, res)
                return
            with self.lock:
                self.guards_pending -= 1
            self.on_guard(job, g)

        def java1(_, r1: dict) -> None:
            if r1.get("error"):  # nothing to compare with: the native result stands, unchecked
                g.update(verdict="java-error", error=r1["error"])
                resolve(dict(nres, guard=g))
                return
            g.update(java_digest=r1.get("digest"), java_cpu_s=r1.get("cpu_s"))
            if native.verdict(nres, r1, mirror=mirror) == "same":
                g["verdict"] = "same"
                resolve(dict(nres, guard=g))
            else:
                self.pool.submit(twin, lambda _, r2: java2(r1, r2))

        def java2(r1: dict, r2: dict) -> None:
            g.update(java2_digest=r2.get("digest"), java2_error=r2.get("error"))
            v = native.verdict(nres, r1, None if r2.get("error") else r2, mirror)
            g["verdict"] = "java-nondeterministic" if v == "java-nondeterministic" else "mismatch"
            resolve(dict(nres, guard=g) if g["verdict"] != "mismatch" else dict(r1, backend="java", guard=g))

        self.pool.submit(twin, java1)

    def mismatch(self, job: Job, g: dict) -> None:
        """A native game the Java engine doesn't play: its binary is never used again and its results leave the
        cache."""
        key = g.get("native_build")
        if key:
            native.mark_bad(key, f"game {job.spec.get('id')} ({job.key}): {json.dumps(g)}")
            if self.cache is not None:
                with self.cache_lock:
                    g["purged"] = self.cache.purge_native(key)
        with self.lock:
            self.mismatches.append(dict(g, id=job.spec.get("id"), key=job.key))
        print(f"tools/play: EXACTNESS GUARD: game {job.spec.get('id')} differs between the engines: {json.dumps(g)}",
              file=sys.stderr, flush=True)
        if self.on_mismatch:
            self.on_mismatch(job, g)

    def close(self, wait: bool = True) -> None:
        if self.native is not None:
            self.native.close()
        self.pool.close(wait)


def describe(r: dict) -> str:
    if r.get("error"):
        return f"{r['id']}: ERROR {r['error']}"
    win = r["a"] if r["winner"] == "A" else r["b"]
    src = "cache" if r["cached"] else f"{r['wall_ms'] / 1000:.1f}s {r.get('backend', 'java')}"
    if r.get("guard"):
        src += f", guard {r['guard']['verdict']}"
    return (f"{r['id']}: {r['map']} s{r['seed']} {r['a']}(A) vs {r['b']}(B) -> {win} "
            f"{r['win_type']} r{r['round']} [{src}]")


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(prog="tools/play", description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("specs", help="JSONL spec file ('-' = stdin)")
    ap.add_argument("outdir", help="writes results.jsonl, logs/ and replays/ here")
    ap.add_argument("--workers", type=int, default=8,
                    help="parallel JVMs (default 8); all runs on this host share BCGAMES_SLOTS games at once (8)")
    ap.add_argument("--engine", choices=sorted(store.ENGINES), help="engine for specs that don't name one")
    ap.add_argument("--replays", choices=["none", "losses", "all"], default="none",
                    help="write replays (losses = games team B won: put the bot you study as A)")
    ap.add_argument("--timeout", type=float, default=DEFAULT_TIMEOUT, help="seconds per game")
    ap.add_argument("--no-cache", action="store_true", help="play every game even if its result is cached")
    ap.add_argument("--backend", choices=["native", "java"], default="native",
                    help="native (default): the native engine where it can play the game, else Java; java: Java only")
    ap.add_argument("--guard", type=float, default=None,
                    help=f"share of native games also played on Java and compared (default {native.GUARD_RATE:g}, "
                         "plus each binary's first game; 0: only specs with guard: true)")
    ap.add_argument("--give-back", action="store_true",
                    help="a remote task: a native game failing for want of disk where games write ends tools/play "
                         f"with code {GIVE_BACK} at once, unplayed, so its piece gives the task to another machine "
                         "(tools/piece.py GIVE_BACK) instead of playing it on Java here")
    args = ap.parse_args(argv)

    t0 = time.time()
    lines = sys.stdin if args.specs == "-" else open(args.specs)
    specs = [json.loads(l) for l in lines if l.strip()]
    outdir = Path(args.outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    replay_dir = outdir / "replays" if args.replays != "none" else None
    if replay_dir:
        replay_dir.mkdir(exist_ok=True)
    gave_back: list[str] = []
    done = threading.Event()

    def no_room(job: Job, err: str) -> None:
        gave_back.append(f"{job.spec.get('id')}: {err}")
        done.set()
    try:
        runner = Runner(args.workers, outdir, args.timeout, use_cache=not args.no_cache, backend=args.backend,
                        guard=args.guard, no_room=no_room if args.give_back else None)
        jobs = []
        for s in specs:
            if args.engine and "engine" not in s:
                s["engine"] = args.engine
            rdir = replay_dir or (outdir / "replays" if s.get("replay") else None)
            if rdir:
                rdir.mkdir(exist_ok=True)
            jobs.append(runner.resolver.job(s, rdir / f"{safe(s['id'])}.bc26" if rdir else None))
    except store.SetupError as e:
        print(f"tools/play: {e}", file=sys.stderr)
        return 1

    out = open(outdir / "results.jsonl", "a")
    lock = threading.Lock()
    counts = {"played": 0, "cached": 0, "errors": 0, "left": len(jobs), "native": 0, "java": 0}

    def record(r: dict) -> None:
        with lock:
            if r.get("replay") and args.replays == "losses" and r.get("winner") != "B":
                os.unlink(r["replay"])
                r["replay"] = None
            out.write(json.dumps(r) + "\n")
            out.flush()
            counts["errors" if r.get("error") else "cached" if r["cached"] else "played"] += 1
            if not r.get("error") and not r["cached"]:
                counts[r["backend"]] += 1
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
    if gave_back:
        print(f"tools/play: no room to write on {socket.gethostname()}: {gave_back[0][-300:]}", file=sys.stderr,
              flush=True)
        os._exit(GIVE_BACK)  # its other games, if any, are played wherever the task goes next
    runner.close()
    wall = time.time() - t0
    rate = counts["played"] / wall * 60 if wall > 0 else 0
    print(f"tools/play: {counts['played']} played ({counts['native']} native, {counts['java']} java), "
          f"{counts['cached']} cached, {counts['errors']} errors in {wall:.0f} s ({rate:.1f} games/min, "
          f"{args.workers} workers) -> {outdir / 'results.jsonl'}", file=sys.stderr)
    if runner.mismatches:
        print(f"tools/play: the exactness guard found {len(runner.mismatches)} games the engines play differently: "
              "their native binaries are marked bad and their results left the cache. Report it (engine/AGENTS.md); "
              "play with --backend java meanwhile.", file=sys.stderr)
        return 3
    return 2 if counts["errors"] else 0
