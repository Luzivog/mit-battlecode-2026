#!/usr/bin/env python3
"""Play specs on the reference engine: one fresh JVM per spec running bcx.RefMain (engine/AGENTS.md).

    engine/play_java.py SPECS.jsonl OUTDIR [-j N] [--dump]

Each game writes OUTDIR/<id, else spec index>/: spec.json, result.json, hashes.txt, replay.bc26, stdout.txt
(and dump.txt with --dump). Result lines are printed in spec order. The jar is picked by the spec's engine field
(battlecode26-java-<engine>.jar in --jars). RefMain and what it uses from engine/jvm/src are compiled when the
sources change, into --build/<hash of the sources>/ (default ~/battlecode26-data/engine/build/jvm-classes).
Exit 0 when every game was played.
"""

import argparse
import hashlib
import json
import os
import re
import shutil
import signal
import subprocess
import sys
import threading
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

ENGINE = Path(__file__).resolve().parent
REPO = ENGINE.parent
DATA = Path.home() / "battlecode26-data" / "engine"
JARS = DATA / "jars"
BUILD = DATA / "build" / "jvm-classes"
COMPILE_JAR = "battlecode26-java-1.2.6.jar"

# the JVM flags of the official Gradle run (ref.sh); heap and GC do not change a game
ADD_OPENS = [
    "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.math=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.util=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
    "--add-opens=java.base/sun.security.action=ALL-UNNAMED",
]
DEFAULT_JAVA_OPTS = "-Xmx1g -XX:+UseSerialGC"


# the sources a reference game depends on (the agreement cache is keyed by them, not by the translator's)
REFERENCE_SOURCES = ["bcx/Match.java", "bcx/RefMain.java", "battlecode/world/BcxStateHash.java",
                     "battlecode/world/BcxMaps.java"]


def reference_hash() -> str:
    src = ENGINE / "jvm" / "src"
    h = hashlib.sha256()
    for f in REFERENCE_SOURCES:
        h.update(f.encode() + b"\0" + (src / f).read_bytes() + b"\0")
    return h.hexdigest()[:16]


def compile_jvm(jars: Path = JARS, build: Path = BUILD) -> Path:
    """Compile RefMain and what it uses from engine/jvm/src (javac --release 21 -sourcepath, against the official
    jar and the ASM jars), once per hash of all the sources: a translator source that does not compile yet cannot
    break the reference."""
    src = ENGINE / "jvm" / "src"
    files = sorted(src.rglob("*.java"))
    roots = [src / f for f in REFERENCE_SOURCES]
    cp = [jars / COMPILE_JAR] + sorted(jars.glob("asm-*.jar"))
    h = hashlib.sha256()
    for f in files:
        h.update(str(f.relative_to(src)).encode() + b"\0" + f.read_bytes() + b"\0")
    for j in cp:
        h.update(j.name.encode() + b"\0")
    v = subprocess.run(["javac", "-version"], capture_output=True, text=True)
    h.update((v.stdout + v.stderr).encode())
    out = build / h.hexdigest()[:16]
    if (out / ".ok").exists():
        return out
    build.mkdir(parents=True, exist_ok=True)
    tmp = build / f".tmp-{os.getpid()}-{threading.get_ident()}"
    shutil.rmtree(tmp, ignore_errors=True)
    tmp.mkdir()
    r = subprocess.run(["javac", "--release", "21", "-nowarn", "-encoding", "UTF-8",
                        "-cp", os.pathsep.join(map(str, cp)), "-sourcepath", str(src), "-d", str(tmp)]
                       + [str(f) for f in roots],
                       capture_output=True, text=True)
    if r.returncode != 0:
        shutil.rmtree(tmp, ignore_errors=True)
        sys.exit(f"play_java: javac failed\n{r.stdout}{r.stderr}")
    (tmp / ".ok").write_text("\n".join(str(f.relative_to(src)) for f in files) + "\n")
    try:
        tmp.rename(out)
    except OSError:  # another process built the same sources first
        shutil.rmtree(tmp, ignore_errors=True)
    return out


