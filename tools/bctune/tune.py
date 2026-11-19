"""tools/tune: tune challenger's numeric constants against awubot by measurement (tools/bctune/AGENTS.md).

A campaign is a set of variants of challenger that differ from a base commit only in src/challenger/Tune.java. Each
variant is a commit (refs/tune/CAMPAIGN/LABEL), plays a fixed-size `tools/games ab` test against awubot, paired with a
reference (main) on the same cells, and `fit` estimates each constant's effect on the win rate from all of them.
"""
import argparse
import json
import os
import random
import re
import subprocess
import sys
import tempfile
import threading
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TUNE_FILE = "src/challenger/Tune.java"
DATA = Path(os.environ.get("TUNE_DATA", "~/battlecode26-data/tune")).expanduser()
GAMES_DATA = Path(os.environ.get("BCGAMES_DATA", "~/battlecode26-data/games")).expanduser()
GAMES = ROOT / "tools" / "games"
POOLS_DIR = GAMES_DATA / "pools"
REF = "ref"  # the label of the reference's own runs in a campaign

LINE = re.compile(r"^(?P<head>\s*public static (?:final )?(?P<type>int|double) (?P<name>[A-Z][A-Z0-9_]*) = )"
                  r"(?P<value>-?[0-9.]+)(?P<tail>; // tune: (?P<step>[0-9.]+) \[(?P<lo>-?[0-9.]+), (?P<hi>-?[0-9.]+)\]"
                  r" (?P<meaning>.*))$")


class TuneError(Exception):
    pass


# ----------------------------------------------------------------------------------------------- Tune.java

def parse(text: str) -> dict:
    """NAME -> {type, value, step, lo, hi, meaning} for every `// tune:` line of a Tune.java."""
    out = {}
    for line in text.splitlines():
        m = LINE.match(line)
        if not m:
            if "// tune:" in line and not line.lstrip().startswith("//"):
                raise TuneError(f"a tune line that does not parse: {line.strip()}")
            continue
        num = int if m["type"] == "int" else float
        out[m["name"]] = {"type": m["type"], "value": num(m["value"]), "step": num(m["step"]), "lo": num(m["lo"]),
                          "hi": num(m["hi"]), "meaning": m["meaning"]}
    return out


def fmt(kind: str, v) -> str:
    return str(int(v)) if kind == "int" else repr(float(v))


def render(text: str, values: dict) -> str:
    """TEXT with the values of VALUES (NAME -> number) written in; every name must be a tune line, every value in
    its [MIN, MAX], ints whole."""
    params = parse(text)
    for name, v in values.items():
        if name not in params:
            raise TuneError(f"{name} is not a tuned constant of {TUNE_FILE}")
        p = params[name]
        if p["type"] == "int" and v != int(v):
            raise TuneError(f"{name} is an int: {v}")
        if not p["lo"] <= v <= p["hi"]:
            raise TuneError(f"{name} = {v} is outside [{p['lo']}, {p['hi']}]")
    lines = []
    for line in text.splitlines(keepends=True):
        m = LINE.match(line.rstrip("\n"))
        if m and m["name"] in values:
            line = m["head"] + fmt(m["type"], values[m["name"]]) + m["tail"] + ("\n" if line.endswith("\n") else "")
        lines.append(line)
    return "".join(lines)


# ------------------------------------------------------------------------------------------------------ git

def git(*args, env=None, inp: bytes | None = None) -> str:
    proc = subprocess.run(["git", "-C", str(ROOT), *args], capture_output=True, input=inp,
                          env=dict(os.environ, **(env or {})))
    if proc.returncode != 0:
        raise TuneError(f"git {' '.join(args)}: {proc.stderr.decode().strip()}")
    return proc.stdout.decode().strip()


def tune_text(rev: str) -> str:
    return git("show", f"{rev}:{TUNE_FILE}")


def make_commit(base: str, text: str, message: str, ref: str) -> str:
    """A commit whose parent is BASE and whose tree is BASE's with Tune.java = TEXT, at REF. The working tree and
    the index are never touched."""
    blob = git("hash-object", "-w", "--stdin", inp=text.encode())
    with tempfile.TemporaryDirectory(prefix="tune-") as tmp:
        env = {"GIT_INDEX_FILE": str(Path(tmp) / "index")}
        git("read-tree", base, env=env)
        git("update-index", "--cacheinfo", f"100644,{blob},{TUNE_FILE}", env=env)
        tree = git("write-tree", env=env)
    commit = git("commit-tree", tree, "-p", base, "-m", message,
                 env={"GIT_AUTHOR_DATE": "2000-01-01T00:00:00Z", "GIT_COMMITTER_DATE": "2000-01-01T00:00:00Z"})
    git("update-ref", ref, commit)
    return commit


