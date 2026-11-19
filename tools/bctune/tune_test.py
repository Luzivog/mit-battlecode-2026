"""python3 -m unittest discover -s tools/bctune -t tools -p '*_test.py'"""
import itertools
import json
import os
import random
import subprocess
import tempfile
import unittest
from pathlib import Path

from bctune import tune

REAL = (tune.ROOT / tune.TUNE_FILE).read_text()


class TuneFile(unittest.TestCase):
    def test_parses_every_tune_line(self):
        params = tune.parse(REAL)
        tune_lines = [ln for ln in REAL.splitlines() if "// tune:" in ln and not ln.lstrip().startswith("//")]
        self.assertEqual(len(params), len(tune_lines))
        self.assertEqual(params["SPAWN_RICH"]["value"], 2000)
        self.assertIsInstance(params["EXPLORE_P"]["value"], float)

    def test_render_changes_only_the_values(self):
        out = tune.render(REAL, {"SPAWN_RICH": 1500, "EXPLORE_P": 0.3})
        p = tune.parse(out)
        self.assertEqual((p["SPAWN_RICH"]["value"], p["EXPLORE_P"]["value"]), (1500, 0.3))
        changed = [(a, b) for a, b in zip(REAL.splitlines(), out.splitlines()) if a != b]
        self.assertEqual(len(changed), 2)
        self.assertEqual(len(REAL.splitlines()), len(out.splitlines()))
        self.assertEqual(tune.render(REAL, {}), REAL)

    def test_a_malformed_tune_line_is_an_error(self):
        bad = REAL.replace("SPAWN_RICH = 2000; // tune: 500 [500, 5000]", "SPAWN_RICH = 2000; // tune: 500 [500 5000]")
        self.assertNotEqual(bad, REAL)
        with self.assertRaises(tune.TuneError):
            tune.parse(bad)

    def test_render_refuses_bad_values(self):
        for bad in ({"NOPE": 1}, {"SPAWN_RICH": 10 ** 6}, {"SPAWN_RICH": 1500.5}):
            with self.assertRaises(tune.TuneError):
                tune.render(REAL, bad)


class Design(unittest.TestCase):
    def check(self, k):
        d = tune.screen_design(k)
        n = len(d)
        self.assertTrue(all(len(r) == k for r in d))
        cols = list(zip(*d))
        for c in cols:
            self.assertEqual(sum(c), 0)  # balanced
        for a, b in itertools.combinations(cols, 2):
            self.assertEqual(sum(x * y for x, y in zip(a, b)), 0)  # orthogonal
        # resolution IV: every main effect orthogonal to every two-factor interaction
        for i, j in itertools.combinations(range(k), 2):
            inter = [r[i] * r[j] for r in d]
            for c in cols:
                self.assertEqual(sum(x * y for x, y in zip(inter, c)), 0, (k, i, j))
        return n

    def test_sizes_and_resolution(self):
        self.assertEqual(self.check(16), 32)
        self.assertEqual(self.check(9), 32)
        self.assertEqual(self.check(8), 16)
        self.assertEqual(self.check(3), 8)
        self.assertEqual(self.check(1), 8)

    def test_rows_distinct(self):
        d = tune.screen_design(16)
        self.assertEqual(len({tuple(r) for r in d}), len(d))


