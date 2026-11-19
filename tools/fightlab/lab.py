"""The fight lab (docs/theory/fl-fight-lab.md): forks of real games at their fights, the micro changed from the fork on.

    tools/fightlab/lab.py scan NAME --pool goal --cells N --seed-base B     base games of flch against awubot, scanned
    tools/fightlab/lab.py forks NAME [--per-game K] [--window W]            forks chosen from the scan (state only)
    tools/fightlab/lab.py arms NAME SET --arm LABEL=NAME:V,NAME:V ... [--salts S] [--to-end]   fork jobs, submitted
    tools/fightlab/lab.py fetch NAME STAGE                                  results of a stage's run, once it ended
    tools/fightlab/lab.py score NAME SET [--ref null] [--metric M]          each arm against the reference arm

A stage (scan, or an arm set) is submitted with `tools/run --kit --detach` and prints its run id: wait for it with
`tools/compute wait RUN` in the background, then `fetch`. Data: ~/battlecode26-data/scratch/fl/lab/NAME/.

Every arm of a set plays every fork with each salt (S salts): flch with bc.testing.fl.r0 = the fork round and
bc.testing.fl.set = the arm's values and the salt. The reference arm "null" is main's values with the same salts, so
each arm's game and the null's share the fork's state, the salt and every decision until the arm's values first change
one. Run mkcopy.py and compile (the kit does: ./gradlew classes) before a stage; cls/ and lib/ ship with it.
"""
from __future__ import annotations

import argparse
import json
import math
import random
import subprocess
import sys
from collections import defaultdict
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / "tools"))
from bcgames import games as G  # noqa: E402
from bcgames import pools as P  # noqa: E402

DATA = Path.home() / "battlecode26-data" / "scratch" / "fl" / "lab"
LAB = "flch"
OPP = "awubot"
MARKS = (10, 20, 40, 80)
# a team's state (FightLab.state): babies, baby HP, kings, king HP, global, raw, transferred, HP lost, cat dmg, traps,
# dirt, [box babies, box baby HP, box kings]
BAB, BHP, KNG, KHP, GLB, RAW, XFR, LOST, CAT, TRP, DRT = range(11)


def d(name: str) -> Path:
    p = DATA / name
    p.mkdir(parents=True, exist_ok=True)
    return p


def submit(jobs: Path, chunk: int, label: str) -> str:
    cmd = ["tools/run", "--kit", "--detach", "--each", str(jobs), "--chunk", str(chunk), "--gather", "results.jsonl",
           "--with", "tools/fightlab/cls", "--with", "tools/fightlab/lib", "--task-mem", "1700M", "--",
           "bash", "tools/fightlab/task.sh", "{file}", "{out}"]
    p = subprocess.run(cmd, cwd=REPO, capture_output=True, text=True)
    if p.returncode != 0:
        raise SystemExit(f"tools/run failed ({p.returncode}):\n{p.stdout}{p.stderr}")
    run = p.stdout.split()[0]
    print(f"{label}: run {run} ({sum(1 for _ in open(jobs))} jobs); wait: tools/compute wait {run}")
    return run


def write_jobs(path: Path, jobs: list[dict]) -> None:
    with open(path, "w") as f:
        for j in jobs:
            f.write(json.dumps(j) + "\n")


def load(path: Path) -> list[dict]:
    return [json.loads(x) for x in open(path) if x.strip()]


# ------------------------------------------------------------------------------------------------------------- scan
def cmd_scan(a) -> None:
    out = d(a.name)
    entries, sealed = P.resolve(a.pool)
    if sealed:
        raise SystemExit("never the holdout")
    jobs = []
    for c in G.cells(entries, a.seed_base):
        if c["idx"] >= a.cells:
            break
        for seat in ("A", "B"):
            j = {"id": f"c{c['idx']:04d}{seat}", "cell": c["idx"], "seat": seat, "map": c["map"],
                 "a": LAB if seat == "A" else a.opp, "b": a.opp if seat == "A" else LAB, "scan": 1}
            if c["seed"] is not None:
                j["seed"] = c["seed"]
            jobs.append(j)
    write_jobs(out / "scan.jobs.jsonl", jobs)
    meta = {"pool": a.pool, "cells": a.cells, "seed_base": a.seed_base, "opp": a.opp}
    (out / "meta.json").write_text(json.dumps(meta))
    run = submit(out / "scan.jobs.jsonl", a.chunk, "scan")
    (out / "scan.run").write_text(run)