# --------------------------------------------------------------------------------------------------- designs

def sylvester(n: int) -> list[list[int]]:
    h = [[1]]
    while len(h) < n:
        h = [r + r for r in h] + [r + [-v for v in r] for r in h]
    return h


def screen_design(k: int) -> list[list[int]]:
    """A two-level design for K factors in 2n runs, n the smallest power of two >= max(K, 4): the rows of Sylvester's
    H_n restricted to columns 1..K-1 with a +1 column appended, then the same rows negated (the fold-over). Every
    column is balanced and orthogonal to every other, and to every product of two columns (resolution IV): a main
    effect is not mixed with any two-constant interaction."""
    if k < 1:
        raise TuneError("a design needs at least one constant")
    n = 4
    while n < k:
        n *= 2
    top = [row[1:k] + [1] for row in sylvester(n)]
    return top + [[-v for v in row] for row in top]


# ------------------------------------------------------------------------------------------------- campaigns

def campaign_path(name: str) -> Path:
    return DATA / name / "campaign.json"


def load(name: str) -> dict:
    p = campaign_path(name)
    if not p.exists():
        raise TuneError(f"no campaign {name} ({p})")
    return json.loads(p.read_text())


def save(c: dict) -> None:
    p = campaign_path(c["name"])
    p.parent.mkdir(parents=True, exist_ok=True)
    tmp = p.with_suffix(".tmp")
    tmp.write_text(json.dumps(c, indent=1))
    os.replace(tmp, p)


def new_campaign(name: str, rev: str, kind: str) -> dict:
    if campaign_path(name).exists():
        raise TuneError(f"campaign {name} exists ({campaign_path(name)})")
    base = git("rev-parse", f"{rev}^{{commit}}")
    params = parse(tune_text(base))
    if not params:
        raise TuneError(f"no tune lines in {TUNE_FILE} at {rev}")
    return {"name": name, "kind": kind, "base": base, "created": time.time(), "params": params, "factors": [],
            "variants": [], "runs": []}


def add_variant(c: dict, label: str, values: dict, x: list | None = None, t: float | None = None) -> dict:
    if label == REF or any(v["label"] == label for v in c["variants"]):
        raise TuneError(f"label {label} is taken")
    base_text = tune_text(c["base"])
    changed = {k: v for k, v in values.items() if v != c["params"][k]["value"]}
    text = render(base_text, changed)
    desc = ", ".join(f"{k} {fmt(c['params'][k]['type'], v)}" for k, v in changed.items()) or "base"
    sha = make_commit(c["base"], text, f"tune {c['name']} {label}: {desc}", f"refs/tune/{c['name']}/{label}")
    v = {"label": label, "values": changed, "commit": sha, "x": x, "t": t}
    c["variants"].append(v)
    return v


def step(c: dict, name: str):
    """The campaign's step for NAME: its own (screen --steps), else Tune.java's."""
    return c.get("steps", {}).get(name, c["params"][name]["step"])


def coded_values(c: dict, x: list[float], scale: float = 1.0) -> dict:
    out = {}
    for name, xi in zip(c["factors"], x):
        p = c["params"][name]
        v = p["value"] + xi * step(c, name) * scale
        out[name] = int(round(v)) if p["type"] == "int" else round(v, 6)
    return out


def cmd_params(a) -> int:
    rev = a.rev or "HEAD"
    for name, p in parse(tune_text(rev)).items():
        print(f"{name:20s} {fmt(p['type'], p['value']):>8s}  step {fmt(p['type'], p['step']):>6s}  "
              f"[{fmt(p['type'], p['lo'])}, {fmt(p['type'], p['hi'])}]  {p['meaning']}")
    return 0


def cmd_screen(a) -> int:
    c = new_campaign(a.name, a.rev or "HEAD", "screen")
    names = [n.strip() for n in a.params.split(",") if n.strip()]
    unknown = [n for n in names if n not in c["params"]]
    if unknown or len(set(names)) != len(names):
        raise TuneError(f"unknown or repeated constants: {unknown or names}")
    c["factors"], c["scale"] = names, a.scale
    c["steps"] = {}
    for item in filter(None, (a.steps or "").split(",")):
        k, _, v = item.partition("=")
        if k not in names:
            raise TuneError(f"--steps names {k}, which is not among --params")
        c["steps"][k] = int(v) if c["params"][k]["type"] == "int" else float(v)
    design = screen_design(len(names))
    for i, x in enumerate(design):
        add_variant(c, f"v{i:02d}", coded_values(c, x, a.scale), x=x)
    if a.center:
        add_variant(c, "c", {}, x=[0] * len(names))
    save(c)
    print(f"campaign {a.name}: {len(design)} variants of {c['base'][:12]} over {len(names)} constants "
          f"({campaign_path(a.name)})")
    return 0


