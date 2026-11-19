#!/usr/bin/env python3
"""RefMain plays the official engine's games: its replay is byte-identical (gunzipped) to battlecode.server.Main's.

    engine/test/refmain_test.py [-j N] [--out DIR] [--quick] [--engine 1.2.6|1.2.5]

(a) the unmodified jar, map seeds: RefMain vs `java -cp JAR battlecode.server.Main` (ref.sh's flags, no websocket),
    on a short game and a long one;
(b) a seed: RefMain with `seed` vs the repo's override (`-cp build/classes:JAR`, `-Dbc.game.seed`);
(c) determinism: the same spec twice through RefMain gives identical hashes.txt and replay;
(d) a match: Main playing MATCH's two maps in one JVM, against RefMain playing the first map, then the second with
    cat_rng_skip = the first game's cat_rng_draws (and the second alone, which ends differently).
Also checks RefMain's winner and round against Main's `[server] ... wins (round N)` line. Exit 0 when all pass.
Needs `./gradlew classes` (build/classes). Games run in fresh JVMs, at most -j at once (default 3).
"""

import argparse
import gzip
import json
import os
import re
import subprocess
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

ENGINE = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ENGINE))
import play_java  # noqa: E402

REPO = ENGINE.parent
JAR = None  # set from --engine
OUT_DEFAULT = play_java.DATA / "refruns" / "refmain_test"

SHORT = {"a": "awubot", "b": "awubot", "map": "DefaultSmall"}
LONG = {"a": "awubot", "b": "awubot_quals", "map": "DefaultLarge"}
SEEDED = {"a": "awubot", "b": "awubot", "map": "DefaultSmall", "seed": 347712782}
# two maps whose second game depends on the cat RNG the first leaves (docs/evaluation.md, "Conditions")
MATCH = {"a": "awubot", "b": "awubot", "map": "t_minus_five_minutes,sunandmoon"}


def main_cmd(spec: dict, replay: Path, override: bool) -> list:
    """battlecode.server.Main as ref.sh runs it (cwd = repo root, so bc.game.map-path=maps is the repo's maps/)."""
    a, b = spec["a"], spec["b"]
    classes = str(REPO / "build" / "classes")
    props = {
        "bc.server.wait-for-client": "false", "bc.server.mode": "headless", "bc.server.websocket": "false",
        "bc.server.map-path": str(REPO / "maps"),
        "bc.server.robot-player-to-system-out": "false", "bc.server.debug": "false",
        "bc.engine.debug-methods": "false", "bc.engine.enable-profiler": "false", "bc.engine.show-indicators": "true",
        "bc.game.team-a": a, "bc.game.team-b": b, "bc.game.team-a.url": classes, "bc.game.team-b.url": classes,
        "bc.game.team-a.package": a, "bc.game.team-b.package": b, "bc.game.maps": spec["map"],
        "bc.server.validate-maps": "true", "bc.server.alternate-order": "false", "bc.server.save-file": str(replay),
    }
    if override:
        props["bc.game.seed"] = str(spec["seed"])
    cp = f"{classes}{os.pathsep}{JAR}" if override else str(JAR)
    return (["java"] + play_java.DEFAULT_JAVA_OPTS.split() + play_java.ADD_OPENS
            + [f"-D{k}={v}" for k, v in props.items()] + ["-cp", cp, "battlecode.server.Main", "-c=-"])


def run_main(spec: dict, gdir: Path, override: bool) -> dict:
    gdir.mkdir(parents=True, exist_ok=True)
    replay = gdir / "replay.bc26"
    replay.unlink(missing_ok=True)
    t = time.time()
    with open(gdir / "stdout.txt", "w") as log:
        p = subprocess.run(main_cmd(spec, replay, override), cwd=REPO, stdout=log, stderr=subprocess.STDOUT)
    out = (gdir / "stdout.txt").read_text()
    ms = re.findall(r"\[server\]\s+\S+ \(([AB])\) wins \(round (\d+)\)", out)
    return {"exit": p.returncode, "winner": ms[0][0] if ms else None, "round": int(ms[0][1]) if ms else None,
            "matches": [(w, int(r)) for w, r in ms], "wall": time.time() - t}


