"""Map classes of docs/theory/t4-maps.md §3, computed from the map files the way T4's classifier did
(~/battlecode26-data/theory/t4/classify.py, catclock.py, classes2.py), stdlib only. A map can be in several.

  FACEOFF    a rat's path from king A's spawn ring to king B's ring costs <= 12 (dirt tile 3.5, else 1; 8-connected)
  NOCONTACT  no such path at all, even through dirt
  CHOKE      neither of those, and a vertex cut of <= 4 tiles (dirt passable) separates the kings' 5x5 boxes
  CATPATH    a cat's straight-line waypoint walk (2 rounds a step, walls ignored) touches a king's 3x3 by round 320
  MINEPOOR   <= 4 cheese mines                DIRT   >= 20% dirt tiles                OPEN   none of the above

    python3 -m bcgames.mapclass [MAP...]      (from tools/) print each map's features and classes
Results are cached per map file hash under ~/battlecode26-data/games/mapclass/.
"""
from __future__ import annotations

import heapq
import json
import subprocess
import sys
from collections import deque

from . import store

DIRT_COST = 3.5
FACEOFF_PATH = 12
CHOKE_CUT = 4
CAT_CLOCK = 320
MINEPOOR_MINES = 4
DIRT_PERCENT = 20
D8 = [(-1, -1), (-1, 0), (-1, 1), (0, -1), (0, 1), (1, -1), (1, 0), (1, 1)]
INF = 10 ** 9


def dump(names: list[str]) -> dict[str, dict]:
    """The engine's view of each map (bcplay.MapInfo)."""
    runner_dir, _ = store.runner_classes()
    proc = subprocess.run(["java", "-cp", f"{store.engine_jar(store.DEFAULT_ENGINE)}:{runner_dir}", "bcplay.MapInfo",
                           *[str(store.map_file(n)) for n in names]], capture_output=True, text=True, check=True)
    return {m["name"]: m for m in map(json.loads, proc.stdout.splitlines())}


def ring(k, w, h, r=2):
    return [(k[0] + dx, k[1] + dy) for dx in range(-r, r + 1) for dy in range(-r, r + 1)
            if max(abs(dx), abs(dy)) == r and 0 <= k[0] + dx < w and 0 <= k[1] + dy < h]


def path_cost(grid, w, h, a, b) -> float:
    """Cheapest path from a's spawn ring to b's ring; dirt costs DIRT_COST, walls block. -1: none."""
    dist = [[INF] * w for _ in range(h)]
    pq = []
    for x, y in ring(a, w, h):
        if grid[y][x] != "#":
            dist[y][x] = 0
            pq.append((0, x, y))
    heapq.heapify(pq)
    while pq:
        d, x, y = heapq.heappop(pq)
        if d > dist[y][x]:
            continue
        for dx, dy in D8:
            nx, ny = x + dx, y + dy
            if 0 <= nx < w and 0 <= ny < h and grid[ny][nx] != "#":
                nd = d + (DIRT_COST if grid[ny][nx] == "d" else 1)
                if nd < dist[ny][nx]:
                    dist[ny][nx] = nd
                    heapq.heappush(pq, (nd, nx, ny))
    best = min([dist[y][x] for x, y in ring(b, w, h)] + [INF])
    return round(best, 1) if best < INF else -1


def min_cut(grid, w, h, a, b, cap: int) -> int:
    """Fewest passable tiles whose removal separates the kings' 5x5 boxes (8-connected, dirt passable; box tiles may
    be cut), counted up to cap + 1. Vertex-split max flow by augmenting paths. -1: a box has no passable tile."""
    def ok(x, y):
        return 0 <= x < w and 0 <= y < h and grid[y][x] != "#"

    def box(k):
        return [(k[0] + dx, k[1] + dy) for dx in range(-2, 3) for dy in range(-2, 3) if ok(k[0] + dx, k[1] + dy)]

    src, snk = box(a), box(b)
    if not src or not snk:
        return -1
    S, T = -1, -2
    res: dict[int, dict[int, int]] = {S: {}, T: {}}  # residual capacities; node 2i = tile i in, 2i + 1 = out

    def arc(u, v, c):
        res.setdefault(u, {})[v] = res.get(u, {}).get(v, 0) + c
        res.setdefault(v, {}).setdefault(u, 0)

    for y in range(h):
        for x in range(w):
            if not ok(x, y):
                continue
            i = x + y * w
            arc(2 * i, 2 * i + 1, 1)
            for dx, dy in D8:
                if ok(x + dx, y + dy):
                    arc(2 * i + 1, 2 * (x + dx + (y + dy) * w), INF)
    for x, y in src:
        arc(S, 2 * (x + y * w), INF)
    for x, y in snk:
        arc(2 * (x + y * w) + 1, T, INF)
    flow = 0
    while flow <= cap:
        parent = {S: None}
        q = deque([S])
        while q and T not in parent:
            u = q.popleft()
            for v, c in res[u].items():
                if c > 0 and v not in parent:
                    parent[v] = u
                    q.append(v)
        if T not in parent:
            break
        v = T
        while parent[v] is not None:  # every augmenting path carries 1 (a tile's in->out arc)
            u = parent[v]
            res[u][v] -= 1
            res[v][u] += 1
            v = u
        flow += 1
    return flow


