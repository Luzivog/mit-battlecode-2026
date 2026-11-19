"""The book's statistics: the effect of each candidate value on each map-seat, against main on the same cells, and
how far to trust the best of several candidates on one map-seat (tools/book/AGENTS.md "fit").

Data: for a candidate c and a map-seat (m, s), the paired game differences d = win(c) - win(main) in {-1, 0, 1},
one per cell (the cell's game in seat s), the candidate and main on the same seeds.

Per estimate: the mean d and its standard error se (from the map-seat's pooled variance over its candidates, so a
candidate whose few games all agree with main does not read se = 0).

Shrinkage (empirical Bayes, normal-normal): true effects across the matrix's (candidate, map-seat) estimates are taken
as N(0, tau^2); tau^2 = max(0, mean(d^2 - se^2)) by moments over every estimate; an estimate shrinks to
d * tau^2 / (tau^2 + se^2), with a posterior sd sqrt(tau^2 se^2 / (tau^2 + se^2)). On a null matrix tau^2 is near 0
and every estimate, the best of each map-seat's candidates included, shrinks to about 0.

Best of K on a map-seat (the selection): the cells' differences flip sign at random, all K candidates of a cell
together (so their sharing of main's game is kept), which is the null "no candidate differs from main". Over the
flips, the maximum over the K candidates of the mean has the null mean `bias` (the upward bias of choosing the best
of K) and p_max = P(null maximum >= the observed maximum). The selection-adjusted effect is adj = best - bias, its
interval adj +- 1.96 se; the best's raw interval is also given at the Sidak level 0.95^(1/K).
"""
from __future__ import annotations

import json
import math
import random
from statistics import NormalDist

Z = NormalDist()


def pair(ref_rows: list[dict], cand_rows: list[dict]) -> dict:
    """(map, seat) -> {seed: d} from two runs' results rows (role o, the tested bot's seat)."""
    def wins(rows):
        out = {}
        for r in rows:
            if r.get("error") or r.get("role") != "o" or r.get("winner") not in ("A", "B"):
                continue
            out[(r["map"], r["seed"], r["seat"])] = 1 if r["winner"] == r["seat"] else 0
        return out
    a, b = wins(ref_rows), wins(cand_rows)
    out: dict = {}
    for k in sorted(set(a) & set(b)):
        out.setdefault((k[0], k[2]), {})[k[1]] = b[k] - a[k]
    return out


def read_rows(path) -> list[dict]:
    with open(path) as f:
        return [json.loads(line) for line in f if line.strip()]


def fit(diffs: dict, flips: int = 2000, seed: int = 1) -> dict:
    """DIFFS: candidate -> (map, seat) -> {seed: d}. Every candidate's cells of a map-seat are those all of them and
    main played. Returns {"tau2", "rows": [per map-seat: n, K, best, per candidate raw/se/shrunk/post_sd], ...}."""
    cands = sorted(diffs)
    mapseats = sorted(set().union(*(set(diffs[c]) for c in cands))) if cands else []
    rows = []
    for ms in mapseats:
        seeds = None
        for c in cands:
            s = set(diffs[c].get(ms, {}))
            seeds = s if seeds is None else seeds & s
        seeds = sorted(seeds or [])
        n = len(seeds)
        if n < 2:
            continue
        mat = [[diffs[c][ms][sd] for c in cands] for sd in seeds]  # cells x candidates
        means = [sum(r[j] for r in mat) / n for j in range(len(cands))]
        pooled = sum((r[j] - means[j]) ** 2 for r in mat for j in range(len(cands))) / (len(cands) * (n - 1))
        se = math.sqrt(pooled / n) if pooled > 0 else 0.5 / n  # no discordant cell: about one game's worth
        rows.append({"map": ms[0], "seat": ms[1], "n": n, "K": len(cands), "mat": mat, "means": means, "se": se})
    est = [(m, r["se"]) for r in rows for m in r["means"]]
    tau2 = max(0.0, sum(m * m - s * s for m, s in est) / len(est)) if est else 0.0
    rng = random.Random(seed)
    out = []
    for r in rows:
        K, n, se = r["K"], r["n"], r["se"]
        k = 1 - (1 - 0.95 ** (1 / K)) / 2
        zk = Z.inv_cdf(k)
        w = tau2 / (tau2 + se * se) if tau2 > 0 else 0.0
        post = math.sqrt(tau2 * se * se / (tau2 + se * se)) if tau2 > 0 else 0.0
        cs = []
        for j, c in enumerate(cands):
            m = r["means"][j]
            cs.append({"cand": c, "raw": m, "lo": m - 1.96 * se, "hi": m + 1.96 * se, "lo_k": m - zk * se,
                       "hi_k": m + zk * se, "shrunk": m * w, "shrunk_lo": m * w - 1.96 * post,
                       "shrunk_hi": m * w + 1.96 * post})
        best = max(range(K), key=lambda j: r["means"][j])
        obs = r["means"][best]
        hits, null_max = 0, 0.0
        for _ in range(flips):
            sg = [rng.choice((-1, 1)) for _ in range(n)]
            mx = max(sum(sg[i] * r["mat"][i][j] for i in range(n)) / n for j in range(K))
            hits += mx >= obs - 1e-12
            null_max += mx
        bias = null_max / flips
        out.append({"map": r["map"], "seat": r["seat"], "n": n, "K": K, "se": se, "cands": cs,
                    "best": cands[best], "p_max": (hits + 1) / (flips + 1), "bias": bias, "adj": obs - bias,
                    "adj_lo": obs - bias - 1.96 * se, "adj_hi": obs - bias + 1.96 * se})
    return {"tau2": tau2, "rows": out, "candidates": cands}


