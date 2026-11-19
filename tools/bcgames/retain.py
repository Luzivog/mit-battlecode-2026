"""Bounded disk: what tools/games and tools/play keep, pruned by sweep() when a tool starts, at most every SWEEP_S.

- Replays: a run's replays/ goes REPLAY_KEEP_S after the run's last activity (its state, its replays, their index),
  sooner when all runs' replays pass REPLAY_CAP (oldest first, never one active within REPLAY_MIN_S), unless the run is
  kept (`tools/games keep ID`: a file `keep` in it). Its results stay, and `tools/games replays ID` plays them again.
  Sealed runs' replays go the same way, by file times alone: nothing under SEALED-TEST-DO-NOT-READ is read.
- Native binaries: native.gc() (count, age and size; never one in use).
- Engine build trees: ~/battlecode26-data/engine/build/gen-VERSION-H, H = sha256 of the checkout that built it
  (engine/build.py), go once no checkout of this repository (`git worktree list`) nor enginebench's export of main
  hashes to H, and nothing was built there for GEN_IDLE_S.
- This checkout's `.bcgames-ship/`: each remote test ships its own folder (imperial.py) and deletes it when it ends;
  one left for SHIP_IDLE_S (its driver died), or the old shared layout, goes.
"""
from __future__ import annotations

import hashlib
import os
import shutil
import subprocess
import time
from pathlib import Path

from . import native, store

SWEEP_S = 3600
REPLAY_KEEP_S = 24 * 3600
REPLAY_CAP = 15 << 30
REPLAY_MIN_S = 2 * 3600
GEN_IDLE_S = 24 * 3600
SHIP_IDLE_S = 24 * 3600
RUNS = store.DATA / "runs"
SEALED_RUNS = store.DATA / "SEALED-TEST-DO-NOT-READ" / "runs"
ENGINE_BUILD = Path.home() / "battlecode26-data" / "engine" / "build"
BENCH_CHECKOUT = Path.home() / "battlecode26-data" / "enginebench" / "main" / "checkout"
STAMP = store.DATA / ".swept"


def mtime(p: Path) -> float:
    try:
        return p.stat().st_mtime
    except OSError:
        return 0.0


def tree_bytes(p: Path) -> int:
    n = 0
    for dirpath, _, files in os.walk(p):
        for f in files:
            try:
                n += os.lstat(os.path.join(dirpath, f)).st_size
            except OSError:
                pass
    return n


def replays(now: float, dry: bool) -> list[tuple[str, int]]:
    """Replay folders removed: (run, bytes)."""
    found = []
    for base in (RUNS, SEALED_RUNS):
        try:
            runs = list(base.iterdir())
        except OSError:
            continue
        for d in runs:
            r = d / "replays"
            if not r.is_dir():
                continue
            last = max(mtime(d / "state.json"), mtime(r), mtime(r / "index.jsonl"))
            found.append((last, d, r, (d / "keep").exists(), tree_bytes(r)))
    total = sum(x[4] for x in found)
    gone = []
    for last, d, r, kept, n in sorted(found, key=lambda x: x[0]):
        if kept or now - last < REPLAY_MIN_S:
            continue
        if now - last > REPLAY_KEEP_S or total > REPLAY_CAP:
            if not dry:
                shutil.rmtree(r, ignore_errors=True)
            gone.append((d.name, n))
            total -= n
    return gone


def checkouts() -> list[Path]:
    """Every checkout of this repository (git worktree list), and enginebench's export of main."""
    out = [BENCH_CHECKOUT]
    p = subprocess.run(["git", "-C", str(store.ROOT), "worktree", "list", "--porcelain"], capture_output=True,
                       text=True)
    out += [Path(x[len("worktree "):]) for x in p.stdout.splitlines() if x.startswith("worktree ")]
    return out


def gen_trees(now: float, dry: bool) -> list[tuple[str, int]]:
    """engine/build.py's generated-files trees of checkouts that are gone: (tree, bytes)."""
    try:
        trees = [g for g in ENGINE_BUILD.iterdir() if g.is_dir() and g.name.startswith("gen-")]
    except OSError:
        return []
    live = {hashlib.sha256(str(c).encode()).hexdigest()[:8] for c in checkouts() if c.exists()}
    gone = []
    for g in trees:
        last = max(mtime(g), mtime(g / ".ninja_log"), mtime(g / "build.ninja"))
        if g.name.rsplit("-", 1)[-1] in live or now - last < GEN_IDLE_S:
            continue
        n = tree_bytes(g)
        if not dry:
            shutil.rmtree(g, ignore_errors=True)
        gone.append((g.name, n))
    return gone


def ship(now: float, dry: bool, root: Path = store.ROOT) -> list[tuple[str, int]]:
    """This checkout's .bcgames-ship leftovers: a test's folder idle SHIP_IDLE_S, the old shared layout's folders."""
    top = root / ".bcgames-ship"
    try:
        items = list(top.iterdir())
    except OSError:
        return []
    gone = []
    for x in items:
        last = max([mtime(x)] + ([mtime(y) for y in x.iterdir()] if x.is_dir() else []))
        if now - last < SHIP_IDLE_S:
            continue
        n = tree_bytes(x) if x.is_dir() else x.stat().st_size
        if not dry:
            if x.is_dir():
                shutil.rmtree(x, ignore_errors=True)
            else:
                x.unlink(missing_ok=True)
        gone.append((x.name, n))
    return gone


def sweep(force: bool = False, dry: bool = False, say=None) -> dict[str, list[tuple[str, int]]]:
    """Every step above, unless one ran within SWEEP_S (FORCE: now; DRY: say what would go, remove nothing)."""
    now = time.time()
    if not force and now - mtime(STAMP) < SWEEP_S:
        return {}
    if not dry:
        STAMP.parent.mkdir(parents=True, exist_ok=True)
        STAMP.touch()
    out = {"replays": replays(now, dry), "binaries": native.gc(dry), "engine build trees": gen_trees(now, dry),
           "ship folders": ship(now, dry)}
    if say:
        for what, gone in out.items():
            if gone:
                say(f"sweep: {len(gone)} {what} {'to remove' if dry else 'removed'}, "
                    f"{sum(n for _, n in gone) / 1e9:.2f} GB")
    return out
