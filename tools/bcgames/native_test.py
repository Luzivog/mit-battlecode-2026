"""The native backend's planning, build slots, cache records and exactness guard, without building or playing.
python3 -m unittest discover -s tools/bcgames -t tools -p '*_test.py'"""
import json
import os
import tempfile
import time
import threading
import unittest
from pathlib import Path

from bcgames import native, play, store


KEY = "f" * 32


def bot(pkg, h):
    return native.Bot(pkg, h, f"/jars/{pkg}-{h}.jar")


def result(digest="d1", winner="A", win_type="KILL_ALL_RAT_KINGS", crashed=(0, 0), **kw):
    team = {"points": 0, "cheese_transferred": 5, "cat_damage": 0, "rats": 3, "kings": 1, "cheese": 9}
    r = {"winner": winner, "win_type": win_type, "round": 300, "cooperation": False, "cat_rng_draws": 7,
         "series": {"every": 100, "A": [[1, 2, 1, 0]], "B": [[1, 2, 1, 0]]}, "digest": digest,
         "A": dict(team, crashed=crashed[0]), "B": dict(team, crashed=crashed[1])}
    r.update(kw)
    return r


class ComposeTest(unittest.TestCase):
    base = [bot("awubot", "a1"), bot("opp_obg", "o1"), bot("opp_x", "x1")]

    def test_extra_bots_append_after_the_base_in_package_order(self):
        got = native.compose(self.base, [bot("zeta", "z1"), bot("challenger", "c1")])
        self.assertEqual([(b.pkg, b.hash) for b in got],
                         [("awubot", "a1"), ("opp_obg", "o1"), ("opp_x", "x1"), ("challenger", "c1"), ("zeta", "z1")])

    def test_base_games_need_only_the_base(self):
        self.assertEqual(native.compose(self.base, [bot("awubot", "a1"), bot("opp_obg", "o1")]), self.base)

    def test_another_build_of_a_base_package_takes_its_place(self):
        got = native.compose(self.base, [bot("challenger", "c1"), bot("opp_obg", "o2")])
        self.assertEqual([(b.pkg, b.hash) for b in got],
                         [("awubot", "a1"), ("opp_obg", "o2"), ("opp_x", "x1"), ("challenger", "c1")])

    def test_two_builds_of_one_package_cant_share_a_binary(self):
        self.assertIsNone(native.compose(self.base, [bot("challenger", "c1"), bot("challenger", "c2")]))
        self.assertIsNone(native.compose(self.base, [bot("awubot", "a2"), bot("awubot", "a1")]))
        self.assertIsNotNone(native.compose(self.base, [bot("challenger", "c1"), bot("challenger", "c1")]))

    def test_key_depends_on_everything_a_binary_holds(self):
        ids = [["awubot", "a1"], ["challenger", "c1"]]
        k = native.binary_key("1.2.6", "e1", ids)
        self.assertEqual(k, native.binary_key("1.2.6", "e1", [list(x) for x in ids]))
        self.assertNotEqual(k, native.binary_key("1.2.5", "e1", ids))
        self.assertNotEqual(k, native.binary_key("1.2.6", "e2", ids))
        self.assertNotEqual(k, native.binary_key("1.2.6", "e1", ids[::-1]))
        self.assertNotEqual(k, native.binary_key("1.2.6", "e1", [["awubot", "a1"], ["challenger", "c2"]]))

    def test_base_is_awubot_and_the_gauntlet(self):
        from bcgames import games
        self.assertEqual(list(native.BASE), sorted({"awubot", *games.MAIN_OPPONENTS, *games.OTHER_OPPONENTS}))


class StoreTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.old = native.NDATA
        native.NDATA = Path(self.tmp.name)

    def tearDown(self):
        native.NDATA = self.old
        self.tmp.cleanup()

    def put_binary(self, key, bots, engine="e1", version="1.2.6", bad=False):
        d = native.NDATA / "bin" / key
        d.mkdir(parents=True)
        (d / "bcx").write_text("")
        (d / "build.json").write_text(json.dumps({"key": key, "version": version, "engine": engine, "bots": bots}))
        if bad:
            (d / "BAD").write_text("why")

    def test_any_good_binary_holding_both_bots_plays_the_game(self):
        self.put_binary("k1", [["awubot", "a1"], ["opp_obg", "o1"], ["challenger", "c1"]])
        self.put_binary("k2", [["awubot", "a1"], ["opp_obg", "o1"], ["challenger", "c2"]], bad=True)
        self.put_binary("k3", [["awubot", "a1"], ["opp_obg", "o1"]], engine="e0")
        plan = lambda *need: native.Plan("1.2.6", (), tuple(bot(p, h) for p, h in need))
        self.assertEqual(native.find(plan(("awubot", "a1"), ("opp_obg", "o1")), "e1"), "k1")
        self.assertEqual(native.find(plan(("challenger", "c1"), ("awubot", "a1")), "e1"), "k1")
        self.assertIsNone(native.find(plan(("challenger", "c2"), ("awubot", "a1")), "e1"))  # only a bad one
        self.assertIsNone(native.find(plan(("challenger", "c1"), ("opp_gst", "g1")), "e1"))
        self.assertEqual(native.find(plan(("awubot", "a1"), ("opp_obg", "o1")), "e0"), "k3")

    def test_build_slot_shares_most_modules_and_skips_busy_ones(self):
        old = native.BUILDS
        native.BUILDS = 3
        try:
            ids = [["awubot", "a1"], ["opp_obg", "o1"], ["challenger", "c2"]]
            for i, last in enumerate([{"engine": "e1", "bots": ids[:1]}, {"engine": "e1", "bots": ids[:2]},
                                      {"engine": "e0", "bots": ids}]):
                g = native.NDATA / "gen" / f"1.2.6-{i}"
                g.mkdir(parents=True)
                (g / "last.json").write_text(json.dumps(last))
            with native.gen_dir("1.2.6", "e1", ids) as g1:
                self.assertEqual(g1.name, "1.2.6-1")  # same engine, two modules in place
                with native.gen_dir("1.2.6", "e1", ids) as g2:
                    self.assertEqual(g2.name, "1.2.6-0")  # the next best free one
        finally:
            native.BUILDS = old

    def test_a_remote_binary_is_unpacked_once_to_local_disk(self):
        import gzip
        src = Path(self.tmp.name) / "k9.gz"
        src.write_bytes(gzip.compress(b"binary" * 100))
        old = tempfile.tempdir
        tempfile.tempdir = str(Path(self.tmp.name) / "local")
        try:
            (Path(self.tmp.name) / "local").mkdir()
            got = native.local_binary(str(src))
            self.assertEqual((got.name, got.read_bytes()), ("k9", b"binary" * 100))
            src.unlink()  # a second task of the piece finds it there
            self.assertEqual(native.local_binary(str(src)), got)
            src = Path(self.tmp.name) / "k8.gz"  # a full local disk: unpacked beside it on the volume
            src.write_bytes(gzip.compress(b"other"))
            os.chmod(Path(self.tmp.name) / "local" / "bcx", 0o500)
            self.assertEqual(native.local_binary(str(src)), Path(self.tmp.name) / "k8")
        finally:
            os.chmod(Path(self.tmp.name) / "local" / "bcx", 0o700)
            tempfile.tempdir = old

    def test_a_pieces_tasks_share_its_unpacked_binary(self):
        import gzip
        cache = Path(self.tmp.name) / "cache"
        src = Path(self.tmp.name) / "k7.gz"
        src.write_bytes(gzip.compress(b"seven"))
        old_bin = cache / "bcx" / "k1"
        old_bin.parent.mkdir(parents=True)
        old_bin.write_bytes(b"old")
        os.utime(old_bin, (time.time() - native.LOCAL_KEEP_S - 10,) * 2)
        recent = cache / "bcx" / "k2"
        recent.write_bytes(b"recent")
        os.environ["JOB_CACHE"] = str(cache)
        try:
            got = native.local_binary(str(src))
            self.assertEqual((got, got.read_bytes()), (cache / "bcx" / "k7", b"seven"))
            self.assertFalse(old_bin.exists())  # unused for LOCAL_KEEP_S: gone
            self.assertTrue(recent.exists())
            src.unlink()  # the piece's next task finds it
            self.assertEqual(native.local_binary(str(src)), got)
        finally:
            del os.environ["JOB_CACHE"]

    def test_no_room_reads_the_error_and_the_disk(self):
        where = Path(self.tmp.name)
        self.assertTrue(native.no_room("native: OSError: [Errno 28] No space left on device", where))
        self.assertFalse(native.no_room("native: the game process exited with 0", where))  # this disk has room
        old = native.NO_ROOM_MB
        native.NO_ROOM_MB = 1 << 40
        try:
            self.assertTrue(native.no_room("native: the game process exited with 0", where))
        finally:
            native.NO_ROOM_MB = old

    def test_purge_removes_one_binarys_results(self):
        cache = store.Cache(Path(self.tmp.name) / "c.sqlite")
        cache.put("k1", {"winner": "A", "backend": "native", "native_build": "b1"})
        cache.put("k2", {"winner": "A", "backend": "native", "native_build": "b2"})
        cache.put("k3", {"winner": "B"})
        self.assertEqual(cache.purge_native("b1"), 1)
        self.assertIsNone(cache.get("k1"))
        self.assertIsNotNone(cache.get("k2"))
        self.assertIsNotNone(cache.get("k3"))