def cmd_points(a) -> int:
    c = new_campaign(a.name, a.rev or "HEAD", "points")
    for spec in a.point:
        label, _, body = spec.partition(":")
        label, _, t = label.partition("@")
        values = {}
        for item in filter(None, body.split(",")):
            k, _, v = item.partition("=")
            if k not in c["params"]:
                raise TuneError(f"{k} is not a tuned constant")
            values[k] = (int(v) if c["params"][k]["type"] == "int" else float(v))
        add_variant(c, label, values, t=float(t) if t else None)
    save(c)
    print(f"campaign {a.name}: {len(c['variants'])} variants of {c['base'][:12]}")
    return 0


def cmd_path(a) -> int:
    """Points along the steepest-ascent line of a screen's fit: each selected constant moves t steps times its effect
    over the largest selected effect."""
    src = load(a.screen)
    fit = fit_campaign(src, a.seed_base, a.pool, boot=a.boot)
    chosen = {e["name"]: e for e in fit["effects"] if abs(e["z"]) >= a.min_z}
    if not chosen:
        raise TuneError(f"no effect of {a.screen} reaches |z| >= {a.min_z}")
    top = max(abs(e["effect"]) for e in chosen.values())
    c = new_campaign(a.name, a.rev or src["base"], "path")
    c["factors"] = list(chosen)
    c["source"] = {"screen": a.screen, "min_z": a.min_z, "effects": {k: e["effect"] for k, e in chosen.items()}}
    for t in [float(s) for s in a.t.split(",")]:
        x = [t * chosen[n]["effect"] / top for n in c["factors"]]
        add_variant(c, f"t{t:g}", coded_values(c, x), x=x, t=t)
    save(c)
    for v in c["variants"]:
        print(f"{v['label']:6s} {v['values']}")
    return 0


# ------------------------------------------------------------------------------------------------------ runs

def run_dir(run_id: str) -> Path:
    return GAMES_DATA / "runs" / run_id


def run_status(run_id: str) -> str:
    try:
        return json.loads((run_dir(run_id) / "state.json").read_text()).get("status", "?")
    except (OSError, ValueError):
        return "starting"


def run_cells(run_id: str):
    """'running', or the cells a stopped run completed."""
    try:
        st = json.loads((run_dir(run_id) / "state.json").read_text())
    except (OSError, ValueError):
        return "running"
    return "running" if st.get("status") == "running" else st["tests"][0].get("cells", 0)


def pin_ref(ref: str) -> str:
    pkg, _, rev = ref.partition("@")
    if rev == "origin/main":
        git("fetch", "-q", "origin", "main")
    return f"{pkg}@{git('rev-parse', '--short=12', (rev or 'HEAD') + '^{commit}')}" if rev else ref