# ------------------------------------------------------------------------------------------------------------ forks
def ours(rec: dict, key: str, seat: str):
    """A result's state lists by our seat: (ours, theirs)."""
    s = rec[key]
    return (s["A"], s["B"]) if seat == "A" else (s["B"], s["A"])


def cmd_forks(a) -> None:
    out = d(a.name)
    rng = random.Random(f"forks:{a.name}")
    forks = []
    for r in load(out / "scan.results.jsonl"):
        if "error" in r:
            continue
        seat = r["seat"]
        won = r.get("winner") == seat
        cands = []
        for f in r.get("scan", []):
            rnd, x, y, nA, nB, hA, hB, kA, kB, gA, gB, tA, tB, start = f
            if rnd + 1 + a.window > r["round"]:
                continue  # the base game ends inside the window
            if a.max_r0 and rnd + 1 > a.max_r0:
                continue
            us = (nA, hA, kA, gA, tA) if seat == "A" else (nB, hB, kB, gB, tB)
            them = (nB, hB, kB, gB, tB) if seat == "A" else (nA, hA, kA, gA, tA)
            if us[0] == 0 or them[0] + them[2] == 0:
                continue
            cands.append({"round": rnd, "x": x, "y": y, "start": start, "us": us, "them": them})
        if not cands:
            continue
        # spread over the game: the first start, then a random K-1 of the rest
        pick = [cands[0]] + rng.sample(cands[1:], min(a.per_game - 1, len(cands) - 1))
        for c in sorted(pick, key=lambda c: c["round"]):
            forks.append({"fork": f"{r['id']}r{c['round']}", "game": r["id"], "cell": r["cell"], "seat": seat,
                          "map": r["map"], "seed": r.get("seed"), "a": r["a"], "b": r["b"], "r0": c["round"] + 1,
                          "cx": c["x"], "cy": c["y"], "start": c["start"], "us": c["us"], "them": c["them"],
                          "base_won": won, "base_round": r["round"], "base_win_type": r.get("win_type")})
    write_jobs(out / "forks.jsonl", forks)
    print(f"{len(forks)} forks from {len(set(f['game'] for f in forks))} games")


# ------------------------------------------------------------------------------------------------------------- arms
def cmd_arms(a) -> None:
    out = d(a.name) / a.set
    out.mkdir(exist_ok=True)
    forks = load(DATA / a.name / "forks.jsonl")
    if a.first_per_game:
        seen, keep = set(), []
        for f in forks:
            if f["game"] not in seen:
                seen.add(f["game"])
                keep.append(f)
        forks = keep
    if a.limit:
        forks = forks[:a.limit]
    arms = {"null": ""}
    for spec in a.arm:
        label, _, vals = spec.partition("=")
        arms[label] = ",".join(v.replace(":", "=") for v in vals.split(",") if v)
    salts = [1000 + i for i in range(a.salts)]
    jobs = []
    for f in forks:
        for label, vals in arms.items():
            for s in salts:
                st = (vals + "," if vals else "") + f"salt={s}"
                j = {"id": f"{f['fork']}.{label}.{s}", "fork": f["fork"], "arm": label, "salt": s, "seat": f["seat"],
                     "cell": f["cell"], "map": f["map"], "a": f["a"], "b": f["b"], "r0": f["r0"], "set": st,
                     "marks": sorted({f["r0"] - 1, *(f["r0"] + w for w in MARKS), *([a.at] if a.at > f["r0"] else [])}),
                     "cx": f["cx"], "cy": f["cy"], "stop": 0 if a.to_end else max(f["r0"] + MARKS[-1], a.at)}
                if f.get("seed") is not None:
                    j["seed"] = f["seed"]
                jobs.append(j)
    rng = random.Random(f"order:{a.name}:{a.set}")
    rng.shuffle(jobs)  # long and short games mixed in every task
    write_jobs(out / "jobs.jsonl", jobs)
    (out / "arms.json").write_text(json.dumps({"arms": arms, "salts": salts, "to_end": a.to_end}))
    run = submit(out / "jobs.jsonl", a.chunk, f"arms {a.set}")
    (out / "run").write_text(run)


def cmd_fetch(a) -> None:
    base = DATA / a.name
    stage = base / ("" if a.stage == "scan" else a.stage)
    run = (base / "scan.run" if a.stage == "scan" else stage / "run").read_text().strip()
    src = REPO / "runs" / run / "results.jsonl"
    if not src.exists():
        raise SystemExit(f"{src}: not there yet (tools/compute wait {run})")
    dst = base / "scan.results.jsonl" if a.stage == "scan" else stage / "results.jsonl"
    rows = load(src)
    dst.write_text("".join(json.dumps(r) + "\n" for r in rows))
    err = sum(1 for r in rows if "error" in r)
    print(f"{len(rows)} results ({err} errors) -> {dst}")