def game_dir_name(spec: dict, index: int) -> str:
    if "id" in spec and spec["id"] is not None:
        return re.sub(r"[^A-Za-z0-9._+=~-]", "_", str(spec["id"]))
    return str(index)


def play_one(spec_line: str, index: int, outdir: Path, classes: Path, args) -> dict:
    try:
        spec = json.loads(spec_line)
        if not isinstance(spec, dict):
            raise ValueError("not an object")
    except ValueError as e:
        return {"backend": "java", "error": f"bad spec line {index}: {e}"}
    gdir = outdir / game_dir_name(spec, index)
    gdir.mkdir(parents=True, exist_ok=True)
    for f in ("result.json", "hashes.txt", "replay.bc26", "dump.txt"):
        (gdir / f).unlink(missing_ok=True)
    (gdir / "spec.json").write_text(spec_line.strip() + "\n")
    base = {k: spec[k] for k in ("id", "a", "b", "map") if k in spec}
    base["engine"] = spec.get("engine", "1.2.6")
    jar = args.jars / f"battlecode26-java-{base['engine']}.jar"
    if not jar.exists():
        return {**base, "backend": "java", "error": f"no jar {jar}"}
    cmd = (["java"] + args.java_opts.split() + ADD_OPENS
           + ["-cp", f"{classes}{os.pathsep}{jar}", "bcx.RefMain", str(gdir / "spec.json"), str(gdir),
              "--root", str(args.root)]
           + (["--dump"] if args.dump else []))
    with open(gdir / "stdout.txt", "wb") as log:
        p = subprocess.Popen(cmd, cwd=gdir, stdout=log, stderr=subprocess.STDOUT, start_new_session=True)
        try:
            p.wait(timeout=args.timeout)
        except subprocess.TimeoutExpired:
            os.killpg(p.pid, signal.SIGKILL)
            p.wait()
            return {**base, "backend": "java", "error": f"timeout after {args.timeout} s"}
    try:
        return json.loads((gdir / "result.json").read_text())
    except (OSError, ValueError):
        return {**base, "backend": "java", "error": f"RefMain exited {p.returncode} without result.json"}


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("specs", type=Path, help="JSONL, one spec per line")
    ap.add_argument("outdir", type=Path)
    ap.add_argument("-j", "--jobs", type=int, default=1, help="games at once (default 1)")
    ap.add_argument("--dump", action="store_true", help="also write each round's state dump (dump.txt)")
    ap.add_argument("--root", type=Path, default=REPO, help="repo root: maps/ and build/classes (default this checkout)")
    ap.add_argument("--jars", type=Path, default=JARS, help=f"jar directory (default {JARS})")
    ap.add_argument("--build", type=Path, default=BUILD, help=f"where RefMain is compiled (default {BUILD})")
    ap.add_argument("--java-opts", default=os.environ.get("BCX_JAVA_OPTS", DEFAULT_JAVA_OPTS))
    ap.add_argument("--timeout", type=float, default=3600, help="seconds per game before it is killed")
    args = ap.parse_args()
    args.root = args.root.resolve()
    args.jars = args.jars.resolve()
    outdir = args.outdir.resolve()
    outdir.mkdir(parents=True, exist_ok=True)

    lines = [ln for ln in args.specs.read_text().splitlines() if ln.strip()]
    classes = compile_jvm(args.jars, args.build.resolve())

    results = [None] * len(lines)
    printed = 0
    lock = threading.Lock()

    def run(i):
        nonlocal printed
        r = play_one(lines[i], i, outdir, classes, args)
        with lock:
            results[i] = r
            while printed < len(results) and results[printed] is not None:
                print(json.dumps(results[printed]), flush=True)
                printed += 1

    with ThreadPoolExecutor(max_workers=max(1, args.jobs)) as ex:
        list(ex.map(run, range(len(lines))))
    sys.exit(0 if all("error" not in r for r in results) else 1)


if __name__ == "__main__":
    main()
