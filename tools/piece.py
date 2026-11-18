"""A piece: the node side of a run (tools/runs.py), `python3 RUN_DIR/piece.py RUN_DIR`, run by job.sh in the run's
checkout. Standard library only: it runs on any node.

It claims the run's tasks one at a time and runs as many at once as its machine holds: JOB_CPUS // task_cpus, and no
more than JOB_MEM_MB // task_mem_mb (a whole-machine task runs alone). A claim is a folder, claims/N, made by mkdir,
which the volume's server makes atomically: two pieces never run one task at once while both live. While a task runs
its piece touches claims/N/beat every BEAT_S; a claim whose beat is older than STALE_S belongs to a piece that died (its
machine evicted it or failed), and any piece may take it over (renamed into lost/, then claimed again). A task is
started at most MAX_STARTS times; the last loss ends it with code LOST.

A task's result is done/N.tar (result.json, log, out/), written whole under a temporary name and renamed in. Its
command runs with {} (its line), {file} (a file of its lines), {out} (its OUT) and {cpus} (its CPUs) replaced, in the
checkout's folder the submitter ran from, with OUT, JOB_OUT, JOB_CPUS, JOB_MEM_MB, TASK, RUN_ID and TMPDIR its own.

SIGTERM (a cancel, an eviction) and SIGUSR1 (Slurm's limit) stop it: its tasks are stopped and their claims given
back at once (into lost/, so they count as a start), then it exits 0 (on Slurm after SIGUSR1, job.sh then requeues
the piece).
"""
from __future__ import annotations

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


def now_on(volume: Path, me: str) -> float:
    """The volume server's clock, as a freshly touched file's mtime says: claims' beats are its times, not ours."""
    f = volume / "clock" / me
    f.parent.mkdir(exist_ok=True)
    f.touch()
    return f.stat().st_mtime


