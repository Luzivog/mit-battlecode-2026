"""retain.py and native.gc on stand-in folders. python3 -m unittest discover -s tools/bcgames -t tools -p '*_test.py'"""
import fcntl
import hashlib
import os
import subprocess
import sys
import tempfile
import time
import unittest
from pathlib import Path

from bcgames import native, retain

H = 3600


def age(p: Path, secs: float) -> None:
    t = time.time() - secs
    os.utime(p, (t, t))


class Replays(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp())
        self.addCleanup(lambda: subprocess.run(["rm", "-rf", str(self.tmp)]))
        self.old = retain.RUNS, retain.SEALED_RUNS, retain.REPLAY_CAP
        retain.RUNS, retain.SEALED_RUNS = self.tmp / "runs", self.tmp / "sealed" / "runs"
        self.addCleanup(lambda: setattr(retain, "RUNS", self.old[0]) or setattr(retain, "SEALED_RUNS", self.old[1])
                        or setattr(retain, "REPLAY_CAP", self.old[2]))

    def run_(self, base: Path, rid: str, hours: float, size: int = 10, keep: bool = False) -> Path:
        d = base / rid
        (d / "replays").mkdir(parents=True)
        (d / "results.jsonl").write_text("{}\n")
        (d / "state.json").write_text("{}")
        (d / "replays" / "g.bc26").write_bytes(b"x" * size)
        for p in (d / "state.json", d / "replays" / "g.bc26", d / "replays"):
            age(p, hours * H)
        if keep:
            (d / "keep").touch()
        return d

    def test_age_keep_and_sealed(self):
        old = self.run_(retain.RUNS, "r-old", 30)
        young = self.run_(retain.RUNS, "r-young", 3)
        kept = self.run_(retain.RUNS, "r-kept", 50, keep=True)
        sealed = self.run_(retain.SEALED_RUNS, "s-old", 30)
        gone = retain.replays(time.time(), dry=False)
        self.assertEqual(sorted(n for n, _ in gone), ["r-old", "s-old"])
        self.assertFalse((old / "replays").exists())
        self.assertTrue((old / "results.jsonl").exists())  # results stay
        self.assertTrue((young / "replays").exists() and (kept / "replays").exists())
        self.assertFalse((sealed / "replays").exists())

    def test_cap_takes_the_oldest_but_never_the_newest(self):
        retain.REPLAY_CAP = 25
        runs = [self.run_(retain.RUNS, f"r{h}", h) for h in (3, 5, 8, 1)]
        gone = retain.replays(time.time(), dry=True)
        self.assertEqual([n for n, _ in gone], ["r8", "r5"])  # 40 bytes to 25: the two oldest, r1 too new to go
        self.assertTrue(all((d / "replays").exists() for d in runs))  # dry: nothing removed

    def test_a_replay_made_lately_restarts_its_clock(self):
        d = self.run_(retain.RUNS, "r-old", 30)
        (d / "replays" / "index.jsonl").write_text("{}\n")  # tools/games replays ID just now
        self.assertEqual(retain.replays(time.time(), dry=True), [])


class GenTrees(unittest.TestCase):
    def test_only_trees_of_checkouts_gone(self):
        tmp = Path(tempfile.mkdtemp())
        self.addCleanup(lambda: subprocess.run(["rm", "-rf", str(tmp)]))
        old = retain.ENGINE_BUILD, retain.checkouts
        self.addCleanup(lambda: (setattr(retain, "ENGINE_BUILD", old[0]), setattr(retain, "checkouts", old[1])))
        retain.ENGINE_BUILD = tmp / "build"
        live = tmp / "live"
        live.mkdir()
        retain.checkouts = lambda: [live, tmp / "gone-but-listed"]

        def tree(path: Path, hours: float) -> Path:
            g = retain.ENGINE_BUILD / f"gen-1.2.6-{hashlib.sha256(str(path).encode()).hexdigest()[:8]}"
            (g / "obj").mkdir(parents=True)
            (g / ".ninja_log").write_text("x" * 100)
            for p in (g / ".ninja_log", g):
                age(p, hours * H)
            return g
        keep_live = tree(live, 50)
        dead = tree(tmp / "removed-worktree", 50)
        dead_listed = tree(tmp / "gone-but-listed", 50)  # listed but its folder is gone
        recent = tree(tmp / "unknown-but-busy", 2)
        (retain.ENGINE_BUILD / "jvm-tr-abc").mkdir()
        gone = retain.gen_trees(time.time(), dry=False)
        self.assertEqual(sorted(n for n, _ in gone), sorted([dead.name, dead_listed.name]))
        self.assertTrue(keep_live.exists() and recent.exists() and (retain.ENGINE_BUILD / "jvm-tr-abc").exists())


class Ship(unittest.TestCase):
    def test_idle_folders_go(self):
        root = Path(tempfile.mkdtemp())
        self.addCleanup(lambda: subprocess.run(["rm", "-rf", str(root)]))
        top = root / ".bcgames-ship"
        busy, idle, flat = top / "1119-1-ab-x-aaaa", top / "1118-1-ab-y-bbbb", top / "bots"
        for d in (busy, idle, flat):
            (d / "bots").mkdir(parents=True) if d is not flat else d.mkdir(parents=True)
        for d in (idle, idle / "bots", flat):
            age(d, 30 * H)
        gone = retain.ship(time.time(), dry=False, root=root)
        self.assertEqual(sorted(n for n, _ in gone), ["1118-1-ab-y-bbbb", "bots"])
        self.assertTrue(busy.exists())