# ------------------------------------------------------------------------------------------------------------ score
WIN = ["rats", "babyHP", "kings", "kingHP", "cheese", "xfer", "HPexch"]  # the window's exchange, ours minus theirs
BASE = ["rats", "babyHP", "kings", "kingHP", "global", "xfer", "r0"]  # the fork's state, ours minus theirs


def feats(r: dict, w: int):
    """A game's (state at the fork, exchange to r0 + w, result or None), from our seat; None if it has no fork mark.
    HP in hundreds, cheese in thousands. The exchange: what we gained minus what they gained; HPexch: the HP they
    lost minus the HP we lost (any source)."""
    if "error" in r or not r.get("marks"):
        return None
    seat, r0 = r["seat"], r["r0"]
    ms = {m["r"]: m for m in r["marks"]}
    if r0 - 1 not in ms:
        return None
    def ut(m):
        return (m["A"], m["B"]) if seat == "A" else (m["B"], m["A"])
    u0, t0 = ut(ms[r0 - 1])
    u, t = ut(ms.get(r0 + w if w > 0 else -w) or r["end"])  # w <= 0: the absolute round -w
    base = [u0[BAB] - t0[BAB], (u0[BHP] - t0[BHP]) / 100, u0[KNG] - t0[KNG], (u0[KHP] - t0[KHP]) / 100,
            (u0[GLB] - t0[GLB]) / 1000, (u0[XFR] - t0[XFR]) / 1000, r0 / 1000]
    def g(i, x):
        return x[i] - (u0 if x is u else t0)[i]
    win = [g(BAB, u) - g(BAB, t), (g(BHP, u) - g(BHP, t)) / 100, g(KNG, u) - g(KNG, t), (g(KHP, u) - g(KHP, t)) / 100,
           ((u[GLB] + u[RAW] - u0[GLB] - u0[RAW]) - (t[GLB] + t[RAW] - t0[GLB] - t0[RAW])) / 1000,
           (g(XFR, u) - g(XFR, t)) / 1000, (g(LOST, t) - g(LOST, u)) / 100]
    won = (1.0 if r.get("winner") == seat else 0.0) if r.get("done") else None
    return base, win, won


def solve(m: list[list[float]], b: list[float]) -> list[float]:
    n = len(m)
    a = [row[:] + [b[i]] for i, row in enumerate(m)]
    for col in range(n):
        piv = max(range(col, n), key=lambda q: abs(a[q][col]))
        a[col], a[piv] = a[piv], a[col]
        for q in range(n):
            if q != col and a[q][col]:
                f = a[q][col] / a[col][col]
                a[q] = [x - f * y for x, y in zip(a[q], a[col])]
    return [a[i][n] / a[i][i] for i in range(n)]


def logit_fit(X: list[list[float]], y: list[float], l2: float = 1e-2) -> list[float]:
    k = len(X[0])
    beta = [0.0] * k
    for _ in range(60):
        g = [-l2 * bj for bj in beta]
        h = [[(-l2 if i == j else 0.0) for j in range(k)] for i in range(k)]
        for x, yi in zip(X, y):
            z = sum(bj * xj for bj, xj in zip(beta, x))
            p = 1 / (1 + math.exp(-max(-30.0, min(30.0, z))))
            for i in range(k):
                g[i] += (yi - p) * x[i]
                wi = p * (1 - p) * x[i]
                for j in range(k):
                    h[i][j] -= wi * x[j]
        step = solve(h, g)
        beta = [bj - sj for bj, sj in zip(beta, step)]
        if max(abs(sj) for sj in step) < 1e-9:
            break
    return beta


def prob(beta: list[float], f) -> float:
    x = [1.0] + f[0] + f[1]
    z = sum(bj * xj for bj, xj in zip(beta, x))
    return 1 / (1 + math.exp(-max(-30.0, min(30.0, z))))


