"""tools/games: is bot A stronger than bot B? Paired seats, sequential test, cache. docs/evaluation.md.

  tools/games ab challenger awubot           gain test: challenger and main's challenger both play awubot on the same
                                             cells; is the branch better than main? Prints the run's ID
  tools/games ab challenger awubot --test harm         harm test: is the branch worse than main?
  tools/games ab X awubot --ref none         plain: X against awubot alone (the integrator's progress rows)
  tools/games wait ID                        block until it stops (run it in the background), print the report
  tools/games wait --any ID ID...            block until the first of them stops: its report; the rest still run
  tools/games show ID [--games | --game G]   the report so far; every game; one game's timeline
  tools/games gauntlet [challenger]          harm test against each sparring bot, paired with main's own games
  tools/games diff ID1 ID2                   games whose result changed between two runs
  tools/games replay ID GAME                 replay one game of a run, with a replay file
  tools/games replays ID [--cells ...]       replay files for a run's cells, after the fact
  tools/games build [BOT...]                 build the native binaries tests of these bots need, ahead of the tests
  tools/games keep ID [--off]                keep a run's replays past the sweep (a day, 15 GB in all)
  tools/games sweep [--dry-run]              free disk now (runs hourly on its own when tools/games starts)
  tools/games list | stop ID | pools | simulate

Exit codes of wait/show/ab --fg: 0 passed (gain test: H1, better; harm test and gauntlet: H0, no harm), 3 failed
(gain: H0; harm: H1, harm found), 4 inconclusive (cell cap reached), 5 still running, 1 error, 2 usage.
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

from . import imperial, native, play, pools, retain, stats, store

RUNS = store.DATA / "runs"
EXIT = {"pass": 0, "fail": 3, "inconclusive": 4, "running": 5, "error": 1}
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


def cells(entries: list[str], seed_base: int):
    """Round robin over the pool's entries, in a new fixed random order each pass, one seed per entry per pass.

    An entry "map:seed" plays that seed in pass 0 of seed base 0 (the cell it names) and derived seeds otherwise, so a
    fresh seed base never replays the listed cells."""
    idx = 0
    pass_no = 0
    while True:
        order = sorted(entries)
        random.Random(f"{seed_base}:{pass_no}").shuffle(order)
        for e in order:
            m, fixed = pools.entry_map(e), pools.entry_seed(e)
            seed = fixed if (fixed is not None and pass_no == 0 and seed_base == 0) else cell_seed(e, seed_base, pass_no)
            yield {"idx": idx, "pass": pass_no, "map": m, "seed": seed}
            idx += 1
        pass_no += 1


def outcome(r: dict) -> tuple:
    """What a game's result is, seat by seat (no names): equal outcomes = the same game."""
    return (r.get("winner"), r.get("win_type"), r.get("round"), json.dumps(r.get("A"), sort_keys=True),
            json.dumps(r.get("B"), sort_keys=True))