class VerdictTest(unittest.TestCase):
    def test_same_game(self):
        self.assertEqual(native.verdict(result(), result(cpu_s=30, engine_errors=0)), "same")

    def test_a_different_replay_is_a_different_game(self):
        self.assertEqual(native.verdict(result("d1"), result("d2")), "pending")
        self.assertEqual(native.verdict(result("d1"), result("d2"), result("d2")), "mismatch")
        self.assertEqual(native.verdict(result("d1"), result("d2"), result("d3")), "mismatch")

    def test_java_differing_from_itself_explains_it(self):
        self.assertEqual(native.verdict(result("d1"), result("d2"), result("d1")), "java-nondeterministic")

    def test_outcome_fields_count_too(self):
        self.assertEqual(native.verdict(result(), result(round=301)), "pending")
        self.assertEqual(native.verdict(result(), result(crashed=(1, 0))), "pending")

    def test_crash_attribution_is_skipped_where_java_cant_attribute(self):
        native_r, java_r = result(crashed=(1, 1)), result(crashed=(2, 0))
        self.assertEqual(native.verdict(native_r, java_r, mirror=True), "same")
        self.assertEqual(native.verdict(native_r, dict(java_r, engine_errors=1)), "same")
        self.assertEqual(native.verdict(native_r, java_r), "pending")

    def test_coin_flip_games_agree_up_to_the_flip(self):
        a = result("d1", winner="A", win_type="WON_BY_DUBIOUS_REASONS")
        b = result("d2", winner="B", win_type="WON_BY_DUBIOUS_REASONS")
        self.assertEqual(native.verdict(a, b), "same")
        self.assertEqual(native.verdict(a, dict(b, round=2)), "pending")

    def test_sampling_is_by_key(self):
        keys = [f"{i * 2654435761 % 2**32:08x}" + "0" * 24 for i in range(20000)]
        self.assertFalse(any(native.sampled(k, 0) for k in keys))
        self.assertTrue(all(native.sampled(k, 1) for k in keys))
        n = sum(native.sampled(k, 1 / 32) for k in keys)
        self.assertTrue(500 < n < 750, n)
        self.assertEqual([native.sampled(k, 0.1) for k in keys[:50]], [native.sampled(k, 0.1) for k in keys[:50]])


class FakePool:
    """Plays a Java game by returning the next prepared result."""

    def __init__(self, results):
        self.results = list(results)
        self.played = 0

    def submit(self, job, cb):
        self.played += 1
        cb(job, dict(self.results.pop(0), backend="java"))


class FakeResolver:
    def job(self, spec, replay=None):
        return play.Job(spec=spec, key=KEY, meta={"key": KEY})