def simulate(K: int = 4, mapseats: int = 60, n: int = 30, q: float = 0.35, effect: float = 0.0, reps: int = 20,
             seed: int = 7, flips: int = 300) -> dict:
    """Null (EFFECT 0) or planted matrices: main wins a cell's game with p 0.8 (0.5 on the planted first map-seat);
    a candidate's game is redrawn with probability Q, with the same p (candidate 0 on the planted map-seat: p 0.5 +
    EFFECT / Q, so its true effect is EFFECT). Returns, over the null map-seats, the best of K's raw, adjusted and
    shrunk means and how often each rule picks it; and the planted candidate's readings."""
    rng = random.Random(seed)
    stats = {"raw_best": [], "adj_best": [], "shrunk_best": [], "p_max_lt_05": 0, "sidak_lo_gt_0": 0,
             "raw_lo_gt_0": 0, "adj_lo_gt_0": 0, "shrunk_lo_gt_0": 0, "nulls": 0, "planted_raw": [],
             "planted_adj": [], "planted_shrunk": [], "planted_p": []}
    for rep in range(reps):
        diffs = {f"c{j}": {} for j in range(K)}
        for i in range(mapseats):
            ms = (f"m{i}", "A")
            planted = effect and i == 0
            base = 0.5 if planted else 0.8
            for s in range(n):
                main = rng.random() < base
                for j in range(K):
                    cand = main
                    if rng.random() < q:
                        cand = rng.random() < base + (effect / q if planted and j == 0 else 0.0)
                    diffs[f"c{j}"].setdefault(ms, {})[s] = int(cand) - int(main)
        f = fit(diffs, flips=flips, seed=rng.randrange(1 << 30))
        for r in f["rows"]:
            b = next(c for c in r["cands"] if c["cand"] == r["best"])
            if effect and r["map"] == "m0":
                c0 = r["cands"][0]
                stats["planted_raw"].append(c0["raw"])
                stats["planted_adj"].append(r["adj"] if r["best"] == "c0" else float("nan"))
                stats["planted_shrunk"].append(c0["shrunk"])
                stats["planted_p"].append(r["p_max"] if r["best"] == "c0" else 1.0)
                continue
            stats["nulls"] += 1
            stats["raw_best"].append(b["raw"])
            stats["adj_best"].append(r["adj"])
            stats["adj_lo_gt_0"] += r["adj_lo"] > 0
            stats["shrunk_best"].append(b["shrunk"])
            stats["p_max_lt_05"] += r["p_max"] < 0.05
            stats["sidak_lo_gt_0"] += b["lo_k"] > 0
            stats["raw_lo_gt_0"] += b["lo"] > 0
            stats["shrunk_lo_gt_0"] += b["shrunk_lo"] > 0
    mean = lambda v: (lambda w: sum(w) / len(w) if w else float("nan"))([x for x in v if x == x])  # noqa: E731
    N = stats["nulls"]
    return {"K": K, "mapseats": mapseats, "n": n, "q": q, "effect": effect, "reps": reps,
            "null_best_raw": mean(stats["raw_best"]), "null_best_adj": mean(stats["adj_best"]),
            "null_best_shrunk": mean(stats["shrunk_best"]),
            "null_pick_raw95": stats["raw_lo_gt_0"] / N, "null_pick_sidak": stats["sidak_lo_gt_0"] / N,
            "null_pick_adj": stats["adj_lo_gt_0"] / N, "null_pick_p_max": stats["p_max_lt_05"] / N,
            "null_pick_shrunk": stats["shrunk_lo_gt_0"] / N,
            "planted_raw": mean(stats["planted_raw"]), "planted_adj": mean(stats["planted_adj"]),
            "planted_shrunk": mean(stats["planted_shrunk"]),
            "planted_p_lt_05": mean([p < 0.05 for p in stats["planted_p"]])}