class Test:
    """One sequential paired test of `ours` against `theirs`.

    Without a reference the unit is the cell score (trinomial; H0 0.5). With one (`ref`, e.g. main's challenger),
    ref plays `theirs` on the same cells and the unit is the paired difference d = ours - ref, as (d + 1) / 2
    (pentanomial; H0 0.5, no difference). mode "gain": H1 = better by delta; "harm": H1 = worse by delta."""

    def __init__(self, name: str, ours: str, theirs: str, ref: str | None, maps: list[str], sealed: bool,
                 seed_base: int, delta: float, alpha: float, beta: float, min_cells: int, max_cells: int,
                 engine: str, mode: str = "gain"):
        self.name, self.ours, self.theirs, self.ref = name, ours, theirs, ref
        self.maps, self.sealed, self.engine = maps, sealed, engine
        self.delta, self.alpha, self.beta = delta, alpha, beta
        self.min_cells, self.max_cells = min_cells, max_cells
        self.seed_base = seed_base
        self.stream = cells(maps, seed_base)
        self.cells: list[dict] = []
        self.prefix = 0           # cells [0, prefix) are complete and scored
        self.verdict = "running"
        self.llr = 0.0
        self.errors = 0
        self.lo, self.hi = stats.bounds(alpha, beta)
        self.mode = mode
        self.scores = stats.PENTANOMIAL if ref else stats.TRINOMIAL
        step = delta / 2 if ref else delta
        self.s0, self.s1 = 0.5, (0.5 + step if mode == "gain" else 0.5 - step)

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

    def sure_games(self) -> list[dict]:
        """The games of the cells this test will play whatever their results: its first min_cells."""
        stream = cells(self.maps, self.seed_base)
        return [g for _ in range(self.min_cells) for g in self.games(next(stream))]

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
        self.llr = stats.llr(counts, self.scores, self.s0, self.s1)
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
               "mode": self.mode, "s1": self.s1,
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
        return [Test("ab", p["a"], p["b"], p.get("ref"), maps, sealed, p["seed_base"], p["delta"], p["alpha"],
                     p["beta"], p["min_cells"] or len(maps), p["max_cells"] or len(maps), p["engine"],
                     p.get("mode", "gain"))]
    tests = []
    for opp in p["opponents"]:
        main = opp in p["main"]
        maps, sealed = pools.resolve(p["main_pool"] if main else p["rest_pool"])
        cap = p["max_cells"] if main else len(maps) * p["rest_passes"]
        tests.append(Test(opp, p["a"], opp, p["ref"], maps, sealed, p["seed_base"], p["delta"], p["alpha"],
                          p["beta"], min(len(maps), cap), cap, p["engine"], "harm"))
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
    cond = threading.Condition()  # re-entrant
    stop = {"flag": False, "error": None}

    def on_mismatch(job, g) -> None:  # the exactness guard: the run's results can't be trusted
        with cond:
            stop["error"] = (f"exactness guard: game {job.spec.get('id')} differs between the native and the Java "
                             f"engine ({json.dumps(g)}). Its binary is marked bad and its results left the cache. "
                             "Report it (engine/AGENTS.md); rerun with --backend java meanwhile.")
            cond.notify_all()

    guard = {"same": 0, "java-nondeterministic": 0, "java-error": 0, "mismatch": 0}
    guard_log = open(d / "guard.jsonl", "a")

    def on_guard(job, g) -> None:  # a local guard check resolved after its game was delivered
        with cond:
            guard[g["verdict"]] = guard.get(g["verdict"], 0) + 1
            guard_log.write(json.dumps(dict(g, id=job.spec.get("id"), key=job.key)) + "\n")
            guard_log.flush()
            cond.notify_all()

    runner = make_runner(d, p, on_mismatch, on_guard=on_guard)
    for ref, (jar, h) in meta.get("bots", {}).items():  # the builds made at submission
        runner.resolver.bots[ref] = (Path(jar), h)
    if hasattr(runner, "guard_ahead"):  # remote: the guards of the games every test will play go out first
        for t in tests:
            for spec in t.sure_games():
                job = runner.resolver.job(spec, None)
                if not runner.cached(job):
                    runner.guard_ahead(job)
    inflight = {"n": 0}
    out = open(d / "results.jsonl", "a")
    t0 = time.time()
    counts = {"played": 0, "cached": 0, "native": 0, "java": 0, "native_errors": 0}

    def on_term(*_):
        stop["flag"] = True
    signal.signal(signal.SIGTERM, on_term)

    def record(test: Test, cell: dict, r: dict, job=None) -> None:
        with cond:
            if r.get("error") and job is not None and not getattr(job, "retried", False):
                job.retried = True  # one more try: remote machines and JVM starts fail now and then
                runner.submit(job, lambda r2: record(test, cell, r2, job))
                return
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
                if not r.get("error"):
                    counts[r.get("backend", "java")] += 1
                counts["native_errors"] += bool(r.get("native_error"))
                if isinstance(r.get("guard"), dict) and r["guard"]["verdict"] != "pending":  # checked remotely
                    guard[r["guard"]["verdict"]] = guard.get(r["guard"]["verdict"], 0) + 1
            cond.notify_all()

    last_save = {"t": 0.0}

    def save_state(status: str, error: str | None = None) -> None:
        if status == "running" and time.time() - last_save["t"] < 2:
            return
        last_save["t"] = time.time()
        wall = time.time() - t0
        state = {"status": status, "updated": time.time(), "wall_s": round(wall), "played": counts["played"],
                 "cached": counts["cached"], "inflight": inflight["n"],
                 "games_per_min": round(counts["played"] / wall * 60, 2) if wall > 0 else 0,
                 "backends": {"native": counts["native"], "java": counts["java"],
                              "native_errors": counts["native_errors"]},
                 "guard": dict(guard, pending=getattr(runner, "guards_pending", 0)),
                 "tests": [t.summary() for t in tests]}
        if error:
            state["error"] = error
        write_json(d / "state.json", state)

    # Games waiting or playing, and games of a test beyond its scored prefix. Remote: --workers games in flight.
    # Native games are cheap and a slow one holds the prefix back: look further ahead.
    queue_cap = p["workers"] if p.get("remote") else 2 * p["workers"]
    if p.get("remote"):  # the games every test plays whatever happens (its min cells) go out at once, beyond --workers
        queue_cap = max([queue_cap] + [(4 if t.ref else 2) * t.min_cells for t in tests])
    ahead_cap = p["workers"] if p.get("remote") else (8 if p.get("backend", "native") == "native" else 4) * p["workers"]
    try:
        while True:
            with cond:
                for t in tests:
                    t.update()
                running = [t for t in tests if t.verdict == "running"]
                if stop["flag"] or stop["error"]:
                    break
                if not running:
                    if not getattr(runner, "guards_pending", 0):
                        break
                    save_state("running")  # the verdicts wait for the guard's last checks
                    cond.wait(timeout=10)
                    continue
                for t in running:
                    per_cell = 4 if t.ref else 2
                    # Remote: a test gets at most a pass (or 32 cells) beyond its prefix, the next decision point.
                    cap = per_cell * max(t.min_cells, 32) if p.get("remote") else ahead_cap
                    while (inflight["n"] < queue_cap and t.wants_more()
                           and (len(t.cells) - t.prefix) * per_cell < cap):
                        cell = t.next_cell()
                        for spec in t.games(cell):
                            path = replay_dir / f"{play.safe(spec['id'])}.bc26" if replay_dir else None
                            job = runner.resolver.job(spec, path)
                            hit = runner.cached(job)
                            if hit:
                                record(t, cell, hit)
                            else:
                                inflight["n"] += 1
                                runner.submit(job, lambda r, t=t, cell=cell, job=job: record(t, cell, r, job))
                        t.update()
                save_state("running")
                if inflight["n"] and any(t.verdict == "running" for t in tests):
                    cond.wait(timeout=10)
    except Exception as e:  # noqa: BLE001
        runner.close(wait=False)
        save_state("error", f"{type(e).__name__}: {e}")
        getattr(runner, "drain", lambda: None)()
        raise
    runner.close(wait=False)
    # the state (the verdict) is written first; the open remote batches are cancelled after it (drain)
    if stop["error"]:
        save_state("error", stop["error"])
    else:
        save_state("stopped" if stop["flag"] else "done")
    getattr(runner, "drain", lambda: None)()
    return 1 if stop["error"] else 0


def make_runner(d: Path, p: dict, on_mismatch=None, replay: bool = False, on_guard=None):
    """The run's backend: local or Imperial, native (the default; runs made before it existed have no "backend") or
    java. REPLAY: playing games again for their replays (no cache, all workers)."""
    backend = p.get("backend", "native")
    guard = p.get("guard")
    if p.get("remote"):
        return imperial.ImperialRunner(d, p.get("where"), 3600 if replay else p["timeout"], backend, guard,
                                       on_mismatch, use_cache=p.get("cache", True), on_guard=on_guard)
    return play.Runner(p["workers"], d, play.DEFAULT_TIMEOUT if replay else p["timeout"],
                       use_cache=not replay and p.get("cache", True), backend=backend, guard=guard,
                       on_mismatch=on_mismatch, on_guard=on_guard)