class Commits(unittest.TestCase):
    def test_variant_commit_changes_only_tune_java(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo = Path(tmp)
            subprocess.run(["git", "init", "-q", str(repo)], check=True)
            (repo / "src/challenger").mkdir(parents=True)
            (repo / tune.TUNE_FILE).write_text(REAL)
            (repo / "src/challenger/Other.java").write_text("class Other {}\n")
            env = dict(os.environ, GIT_AUTHOR_NAME="t", GIT_AUTHOR_EMAIL="t@t", GIT_COMMITTER_NAME="t",
                       GIT_COMMITTER_EMAIL="t@t")
            subprocess.run(["git", "-C", str(repo), "add", "-A"], check=True)
            subprocess.run(["git", "-C", str(repo), "commit", "-qm", "base"], check=True, env=env)
            old_root, tune.ROOT = tune.ROOT, repo
            old_env = {k: os.environ.get(k) for k in ("GIT_AUTHOR_NAME", "GIT_AUTHOR_EMAIL", "GIT_COMMITTER_NAME",
                                                       "GIT_COMMITTER_EMAIL")}
            os.environ.update({k: "t" if "NAME" in k else "t@t" for k in old_env})
            try:
                base = tune.git("rev-parse", "HEAD")
                sha = tune.make_commit(base, tune.render(REAL, {"SPAWN_FLOOR": 150}), "v", "refs/tune/t/v00")
                self.assertEqual(tune.git("diff", "--name-only", base, sha), tune.TUNE_FILE)
                self.assertEqual(tune.parse(tune.tune_text(sha))["SPAWN_FLOOR"]["value"], 150)
                self.assertEqual(tune.git("rev-parse", "refs/tune/t/v00"), sha)
                self.assertEqual(tune.git("status", "--porcelain"), "")  # working tree and index untouched
            finally:
                tune.ROOT = old_root
                for k, v in old_env.items():
                    if v is None:
                        os.environ.pop(k, None)
                    else:
                        os.environ[k] = v


def fake_runs(games_data: Path, c: dict, effects: dict, maps: int, passes: int, seed_base: int, rng,
              split: bool = False) -> None:
    """Writes one results.jsonl a variant: each game won with p = base(map) + sum effects * x, ref at x = 0; the ref's
    games in every variant's run (a paired run), or with SPLIT once in a run of its own (tools/tune play)."""
    names = c["factors"]
    base_p = {f"m{i}": 0.55 + 0.4 * (i % 5) / 4 for i in range(maps)}
    ref_games = []
    for m in base_p:
        for s in range(passes):
            for seat in "AB":
                ref_games.append((m, s, seat, rng.random() < base_p[m]))
    for v in c["variants"]:
        rid = f"run-{c['name']}-{v['label']}-{seed_base}"
        d = games_data / "runs" / rid
        d.mkdir(parents=True)
        lines = []
        for m, s, seat, won in ref_games:
            other = "AB".replace(seat, "")
            if not split:
                lines.append({"role": "r", "map": m, "seed": s, "seat": seat, "winner": seat if won else other})
            p = base_p[m] + sum(effects.get(n, 0) * x for n, x in zip(names, v["x"]))
            w = rng.random() < max(0.0, min(1.0, p))
            lines.append({"role": "o", "map": m, "seed": s, "seat": seat, "winner": seat if w else other})
        (d / "results.jsonl").write_text("".join(json.dumps(x) + "\n" for x in lines))
        (d / "state.json").write_text(json.dumps({"status": "done"}))
        c["runs"].append({"variant": v["label"], "id": rid, "pool": "goal", "seed_base": seed_base,
                          "cells": maps * passes, "ref": "challenger@x", "engine": "1.2.6"})
    if split:
        d = games_data / "runs" / f"run-{c['name']}-ref-{seed_base}"
        d.mkdir(parents=True)
        lines = [{"role": "o", "map": m, "seed": s, "seat": seat, "winner": seat if won else "AB".replace(seat, "")}
                 for m, s, seat, won in ref_games]
        (d / "results.jsonl").write_text("".join(json.dumps(x) + "\n" for x in lines))
        c["runs"].append({"variant": tune.REF, "id": d.name, "pool": "goal", "seed_base": seed_base,
                          "cells": maps * passes, "ref": "challenger@x", "engine": "1.2.6"})


class Metric(unittest.TestCase):
    def test_share_reads_the_tested_bots_seat(self):
        r = {"seat": "B", "winner": "A", "series": {"every": 100, "A": [[30, 0], [60, 0]], "B": [[10, 0], [40, 0]]}}
        self.assertEqual(tune.metric_value(r, "win"), 0.0)
        self.assertAlmostEqual(tune.metric_value(r, "share100"), 0.25)
        self.assertAlmostEqual(tune.metric_value(r, "share200"), 0.4)
        self.assertAlmostEqual(tune.metric_value(r, "share900"), 0.4)  # ended sooner: its last point
        r["seat"] = "A"
        self.assertAlmostEqual(tune.metric_value(r, "share200"), 0.6)


class Fit(unittest.TestCase):
    def test_recovers_known_effects(self):
        rng = random.Random(7)
        names = ["A", "B", "C", "D", "E"]
        c = {"name": "syn", "factors": names, "variants": [], "runs": [],
             "params": {n: {"type": "int", "value": 10, "step": 1} for n in names}}
        for i, x in enumerate(tune.screen_design(len(names))):
            c["variants"].append({"label": f"v{i:02d}", "x": x, "values": {}})
        c["variants"].append({"label": "c", "x": [0] * len(names), "values": {}})
        effects = {"A": 0.06, "C": -0.04}
        with tempfile.TemporaryDirectory() as tmp:
            old = tune.GAMES_DATA
            tune.GAMES_DATA = Path(tmp)
            try:
                fake_runs(Path(tmp), c, effects, maps=60, passes=10, seed_base=1, rng=rng)
                f = tune.fit_campaign(c, None, boot=400)
            finally:
                tune.GAMES_DATA = old
        got = {e["name"]: e for e in f["effects"]}
        self.assertEqual(f["cells"], 600)
        for n in names:
            true = effects.get(n, 0.0)
            self.assertLess(got[n]["lo"] - 0.01, true, n)
            self.assertGreater(got[n]["hi"] + 0.01, true, n)
        self.assertGreater(got["A"]["z"], 3)
        self.assertLess(got["C"]["z"], -2)
        self.assertLess(abs(got["B"]["z"]), 3)
        # every variant pays nothing at the centre: curvature near 0, and the centre point equals the corners' mean
        self.assertLess(abs(f["curvature"]["est"]), 0.03)
        self.assertLess(abs(f["centre"]["c"][0]), 0.04)
        self.assertEqual(len(f["variants"]), 17)

    def test_reference_in_its_own_run_reads_the_same(self):
        names = ["A", "B", "C"]
        fits = []
        for split in (False, True):
            c = {"name": f"split{split}", "factors": names, "variants": [], "runs": [],
                 "params": {n: {"type": "int", "value": 10, "step": 1} for n in names}}
            for i, x in enumerate(tune.screen_design(len(names))):
                c["variants"].append({"label": f"v{i:02d}", "x": x, "values": {}})
            with tempfile.TemporaryDirectory() as tmp:
                old = tune.GAMES_DATA
                tune.GAMES_DATA = Path(tmp)
                try:
                    fake_runs(Path(tmp), c, {"B": 0.05}, maps=30, passes=4, seed_base=1, rng=random.Random(5),
                              split=split)
                    fits.append(tune.fit_campaign(c, None, boot=200))
                finally:
                    tune.GAMES_DATA = old
        self.assertEqual(fits[0]["cells"], fits[1]["cells"])
        self.assertAlmostEqual(fits[0]["ref_score"], fits[1]["ref_score"])
        for e0, e1 in zip(fits[0]["effects"], fits[1]["effects"]):
            self.assertAlmostEqual(e0["effect"], e1["effect"])
        self.assertAlmostEqual(fits[0]["variants"][0]["diff"], fits[1]["variants"][0]["diff"])

    def test_vs_a_variant(self):
        names = ["A", "B", "C"]
        c = {"name": "vs", "factors": names, "variants": [], "runs": [],
             "params": {n: {"type": "int", "value": 10, "step": 1} for n in names}}
        for i, x in enumerate(tune.screen_design(len(names))):
            c["variants"].append({"label": f"v{i:02d}", "x": x, "values": {}})
        with tempfile.TemporaryDirectory() as tmp:
            old = tune.GAMES_DATA
            tune.GAMES_DATA = Path(tmp)
            try:
                fake_runs(Path(tmp), c, {"A": 0.2}, maps=40, passes=10, seed_base=1, rng=random.Random(9))
                plain = tune.fit_campaign(c, None, boot=200)
                f = tune.fit_campaign(c, None, boot=200, vs="v00")
            finally:
                tune.GAMES_DATA = old
        by = {v["label"]: v for v in plain["variants"]}
        self.assertEqual(f["ref_id"], "v00")
        self.assertNotIn("v00", [v["label"] for v in f["variants"]])
        self.assertAlmostEqual(f["ref_score"], by["v00"]["score"])
        for v in f["variants"]:
            self.assertAlmostEqual(v["diff"], by[v["label"]]["score"] - by["v00"]["score"])

    def test_path_fit_finds_the_peak(self):
        rng = random.Random(3)
        c = {"name": "path", "factors": ["A"], "variants": [], "runs": [],
             "params": {"A": {"type": "int", "value": 10, "step": 1}}}
        for t in (0.5, 1, 1.5, 2, 3):
            c["variants"].append({"label": f"t{t}", "x": [t], "t": t, "values": {}})
        # p(t) = base + 0.08 t - 0.02 t^2: peak at t = 2; the fake runner reads x linearly, so feed the curve per t
        with tempfile.TemporaryDirectory() as tmp:
            old = tune.GAMES_DATA
            tune.GAMES_DATA = Path(tmp)
            try:
                for v in c["variants"]:
                    t = v["t"]
                    v["x"] = [0.08 * t - 0.02 * t * t]
                fake_runs(Path(tmp), c, {"A": 1.0}, maps=80, passes=20, seed_base=1, rng=rng)
                for v in c["variants"]:
                    v["x"] = None
                f = tune.fit_campaign(c, None, boot=300)
            finally:
                tune.GAMES_DATA = old
        self.assertIsNotNone(f["path"]["t_best"])
        self.assertLess(abs(f["path"]["t_best"] - 2.0), 0.7)


if __name__ == "__main__":
    unittest.main()


class Wait(unittest.TestCase):
    def test_next_reports_each_stopped_run_once(self):
        import contextlib
        import io
        import types
        with tempfile.TemporaryDirectory() as tmp:
            old = tune.DATA, tune.GAMES_DATA
            tune.DATA, tune.GAMES_DATA = Path(tmp) / "tune", Path(tmp) / "games"
            try:
                c = {"name": "w", "runs": [{"variant": v, "id": f"r{v}", "seed_base": 1} for v in "abc"]}
                tune.save(c)

                def state(rid, status):
                    d = tune.GAMES_DATA / "runs" / rid
                    d.mkdir(parents=True, exist_ok=True)
                    (d / "state.json").write_text(json.dumps({"status": status, "tests": [{"cells": 5, "score": .5}]}))

                def wait(**kw):
                    out = io.StringIO()
                    with contextlib.redirect_stdout(out):
                        code = tune.cmd_wait(types.SimpleNamespace(name="w", seed_base=None, poll=0.01, **kw))
                    return code, out.getvalue()
                for v in "abc":
                    state(f"r{v}", "running")
                state("ra", "done")
                code, out = wait(next=True)
                self.assertEqual((code, out.splitlines()[0].split()[:3]), (0, ["a", "ra", "[done]"]))
                self.assertIn("1 of 3 runs stopped; 2 running", out)
                state("rb", "done")
                state("rc", "error")
                code, out = wait(next=True)  # both stopped since: both, at once
                self.assertEqual([x.split()[0] for x in out.splitlines()[:2]], ["b", "c"])
                code, out = wait(next=True)  # nothing left: returns at once
                self.assertEqual(out.strip(), "campaign w: 3 of 3 runs stopped")
                self.assertEqual(wait(next=False)[1].strip(), "campaign w: 3 runs stopped")
            finally:
                tune.DATA, tune.GAMES_DATA = old


class Play(unittest.TestCase):
    def test_builds_two_at_once_and_starts_each_test_after_its_build(self):
        import contextlib
        import io
        import time
        import types
        with tempfile.TemporaryDirectory() as tmp:
            fake = Path(tmp) / "games"
            log = Path(tmp) / "log"
            fake.write_text(f'#!/bin/bash\necho "$1 $2 start $(date +%s.%N)" >> {log}\n'
                            f'if [ "$1" = build ]; then sleep 0.6; fi\necho "$1 $2 end $(date +%s.%N)" >> {log}\n'
                            f'[ "$1" = ab ] && echo "1119-000000-ab-$RANDOM"\nexit 0\n')
            fake.chmod(0o755)
            old = tune.DATA, tune.GAMES, tune.GAMES_DATA
            tune.DATA, tune.GAMES, tune.GAMES_DATA = Path(tmp) / "tune", fake, Path(tmp) / "games"
            try:
                c = {"name": "p", "runs": [], "variants": [{"label": f"v{i}", "commit": str(i + 1) * 40} for i in range(4)]}
                tune.save(c)
                a = types.SimpleNamespace(name="p", pool="goal", seed_base=5, cells=10, ref="challenger",
                                          engine="1.2.6", local=False, where=None, only=None, builds=2)
                t0 = time.time()
                with contextlib.redirect_stdout(io.StringIO()):
                    self.assertEqual(tune.cmd_play(a), 0)
                wall = time.time() - t0
                runs = tune.load("p")["runs"]
                self.assertEqual(sorted(r["variant"] for r in runs), ["ref", "v0", "v1", "v2", "v3"])
                self.assertLess(wall, 5 * 0.6 - 0.4)  # five builds of 0.6 s, two at once
                ev = [x.split() for x in log.read_text().splitlines()]
                for r in runs:  # each test starts after its own build has ended
                    bot = "challenger" if r["variant"] == "ref" else f"challenger@{str(int(r['variant'][1:]) + 1) * 12}"
                    built = next(float(e[3]) for e in ev if e[:3] == ["build", bot, "end"])
                    started = next(float(e[3]) for e in ev if e[:3] == ["ab", bot, "start"])
                    self.assertGreaterEqual(started, built)
            finally:
                tune.DATA, tune.GAMES, tune.GAMES_DATA = old
