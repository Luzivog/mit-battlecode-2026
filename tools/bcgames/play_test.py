"""The runners on the real engines (a few short games). python3 -m unittest discover -s tools/bcgames -t tools -p '*_test.py'

Uses a scratch data dir (BCGAMES_DATA is set before importing), so it never touches the shared cache. The native
tests use the shared native store (binaries are minutes to build): they play only when a binary there holds their bots
(any tools/games run makes one), or build one with BCGAMES_TEST_BUILD=1.
"""
import atexit
import json
import os
import shutil
import struct
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

TMP = tempfile.mkdtemp(prefix="bcgames-test-")
atexit.register(shutil.rmtree, TMP, True)
SHARED_NATIVE = Path(os.environ.get("BCGAMES_DATA", "~/battlecode26-data/games")).expanduser() / "native"
os.environ["BCGAMES_DATA"] = TMP
os.environ.setdefault("BCGAMES_NATIVE_DATA", str(SHARED_NATIVE))
# Engine jars are 10 MB downloads: reuse the shared ones when present.
_shared = Path("~/battlecode26-data/games/engines").expanduser()
if _shared.exists():
    os.symlink(_shared, Path(TMP) / "engines")

from bcgames import native, play, store  # noqa: E402

TOOLS = Path(__file__).resolve().parents[1]


def run_play(specs, *args):
    """tools/play on SPECS; the Java engine unless ARGS name a backend."""
    out = Path(tempfile.mkdtemp(dir=TMP))
    spec_file = out / "specs.jsonl"
    spec_file.write_text("".join(json.dumps(s) + "\n" for s in specs))
    if "--backend" not in args:
        args = (*args, "--backend", "java")
    proc = subprocess.run([sys.executable, str(TOOLS / "play"), str(spec_file), str(out / "o"), *args],
                          capture_output=True, text=True, env=dict(os.environ))
    path = out / "o" / "results.jsonl"
    results = [json.loads(l) for l in path.read_text().splitlines()] if path.exists() else []
    return proc, {r["id"]: r for r in results}, out / "o"


class MapVariantTest(unittest.TestCase):
    def test_seed_patch_changes_only_the_seed(self):
        self.assertEqual(store.embedded_seed("DefaultSmall"), 158)
        d = store.map_variant("DefaultSmall", 12345)
        orig = store.map_file("DefaultSmall").read_bytes()
        var = (d / "DefaultSmall.map26").read_bytes()
        diff = [i for i in range(len(orig)) if orig[i] != var[i]]
        self.assertEqual(len(orig), len(var))
        self.assertLessEqual(len(diff), 4)
        self.assertEqual(struct.unpack_from("<i", var, store._seed_offset(var))[0], 12345)


class PlayTest(unittest.TestCase):
    def test_play_cache_and_crash_count(self):
        # A bot whose king throws an AssertionError on its first turn: the engine destroys it, so it loses.
        src = Path(tempfile.mkdtemp(dir=TMP))
        (src / "crashbot").mkdir()
        (src / "crashbot" / "RobotPlayer.java").write_text(
            "package crashbot;\nimport battlecode.common.*;\npublic class RobotPlayer {\n"
            "  public static void run(RobotController rc) { throw new AssertionError(\"boom\"); }\n}\n")
        classes = src / "classes"
        subprocess.run(["javac", "-d", str(classes), "-cp", str(store.engine_jar("1.2.6")),
                        str(src / "crashbot" / "RobotPlayer.java")], check=True)
        specs = [{"id": "c", "a": "crashbot", "classes_a": str(classes), "b": "afk", "map": "tiny"},
                 {"id": "s", "a": "afk", "b": "suicide", "map": "tiny", "seed": 5}]
        proc, res, out = run_play(specs, "--workers", "2")
        self.assertEqual(proc.returncode, 0, proc.stderr)
        c, s = res["c"], res["s"]
        self.assertEqual((c["winner"], c["winner_pkg"], c["win_type"]), ("B", "afk", "KILL_ALL_RAT_KINGS"))
        self.assertEqual((c["A"]["crashed"], c["B"]["crashed"]), (1, 0))
        self.assertEqual(c["seed"], 36)  # tiny's embedded seed
        self.assertEqual((s["winner"], s["seed"], s["A"]["crashed"]), ("A", 5, 0))
        self.assertEqual(c["series"]["A"][-1], [0, 0, 0, 0])  # at the end: no cheese, rats, kings or cat damage
        self.assertFalse(c["cached"])
        proc, res2, _ = run_play(specs)
        self.assertTrue(res2["c"]["cached"] and res2["s"]["cached"])
        self.assertEqual(res2["c"]["digest"], c["digest"])

    def test_same_game_as_the_official_main(self):
        # bcplay.Play must play exactly what battlecode.server.Main plays: byte-identical replays.
        spec = {"id": "g", "a": "awubot", "b": "afk", "map": "tiny"}
        proc, res, out = run_play([spec], "--replays", "all", "--no-cache")
        self.assertEqual(proc.returncode, 0, proc.stderr)
        jar_a, _ = store.bot_jar("awubot")
        jar_b, _ = store.bot_jar("afk")
        main_replay = out / "main.bc26"
        props = dict(play.PROPS, **{
            "bc.engine.show-indicators": "false", "bc.game.team-a": "awubot", "bc.game.team-b": "afk",
            "bc.game.team-a.url": str(jar_a), "bc.game.team-b.url": str(jar_b), "bc.game.maps": "tiny",
            "bc.game.map-path": str(store.ROOT / "maps"), "bc.server.save-file": str(main_replay)})
        subprocess.run(["java", *play.OPENS, *[f"-D{k}={v}" for k, v in props.items()], "-cp",
                        str(store.engine_jar("1.2.6")), "battlecode.server.Main", "-c=-"],
                       check=True, capture_output=True)
        self.assertEqual(Path(res["g"]["replay"]).read_bytes(), main_replay.read_bytes())

    def test_games_in_one_jvm_equal_games_alone(self):
        # Both games draw from the engine's static cat RNG; without the per-game reset the second one differs.
        first = {"id": "1", "a": "awubot", "b": "awubot_us", "map": "t_minus_five_minutes"}
        second = {"id": "2", "a": "awubot", "b": "awubot_us", "map": "sunandmoon"}
        _, seq, _ = run_play([first, second], "--workers", "1", "--no-cache")
        _, alone, _ = run_play([dict(second)], "--workers", "1", "--no-cache")
        self.assertGreater(seq["1"]["cat_rng_draws"], 0)
        self.assertGreater(seq["2"]["cat_rng_draws"], 0)
        self.assertEqual(seq["2"]["digest"], alone["2"]["digest"])


