"""Sequential-test bookkeeping on synthetic results. python3 -m unittest discover -s tools/bcgames -t tools -p '*_test.py'"""
import unittest

from bcgames import games


def result(spec, winner, rounds=100, **metrics):
    team = {"points": 0, "cheese_transferred": 0, "cat_damage": 0, "rats": 0, "kings": 1, "cheese": 0, "crashed": 0}
    team.update(metrics)
    r = dict(spec)
    r.update({"winner": winner, "win_type": "KILL_ALL_RAT_KINGS", "round": rounds, "A": dict(team),
              "B": dict(team), "cached": False, "error": None})
    return r


def make(ref=None, maps=("m1", "m2", "m3", "m4"), min_cells=4, max_cells=400, delta=0.1, mode="gain"):
    return games.Test("t", "ours", "theirs", ref, list(maps), False, 0, delta, 0.05, 0.05, min_cells, max_cells,
                      "1.2.6", mode)


def play_cell(t, ours_wins, ref_wins=(True, False), rounds=(100, 100)):
    """ours_wins: did ours win as (A, B)."""
    cell = t.next_cell()
    for spec in t.games(cell):
        wins = ours_wins if spec["role"] == "o" else ref_wins
        bot_won = wins[0] if spec["seat"] == "A" else wins[1]
        winner = spec["seat"] if bot_won else ("B" if spec["seat"] == "A" else "A")
        cell["games"][spec["id"]] = result(spec, winner, rounds[0] if spec["seat"] == "A" else rounds[1])
    t.update()
    return cell


class CellsTest(unittest.TestCase):
    def test_round_robin_one_seed_per_map_per_pass(self):
        it = games.cells(["a", "b", "c"], 0)
        first = [next(it) for _ in range(6)]
        self.assertEqual(sorted(c["map"] for c in first[:3]), ["a", "b", "c"])
        self.assertEqual(sorted(c["map"] for c in first[3:]), ["a", "b", "c"])
        self.assertTrue(all(c["seed"] is None for c in first[:3]))  # pass 0: the official seeds
        self.assertTrue(all(isinstance(c["seed"], int) for c in first[3:]))

    def test_cells_are_fixed_by_seed_base(self):
        a = [next(games.cells(["a", "b", "c"], 7)) for _ in range(1)]
        b = [next(games.cells(["a", "b", "c"], 7)) for _ in range(1)]
        self.assertEqual(a, b)
        self.assertNotEqual(next(games.cells(["a", "b", "c"], 7))["seed"], next(games.cells(["a", "b", "c"], 8))["seed"])


class ListPoolTest(unittest.TestCase):
    def test_list_pool_entries_and_refusal(self):
        import tempfile
        from pathlib import Path
        from bcgames import pools, store
        with tempfile.TemporaryDirectory() as d:
            ok = Path(d) / "ok.txt"
            ok.write_text("tiny:5  # a cell\nDefaultSmall\n")
            entries = pools.list_pool(ok)
            self.assertEqual(entries, ["tiny:5", "DefaultSmall"])
            first = [next(c) for c in [games.cells(entries, 0)] for _ in range(2)]
            self.assertEqual({(c["map"], c["seed"]) for c in first if c["map"] == "tiny"}, {("tiny", 5)})
            fresh = [next(c) for c in [games.cells(entries, 9)] for _ in range(2)]
            tiny = [c["seed"] for c in fresh if c["map"] == "tiny"]
            self.assertEqual(len(tiny), 1)
            self.assertNotEqual(tiny[0], 5)  # a fresh seed base never replays the listed cell
            bad = Path(d) / "bad.txt"
            bad.write_text("tiny\nnosuchmap:3\n")
            with self.assertRaises(store.SetupError):
                pools.list_pool(bad)


class AbTest(unittest.TestCase):
    def test_identical_bots_split_and_read_no_change(self):
        t = make()
        for _ in range(14):
            play_cell(t, (True, False))  # seat A wins both games: a split
        s = t.summary()
        self.assertEqual(s["counts"], [0, 14, 0])
        self.assertEqual(s["score"], 0.5)
        self.assertTrue(s["identical"])
        self.assertEqual(t.verdict, "H0")

    def test_a_change_breaks_identity(self):
        t = make()
        play_cell(t, (True, False), rounds=(100, 101))
        self.assertFalse(t.summary()["identical"])

    def test_strong_bot_accepts_h1(self):
        t = make()
        for i in range(40):
            play_cell(t, (True, True) if i % 2 == 0 else (True, False))
            if t.verdict != "running":
                break
        self.assertEqual(t.verdict, "H1")
        self.assertGreater(t.summary()["score"], 0.7)

    def test_verdict_waits_for_min_cells(self):
        t = make(min_cells=10)
        for _ in range(9):
            play_cell(t, (True, True))
        self.assertEqual(t.verdict, "running")

    def test_only_the_complete_prefix_is_scored(self):
        t = make()
        c0 = t.next_cell()
        play_cell(t, (True, True))
        self.assertEqual(t.prefix, 0)  # cell 0 is still playing
        for spec in t.games(c0):
            c0["games"][spec["id"]] = result(spec, spec["seat"])
        t.update()
        self.assertEqual(t.prefix, 2)

    def test_cap_makes_it_inconclusive(self):
        t = make(max_cells=6, min_cells=4)
        for i in range(6):
            play_cell(t, [(True, True), (False, False)][i % 2])
        self.assertEqual(t.verdict, "inconclusive")


