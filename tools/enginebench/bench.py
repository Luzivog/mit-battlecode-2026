"""enginebench: the native engine's cost over a fixed slice of the agreement corpus (tools/enginebench/AGENTS.md).

    tools/enginebench/enginebench score [-j N] [--json] [--only ID[,ID]] [--main] [--sealed]
    tools/enginebench/enginebench explain [--profile ID]
    tools/enginebench/enginebench scenarios [--json]

score: builds the checkout's engine (engine/build.py, the pinned bots), plays every visible scenario once in a
sandbox under `perf stat -e instructions:u`, checks every output against the official engine's recording, and
prints the score: estimated instructions:u per corpus game, in G (lower is better), main's beside it.
--main scores origin/main's engine instead (exported to the data folder) and caches it; --sealed (integrator only:
ENGINEBENCH_INTEGRATOR=1) scores the sealed scenarios.
explain: the last score's scenarios, worst first, with each INVALID reason in full; --profile ID plays one scenario
under `perf record` and prints where its instructions go.
scenarios: what each visible scenario holds.
record / calibrate: designer only (ENGINEBENCH_DESIGN=1), see AGENTS.md.
"""
from __future__ import annotations

import argparse
import fcntl
import gzip
import hashlib
import io
import json
import math
import os
import re
import shutil
import subprocess
import sys
import tarfile
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

HERE = Path(__file__).resolve().parent
REPO = HERE.parent.parent
DATA = Path.home() / "battlecode26-data" / "enginebench"
ENGINE_DATA = Path.home() / "battlecode26-data" / "engine"
KINDS = {"visible": DATA / "visible", "sealed": DATA / "SEALED-TEST-DO-NOT-READ"}
# sha256 of each manifest.json: the scenarios, their weights and caps, and the reference's outputs (pinned)
MANIFEST_SHA256 = {
    "visible": "b623229a72557c1c5503b4f0dcaf2a0dacdb2eaba50632bd463400bf54d0180d",
    "sealed": "6561ef677caca2d233dabbb8ac674814a3164cd9481e60ab9635831a473ca11e",
}
ENGINE_VERSION = "1.2.6"
# the bots compiled into the scored binary: every frozen package of the agreement corpus (engine/test/corpus.py)
BOTS = ["awubot", "awubot_mystery", "awubot_quals", "awubot_sus", "awubot_us", "fuzz_api", "fuzz_chaos", "fuzz_lang",
        "fuzz_resign", "opp_cerr", "opp_food", "opp_goobie", "opp_gravy", "opp_gst", "opp_obg", "opp_parmesan",
        "opp_powerpuff", "opp_spaark"]
# result.json fields that are not the game's: timing and the backend's name
NOT_COMPARED = {"cpu_s", "wall_ms", "backend"}
OUTPUT_FILES = {"result.json", "hashes.txt", "replay.bc26"}
# peak RSS cap: this factor times main's peak on the scenario, measured at design (calibrate) and pinned
RSS_FACTOR = 1.25
# compiler flags that tie the binary to a CPU or break IEEE semantics; the binary runs on DoC's machines too
ISA_FLAG = re.compile(r"(?<!\S)(-march=(?!x86-64(-v2)?(\s|$))\S*|-mcpu=\S*|-mavx\S*|-mfma\S*|-mbmi\S*|-mf16c|-mlzcnt|"
                      r"-mmovbe|-mamx\S*|-mvaes|-mvpclmulqdq|-mgfni|-msha\b|-mxop|-mfma4|-ffast-math|-Ofast|"
                      r"-funsafe-math-optimizations|-mno-sse\S*)")
ISA_SOURCE = re.compile(r"__attribute__\s*\(\(\s*target|#\s*pragma\s+(GCC|clang)\s+(target|attribute)|"
                        r"#\s*include\s*<(immintrin|avx\w*|x86intrin)\.h>")
# what the code under test may not name
EVAL_NAMES = ("enginebench", "SEALED-TEST", "battlecode26-data/enginebench", "/bench/")


class Refused(Exception):
    """The checkout cannot be scored (no score at all, not INVALID)."""


def sh(cmd, cwd=None, check=True, **kw) -> str:
    r = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, **kw)
    if check and r.returncode != 0:
        raise Refused(f"{' '.join(map(str, cmd))}: exit {r.returncode}: {(r.stderr or r.stdout).strip()[-500:]}")
    return r.stdout