class GuardFlowTest(unittest.TestCase):
    """play.Runner's guard: what is delivered, cached and reported for each verdict."""

    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.old = native.NDATA
        native.NDATA = Path(self.tmp.name)
        (native.NDATA / "bin" / "b1").mkdir(parents=True)

    def tearDown(self):
        native.NDATA = self.old
        self.tmp.cleanup()

    def runner(self, java_results, rate=1.0):
        r = play.Runner.__new__(play.Runner)
        r.resolver = FakeResolver()
        r.cache = store.Cache(Path(self.tmp.name) / "c.sqlite")
        r.cache_lock = threading.Lock()
        r.pool = FakePool(java_results)
        r.guard_rate, r.first, r.mismatches, r.lock = rate, set(), [], threading.Lock()
        r.seen, r.checked = [], []
        r.on_mismatch = lambda job, g: r.seen.append(g)
        r.on_guard, r.guards_pending = None, 0
        r.no_room, r.outdir = None, Path(self.tmp.name)
        return r

    def play(self, r, nres, spec=None):
        class OneNative:
            def submit(self, job, cb):
                cb(job, dict(nres))
                return True
        r.native = OneNative()
        r.cache.put("old", {"winner": "A", "native_build": "b1"})
        job = play.Job(spec=dict(spec or {"id": "g", "a": "challenger", "b": "awubot", "map": "m"}), key=KEY,
                       meta={"key": KEY})
        out = []
        r.submit(job, out.append)
        return out[0]

    def test_same(self):
        r = self.runner([result()])
        got = self.play(r, result(backend="native", native_build="b1"))
        self.assertEqual((got["backend"], got["guard"]["verdict"]), ("native", "same"))
        self.assertEqual(r.cache.get(KEY)["backend"], "native")
        self.assertNotIn("guard", r.cache.get(KEY))
        self.assertEqual(r.pool.played, 1)

    def test_java_nondeterminism_keeps_the_native_game(self):
        r = self.runner([result("d2"), result("d1")])
        got = self.play(r, result("d1", backend="native", native_build="b1"))
        self.assertEqual((got["backend"], got["guard"]["verdict"]), ("native", "java-nondeterministic"))
        self.assertEqual(r.cache.get(KEY)["digest"], "d1")
        self.assertEqual(r.seen, [])

    def test_mismatch_marks_the_binary_bad_and_empties_its_cache(self):
        r = self.runner([result("d2"), result("d2")])
        got = self.play(r, result("d1", backend="native", native_build="b1"))
        self.assertEqual((got["backend"], got["digest"], got["guard"]["verdict"]), ("java", "d2", "mismatch"))
        self.assertIsNone(r.cache.get(KEY))  # not cached
        self.assertIsNone(r.cache.get("old"))      # the binary's earlier results purged
        self.assertTrue(native.bad("b1"))
        self.assertEqual(len(r.seen), 1)
        self.assertEqual(len(r.mismatches), 1)

    def test_async_delivers_now_and_reports_the_verdict_after(self):
        r = self.runner([result("d2"), result("d2")])
        r.on_guard = lambda job, g: r.checked.append((g["verdict"], r.guards_pending))
        got = self.play(r, result("d1", backend="native", native_build="b1"))
        self.assertEqual((got["backend"], got["guard"]["verdict"]), ("native", "pending"))
        self.assertEqual(r.checked, [("mismatch", 0)])
        self.assertIsNone(r.cache.get(KEY))  # cached when delivered, purged with its binary
        self.assertEqual(len(r.seen), 1)
        r = self.runner([result()])
        r.on_guard = lambda job, g: r.checked.append(g["verdict"])
        self.play(r, result(backend="native", native_build="b1"))
        self.assertEqual((r.checked, r.cache.get(KEY)["backend"]), (["same"], "native"))

    def test_unsampled_games_are_not_guarded_after_the_binarys_first(self):
        r = self.runner([result()], rate=0.0)
        self.play(r, result(backend="native", native_build="b1"))
        self.assertEqual(r.pool.played, 0)  # rate 0: no guard at all
        r = self.runner([result(), result()], rate=1e-9)
        self.play(r, result(backend="native", native_build="b1"))  # the binary's first game
        self.play(r, result(backend="native", native_build="b1"))
        self.assertEqual(r.pool.played, 1)

    def test_spec_asks_for_the_guard(self):
        r = self.runner([result()], rate=0.0)
        got = self.play(r, result(backend="native", native_build="b1"),
                        {"id": "g", "a": "x", "b": "y", "map": "m", "guard": True})
        self.assertEqual(got["guard"]["verdict"], "same")

    def test_a_native_failure_for_want_of_disk_goes_back_unplayed(self):
        r = self.runner([result()])
        gave = []
        r.no_room = lambda job, err: gave.append(err)
        r.native = type("N", (), {"submit": lambda self, job, cb: (cb(job, {"error": "native: OSError: [Errno 28] "
                                                                                  "No space left on device"}), True)[1]})()
        job = play.Job(spec={"id": "g", "a": "challenger", "b": "awubot", "map": "m"}, key=KEY, meta={"key": KEY})
        out = []
        r.submit(job, out.append)
        self.assertEqual((len(gave), out, r.pool.played), (1, [], 0))  # not played on Java, nothing delivered

    def test_native_failure_plays_on_java(self):
        r = self.runner([result()])
        got = self.play(r, {"error": "native: boom"})
        self.assertEqual((got["backend"], got["native_error"], got["error"]), ("java", "native: boom", None))
        self.assertEqual(r.cache.get(KEY)["backend"], "java")