class RefTest(unittest.TestCase):
    """Paired against a reference (main): the test is on ours - ref, so an edge both share cancels out."""

    def run_until_verdict(self, t, ours, ref, n=120):
        for i in range(n):
            play_cell(t, ours(i), ref_wins=ref(i))
            if t.verdict != "running":
                break
        return t

    def test_same_as_ref_reads_no_gain_and_no_harm(self):
        same = lambda i: (True, i % 3 == 0)  # ref and ours both beat theirs often: a shared edge
        g = self.run_until_verdict(make(ref="ref", mode="gain"), same, same)
        h = self.run_until_verdict(make(ref="ref", mode="harm"), same, same)
        self.assertEqual((g.verdict, h.verdict), ("H0", "H0"))
        self.assertTrue(g.summary()["identical"])
        self.assertEqual(games.test_outcome(g.summary(), "ab"), "fail")
        self.assertEqual(games.test_outcome(h.summary(), "ab"), "pass")

    def test_better_than_ref_passes_the_gain_test(self):
        t = self.run_until_verdict(make(ref="ref", mode="gain"), lambda i: (True, i % 2 == 0), lambda i: (True, False))
        self.assertEqual(t.verdict, "H1")
        self.assertGreater(t.summary()["diff"], 0)

    def test_worse_than_ref_fails_the_harm_test(self):
        t = self.run_until_verdict(make(ref="ref", mode="harm"), lambda i: (False, False) if i % 2 else (True, False),
                                   lambda i: (True, False))
        self.assertEqual(t.verdict, "H1")
        self.assertEqual(games.test_outcome(t.summary(), "ab"), "fail")
        self.assertLess(t.summary()["diff"], 0)

    def test_plain_harm_test(self):
        t = self.run_until_verdict(make(mode="harm"), lambda i: (False, False) if i % 2 else (True, False),
                                   lambda i: (True, False))
        self.assertEqual(t.verdict, "H1")


class SureGamesTest(unittest.TestCase):
    def test_the_first_min_cells_games_as_the_test_will_play_them(self):
        t = make(ref="ref", min_cells=6, maps=("m1", "m2", "m3", "m4"))
        sure = t.sure_games()
        played = [g for _ in range(8) for g in t.games(t.next_cell())]
        self.assertEqual(sure, played[:len(sure)])
        self.assertEqual(len(sure), 6 * 4)


class WaitTest(unittest.TestCase):
    def test_any_returns_the_first_stopped_and_names_the_rest(self):
        import contextlib
        import io
        import json
        import tempfile
        import types
        from pathlib import Path
        with tempfile.TemporaryDirectory() as tmp:
            old = games.RUNS, games.report
            games.RUNS = Path(tmp)
            games.report = lambda d, show_games=False: (f"report of {d.name}\nverdict line", "pass")
            try:
                for rid, status in (("r1", "running"), ("r2", "done"), ("r3", "running")):
                    (Path(tmp) / rid).mkdir()
                    (Path(tmp) / rid / "run.json").write_text(json.dumps({"id": rid}))  # no pid: alive
                    (Path(tmp) / rid / "state.json").write_text(json.dumps({"status": status}))
                out = io.StringIO()
                with contextlib.redirect_stdout(out):
                    code = games.cmd_wait(types.SimpleNamespace(ids=["r1", "r2", "r3"], any=True, games=False,
                                                                timeout=0))
                self.assertEqual(code, 0)
                self.assertIn("report of r2", out.getvalue())
                self.assertIn("stopped: r2; still running: r1 r3", out.getvalue())
                out = io.StringIO()
                with contextlib.redirect_stdout(out):
                    code = games.cmd_wait(types.SimpleNamespace(ids=["r1", "r3"], any=True, games=False, timeout=1))
                self.assertEqual(code, games.EXIT["running"])  # neither stopped within --timeout
            finally:
                games.RUNS, games.report = old


if __name__ == "__main__":
    unittest.main()