def run_ref(spec: dict, gdir: Path, classes: Path, engine: str) -> dict:
    gdir.mkdir(parents=True, exist_ok=True)
    (gdir / "spec.json").write_text(json.dumps({**spec, "engine": engine}) + "\n")
    t = time.time()
    cmd = (["java"] + play_java.DEFAULT_JAVA_OPTS.split() + play_java.ADD_OPENS
           + ["-cp", f"{classes}{os.pathsep}{JAR}", "bcx.RefMain", str(gdir / "spec.json"), str(gdir),
              "--root", str(REPO)])
    with open(gdir / "stdout.txt", "w") as log:
        subprocess.run(cmd, cwd=gdir, stdout=log, stderr=subprocess.STDOUT)
    try:
        r = json.loads((gdir / "result.json").read_text())
    except (OSError, ValueError):
        r = {"error": "no result.json"}
    r["wall"] = time.time() - t
    return r


def run_match_ref(gdir: Path, classes: Path, engine: str) -> dict:
    """RefMain: MATCH's first map, its second after it (cat_rng_skip), its second alone."""
    m1, m2 = MATCH["map"].split(",")
    t = time.time()
    r1 = run_ref({**MATCH, "map": m1}, gdir / "game1", classes, engine)
    r2 = run_ref({**MATCH, "map": m2, "cat_rng_skip": r1.get("cat_rng_draws", 0)}, gdir / "game2", classes, engine)
    r0 = run_ref({**MATCH, "map": m2}, gdir / "game2_alone", classes, engine)
    return {"games": [r1, r2, r0], "winner": r2.get("winner"), "round": r2.get("round"), "wall": time.time() - t}


def gunzip(p: Path):
    try:
        return gzip.decompress(p.read_bytes())
    except OSError:
        return None