class RemoteGuardTest(unittest.TestCase):
    """imperial.ImperialRunner: a guarded game's verdict comes from its own task, apart from the game's result."""

    def test_guard_task_resolves_apart_from_the_game(self):
        from bcgames import imperial
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        old = native.NDATA
        native.NDATA = Path(tmp.name)
        self.addCleanup(setattr, native, "NDATA", old)
        r = imperial.ImperialRunner.__new__(imperial.ImperialRunner)
        r.lock, r.cache_lock = threading.Lock(), threading.Lock()
        r.cache = store.Cache(Path(tmp.name) / "c.sqlite")
        r.mismatches, r.seen, r.got = [], [], []
        r.on_mismatch = lambda job, g: r.seen.append(g)
        r.on_guard = lambda job, g: r.got.append((g["verdict"], r.guards_pending))
        r._say = lambda msg: None
        r.guards_pending = 2
        job = play.Job(spec={"id": "g", "a": "x", "b": "y", "map": "m"}, key=KEY, meta={"key": KEY})
        delivered = []
        b = {"run": "run-1", "jobs": {"g": (job, delivered.append)}, "done": set(),
             "guards": {"g~guard": job, "h~guard": job},
             "gstate": {k: {"left": 1, "done": False, "err": None} for k in ("g~guard", "h~guard")}}
        r._deliver(b, dict(result(), id="g~guard", guard={"verdict": "same", "native_build": "b1"}))
        self.assertEqual((r.got, delivered), ([("same", 1)], []))
        r._deliver(b, dict(result(), id="g", key=KEY, backend="native", native_build="b1"))
        self.assertEqual((delivered[0]["backend"], r.cache.get(KEY)["backend"]), ("native", "native"))
        r._deliver(b, dict(result("d2"), id="h~guard", guard={"verdict": "mismatch", "native_build": "b1"}))
        self.assertEqual((r.got[-1], len(r.seen)), (("mismatch", 0), 1))
        self.assertIsNone(r.cache.get(KEY))  # the binary's results purged


class GuardTwinTest(unittest.TestCase):
    def test_the_first_verdict_counts_and_an_error_waits_for_the_twin(self):
        from bcgames import imperial
        r = imperial.ImperialRunner.__new__(imperial.ImperialRunner)
        r.lock = threading.Lock()
        r.got, r.mism = [], []
        r.on_guard = lambda job, g: r.got.append(g["verdict"])
        r._mismatch = lambda job, g: r.mism.append(g)
        r.guards_pending = 2
        job = play.Job(spec={"id": "g"}, key=KEY)
        b = {"run": "run-1", "guards": {"a~guard": job, "a~guard~2": job, "b~guard": job, "b~guard~2": job},
             "gstate": {k: {"left": 2, "done": False, "err": None} for k in ("a~guard", "b~guard")}}
        r._guard_result(b, "a~guard~2", {"guard": {"verdict": "same"}})
        r._guard_result(b, "a~guard", {"guard": {"verdict": "same"}})
        self.assertEqual((r.got, r.guards_pending), (["same"], 1))  # once
        r._guard_result(b, "b~guard", {"error": "boom"})  # its twin may still check it
        self.assertEqual((r.got, r.guards_pending), (["same"], 1))
        r._guard_result(b, "b~guard~2", {"guard": {"verdict": "mismatch"}})
        self.assertEqual((r.got, r.guards_pending, len(r.mism)), (["same", "mismatch"], 0, 1))


