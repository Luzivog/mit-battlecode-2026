"""The native backend's planning, build slots, cache records and exactness guard, without building or playing.
python3 -m unittest discover -s tools/bcgames -t tools -p '*_test.py'"""
import json
import tempfile
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
        finally:
            tempfile.tempdir = old

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

    def test_native_failure_plays_on_java(self):
        r = self.runner([result()])
        got = self.play(r, {"error": "native: boom"})
        self.assertEqual((got["backend"], got["native_error"], got["error"]), ("java", "native: boom", None))
        self.assertEqual(r.cache.get(KEY)["backend"], "java")


class FinishTest(unittest.TestCase):
    def test_backend_of_old_cache_entries_is_java(self):
        job = play.Job(spec={"id": "g"}, key="k", meta={"key": "k"})
        self.assertEqual(play.finish(job, {"winner": "A"}, True)["backend"], "java")
        self.assertEqual(play.finish(job, {"winner": "A", "backend": "native"}, True)["backend"], "native")


if __name__ == "__main__":
    unittest.main()