def cmd_play(a) -> int:
    c = load(a.name)
    # a campaign keeps the reference its first runs at these settings pinned (main moves between plays)
    same = [r["ref"] for r in c["runs"] if (r["pool"], r["seed_base"], r["cells"], r["engine"])
            == (a.pool, a.seed_base, a.cells, a.engine)]
    ref = same[0] if same and a.ref == "challenger@origin/main" else pin_ref(a.ref)
    key = {"pool": a.pool, "seed_base": a.seed_base, "cells": a.cells, "ref": ref, "engine": a.engine}
    # a variant is played again when its run stopped short of its cells (failed batches): the new run reads the
    # games already played from the cache
    todo = [v for v in c["variants"] if not any(r["variant"] == v["label"] and {k: r[k] for k in key} == key
                                                 and run_cells(r["id"]) in ("running", a.cells) for r in c["runs"])]
    if a.only:
        todo = [v for v in todo if v["label"] in a.only.split(",")]
    print(f"campaign {a.name}: {len(todo)} variants to play against awubot, ref {ref}, pool {a.pool}, "
          f"seed base {a.seed_base}, {a.cells} cells", flush=True)
    # The reference plays its cells once, in a plain run of its own, and the variants play without a reference: run
    # in parallel, each variant's paired run would play the reference's games again until the cache held them.
    if todo and not any(r["variant"] == REF and {k: r[k] for k in key} == key
                        and run_cells(r["id"]) in ("running", a.cells) for r in c["runs"]):
        todo = [{"label": REF, "bot": ref}] + todo
    lock = threading.Lock()

    def one(v: dict) -> bool:
        """V built and its test started (each try a build, then tools/games ab); False when every try failed."""
        bot = v.get("bot") or f"challenger@{v['commit'][:12]}"
        cmd = [str(GAMES), "ab", bot, "awubot", "--ref", "none", "--pool", a.pool, "--seed-base", str(a.seed_base),
               "--min-cells", str(a.cells), "--max-cells", str(a.cells), "--engine", a.engine]
        if not a.local:
            cmd.append("--remote")
            if a.where:
                cmd += ["--where", a.where]
        rid = None
        for attempt in range(3):  # a transient error (the volume, the login node) should not end a campaign
            if attempt:
                time.sleep(30)
            if not a.local:
                b = subprocess.run([str(GAMES), "build", bot, "--engine", a.engine, "--remote"], capture_output=True,
                                   text=True)
                if b.returncode != 0:
                    print(f"{v['label']}: build failed (try {attempt + 1}):\n{b.stderr[-1500:]}", file=sys.stderr)
                    continue
            p = subprocess.run(cmd, capture_output=True, text=True)
            rid = p.stdout.strip().splitlines()[0] if p.returncode == 0 and p.stdout.strip() else None
            if rid:
                break
            print(f"{v['label']}: tools/games ab failed (try {attempt + 1}):\n{p.stderr[-1500:]}", file=sys.stderr)
        if not rid:
            return False
        with lock:
            c = load(a.name)  # another play may have written meanwhile
            c["runs"].append(dict(key, variant=v["label"], id=rid, started=time.time()))
            save(c)
            print(f"{v['label']}: {bot} run {rid}", flush=True)
        return True

    # BUILDS variants at once (the host builds two at once, one translating): a build's compile, link and its test's
    # start overlap the next one's translation. Each test starts as soon as its own build is done.
    with ThreadPoolExecutor(max(1, a.builds)) as pool:
        failed = sum(not ok for ok in pool.map(one, todo))
    if failed:
        print(f"{failed} variants not started: run play again", file=sys.stderr)
    return 1 if failed else 0


def run_line(r: dict) -> str:
    try:
        st = json.loads((run_dir(r["id"]) / "state.json").read_text())
        t = st["tests"][0]
        what = f"{t.get('cells', 0)} cells, score {t.get('score', float('nan')):.3f}"
    except (OSError, ValueError, KeyError, IndexError):
        what = "no state"
    return f"{r['variant']} {r['id']} [{run_status(r['id'])}] {what}"


def cmd_wait(a) -> int:
    """Every run of the campaign stopped; with --next, the next ones: those no earlier --next call reported (at once
    if some have stopped since), so a background `wait --next` wakes the agent at each result."""
    seen_file = DATA / a.name / "waited.json"
    seen = set(json.loads(seen_file.read_text())) if a.next and seen_file.exists() else set()
    while True:
        c = load(a.name)  # play may add runs meanwhile
        runs = [r for r in c["runs"] if a.seed_base is None or r["seed_base"] == a.seed_base]
        over = [r for r in runs if run_status(r["id"]) in ("done", "stopped", "error")]
        left = len(runs) - len(over)
        if a.next:
            new = [r for r in over if r["id"] not in seen]
            if new or not left:
                for r in new:
                    print(run_line(r))
                seen |= {r["id"] for r in new}
                seen_file.write_text(json.dumps(sorted(seen)))
                print(f"campaign {a.name}: {len(over)} of {len(runs)} runs stopped" + (
                    f"; {left} running: `tools/tune wait {a.name} --next` again" if left else ""))
                return 0
        elif not left:
            print(f"campaign {a.name}: {len(runs)} runs stopped")
            return 0
        time.sleep(a.poll)


def cmd_show(a) -> int:
    c = load(a.name)
    print(f"campaign {c['name']} ({c['kind']}), base {c['base'][:12]}, constants: {', '.join(c['factors']) or '-'}")
    for r in c["runs"]:
        if r["variant"] == REF:
            print(f"  {REF:6s} {r['ref']}  {r['id']} [{run_status(r['id'])}]")
    for v in c["variants"]:
        runs = [f"{r['id']} [{run_status(r['id'])}]" for r in c["runs"] if r["variant"] == v["label"]]
        vals = ", ".join(f"{k} {fmt(c['params'][k]['type'], x)}" for k, x in v["values"].items()) or "base"
        print(f"  {v['label']:6s} {v['commit'][:12]}  {vals}")
        for r in runs:
            print(f"         {r}")
    return 0


# ------------------------------------------------------------------------------------------------------- fit

def outcome(r: dict) -> tuple:
    """A game's result seat by seat, as tools/games compares games: equal outcomes = the same game."""
    return (r.get("winner"), r.get("win_type"), r.get("round"), json.dumps(r.get("A"), sort_keys=True),
            json.dumps(r.get("B"), sort_keys=True))


