#!/usr/bin/env python3
"""Agreement of the native engine with the reference (engine/AGENTS.md, "Exactness").

    engine/test/agreement.py [--quick|--full] [--native CMD] [--java CMD] [-j N] [--specs FILE] [--filter RE] [--prune]

Plays the corpus (corpus.py; --specs FILE instead) on both backends and compares every game: the result (winner and
replay digest unless the win type is WON_BY_DUBIOUS_REASONS, win type, round, seed, hash, cat RNG draws, the team
counters and series), hashes.txt line by line (first differing round) and the gunzipped replay (first differing
offset, decoded to round and turn by replay.py).
Each backend is a command run from the repo root as `CMD SPECS.jsonl OUTDIR -j N`; it prints one result line per
spec in spec order and writes OUTDIR/<id>/{hashes.txt,replay.bc26}. A first word ending in .py runs with this Python.
The reference (--java, default engine/play_java.py) results are cached in --java-cache, keyed by the spec, the jar,
RefMain's sources (play_java.REFERENCE_SOURCES) and the class files of both packages: only new or changed games
are replayed.
Writes OUT/summary.json and OUT/rows.jsonl (one line a game: id, ok, the three comparisons, round, both cpu_s);
--prune deletes both backends' files of every game that agrees. Exit 0 only when every game agrees.
"""

import argparse
import gzip
import hashlib
import json
import re
import shlex
import shutil
import subprocess
import sys
import time
from pathlib import Path

TEST = Path(__file__).resolve().parent
ENGINE = TEST.parent
REPO = ENGINE.parent
sys.path.insert(0, str(TEST))
sys.path.insert(0, str(ENGINE))
import corpus  # noqa: E402
import play_java  # noqa: E402
import replay  # noqa: E402

NATIVE_DEFAULT = "engine/build/bcx play"
JAVA_DEFAULT = "engine/play_java.py"
CACHE_DEFAULT = play_java.DATA / "refruns" / "reference"
# RefMain errors that are the game's own outcome (the game could not be played), not the harness's
GAME_ERRORS = ("runMatch:", "server:", "no winner", "uncaught:")


def command(cmd: str) -> list:
    words = shlex.split(cmd)
    first = Path(words[0])
    if not first.is_absolute() and (REPO / first).exists():
        words[0] = str(REPO / first)
    if words[0].endswith(".py"):
        words = [sys.executable] + words
    return words


def run_backend(name: str, cmd: str, specs: list, outdir: Path, jobs: int) -> list:
    """Runs CMD on the specs; returns their results in spec order (stdout lines, else OUTDIR/<id>/result.json)."""
    outdir.mkdir(parents=True, exist_ok=True)
    words = command(cmd)
    if not Path(words[0]).exists() and "/" in words[0]:
        sys.exit(f"agreement: {name} backend not found: {words[0]} (pass --{name} CMD)")
    if not specs:
        return []
    spec_file = outdir / f"specs-{time.strftime('%Y%m%d-%H%M%S')}.jsonl"
    spec_file.write_text("".join(json.dumps(s) + "\n" for s in specs))
    print(f"agreement: {name}: {len(specs)} games: {' '.join(words)} {spec_file} {outdir} -j {jobs}", flush=True)
    t = time.time()
    p = subprocess.run(words + [str(spec_file), str(outdir), "-j", str(jobs)], cwd=REPO,
                       stdout=subprocess.PIPE, text=True)
    lines = []
    for ln in p.stdout.splitlines():
        try:
            r = json.loads(ln)
            if isinstance(r, dict):
                lines.append(r)
        except ValueError:
            pass
    print(f"agreement: {name}: done in {time.time() - t:.0f} s (exit {p.returncode})", flush=True)
    out = []
    for i, s in enumerate(specs):
        r = lines[i] if i < len(lines) and lines[i].get("id") == s.get("id") else None
        if r is None:
            try:
                r = json.loads((game_dir(outdir, s, i) / "result.json").read_text())
            except (OSError, ValueError):
                r = {"id": s.get("id"), "error": f"{name}: no result"}
        out.append(r)
    return out


def game_dir(outdir: Path, spec: dict, index: int) -> Path:
    return outdir / play_java.game_dir_name(spec, index)


_class_hashes = {}


def package_hash(classes: Path, pkg: str) -> str:
    key = (str(classes), pkg)
    if key not in _class_hashes:
        h = hashlib.sha256()
        root = classes / pkg
        for f in sorted(root.rglob("*")) if root.is_dir() else []:
            if f.is_file():
                h.update(str(f.relative_to(root)).encode() + b"\0" + f.read_bytes())
        _class_hashes[key] = h.hexdigest()
    return _class_hashes[key]