def test_outcome(t: dict, kind: str) -> str:
    """pass / fail / inconclusive / running / error. Gain: H1 passes. Harm: H0 passes."""
    v = t["verdict"]
    if v not in ("H0", "H1"):
        return v
    mode = t.get("mode", "gain" if kind == "ab" else "noninf")  # noninf: runs made before harm tests existed
    return "pass" if v == {"gain": "H1", "harm": "H0", "noninf": "H1"}[mode] else "fail"


def rescore(d: Path) -> None:
    """Rebuilds state.json from results.jsonl (the last result of a game wins), with the run's own tests."""
    meta = json.loads((d / "run.json").read_text())
    tests = {t.name: t for t in make_tests(meta)}
    old = json.loads((d / "state.json").read_text()) if (d / "state.json").exists() else {}
    for line in open(d / "results.jsonl"):
        r = json.loads(line)
        t = tests[r.get("test", "ab")]
        while len(t.cells) <= r["cell"]:
            t.next_cell()
        t.cells[r["cell"]]["games"][r["id"]] = r
    for t in tests.values():
        t.update()
    old["tests"] = [t.summary() for t in tests.values()]
    write_json(d / "state.json", old)


def cmd_retry(args) -> int:
    """Plays a finished run's failed games again (with replays if the run kept them) and rescores the run."""
    d = run_dir(args.id)
    meta = json.loads((d / "run.json").read_text())
    st = json.loads((d / "state.json").read_text())
    if st["status"] == "running":
        raise store.SetupError("the run is still running")
    last = {}
    for line in open(d / "results.jsonl"):
        r = json.loads(line)
        last[r["id"]] = r
    failed = [r for r in last.values() if r.get("error")]
    print(f"{len(failed)} failed games to play again", file=sys.stderr)
    if failed:
        runner = make_runner(d, {"remote": args.remote, "where": args.where, "workers": args.workers,
                                 "timeout": 3600 if args.remote else play.DEFAULT_TIMEOUT, "backend": args.backend})
        for ref, (jar, h) in meta.get("bots", {}).items():
            runner.resolver.bots[ref] = (Path(jar), h)
        keep_replays = meta["params"].get("replays", "none") != "none"
        lock = threading.Lock()
        done = threading.Event()
        left = {"n": len(failed)}
        out = open(d / "results.jsonl", "a")

        def record(old: dict, r: dict) -> None:
            with lock:
                r["test"], r["cell"] = old.get("test", "ab"), old["cell"]
                out.write(json.dumps(r) + "\n")
                out.flush()
                print(play.describe(r), file=sys.stderr)
                left["n"] -= 1
                if left["n"] == 0:
                    done.set()

        for old in failed:
            spec = {k: old[k] for k in ("id", "a", "b", "map", "seed", "engine", "role", "seat") if k in old}
            path = (d / "replays" / f"{play.safe(old['id'])}.bc26").resolve() if keep_replays else None
            job = runner.resolver.job(spec, path)
            runner.submit(job, lambda r, old=old: record(old, r))
        done.wait()
        runner.close(wait=not args.remote)
        getattr(runner, "drain", lambda: None)()  # remote: its open batches cancelled
        out.close()
    rescore(d)
    text, verdict = report(d)
    print(text)
    return EXIT[verdict]


def verdict_of(state: dict, kind: str) -> str:
    if state["status"] == "running":
        return "running"
    if state["status"] in ("error", "stopped"):
        return "error"
    vs = [test_outcome(t, kind) for t in state["tests"]]
    for v in ("error", "fail", "running", "inconclusive"):
        if v in vs:
            return "inconclusive" if v == "running" else v
    return "pass"


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
    what = f"{p['a']} vs {p['b']}" if meta["kind"] == "ab" else f"{p['a']} vs the gauntlet"
    test = p.get("mode", "gain") if meta["kind"] == "ab" else "harm"
    L.append(f"run {meta['id']} [{st['status']}]  {what}, {test} test"
             + (f" against ref {p['ref']}" if p.get("ref") else ", no ref") + f"  engine {p['engine']}  "
             f"pool {p['pool'] if meta['kind'] == 'ab' else p['main_pool'] + '/' + p['rest_pool']}  "
             f"seed base {p['seed_base']}")
    if st.get("error"):
        L.append(f"ERROR: {st['error']}")
    for t in st["tests"]:
        L += report_test(t, meta["kind"], p)
    L.append(f"games: {st['played']} played, {st['cached']} from cache, {st['wall_s']} s wall, "
             f"{st['games_per_min']} games/min on {p['workers']} workers")
    L += report_backends(st, p)
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


def report_backends(st: dict, p: dict) -> list[str]:
    """Which engine played the games, and the exactness guard (runs made before the native backend have neither)."""
    be, g = st.get("backends"), st.get("guard")
    if not be:
        return []
    L = [f"engines: {be['native']} games native, {be['java']} java (backend {p.get('backend', 'native')})"
         + (f"; {be['native_errors']} native failures played on java (tools/games show ID --games)"
            if be.get("native_errors") else "")]
    pending = (g or {}).get("pending", 0)
    checked = sum(v for k, v in g.items() if k != "pending") if g else 0
    if checked or pending:
        bad = g.get("mismatch", 0)
        L.append(f"exactness guard: {checked} games played on both engines: {g.get('same', 0)} identical"
                 + (f", {pending} being checked" if pending else "")
                 + (f", {g['java-nondeterministic']} where the Java engine differed from itself (explained)"
                    if g.get("java-nondeterministic") else "")
                 + (f", {g['java-error']} the Java engine failed" if g.get("java-error") else "")
                 + (f", {bad} MISMATCHES" if bad else ""))
    return L


