"""Tests of tools/book/book: python3 -m unittest discover -s tools/book -p '*_test.py'"""
import json
import tempfile
import unittest
from pathlib import Path

import bookfit
import mapbook as mb
from bctune import tune

TUNE = """public final class Tune {
    public static final int A = 40; // tune: 10 [20, 80] a
    public static int B = 3; // tune: 1 [0, 5] b
    public static final double C = 0.6; // tune: 0.1 [0.0, 1.0] c
}
"""


def grid_map(name="m", w=20, h=20, a=(3, 3), b=(16, 16), cells=()):
    g = [["."] * w for _ in range(h)]
    for x, y, c in cells:
        g[y][x] = c
    return {"name": name, "width": w, "height": h, "symmetry": "ROTATIONAL", "grid": ["".join(r) for r in g],
            "body_list": [{"id": 1, "team": "A", "type": "RAT_KING", "x": a[0], "y": a[1]},
                          {"id": 2, "team": "B", "type": "RAT_KING", "x": b[0], "y": b[1]}]}


def book_of(lines):
    with tempfile.NamedTemporaryFile("w", suffix=".jsonl", delete=False) as f:
        f.write("\n".join(json.dumps(x) if isinstance(x, dict) else x for x in lines) + "\n")
    return mb.load(Path(f.name))


class TuneFinality(unittest.TestCase):
    def test_drops_final_only_on_booked(self):
        out = mb.render_tune(TUNE, {"A"})
        self.assertIn("public static int A = 40;", out)
        self.assertIn("public static final int B = 3;", out)
        self.assertIn("public static final double C = 0.6;", out)
        again = mb.render_tune(out, {"B"})
        self.assertIn("public static final int A = 40;", again)
        self.assertIn("public static int B = 3;", again)

    def test_tune_parses_both(self):
        p = tune.parse(mb.render_tune(TUNE, {"A", "C"}))
        self.assertEqual((p["A"]["value"], p["B"]["value"], p["C"]["value"]), (40, 3, 0.6))
        out = tune.render(mb.render_tune(TUNE, {"A"}), {"A": 50})
        self.assertIn("public static int A = 50;", out)


class View(unittest.TestCase):
    def test_bits_and_key(self):
        m = grid_map(cells=[(3, 4, "#"), (8, 3, "d"), (3, 8, "M"), (9, 3, "#")])  # (9, 3) is at d² 36: unseen
        v = mb.view_of(m, "A")
        bit = lambda dx, dy: 1 << ((dx + 5) * 11 + dy + 5)  # noqa: E731
        walls = v["walls"][0] | v["walls"][1] << 64
        self.assertEqual(walls, bit(0, 1))
        self.assertEqual(v["dirt"][0] | v["dirt"][1] << 64, bit(5, 0))
        self.assertNotIn("mines", v)
        self.assertEqual(v["key"], 20 << 18 | 20 << 12 | 3 << 6 | 3)
        self.assertEqual(mb.view_of(m, "B")["key"], 1 << 24 | 20 << 18 | 20 << 12 | 16 << 6 | 16)

    def test_off_map_clipped(self):
        m = grid_map(a=(0, 0))
        self.assertEqual(mb.view_of(m, "A")["walls"], [0, 0])

    def test_symmetry_matches_grids(self):
        """The engine's symmetry name -> the transform its grid obeys -> the bot's ordinal (CellTracker.checkHor
        reverses x for Horizontal)."""
        from bcgames import mapclass
        flip = {1: lambda x, y, w, h: (w - 1 - x, h - 1 - y), 2: lambda x, y, w, h: (w - 1 - x, y),
                3: lambda x, y, w, h: (x, h - 1 - y)}
        for m in mapclass.dump(["DefaultSmall", "arrows", "evileye", "bed", "rift", "jail"]).values():
            f, w, h, g = flip[mb.SYMMETRY[m["symmetry"]]], m["width"], m["height"], m["grid"]
            self.assertTrue(all(g[y][x] == g[f(x, y, w, h)[1]][f(x, y, w, h)[0]] for y in range(h) for x in range(w)),
                            m["name"])

    def test_twins(self):
        a = grid_map("a", cells=[(3, 4, "#")])
        b = grid_map("b", cells=[(3, 4, "#"), (15, 15, "#")])  # differs outside A's view only
        c = grid_map("c", cells=[(3, 5, "#")])
        vs = {n: {t: mb.view_of(m, t) for t in "AB"} for n, m in (("a", a), ("b", b), ("c", c))}
        tw = mb.twins(vs)
        self.assertEqual(tw[("a", "A")], [("b", "A")])
        self.assertEqual(tw[("c", "A")], [])
        self.assertEqual(tw[("a", "B")], [("c", "B")])  # b's wall at (15, 15) is in B's view; c and a match there