def fingerprint(spec: dict, jvm: str) -> str:
    engine = spec.get("engine", "1.2.6")
    jar = play_java.JARS / f"battlecode26-java-{engine}.jar"
    jar_id = f"{jar.name}:{jar.stat().st_size}" if jar.exists() else "missing"
    classes = REPO / spec.get("classes", "build/classes")
    ca = REPO / spec.get("classes_a", str(classes))
    cb = REPO / spec.get("classes_b", str(classes))
    parts = [json.dumps(spec, sort_keys=True), jar_id, jvm,
             package_hash(ca.resolve(), spec["a"]), package_hash(cb.resolve(), spec["b"])]
    return hashlib.sha256("\n".join(parts).encode()).hexdigest()


def reference(args, specs: list) -> list:
    """The java results, from the cache when the fingerprint matches."""
    cache = args.java_cache.resolve()
    jvm = play_java.reference_hash()
    fps = [fingerprint(s, jvm) for s in specs]
    todo = []
    for i, s in enumerate(specs):
        d = game_dir(cache, s, i)
        try:
            ok = (d / "fingerprint").read_text().strip() == fps[i] and (d / "result.json").exists()
        except OSError:
            ok = False
        if not ok or args.no_cache:
            todo.append(i)
    print(f"agreement: java: {len(specs) - len(todo)} games cached in {cache}, {len(todo)} to play", flush=True)
    fresh = run_backend("java", args.java, [specs[i] for i in todo], cache, args.jobs)
    for i, r in zip(todo, fresh):
        d = game_dir(cache, specs[i], i)
        if d.is_dir() and "error" not in r:
            (d / "fingerprint").write_text(fps[i] + "\n")
    out = []
    for i, s in enumerate(specs):
        try:
            out.append(json.loads((game_dir(cache, s, i) / "result.json").read_text()))
        except (OSError, ValueError):
            out.append({"id": s.get("id"), "error": "java: no result"})
    return out


def read_lines(p: Path):
    try:
        return p.read_text().splitlines()
    except OSError:
        return None


def read_replay(p: Path):
    try:
        return gzip.decompress(p.read_bytes())
    except OSError:
        return None