def report_test(t: dict, kind: str, p: dict) -> list[str]:
    L = [""]
    name = t["ours"] + " vs " + t["theirs"]
    mode = t.get("mode", "gain" if kind == "ab" else "noninf")
    d, ref = t["delta"], t.get("ref")
    vs = f"than {ref} " if ref else ""
    texts = {
        "gain": {"H1": f"PASS (H1): {t['ours']} is better {vs}by >= {d:.2f}",
                 "H0": f"FAIL (H0): {t['ours']} is not better {vs}".rstrip()},
        "harm": {"H0": f"PASS (H0): no harm" + (f" relative to {ref}" if ref else ""),
                 "H1": f"FAIL (H1): {t['ours']} is worse {vs}by >= {d:.2f}"},
        "noninf": {"H1": f"not worse than {ref}", "H0": f"WORSE than {ref} (by >= {d:.2f})"},
    }[mode]
    texts.update({"inconclusive": "INCONCLUSIVE: cell cap reached", "running": "running",
                  "error": "ERROR: too many failed games"})
    L.append(f"{name}: {texts[t['verdict']]}")
    unit = "diff (ours - ref)" if ref else "score"
    if mode == "noninf":
        h = f"H0 diff <= -{d:.2f}, H1 diff >= 0"
    else:
        h0, h1 = (0.0, (t["s1"] - 0.5) * 2) if ref else (0.5, t["s1"])
        h = f"H0 {unit} = {h0:+.2f}, H1 {unit} = {h1:+.2f}" if ref else f"H0 {unit} = {h0:.2f}, H1 {unit} = {h1:.2f}"
    L.append(f"  LLR {t['llr']:+.2f} (stop at {t['bounds'][0]:+.2f} / {t['bounds'][1]:+.2f}; {h})")
    if not t["cells"]:
        L.append(f"  no complete cells yet ({t['submitted']} submitted)")
        return L
    c = t["counts"]
    if not ref:
        n = t["cells"]
        L.append(f"  cells {n} (map+seed, both seats): won both {c[2]}, split {c[1]}, lost both {c[0]}"
                 + (f"; {t['errors']} failed" if t["errors"] else "")
                 + f"; flip rate (2-0 + 0-2) {pct((c[0] + c[2]) / n)}, net shift (2-0 - 0-2) {pct((c[2] - c[0]) / n)}")
    else:
        L.append(f"  cells {t['cells']}: {t['ours']} better {t['better_cells']}, worse {t['worse_cells']}; "
                 f"diff counts -1..+1: {c}" + (f"; {t['errors']} failed" if t["errors"] else ""))
    ci = t.get("elo_ci")
    L.append(f"  games {t['games_won']}-{t['games_lost']}  score {t['score']:.3f} "
             f"[{t['score_ci'][0]:.3f}, {t['score_ci'][1]:.3f}]  Elo {t['elo']:+d}"
             + (f" [{ci[0]:+d}, {ci[1]:+d}]" if ci else "") + f"  LOS {pct(t['los'])}  (95% CI, bootstrap over maps)")
    if ref:
        L.append(f"  {t['ref']} on the same cells: {t['ref_score']:.3f}; diff {t['diff']:+.3f} "
                 f"[{t['diff_ci'][0]:+.3f}, {t['diff_ci'][1]:+.3f}]")
    pm = t["points_margin_ci"]
    r2 = t["points_margin_r2000"]
    L.append(f"  points margin per game (ours - theirs, engine scoring): {t['points_margin']:+.1f}"
             + (f" [{pm[0]:+.1f}, {pm[1]:+.1f}]" if pm else "")
             + (f"; in the {r2[1]} games that reached round 2000: {r2[0]:+.1f}" if r2 else ""))
    if t["identical"]:
        L.append("  no behaviour change: " + (f"every game equals {ref}'s" if ref else "every game equals its mirror"))
    mix = "; ".join(f"{k}: " + " ".join(f"{w} {n}" for w, n in sorted(v.items())) for k, v in sorted(t["win_types"].items()))
    L.append(f"  win types: {mix}")
    L.append(f"  mean rounds {t['mean_rounds']}; robots killed by their own uncaught exception: " +
             ", ".join(f"{k} {v}" for k, v in t["crashed"].items()) +
             (f"; engine errors {t['engine_errors']}" if t["engine_errors"] else ""))
    if "maps" in t:
        rows = sorted(t["maps"].items(), key=lambda kv: (kv[1]["score"] / kv[1]["cells"], kv[0]))
        L.append("  per map (worst first): map cells won/split/lost score" + (" ref" if ref else ""))
        for m, v in rows:
            L.append(f"    {m:28s} {v['cells']:3d}  {v['won']:2d}/{v['split']:2d}/{v['lost']:2d}  "
                     f"{v['score'] / v['cells']:.2f}" + (f"  {v['ref'] / v['cells']:.2f}" if ref else ""))
    return L


# --------------------------------------------------------------------------------------------------------- commands

def pin(ref: str | None) -> str | None:
    """pkg@REV -> pkg@<commit>, so a run names exactly what it played (origin/... is fetched first)."""
    if ref is None:
        return None
    pkg, rev = store.parse_bot(ref)
    if rev is None:
        return ref
    if rev.startswith("origin/"):
        subprocess.run(["git", "-C", str(store.ROOT), "fetch", "-q", "origin", rev.split("/", 1)[1]],
                       capture_output=True)
    proc = subprocess.run(["git", "-C", str(store.ROOT), "rev-parse", "--short=12", f"{rev}^{{commit}}"],
                          capture_output=True, text=True)
    if proc.returncode != 0:
        raise store.SetupError(f"bot {ref}: unknown revision {rev!r}")
    return f"{pkg}@{proc.stdout.strip()}"