class Binaries(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp())
        self.addCleanup(lambda: subprocess.run(["rm", "-rf", str(self.tmp)]))
        self.old = native.NDATA, native.KEEP, native.KEEP_S, native.KEEP_BYTES
        self.addCleanup(lambda: (setattr(native, "NDATA", self.old[0]), setattr(native, "KEEP", self.old[1]),
                                 setattr(native, "KEEP_S", self.old[2]), setattr(native, "KEEP_BYTES", self.old[3])))
        native.NDATA = self.tmp
        (self.tmp / "bin").mkdir()

    def binary(self, key: str, hours: float, size: int = 100) -> Path:
        d = self.tmp / "bin" / key
        d.mkdir()
        (d / "bcx").write_bytes(b"x" * size)
        (d / "build.json").write_text("{}")
        age(d / "build.json", hours * H)
        return d

    def test_count_age_and_size(self):
        native.KEEP, native.KEEP_S, native.KEEP_BYTES = 2, 6 * H, 350
        ages = {"a": 0.1, "b": 1, "c": 2, "d": 3, "e": 8, "f": 9}
        for k, h in ages.items():
            self.binary(k, h)
        gone = sorted(k for k, _ in native.gc(dry=True))
        # a (0.1 h, grace) and b, c (KEEP 2, then within 6 h and 350 bytes): kept; d over the byte cap; e, f old
        self.assertEqual(gone, ["d", "e", "f"])
        native.KEEP_BYTES = 10 ** 9
        self.assertEqual(sorted(k for k, _ in native.gc(dry=True)), ["e", "f"])  # beyond KEEP and 6 h

    def test_a_binary_in_use_stays(self):
        native.KEEP, native.KEEP_S, native.KEEP_BYTES = 0, 0, 0
        self.binary("held", 10)
        self.binary("free", 10)
        code = (f"import fcntl, sys, time; f = open({str(self.tmp / 'bin' / 'held.use')!r}, 'a'); "
                "fcntl.flock(f, fcntl.LOCK_SH); print('held', flush=True); time.sleep(30)")
        p = subprocess.Popen([sys.executable, "-c", code], stdout=subprocess.PIPE, text=True)
        self.addCleanup(p.stdout.close)
        self.addCleanup(p.kill)
        self.assertEqual(p.stdout.readline().strip(), "held")
        gone = native.gc()
        self.assertEqual([k for k, _ in gone], ["free"])
        self.assertTrue((self.tmp / "bin" / "held" / "bcx").exists())
        self.assertFalse((self.tmp / "bin" / "free").exists())
        p.kill()
        p.wait()
        self.assertEqual([k for k, _ in native.gc()], ["held"])  # its process ended: no longer in use

    def test_gc_runs_one_at_a_time_and_survives_a_vanished_binary(self):
        native.KEEP, native.KEEP_S, native.KEEP_BYTES = 0, 0, 0
        self.binary("old", 10)
        lock = native._flock(self.tmp / "bin" / ".gc.lock")
        try:
            self.assertEqual(native.gc(), [])  # another process's gc runs: this one skips
        finally:
            lock.close()
        real = Path.iterdir

        def vanishing(d):  # the binary goes between the listing and its sizing
            if d.name == "old":
                raise FileNotFoundError(2, "No such file or directory", str(d))
            return real(d)
        Path.iterdir = vanishing
        try:
            native.gc()
        finally:
            Path.iterdir = real

    def test_hold_keeps_it_from_this_process_too(self):
        native.KEEP, native.KEEP_S, native.KEEP_BYTES = 0, 0, 0
        self.binary("mine", 10)
        old = dict(native._held)
        native._held.clear()
        try:
            native.hold("mine")
            self.assertEqual(native.gc(), [])
        finally:
            for f in native._held.values():
                f.close()
            native._held.clear()
            native._held.update(old)


class Sweep(unittest.TestCase):
    def test_at_most_hourly(self):
        tmp = Path(tempfile.mkdtemp())
        self.addCleanup(lambda: subprocess.run(["rm", "-rf", str(tmp)]))
        old = retain.STAMP, retain.replays, retain.gen_trees, retain.ship, native.gc
        calls = []
        retain.STAMP = tmp / ".swept"
        retain.replays = lambda now, dry: calls.append("r") or []
        retain.gen_trees = lambda now, dry: []
        retain.ship = lambda now, dry: []
        native.gc = lambda dry=False: []
        try:
            retain.sweep()
            retain.sweep()
            self.assertEqual(calls, ["r"])
            retain.sweep(force=True)
            self.assertEqual(calls, ["r", "r"])
        finally:
            retain.STAMP, retain.replays, retain.gen_trees, retain.ship, native.gc = old


if __name__ == "__main__":
    unittest.main()