class Validate(unittest.TestCase):
    params = tune.parse(TUNE)

    def errs(self, *lines):
        return mb.validate(book_of(lines), self.params)

    def test_good(self):
        self.assertEqual(self.errs({"declare": "SITE", "type": "tiles", "meaning": "m"},
                                   {"map": "bed", "seat": "B", "set": "A", "value": 50, "evidence": ["r1", "r2"]},
                                   "# a comment",
                                   {"map": "bed", "seat": "both", "data": "SITE", "value": [[1, 2]], "evidence": []}),
                         [])

    def test_bad(self):
        cases = [
            ({"map": "nomap", "seat": "A", "set": "A", "value": 50, "evidence": []}, "not a goal map"),
            ({"map": "bed", "seat": "C", "set": "A", "value": 50, "evidence": []}, "seat"),
            ({"map": "bed", "seat": "A", "set": "A", "value": 90, "evidence": []}, "outside"),
            ({"map": "bed", "seat": "A", "set": "A", "value": 50.5, "evidence": []}, "is int"),
            ({"map": "bed", "seat": "A", "set": "Z", "value": 1, "evidence": []}, "not a tune constant"),
            ({"map": "bed", "seat": "A", "data": "Q", "value": [1], "evidence": []}, "not declared"),
            ({"map": "bed", "seat": "A", "set": "A", "value": 50}, "evidence"),
        ]
        for line, msg in cases:
            e = self.errs(line)
            self.assertTrue(e and msg in e[0], (line, e))

    def test_duplicate_seat(self):
        e = self.errs({"map": "bed", "seat": "both", "set": "A", "value": 50, "evidence": []},
                      {"map": "bed", "seat": "B", "set": "A", "value": 60, "evidence": []})
        self.assertTrue(e and "also set" in e[0])


class Render(unittest.TestCase):
    def test_book_java(self):
        params = tune.parse(mb.render_tune(TUNE, {"A"}))
        book = book_of([{"declare": "SITE", "type": "tiles", "meaning": "m"},
                        {"map": "bed", "seat": "B", "set": "A", "value": 50, "evidence": []},
                        {"map": "bed", "seat": "both", "data": "SITE", "value": [[1, 2]], "evidence": []}])
        m = grid_map("bed", cells=[(3, 4, "#")])
        vs = {"bed": {t: mb.view_of(m, t) for t in "AB"}}
        java = mb.render_book(book, params, vs)
        i = mb.map_id("bed")
        self.assertIn(f"case {vs['bed']['A']['key']} -> {{", java)
        self.assertIn(f"case {vs['bed']['B']['key']} -> {{", java)
        self.assertIn(f"case {i << 1 | 1} -> {{ // bed seat B\n                symmetry = 1;\n                Tune.A = 50;\n"
                      f"                SITE = new MapLocation[] {{new MapLocation(1, 2)}};", java)
        self.assertNotIn(f"case {i << 1} -> {{ // bed seat A\n                Tune.A", java)
        self.assertIn("public static MapLocation[] SITE;", java)

    def test_data_shapes(self):
        book = book_of([{"declare": "SYM2", "type": "int", "default": 0, "meaning": "m"},
                        {"declare": "LANE", "type": "tileset", "meaning": "m"},
                        {"map": "bed", "seat": "A", "data": "SYM2", "value": 3, "evidence": []},
                        {"map": "bed", "seat": "A", "data": "LANE", "value": [[1, 2], [5, 2], [0, 7]],
                         "evidence": []}])
        self.assertEqual(mb.validate(book, {}), [])
        m = grid_map("bed")
        java = mb.render_book(book, {}, {"bed": {t: mb.view_of(m, t) for t in "AB"}})
        self.assertIn("    public static int SYM2; //", java)
        self.assertIn("                SYM2 = 3;\n                LANE = new long[Game.mapHeight];\n"
                      "                LANE[2] = 0x22L;\n                LANE[7] = 0x1L;", java)
        bad = book_of([{"declare": "N", "type": "int", "meaning": "m"}])
        self.assertTrue(mb.validate(bad, {}))

    def test_empty_book(self):
        java = mb.render_book({"declare": {}, "entries": []}, {}, {})
        self.assertIn("default -> {\n                return 0;", java)
        self.assertNotIn("case", java.split("identify()")[1].split("default")[0])