def default_ref(a: str, ref: str) -> str | None:
    """--ref auto: main's challenger when A is the working tree's challenger, else none."""
    if ref == "none":
        return None
    if ref == "auto":
        return "challenger@origin/main" if a == "challenger" else None
    return ref


def start(kind: str, params: dict, fg: bool) -> int:
    for k in ("a", "b", "ref"):
        if params.get(k):
            params[k] = pin(params[k])
    params["opponents"] = [pin(o) for o in params.get("opponents", [])] if kind == "gauntlet" else []
    if kind == "ab":
        params.pop("opponents")
    bots = [params["a"], params.get("b"), params.get("ref"), *params.get("opponents", [])]
    for ref in bots:
        if ref:
            store.parse_bot(ref)
    if kind == "ab" and params["a"] == params["b"]:
        raise store.SetupError("A and B are the same bot: every cell splits by construction")
    if params.get("ref") and params["a"] == params["ref"]:
        raise store.SetupError("A and REF are the same bot")
    if params.get("ref") and params["ref"] == params.get("b"):
        raise store.SetupError("REF and B are the same bot")
    sealed = any(pools.resolve(params[k])[1] for k in ("pool", "main_pool", "rest_pool") if k in params)
    if sealed and os.environ.get("BCGAMES_INTEGRATOR") != "1":
        raise store.SetupError("the holdout pool is for the integrator only (BCGAMES_INTEGRATOR=1)")
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


def stopped(d: Path) -> str | None:
    """None while RUN D runs; else why it stopped: its status, or "died" (its driver is gone)."""
    st = d / "state.json"
    if not st.exists():
        return None
    status = json.loads(st.read_text())["status"]
    if status != "running":
        return status
    return None if alive(json.loads((d / "run.json").read_text()).get("pid")) else "died"


def cmd_wait(args) -> int:
    """One run: its report when it stops, exit its verdict. Several: --any returns when the first of them stops (at
    once if one has), with its report; else when all have, a line each, exit 0 only if every one passed."""
    dirs = {i: run_dir(i) for i in args.ids}
    deadline = time.time() + args.timeout if args.timeout else None
    while True:
        why = {i: stopped(d) for i, d in dirs.items()}
        done = [i for i in args.ids if why[i]]
        if (done and args.any) or len(done) == len(dirs) or (deadline and time.time() > deadline):
            break
        time.sleep(5)
    if args.any or len(dirs) == 1:
        if not done:  # --timeout
            text, verdict = report(dirs[args.ids[0]], args.games)
            print(text)
            return EXIT["running"]
        i = done[0]
        if why[i] == "died":
            print(f"driver of {i} died; see {dirs[i] / 'driver.log'}", file=sys.stderr)
            return EXIT["error"]
        text, verdict = report(dirs[i], args.games)
        print(text)
        left = [x for x in args.ids if x != i and not why[x]]
        if len(dirs) > 1:
            print(f"stopped: {i}; " + (f"still running: {' '.join(left)}" if left else "none still running")
                  + (f"; stopped before: {' '.join(x for x in done if x != i)}" if len(done) > 1 else ""))
        return EXIT[verdict]
    code = 0
    for i in args.ids:
        if why[i] == "died":
            line, c = f"{i}: driver died (see {dirs[i] / 'driver.log'})", EXIT["error"]
        else:
            text, verdict = report(dirs[i])
            lines = [x for x in text.splitlines() if x.strip()]
            line, c = f"{lines[0]}\n  {lines[1].strip() if len(lines) > 1 else ''}", EXIT[verdict]
        print(line)
        code = code or c
    return code


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


def game_record(d: Path, gid: str) -> dict:
    rec = None
    for line in open(d / "results.jsonl"):
        r = json.loads(line)
        if r["id"] == gid:
            rec = r
    if rec is None:
        raise store.SetupError(f"no game {gid!r} in run {d.name} (tools/games show {d.name} --games)")
    return rec


def timeline(rec: dict) -> str:
    """One game's record and its per-100-round series, for reading a game without a replay viewer."""
    if rec.get("error"):
        return f"{rec['id']}: ERROR {rec['error']}"
    w = rec["a"] if rec["winner"] == "A" else rec["b"]
    L = [f"{rec['id']}: {rec['map']} s{rec['seed']} engine {rec['engine']}: {rec['a']} (A) vs {rec['b']} (B) -> {w} "
         f"{WIN_SHORT.get(rec['win_type'], rec['win_type'])} round {rec['round']}"
         + (" (cooperation)" if rec.get("cooperation") else "")]
    for t in "AB":
        m = rec[t]
        L.append(f"  {t} {rec['a' if t == 'A' else 'b']}: points {m['points']}, cheese transferred "
                 f"{m['cheese_transferred']}, cat damage {m['cat_damage']}, rats {m['rats']}, kings {m['kings']}, "
                 f"cheese {m['cheese']}, robots killed by their own exception {m['crashed']}")
    ser = rec.get("series")
    if ser:
        L.append("  round   A: cheese_tr rats kings cat_dmg   B: cheese_tr rats kings cat_dmg")
        for i, (a, b) in enumerate(zip(ser["A"], ser["B"])):
            rnd = (i + 1) * ser["every"] if i < len(ser["A"]) - 1 else rec["round"]
            L.append(f"  {rnd:5d}   {a[0]:12d} {a[1]:4d} {a[2]:5d} {a[3]:7d}   {b[0]:12d} {b[1]:4d} {b[2]:5d} {b[3]:7d}")
    return "\n".join(L)


def cmd_show(args) -> int:
    d = run_dir(args.id)
    if args.game:
        print(timeline(game_record(d, args.game)))
        return 0
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