def compare(spec, rj, rn, dj: Path, dn: Path) -> dict:
    """One game: {'ok': bool, 'result': str, 'hashes': str, 'replay': str}."""
    row = {"ok": True, "result": "=", "hashes": "=", "replay": "="}
    ej, en = rj.get("error"), rn.get("error")
    if ej and not str(ej).startswith(GAME_ERRORS):
        return {"ok": False, "result": f"java error: {ej}", "hashes": "", "replay": ""}
    if ej or en:
        if ej and en:
            row["result"] = "both error"
        else:
            row["ok"] = False
            row["result"] = f"java error: {ej}" if ej else f"native error: {en}"
    else:
        diffs = []
        dubious = "WON_BY_DUBIOUS_REASONS" in (rj.get("win_type"), rn.get("win_type"))
        for f in ("win_type", "round", "seed", "hash", "cat_rng_draws", "cooperation", "A", "B", "series") + (
                () if dubious else ("winner", "digest")):
            if rj.get(f) != rn.get(f):
                diffs.append(f"{f} {rj.get(f)} vs {rn.get(f)}")
        if diffs:
            row["ok"] = False
            row["result"] = "; ".join(diffs)
        elif dubious:
            row["result"] = "= (coin flip)"

    hj, hn = read_lines(dj / "hashes.txt"), read_lines(dn / "hashes.txt")
    if hj is None or hn is None:
        if not (ej and en):
            row["ok"] = False
            row["hashes"] = f"missing ({'java' if hj is None else 'native'})"
    elif hj != hn:
        row["ok"] = False
        k = next((i for i in range(min(len(hj), len(hn))) if hj[i] != hn[i]), None)
        if k is None:
            row["hashes"] = f"{len(hj)} vs {len(hn)} rounds"
        else:
            row["hashes"] = f"first differs at round {hj[k].split()[0]}"
    else:
        row["hashes"] = f"= {len(hj)} rounds"

    if ej and en:
        row["replay"] = "-"
        return row
    xj, xn = read_replay(dj / "replay.bc26"), read_replay(dn / "replay.bc26")
    if xj is None or xn is None:
        row["ok"] = False
        row["replay"] = f"missing ({'java' if xj is None else 'native'})"
    else:
        if "WON_BY_DUBIOUS_REASONS" in (rj.get("win_type"), rn.get("win_type")):
            try:
                xj, xn = replay.without_winner(xj), replay.without_winner(xn)
            except Exception:  # undecodable: replay.compare reports it
                pass
        d = replay.compare(xj, xn)
        if d is not None:
            row["ok"] = False
            row["replay"] = d
        else:
            row["replay"] = f"= {len(xj)} bytes"
    return row


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    mode = ap.add_mutually_exclusive_group()
    mode.add_argument("--quick", action="store_true", help="the quick corpus (default)")
    mode.add_argument("--full", action="store_true", help="the full corpus")
    mode.add_argument("--specs", type=Path, help="a JSONL spec file instead of the corpus")
    ap.add_argument("--native", default=NATIVE_DEFAULT, help=f"native backend command (default: {NATIVE_DEFAULT})")
    ap.add_argument("--java", default=JAVA_DEFAULT, help=f"reference backend command (default: {JAVA_DEFAULT})")
    ap.add_argument("-j", "--jobs", type=int, default=3, help="games at once per backend (default 3)")
    ap.add_argument("--filter", help="only specs whose id matches this regex")
    ap.add_argument("--limit", type=int, help="only the first N specs")
    ap.add_argument("--out", type=Path, help="output dir (default refruns/agreement/<time>)")
    ap.add_argument("--java-cache", type=Path, default=CACHE_DEFAULT, help=f"reference results (default {CACHE_DEFAULT})")
    ap.add_argument("--no-cache", action="store_true", help="replay every reference game")
    ap.add_argument("--prune", action="store_true", help="delete the files of every game that agrees")
    args = ap.parse_args()

    if args.specs:
        specs = [json.loads(ln) for ln in args.specs.read_text().splitlines() if ln.strip()]
    else:
        specs = corpus.full() if args.full else corpus.quick()
    if args.filter:
        specs = [s for s in specs if re.search(args.filter, str(s.get("id", "")))]
    if args.limit is not None:
        specs = specs[:args.limit]
    if not specs:
        sys.exit("agreement: no specs")
    for i, s in enumerate(specs):
        s.setdefault("id", f"spec{i}")
    ids = [play_java.game_dir_name(s, i) for i, s in enumerate(specs)]
    if len(set(ids)) != len(ids):
        sys.exit("agreement: spec ids are not unique")
    native_words = command(args.native)
    if "/" in native_words[0] and not Path(native_words[0]).exists():
        sys.exit(f"agreement: native engine not built ({native_words[0]}); pass --native CMD")
    out = (args.out or play_java.DATA / "refruns" / "agreement" / time.strftime("%Y%m%d-%H%M%S")).resolve()
    out.mkdir(parents=True, exist_ok=True)
    (out / "specs.jsonl").write_text("".join(json.dumps(s) + "\n" for s in specs))
    print(f"agreement: {len(specs)} games, output in {out}", flush=True)

    rj = reference(args, specs)
    rn = run_backend("native", args.native, specs, out / "native", args.jobs)

    rows = []
    for i, s in enumerate(specs):
        dj, dn = game_dir(args.java_cache.resolve(), s, i), game_dir(out / "native", s, i)
        rows.append(compare(s, rj[i], rn[i], dj, dn))

    w = max(len(x) for x in ids)
    print()
    print(f"{'game':{w}s}  {'ok':4s} {'round':>5s} {'java s':>7s} {'native s':>8s}  result / hashes / replay")
    cj = cn = 0.0
    for i, s in enumerate(specs):
        r = rows[i]
        tj, tn = rj[i].get("cpu_s"), rn[i].get("cpu_s")
        if isinstance(tj, (int, float)) and isinstance(tn, (int, float)):
            cj += tj
            cn += tn
        print(f"{ids[i]:{w}s}  {'yes' if r['ok'] else 'NO':4s} {str(rj[i].get('round', '-')):>5s} "
              f"{str(tj if tj is not None else '-'):>7s} {str(tn if tn is not None else '-'):>8s}  "
              f"{r['result']} / {r['hashes']} / {r['replay']}")
    bad = sum(1 for r in rows if not r["ok"])
    print()
    print(f"agreement: {len(rows) - bad}/{len(rows)} games agree"
          + (f"; cpu java {cj:.1f} s, native {cn:.1f} s, ratio {cj / cn:.1f}x" if cn > 0 else ""))
    (out / "summary.json").write_text(json.dumps({"games": len(rows), "agree": len(rows) - bad, "rows": [
        {"id": ids[i], **rows[i], "java": rj[i], "native": rn[i]} for i in range(len(rows))]}, indent=1) + "\n")
    (out / "rows.jsonl").write_text("".join(json.dumps(
        {"id": ids[i], **rows[i], "round": rj[i].get("round"), "win_type": rj[i].get("win_type"),
         "java_cpu_s": rj[i].get("cpu_s"), "native_cpu_s": rn[i].get("cpu_s")}) + "\n" for i in range(len(rows))))
    if args.prune:
        for i, s in enumerate(specs):
            if rows[i]["ok"]:
                for d in (game_dir(args.java_cache.resolve(), s, i), game_dir(out / "native", s, i)):
                    shutil.rmtree(d, ignore_errors=True)
    sys.exit(0 if bad == 0 else 1)


if __name__ == "__main__":
    main()