class Piece:
    def __init__(self, run: Path) -> None:
        self.run = run
        self.meta = json.loads((run / "run.json").read_text())
        self.tasks = [json.loads(x) for x in (run / "tasks.jsonl").read_text().splitlines() if x.strip()]
        self.me = f"{os.environ.get('JOB_ID', 'piece')}"
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
        self.running: dict[int, tuple[subprocess.Popen[bytes], float, Path]] = {}
        self.stopping = ""
        for d in ("claims", "done", "lost"):
            (run / d).mkdir(exist_ok=True)
        self.say(f"piece {self.me} on {self.host}: {self.slots} at once, {self.task_cpus} CPUs and "
                 f"{self.task_mem} MB a task, {len(self.tasks)} tasks in the run")

    def say(self, msg: str) -> None:
        print(f"[{time.strftime('%H:%M:%S')}] {msg}", flush=True)

    def done(self) -> set[int]:
        return {int(n[:-4]) for n in os.listdir(self.run / "done") if n.endswith(".tar") and n[:-4].isdigit()}

    def starts(self, n: int) -> int:
        """Task N's starts lost so far (its claims taken over or given back), plus the one it holds."""
        return sum(1 for x in os.listdir(self.run / "lost") if x.split(".")[0] == str(n))

    def claim(self, n: int) -> bool:
        try:
            (self.run / "claims" / str(n)).mkdir()
        except FileExistsError:
            return False
        (self.run / "claims" / str(n) / "by").write_text(f"{self.me} {self.host} {os.getpid()}\n")
        return True

    def give_back(self, n: int, why: str) -> None:
        """Task N's claim moved into lost/ (a rename, atomic on the server: only one piece's move wins)."""
        try:
            os.rename(self.run / "claims" / str(n), self.run / "lost" / f"{n}.{secrets.token_hex(4)}")
            self.say(f"task {n}: claim given up ({why})")
        except OSError:
            pass

    def next_task(self) -> int | None:
        """The first task neither done nor claimed, claimed; else a dead piece's claim taken over; else None."""
        done = self.done()
        claimed = set()
        for x in os.listdir(self.run / "claims"):
            if x.isdigit():
                claimed.add(int(x))
        for n in range(len(self.tasks)):
            if n not in done and n not in claimed and self.claim(n):
                return n
        clock = now_on(self.run, self.me)
        for n in sorted(claimed - done - set(self.running)):
            d = self.run / "claims" / str(n)
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
                self.give_back(n, "an earlier attempt of this piece" if mine else f"no beat for {clock - beat:.0f} s")
                if self.claim(n):
                    return n
        return None

    def start(self, n: int) -> None:
        t = self.tasks[n]
        here = self.scratch / str(n)
        shutil.rmtree(here, ignore_errors=True)
        (here / "out").mkdir(parents=True)
        (here / "tmp").mkdir()
        lines = t.get("lines", [])
        (here / "input").write_text("".join(f"{x}\n" for x in lines))
        subs = {"{}": lines[0] if len(lines) == 1 else "", "{file}": str(here / "input"), "{out}": str(here / "out"),
                "{cpus}": str(self.task_cpus)}
        argv = []
        for a in t["argv"]:
            for k, v in subs.items():
                a = a.replace(k, v)
            argv.append(a)
        env = dict(os.environ, OUT=str(here / "out"), JOB_OUT=str(here / "out"), JOB_CPUS=str(self.task_cpus),
                   JOB_MEM_MB=str(self.task_mem), TASK=str(n), RUN_ID=self.meta["id"], TMPDIR=str(here / "tmp"))
        cwd = Path.cwd() / self.meta.get("cwd", ".")
        log = open(here / "log", "wb")
        try:
            p = subprocess.Popen(argv, cwd=cwd, env=env, stdin=subprocess.DEVNULL, stdout=log,
                                 stderr=subprocess.STDOUT, start_new_session=True)
        except OSError as e:
            log.write(f"piece: {e}\n".encode())
            log.close()
            self.finish(n, 127, 0, 0.0, here)
            return
        log.close()
        self.running[n] = (p, time.monotonic(), here)
        self.say(f"task {n}: started (start {self.starts(n) + 1})")

    def finish(self, n: int, code: int, peak: int, secs: float, here: Path) -> None:
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
                  "start": self.starts(n) + 1}
        (here / "result.json").write_text(json.dumps(result) + "\n")
        tmp = self.scratch / f"{n}.tar"
        with tarfile.open(tmp, "w") as tar:
            for name in ("result.json", "log", "out"):
                tar.add(here / name, name)
        dest = self.run / "done" / f".{n}.tar.{secrets.token_hex(4)}"
        shutil.copyfile(tmp, dest)
        os.rename(dest, self.run / "done" / f"{n}.tar")
        tmp.unlink()
        shutil.rmtree(here, ignore_errors=True)
        self.say(f"task {n}: ended {code} in {secs:.1f} s")

    def lost(self, n: int) -> None:
        """Task N, claimed again after MAX_STARTS starts that were all lost: ended LOST without running."""
        here = self.scratch / str(n)
        (here / "out").mkdir(parents=True, exist_ok=True)
        (here / "log").write_text(f"piece: task {n} lost its machine at each of its {MAX_STARTS} starts\n")
        self.finish(n, LOST, 0, 0.0, here)

    def reap(self) -> None:
        for n, (p, began, here) in list(self.running.items()):
            got = os.wait4(p.pid, os.WNOHANG)
            if got[0] == 0:
                continue
            del self.running[n]
            p.returncode = os.waitstatus_to_exitcode(got[1])
            self.finish(n, p.returncode, got[2].ru_maxrss * 1024, time.monotonic() - began, here)

    def beat(self) -> None:
        for n in self.running:
            try:
                (self.run / "claims" / str(n) / "beat").touch()
            except OSError:
                pass

    def stop(self, sig: int, _frame: object) -> None:
        self.stopping = signal.Signals(sig).name

    def main(self) -> int:
        signal.signal(signal.SIGTERM, self.stop)
        signal.signal(signal.SIGUSR1, self.stop)
        last_beat = 0.0
        while not self.stopping and not (self.run / "closed").exists():
            while len(self.running) < self.slots and not self.stopping:
                n = self.next_task()
                if n is None:
                    break
                if self.starts(n) >= MAX_STARTS:
                    self.lost(n)
                    continue
                self.start(n)
            if not self.running:
                break
            time.sleep(1)
            self.reap()
            if time.monotonic() - last_beat >= BEAT_S:
                self.beat()
                last_beat = time.monotonic()
        if self.running:
            self.say(f"stopping ({self.stopping or 'the run is closed'}): {len(self.running)} tasks stopped")
            for p, _, _ in self.running.values():
                try:
                    os.killpg(p.pid, signal.SIGTERM)
                except ProcessLookupError:
                    pass
            end = time.monotonic() + 10
            while any(p.poll() is None for p, _, _ in self.running.values()) and time.monotonic() < end:
                time.sleep(0.2)
            for n, (p, _, _) in self.running.items():
                try:
                    os.killpg(p.pid, signal.SIGKILL)
                except ProcessLookupError:
                    pass
                p.wait()
                self.give_back(n, self.stopping or "the run is closed")
        shutil.rmtree(self.scratch, ignore_errors=True)
        self.say("piece done")
        return 0


if __name__ == "__main__":
    sys.exit(Piece(Path(sys.argv[1])).main())