def metric_value(r: dict, metric: str) -> float:
    """The tested bot's value of METRIC in game R: win (1 or 0), or shareN, its share of the cheese transferred by
    both teams at round N (or at the game's end if sooner), a diagnostic (docs/theory/w1-classes.md §3.2)."""
    if metric == "win":
        return 1.0 if r["winner"] == r["seat"] else 0.0
    if metric.startswith("share"):
        s = r.get("series") or {}
        i = int(metric[5:]) // s.get("every", 100) - 1
        ours, theirs = (s.get("A"), s.get("B")) if r["seat"] == "A" else (s.get("B"), s.get("A"))
        if not ours or not theirs:
            return 0.5
        i = max(0, min(i, len(ours) - 1, len(theirs) - 1))
        a, b = ours[i][0], theirs[i][0]
        return a / (a + b) if a + b else 0.5
    raise TuneError(f"unknown metric {metric} (win, shareN)")


def cell_scores(run_id: str, metric: str = "win") -> tuple[dict, dict, dict, dict]:
    """(ours, ref, ours_outcomes, ref_outcomes): (map, seed) -> score of the cell (the metric's mean over its two
    games; win: games won / 2), for cells with both seats played; (map, seed, seat) -> outcome() of every game."""
    games, outs = {"o": {}, "r": {}}, {"o": {}, "r": {}}
    with open(run_dir(run_id) / "results.jsonl") as f:
        for line in f:
            r = json.loads(line)
            if r.get("error") or r.get("role") not in games or r.get("winner") not in ("A", "B"):
                continue
            # seat: the seat of the tested bot (ours, or the reference)
            games[r["role"]].setdefault((r["map"], r["seed"]), {})[r["seat"]] = metric_value(r, metric)
            outs[r["role"]][(r["map"], r["seed"], r["seat"])] = outcome(r)
    ours, ref = ({cell: (g["A"] + g["B"]) / 2 for cell, g in games[role].items() if len(g) == 2} for role in "or")
    return ours, ref, outs["o"], outs["r"]


def solve(m: list[list[float]], b: list[float]) -> list[float]:
    """Gaussian elimination with partial pivoting; m is square."""
    n = len(m)
    a = [row[:] + [b[i]] for i, row in enumerate(m)]
    for col in range(n):
        piv = max(range(col, n), key=lambda r: abs(a[r][col]))
        if abs(a[piv][col]) < 1e-12:
            raise TuneError("the design cannot separate these constants (singular)")
        a[col], a[piv] = a[piv], a[col]
        for r in range(n):
            if r != col and a[r][col]:
                f = a[r][col] / a[col][col]
                a[r] = [x - f * y for x, y in zip(a[r], a[col])]
    return [a[i][n] / a[i][i] for i in range(n)]


def projector(x: list[list[float]]) -> list[list[float]]:
    """P = (X'X)^-1 X' for X = [1 | x]: coefficients = P y."""
    X = [[1.0] + list(map(float, row)) for row in x]
    k = len(X[0])
    xtx = [[sum(r[i] * r[j] for r in X) for j in range(k)] for i in range(k)]
    cols = []
    for v in range(len(X)):
        cols.append(solve(xtx, X[v]))  # (X'X)^-1 x_v
    return [[cols[v][i] for v in range(len(X))] for i in range(k)]


def pool_maps(pool: str) -> set[str]:
    path = POOLS_DIR / f"{pool}.txt"
    if not path.exists():
        raise TuneError(f"no list pool {path}")
    return {ln.split("#")[0].strip().split(":")[0] for ln in path.read_text().splitlines()
            if ln.split("#")[0].strip()}


