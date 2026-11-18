#!/usr/bin/env python3
"""The agreement corpus (engine/AGENTS.md): game specs with stable ids.

    engine/test/corpus.py quick|full [--root REPO]     # prints the specs as JSONL

quick: DefaultSmall, awubot vs awubot at the map seed and at one extra seed, awubot vs QUICK_OPP in both seats, and
       the first fuzz_* package vs awubot when src/fuzz_* exists.
full:  awubot vs itself and vs awubot_{quals,us,mystery,sus} and every opp_*, both seats, every map in maps/, at the
       map seed and at one extra seed; every ordered pair of fuzz_* packages (itself included) and every fuzz_* vs
       awubot in both seats, on FUZZ_MAPS at both seeds.
The extra seed of a map is crc32(map name) mod 2^31, so ids and games never change with the map list.
An id is MAP~A~B~SEED (SEED = "mapseed" or "s<seed>", spaces in map names become '_').
"""

import argparse
import json
import zlib
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent.parent
SNAPSHOTS = ["awubot_quals", "awubot_us", "awubot_mystery", "awubot_sus"]
QUICK_OPP = "opp_gst"
FUZZ_MAPS = ["DefaultSmall", "cheeseguardians", "dirtfulcat", "trapped", "tiny", "whereisthecheese"]


def extra_seed(map_name: str) -> int:
    return zlib.crc32(map_name.encode()) & 0x7FFFFFFF


def spec(a: str, b: str, map_name: str, seed=None, engine=None) -> dict:
    tag = "mapseed" if seed is None else f"s{seed}"
    s = {"id": f"{map_name.replace(' ', '_')}~{a}~{b}~{tag}", "a": a, "b": b, "map": map_name}
    if seed is not None:
        s["seed"] = seed
    if engine is not None:
        s["engine"] = engine
    return s


def packages(root: Path, prefix: str) -> list:
    return sorted(p.name for p in (root / "src").glob(prefix + "*") if (p / "RobotPlayer.java").exists())


def maps(root: Path) -> list:
    return sorted(p.stem for p in (root / "maps").glob("*.map26"))


def both_seeds(a, b, m, engine):
    return [spec(a, b, m, None, engine), spec(a, b, m, extra_seed(m), engine)]


def both_seats(a, b):
    return [(a, b)] if a == b else [(a, b), (b, a)]


def quick(root: Path = REPO, engine=None) -> list:
    m = "DefaultSmall"
    out = both_seeds("awubot", "awubot", m, engine)
    out += [spec(a, b, m, None, engine) for a, b in both_seats("awubot", QUICK_OPP)]
    fuzz = packages(root, "fuzz_")
    if fuzz:
        out.append(spec(fuzz[0], "awubot", m, None, engine))
    return out


def full(root: Path = REPO, engine=None) -> list:
    out = []
    opponents = ["awubot"] + SNAPSHOTS + packages(root, "opp_")
    for m in maps(root):
        for o in opponents:
            for a, b in both_seats("awubot", o):
                out += both_seeds(a, b, m, engine)
    fuzz = packages(root, "fuzz_")
    for m in FUZZ_MAPS:
        for f in fuzz:
            for g in fuzz:
                out += both_seeds(f, g, m, engine)
            for a, b in both_seats(f, "awubot"):
                out += both_seeds(a, b, m, engine)
    ids = [s["id"] for s in out]
    assert len(ids) == len(set(ids)), "duplicate corpus ids"
    return out


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("mode", choices=["quick", "full"])
    ap.add_argument("--root", type=Path, default=REPO)
    ap.add_argument("--engine", choices=["1.2.6", "1.2.5"], help="set every spec's engine (default: none, 1.2.6)")
    args = ap.parse_args()
    for s in (quick if args.mode == "quick" else full)(args.root.resolve(), args.engine):
        print(json.dumps(s))


if __name__ == "__main__":
    main()