def cmd_keep(args) -> int:
    """A run kept: its replays outlive the sweep's age and cap (retain.py); --off: kept no more."""
    d = run_dir(args.id)
    if args.off:
        (d / "keep").unlink(missing_ok=True)
        print(f"{d.name}: not kept; its replays go {retain.REPLAY_KEEP_S // 3600} h after its last activity")
    else:
        (d / "keep").touch()
        print(f"{d.name}: kept; its replays stay until `tools/games keep {d.name} --off`")
    return 0


def cmd_sweep(args) -> int:
    """Every retention step now (retain.py, and this checkout's runs/ packed), saying what it frees."""
    if str(store.ROOT) not in sys.path:
        sys.path.insert(0, str(store.ROOT))
    from tools import runs as R
    out = retain.sweep(force=True, dry=args.dry_run)
    out["run results packed (this checkout's runs/)"] = R.compact_local(store.ROOT / "runs", dry=args.dry_run,
                                                                         force=True)
    total = 0
    for what, gone in out.items():
        n = sum(b for _, b in gone)
        total += n
        print(f"{what}: {len(gone)}, {n / 1e9:.2f} GB" + (" (dry run: nothing removed)" if args.dry_run else ""))
        for name, b in gone[:args.list]:
            print(f"  {name} {b / 1e6:.0f} MB")
    print(f"total {total / 1e9:.2f} GB")
    return 0


def find_jar(class_hash: str) -> Path | None:
    for meta in (store.DATA / "bots").glob("*.json"):
        if json.loads(meta.read_text()).get("class_hash") == class_hash:
            return meta.with_suffix(".jar")
    return None


def cmd_replay(args) -> int:
    d = run_dir(args.id)
    rec = game_record(d, args.game)
    if rec.get("error"):
        raise store.SetupError(f"game {args.game!r} of run {d.name} failed: {rec['error']}")
    resolver = play.Resolver()
    for ref, h in ((rec["a"], rec["a_hash"]), (rec["b"], rec["b_hash"])):
        jar = find_jar(h)
        if jar is None:
            raise store.SetupError(f"the build of {ref} that played this game ({h}) is no longer in the store")
        resolver.bots[ref] = (jar, h)
    out = Path(args.out) if args.out else d / "replays" / f"{play.safe(rec['id'])}.bc26"
    out.parent.mkdir(parents=True, exist_ok=True)
    spec = {k: rec[k] for k in ("id", "a", "b", "map", "seed", "engine")}
    # one game: no need to queue for a slot
    runner = play.Runner(1, d, use_cache=False, backend=args.backend, guard=0, slots=False)
    runner.resolver.bots.update(resolver.bots)
    job = runner.resolver.job(spec, out.resolve())
    done = threading.Event()
    res = {}
    runner.submit(job, lambda r: (res.update(r), done.set()))
    done.wait()
    runner.close()
    if res.get("error"):
        print(f"replay failed: {res['error']}")
        return 1
    same = res["digest"] == rec["digest"]
    print(f"{out}\n{rec['map']} s{rec['seed']} {rec['a']}(A) vs {rec['b']}(B): {res['winner']} {res['win_type']} "
          f"r{res['round']}; " + ("identical to the recorded game" if same else
                                   f"DIFFERENT from the recorded game ({rec['digest']} vs {res['digest']})"))
    print(timeline(rec))
    return 0 if same else 1


def select_games(d: Path, which: str) -> list[dict]:
    """A run's finished games for `replays`: all, the decisive cells (ours won or lost both seats), the games ours
    lost, or (with a ref) the cells where ours' score differs from the ref's."""
    recs = [json.loads(l) for l in open(d / "results.jsonl")]
    recs = [r for r in recs if not r.get("error")]
    if which == "all":
        return recs
    if which == "lost":
        return [r for r in recs if r.get("role", "o") == "o" and r["winner"] != r.get("seat", "A")]
    cells: dict = {}
    for r in recs:
        cells.setdefault((r.get("test", "ab"), r.get("cell")), []).append(r)
    out = []
    for games in cells.values():
        score = {"o": 0.0, "r": 0.0}
        n = {"o": 0, "r": 0}
        for r in games:
            role = r.get("role", "o")
            score[role] += r["winner"] == r.get("seat", "A")
            n[role] += 1
        if n["o"] != 2:
            continue
        if which == "decisive" and score["o"] in (0, 2):
            out += games
        elif which == "changed" and n["r"] == 2 and score["o"] != score["r"]:
            out += games
    return out


def cmd_replays(args) -> int:
    d = run_dir(args.id)
    meta = json.loads((d / "run.json").read_text())
    recs = select_games(d, args.cells)
    out = d / "replays"
    out.mkdir(exist_ok=True)
    todo = [r for r in recs if not (out / f"{play.safe(r['id'])}.bc26").exists()]
    print(f"{len(recs)} games selected ({args.cells}), {len(recs) - len(todo)} already have replays, "
          f"{len(todo)} to play", file=sys.stderr)
    runner = make_runner(d, {"remote": args.remote, "where": args.where, "workers": args.workers,
                             "backend": args.backend, "guard": 0}, replay=True)
    for ref, (jar, h) in meta.get("bots", {}).items():
        runner.resolver.bots[ref] = (Path(jar), h)
    index = open(out / "index.jsonl", "a")
    lock = threading.Lock()
    done = threading.Event()
    left = {"n": len(todo), "bad": 0}

    def record(rec: dict, res: dict) -> None:
        with lock:
            same = res.get("digest") == rec["digest"]
            left["bad"] += not same
            index.write(json.dumps({k: rec.get(k) for k in ("id", "test", "cell", "map", "seed", "engine", "a", "b",
                                                             "role", "seat", "winner", "win_type", "round")}
                                   | {"replay": res.get("replay"), "same_game": same, "backend": res.get("backend"),
                                      "error": res.get("error")}) + "\n")
            index.flush()
            left["n"] -= 1
            if left["n"] == 0:
                done.set()

    for rec in todo:
        for ref, h in ((rec["a"], rec["a_hash"]), (rec["b"], rec["b_hash"])):
            if ref not in runner.resolver.bots:
                jar = find_jar(h)
                if jar is None:
                    raise store.SetupError(f"the build of {ref} that played {rec['id']} ({h}) is no longer in the store")
                runner.resolver.bots[ref] = (jar, h)
        spec = {k: rec[k] for k in ("id", "a", "b", "map", "seed", "engine")}
        job = runner.resolver.job(spec, (out / f"{play.safe(rec['id'])}.bc26").resolve())
        runner.submit(job, lambda r, rec=rec: record(rec, r))
    if todo:
        done.wait()
    runner.close(wait=not args.remote)
    getattr(runner, "drain", lambda: None)()  # remote: its open batches cancelled
    print(f"{len(todo)} replays in {out}/ ({left['bad']} not identical to the recorded game); index: "
          f"{out / 'index.jsonl'}")
    return 1 if left["bad"] else 0


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