def cat_clock(m) -> int:
    """Earliest round a cat walking its waypoints in straight Chebyshev steps (2 rounds a step, twice round the loop,
    from its spawn) touches a king's 3x3. -1: never."""
    kings = [(b["x"], b["y"]) for b in m["body_list"] if b["type"] == "RAT_KING"]
    cats = {b["id"]: (b["x"], b["y"]) for b in m["body_list"] if b["type"] == "CAT"}
    best = INF
    for c in m["cat_waypoints"]:
        x, y = cats.get(c["id"], tuple(c["wp"][0]))
        t = 0
        for tx, ty in [tuple(p) for p in c["wp"]] * 2:
            while (x, y) != (tx, ty):
                x += (tx > x) - (tx < x)
                y += (ty > y) - (ty < y)
                t += 2
                for kx, ky in kings:
                    gap = max(max(kx - 1 - (x + 1), x - (kx + 1)), max(ky - 1 - (y + 1), y - (ky + 1)))
                    if gap <= 0 and t < best:
                        best = t
    return best if best < INF else -1


def features(m) -> dict:
    grid, w, h = m["grid"], m["width"], m["height"]
    kings = {b["team"]: (b["x"], b["y"]) for b in m["body_list"] if b["type"] == "RAT_KING"}
    a, b = kings["A"], kings["B"]
    area = w * h
    return {"path": path_cost(grid, w, h, a, b), "cut": min_cut(grid, w, h, a, b, CHOKE_CUT),
            "cat_clock": cat_clock(m), "mines": sum(r.count("M") for r in grid),
            "dirt_pct": round(100 * sum(r.count("d") for r in grid) / area, 1)}


def classes_of(f: dict) -> list[str]:
    t = []
    if f["path"] < 0:
        t.append("NOCONTACT")
    elif f["path"] <= FACEOFF_PATH:
        t.append("FACEOFF")
    elif f["cut"] <= CHOKE_CUT:
        t.append("CHOKE")
    if 0 <= f["cat_clock"] <= CAT_CLOCK:
        t.append("CATPATH")
    if f["mines"] <= MINEPOOR_MINES:
        t.append("MINEPOOR")
    if f["dirt_pct"] >= DIRT_PERCENT:
        t.append("DIRT")
    return t or ["OPEN"]


def classify(names: list[str]) -> dict[str, dict]:
    """name -> {features..., classes}, cached by map file hash."""
    cache = store.DATA / "mapclass"
    cache.mkdir(parents=True, exist_ok=True)
    out, todo = {}, []
    for n in names:
        p = cache / f"{store.map_hash(n)}.json"
        if p.exists():
            out[n] = json.loads(p.read_text())
        else:
            todo.append(n)
    if todo:
        for n, m in dump(todo).items():
            f = features(m)
            f["classes"] = classes_of(f)
            (cache / f"{store.map_hash(n)}.json").write_text(json.dumps(f))
            out[n] = f
    return out


def main(argv=None) -> int:
    names = (argv if argv is not None else sys.argv[1:]) or sorted(p.stem for p in (store.ROOT / "maps").glob("*.map26"))
    for n, f in sorted(classify(names).items(), key=lambda kv: kv[0].lower()):
        print(f"{n:28s} path {f['path']:6} cut {f['cut']:2d} cat {f['cat_clock']:5d} mines {f['mines']:2d} "
              f"dirt {f['dirt_pct']:5}%  {' '.join(f['classes'])}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