def cmd_calibrate(a) -> None:
    """Fit P(win | the fork's state, the window's exchange) on sets played to the end."""
    rows = []
    for spec in a.sets:
        n, st = spec.split("/")
        rows += [f for f in (feats(r, a.mark) for r in load(DATA / n / st / "results.jsonl")) if f and f[2] is not None]
    X = [[1.0] + f[0] + f[1] for f in rows]
    y = [f[2] for f in rows]
    beta = logit_fit(X, y)
    out = {"mark": a.mark, "sets": a.sets, "games": len(rows), "beta": beta,
           "names": ["1"] + ["fork." + x for x in BASE] + ["win." + x for x in WIN]}
    (DATA / "calib.json").write_text(json.dumps(out, indent=1))
    print(f"{len(rows)} games (won {sum(y) / len(y):.3f}), mark +{a.mark}: " +
          " ".join(f"{nm}={bj:+.3f}" for nm, bj in zip(out["names"], beta)))


def boot_ci(xs_by_map: dict, n: int = 2000, seed: int = 1) -> tuple[float, float, float]:
    maps = list(xs_by_map)
    allx = [x for m in maps for x in xs_by_map[m]]
    mean = sum(allx) / len(allx)
    rng = random.Random(seed)
    bs = []
    for _ in range(n):
        s, c = 0.0, 0
        for _ in maps:
            v = xs_by_map[rng.choice(maps)]
            s += sum(v)
            c += len(v)
        bs.append(s / c)
    bs.sort()
    return mean, bs[int(0.025 * n)], bs[int(0.975 * n)]


def paired(name: str, set_: str, mark: int):
    """(fork, salt) -> arm -> {metric: value}; fork -> map. Metrics: the window's exchange, labP (calib.json's win
    probability), won (played to the end), same (the window's state equals the null's: the arm never acted)."""
    cal = json.loads((DATA / "calib.json").read_text()) if (DATA / "calib.json").exists() else None
    by, mp = defaultdict(dict), {}
    for r in load(DATA / name / set_ / "results.jsonl"):
        f = feats(r, mark)
        if f is None:
            continue
        m = {k: v for k, v in zip(WIN, f[1])}
        if cal and cal["mark"] == mark:
            m["labP"] = prob(cal["beta"], f)
        if f[2] is not None:
            m["won"] = f[2]
        m["_w"] = f[1]
        by[(r["fork"], r["salt"])][r["arm"]] = m
        mp[r["fork"]] = r["map"]
    return by, mp


def diffs(by, mp, arm: str, ref: str, key: str) -> dict:
    xs = defaultdict(list)
    for (fork, s), v in by.items():
        if arm in v and ref in v:
            if key == "same":
                xs[mp[fork]].append(1.0 if v[arm]["_w"] == v[ref]["_w"] else 0.0)
            elif key in v[arm] and key in v[ref]:
                xs[mp[fork]].append(v[arm][key] - v[ref][key])
    return xs


def cmd_score(a) -> None:
    by, mp = paired(a.name, a.set, a.mark)
    arms = sorted({arm for v in by.values() for arm in v})
    keys = a.metric.split(",")
    print(f"{a.name}/{a.set}: {len(by)} fork-salts, mark +{a.mark}; each arm minus {a.ref} (same fork, same salt), "
          "95% CI by map")
    for arm in arms:
        if arm == a.ref:
            continue
        line = [f"{arm:>12}"]
        for k in keys:
            xs = diffs(by, mp, arm, a.ref, k)
            if not xs:
                continue
            mean, lo, hi = boot_ci(xs)
            n = sum(len(x) for x in xs.values())
            line.append(f"{k} {mean:+.4f} [{lo:+.4f}, {hi:+.4f}]" if k in ("labP", "won", "same") else
                        f"{k} {mean:+.3f} [{lo:+.3f}, {hi:+.3f}]")
        print("  ".join(line) + f"  n{n}")


# ------------------------------------------------------------------------------------------------------------ screen
def cmd_screen(a) -> None:
    """A two-level resolution-IV design (tools/bctune's) over PARAMS around main's values, as lab arms."""
    sys.path.insert(0, str(REPO))
    from tools.bctune import tune as T
    params = T.parse((REPO / "src" / "challenger" / "Tune.java").read_text())
    names = [n for n in a.params.split(",") if n]
    steps = dict(kv.split("=") for kv in a.steps.split(",") if kv)
    design = T.screen_design(len(names))
    arms = []
    for i, row in enumerate(design):
        vals = []
        for nm, x in zip(names, row):
            p = params[nm]
            st = float(steps.get(nm, p["step"]))
            v = p["value"] + x * st
            vals.append(f"{nm}:{int(round(v)) if p['type'] == 'int' else round(v, 6)}")
        arms.append(f"v{i:02d}=" + ",".join(vals))
    meta = {"factors": names, "design": design, "steps": {n: float(steps.get(n, params[n]["step"])) for n in names},
            "values": {n: params[n]["value"] for n in names}}
    out = d(a.name) / a.set
    out.mkdir(exist_ok=True)
    (out / "design.json").write_text(json.dumps(meta, indent=1))
    a.arm = arms
    cmd_arms(a)