class FlusherTest(unittest.TestCase):
    """imperial.ImperialRunner._flusher: a wave of jobs goes as one batch, batches at least FLUSH_S apart, and jobs
    waiting for their binary go once it is ready."""

    def test_batches(self):
        import time
        from bcgames import imperial
        old = imperial.FLUSH_S, imperial.COALESCE_S
        imperial.FLUSH_S, imperial.COALESCE_S = 0.5, 0.05
        self.addCleanup(lambda: (setattr(imperial, "FLUSH_S", old[0]), setattr(imperial, "COALESCE_S", old[1])))
        r = imperial.ImperialRunner.__new__(imperial.ImperialRunner)
        r.lock, r.stopping, r.wake = threading.Lock(), threading.Event(), threading.Event()
        r.pending, r.binaries, r.ahead, r.guards_pending, r.on_guard = [], {}, [], 0, None
        sent = []
        r._launch = lambda batch, is_native, ahead=(): sent.append((time.time(), [j.spec["id"] for j, _ in batch],
                                                                    is_native))
        r._say = lambda msg: None
        threading.Thread(target=r._flusher, daemon=True).start()
        self.addCleanup(lambda: (r.stopping.set(), r.wake.set()))

        def submit(gid, plan=None):
            job = play.Job(spec={"id": gid}, key=KEY)
            job.plan = plan
            with r.lock:
                r.pending.append((job, None))
            r.wake.set()

        t0 = time.time()
        for g in ("a", "b", "c"):
            submit(g)
        time.sleep(0.2)
        submit("d")
        plan = native.Plan("1.2.6", ())
        r.binaries[plan] = {"state": "building"}
        submit("e", plan)
        time.sleep(0.8)
        with r.lock:
            r.binaries[plan] = {"state": "ready", "key": "k", "remote": "/x"}
        r.wake.set()
        time.sleep(0.8)
        self.assertEqual([ids for _, ids, _ in sent], [["a", "b", "c"], ["d"], ["e"]])
        self.assertEqual([n for _, _, n in sent], [False, False, True])
        self.assertLess(sent[0][0] - t0, 0.15)            # the first wave goes at once
        self.assertGreaterEqual(sent[1][0] - sent[0][0], 0.45)  # then FLUSH_S apart
        self.assertGreaterEqual(sent[2][0] - sent[1][0], 0.45)


class GuardsFirstTest(unittest.TestCase):
    """imperial.ImperialRunner: guards of games to come go out with their binary's first batch, every guard task comes
    before the batch's games, and a game guarded ahead is not guarded again."""

    def runner(self):
        from bcgames import imperial
        r = imperial.ImperialRunner.__new__(imperial.ImperialRunner)
        r.lock = threading.Lock()
        r.guard_rate, r.guarded, r.guard_sent, r.ahead, r.guards_pending = 1 / 32, set(), set(), [], 0
        r.plan = native.Plan("1.2.6", ())
        r.binaries = {r.plan: {"state": "ready", "key": "bin1", "remote": "/vol/bcx/bin1.gz"}}
        r.resolver = type("R", (), {"bot": lambda self, ref: (Path(f"/j/{ref}.jar"), "h")})()
        r.planner = type("P", (), {"plan": lambda self, job: r.plan})()
        r.wake = threading.Event()
        return r

    def job(self, gid, key):
        return play.Job(spec={"id": gid, "a": "x", "b": "y", "map": "m"}, key=key, meta={"key": key})

    def test_guards_ahead_then_in_batch_guards_then_games(self):
        r = self.runner()
        import hashlib
        keys = [hashlib.sha256(str(i).encode()).hexdigest()[:32] for i in range(400)]
        sampled = [k for k in keys if native.sampled(k, r.guard_rate)]
        unsampled = [k for k in keys if not native.sampled(k, r.guard_rate)]
        ahead = self.job("late", sampled[0])
        r.guard_ahead(ahead)
        r.guard_ahead(ahead)  # once
        r.guard_ahead(self.job("no", unsampled[0]))  # not sampled: no guard
        self.assertEqual((len(r.ahead), r.guards_pending), (1, 1))
        jars = {"/j/x.jar": "s/x.jar", "/j/y.jar": "s/y.jar"}
        batch = [(self.job(f"g{i}", k), None) for i, k in enumerate([unsampled[1], sampled[1], unsampled[2]])]
        for j, _ in batch:
            j.plan = r.plan
        lines, guards, fresh = r._specs(batch, True, list(r.ahead), jars)
        ids = [json.loads(x)["id"] for x in lines]
        # the binary's first game isn't g0: it has a guard already; each guard twice, its twin in the second half
        self.assertEqual(ids, ["late~guard", "g1~guard", "g0", "late~guard~2", "g1", "g1~guard~2", "g2"])
        self.assertEqual((sorted(guards), fresh), (["g1~guard", "g1~guard~2", "late~guard", "late~guard~2"], 1))
        later = self.job("late", sampled[0])
        later.plan = r.plan
        lines, guards, fresh = r._specs([(later, None)], True, [], jars)
        self.assertEqual(([json.loads(x)["id"] for x in lines], fresh), (["late"], 0))  # guarded ahead: once


