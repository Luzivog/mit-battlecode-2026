"""tools/games: is bot A stronger than bot B? Paired seats, sequential test, cache. docs/evaluation.md.

  tools/games ab challenger awubot --pool quick      start a test, print its ID
  tools/games wait ID                                block until it stops (run it in the background), print the report
  tools/games show ID [--games]                      the report so far
  tools/games gauntlet [challenger]                  challenger vs each sparring bot, paired with awubot's own score
  tools/games diff ID1 ID2                           games whose result changed between two runs
  tools/games replay ID GAME                         replay one game of a run, with a replay file
  tools/games list | stop ID | pools | simulate

Exit codes of wait/show/ab --fg: 0 H1 accepted (A stronger; gauntlet: no opponent worse), 3 H0 accepted
(A not stronger; gauntlet: worse against an opponent), 4 inconclusive (cell cap reached), 5 still running, 1 error,
2 usage.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import random
import signal
import socket
import subprocess
import sys
import threading
import time
from pathlib import Path

from . import play, pools, stats, store

RUNS = store.DATA / "runs"
EXIT = {"H1": 0, "H0": 3, "inconclusive": 4, "running": 5, "error": 1}
MAIN_OPPONENTS = ["opp_obg", "opp_gst"]
OTHER_OPPONENTS = ["opp_spaark", "opp_cerr", "opp_parmesan", "opp_gravy", "opp_goobie", "opp_powerpuff", "opp_food",
                   "awubot_quals", "awubot_us", "awubot_mystery", "awubot_sus"]
WIN_SHORT = {"KILL_ALL_RAT_KINGS": "KILL", "MORE_POINTS": "PTS", "MORE_CHEESE": "CHEESE",
             "MORE_ROBOTS_ALIVE": "ALIVE", "WON_BY_DUBIOUS_REASONS": "RNG", "RESIGNATION": "RESIGN"}


# ------------------------------------------------------------------------------------------------------------ cells

def cell_seed(map_name: str, seed_base: int, pass_no: int) -> int | None:
    """Pass 0 of seed base 0 is the official game (the map's embedded seed, None here); the rest are derived."""
    if seed_base == 0 and pass_no == 0:
        return None
    h = hashlib.sha256(f"{seed_base}:{pass_no}:{map_name}".encode()).digest()
    return int.from_bytes(h[:4], "big") & 0x7FFFFFFF


def cells(maps: list[str], seed_base: int):
    """Round robin over the maps, in a new fixed random order each pass, one seed per map per pass."""
    idx = 0
    pass_no = 0
    while True:
        order = sorted(maps)
        random.Random(f"{seed_base}:{pass_no}").shuffle(order)
        for m in order:
            yield {"idx": idx, "pass": pass_no, "map": m, "seed": cell_seed(m, seed_base, pass_no)}
            idx += 1
        pass_no += 1


def outcome(r: dict) -> tuple:
    """What a game's result is, seat by seat (no names): equal outcomes = the same game."""
    return (r.get("winner"), r.get("win_type"), r.get("round"), json.dumps(r.get("A"), sort_keys=True),
            json.dumps(r.get("B"), sort_keys=True))


class Test:
    """One sequential paired test: `ours` vs `theirs` (ab), or ours-vs-theirs paired with ref-vs-theirs (gauntlet)."""

    def __init__(self, name: str, ours: str, theirs: str, ref: str | None, maps: list[str], sealed: bool,
                 seed_base: int, delta: float, alpha: float, beta: float, min_cells: int, max_cells: int,
                 engine: str):
        self.name, self.ours, self.theirs, self.ref = name, ours, theirs, ref
        self.maps, self.sealed, self.engine = maps, sealed, engine
        self.delta, self.alpha, self.beta = delta, alpha, beta
        self.min_cells, self.max_cells = min_cells, max_cells
        self.stream = cells(maps, seed_base)
        self.cells: list[dict] = []
        self.prefix = 0           # cells [0, prefix) are complete and scored
        self.verdict = "running"
        self.llr = 0.0
        self.errors = 0
        self.lo, self.hi = stats.bounds(alpha, beta)

    def games(self, cell: dict) -> list[dict]:
        """The cell's games. role o: ours vs theirs; role r: ref vs theirs. seat: where the role's bot sits."""
        roles = [("o", self.ours)] + ([("r", self.ref)] if self.ref else [])
        out = []
        for role, bot in roles:
            for seat in "AB":
                a, b = (bot, self.theirs) if seat == "A" else (self.theirs, bot)
                gid = f"{self.name}.{role}{cell['idx']:03d}{seat}" if self.ref else f"c{cell['idx']:03d}{seat}"
                out.append({"id": gid, "a": a, "b": b, "map": cell["map"], "seed": cell["seed"],
                            "engine": self.engine, "role": role, "seat": seat})
        return out

    def next_cell(self) -> dict:
        cell = next(self.stream)
        cell["games"] = {}
        self.cells.append(cell)
        return cell

    def wants_more(self) -> bool:
        return self.verdict == "running" and len(self.cells) < self.max_cells

    def cell_scores(self, cell: dict) -> tuple[float, float | None] | None:
        """(ours, ref) cell scores: 1 won both seats, 0.5 split, 0 lost both. None if a game failed."""
        g = list(cell["games"].values())
        if any(r.get("error") for r in g):
            return None
        score = {"o": 0.0, "r": 0.0}
        for r in g:
            score[r["role"]] += (r["winner"] == r["seat"]) / 2
        return score["o"], (score["r"] if self.ref else None)

    def update(self) -> None:
        n_games = 4 if self.ref else 2
        while self.prefix < len(self.cells) and len(self.cells[self.prefix]["games"]) == n_games:
            if self.cell_scores(self.cells[self.prefix]) is None:
                self.errors += 1
            self.prefix += 1
        counts = self.counts()
        if self.ref:
            self.llr = stats.llr(counts, stats.PENTANOMIAL, 0.5 - self.delta / 2, 0.5)
        else:
            self.llr = stats.llr(counts, stats.TRINOMIAL, 0.5, 0.5 + self.delta)
        if self.verdict != "running":
            return
        scored = sum(counts)
        if scored >= self.min_cells and self.llr >= self.hi:
            self.verdict = "H1"
        elif scored >= self.min_cells and self.llr <= self.lo:
            self.verdict = "H0"
        elif self.prefix >= self.max_cells:
            self.verdict = "inconclusive"
        if self.prefix and self.errors > max(2, self.prefix // 10):
            self.verdict = "error"

    def scored_cells(self) -> list[dict]:
        return [c for c in self.cells[: self.prefix] if self.cell_scores(c) is not None]

    def counts(self) -> list[int]:
        if self.ref:
            c = [0] * 5
            for cell in self.scored_cells():
                o, r = self.cell_scores(cell)
                c[int(round((o - r + 1) * 2))] += 1
            return c
        c = [0] * 3
        for cell in self.scored_cells():
            c[int(self.cell_scores(cell)[0] * 2)] += 1
        return c

    # -------------------------------------------------------------------------------------------------- reporting

    def summary(self) -> dict:
        sc = self.scored_cells()
        out = {"name": self.name, "ours": self.ours, "theirs": self.theirs, "ref": self.ref, "verdict": self.verdict,
               "llr": round(self.llr, 3), "bounds": [round(self.lo, 3), round(self.hi, 3)], "delta": self.delta,
               "cells": len(sc), "submitted": len(self.cells), "errors": self.errors, "counts": self.counts()}
        if not sc:
            return out
        ours = [(c["map"], self.cell_scores(c)[0]) for c in sc]
        mean = sum(s for _, s in ours) / len(ours)
        lo, hi = stats.bootstrap_by_map(ours)
        won = sum(1 for _, s in ours if s == 1)
        lost = sum(1 for _, s in ours if s == 0)
        out.update({"score": round(mean, 4), "score_ci": [round(lo, 4), round(hi, 4)], "elo": round(stats.elo(mean)),
                    "elo_ci": [round(stats.elo(lo)), round(stats.elo(hi))] if lo == lo else None,
                    "los": round(stats.los(won, lost), 4)})
        if self.ref:
            refs = [(c["map"], self.cell_scores(c)[1]) for c in sc]
            rmean = sum(s for _, s in refs) / len(refs)
            diffs = [(m, o - r) for (m, o), (_, r) in zip(ours, refs)]
            dlo, dhi = stats.bootstrap_by_map(diffs)
            out.update({"ref_score": round(rmean, 4), "diff": round(mean - rmean, 4),
                        "diff_ci": [round(dlo, 4), round(dhi, 4)],
                        "better_cells": sum(1 for _, d in diffs if d > 0),
                        "worse_cells": sum(1 for _, d in diffs if d < 0)})
        games = [g for c in sc for g in c["games"].values() if g["role"] == "o"]
        # Points margin (ours - theirs, as the engine scores round 2000), averaged per cell: T3's diagnostic.
        def margin(g):
            other = "B" if g["seat"] == "A" else "A"
            return g[g["seat"]]["points"] - g[other]["points"]
        cell_margin = [(c["map"], sum(margin(g) for g in c["games"].values() if g["role"] == "o") / 2) for c in sc]
        plo, phi = stats.bootstrap_by_map(cell_margin)
        long = [margin(g) for g in games if g["round"] >= 2000]
        out["points_margin"] = round(sum(m for _, m in cell_margin) / len(cell_margin), 2)
        out["points_margin_ci"] = [round(plo, 2), round(phi, 2)] if plo == plo else None
        out["points_margin_r2000"] = [round(sum(long) / len(long), 2), len(long)] if long else None
        out["games_won"] = sum(1 for g in games if g["winner"] == g["seat"])
        out["games_lost"] = len(games) - out["games_won"]
        out["identical"] = self.identical(sc)
        out["mean_rounds"] = round(sum(g["round"] for g in games) / len(games))
        crashed = {self.ours: 0, self.theirs: 0}
        for g in games:
            crashed[g["a"]] += g["A"]["crashed"]
            crashed[g["b"]] += g["B"]["crashed"]
        out["crashed"] = crashed
        out["engine_errors"] = sum(g.get("engine_errors", 0) for g in games)
        mix = {}
        for g in games:
            res = "won" if g["winner"] == g["seat"] else "lost"
            key = f"{res} as {g['seat']}"
            mix.setdefault(key, {})
            wt = WIN_SHORT.get(g["win_type"], g["win_type"])
            mix[key][wt] = mix[key].get(wt, 0) + 1
        out["win_types"] = mix
        if not self.sealed:
            per = {}
            for c in sc:
                o = self.cell_scores(c)
                p = per.setdefault(c["map"], {"cells": 0, "won": 0, "split": 0, "lost": 0, "ref": 0.0, "score": 0.0})
                p["cells"] += 1
                p["score"] += o[0]
                p["won" if o[0] == 1 else "lost" if o[0] == 0 else "split"] += 1
                if self.ref:
                    p["ref"] += o[1]
            out["maps"] = per
        return out

    def identical(self, sc: list[dict]) -> bool:
        """ab: every game equals its mirror (ours plays exactly like theirs). gauntlet: equal to ref's games."""
        for c in sc:
            g = c["games"]
            by = {(r["role"], r["seat"]): outcome(r) for r in g.values()}
            if self.ref:
                if by[("o", "A")] != by[("r", "A")] or by[("o", "B")] != by[("r", "B")]:
                    return False
            elif by[("o", "A")] != by[("o", "B")]:
                return False
        return bool(sc)


# ------------------------------------------------------------------------------------------------------------- runs

SEALED_RUNS = store.DATA / "SEALED-TEST-DO-NOT-READ" / "runs"


def run_dir(run_id: str) -> Path:
    for base in (RUNS, SEALED_RUNS):
        if (base / run_id / "run.json").exists():
            return base / run_id
    matches = sorted(p.name for p in RUNS.glob(f"*{run_id}*")) if RUNS.exists() else []
    if len(matches) == 1:
        return RUNS / matches[0]
    raise store.SetupError(f"no run {run_id!r}" + (f" (matches: {', '.join(matches[:5])})" if matches else ""))


def write_json(path: Path, obj) -> None:
    tmp = path.with_suffix(".tmp")
    tmp.write_text(json.dumps(obj, indent=1))
    os.replace(tmp, path)


def make_tests(meta: dict) -> list[Test]:
    p = meta["params"]
    if meta["kind"] == "ab":
        maps, sealed = pools.resolve(p["pool"])
        return [Test("ab", p["a"], p["b"], None, maps, sealed, p["seed_base"], p["delta"], p["alpha"], p["beta"],
                     p["min_cells"] or len(maps), p["max_cells"] or len(maps), p["engine"])]
    tests = []
    for opp in p["opponents"]:
        main = opp in p["main"]
        maps, sealed = pools.resolve(p["main_pool"] if main else p["rest_pool"])
        cap = p["max_cells"] if main else len(maps) * p["rest_passes"]
        tests.append(Test(opp, p["a"], opp, p["ref"], maps, sealed, p["seed_base"], p["delta"], p["alpha"],
                          p["beta"], min(len(maps), cap), cap, p["engine"]))
    return tests


def drive(d: Path) -> int:
    """Plays a run's tests to their verdicts, tests in priority order, the next one taking the idle workers.

    A test scores only its complete prefix of cells (in submission order), so its verdict doesn't depend on timing.
    state.json is rewritten after every game.
    """
    meta = json.loads((d / "run.json").read_text())
    p = meta["params"]
    tests = make_tests(meta)
    replay_dir = d / "replays" if p.get("replays", "none") != "none" else None
    if replay_dir:
        replay_dir.mkdir(exist_ok=True)
    runner = play.Runner(p["workers"], d, p["timeout"])
    for ref, (jar, h) in meta.get("bots", {}).items():  # the builds made at submission
        runner.resolver.bots[ref] = (Path(jar), h)
    cond = threading.Condition()  # re-entrant
    inflight = {"n": 0}
    out = open(d / "results.jsonl", "a")
    t0 = time.time()
    counts = {"played": 0, "cached": 0}
    stop = {"flag": False}

    def on_term(*_):
        stop["flag"] = True
    signal.signal(signal.SIGTERM, on_term)

    def record(test: Test, cell: dict, r: dict) -> None:
        with cond:
            if r.get("replay") and p.get("replays") == "losses" and r.get("winner") == r["seat"]:
                os.unlink(r["replay"])
                r["replay"] = None
            r["test"], r["cell"] = test.name, cell["idx"]
            cell["games"][r["id"]] = r
            out.write(json.dumps(r) + "\n")
            out.flush()
            counts["cached" if r["cached"] else "played"] += 1
            if not r["cached"]:
                inflight["n"] -= 1
            cond.notify_all()

    def save_state(status: str, error: str | None = None) -> None:
        wall = time.time() - t0
        state = {"status": status, "updated": time.time(), "wall_s": round(wall), "played": counts["played"],
                 "cached": counts["cached"], "inflight": inflight["n"],
                 "games_per_min": round(counts["played"] / wall * 60, 2) if wall > 0 else 0,
                 "tests": [t.summary() for t in tests]}
        if error:
            state["error"] = error
        write_json(d / "state.json", state)

    queue_cap = 2 * p["workers"]          # games waiting or playing
    ahead_cap = 4 * p["workers"]          # games of a test beyond its scored prefix
    try:
        while True:
            with cond:
                for t in tests:
                    t.update()
                running = [t for t in tests if t.verdict == "running"]
                if not running or stop["flag"]:
                    break
                for t in running:
                    per_cell = 4 if t.ref else 2
                    while (inflight["n"] < queue_cap and t.wants_more()
                           and (len(t.cells) - t.prefix) * per_cell < ahead_cap):
                        cell = t.next_cell()
                        for spec in t.games(cell):
                            path = replay_dir / f"{play.safe(spec['id'])}.bc26" if replay_dir else None
                            job = runner.resolver.job(spec, path)
                            hit = runner.cached(job)
                            if hit:
                                record(t, cell, hit)
                            else:
                                inflight["n"] += 1
                                runner.submit(job, lambda r, t=t, cell=cell: record(t, cell, r))
                        t.update()
                save_state("running")
                if inflight["n"] and any(t.verdict == "running" for t in tests):
                    cond.wait(timeout=10)
    except Exception as e:  # noqa: BLE001
        runner.close(wait=False)
        save_state("error", f"{type(e).__name__}: {e}")
        raise
    runner.close(wait=False)
    save_state("stopped" if stop["flag"] else "done")
    return 0


def verdict_of(state: dict, kind: str) -> str:
    if state["status"] == "running":
        return "running"
    if state["status"] in ("error", "stopped"):
        return "error"
    vs = [t["verdict"] for t in state["tests"]]
    if kind == "ab":
        return vs[0]
    if "error" in vs:
        return "error"
    if "H0" in vs:
        return "H0"
    if "inconclusive" in vs or "running" in vs:
        return "inconclusive"
    return "H1"


# ----------------------------------------------------------------------------------------------------------- report

def pct(x: float) -> str:
    return f"{100 * x:.1f}%"


def report(d: Path, show_games: bool = False) -> tuple[str, str]:
    meta = json.loads((d / "run.json").read_text())
    p = meta["params"]
    if not (d / "state.json").exists():
        return f"run {meta['id']}: starting", "running"
    st = json.loads((d / "state.json").read_text())
    verdict = verdict_of(st, meta["kind"])
    L = []
    L.append(f"run {meta['id']} [{st['status']}]  {meta['kind']} {p['a']} vs "
             f"{p['b'] if meta['kind'] == 'ab' else 'gauntlet (ref ' + p['ref'] + ')'}  engine {p['engine']}  "
             f"pool {p['pool'] if meta['kind'] == 'ab' else p['main_pool'] + '/' + p['rest_pool']}  "
             f"seed base {p['seed_base']}")
    if st.get("error"):
        L.append(f"ERROR: {st['error']}")
    for t in st["tests"]:
        L += report_test(t, meta["kind"], p)
    L.append(f"games: {st['played']} played, {st['cached']} from cache, {st['wall_s']} s wall, "
             f"{st['games_per_min']} games/min on {p['workers']} workers")
    if show_games:
        L.append("")
        L.append("games (id: map seed, A vs B -> winner type round [robots crashed A/B]):")
        for line in open(d / "results.jsonl"):
            r = json.loads(line)
            if r.get("error"):
                L.append(f"  {r['id']}: {r['map']} s{r['seed']} ERROR {r['error']}")
                continue
            if d.parent == SEALED_RUNS:
                r["map"] = "(sealed)"
            w = r["a"] if r["winner"] == "A" else r["b"]
            L.append(f"  {r['id']}: {r['map']} s{r['seed']}, {r['a']} vs {r['b']} -> {w} "
                     f"{WIN_SHORT.get(r['win_type'], r['win_type'])} r{r['round']} "
                     f"[{r['A']['crashed']}/{r['B']['crashed']}]" + (f" replay {r['replay']}" if r.get("replay") else ""))
    L.append(f"exit {EXIT[verdict]} ({verdict})")
    return "\n".join(L), verdict


def report_test(t: dict, kind: str, p: dict) -> list[str]:
    L = [""]
    name = t["ours"] + " vs " + t["theirs"]
    vtext = {
        "H1": f"H1 accepted: {t['ours']} is stronger (score >= {0.5 + t['delta']:.2f})" if kind == "ab" else
              f"not worse than {t['ref']} against {t['theirs']}",
        "H0": f"H0 accepted: {t['ours']} is not stronger (score <= 0.50)" if kind == "ab" else
              f"WORSE than {t['ref']} against {t['theirs']} (by >= {t['delta']:.2f})",
        "inconclusive": "inconclusive: cell cap reached", "running": "running", "error": "error: too many failed games",
    }[t["verdict"]]
    L.append(f"{name}: {vtext}")
    if kind == "ab":
        h = f"H0 score <= 0.50, H1 score >= {0.5 + t['delta']:.2f}"
    else:
        h = f"H0 diff <= -{t['delta']:.2f}, H1 diff >= 0"
    L.append(f"  LLR {t['llr']:+.2f} (stop at {t['bounds'][0]:+.2f} / {t['bounds'][1]:+.2f}; {h})")
    if not t["cells"]:
        L.append(f"  no complete cells yet ({t['submitted']} submitted)")
        return L
    c = t["counts"]
    if kind == "ab":
        L.append(f"  cells {t['cells']} (map+seed, both seats): won both {c[2]}, split {c[1]}, lost both {c[0]}"
                 + (f"; {t['errors']} failed" if t["errors"] else ""))
    else:
        L.append(f"  cells {t['cells']}: {t['ours']} better {t['better_cells']}, worse {t['worse_cells']}; "
                 f"diff counts -1..+1: {c}" + (f"; {t['errors']} failed" if t["errors"] else ""))
    ci = t.get("elo_ci")
    L.append(f"  games {t['games_won']}-{t['games_lost']}  score {t['score']:.3f} "
             f"[{t['score_ci'][0]:.3f}, {t['score_ci'][1]:.3f}]  Elo {t['elo']:+d}"
             + (f" [{ci[0]:+d}, {ci[1]:+d}]" if ci else "") + f"  LOS {pct(t['los'])}  (95% CI, bootstrap over maps)")
    if kind != "ab":
        L.append(f"  {t['ref']} on the same cells: {t['ref_score']:.3f}; diff {t['diff']:+.3f} "
                 f"[{t['diff_ci'][0]:+.3f}, {t['diff_ci'][1]:+.3f}]")
    pm = t["points_margin_ci"]
    r2 = t["points_margin_r2000"]
    L.append(f"  points margin per game (ours - theirs, engine scoring): {t['points_margin']:+.1f}"
             + (f" [{pm[0]:+.1f}, {pm[1]:+.1f}]" if pm else "")
             + (f"; in the {r2[1]} games that reached round 2000: {r2[0]:+.1f}" if r2 else ""))
    if t["identical"]:
        L.append("  no behaviour change: " + ("every game equals its mirror" if kind == "ab" else
                                              f"every game equals {t['ref']}'s"))
    mix = "; ".join(f"{k}: " + " ".join(f"{w} {n}" for w, n in sorted(v.items())) for k, v in sorted(t["win_types"].items()))
    L.append(f"  win types: {mix}")
    L.append(f"  mean rounds {t['mean_rounds']}; robots killed by their own uncaught exception: " +
             ", ".join(f"{k} {v}" for k, v in t["crashed"].items()) +
             (f"; engine errors {t['engine_errors']}" if t["engine_errors"] else ""))
    if "maps" in t:
        rows = sorted(t["maps"].items(), key=lambda kv: (kv[1]["score"] / kv[1]["cells"], kv[0]))
        L.append("  per map (worst first): map cells won/split/lost score" + (" ref" if kind != "ab" else ""))
        for m, v in rows:
            L.append(f"    {m:28s} {v['cells']:3d}  {v['won']:2d}/{v['split']:2d}/{v['lost']:2d}  "
                     f"{v['score'] / v['cells']:.2f}" + (f"  {v['ref'] / v['cells']:.2f}" if kind != "ab" else ""))
    return L


# --------------------------------------------------------------------------------------------------------- commands

def start(kind: str, params: dict, fg: bool) -> int:
    bots = [params["a"], params.get("b"), params.get("ref"), *params.get("opponents", [])]
    for ref in bots:
        if ref:
            store.parse_bot(ref)
    if kind == "ab" and params["a"] == params["b"]:
        raise store.SetupError("A and B are the same bot: every cell splits by construction")
    if kind == "gauntlet" and params["a"] == params["ref"]:
        raise store.SetupError("A and REF are the same bot")
    sealed = any(pools.resolve(params[k])[1] for k in ("pool", "main_pool", "rest_pool") if k in params)
    if sealed and os.environ.get("BCGAMES_INTEGRATOR") != "1":
        raise store.SetupError("the holdout pool is for the integrator only (BCGAMES_INTEGRATOR=1)")
    if params.get("remote"):
        raise store.SetupError("--remote: track C's Imperial runner is not on main yet; run locally")
    run_id = time.strftime("%m%d-%H%M%S") + f"-{kind}-{os.urandom(2).hex()}"
    d = (SEALED_RUNS if sealed else RUNS) / run_id
    d.mkdir(parents=True)
    meta = {"id": run_id, "kind": kind, "created": time.time(), "host": socket.gethostname(),
            "root": str(store.ROOT), "argv": sys.argv, "params": params}
    # Build the bots now: compile errors show here, and the run plays the code as it is at submission.
    resolver = play.Resolver()
    meta["bots"] = {ref: [str(resolver.bot(ref)[0]), resolver.bot(ref)[1]] for ref in dict.fromkeys(bots) if ref}
    write_json(d / "run.json", meta)
    if fg:
        drive(d)
        text, verdict = report(d)
        print(text)
        return EXIT[verdict]
    log = open(d / "driver.log", "ab")
    proc = subprocess.Popen([sys.executable, str(Path(__file__).resolve().parents[1] / "games"), "_drive", run_id],
                            stdout=log, stderr=log, stdin=subprocess.DEVNULL, start_new_session=True)
    meta["pid"] = proc.pid
    write_json(d / "run.json", meta)
    print(run_id)
    print(f"  wait (in the background; exits when the test stops): tools/games wait {run_id}", file=sys.stderr)
    print(f"  progress: tools/games show {run_id}", file=sys.stderr)
    return 0


def cmd_wait(args) -> int:
    d = run_dir(args.id)
    deadline = time.time() + args.timeout if args.timeout else None
    while True:
        st = d / "state.json"
        meta = json.loads((d / "run.json").read_text())
        if st.exists():
            s = json.loads(st.read_text())
            if s["status"] != "running":
                break
            if not alive(meta.get("pid")):
                print(f"driver of {meta['id']} died; see {d / 'driver.log'}", file=sys.stderr)
                return EXIT["error"]
        if deadline and time.time() > deadline:
            break
        time.sleep(5)
    text, verdict = report(d, args.games)
    print(text)
    return EXIT[verdict]


def alive(pid) -> bool:
    if not pid:
        return True
    try:
        os.kill(pid, 0)
    except ProcessLookupError:
        return False
    except PermissionError:
        return True
    return True


def cmd_show(args) -> int:
    d = run_dir(args.id)
    if args.json:
        st = json.loads((d / "state.json").read_text()) if (d / "state.json").exists() else {}
        meta = json.loads((d / "run.json").read_text())
        st["verdict"] = verdict_of(st, meta["kind"]) if st else "running"
        st["run"] = meta
        print(json.dumps(st, indent=1))
        return EXIT[st["verdict"]]
    text, verdict = report(d, args.games)
    print(text)
    return EXIT[verdict]


def cmd_list(args) -> int:
    if not RUNS.exists():
        return 0
    for d in sorted(RUNS.iterdir(), key=lambda p: p.stat().st_mtime)[-args.n:]:
        try:
            meta = json.loads((d / "run.json").read_text())
            st = json.loads((d / "state.json").read_text()) if (d / "state.json").exists() else {"status": "starting"}
            v = verdict_of(st, meta["kind"]) if "tests" in st else st["status"]
            p = meta["params"]
            what = f"{p['a']} vs {p['b']}" if meta["kind"] == "ab" else f"{p['a']} gauntlet"
            cells = sum(t["cells"] for t in st.get("tests", []))
            print(f"{meta['id']}  {st['status']:8s} {v:12s} {what}  pool {p.get('pool', p.get('main_pool'))}  "
                  f"{cells} cells")
        except (OSError, ValueError, KeyError):
            continue
    return 0


def cmd_stop(args) -> int:
    d = run_dir(args.id)
    meta = json.loads((d / "run.json").read_text())
    pid = meta.get("pid")
    if not pid or not alive(pid):
        print("not running")
        return 0
    os.killpg(pid, signal.SIGTERM)
    print(f"stopping {meta['id']}")
    return 0


def find_jar(class_hash: str) -> Path | None:
    for meta in (store.DATA / "bots").glob("*.json"):
        if json.loads(meta.read_text()).get("class_hash") == class_hash:
            return meta.with_suffix(".jar")
    return None


def cmd_replay(args) -> int:
    d = run_dir(args.id)
    rec = None
    for line in open(d / "results.jsonl"):
        r = json.loads(line)
        if r["id"] == args.game:
            rec = r
    if rec is None or rec.get("error"):
        raise store.SetupError(f"no finished game {args.game!r} in run {d.name} (tools/games show {d.name} --games)")
    resolver = play.Resolver()
    for ref, h in ((rec["a"], rec["a_hash"]), (rec["b"], rec["b_hash"])):
        jar = find_jar(h)
        if jar is None:
            raise store.SetupError(f"the build of {ref} that played this game ({h}) is no longer in the store")
        resolver.bots[ref] = (jar, h)
    out = Path(args.out) if args.out else d / "replays" / f"{play.safe(rec['id'])}.bc26"
    out.parent.mkdir(parents=True, exist_ok=True)
    spec = {k: rec[k] for k in ("id", "a", "b", "map", "seed", "engine")}
    job = resolver.job(spec, out.resolve())
    pool = play.Pool(1, d / "logs", resolver.runner_dir)
    done = threading.Event()
    res = {}
    pool.submit(job, lambda j, r: (res.update(r), done.set()))
    done.wait()
    pool.close()
    if res.get("error"):
        print(f"replay failed: {res['error']}")
        return 1
    same = res["digest"] == rec["digest"]
    print(f"{out}\n{rec['map']} s{rec['seed']} {rec['a']}(A) vs {rec['b']}(B): {res['winner']} {res['win_type']} "
          f"r{res['round']}; " + ("identical to the recorded game" if same else
                                   f"DIFFERENT from the recorded game ({rec['digest']} vs {res['digest']})"))
    return 0 if same else 1


def game_index(d: Path) -> dict:
    """(test, role, map, seed, seat of the role's bot) -> result, for matching games across runs."""
    out = {}
    for line in open(d / "results.jsonl"):
        r = json.loads(line)
        out[(r.get("test", "ab"), r.get("role", "o"), r["map"], r["seed"], r.get("seat", "A"))] = r
    return out


def cmd_diff(args) -> int:
    d1, d2 = run_dir(args.id1), run_dir(args.id2)
    g1, g2 = game_index(d1), game_index(d2)
    common = sorted(set(g1) & set(g2), key=lambda k: (k[0], k[2], str(k[3]), k[1], k[4]))
    changed = flips_up = flips_down = 0
    lines = []
    for k in common:
        a, b = g1[k], g2[k]
        if a.get("error") or b.get("error"):
            continue
        if outcome(a) == outcome(b):
            continue
        changed += 1
        seat = k[4]
        w1, w2 = a["winner"] == seat, b["winner"] == seat
        tag = "won->LOST" if w1 and not w2 else "lost->WON" if w2 and not w1 else "same winner"
        flips_up += tag == "lost->WON"
        flips_down += tag == "won->LOST"
        if args.all or tag != "same winner":
            lines.append(f"  {k[0]:10s} {k[2]:26s} s{k[3]!s:11s} seat {seat}  {tag:11s} "
                         f"{WIN_SHORT.get(a['win_type'])} r{a['round']} -> {WIN_SHORT.get(b['win_type'])} r{b['round']}"
                         f"   {d2.name} game {b['id']}")
    print(f"{d1.name} -> {d2.name}: {len(common)} games in both, {changed} changed outcome "
          f"({flips_up} lost->WON, {flips_down} won->LOST, {changed - flips_up - flips_down} same winner)")
    print("\n".join(lines) if lines else "  no game changed winner" if changed else "  no behaviour change")
    return 0


def cmd_pools(args) -> int:
    every = list(pools.POOLS.items()) + [(n, pools.class_pool(n)) for n in pools.CLASS_POOLS]
    for name, maps in every:
        print(f"{name:11s} {len(maps):3d}  " + ", ".join(maps if args.v else maps[:8] + (["..."] if len(maps) > 8 else [])))
    try:
        print(f"holdout     {len(pools.holdout()):3d}  sealed (integrator only)")
    except OSError:
        print("holdout       -  not on this machine")
    return 0


def cmd_simulate(args) -> int:
    print(f"SPRT on cells (both seats), H0 score <= 0.5 vs H1 score >= 0.5 + delta, alpha = beta = {args.alpha}, "
          f"min {args.min_cells} cells, cap {args.max_cells}; {args.runs} runs each")
    print("delta  split  true score   P(H1)  P(H0)  P(inc)  cells: mean median p90")
    for delta in args.delta:
        for split in args.split:
            for true in (0.5, 0.5 + delta / 2, 0.5 + delta):
                try:
                    probs = stats.trinomial(true, split)
                except ValueError:
                    continue
                r = stats.simulate(probs, stats.TRINOMIAL, 0.5, 0.5 + delta, args.alpha, args.alpha,
                                   args.min_cells, args.max_cells, runs=args.runs)
                print(f"{delta:5.2f}  {split:5.2f}  {true:10.3f}   {r['rates']['H1']:5.2f}  {r['rates']['H0']:5.2f}  "
                      f"{r['rates']['inconclusive']:6.2f}  {r['mean']:9.0f} {r['median']:6d} {r['p90']:4d}")
    return 0


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(prog="tools/games", description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)

    def common(sp, delta):
        sp.add_argument("--engine", choices=sorted(store.ENGINES), default=store.DEFAULT_ENGINE)
        sp.add_argument("--delta", type=float, default=delta, help=f"effect size in score (default {delta})")
        sp.add_argument("--alpha", type=float, default=0.05)
        sp.add_argument("--beta", type=float, default=0.05)
        sp.add_argument("--max-cells", type=int, default=None,
                        help="cap, then the verdict is inconclusive (default 400; smoke pool: one pass)")
        sp.add_argument("--seed-base", type=int, default=0,
                        help="0: pass 0 plays the official seeds, later passes derived seeds; N: all derived from N")
        sp.add_argument("--workers", type=int, default=8)
        sp.add_argument("--timeout", type=float, default=play.DEFAULT_TIMEOUT, help="seconds per game")
        sp.add_argument("--replays", choices=["none", "losses", "all"], default="none",
                        help="write replays of games played (losses: games A lost); any game: tools/games replay")
        sp.add_argument("--remote", action="store_true", help="play on Imperial (track C)")
        sp.add_argument("--fg", action="store_true", help="run in the foreground and print the report")

    sp = sub.add_parser("ab", help="sequential paired test: is A stronger than B?")
    sp.add_argument("a")
    sp.add_argument("b")
    sp.add_argument("--pool", default="quick")
    sp.add_argument("--min-cells", type=int, default=0, help="default: one pass over the pool")
    common(sp, 0.10)

    sp = sub.add_parser("gauntlet", help="A vs each sparring bot, paired with REF's own games on the same cells")
    sp.add_argument("a", nargs="?", default="challenger")
    sp.add_argument("--ref", default="awubot")
    sp.add_argument("--opponents", default=",".join(MAIN_OPPONENTS + OTHER_OPPONENTS))
    sp.add_argument("--main", default=",".join(MAIN_OPPONENTS), help="opponents tested sequentially on --main-pool")
    sp.add_argument("--main-pool", default="tune")
    sp.add_argument("--rest-pool", default="quick")
    sp.add_argument("--rest-passes", type=int, default=1, help="passes over --rest-pool for the other opponents")
    common(sp, 0.10)

    sp = sub.add_parser("wait", help="block until a run stops, then print its report")
    sp.add_argument("id")
    sp.add_argument("--games", action="store_true")
    sp.add_argument("--timeout", type=float, default=0, help="give up after this many seconds (exit 5)")
    sp = sub.add_parser("show", help="print a run's report")
    sp.add_argument("id")
    sp.add_argument("--games", action="store_true", help="list every game")
    sp.add_argument("--json", action="store_true")
    sp = sub.add_parser("list", help="recent runs")
    sp.add_argument("-n", type=int, default=20)
    sp = sub.add_parser("stop", help="stop a run")
    sp.add_argument("id")
    sp = sub.add_parser("replay", help="replay one game of a run with a replay file")
    sp.add_argument("id")
    sp.add_argument("game", help="game id, from tools/games show ID --games")
    sp.add_argument("--out")
    sp = sub.add_parser("diff", help="games whose outcome changed between two runs")
    sp.add_argument("id1")
    sp.add_argument("id2")
    sp.add_argument("--all", action="store_true", help="also games with the same winner but another ending")
    sp = sub.add_parser("pools", help="list map pools")
    sp.add_argument("-v", action="store_true")
    sp = sub.add_parser("simulate", help="SPRT cells needed, from the trinomial model")
    sp.add_argument("--delta", type=float, nargs="+", default=[0.05, 0.10, 0.20])
    sp.add_argument("--split", type=float, nargs="+", default=[0.5, 0.7, 0.9])
    sp.add_argument("--alpha", type=float, default=0.05)
    sp.add_argument("--min-cells", type=int, default=16)
    sp.add_argument("--max-cells", type=int, default=2000)
    sp.add_argument("--runs", type=int, default=1000)
    sp = sub.add_parser("_drive")
    sp.add_argument("id")
    args = ap.parse_args(argv)

    try:
        if args.cmd == "ab":
            params = {k: getattr(args, k) for k in ("a", "b", "pool", "engine", "delta", "alpha", "beta",
                                                      "min_cells", "seed_base", "workers", "timeout", "replays",
                                                      "remote")}
            params["max_cells"] = args.max_cells or (0 if args.pool == "smoke" else 400)
            return start("ab", params, args.fg)
        if args.cmd == "gauntlet":
            opps = [o for o in args.opponents.split(",") if o]
            params = {"a": args.a, "ref": args.ref, "opponents": opps, "main": args.main.split(","),
                      "main_pool": args.main_pool, "rest_pool": args.rest_pool, "rest_passes": args.rest_passes,
                      "engine": args.engine, "delta": args.delta, "alpha": args.alpha, "beta": args.beta,
                      "max_cells": args.max_cells or 400, "seed_base": args.seed_base, "workers": args.workers,
                      "timeout": args.timeout, "replays": args.replays, "remote": args.remote}
            return start("gauntlet", params, args.fg)
        if args.cmd == "_drive":
            return drive(run_dir(args.id))
        return {"wait": cmd_wait, "show": cmd_show, "list": cmd_list, "stop": cmd_stop, "replay": cmd_replay,
                "diff": cmd_diff, "pools": cmd_pools, "simulate": cmd_simulate}[args.cmd](args)
    except store.SetupError as e:
        print(f"tools/games: {e}", file=sys.stderr)
        return 1
