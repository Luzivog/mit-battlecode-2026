#!/usr/bin/env python3
"""Agreement runs on Imperial (the compute skill): each task plays its specs on both engines, on the same machine.

    engine/test/imperial.py run [--quick|--full|--specs FILE] [--engine 1.2.6|1.2.5] [--chunk K] [--limit N] [-- RUN_ARGS]
    engine/test/imperial.py report RUN

run: makes engine/build/dist (the stripped native engines, their bundled maps, the jars), writes the specs to
~/battlecode26-data/engine/runs/, and starts `tools/run --kit --detach --each SPECS --chunk K --gather rows.jsonl`
with agreement.py --prune as each task's command; prints the run's id. Follow it with `tools/compute wait RUN`.
Both engines run in the same task because HotSpot's Math.pow/log stubs use rcpps, whose result depends on the CPU.
report: reads runs/RUN/rows.jsonl and prints the games that disagree, the agreement and both engines' CPU time.
A task that disagrees keeps both engines' files in runs/RUN/N/out/.
"""
import argparse
import json
import shutil
import subprocess
import sys
import time
from pathlib import Path

TEST = Path(__file__).resolve().parent
ENGINE = TEST.parent
REPO = ENGINE.parent
sys.path.insert(0, str(TEST))
import corpus  # noqa: E402

DATA = Path.home() / "battlecode26-data" / "engine"
DIST = ENGINE / "build" / "dist"


def dist(engine: str):
    exe = ENGINE / "build" / ("bcx" if engine == "1.2.6" else f"bcx-{engine}")
    if not exe.exists():
        sys.exit(f"imperial: {exe} is missing: engine/build.py --engine {engine}")
    DIST.mkdir(parents=True, exist_ok=True)
    out = DIST / exe.name
    if not out.exists() or out.stat().st_mtime < exe.stat().st_mtime:
        subprocess.run(["strip", "-o", str(out), str(exe)], check=True)
    maps = DIST / f"maps-{engine}"
    if not maps.exists():
        shutil.copytree(ENGINE / "build" / f"maps-{engine}", maps)
    jars = DIST / "jars"
    jars.mkdir(exist_ok=True)
    for j in (DATA / "jars").glob("*.jar"):
        if not (jars / j.name).exists():
            shutil.copy2(j, jars / j.name)
    return out


def run(args, extra):
    exe = dist(args.engine)
    if args.specs:
        specs = [json.loads(ln) for ln in args.specs.read_text().splitlines() if ln.strip()]
    else:
        specs = corpus.full(REPO, args.engine) if args.full else corpus.quick(REPO, args.engine)
    if args.limit is not None:
        specs = specs[:args.limit]
    f = DATA / "runs" / f"imperial-{time.strftime('%m%d-%H%M%S')}.jsonl"
    f.parent.mkdir(parents=True, exist_ok=True)
    f.write_text("".join(json.dumps(s) + "\n" for s in specs))
    rel = exe.relative_to(REPO)
    cmd = ["tools/run", "--kit", "--detach", "--with", str(DIST.relative_to(REPO)), "--each", str(f),
           "--chunk", str(args.chunk), "--gather", "rows.jsonl"] + extra + [
        "--", "python3", "engine/test/agreement.py", "--specs", "{file}", "--native", f"{rel} play --root .",
        "--java", f"engine/play_java.py --jars {DIST.relative_to(REPO)}/jars --build engine/build/jvm-classes", "--java-cache", "{out}/java",
        "--out", "{out}", "-j", "1", "--prune"]
    print(f"imperial: {len(specs)} games in {f}", flush=True)
    print(" ".join(cmd), flush=True)
    sys.exit(subprocess.run(cmd, cwd=REPO).returncode)


def report(args):
    d = REPO / "runs" / args.run
    rows = [json.loads(ln) for ln in (d / "rows.jsonl").read_text().splitlines() if ln.strip()]
    bad = [r for r in rows if not r["ok"]]
    for r in bad:
        print(f"{r['id']}: {r['result']} / {r['hashes']} / {r['replay']}")
    cj = sum(r["java_cpu_s"] for r in rows if isinstance(r.get("java_cpu_s"), (int, float)))
    cn = sum(r["native_cpu_s"] for r in rows if isinstance(r.get("native_cpu_s"), (int, float)))
    try:
        s = json.loads((d / "summary.json").read_text())
        tasks = f"; tasks failed: {s.get('failed')}" if s.get("failed") else ""
    except (OSError, ValueError):
        tasks = ""
    print(f"imperial: {len(rows) - len(bad)}/{len(rows)} games agree; cpu java {cj:.0f} s, native {cn:.0f} s"
          + (f", ratio {cj / cn:.1f}x" if cn else "") + tasks)
    sys.exit(0 if rows and not bad else 1)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    r = sub.add_parser("run")
    mode = r.add_mutually_exclusive_group()
    mode.add_argument("--quick", action="store_true")
    mode.add_argument("--full", action="store_true")
    mode.add_argument("--specs", type=Path)
    r.add_argument("--engine", default="1.2.6", choices=["1.2.6", "1.2.5"])
    r.add_argument("--chunk", type=int, default=4, help="games a task (default 4)")
    r.add_argument("--limit", type=int)
    p = sub.add_parser("report")
    p.add_argument("run")
    args, extra = ap.parse_known_args()
    if extra and extra[0] == "--":
        extra = extra[1:]
    if args.cmd == "run":
        run(args, extra)
    else:
        report(args)


if __name__ == "__main__":
    main()
