"""Sequential-test bookkeeping on synthetic results. python3 -m unittest discover -s tools -p '*_test.py'"""
import unittest

from bcgames import games


def result(spec, winner, rounds=100, **metrics):
    team = {"points": 0, "cheese_transferred": 0, "cat_damage": 0, "rats": 0, "kings": 1, "cheese": 0, "crashed": 0}
    team.update(metrics)
    r = dict(spec)
    r.update({"winner": winner, "win_type": "KILL_ALL_RAT_KINGS", "round": rounds, "A": dict(team),
              "B": dict(team), "cached": False, "error": None})
    return r


def make(ref=None, maps=("m1", "m2", "m3", "m4"), min_cells=4, max_cells=400, delta=0.1):
    return games.Test("t", "ours", "theirs", ref, list(maps), False, 0, delta, 0.05, 0.05, min_cells, max_cells,
                      "1.2.6")


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


class GauntletTest(unittest.TestCase):
    def test_same_as_ref_is_not_worse(self):
        t = make(ref="ref")
        for _ in range(40):
            play_cell(t, (True, False), ref_wins=(True, False))
            if t.verdict != "running":
                break
        self.assertEqual(t.verdict, "H1")
        self.assertTrue(t.summary()["identical"])

    def test_worse_than_ref_is_flagged(self):
        t = make(ref="ref")
        for i in range(80):
            play_cell(t, (False, False) if i % 2 else (True, False), ref_wins=(True, False))
            if t.verdict != "running":
                break
        self.assertEqual(t.verdict, "H0")
        self.assertLess(t.summary()["diff"], 0)


if __name__ == "__main__":
    unittest.main()