class ShipBinaryTest(unittest.TestCase):
    """imperial.ship_binary on the stand-in volume (BC26_LOCAL_REMOTE): its prune of old binaries is best effort. A find
    that saw another upload's temporary file renamed away exited 1 and failed the upload (19 Nov)."""

    def test_an_upload_survives_a_failing_prune(self):
        import subprocess
        import sys
        tmp = Path(tempfile.mkdtemp())
        self.addCleanup(lambda: subprocess.run(["rm", "-rf", str(tmp)]))
        remote = tmp / "remote"
        bcx = remote / "runtime" / "bcx"
        bcx.mkdir(parents=True)
        old, inflight = bcx / "k0.gz", bcx / "k1.gz.Ab12Cd"  # unused for days; another upload's temporary file
        for f in (old, inflight):
            f.write_bytes(b"x")
            os.utime(f, (time.time() - 10 * 86400,) * 2)
        shim = tmp / "bin"
        shim.mkdir()
        (shim / "find").write_text('#!/bin/bash\n/usr/bin/find "$@"; echo "find: gone: No such file or directory" >&2; '
                                   'exit 1\n')
        (shim / "find").chmod(0o755)
        (tmp / "bcx").write_bytes(b"binary")
        root = Path(__file__).resolve().parents[2]
        code = (f"import sys; sys.path[:0] = [{str(root)!r}, {str(root / 'tools')!r}]\n"
                "from pathlib import Path\nfrom bcgames import imperial\n"
                f"print(imperial.ship_binary('k2', Path({str(tmp / 'bcx')!r})))\n")
        env = dict(os.environ, BC26_LOCAL_REMOTE=str(remote), PATH=f"{shim}:{os.environ['PATH']}")
        p = subprocess.run([sys.executable, "-c", code], env=env, capture_output=True, text=True, timeout=60)
        self.assertEqual(p.returncode, 0, p.stderr)
        import gzip
        self.assertEqual(gzip.decompress((bcx / "k2.gz").read_bytes()), b"binary")
        self.assertEqual(p.stdout.strip(), str(bcx / "k2.gz"))
        self.assertFalse(old.exists())  # pruned
        self.assertTrue(inflight.exists())  # another upload's file: left alone


