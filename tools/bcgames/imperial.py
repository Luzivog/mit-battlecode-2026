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
on_guard) before it concludes. A guard task takes 5 times its game (80 s median, 500 s at p99), and ended last in
three tests of four (19 Nov), so guards go first: the games a test will surely play are guarded ahead
(guard_ahead: the first batch of their binary holds them), and a batch's guard tasks come before its games. Each goes
twice (GUARD_COPIES; `ID~guard~2` in the batch's second half, so another machine plays it): the first verdict counts.

A batch's tools/run is read for its run's id and followed at once: its placement (submitting pieces: seconds to
minutes) goes on beside it, and the next batch need not wait for it.

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

SHIPS = store.ROOT / ".bcgames-ship"  # a test's own folder in it (ImperialRunner.ship), removed when it ends
# A batch goes out as soon as jobs wait (after COALESCE_S, so a wave goes as one batch), but no sooner than FLUSH_S
# after the run's previous batch: each batch is a tools/run call and a poller against the login nodes. Measured
# (docs/evaluation.md): a batch's oldest job waits 5-7 s, mostly the tools/run call; at 10 s and at 30 s a test made
# the same 5-6 batches, refills being paced by the results' arrival.
FLUSH_S = 10.0
COALESCE_S = 1.0
POLL_S = 3            # reading finished tasks' results (local files, which tools/compute wait fetches)
# Games a task, native or Java: one, so a batch ends with its longest game. Native tasks of 4 games used a quarter
# less slot time (overhead 1 s a game instead of 2.4) but took twice the wall time.
CHUNK = 1
REMOTE_KEEP_DAYS = 3
# Each guard goes out twice, on two machines (its first copy at the head of its batch, its twin in the second half,
# where other pieces claim): its Java game takes 2-3 times as long on the slowest DoC CPUs as on the fastest (the same
# game: 82 s on rubeus, 197 s on dove02, 291 s on kipp), and the first verdict counts. A twin's mismatch still stops
# the run.
GUARD_COPIES = 2
TWIN = "~2"


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
    # the prune is best effort and leaves other uploads' temporary files alone: a find that saw one renamed away
    # failed the whole upload, and every game of two tests went to Java (424 games, 19 Nov)
    remote.login(f'mkdir -p {d} && t=$(mktemp {dest}.XXXXXX) && cat > "$t" && mv -f "$t" {dest} && '
                 f'{{ find {d} -maxdepth 1 -type f ! -name "*.gz.*" -mtime +{REMOTE_KEEP_DAYS} -delete 2>/dev/null; '
                 f'true; }}', data, stream=2)
    return dest


def link(src, dest) -> None:
    """DEST a hard link to SRC (no space; same filesystem), else a copy; made whole under a temporary name."""
    src, dest = Path(src), Path(dest)
    tmp = dest.with_name(f".{dest.name}.{os.getpid()}")
    tmp.unlink(missing_ok=True)
    try:
        os.link(src, tmp)
    except OSError:
        shutil.copy2(src, tmp)
    os.replace(tmp, dest)


class ImperialRunner:
    """Same interface as play.Runner: resolver, cached(job), submit(job, callback), close(wait), mismatches."""

    def __init__(self, outdir: Path, where: str | None, timeout: float, backend: str = "native",
                 guard: float | None = None, on_mismatch=None, use_cache: bool = True, on_guard=None):
        self.resolver = play.Resolver()
        self.use_cache = use_cache
        self.planner = native.Planner(self.resolver) if backend == "native" else None
        self.binaries: dict[native.Plan, dict] = {}  # plan -> {state: building|ready|failed, key, remote, error}
        self.guard_rate = native.GUARD_RATE if guard is None else guard
        self.guarded: set[str] = set()  # binaries with a guarded game in this run
        self.guard_sent: set[str] = set()  # games (keys) whose guard task is out or queued
        self.ahead: list[play.Job] = []  # guards of games to come, waiting for their binary
        self.closing: list[threading.Thread] = []
        self.on_mismatch, self.on_guard = on_mismatch, on_guard
        self.guards_pending = 0
        self.mismatches: list[dict] = []
        self.cache = store.Cache()
        self.cache_lock = threading.Lock()
        self.outdir = outdir / "imperial"
        self.outdir.mkdir(parents=True, exist_ok=True)
        self.ship = SHIPS / f"{outdir.name}-{os.urandom(2).hex()}"
        self.where, self.timeout = where, timeout
        self.pending: list[tuple[play.Job, object]] = []
        self.batches: list[dict] = []
        self.lock = threading.Lock()
        self.stopping = threading.Event()
        self.wake = threading.Event()  # jobs to send: submitted, or their binary is ready
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
        job.queued = time.time()
        with self.lock:
            if plan is not None and plan not in self.binaries:
                self.binaries[plan] = {"state": "building"}
                threading.Thread(target=self._prepare, args=(plan,), daemon=True).start()
            self.pending.append((job, callback))
        self.wake.set()

    def guard_ahead(self, job: play.Job) -> None:
        """JOB, a game the run will surely play, guarded now if sampled: its guard task goes out with the first batch
        of its binary, not with the game (which may come last)."""
        if self.planner is None or self.guard_rate <= 0 or job.key in self.guard_sent:
            return
        if not native.sampled(job.key, self.guard_rate):
            return
        try:
            plan = self.planner.plan(job)
        except (store.SetupError, native.BuildError):
            return
        if plan is None:
            return
        job.plan = plan
        with self.lock:
            self.guard_sent.add(job.key)
            self.guards_pending += 1
            if plan not in self.binaries:
                self.binaries[plan] = {"state": "building"}
                threading.Thread(target=self._prepare, args=(plan,), daemon=True).start()
            self.ahead.append(job)
        self.wake.set()

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
        self.wake.set()

    def close(self, wait: bool = True) -> None:
        """Stops reading results; the batches still open are cancelled in the background (drain() waits for that),
        each once its placement has ended, so no piece it submits is missed."""
        self.stopping.set()
        self.wake.set()
        for b in self.batches:
            if b.get("complete"):  # its follower closes the run and cancels its unstarted pieces itself
                continue
            if b.get("proc") and b["proc"].poll() is None:
                b["proc"].kill()
            if b.get("run") and not b.get("ended"):
                t = threading.Thread(target=self._cancel, args=(b,), daemon=True)
                t.start()
                self.closing.append(t)
        if wait:
            self.drain()

    def _cancel(self, b: dict) -> None:
        if b.get("place"):
            b["place"].join()
        subprocess.run([str(store.ROOT / "tools" / "compute"), "cancel", b["run"]], cwd=store.ROOT,
                       stdout=self.log, stderr=self.log)

    def drain(self) -> None:
        """The cancels done; this test's ship folder removed (every batch's tools/run has shipped it by now)."""
        for t in self.closing:
            t.join()
        for b in self.batches:
            if b.get("place"):
                b["place"].join()
        shutil.rmtree(self.ship, ignore_errors=True)

    # ------------------------------------------------------------------------------------------------------ batches

    def _say(self, msg: str) -> None:
        self.log.write(f"{time.strftime('%H:%M:%S')} {msg}\n")
        self.log.flush()

    def _flusher(self) -> None:
        last = 0.0
        while True:
            self.wake.wait()
            if self.stopping.wait(max(COALESCE_S, FLUSH_S - (time.time() - last))):
                return
            self.wake.clear()
            java, nat, wait, ahead, dropped = [], [], [], [], []
            with self.lock:
                for job in self.ahead:  # guards of games to come: with their binary's next batch
                    st = self.binaries.get(job.plan)
                    if st is None or st["state"] == "failed":
                        dropped.append(job)  # no native game to check: its games play on Java
                    elif st["state"] == "ready":
                        ahead.append(job)
                self.ahead = [j for j in self.ahead if j not in ahead and j not in dropped]
                self.guards_pending -= len(dropped)
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
            for job in dropped:
                if self.on_guard:
                    self.on_guard(job, {"verdict": "java-error", "error": "its binary failed: no native game"})
            for batch, is_native in ((java, False), (nat, True)):
                if not batch and not (is_native and ahead):
                    continue
                last = time.time()
                try:
                    self._launch(batch, is_native, ahead if is_native else [])
                except Exception as e:  # noqa: BLE001 - every game of the batch fails with the reason
                    self._say(f"batch failed to start: {e}")
                    for job, cb in batch:
                        cb(play.finish(job, {"error": f"imperial: {e}"}, False))
                    if is_native and ahead:
                        with self.lock:
                            self.guards_pending -= len(ahead)
                        for job in ahead:
                            if self.on_guard:
                                self.on_guard(job, {"verdict": "java-error", "error": f"imperial: {e}"})

    def _wants_guard(self, job: play.Job, key: str) -> bool:
        """A game guarded at its launch: its binary's first game in this run (unless one was guarded ahead), or
        sampled, unless guarded ahead."""
        if self.guard_rate <= 0 or job.key in self.guard_sent:
            return False
        first = key not in self.guarded
        self.guarded.add(key)
        if first or native.sampled(job.key, self.guard_rate):
            self.guard_sent.add(job.key)
            return True
        return False

    def _ship(self, jobs: list[play.Job]) -> dict[str, str]:
        """Links (or copies) what the nodes need into this test's ship folder; returns jar path -> its path there."""
        ship = self.ship
        (ship / "engines").mkdir(parents=True, exist_ok=True)
        (ship / "bots").mkdir(exist_ok=True)
        for v in {j.spec["engine"] for j in jobs}:
            src = store.engine_jar(v)
            if not (ship / "engines" / src.name).exists():
                link(src, ship / "engines" / src.name)
        runner = self.resolver.runner_dir
        if not (ship / "runner" / runner.name).exists():
            shutil.copytree(runner, ship / "runner" / runner.name, copy_function=link)
        jars = {}
        for jar, _ in self.resolver.bots.values():
            dest = ship / "bots" / Path(jar).name
            if not dest.exists():
                link(Path(jar), dest)
            jars[str(jar)] = str(dest.relative_to(store.ROOT))
        return jars

    def _launch(self, batch: list[tuple[play.Job, object]], is_native: bool, ahead: list[play.Job] = ()) -> None:
        """BATCH's games (and AHEAD's guards) as one tools/run, guard tasks first; followed as soon as tools/run
        prints the run's id."""
        n = len(self.batches)
        specs = self.outdir / f"batch-{n}.jsonl"
        lines, guards, fresh = self._specs(batch, is_native, ahead, self._ship([j for j, _ in batch] + list(ahead)))
        specs.write_text("".join(x + "\n" for x in lines))
        cmd = [str(store.ROOT / "tools" / "run"), "--detach", "--each", str(specs), "--chunk",
               str(CHUNK), "--gather", "results.jsonl",
               "--with", str(self.ship.relative_to(store.ROOT))]
        if self.where:
            cmd += ["--where", self.where]
        cmd += ["--", "env", f"BCGAMES_DATA={self.ship.relative_to(store.ROOT)}", "BCGAMES_SLOTS=0", "tools/play",
                "{file}", "{out}", "--workers", "1", "--no-cache", "--timeout", str(int(self.timeout)),
                "--backend", "native" if is_native else "java", "--guard", "0", "--give-back"]
        proc = subprocess.Popen(cmd, cwd=store.ROOT, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE, text=True)
        assert proc.stdout is not None
        run = proc.stdout.readline().strip()  # printed once the run's folder is made: its tasks are claimable
        if not run.startswith("run-"):
            out, err = proc.communicate()
            self.log.write(err)
            raise store.SetupError(f"tools/run exit {proc.returncode}: {(run + out + err).strip()[-500:]}")
        b = {"run": run, "jobs": {j.spec["id"]: (j, cb) for j, cb in batch}, "guards": guards, "done": set(),
             "ended": False, "gstate": {}, "ids": [json.loads(x)["id"] for x in lines]}
        for gid in guards:
            st = b["gstate"].setdefault(gid.removesuffix(TWIN), {"left": 0, "done": False, "err": None})
            st["left"] += 1
        b["place"] = threading.Thread(target=self._placed, args=(proc, run), daemon=True)
        b["place"].start()
        with self.lock:
            self.guards_pending += fresh
        b["proc"] = subprocess.Popen([str(store.ROOT / "tools" / "compute"), "wait", run], cwd=store.ROOT,
                                     stdout=self.log, stderr=self.log)
        self.batches.append(b)
        waited = time.time() - min([getattr(j, "queued", time.time()) for j, _ in batch] or [time.time()])
        self._say(f"batch {n}: {len(batch)} {'native' if is_native else 'java'} games as run {run} "
                  f"(oldest queued {waited:.0f} s)" + (f", {len(guards)} guards first" if guards else ""))
        threading.Thread(target=self._follow, args=(b,), daemon=True).start()

    def _specs(self, batch: list[tuple[play.Job, object]], is_native: bool, ahead: list[play.Job],
               jars: dict[str, str]) -> tuple[list[str], dict[str, play.Job], int]:
        """The batch's spec lines, every guard task first (AHEAD's, then those of its own games), each game after;
        the guard tasks by id; how many guards are new (AHEAD's were counted when queued)."""
        guards: dict[str, play.Job] = {}
        lines: list[str] = []

        def spec_of(job: play.Job) -> dict:
            spec = dict(job.spec)
            (a_jar, _), (b_jar, _) = self.resolver.bot(spec["a"]), self.resolver.bot(spec["b"])
            spec["classes_a"], spec["classes_b"] = jars[str(a_jar)], jars[str(b_jar)]
            if is_native:
                spec["bcx"] = self.binaries[job.plan]["remote"]
            return spec

        heads: list[str] = []
        twins: list[str] = []

        def guard_line(job: play.Job, spec: dict) -> None:
            gid = f"{spec['id']}~guard"
            for k in range(GUARD_COPIES):
                cid = gid + (TWIN if k else "")
                guards[cid] = job
                (twins if k else heads).append(json.dumps(dict(spec, id=cid, guard=True, replay=False)))

        for job in ahead:
            self.guarded.add(self.binaries[job.plan]["key"])
            guard_line(job, spec_of(job))
        fresh = 0
        for job, _ in batch:
            spec = spec_of(job)
            if job.replay:
                spec["replay"] = True  # the task writes it to OUT/replays/, it comes back with the results
            if is_native and self._wants_guard(job, self.binaries[job.plan]["key"]):
                guard_line(job, spec)
                fresh += 1
            lines.append(json.dumps(spec))
        # first copies first; the twins spread over the second half, where other pieces claim
        n = len(lines)
        for k, t in enumerate(twins):
            lines.insert(n // 2 + (k * (n - n // 2)) // len(twins) + k, t)
        lines[:0] = heads
        return lines, guards, fresh

    def _placed(self, proc: subprocess.Popen, run: str) -> None:
        """tools/run's placement, beside the batch's follower: its output to the log once it ends."""
        out, err = proc.communicate()
        self.log.write(err + out)
        if proc.returncode:
            self._say(f"run {run}: tools/run exit {proc.returncode} (its follower ends it)")
        self.log.flush()

    def _follow(self, b: dict) -> None:
        """B's results read as tools/compute wait fetches them: each task's folder once, when it appears (whole:
        runs.fetch renames it in). A task that ended without a result for its game (it failed, or lost its machine at
        every start) is that game's failure at once. Following ends when every game and check is delivered (the
        follower may still be cancelling the run's unstarted pieces: that is not waited for) or the follower ends."""
        local = store.ROOT / "runs" / b["run"]
        seen: set[str] = set()
        while not self.stopping.is_set():
            ended = b["proc"].poll() is not None
            try:
                names = os.listdir(local)
            except OSError:
                names = []
            for name in names:
                if not name.isdigit() or name in seen:
                    continue
                seen.add(name)
                t = local / name
                try:
                    lines = (t / "out" / "results.jsonl").read_text().splitlines()
                except OSError:
                    lines = []
                for line in lines:
                    self._deliver(b, json.loads(line))
                for gid in b["ids"][int(name) * CHUNK:(int(name) + 1) * CHUNK]:
                    if gid not in b["done"]:
                        self._deliver(b, self._failure(b, gid, t))
            if all(gid in b["done"] for gid in b["ids"]):
                b["complete"] = True
                break
            if ended:
                break
            self.stopping.wait(POLL_S)
        b["ended"] = True
        for gid in b["ids"]:
            if gid not in b["done"] and not self.stopping.is_set():
                self._deliver(b, {"id": gid, "error": f"imperial run {b['run']}: no result (runs/{b['run']}/*/log)"})

    @staticmethod
    def _failure(b: dict, gid: str, t: Path) -> dict:
        """GID's result when its task's folder T holds none: the task's code and the end of its log."""
        try:
            code = json.loads((t / "result.json").read_text()).get("code")
        except (OSError, ValueError):
            code = None
        try:
            tail = (t / "log").read_text(errors="replace").strip().splitlines()[-1:]
        except OSError:
            tail = []
        why = f"imperial run {b['run']} task {t.name} ended {code} with no result" + (f": {tail[0][-300:]}"
                                                                                       if tail else "")
        if gid in b["guards"]:
            return {"id": gid, "guard": {"verdict": "java-error", "error": why}}
        return {"id": gid, "error": why}

    def _guard_result(self, b: dict, gid: str, r: dict) -> None:
        """A guard task's verdict (its game's result came from its own task). Of a guard's copies the first verdict
        counts, a java-error only once no other copy can check the game; a mismatch from any copy stops the run."""
        job = b["guards"][gid]
        g = r.get("guard") if isinstance(r.get("guard"), dict) else {
            "verdict": "java-error", "error": r.get("error") or "the guard task played no Java game"}
        g = dict(g, remote=b["run"])
        st = b["gstate"][gid.removesuffix(TWIN)]
        st["left"] -= 1
        if g.get("verdict") == "mismatch":
            self._mismatch(job, g)
        if st["done"] or (g.get("verdict") == "java-error" and st["left"] > 0):
            return
        st["done"] = True
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