def sha256_file(p: Path) -> str:
    h = hashlib.sha256()
    with open(p, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def eval_hash() -> str:
    """The eval's own code (keys the main cache)."""
    h = hashlib.sha256()
    for f in sorted(HERE.glob("*.py")) + [HERE / "enginebench", HERE / "runner.c"]:
        if f.exists() and not f.name.endswith("_test.py"):
            h.update(f.name.encode() + b"\0" + f.read_bytes() + b"\0")
    return h.hexdigest()[:16]


# ---------------------------------------------------------------- manifest and machine

def load_manifest(kind: str) -> dict:
    d = KINDS[kind]
    p = d / "manifest.json"
    if not p.exists():
        raise Refused(f"no {kind} manifest at {p}")
    pin = MANIFEST_SHA256[kind]
    got = sha256_file(p)
    if got != pin and not os.environ.get("ENGINEBENCH_DESIGN"):
        raise Refused(f"{p} is not the pinned one (sha256 {got[:16]}, pinned {pin[:16]})")
    m = json.loads(p.read_text())
    m["dir"] = str(d)
    return m


def check_reference_files(m: dict):
    d = Path(m["dir"])
    for sc in m["scenarios"]:
        r = d / "ref" / sc["id"]
        for name, key in (("hashes.txt", "hashes_sha256"), ("replay.bc26", "replay_sha256")):
            if sha256_file(r / name) != sc["ref"][key]:
                raise Refused(f"reference {r / name} does not match the manifest")
    for f, h in m["maps"].items():
        if sha256_file(d / "maps" / f) != h:
            raise Refused(f"map {f} does not match the manifest")
    for pkg, h in m["classes"].items():
        if package_hash(Path(m["classes_dir"]) / pkg) != h:
            raise Refused(f"classes {pkg} in {m['classes_dir']} do not match the manifest")


def package_hash(root: Path) -> str:
    h = hashlib.sha256()
    for f in sorted(root.rglob("*")):
        if f.is_file():
            h.update(str(f.relative_to(root)).encode() + b"\0" + f.read_bytes() + b"\0")
    return h.hexdigest()[:16]


def machine() -> dict:
    cpu = ""
    for ln in Path("/proc/cpuinfo").read_text().splitlines():
        if ln.startswith("model name"):
            cpu = ln.split(":", 1)[1].strip()
            break
    java = subprocess.run(["java", "-version"], capture_output=True, text=True).stderr.strip()
    jar = ENGINE_DATA / "jars" / f"battlecode26-java-{ENGINE_VERSION}.jar"
    return {"cpu": cpu, "java": java, "jar_sha256": sha256_file(jar) if jar.exists() else "missing"}


def check_machine(m: dict):
    here = machine()
    for k in ("cpu", "java", "jar_sha256"):
        if here[k] != m["recorded"][k]:
            raise Refused(f"the reference was recorded with {k} = {m['recorded'][k]!r}; here it is {here[k]!r}: "
                          f"HotSpot's Math stubs and the translated JDK make games machine-specific (AGENTS.md)")


# ---------------------------------------------------------------- the checkout

def check_pinned(repo: Path) -> str:
    """The eval in the checkout must be main's. Returns a note for the header."""
    if os.environ.get("ENGINEBENCH_DESIGN"):
        return "unpinned (ENGINEBENCH_DESIGN=1)"
    tree = sh(["git", "ls-tree", "-r", "origin/main", "--", "tools/enginebench"], cwd=repo, check=False)
    want = {}
    for ln in tree.splitlines():
        meta, path = ln.split("\t", 1)
        want[path] = meta.split()[2]
    if not want:
        raise Refused("origin/main has no tools/enginebench: nothing to pin against")
    have = {}
    for f in sorted((repo / "tools" / "enginebench").rglob("*")):
        if f.is_file() and "__pycache__" not in f.parts:
            rel = str(f.relative_to(repo))
            have[rel] = sh(["git", "hash-object", str(f)], cwd=repo).strip()
    if have != want:
        diff = sorted(set(have) ^ set(want)) + sorted(k for k in set(have) & set(want) if have[k] != want[k])
        raise Refused(f"tools/enginebench differs from origin/main's ({', '.join(diff)}): the eval is pinned")
    return "pinned to origin/main"


def code_under_test(repo: Path) -> list[Path]:
    """engine/'s files that go into the binary or its build (not its tests, not its docs)."""
    out = []
    for f in (repo / "engine").rglob("*"):
        rel = f.relative_to(repo / "engine")
        if not f.is_file() or rel.parts[0] in ("test", "build") or f.suffix == ".md" or "__pycache__" in rel.parts:
            continue
        out.append(f)
    return sorted(out)


def check_names(repo: Path, scenarios: list) -> list[str]:
    """Reasons the code under test is INVALID for naming the eval or a scenario."""
    reasons = []
    maps = sorted({sc["spec"]["map"] for sc in scenarios})
    seeds = sorted({str(sc["spec"]["seed"]) for sc in scenarios if sc["spec"].get("seed", 0) >= 100000})
    lit = re.compile("|".join([re.escape(n) for n in EVAL_NAMES] +
                              [re.escape(f'"{m}"') + "|" + re.escape(f'"{m}.map26"') for m in maps] +
                              [r"\b" + s + r"\b" for s in seeds]))
    main_files = set(sh(["git", "ls-tree", "-r", "--name-only", "origin/main", "--", "engine"], cwd=repo,
                        check=False).split("\n"))
    for f in code_under_test(repo):
        try:
            text = f.read_text(errors="replace")
        except OSError:
            continue
        rel = str(f.relative_to(repo))
        base = ""
        if rel in main_files:
            base = sh(["git", "show", f"origin/main:{rel}"], cwd=repo, check=False)
        hits = [(i, mt.group(0)) for i, ln in enumerate(text.splitlines(), 1) for mt in lit.finditer(ln)
                if mt.group(0) not in base]
        if hits:
            i, name = hits[0]
            reasons.append(f"{rel}:{i} names {name!r} (the eval or a scenario): code under test must not")
    return reasons


def check_portable(repo: Path, ninja: Path | None) -> list[str]:
    reasons = []
    if ninja is None:
        return ["cannot find the build's build.ninja to check its compiler flags"]
    for ln in ninja.read_text().splitlines():
        if not ln.startswith("build "):
            mt = ISA_FLAG.search(ln)
            if mt:
                reasons.append(f"build flag {mt.group(0)} ties the binary to a CPU or to inexact math "
                               f"(the binary must run on any x86-64-v2 machine)")
    for f in code_under_test(repo):
        if f.suffix in (".c", ".cc", ".cpp", ".h", ".hpp", ".py", ".java", ".txt"):
            mt = ISA_SOURCE.search(f.read_text(errors="replace"))
            if mt:
                reasons.append(f"{f.relative_to(repo)}: {mt.group(0)!r}: CPU-specific code")
    return reasons


def find_ninja(binary: Path) -> Path | None:
    best = None
    for p in (ENGINE_DATA / "build").glob(f"gen-{ENGINE_VERSION}-*/build.ninja"):
        try:
            text = p.read_text()
        except OSError:
            continue
        if f"build {binary}: link" in text and (best is None or p.stat().st_mtime > best.stat().st_mtime):
            best = p
    return best


def build(repo: Path, jobs: int, log: Path) -> tuple[Path, float]:
    """Runs the checkout's engine/build.py for the pinned bots; returns the binary and the seconds it took."""
    missing = [b for b in BOTS if not (repo / "build" / "classes" / b / "RobotPlayer.class").exists()]
    if missing:
        raise Refused(f"build/classes lacks {', '.join(missing)}: run ./gradlew classes")
    t = time.time()
    with open(log, "w") as f:
        r = subprocess.run([sys.executable, "engine/build.py", "--engine", ENGINE_VERSION, "--bots", ",".join(BOTS),
                            "-j", str(jobs)], cwd=repo, stdout=f, stderr=subprocess.STDOUT)
    if r.returncode != 0:
        tail = log.read_text().splitlines()[-15:]
        raise Refused("engine/build.py failed:\n  " + "\n  ".join(tail))
    binary = repo / "engine" / "build" / "bcx"
    if not binary.exists():
        raise Refused(f"engine/build.py made no {binary}")
    return binary, time.time() - t


# ---------------------------------------------------------------- playing a scenario

def runner() -> Path:
    """runner.c, compiled static once per source (the first process in the sandbox: no address randomization,
    the peak RSS of the whole tree; address randomization moves the conservative collector's work, 1.6e-4 of the
    count from run to run with it, 1e-8 without)."""
    src = HERE / "runner.c"
    exe = DATA / "bin" / f"runner-{hashlib.sha256(src.read_bytes()).hexdigest()[:16]}"
    if not exe.exists():
        exe.parent.mkdir(parents=True, exist_ok=True)
        tmp = exe.with_suffix(f".tmp{os.getpid()}")
        sh(["clang", "-static", "-O2", "-o", str(tmp), str(src)])
        tmp.rename(exe)
    return exe


def sandbox_cmd(binary: Path, root: Path, classes: Path, out: Path, tmp: Path, home: Path, env: dict, fd: int) -> list:
    """The game sees only its inputs (read-only) and three empty writable folders, no network, no environment.
    The runner reports to FD."""
    if str(binary).startswith(("/tmp/", "/bench/", "/home/bench/", "/proc/", "/dev/")):
        raise Refused(f"the binary {binary} would sit under one of the sandbox's own mounts")
    cmd = ["bwrap", "--unshare-all", "--die-with-parent", "--new-session", "--clearenv",
           "--setenv", "HOME", "/home/bench", "--setenv", "BCX_BUNDLED_MAPS", "/bench/root/maps"]
    for k, v in env.items():
        cmd += ["--setenv", k, v]
    cmd += ["--ro-bind", str(runner()), "/bench/runner", "--ro-bind", str(binary), str(binary),
            "--ro-bind", str(root / "maps"), "/bench/root/maps", "--ro-bind", str(classes), "/bench/classes",
            "--bind", str(out), "/bench/out", "--bind", str(tmp), "/tmp", "--bind", str(home), "/home/bench",
            "--proc", "/proc", "--dev", "/dev", "--chdir", "/bench/out",
            "/bench/runner", str(fd), str(binary), "play", "/bench/out/spec.jsonl", "/bench/out/games", "-j", "1",
            "--no-indicators", "--root", "/bench/root"]
    return cmd


def play(sc: dict, binary: Path, m: dict, rundir: Path, env: dict, timeout: float, perf: list | None = None) -> dict:
    """Plays one scenario; returns its reading: instructions, peak RSS, wall, the output dir."""
    d = rundir / sc["id"]
    shutil.rmtree(d, ignore_errors=True)
    out, tmp, home = d / "out", d / "tmp", d / "home"
    for x in (out, tmp, home):
        x.mkdir(parents=True)
    spec = dict(sc["spec"], classes="/bench/classes", indicators=False, replay=True)
    (out / "spec.jsonl").write_text(json.dumps(spec) + "\n")
    stat = d / "perf.txt"
    pre = perf or ["perf", "stat", "-x", ",", "-e", "instructions:u,instructions:k,task-clock", "-o", str(stat), "--"]
    rfd, wfd = os.pipe()
    cmd = pre + sandbox_cmd(binary, Path(m["dir"]), Path(m["classes_dir"]), out, tmp, home, env, wfd)
    t = time.time()
    with open(d / "stdout.txt", "w") as so, open(d / "stderr.txt", "w") as se:
        p = subprocess.Popen(cmd, stdout=so, stderr=se, pass_fds=(wfd,))
        os.close(wfd)
        killed = False
        while True:
            pid, status, ru = os.wait4(p.pid, os.WNOHANG)
            if pid:
                break
            if time.time() - t > timeout and not killed:
                p.kill()
                killed = True
            time.sleep(0.05)
    wall = time.time() - t
    with os.fdopen(rfd) as f:
        reading = f.read().split()
    r = {"id": sc["id"], "wall_s": round(wall, 2), "rss_kb": None, "exit": None, "dir": str(d), "timeout": killed}
    if len(reading) == 2:
        r["rss_kb"], r["exit"] = int(reading[0]), int(reading[1])
    if perf is None:
        r.update(read_perf(stat))
    return r


def read_perf(p: Path) -> dict:
    """instructions:u and :k, and task-clock (the tree's CPU seconds, a diagnostic)."""
    got = {}
    try:
        for ln in p.read_text().splitlines():
            f = ln.split(",")
            if len(f) > 2 and f[2] in ("instructions:u", "instructions:k"):
                got["instr_" + f[2][-1]] = int(f[0]) if f[0].isdigit() else None
            elif len(f) > 2 and f[2] == "task-clock":
                try:
                    got["cpu_s"] = round(float(f[0]) / 1000, 2)
                except ValueError:
                    pass
    except OSError:
        pass
    return got


def first_diff_round(a: list, b: list) -> str:
    for i in range(min(len(a), len(b))):
        if a[i] != b[i]:
            return f"first differs at round {a[i].split()[0] if a[i].split() else i + 1}"
    return f"{len(a)} rounds against the reference's {len(b)}"


def validate(sc: dict, r: dict, m: dict) -> list[str]:
    """Every reason this reading is INVALID, each saying why."""
    reasons = []
    if r.get("timeout"):
        reasons.append(f"timeout: killed after {r['wall_s']} s")
    out = Path(r["dir"]) / "out"
    g = out / "games" / sc["id"]
    ref = Path(m["dir"]) / "ref" / sc["id"]
    if r.get("instr_u") is None:
        reasons.append("perf counted no instructions:u")
    if r.get("rss_kb") is None:
        reasons.append(f"no reading from the sandbox's runner (see {r['dir']}/stderr.txt)")
    try:
        res = json.loads((g / "result.json").read_text())
    except (OSError, ValueError):
        res = None
        reasons.append(f"no result.json (exit {r['exit']}; see {r['dir']}/stderr.txt)")
    if res is not None and "error" in res:
        reasons.append(f"the game ended in an error: {res['error']}")
    elif res is not None:
        want = sc["ref"]["result"]
        bad = [k for k in sorted(want) if k not in NOT_COMPARED and res.get(k) != want[k]]
        if bad:
            k = bad[0]
            reasons.append(f"result differs from the official engine's in {', '.join(bad)} "
                           f"(e.g. {k}: {res.get(k)!r} against {want[k]!r})")
    if r.get("exit") not in (0, None) and res is not None and "error" not in res:
        reasons.append(f"bcx exited {r['exit']}")
    hp = g / "hashes.txt"
    if not hp.exists():
        reasons.append("no hashes.txt")
    elif sha256_file(hp) != sc["ref"]["hashes_sha256"]:
        a = hp.read_text().splitlines()
        b = (ref / "hashes.txt").read_text().splitlines()
        reasons.append(f"state hashes differ from the official engine's: {first_diff_round(a, b)}")
    rp = g / "replay.bc26"
    if not rp.exists():
        reasons.append("no replay.bc26")
    elif sha256_file(rp) != sc["ref"]["replay_sha256"]:
        reasons.append("replay differs from the official engine's (bytecodes, actions or bytes): " + replay_diff(rp, ref / "replay.bc26"))
    cap = sc.get("rss_cap_kb")
    if cap and r.get("rss_kb") is not None and r["rss_kb"] > cap:
        reasons.append(f"peak RSS {r['rss_kb'] / 1024:.0f} MB over the cap {cap / 1024:.0f} MB "
                       f"({RSS_FACTOR}x main's at design): memory traded for instructions")
    stray = []
    for x in sorted(out.rglob("*")):
        rel = x.relative_to(out)
        expected = str(rel) in ("spec.jsonl", "games", f"games/{sc['id']}") or (
            len(rel.parts) == 3 and rel.parts[:2] == ("games", sc["id"]) and rel.parts[2] in OUTPUT_FILES)
        if not expected or (x.is_dir() != (len(rel.parts) < 3 and str(rel) != "spec.jsonl")):
            stray.append(str(rel))
    for name in ("tmp", "home"):
        for x in sorted((Path(r["dir"]) / name).rglob("*")):
            stray.append(f"{name}/{x.relative_to(Path(r['dir']) / name)}")
    if stray:
        reasons.append(f"wrote outside its outputs: {', '.join(stray[:5])} (a game keeps nothing between runs)")
    return reasons


def replay_diff(a: Path, b: Path) -> str:
    try:
        x, y = gzip.decompress(a.read_bytes()), gzip.decompress(b.read_bytes())
    except (OSError, EOFError, gzip.BadGzipFile) as e:
        return f"cannot gunzip ({e})"
    if x == y:
        return "the gzip container differs, the flatbuffer is the same"
    i = next((k for k in range(min(len(x), len(y))) if x[k] != y[k]), min(len(x), len(y)))
    return (f"gunzipped {len(x)} bytes against {len(y)}, first differing at offset {i} "
            f"(decode: engine/test/replay.py)")


# ---------------------------------------------------------------- scoring

def score_of(rows: list, scenarios: list) -> float | None:
    by = {r["id"]: r for r in rows}
    total = 0.0
    for sc in scenarios:
        r = by.get(sc["id"])
        if r is None or r.get("instr_u") is None:
            return None
        total += sc["weight"] * r["instr_u"]
    return total / 1e9


def run_all(scenarios: list, m: dict, jobs: int, binary: Path, rundir: Path) -> list:
    """Plays the scenarios, longest first, JOBS at once; the validity-only ones (weight 0) with
    BCX_CHECK_FASTPATH=1, so a fast path's differential check runs on them."""
    timeout_of = lambda sc: max(300.0, 10 * sc.get("main_wall_s", 60))  # noqa: E731
    env_of = lambda sc: {"BCX_CHECK_FASTPATH": "1"} if sc["weight"] == 0 else {}  # noqa: E731
    order = sorted(scenarios, key=lambda sc: -sc.get("main_wall_s", 0))
    with ThreadPoolExecutor(jobs) as ex:
        futs = {sc["id"]: ex.submit(play, sc, binary, m, rundir, env_of(sc), timeout_of(sc)) for sc in order}
        rows = []
        for sc in scenarios:
            r = futs[sc["id"]].result()
            r["invalid"] = validate(sc, r, m)
            rows.append(r)
    return rows


def main_cache_key(repo: Path, kind: str, m: dict) -> str:
    tree = sh(["git", "rev-parse", "origin/main:engine"], cwd=repo, check=False).strip() or "none"
    return hashlib.sha256(f"{tree}\n{kind}\n{MANIFEST_SHA256[kind]}\n{eval_hash()}\n{BOTS}".encode()).hexdigest()[:16]


def main_reading(repo: Path, kind: str, m: dict) -> dict | None:
    p = DATA / "main" / f"{kind}-{main_cache_key(repo, kind, m)}.json"
    try:
        return json.loads(p.read_text())
    except (OSError, ValueError):
        return None


def export_main(repo: Path) -> Path:
    """origin/main's engine/ in a fixed folder of the data dir, so it builds incrementally: a file whose bytes did
    not change is left alone (git archive stamps every file with the commit's time, which would rebuild all)."""
    dest = DATA / "main" / "checkout"
    commit = sh(["git", "rev-parse", "origin/main"], cwd=repo).strip()
    tar = subprocess.run(["git", "archive", "origin/main", "engine"], cwd=repo, capture_output=True, check=True).stdout
    seen = set()
    with tarfile.open(fileobj=io.BytesIO(tar)) as t:
        for mem in t.getmembers():
            if not mem.isfile():
                continue
            seen.add(mem.name)
            data = t.extractfile(mem).read()
            p = dest / mem.name
            if p.is_file() and p.read_bytes() == data:
                continue
            p.parent.mkdir(parents=True, exist_ok=True)
            p.write_bytes(data)
            p.chmod(mem.mode)
    for p in sorted((dest / "engine").rglob("*")):
        rel = str(p.relative_to(dest))
        if p.is_file() and not rel.startswith("engine/build/") and rel not in seen:
            p.unlink()
    (dest / "COMMIT").write_text(commit + "\n")
    return dest


def cmd_score(args) -> int:
    t0 = time.time()
    kind = "sealed" if args.sealed else "visible"
    if args.sealed and not os.environ.get("ENGINEBENCH_INTEGRATOR"):
        raise Refused("the sealed scenarios are scored by the integrator only (ENGINEBENCH_INTEGRATOR=1)")
    if args.sealed and args.only:
        raise Refused("--only is for visible scenarios")
    m = load_manifest(kind)
    check_reference_files(m)
    check_machine(m)
    target = DATA / "main" / "checkout" if args.main else REPO
    key = hashlib.sha256(str(target).encode()).hexdigest()[:8]
    rundir = DATA / "runs" / f"{kind}-{key}"
    rundir.mkdir(parents=True, exist_ok=True)
    lock = open(rundir / ".lock", "w")
    try:
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
    except OSError:
        raise Refused(f"another score of this checkout is running ({rundir})")
    if args.main:
        pin = "origin/main's engine"
        export_main(REPO)
        classes = target / "build" / "classes"
        if classes.is_symlink():
            classes.unlink()
        if not classes.exists():
            classes.parent.mkdir(parents=True, exist_ok=True)
            classes.symlink_to(Path(m["classes_dir"]))
        head = (target / "COMMIT").read_text().strip()[:7]
        dirty = False
    else:
        pin = check_pinned(REPO)
        head = sh(["git", "rev-parse", "--short", "HEAD"], cwd=target, check=False).strip()
        dirty = bool(sh(["git", "status", "--porcelain", "--", "engine"], cwd=target, check=False).strip())
    scenarios = m["scenarios"]
    if args.only:
        ids = args.only.split(",")
        unknown = [i for i in ids if i not in {sc["id"] for sc in scenarios}]
        if unknown:
            raise Refused(f"no visible scenario {', '.join(unknown)} (see scenarios)")
        scenarios = [sc for sc in scenarios if sc["id"] in ids]
    say = (lambda *a: None) if args.json else (lambda *a: print(*a, flush=True))
    say(f"enginebench score ({kind}): {target} ({head}{', engine/ uncommitted changes' if dirty else ''}); {pin}")
    invalid_all = check_names(target, m["scenarios"]) if not args.main else []
    build_s = 0.0
    if not invalid_all:
        binary, build_s = build(target, args.build_jobs, rundir / "build.log")
        invalid_all += check_portable(target, find_ninja(binary))
        say(f"build: {build_s:.1f} s, {binary} sha256 {sha256_file(binary)[:12]}")
    if invalid_all:  # the code itself is INVALID: no game would change that
        for reason in invalid_all:
            print(f"INVALID: {reason}")
        print(f"score: INVALID (no game played) [{time.time() - t0:.1f} s]")
        return 1
    t1 = time.time()
    rows = run_all(scenarios, m, args.jobs, binary, rundir)
    games_s = time.time() - t1
    score = score_of(rows, m["scenarios"]) if not args.only else None
    valid = not invalid_all and all(not r["invalid"] for r in rows)
    ref = main_reading(REPO, kind, m) if not args.main else None
    refrows = {r["id"]: r for r in ref["rows"]} if ref else {}
    sealed = kind == "sealed"
    out = {"kind": kind, "checkout": str(target), "head": head, "dirty": dirty, "valid": valid,
           "score": round(score, 6) if (score is not None and valid) else None,
           "score_if_valid": round(score, 6) if score is not None else None,
           "main_score": ref["score"] if ref else None, "unit": "G instructions:u per corpus game (lower is better)",
           "invalid": invalid_all, "build_s": round(build_s, 1), "games_s": round(games_s, 1),
           "wall_s": round(time.time() - t0, 1), "jobs": args.jobs, "only": args.only, "rows": rows,
           "instr_k": sum(r.get("instr_k") or 0 for r in rows), "cpu_s": round(sum(r.get("cpu_s") or 0 for r in rows), 1)}
    if not args.json:
        w = 8 if sealed else max(len(sc["id"]) for sc in scenarios)
        print(f"{'scenario':{w}s} {'stratum':9s} {'weight':>7s} {'Ginstr':>9s} {'main':>9s} {'ratio':>7s} "
              f"{'RSS MB':>7s} {'cap':>6s} {'cpu s':>6s}  status")
        for i, sc in enumerate(scenarios):
            r = rows[i]
            name = f"S{i + 1:02d}" if sealed else sc["id"]
            g = r.get("instr_u")
            mr = refrows.get(sc["id"], {}).get("instr_u")
            ratio = f"{g / mr:7.4f}" if g and mr else f"{'-':>7s}"
            print(f"{name:{w}s} {sc['stratum']:9s} {sc['weight']:7.4f} {g / 1e9 if g else float('nan'):9.4f} "
                  f"{mr / 1e9 if mr else float('nan'):9.4f} {ratio} {r['rss_kb'] / 1024:7.0f} "
                  f"{sc.get('rss_cap_kb', 0) / 1024:6.0f} {r.get('cpu_s') or 0:6.1f}  "
                  f"{'ok' if not r['invalid'] else 'INVALID'}")
        for i, r in enumerate(rows):
            for reason in r["invalid"]:
                print(f"INVALID {f'S{i + 1:02d}' if sealed else r['id']}: {reason}")
        if args.only:
            print(f"partial run ({len(rows)} of {len(m['scenarios'])} scenarios): no score; "
                  f"{'valid' if valid else 'INVALID'}")
        else:
            sline = f"{score:.4f}" if score is not None else "none"
            mline = f"main {ref['score']:.4f}" if ref else "main not cached (enginebench score --main)"
            if valid and ref and score:
                mline += f"; {ref['score'] / score:.4f}x main's speed"
            print(f"score: {sline if valid else 'INVALID (' + sline + ' if it were valid)'} G instructions:u per "
                  f"corpus game, lower is better ({mline})")
        print(f"not scored: kernel instructions {out['instr_k'] / 1e9:.3f} G, CPU {out['cpu_s']} s over these games")
        print(f"wall: {out['wall_s']} s (build {build_s:.1f} s, games {games_s:.1f} s at {args.jobs} at once)")
    else:
        print(json.dumps(out))
    (rundir / ("last-only.json" if args.only else "last.json")).write_text(json.dumps(out, indent=1) + "\n")
    if args.main and valid and not args.only:
        (DATA / "main").mkdir(parents=True, exist_ok=True)
        (DATA / "main" / f"{kind}-{main_cache_key(REPO, kind, m)}.json").write_text(json.dumps(out, indent=1) + "\n")
    return 0 if valid else 1


# ---------------------------------------------------------------- explain, scenarios

def cmd_explain(args) -> int:
    t0 = time.time()
    m = load_manifest("visible")
    key = hashlib.sha256(str(REPO).encode()).hexdigest()[:8]
    last = DATA / "runs" / f"visible-{key}" / "last.json"
    if args.profile:
        return profile(args.profile, m, last)
    try:
        s = json.loads(last.read_text())
    except (OSError, ValueError):
        raise Refused(f"no score of this checkout yet ({last}): run score first")
    ref = main_reading(REPO, "visible", m)
    refrows = {r["id"]: r for r in ref["rows"]} if ref else {}
    weights = {sc["id"]: sc for sc in m["scenarios"]}
    total = s["score_if_valid"] or 0
    print(f"last score of {s['checkout']} ({s['head']}): {s['score_if_valid']} G instr/game, "
          f"{'valid' if s['valid'] else 'INVALID'}; main {ref['score'] if ref else 'not cached'}")
    for reason in s["invalid"]:
        print(f"INVALID: {reason}")
    rows = sorted(s["rows"], key=lambda r: -(weights[r["id"]]["weight"] * (r.get("instr_u") or 0)))
    print(f"{'scenario':58s} {'share':>6s} {'w*Ginstr':>9s} {'Ginstr':>8s} {'vs main':>8s} {'k Ginstr':>8s} "
          f"{'rounds':>6s} {'tiles':>5s}")
    for r in rows:
        sc = weights[r["id"]]
        g = r.get("instr_u") or 0
        wg = sc["weight"] * g / 1e9
        mr = refrows.get(r["id"], {}).get("instr_u")
        print(f"{r['id']:58s} {wg / total if total else 0:6.1%} {wg:9.4f} {g / 1e9:8.3f} "
              f"{(f'{(g - mr) / 1e9:+8.3f}' if mr else '       -')} {(r.get('instr_k') or 0) / 1e9:8.3f} "
              f"{sc['ref']['result']['round']:6d} {sc['tiles']:5d}")
        for reason in r["invalid"]:
            print(f"    INVALID: {reason}")
    print(f"(per game: instructions:u of the whole sandboxed bcx play; 'share' of the score; "
          f"'vs main' in G instructions; explain --profile ID shows where one game's go) [{time.time() - t0:.2f} s]")
    return 0


def profile(sid: str, m: dict, last: Path) -> int:
    sc = next((x for x in m["scenarios"] if x["id"] == sid), None)
    if sc is None:
        raise Refused(f"no visible scenario {sid} (see scenarios)")
    binary = REPO / "engine" / "build" / "bcx"
    if not binary.exists():
        raise Refused("no engine/build/bcx: run score first")
    rundir = last.parent / "profile"
    rundir.mkdir(parents=True, exist_ok=True)
    data = rundir / "perf.data"
    r = play(sc, binary, m, rundir, {}, 1800, perf=["perf", "record", "-q", "-e", "instructions:u", "-c", "200003",
                                                     "-o", str(data), "--"])
    rep = subprocess.run(["perf", "report", "--stdio", "-i", str(data), "--no-children", "--sort", "sym",
                          "--percent-limit", "0.5"], capture_output=True, text=True).stdout
    lines = [ln for ln in rep.splitlines() if ln.strip() and not ln.startswith("#")]
    print(f"{sid}: instructions:u by function (sampled every 200003; built binary {binary}); "
          f"game wall {r['wall_s']} s")
    print("\n".join(lines[:40]))
    print(f"full report: perf report -i {data}")
    return 0


def cmd_scenarios(args) -> int:
    t0 = time.time()
    m = load_manifest("visible")
    ref = main_reading(REPO, "visible", m)
    refrows = {r["id"]: r for r in ref["rows"]} if ref else {}
    if args.json:
        print(json.dumps([{k: v for k, v in sc.items() if k != "ref"} | {"result": sc["ref"]["result"]}
                          for sc in m["scenarios"]], indent=1))
        return 0
    print(f"{len(m['scenarios'])} visible scenarios, engine {ENGINE_VERSION}, recorded on {m['recorded']['cpu']}; "
          f"population {m['population']} corpus games (non-fuzz) in "
          f"{len({s['stratum'] for s in m['scenarios'] if s['weight'] > 0})} strata")
    print(f"{'scenario':58s} {'stratum':9s} {'weight':>7s} {'map':>7s} {'rounds':>6s} {'win type':19s} "
          f"{'main G':>7s} {'cap MB':>6s}")
    for sc in m["scenarios"]:
        res = sc["ref"]["result"]
        mr = refrows.get(sc["id"], {}).get("instr_u")
        print(f"{sc['id']:58s} {sc['stratum']:9s} {sc['weight']:7.4f} {sc['size']:>7s} {res['round']:6d} "
              f"{res['win_type']:19s} {mr / 1e9 if mr else float('nan'):7.2f} {sc.get('rss_cap_kb', 0) / 1024:6.0f}")
    print("weight = the stratum's share of the corpus games / games drawn in it (fuzz: validity only, run with "
          f"BCX_CHECK_FASTPATH=1); map = width x height [{time.time() - t0:.2f} s]")
    return 0


# ---------------------------------------------------------------- designer: record and calibrate

def cmd_record(args) -> int:
    """Builds a manifest from a selection (entries with spec, stratum, weight) and the official engine's outputs
    of those specs (engine/play_java.py OUTDIR)."""
    if not os.environ.get("ENGINEBENCH_DESIGN"):
        raise Refused("record is for the eval's designer (ENGINEBENCH_DESIGN=1)")
    kind = args.kind
    d = KINDS[kind]
    sel = json.loads(Path(args.selection).read_text())
    entries = sel[kind]
    rec = Path(args.recorded)
    classes = Path(args.classes).resolve()
    (d / "ref").mkdir(parents=True, exist_ok=True)
    (d / "maps").mkdir(parents=True, exist_ok=True)
    dims = json.loads(Path(args.dims).read_text())
    scen, maps = [], {}
    for e in entries:
        s = e["spec"]
        sid = s["id"]
        g = rec / sid
        res = json.loads((g / "result.json").read_text())
        if "error" in res:
            raise Refused(f"{sid}: the reference run failed: {res['error']}")
        rd = d / "ref" / sid
        rd.mkdir(parents=True, exist_ok=True)
        for f in ("result.json", "hashes.txt", "replay.bc26"):
            shutil.copyfile(g / f, rd / f)
        mf = f"{s['map']}.map26"
        shutil.copyfile(REPO / "maps" / mf, d / "maps" / mf)
        maps[mf] = sha256_file(d / "maps" / mf)
        dm = dims[s["map"]]
        scen.append({"id": sid, "spec": {k: v for k, v in s.items() if k in ("id", "a", "b", "map", "seed")},
                     "stratum": e["stratum"], "weight": e["weight"], "stratum_games": e["stratum_games"],
                     "tiles": dm["width"] * dm["height"], "size": f"{dm['width']}x{dm['height']}",
                     "ref": {"result": {k: v for k, v in res.items() if k not in NOT_COMPARED},
                             "hashes_sha256": sha256_file(rd / "hashes.txt"),
                             "replay_sha256": sha256_file(rd / "replay.bc26")}})
    pkgs = sorted({sc["spec"][t] for sc in scen for t in ("a", "b")})
    man = {"kind": kind, "engine": ENGINE_VERSION, "population": sel["population"],
           "recorded": machine() | {"date": time.strftime("%Y-%m-%d"), "by": "engine/play_java.py (bcx.RefMain), "
                                    "indicators false, one fresh JVM a game"},
           "classes_dir": str(classes), "classes": {p: package_hash(classes / p) for p in pkgs},
           "maps": maps, "scenarios": scen}
    (d / "manifest.json").write_text(json.dumps(man, indent=1) + "\n")
    print(f"{kind}: {len(scen)} scenarios; manifest sha256 {sha256_file(d / 'manifest.json')}")
    return 0


def cmd_calibrate(args) -> int:
    """Sets each scenario's RSS cap and main's wall from the --main runs' last.json files (several runs: the max)."""
    if not os.environ.get("ENGINEBENCH_DESIGN"):
        raise Refused("calibrate is for the eval's designer (ENGINEBENCH_DESIGN=1)")
    d = KINDS[args.kind]
    man = json.loads((d / "manifest.json").read_text())
    runs = [json.loads(Path(p).read_text()) for p in args.runs]
    for sc in man["scenarios"]:
        rs = [r for run in runs for r in run["rows"] if r["id"] == sc["id"]]
        sc["rss_cap_kb"] = int(math.ceil(RSS_FACTOR * max(r["rss_kb"] for r in rs)))
        sc["main_wall_s"] = max(r["wall_s"] for r in rs)
    (d / "manifest.json").write_text(json.dumps(man, indent=1) + "\n")
    print(f"{args.kind}: manifest sha256 {sha256_file(d / 'manifest.json')}")
    return 0


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    s = sub.add_parser("score", help="build, play the scenarios, print the score")
    s.add_argument("-j", "--jobs", type=int, default=4, help="games at once (default 4; counts do not depend on it)")
    s.add_argument("--build-jobs", type=int, default=8, help="ninja jobs for the build (default 8)")
    s.add_argument("--json", action="store_true")
    s.add_argument("--main", action="store_true", help="score origin/main's engine and cache it")
    s.add_argument("--sealed", action="store_true", help="the sealed scenarios (integrator only)")
    s.add_argument("--only", metavar="ID[,ID]", help="play only these visible scenarios: their rows, no score")
    e = sub.add_parser("explain", help="the last score, worst first; --profile ID: one game under perf record")
    e.add_argument("--profile", metavar="ID")
    c = sub.add_parser("scenarios", help="what each visible scenario holds")
    c.add_argument("--json", action="store_true")
    r = sub.add_parser("record", help="designer: make a manifest from recorded reference runs")
    r.add_argument("kind", choices=list(KINDS))
    r.add_argument("selection")
    r.add_argument("recorded")
    r.add_argument("classes")
    r.add_argument("dims")
    k = sub.add_parser("calibrate", help="designer: RSS caps and main's wall from --main runs")
    k.add_argument("kind", choices=list(KINDS))
    k.add_argument("runs", nargs="+")
    args = ap.parse_args(argv)
    try:
        return {"score": cmd_score, "explain": cmd_explain, "scenarios": cmd_scenarios, "record": cmd_record,
                "calibrate": cmd_calibrate}[args.cmd](args)
    except Refused as ex:
        print(f"enginebench: refused: {ex}", file=sys.stderr)
        return 2