def fit_campaign(c: dict, seed_base: int | None, pool: str | None = None, boot: int = 2000,
                 maps: set[str] | None = None, exclude: bool = False, seed: int = 1, metric: str = "win",
                 drop: set[str] = frozenset(), vs: str | None = None) -> dict:
    runs = [r for r in c["runs"] if (seed_base is None or r["seed_base"] == seed_base)
            and (pool is None or r["pool"] == pool) and r["variant"] not in drop]
    if not runs:
        raise TuneError(f"campaign {c['name']}: no runs at seed base {seed_base}")
    keys = {(r["ref"], r["engine"]) for r in runs}
    if len(keys) > 1:
        raise TuneError(f"runs against different references or engines, pick with --seed-base/--pool: {sorted(keys)}")
    by_variant, ref, outs, ref_outs, ref_mismatch = {}, {}, {}, {}, 0
    for r in runs:
        ours, rs, o_out, r_out = cell_scores(r["id"], metric)
        if r["variant"] == REF:  # the reference's own plain run
            ours, rs, o_out, r_out = {}, ours, {}, o_out
        else:
            by_variant.setdefault(r["variant"], {}).update(ours)
            outs.setdefault(r["variant"], {}).update(o_out)
        ref_outs.update(r_out)
        for cell, s in rs.items():
            # the same game twice: equal unless two runs played it on CPUs whose Math stubs differ (engine/AGENTS.md)
            if ref.setdefault(cell, s) != s:
                ref_mismatch += 1
    if vs is not None:  # a variant is the reference: diffs pair every other variant with it
        if vs not in by_variant:
            raise TuneError(f"no runs of variant {vs}")
        ref, ref_outs = by_variant.pop(vs), outs.pop(vs)
    labels = [v["label"] for v in c["variants"] if v["label"] in by_variant]
    cells = set(ref)
    for lab in labels:
        cells &= set(by_variant[lab])
    if maps is not None:
        cells = {k for k in cells if (k[0] in maps) != exclude}
    cells = sorted(cells)
    if not cells:
        raise TuneError("no cell was played by every variant and the reference")
    mlist = sorted({m for m, _ in cells})
    midx = {m: i for i, m in enumerate(mlist)}
    nm = [0] * len(mlist)
    for m, _ in cells:
        nm[midx[m]] += 1
    sums = {lab: [0.0] * len(mlist) for lab in labels}
    rsum = [0.0] * len(mlist)
    for k in cells:
        i = midx[k[0]]
        rsum[i] += ref[k]
        for lab in labels:
            sums[lab][i] += by_variant[lab][k]
    variants = {v["label"]: v for v in c["variants"]}
    out = {"campaign": c["name"], "metric": metric, "cells": len(cells), "maps": len(mlist), "variants": [],
           "effects": [], "ref_mismatch": ref_mismatch,
           "seed_base": sorted({r["seed_base"] for r in runs}), "pool": sorted({r["pool"] for r in runs}),
           "ref_id": vs or runs[0]["ref"]}
    rng = random.Random(seed)
    draws = [[rng.randrange(len(mlist)) for _ in mlist] for _ in range(boot)]

    def ci(per_map: list[float]) -> tuple[float, float, float]:
        """(estimate, lo, hi): a per-map-summed statistic over the cell count, CI by resampling maps."""
        est = sum(per_map) / len(cells)
        bs = sorted(sum(per_map[j] for j in d) / sum(nm[j] for j in d) for d in draws)
        return est, bs[int(0.025 * boot)], bs[int(0.975 * boot) - 1]

    ref_score = sum(rsum) / len(cells)
    out["ref_score"] = ref_score
    for lab in labels:
        d, lo, hi = ci([s - r for s, r in zip(sums[lab], rsum)])
        out["variants"].append({"label": lab, "values": variants[lab]["values"], "t": variants[lab].get("t"),
                                "score": sum(sums[lab]) / len(cells), "diff": d, "lo": lo, "hi": hi,
                                "same": sum(1 for k, o in outs[lab].items() if ref_outs.get(k) == o)
                                / max(1, len(outs[lab]))})
    # the design's rows; a centre point (every x 0) is compared with them instead
    rows = [lab for lab in labels if variants[lab]["x"] is not None and any(variants[lab]["x"])]
    centres = [lab for lab in labels if variants[lab]["x"] is not None and not any(variants[lab]["x"])]
    if c["factors"] and len(rows) > len(c["factors"]):
        P = projector([variants[lab]["x"] for lab in rows])
        # coefficient i = sum_v P[i][v] * mean_v = per map: sum_v P[i][v] * sums[v][m], over the cell count
        for i, name in enumerate(["(intercept)"] + c["factors"]):
            per_map = [sum(P[i][v] * sums[rows[v]][j] for v in range(len(rows))) for j in range(len(mlist))]
            if i == 0:
                est, lo, hi = ci([p - r for p, r in zip(per_map, rsum)])
                out["curvature"] = {"est": est, "lo": lo, "hi": hi}
                for lab in centres:
                    out.setdefault("centre", {})[lab] = ci([s - p for s, p in zip(sums[lab], per_map)])
                continue
            est, lo, hi = ci(per_map)
            se = (hi - lo) / 3.92 or 1e-9
            p = c["params"][name]
            out["effects"].append({"name": name, "value": p["value"], "step": step(c, name), "effect": est,
                                   "lo": lo, "hi": hi, "z": est / se})
    on_path = [lab for lab in labels if variants[lab].get("t") is not None]
    if len({variants[lab]["t"] for lab in on_path}) >= 3:
        # score along the path (the variants with a position t): quadratic in t, with the reference at t = 0
        tx = [[0.0, 0.0]] + [[variants[lab]["t"], variants[lab]["t"] ** 2] for lab in on_path]
        P = projector(tx)
        coef = []
        for i in range(3):
            per_map = [P[i][0] * rsum[j] + sum(P[i][v + 1] * sums[on_path[v]][j] for v in range(len(on_path)))
                       for j in range(len(mlist))]
            coef.append(ci(per_map))
        b1, b2 = coef[1][0], coef[2][0]
        out["path"] = {"linear": coef[1], "quadratic": coef[2],
                       "t_best": (-b1 / (2 * b2)) if b2 < 0 else None}
    return out