def cmd_build(args) -> int:
    """Builds now the native binaries tests of these bots will need (each with the sparring bots), so the tests find
    them ready: after a merge, `tools/games build challenger@origin/main`; while working, `tools/games build`."""
    resolver = play.Resolver()
    planner = native.Planner(resolver)
    code = 0
    for ref in args.bots:
        ref = pin(ref)
        job = resolver.job({"id": "build", "a": ref, "b": "awubot", "map": "DefaultSmall", "engine": args.engine})
        plan = planner.plan(job)
        if plan is None:
            print(f"{ref}: the native engine can't play it here (no toolchain, or another build of awubot)")
            code = 1
            continue
        try:
            key, path = native.build(plan)
        except native.BuildError as e:
            print(f"{ref}: build failed: {e}")
            code = 1
            continue
        print(f"{ref}: binary {key} ({path})" + (f", on the volume: {imperial.ship_binary(key, path)}"
                                                  if args.remote else ""))
    return code


def cmd_pools(args) -> int:
    every = list(pools.POOLS.items()) + [("points", pools.resolve("points")[0])]
    every += [(n, pools.class_pool(n)) for n in pools.CLASS_POOLS]
    every += [(f.stem, pools.list_pool(f)) for f in sorted(pools.LIST_DIR.glob("*.txt"))]
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
                        help="cap, then the verdict is inconclusive (default 4/delta^2: 400 at 0.10, 1600 at 0.05, "
                             "above the simulated p90; smoke pool: one pass)")
        sp.add_argument("--seed-base", type=int, default=0,
                        help="0: pass 0 plays the official seeds, later passes derived seeds; N: all derived from N")
        sp.add_argument("--workers", type=int, default=None,
                        help="local: parallel JVMs (default 8); --remote: games in flight (default 256)")
        sp.add_argument("--timeout", type=float, default=None,
                        help=f"seconds per game (default {play.DEFAULT_TIMEOUT:.0f}; --remote 3600)")
        sp.add_argument("--replays", choices=["none", "losses", "all"], default="none",
                        help="write replays of games played (losses: games A lost); any game: tools/games replay")
        sp.add_argument("--remote", action="store_true",
                        help="play on DoC's machines through tools/run (the compute skill); the cache is shared")
        sp.add_argument("--where", help="--remote backends, e.g. condor,slurm (default: tools/run's)")
        sp.add_argument("--fg", action="store_true", help="run in the foreground and print the report")
        backend(sp)
        sp.add_argument("--guard", type=float, default=None,
                        help=f"exactness guard: share of native games also played on Java and compared (default "
                             f"{native.GUARD_RATE:g}, plus each binary's first game)")
        sp.add_argument("--no-cache", dest="cache", action="store_false",
                        help="play every game even if cached (measurements); results still enter the cache")

    def backend(sp):
        sp.add_argument("--backend", choices=["native", "java"], default="native",
                        help="native (default): the native engine (engine/) where it can play the game, else Java; "
                             "java: the official Java engine only")

    sp = sub.add_parser("ab", help="sequential paired test of A against B (by default: is A better than main?)")
    sp.add_argument("a")
    sp.add_argument("b")
    sp.add_argument("--ref", default="auto",
                    help="REF plays B on the same cells and the test is on A - REF. auto (default): "
                         "challenger@origin/main when A is challenger, else none; none: A against B alone")
    sp.add_argument("--test", dest="mode", choices=["gain", "harm"], default="gain",
                    help="gain (default): H1 = A better by delta, passes on H1; harm: H1 = A worse by delta, "
                         "passes on H0")
    sp.add_argument("--pool", default="quick")
    sp.add_argument("--min-cells", type=int, default=0, help="default: one pass over the pool")
    common(sp, 0.10)

    sp = sub.add_parser("gauntlet", help="harm test of A against each sparring bot, paired with REF's games")
    sp.add_argument("a", nargs="?", default="challenger")
    sp.add_argument("--ref", default="auto",
                    help="auto (default): challenger@origin/main when A is challenger, else awubot")
    sp.add_argument("--opponents", default=",".join(MAIN_OPPONENTS + OTHER_OPPONENTS))
    sp.add_argument("--main", default=",".join(MAIN_OPPONENTS), help="opponents tested sequentially on --main-pool")
    sp.add_argument("--main-pool", default="goal")
    sp.add_argument("--rest-pool", default="quick")
    sp.add_argument("--rest-passes", type=int, default=2, help="passes over --rest-pool for the other opponents")
    common(sp, 0.10)

    sp = sub.add_parser("wait", help="block until a run stops (or, of several, the first with --any, else all), "
                                     "then print its report")
    sp.add_argument("ids", nargs="+", metavar="ID")
    sp.add_argument("--any", action="store_true",
                    help="return when the first of the runs stops (at once if one has), with its report and exit "
                         "code, naming the ones still running: wait again on those")
    sp.add_argument("--games", action="store_true")
    sp.add_argument("--timeout", type=float, default=0, help="give up after this many seconds (exit 5)")
    sp = sub.add_parser("show", help="print a run's report")
    sp.add_argument("id")
    sp.add_argument("--games", action="store_true", help="list every game")
    sp.add_argument("--game", help="one game's record and per-100-round timeline")
    sp.add_argument("--json", action="store_true")
    sp = sub.add_parser("list", help="recent runs")
    sp.add_argument("-n", type=int, default=20)
    sp = sub.add_parser("stop", help="stop a run")
    sp.add_argument("id")
    sp = sub.add_parser("replay", help="replay one game of a run with a replay file")
    sp.add_argument("id")
    sp.add_argument("game", help="game id, from tools/games show ID --games")
    sp.add_argument("--out")
    backend(sp)
    sp = sub.add_parser("replays", help="replay files for a run's games, played again deterministically")
    sp.add_argument("id")
    sp.add_argument("--cells", choices=["all", "decisive", "lost", "changed"], default="decisive",
                    help="decisive (default): cells ours won or lost in both seats; lost: games ours lost; changed: "
                         "cells where ours and the ref differ; all")
    sp.add_argument("--workers", type=int, default=8)
    sp.add_argument("--remote", action="store_true")
    sp.add_argument("--where")
    backend(sp)
    sp = sub.add_parser("retry", help="play a finished run's failed games again and rescore it")
    sp.add_argument("id")
    sp.add_argument("--workers", type=int, default=8)
    sp.add_argument("--remote", action="store_true")
    sp.add_argument("--where")
    backend(sp)
    sp = sub.add_parser("diff", help="games whose outcome changed between two runs")
    sp.add_argument("id1")
    sp.add_argument("id2")
    sp.add_argument("--all", action="store_true", help="also games with the same winner but another ending")
    sp = sub.add_parser("build", help="build the native binaries of these bots now (default: challenger and main's)")
    sp.add_argument("bots", nargs="*", default=["challenger", "challenger@origin/main"])
    sp.add_argument("--engine", choices=sorted(store.ENGINES), default=store.DEFAULT_ENGINE)
    sp.add_argument("--remote", action="store_true", help="also put them on the volume for --remote runs")
    sp = sub.add_parser("pools", help="list map pools")
    sp.add_argument("-v", action="store_true")
    sp = sub.add_parser("simulate", help="SPRT cells needed, from the trinomial model")
    sp.add_argument("--delta", type=float, nargs="+", default=[0.05, 0.10, 0.20])
    sp.add_argument("--split", type=float, nargs="+", default=[0.5, 0.7, 0.9])
    sp.add_argument("--alpha", type=float, default=0.05)
    sp.add_argument("--min-cells", type=int, default=16)
    sp.add_argument("--max-cells", type=int, default=2000)
    sp.add_argument("--runs", type=int, default=1000)
    sp = sub.add_parser("keep", help="keep a run's replays past the sweep (retain.py); --off undoes it")
    sp.add_argument("id")
    sp.add_argument("--off", action="store_true")
    sp = sub.add_parser("sweep", help="free disk now: old replays, unused binaries, dead engine build trees, "
                                      "leftover ship folders, this checkout's old run results (done hourly anyway)")
    sp.add_argument("--dry-run", action="store_true", help="say what would go, remove nothing")
    sp.add_argument("--list", type=int, default=0, metavar="N", help="name the first N of each")
    sp = sub.add_parser("_drive")
    sp.add_argument("id")
    args = ap.parse_args(argv)

    if getattr(args, "remote", None) is not None and hasattr(args, "timeout"):
        args.workers = args.workers or (256 if args.remote else 8)
        args.timeout = args.timeout or (3600 if args.remote else play.DEFAULT_TIMEOUT)
    try:
        if args.cmd == "ab":
            params = {k: getattr(args, k) for k in ("a", "b", "pool", "engine", "delta", "alpha", "beta",
                                                      "min_cells", "seed_base", "workers", "timeout", "replays",
                                                      "remote", "where", "mode", "backend", "guard", "cache")}
            params["ref"] = default_ref(args.a, args.ref)
            params["max_cells"] = args.max_cells or (0 if args.pool == "smoke" else round(4 / args.delta ** 2))
            return start("ab", params, args.fg)
        if args.cmd == "gauntlet":
            opps = [o for o in args.opponents.split(",") if o]
            ref = default_ref(args.a, args.ref) or ("awubot" if args.ref == "auto" else None)
            if ref is None:
                raise store.SetupError("the gauntlet needs a ref")
            params = {"a": args.a, "ref": ref, "opponents": opps, "main": args.main.split(","),
                      "main_pool": args.main_pool, "rest_pool": args.rest_pool, "rest_passes": args.rest_passes,
                      "engine": args.engine, "delta": args.delta, "alpha": args.alpha, "beta": args.beta,
                      "max_cells": args.max_cells or round(4 / args.delta ** 2), "seed_base": args.seed_base, "workers": args.workers,
                      "timeout": args.timeout, "replays": args.replays, "remote": args.remote, "where": args.where,
                      "backend": args.backend, "guard": args.guard, "cache": args.cache}
            return start("gauntlet", params, args.fg)
        if args.cmd == "_drive":
            return drive(run_dir(args.id))
        if args.cmd not in ("sweep", "keep"):
            try:  # at most hourly: bounded disk (retain.py)
                retain.sweep(say=lambda m: print(f"tools/games: {m}", file=sys.stderr))
            except OSError as e:
                print(f"tools/games: sweep: {e}", file=sys.stderr)
        return {"wait": cmd_wait, "show": cmd_show, "list": cmd_list, "stop": cmd_stop, "replay": cmd_replay,
                "replays": cmd_replays, "retry": cmd_retry, "build": cmd_build, "keep": cmd_keep, "sweep": cmd_sweep,
                "diff": cmd_diff, "pools": cmd_pools, "simulate": cmd_simulate}[args.cmd](args)
    except store.SetupError as e:
        print(f"tools/games: {e}", file=sys.stderr)
        return 1