class ShipFolderTest(unittest.TestCase):
    def test_a_test_links_its_own_ship_folder_and_removes_it_at_the_end(self):
        from bcgames import imperial
        imperial.SHIPS.mkdir(exist_ok=True)
        tmp = Path(tempfile.mkdtemp(dir=imperial.SHIPS))  # the store's side of the same filesystem
        self.addCleanup(lambda: __import__("shutil").rmtree(tmp, ignore_errors=True))
        jar = tmp / "challenger-abc.jar"
        jar.write_bytes(b"jar")
        runner = tmp / "runner" / "r1"
        runner.mkdir(parents=True)
        (runner / "Play.class").write_bytes(b"cls")
        r = imperial.ImperialRunner.__new__(imperial.ImperialRunner)
        r.ship = imperial.SHIPS / f"test-{os.getpid()}-{time.time_ns()}"
        self.addCleanup(lambda: __import__("shutil").rmtree(r.ship, ignore_errors=True))
        r.resolver = type("R", (), {"bots": {"challenger": (jar, "abc")}, "runner_dir": runner})()
        r.closing, r.batches = [], []
        jars = r._ship([play.Job(spec={"id": "g", "engine": "1.2.6"}, key=KEY)])
        dest = store.ROOT / jars[str(jar)]
        self.assertEqual((dest.read_bytes(), dest.stat().st_ino), (b"jar", jar.stat().st_ino))  # a link: no space
        self.assertTrue((r.ship / "runner" / "r1" / "Play.class").exists())
        self.assertTrue((r.ship / "engines" / store.engine_jar("1.2.6").name).exists())
        r.drain()
        self.assertFalse(r.ship.exists())


class FollowTest(unittest.TestCase):
    def test_a_failed_task_is_its_games_failure_at_once(self):
        """A task that ended without a result (here: code 1, its jar missing on the node) fails its game as soon as
        its folder appears, and following ends once every game and check is delivered, while the follower still
        runs (cancelling the run's unstarted pieces: 33 min on 19 Nov)."""
        import shutil
        from bcgames import imperial
        rid = f"run-1119-000000-{os.getpid() % 10000:04d}"
        local = store.ROOT / "runs" / rid
        self.addCleanup(lambda: shutil.rmtree(local, ignore_errors=True))
        for n, (res, code, log) in enumerate([(dict(result(), id="g0", key=KEY), 0, "ok"), (None, 1, "no classes"),
                                              (None, 1, "boom")]):
            (local / str(n) / "out").mkdir(parents=True)
            (local / str(n) / "result.json").write_text(json.dumps({"task": n, "code": code}))
            (local / str(n) / "log").write_text(f"tools/play: {log}\n")
            if res:
                (local / str(n) / "out" / "results.jsonl").write_text(json.dumps(res) + "\n")
        r = imperial.ImperialRunner.__new__(imperial.ImperialRunner)
        r.lock, r.cache_lock, r.stopping = threading.Lock(), threading.Lock(), threading.Event()
        r.cache = store.Cache(Path(tempfile.mkdtemp()) / "c.sqlite")
        r.mismatches, r.got = [], []
        r.on_guard = lambda job, g: r.got.append(g)
        r._say = lambda msg: None
        r.guards_pending = 1
        jobs, out = {}, []
        for gid in ("g0", "g1"):
            jobs[gid] = (play.Job(spec={"id": gid, "a": "x", "b": "y", "map": "m"}, key=KEY, meta={"key": KEY}),
                         out.append)
        b = {"run": rid, "jobs": jobs, "guards": {"g1~guard": jobs["g1"][0]}, "done": set(), "ended": False,
             "gstate": {"g1~guard": {"left": 1, "done": False, "err": None}}, "ids": ["g0", "g1", "g1~guard"],
             "proc": type("P", (), {"poll": lambda self: None})()}  # the follower still running
        old = imperial.POLL_S
        imperial.POLL_S = 0.01
        try:
            r._follow(b)
        finally:
            imperial.POLL_S = old
        self.assertTrue(b["complete"])
        got = {x["id"]: x.get("error") for x in out}
        self.assertIsNone(got["g0"])
        self.assertIn("task 1 ended 1 with no result: tools/play: no classes", got["g1"])
        self.assertEqual((r.got[0]["verdict"], r.guards_pending), ("java-error", 0))
        self.assertIn("boom", r.got[0]["error"])


class FinishTest(unittest.TestCase):
    def test_backend_of_old_cache_entries_is_java(self):
        job = play.Job(spec={"id": "g"}, key="k", meta={"key": "k"})
        self.assertEqual(play.finish(job, {"winner": "A"}, True)["backend"], "java")
        self.assertEqual(play.finish(job, {"winner": "A", "backend": "native"}, True)["backend"], "native")


if __name__ == "__main__":
    unittest.main()