def print_fit(f: dict, c: dict) -> None:
    print(f"campaign {f['campaign']} ({f['metric']}): {len(f['variants'])} variants, {f['cells']} cells on "
          f"{f['maps']} maps, pool {','.join(f['pool'])}, seed base {','.join(map(str, f['seed_base']))}; "
          f"ref {f['ref_id']} scores {f['ref_score']:.3f}")
    if f["ref_mismatch"]:
        print(f"  note: {f['ref_mismatch']} reference cells scored differently in two runs (the first is used)")
    print("\nvariants: score against awubot; diff = variant - ref on the same cells, 95% CI by map; same = share of "
          "its games with the ref's outcome")
    for v in sorted(f["variants"], key=lambda v: -v["diff"]):
        vals = ", ".join(f"{k} {fmt(c['params'][k]['type'], x)}" for k, x in v["values"].items()) or "base"
        print(f"  {v['label']:6s} {v['score']:.3f}  {v['diff']:+.3f} [{v['lo']:+.3f}, {v['hi']:+.3f}]  "
              f"same {v['same']:.2f}  {vals}")
    sames = [v["same"] for v in f["variants"]]
    print(f"  mean share of games equal to the ref's: {sum(sames) / len(sames):.2f}")
    if f["effects"]:
        cv = f["curvature"]
        print(f"\ncurvature (mean of the design's variants - ref): {cv['est']:+.3f} [{cv['lo']:+.3f}, {cv['hi']:+.3f}]")
        for lab, (est, lo, hi) in f.get("centre", {}).items():
            print(f"centre {lab} - mean of the design's variants: {est:+.3f} [{lo:+.3f}, {hi:+.3f}]")
        print("effects: score change for one step up (+step), 95% CI by map, z; worst-to-best by z")
        for e in sorted(f["effects"], key=lambda e: e["z"]):
            p = c["params"][e["name"]]
            print(f"  {e['name']:20s} {fmt(p['type'], e['value']):>6s} {'±' + fmt(p['type'], e['step']):>7s}  "
                  f"{e['effect']:+.4f} [{e['lo']:+.4f}, {e['hi']:+.4f}]  z {e['z']:+.2f}")
    if "path" in f:
        p = f["path"]
        print(f"\npath fit (score - ref = a t + b t^2): a {p['linear'][0]:+.4f} [{p['linear'][1]:+.4f}, "
              f"{p['linear'][2]:+.4f}], b {p['quadratic'][0]:+.4f} [{p['quadratic'][1]:+.4f}, "
              f"{p['quadratic'][2]:+.4f}]; best t {p['t_best'] if p['t_best'] is None else round(p['t_best'], 2)}")


def cmd_fit(a) -> int:
    c = load(a.name)
    maps, exclude = (pool_maps(a.split), False) if a.split else (None, False)
    parts = [("all", None, False)] + ([(a.split, maps, False), (f"not {a.split}", maps, True)] if a.split else [])
    fits = []
    for title, m, ex in parts:
        f = fit_campaign(c, a.seed_base, a.pool, a.boot, maps=m, exclude=ex, metric=a.metric,
                         drop=set(filter(None, (a.drop or "").split(","))), vs=a.vs)
        f["part"] = title
        fits.append(f)
        if not a.json:
            if len(parts) > 1:
                print(f"\n===== {title}")
            print_fit(f, c)
    if a.json:
        print(json.dumps(fits, indent=1))
    return 0


def cmd_apply(a) -> int:
    """Writes a variant's values into this checkout's Tune.java (to commit it on a branch)."""
    c = load(a.name)
    v = next((v for v in c["variants"] if v["label"] == a.label), None)
    if v is None:
        raise TuneError(f"no variant {a.label} in {a.name}")
    path = ROOT / TUNE_FILE
    path.write_text(render(path.read_text(), v["values"]))
    print(f"{TUNE_FILE}: " + (", ".join(f"{k} = {x}" for k, x in v["values"].items()) or "unchanged"))
    return 0


# ------------------------------------------------------------------------------------------------------- CLI