def first_diff(x: bytes, y: bytes) -> str:
    n = min(len(x), len(y))
    i = next((k for k in range(n) if x[k] != y[k]), n)
    j = next((k for k in range(n) if x[-1 - k] != y[-1 - k]), n)
    return f"sizes {len(x)}/{len(y)}, first difference at offset {i}, common tail {j} bytes"


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("-j", "--jobs", type=int, default=3)
    ap.add_argument("--out", type=Path, default=OUT_DEFAULT / time.strftime("%Y%m%d-%H%M%S"))
    ap.add_argument("--quick", action="store_true", help="only the short game (skip the long one)")
    ap.add_argument("--engine", default="1.2.6", choices=["1.2.6", "1.2.5"])
    args = ap.parse_args()
    global JAR
    JAR = play_java.JARS / f"battlecode26-java-{args.engine}.jar"
    if not (REPO / "build" / "classes").is_dir():
        sys.exit("refmain_test: build/classes missing, run ./gradlew classes")
    classes = play_java.compile_jvm()
    out = args.out.resolve()
    print(f"refmain_test: engine {args.engine}, output in {out}", flush=True)
    e = args.engine

    jobs = {
        "short_ref": lambda: run_ref(SHORT, out / "short_ref", classes, e),
        "short_ref2": lambda: run_ref(SHORT, out / "short_ref2", classes, e),
        "short_main": lambda: run_main(SHORT, out / "short_main", False),
        "seeded_ref": lambda: run_ref(SEEDED, out / "seeded_ref", classes, e),
        "seeded_override": lambda: run_main(SEEDED, out / "seeded_override", True),
        "match_ref": lambda: run_match_ref(out / "match_ref", classes, e),
        "match_main": lambda: run_main(MATCH, out / "match_main", False),
    }
    if not args.quick:
        jobs["long_ref"] = lambda: run_ref(LONG, out / "long_ref", classes, e)
        jobs["long_main"] = lambda: run_main(LONG, out / "long_main", False)
    order = ["long_ref", "long_main"] if not args.quick else []
    order += [k for k in jobs if k not in order]
    with ThreadPoolExecutor(max_workers=args.jobs) as ex:
        futs = {k: ex.submit(jobs[k]) for k in order}
        res = {k: f.result() for k, f in futs.items()}

    checks = []

    def check(name, ok, detail=""):
        checks.append((name, ok, detail))

    def replay_pair(name, ref, other, other_label):
        rr, ro = out / ref / "replay.bc26", out / other / "replay.bc26"
        x, y = gunzip(rr), gunzip(ro)
        if x is None or y is None:
            check(f"{name}: replay vs {other_label}", False, "missing replay")
            return
        same = x == y
        gz_same = rr.read_bytes() == ro.read_bytes()
        check(f"{name}: replay vs {other_label}", same,
              f"{len(x)} bytes, gzip {'identical' if gz_same else 'differs'}" if same else first_diff(x, y))

    def result_pair(name, ref, other):
        r, m = res[ref], res[other]
        ok = "error" not in r and r.get("winner") == m["winner"] and r.get("round") == m["round"]
        check(f"{name}: result vs Main's log", ok,
              f"RefMain {r.get('winner')} {r.get('win_type')} round {r.get('round')}, "
              f"Main {m['winner']} round {m['round']} (exit {m['exit']})")

    pairs = [("short", "short_ref", "short_main", "Main (jar)"),
             ("seeded", "seeded_ref", "seeded_override", "override Main")]
    if not args.quick:
        pairs.insert(1, ("long", "long_ref", "long_main", "Main (jar)"))
    for name, ref, other, label in pairs:
        result_pair(name, ref, other)
        replay_pair(name, ref, other, label)
    if not args.quick:
        r = res["long_ref"]
        check("long: both teams alive past round 300", "error" not in r and r.get("round", 0) > 300,
              f"round {r.get('round')}")
    g1, g2, g0 = res["match_ref"]["games"]
    mm = res["match_main"]["matches"]
    ref_pairs = [(g.get("winner"), g.get("round")) for g in (g1, g2)]
    check("match: games 1, 2 vs Main's two maps", ref_pairs == mm,
          f"RefMain {ref_pairs} (skip {g2.get('cat_rng_skip')}), Main {mm}")
    check("match: game 2 alone differs", (g0.get("winner"), g0.get("round")) != ref_pairs[1],
          f"alone {g0.get('winner')} round {g0.get('round')}")
    s = res["seeded_ref"]
    check("seeded: plays the seed", s.get("seed") == SEEDED["seed"], f"seed {s.get('seed')}")

    h1 = (out / "short_ref" / "hashes.txt").read_text() if (out / "short_ref" / "hashes.txt").exists() else None
    h2 = (out / "short_ref2" / "hashes.txt").read_text() if (out / "short_ref2" / "hashes.txt").exists() else None
    check("determinism: hashes.txt", h1 is not None and h1 == h2,
          f"{len(h1.splitlines())} rounds, result hash {res['short_ref'].get('hash')}" if h1 else "missing")
    check("determinism: result hash", res["short_ref"].get("hash") == res["short_ref2"].get("hash"),
          f"{res['short_ref'].get('hash')} / {res['short_ref2'].get('hash')}")
    replay_pair("determinism", "short_ref", "short_ref2", "second RefMain run")

    print()
    for k in order:
        r = res[k]
        desc = (f"{r.get('winner')} {r.get('win_type', '')} round {r.get('round')}"
                + (f" error {r['error']}" if r.get("error") else ""))
        print(f"  {k:16s} {desc:44s} wall {r['wall']:6.1f} s" + (f"  cpu {r['cpu_s']} s" if "cpu_s" in r else ""))
    print()
    for name, ok, detail in checks:
        print(f"  {'PASS' if ok else 'FAIL'}  {name:42s} {detail}")
    bad = sum(1 for _, ok, _ in checks if not ok)
    print(f"\nrefmain_test: {len(checks) - bad}/{len(checks)} checks pass")
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    main()