class Summary(unittest.TestCase):
    def test_clustered(self):
        d = {("a", "A"): {1: 1, 2: 0, 3: 1, 4: 0}, ("a", "B"): {1: 1, 2: 0, 3: 1, 4: 0}}
        m, lo, hi, n = mb.summary(d)
        self.assertEqual((m, n), (0.5, 8))
        self.assertAlmostEqual(hi - m, 1.96 * (4 / 3 * 4 / 64) ** 0.5)  # 4 cells of sums 2, 0, 2, 0
        self.assertEqual(mb.summary({}), (0.0, 0.0, 0.0, 0))


class Changed(unittest.TestCase):
    def test_changed_seats(self):
        old = book_of([{"map": "bed", "seat": "both", "set": "A", "value": 50, "evidence": []},
                       {"map": "rift", "seat": "A", "set": "A", "value": 50, "evidence": []}])
        new = book_of([{"map": "bed", "seat": "A", "set": "A", "value": 50, "evidence": []},
                       {"map": "bed", "seat": "B", "set": "A", "value": 60, "evidence": []},
                       {"map": "rift", "seat": "A", "set": "A", "value": 50, "evidence": []},
                       {"map": "tiny", "seat": "B", "set": "B", "value": 1, "evidence": []}])
        self.assertEqual(mb.changed_seats(new, old), {("bed", "B"), ("tiny", "B")})


class PointOverBook(unittest.TestCase):
    def test_point_drops_the_entry_on_the_matrix_maps(self):
        book = mb.load()
        hit = next((e for e in book["entries"] if "set" in e), None)
        if hit is None:
            self.skipTest("the book sets no constant")
        p = mb.tune.parse(mb.TUNE_JAVA.read_text())[hit["set"]]
        value = p["lo"] if p["value"] != p["lo"] else p["hi"]
        with tempfile.TemporaryDirectory() as tmp:
            old = mb.tune.DATA
            mb.tune.DATA = Path(tmp)
            try:
                mb.tune.main(["points", "mb-unit", "--point", f"x:{hit['set']}={value}"])
                v = mb.tune.load("mb-unit")["variants"][0]
                dropped = mb.point_over_book(v, [hit["map"]])
                text = mb.tune.git("show", f"{v['commit']}:src/challenger/book.jsonl")
            finally:
                mb.tune.git("update-ref", "-d", "refs/tune/mb-unit/x")
                mb.tune.DATA = old
        self.assertIn(f"{hit['map']} {hit['seat']} {hit['set']}", dropped)
        self.assertFalse(any(e.get("set") == hit["set"] and e["map"] == hit["map"]
                             for e in mb.load_text(text)["entries"]))


class Repo(unittest.TestCase):
    def test_generated_current(self):
        java, tune_text = mb.generate()
        self.assertEqual(mb.BOOK_JAVA.read_text(), java)
        self.assertEqual(mb.TUNE_JAVA.read_text(), tune_text)

    def test_goal_ids(self):
        self.assertEqual(len({mb.map_id(m) for m in mb.pools.GOAL}), len(mb.pools.GOAL))
        self.assertEqual(mb.map_id("bridges"), 0)


if __name__ == "__main__":
    unittest.main()


class Fit(unittest.TestCase):
    def test_pair(self):
        row = lambda m, s, seat, w, role="o": {"map": m, "seed": s, "seat": seat, "winner": w, "role": role}  # noqa
        ref = [row("a", 1, "A", "A"), row("a", 1, "B", "A"), row("a", 2, "A", "B")]
        cand = [row("a", 1, "A", "B"), row("a", 1, "B", "B"), row("a", 3, "A", "A")]
        self.assertEqual(bookfit.pair(ref, cand), {("a", "A"): {1: -1}, ("a", "B"): {1: 1}})

    def test_null_selection_adjusted(self):
        r = bookfit.simulate(K=4, mapseats=40, n=30, reps=3, flips=200)
        self.assertGreater(r["null_best_raw"], 0.03)  # the best of 4 nulls reads high
        self.assertLess(abs(r["null_best_adj"]), 0.015)  # the adjustment removes it
        self.assertLess(abs(r["null_best_shrunk"]), 0.015)
        self.assertLess(r["null_pick_p_max"], 0.06)

    def test_real_effect_survives(self):
        diffs = {"c0": {("m", "A"): {s: (1 if s % 2 else 0) for s in range(60)}},
                 "c1": {("m", "A"): {s: 0 for s in range(60)}}}
        f = bookfit.fit(diffs, flips=500)
        r = f["rows"][0]
        self.assertEqual(r["best"], "c0")
        self.assertLess(r["p_max"], 0.01)
        self.assertGreater(r["adj_lo"], 0)