def main(argv=None) -> int:
    ap = argparse.ArgumentParser(prog="tools/tune", description=__doc__ + "\nGuide: tools/bctune/AGENTS.md.",
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    sp = sub.add_parser("params", help="the tuned constants of src/challenger/Tune.java")
    sp.add_argument("--rev", help="at this revision (default HEAD)")
    sp.set_defaults(fn=cmd_params)
    sp = sub.add_parser("screen", help="a two-level resolution-IV design: one variant commit per row")
    sp.add_argument("name")
    sp.add_argument("--params", required=True, help="comma-separated constants")
    sp.add_argument("--rev", help="the base (default HEAD)")
    sp.add_argument("--scale", type=float, default=1.0, help="half-swing in steps (default 1)")
    sp.add_argument("--steps", help="this campaign's steps instead of Tune.java's: NAME=STEP,...")
    sp.add_argument("--center", action="store_true", help="also a variant at the base values (label c)")
    sp.set_defaults(fn=cmd_screen)
    sp = sub.add_parser("points", help="variants at given values")
    sp.add_argument("name")
    sp.add_argument("--point", action="append", required=True, metavar="LABEL[@T]:NAME=V,NAME=V",
                    help="T: the point's position on a line from the reference (t = 0), for fit's parabola")
    sp.add_argument("--rev", help="the base (default HEAD)")
    sp.set_defaults(fn=cmd_points)
    sp = sub.add_parser("path", help="variants along a screen's steepest-ascent line")
    sp.add_argument("name")
    sp.add_argument("--screen", required=True)
    sp.add_argument("--seed-base", type=int, help="the screen's runs to fit (default: its only setting)")
    sp.add_argument("--pool")
    sp.add_argument("--min-z", type=float, default=2.0, help="constants whose effect reaches this |z| (default 2)")
    sp.add_argument("--t", default="0.5,1,1.5,2", help="positions, in steps of the largest effect")
    sp.add_argument("--rev", help="the base (default the screen's)")
    sp.add_argument("--boot", type=int, default=2000)
    sp.set_defaults(fn=cmd_path)
    sp = sub.add_parser("play", help="build the variants (two at once) and start each one's fixed-size test")
    sp.add_argument("name")
    sp.add_argument("--seed-base", type=int, required=True, help="a seed base no earlier test of these variants used")
    sp.add_argument("--pool", default="goal")
    sp.add_argument("--cells", type=int, default=285, help="cells a variant (default 285: 3 passes of goal)")
    sp.add_argument("--ref", default="challenger@origin/main", help="plays the same cells (pinned to its commit)")
    sp.add_argument("--engine", default="1.2.6")
    sp.add_argument("--local", action="store_true", help="play here (default: Imperial, --remote)")
    sp.add_argument("--where", help="Imperial backends (tools/games --where)")
    sp.add_argument("--only", help="comma-separated variant labels")
    sp.add_argument("--builds", type=int, default=2,
                    help="variants built at once (default 2, the host's build slots; translations go one at a time)")
    sp.set_defaults(fn=cmd_play)
    sp = sub.add_parser("wait", help="block until every run of the campaign has stopped, or with --next the next one "
                                     "(run it in the background)")
    sp.add_argument("name")
    sp.add_argument("--next", action="store_true",
                    help="return when a run stops that no earlier --next reported (at once if one has): its line")
    sp.add_argument("--seed-base", type=int)
    sp.add_argument("--poll", type=float, default=30)
    sp.set_defaults(fn=cmd_wait)
    sp = sub.add_parser("show", help="variants, values, commits and runs")
    sp.add_argument("name")
    sp.set_defaults(fn=cmd_show)
    sp = sub.add_parser("fit", help="each variant's score and each constant's effect, 95%% CIs by map")
    sp.add_argument("name")
    sp.add_argument("--seed-base", type=int)
    sp.add_argument("--pool")
    sp.add_argument("--split", metavar="LISTPOOL", help="also fit the cells on this list pool's maps and the rest")
    sp.add_argument("--metric", default="win", help="win (default, the objective) or shareN: our share of the cheese "
                    "transferred at round N, a diagnostic")
    sp.add_argument("--boot", type=int, default=2000)
    sp.add_argument("--drop", help="comma-separated variants to leave out (a failed run)")
    sp.add_argument("--vs", metavar="LABEL", help="pair the variants with this one instead of the reference")
    sp.add_argument("--json", action="store_true")
    sp.set_defaults(fn=cmd_fit)
    sp = sub.add_parser("apply", help="write a variant's values into this checkout's Tune.java")
    sp.add_argument("name")
    sp.add_argument("label")
    sp.set_defaults(fn=cmd_apply)
    a = ap.parse_args(argv)
    try:
        return a.fn(a)
    except TuneError as e:
        print(f"tools/tune: {e}", file=sys.stderr)
        return 2