def native_binary_for(a: str, b: str) -> bool:
    """A binary of the shared store already holds both bots (this checkout's builds), or a build is allowed."""
    if os.environ.get("BCGAMES_TEST_BUILD") == "1":
        return True
    try:
        r = play.Resolver()
        job = r.job({"id": "x", "a": a, "b": b, "map": "DefaultSmall"})
        plan = native.Planner(r).plan(job)
        eid, _ = native.snapshot(plan.version)
        return (native.binary_dir(native.binary_key(plan.version, eid, plan.ids())) / "bcx").exists() or \
            native.find(plan, eid) is not None
    except Exception:  # noqa: BLE001 - no toolchain, no store: skip
        return False


class NativePlayTest(unittest.TestCase):
    """The native backend plays the Java engine's games: same results, replays and digests."""

    @classmethod
    def setUpClass(cls):
        if not native_binary_for("awubot", "opp_food"):
            raise unittest.SkipTest("no native binary holds awubot and opp_food (BCGAMES_TEST_BUILD=1 builds one)")

    def test_native_equals_java_with_the_guard(self):
        specs = [{"id": "s", "a": "awubot", "b": "opp_food", "map": "DefaultSmall"},
                 {"id": "t", "a": "opp_food", "b": "awubot", "map": "tiny", "seed": 77}]
        proc, nat, out = run_play(specs, "--backend", "native", "--guard", "1", "--replays", "all", "--no-cache")
        self.assertEqual(proc.returncode, 0, proc.stderr)
        _, jav, jout = run_play(specs, "--replays", "all", "--no-cache")
        for gid in ("s", "t"):
            n, j = nat[gid], jav[gid]
            self.assertEqual((n["backend"], j["backend"]), ("native", "java"))
            self.assertEqual(n["guard"]["verdict"], "same")
            self.assertTrue(n["native_build"])
            for k in ("winner", "win_type", "round", "seed", "digest", "A", "B", "series", "cat_rng_draws", "key"):
                self.assertEqual(n[k], j[k], k)
            self.assertEqual(Path(n["replay"]).read_bytes(), Path(j["replay"]).read_bytes())

    def test_two_builds_of_one_package_play_on_java(self):
        specs = [{"id": "m", "a": "awubot", "b": "awubot", "classes_b": str(self.other_awubot()), "map": "tiny"}]
        proc, res, _ = run_play(specs, "--backend", "native", "--no-cache")
        self.assertEqual(proc.returncode, 0, proc.stderr)
        self.assertEqual(res["m"]["backend"], "java")

    def other_awubot(self) -> Path:
        """awubot's classes with one class file more: another build of the package."""
        jar, _ = store.bot_jar("awubot")
        d = Path(tempfile.mkdtemp(dir=TMP))
        import zipfile
        with zipfile.ZipFile(jar) as z:
            z.extractall(d)
        shutil.copy(d / "awubot" / "RobotPlayer.class", d / "awubot" / "Extra.class")
        return d


if __name__ == "__main__":
    unittest.main()