def cmd_fit(a) -> None:
    """Each factor's effect per +1 step on a metric, from a screen's arms against the null (orthogonal design)."""
    meta = json.loads((DATA / a.name / a.set / "design.json").read_text())
    by, mp = paired(a.name, a.set, a.mark)
    k = a.metric
    rows = meta["design"]
    per = []  # per variant: map -> [arm - null]
    for i in range(len(rows)):
        per.append(diffs(by, mp, f"v{i:02d}", "null", k))
    maps = sorted({m for p in per for m in p})
    def effects(sample_maps):
        ys = []
        for p in per:
            v = [x for m in sample_maps for x in p.get(m, [])]
            ys.append(sum(v) / len(v) if v else 0.0)
        n = len(rows)
        return [sum(rows[i][j] * ys[i] for i in range(n)) / n for j in range(len(meta["factors"]))], sum(ys) / n
    eff, centre = effects(maps)
    rng = random.Random(7)
    bs = [effects([rng.choice(maps) for _ in maps]) for _ in range(a.boot)]
    print(f"{a.name}/{a.set}: {len(rows)} variants, metric {k} at +{a.mark}; variants' mean - null {centre:+.4f}")
    order = sorted(range(len(eff)), key=lambda j: -abs(eff[j]))
    for j in order:
        b = sorted(x[0][j] for x in bs)
        lo, hi = b[int(0.025 * len(b))], b[int(0.975 * len(b))]
        sd = (sum((x - sum(b) / len(b)) ** 2 for x in b) / len(b)) ** 0.5
        nm = meta["factors"][j]
        print(f"  {nm:16s} {meta['values'][nm]:>7} +-{meta['steps'][nm]:<6g} {eff[j]:+.4f} [{lo:+.4f}, {hi:+.4f}]  z {eff[j] / sd if sd else 0:+.1f}")


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sp = ap.add_subparsers(dest="cmd", required=True)
    s = sp.add_parser("scan")
    s.add_argument("name"); s.add_argument("--pool", default="goal"); s.add_argument("--cells", type=int, default=95)
    s.add_argument("--seed-base", type=int, required=True); s.add_argument("--opp", default=OPP)
    s.add_argument("--chunk", type=int, default=2)
    s = sp.add_parser("forks")
    s.add_argument("name"); s.add_argument("--per-game", type=int, default=3)
    s.add_argument("--window", type=int, default=MARKS[-1])
    s.add_argument("--max-r0", type=int, default=0, help="no fork after this round (cost: a fork replays its prefix)")
    for nm in ("arms", "screen"):
        s = sp.add_parser(nm)
        s.add_argument("name"); s.add_argument("set"); s.add_argument("--arm", action="append", default=[])
        s.add_argument("--salts", type=int, default=2 if nm == "arms" else 1); s.add_argument("--to-end", action="store_true")
        s.add_argument("--limit", type=int, default=0); s.add_argument("--chunk", type=int, default=8)
        s.add_argument("--first-per-game", action="store_true", help="only each game's first fork")
        s.add_argument("--at", type=int, default=0, help="also mark (and play to) this absolute round")
        if nm == "screen":
            s.add_argument("--params", required=True); s.add_argument("--steps", default="")
    s = sp.add_parser("fetch")
    s.add_argument("name"); s.add_argument("stage")
    s = sp.add_parser("calibrate")
    s.add_argument("sets", nargs="+", help="NAME/SET played to the end"); s.add_argument("--mark", type=int, default=80)
    s = sp.add_parser("score")
    s.add_argument("name"); s.add_argument("set"); s.add_argument("--ref", default="null")
    s.add_argument("--mark", type=int, default=80); s.add_argument("--metric", default="labP,HPexch,rats,kingHP,same")
    s = sp.add_parser("fit")
    s.add_argument("name"); s.add_argument("set"); s.add_argument("--mark", type=int, default=80)
    s.add_argument("--metric", default="labP"); s.add_argument("--boot", type=int, default=1000)
    a = ap.parse_args()
    {"scan": cmd_scan, "forks": cmd_forks, "arms": cmd_arms, "screen": cmd_screen, "fetch": cmd_fetch,
     "calibrate": cmd_calibrate, "score": cmd_score, "fit": cmd_fit}[a.cmd](a)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
