"""tools/book/book: the per-map book of challenger (tools/book/AGENTS.md).

The book (src/challenger/book.jsonl) holds entries (map, seat, constant of Tune.java, value) and per-map data. `gen`
writes src/challenger/Book.java from it, and drops `final` from the Tune.java constants an entry sets, so that only
those become settable per robot at its first turn. A robot knows its map from what its first king sees at round 1:
the map's size, the king's team, location and ID, and every wall and dirt tile of its r^2 25 view.
"""
from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
import time
from pathlib import Path

TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))
from bcgames import pools, store  # noqa: E402
from bctune import tune  # noqa: E402

import bookfit  # noqa: E402

ROOT = TOOLS.parent
BOOK = ROOT / "src/challenger/book.jsonl"
BOOK_JAVA = ROOT / "src/challenger/Book.java"
TUNE_JAVA = ROOT / "src/challenger/Tune.java"
VIEW_CACHE = store.DATA / "bookview"
CONFIRM_DIR = tune.DATA.parent / "book"  # ~/battlecode26-data/book: confirm runs
GAMES = TOOLS / "games"
TUNE = TOOLS / "tune"
SEATS = {"A": ["A"], "B": ["B"], "both": ["A", "B"]}
# declared data: a scalar (its default off the book), a tile set (row bitmasks: bit x of NAME[y]), a tile list, ints
DATA_TYPES = {"int": "int", "tileset": "long[]", "tiles": "MapLocation[]", "ints": "int[]"}
VIEW_R2 = 25  # the king's vision, 360 degrees from its centre (docs/rules.md §7)
# the engine's symmetry -> CellTracker.Symmetry's ordinal (the bot's Horizontal is x -> W-1-x, the engine's VERTICAL;
# checked on the grids of all 150 maps)
SYMMETRY = {"ROTATIONAL": 1, "VERTICAL": 2, "HORIZONTAL": 3}
KEY_RE = re.compile(r"^[A-Z][A-Z0-9_]*$")


class BookError(Exception):
    pass


# ------------------------------------------------------------------------------------------------- map ids

def map_id(name: str) -> int:
    """The map's id: 1 + its index in the goal pool (the book's maps); 0 for any other map."""
    return pools.GOAL.index(name) + 1 if name in pools.GOAL else 0


def all_maps() -> list[str]:
    return sorted(p.stem for p in (ROOT / "maps").glob("*.map26"))


def view_of(m: dict, team: str) -> dict:
    """What the team's starting king knows at round 1: the switch key and the tile bitmasks Book.scan builds.
    Bit (dx + 5) * 11 + (dy + 5) of a 128-bit mask, as two longs (low, high)."""
    king = [b for b in m["body_list"] if b["type"] == "RAT_KING" and b["team"] == team]
    if len(king) != 1:
        raise BookError(f"{m['name']}: {len(king)} kings of team {team}")
    k = king[0]
    x, y, w, h = k["x"], k["y"], m["width"], m["height"]
    masks = {"#": 0, "d": 0}
    for dx in range(-5, 6):
        for dy in range(-5, 6):
            if dx * dx + dy * dy > VIEW_R2 or not (0 <= x + dx < w and 0 <= y + dy < h):
                continue
            c = m["grid"][y + dy][x + dx]
            if c in masks:
                masks[c] |= 1 << ((dx + 5) * 11 + dy + 5)
    split = lambda v: [v & (2 ** 64 - 1), v >> 64]  # noqa: E731
    return {"key": (1 if team == "B" else 0) << 24 | w << 18 | h << 12 | x << 6 | y, "id": k["id"],
            "walls": split(masks["#"]), "dirt": split(masks["d"]), "symmetry": SYMMETRY[m["symmetry"]]}


def views(names: list[str]) -> dict[str, dict[str, dict]]:
    """name -> {"A": view, "B": view}, cached by map file hash."""
    from bcgames import mapclass
    VIEW_CACHE.mkdir(parents=True, exist_ok=True)
    out, todo = {}, []
    for n in names:
        p = VIEW_CACHE / f"{store.map_hash(n)}.json"
        if p.exists():
            out[n] = json.loads(p.read_text())
        else:
            todo.append(n)
    for i in range(0, len(todo), 40):
        for n, m in mapclass.dump(todo[i:i + 40]).items():
            v = {t: view_of(m, t) for t in "AB"}
            (VIEW_CACHE / f"{store.map_hash(n)}.json").write_text(json.dumps(v))
            out[n] = v
    return out


def twins(vs: dict[str, dict[str, dict]]) -> dict[tuple[str, str], list[tuple[str, str]]]:
    """(map, seat) -> the other (map, seat) pairs of maps/ that look the same at round 1."""
    by: dict[str, list[tuple[str, str]]] = {}
    for n, v in vs.items():
        for t in "AB":
            by.setdefault(json.dumps(v[t], sort_keys=True), []).append((n, t))
    return {p: [q for q in g if q != p] for g in by.values() for p in g}


# --------------------------------------------------------------------------------------------------- the book

def load(path: Path = BOOK) -> dict:
    """The book: {"declare": {NAME: decl}, "entries": [entry]}. One JSON object a line; blank and # lines skipped."""
    decl, entries = {}, []
    for i, line in enumerate(path.read_text().splitlines(), 1):
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        try:
            e = json.loads(line)
        except json.JSONDecodeError as err:
            raise BookError(f"{path.name}:{i}: not JSON: {err}")
        e["line"] = i
        if "declare" in e:
            decl[e["declare"]] = e
        else:
            entries.append(e)
    return {"declare": decl, "entries": entries}


def validate(book: dict, params: dict) -> list[str]:
    """Problems of the book's entries against Tune.java's constants (PARAMS: tune.parse)."""
    errs = []
    for name, d in book["declare"].items():
        if not KEY_RE.match(name) or d.get("type") not in DATA_TYPES or not d.get("meaning"):
            errs.append(f"line {d['line']}: a declaration needs an UPPER_CASE name, a type in {sorted(DATA_TYPES)} "
                        f"and a meaning")
        if name in params:
            errs.append(f"line {d['line']}: {name} is a Tune.java constant")
        if d.get("type") == "int" and not (isinstance(d.get("default"), int) and not isinstance(d["default"], bool)):
            errs.append(f"line {d['line']}: an int datum needs an int default (its value off the book)")
    seen = {}
    for e in book["entries"]:
        where = f"line {e['line']}"
        if e.get("map") not in pools.GOAL:
            errs.append(f"{where}: map {e.get('map')!r} is not a goal map")
        if e.get("seat") not in SEATS:
            errs.append(f"{where}: seat must be A, B or both")
        if not isinstance(e.get("evidence"), list) or not all(isinstance(r, str) for r in e["evidence"]):
            errs.append(f"{where}: evidence must be a list of run IDs")
        if not isinstance(e.get("twins", []), list):
            errs.append(f"{where}: twins must be a list of map names")
        kinds = [k for k in ("set", "data") if k in e]
        if len(kinds) != 1:
            errs.append(f"{where}: an entry sets one Tune constant (set) or one declared datum (data)")
            continue
        name, v = e[kinds[0]], e.get("value")
        if kinds[0] == "set":
            p = params.get(name)
            if p is None:
                errs.append(f"{where}: {name} is not a tune constant of Tune.java")
            elif isinstance(v, bool) or not isinstance(v, (int, float)) or (p["type"] == "int" and v != int(v)):
                errs.append(f"{where}: {name} is {p['type']}: {v!r}")
            elif not p["lo"] <= v <= p["hi"]:
                errs.append(f"{where}: {name} = {v} is outside [{p['lo']}, {p['hi']}]")
        else:
            d = book["declare"].get(name)
            if d is None:
                errs.append(f"{where}: datum {name} is not declared")
            elif d["type"] == "int" and (isinstance(v, bool) or not isinstance(v, int) or not -2 ** 31 <= v < 2 ** 31):
                errs.append(f"{where}: {name} takes an int")
            elif d["type"] in ("tiles", "tileset") and not (isinstance(v, list) and all(
                    isinstance(t, list) and len(t) == 2 and all(isinstance(c, int) and 0 <= c < 60 for c in t)
                    for t in v)):
                errs.append(f"{where}: {name} takes a list of [x, y] tiles")
            elif d["type"] == "ints" and not (isinstance(v, list) and all(
                    isinstance(c, int) and not isinstance(c, bool) and -2 ** 31 <= c < 2 ** 31 for c in v)):
                errs.append(f"{where}: {name} takes a list of ints")
        for seat in SEATS.get(e.get("seat"), []):
            k = (e.get("map"), seat, name)
            if k in seen:
                errs.append(f"{where}: {name} on {k[0]} seat {seat} is also set on line {seen[k]}")
            seen[k] = e["line"]
    return errs


def map_seats(book: dict) -> list[tuple[str, str]]:
    """The (map, seat) pairs the book has entries for, in goal order."""
    s = {(e["map"], t) for e in book["entries"] for t in SEATS[e["seat"]]}
    return sorted(s, key=lambda p: (map_id(p[0]), p[1]))


def check_twins(book: dict, vs: dict) -> list[str]:
    """Every map-seat of the book must look unlike every other map-seat of maps/ at round 1, unless its entries name
    the twin (a visible map that copies the book map's start, e.g. its catless version)."""
    tw = twins(vs)
    vis = set(pools.visible())
    errs = []
    for m, t in map_seats(book):
        named = {n for e in book["entries"] if e["map"] == m for n in e.get("twins", [])}
        for n, u in tw[(m, t)]:
            if n not in vis:
                errs.append(f"{m} seat {t} looks the same at round 1 as a map outside the visible pools: no entry "
                            f"can go on it")
            elif n not in named:
                errs.append(f"{m} seat {t} looks the same at round 1 as {n} seat {u}: name it in the entries' "
                            f"twins to accept that the entries play there too")
    return errs


# ----------------------------------------------------------------------------------------------- generation

def booked(book: dict) -> set[str]:
    return {e["set"] for e in book["entries"] if "set" in e}


def render_tune(text: str, names: set[str]) -> str:
    """Tune.java with `final` dropped from the constants in NAMES and kept on every other."""
    out = []
    for line in text.splitlines(keepends=True):
        m = tune.LINE.match(line.rstrip("\n"))
        if m:
            head = re.sub(r"public static (final )?", "public static " if m["name"] in names else
                          "public static final ", m["head"], count=1)
            line = head + line[len(m["head"]):]
        out.append(line)
    return "".join(out)


def jlong(v: int) -> str:
    return f"0x{v:x}L"


def java_value(v, kind: str) -> str:
    if kind == "int":
        return str(int(v))
    if kind == "double":
        return repr(float(v))
    if kind == "tiles":
        return "new MapLocation[] {" + ", ".join(f"new MapLocation({x}, {y})" for x, y in v) + "}"
    return "new int[] {" + ", ".join(str(c) for c in v) + "}"


def render_book(book: dict, params: dict, vs: dict) -> str:
    seats = map_seats(book)
    groups: dict[int, list[tuple[str, str]]] = {}
    for m, t in seats:
        groups.setdefault(vs[m][t]["key"], []).append((m, t))
    L = ["package challenger;", "",
         "import battlecode.common.*;", "",
         "// Generated by tools/book/book gen from book.jsonl: never edit (tools/book/AGENTS.md). The first king",
         "// identifies the map at round 1 from its size, the king's team, location and ID and every wall and dirt",
         "// tile of its r² 25 view, and writes the map's id to slot 15; every other robot reads it at its first turn.",
         "// Only the map-seats the book has entries for are identified: elsewhere map is 0 and nothing runs but the",
         "// slot read. init runs right after Game.init, before any class that reads Tune is loaded.",
         "public final class Book {",
         "    public static int map; // the map's id (tools/book/book ids) on a map-seat with entries, else 0",
         "    public static int seat; // 0 team A, 1 team B (set where map != 0)",
         "    public static int symmetry; // the map's CellTracker.Symmetry ordinal (1 R, 2 H: x -> W-1-x, 3 V), 0 where map is 0"]
    for name, d in sorted(book["declare"].items()):
        if d["type"] == "int":
            L.append(f"    public static int {name}{' = ' + str(d['default']) if d['default'] else ''}; // {d['meaning']}"
                     f" ({d['default']} off the book)")
        elif d["type"] == "tileset":
            L.append(f"    public static long[] {name}; // {d['meaning']}: tile (x, y) in it iff {name} != null && "
                     f"({name}[y] >>> x & 1) != 0 (null off the book)")
        else:
            L.append(f"    public static {DATA_TYPES[d['type']]} {name}; // {d['meaning']} (null off the book)")
    L += ["",
          "    public static void init() throws GameActionException {",
          "        if (Game.isRatKing && Game.time == 1) {",
          "            map = identify();",
          "            if (map != 0) Game.rc.writeSharedArray(challenger.comms.GlobalArray.BOOK_MAP, map);",
          "        } else {",
          "            map = Game.rc.readSharedArray(challenger.comms.GlobalArray.BOOK_MAP);",
          "        }",
          "        if (map != 0) apply();",
          "    }", "",
          "    private static int identify() throws GameActionException {",
          "        switch ((Game.team == Team.B ? 1 << 24 : 0) | Game.mapWidth << 18 | Game.mapHeight << 12"
          " | Game.myLocX << 6 | Game.myLocY) {"]
    for key, ms in sorted(groups.items()):
        L.append(f"            case {key} -> {{")
        L.append("                scan();")
        for m, t in ms:
            v = vs[m][t]
            L.append(f"                // {m} seat {t}")
            L.append(f"                if (id == {v['id']} && w0 == {jlong(v['walls'][0])} && w1 == {jlong(v['walls'][1])}")
            L.append(f"                        && d0 == {jlong(v['dirt'][0])} && d1 == {jlong(v['dirt'][1])})"
                     f" return {map_id(m)};")
        L += ["                return 0;", "            }"]
    L += ["            default -> {", "                return 0;", "            }", "        }", "    }", "",
          "    private static int id;",
          "    private static long w0, w1, d0, d1;", "",
          "    // Bit (dx + 5) * 11 + (dy + 5) of each 128-bit mask (w0 low, w1 high; a long shift takes its count mod 64).",
          "    // At round 1 nothing flies, so an impassable tile is a wall or dirt; a passable one costs one call.",
          "    private static void scan() throws GameActionException {",
          "        id = Game.rc.getID();",
          "        MapInfo[] infos = Game.rc.senseNearbyMapInfos();",
          "        int base = (Game.myLocX - 5) * 11 + Game.myLocY - 5;",
          "        for (int k = infos.length; --k >= 0; ) {",
          "            MapInfo t = infos[k];",
          "            if (t.isPassable()) continue;",
          "            MapLocation l = t.getMapLocation();",
          "            int i = l.x * 11 + l.y - base;",
          "            if (t.isWall()) {",
          "                if (i < 64) w0 |= 1L << i; else w1 |= 1L << i;",
          "            } else if (i < 64) d0 |= 1L << i; else d1 |= 1L << i;",
          "        }",
          "    }", "",
          "    private static void apply() {",
          "        seat = Game.team == Team.A ? 0 : 1;",
          "        switch (map << 1 | seat) {"]
    for m, t in seats:
        L.append(f"            case {map_id(m) << 1 | (t == 'B')} -> {{ // {m} seat {t}")
        L.append(f"                symmetry = {vs[m][t]['symmetry']};")
        for e in book["entries"]:
            if e["map"] == m and t in SEATS[e["seat"]]:
                if "set" in e:
                    L.append(f"                Tune.{e['set']} = {java_value(e['value'], params[e['set']]['type'])};")
                else:
                    kind = book["declare"][e["data"]]["type"]
                    if kind == "tileset":
                        rows: dict[int, int] = {}
                        for x, y in e["value"]:
                            rows[y] = rows.get(y, 0) | 1 << x
                        L.append(f"                {e['data']} = new long[Game.mapHeight];")
                        L += [f"                {e['data']}[{y}] = {jlong(m)};" for y, m in sorted(rows.items())]
                    else:
                        L.append(f"                {e['data']} = {java_value(e['value'], kind)};")
        L.append("            }")
    L += ["            default -> {", "            }", "        }", "    }", "}", ""]
    return "\n".join(L)


def generate(book: dict | None = None) -> tuple[str, str]:
    """(Book.java, Tune.java) as the book says they should read."""
    book = load() if book is None else book
    tune_text = render_tune(TUNE_JAVA.read_text(), booked(book))
    params = tune.parse(tune_text)
    errs = validate(book, params)
    if errs:
        raise BookError("\n".join(errs))
    vs = views(sorted({m for m, _ in map_seats(book)}))
    return render_book(book, params, vs), tune_text



# ------------------------------------------------------------------------------------------------- matrix, fit

def pool_of(maps: list[str]) -> str:
    bad = [m for m in maps if m not in pools.GOAL]
    if bad:
        raise BookError(f"not goal maps: {bad}")
    return "map:" + ",".join(maps)


def cmd_matrix(a) -> int:
    """A tools/tune campaign of candidate values, played on the maps' map: pool with main on the same cells."""
    maps = [m for m in a.maps.split(",") if m]
    pool = pool_of(maps)
    argv = ["points", a.name, *sum((["--point", p] for p in a.point), [])] + (["--rev", a.rev] if a.rev else [])
    if tune.main(argv) != 0:
        return 1
    c = tune.load(a.name)
    c["book"] = {"maps": maps, "cells_per_map": a.cells_per_map, "seed_base": a.seed_base}
    for v in c["variants"]:
        dropped = point_over_book(v, maps)
        if dropped:
            print(f"{v['label']}: the point wins over the book's entries for {', '.join(dropped)} on the matrix's maps")
    tune.save(c)
    cmd = [str(TUNE), "play", a.name, "--seed-base", str(a.seed_base), "--pool", pool,
           "--cells", str(a.cells_per_map * len(maps))]
    print(" ".join(cmd), flush=True)
    return subprocess.call(cmd)


def point_over_book(v: dict, maps: list[str]) -> list[str]:
    """Make variant V's values win over the book on MAPS: rewrite its commit without the entries that set one of its
    constants on those maps (Book.java and Tune.java's finality regenerated). The reference keeps main's book.
    Returns the lines dropped ("MAP SEAT CONST")."""
    book_text = tune.git("show", f"{v['commit']}:src/challenger/book.jsonl")
    book = load_text(book_text)
    hit = [e for e in book["entries"] if e.get("set") in v["values"] and e["map"] in maps]
    if not hit:
        return []
    keep = {e["line"] for e in hit}
    lines = [ln for i, ln in enumerate(book_text.splitlines(), 1) if i not in keep]
    new_text = "\n".join(lines) + "\n"
    book = load_text(new_text)
    tune_text = render_tune(tune.git("show", f"{v['commit']}:src/challenger/Tune.java") + "\n", booked(book))
    java = render_book(book, tune.parse(tune_text), views(sorted({m for m, _ in map_seats(book)})))
    files = {"src/challenger/book.jsonl": new_text, "src/challenger/Book.java": java,
             "src/challenger/Tune.java": tune_text}
    import os
    import tempfile
    with tempfile.TemporaryDirectory(prefix="book-") as tmp:
        env = {"GIT_INDEX_FILE": os.path.join(tmp, "index")}
        tune.git("read-tree", v["commit"], env=env)
        for path, text in files.items():
            blob = tune.git("hash-object", "-w", "--stdin", inp=text.encode())
            tune.git("update-index", "--cacheinfo", f"100644,{blob},{path}", env=env)
        tree = tune.git("write-tree", env=env)
    parent = tune.git("rev-parse", f"{v['commit']}^")
    msg = tune.git("log", "-1", "--format=%B", v["commit"]) + "\nthe point wins over the book on the matrix's maps"
    sha = tune.git("commit-tree", tree, "-p", parent, "-m", msg,
                   env={"GIT_AUTHOR_DATE": "2000-01-01T00:00:00Z", "GIT_COMMITTER_DATE": "2000-01-01T00:00:00Z"})
    ref = next((r for r in tune.git("for-each-ref", "--format=%(refname)", "refs/tune").splitlines()
                if tune.git("rev-parse", r) == v["commit"]), None)
    if ref:
        tune.git("update-ref", ref, sha)
    v["commit"] = sha
    v["book_dropped"] = [f"{e['map']} {e['seat']} {e['set']}" for e in hit]
    return v["book_dropped"]


def matrix_diffs(c: dict, seed_base: int | None = None) -> dict:
    """candidate label -> (map, seat) -> {seed: d} from a campaign's runs (its reference's run and the variants')."""
    runs = [r for r in c["runs"] if seed_base is None or r["seed_base"] == seed_base]
    refs = [r for r in runs if r["variant"] == tune.REF]
    if not refs:
        raise BookError(f"campaign {c['name']}: no reference run yet")
    ref_rows = sum((bookfit.read_rows(tune.run_dir(r["id"]) / "results.jsonl") for r in refs), [])
    out = {}
    for r in runs:
        if r["variant"] != tune.REF:
            rows = bookfit.read_rows(tune.run_dir(r["id"]) / "results.jsonl")
            for ms, d in bookfit.pair(ref_rows, rows).items():
                out.setdefault(r["variant"], {}).setdefault(ms, {}).update(d)
    return out


def print_fit(f: dict, c: dict | None = None) -> None:
    vals = {v["label"]: ", ".join(f"{k}={x}" for k, x in v["values"].items()) for v in (c or {}).get("variants", [])}
    print(f"{len(f['candidates'])} candidates: " + "; ".join(f"{k} {vals.get(k, '')}" for k in f["candidates"]))
    print(f"empirical Bayes tau = {f['tau2'] ** 0.5:.3f} over {sum(len(r['cands']) for r in f['rows'])} estimates")
    print("per map-seat: cells n; the best of K: raw [95%], Sidak [0.95^(1/K)], selection-adjusted adj [95%], "
          "shrunk [95%], p_max; then every candidate's raw")
    for r in sorted(f["rows"], key=lambda r: -r["adj"]):
        b = next(x for x in r["cands"] if x["cand"] == r["best"])
        rest = "  ".join(f"{x['cand']} {x['raw']:+.3f}" for x in r["cands"])
        print(f"  {r['map']:24s} {r['seat']}  n {r['n']:3d}  best {r['best']:6s} {b['raw']:+.3f} [{b['lo']:+.3f}, "
              f"{b['hi']:+.3f}]  sidak [{b['lo_k']:+.3f}, {b['hi_k']:+.3f}]  adj {r['adj']:+.3f} [{r['adj_lo']:+.3f}, "
              f"{r['adj_hi']:+.3f}]  shrunk {b['shrunk']:+.3f} [{b['shrunk_lo']:+.3f}, {b['shrunk_hi']:+.3f}]  "
              f"p_max {r['p_max']:.3f}   | {rest}")


def cmd_fit(a) -> int:
    if a.simulate:
        for K in (1, 2, 4, 8):
            r = bookfit.simulate(K=K, mapseats=a.mapseats, n=a.n, q=a.q, reps=a.reps)
            print(f"null K {K}: best raw {r['null_best_raw']:+.3f}  adj {r['null_best_adj']:+.3f}  shrunk "
                  f"{r['null_best_shrunk']:+.3f};  picked (lower bound > 0): raw 95% {r['null_pick_raw95']:.3f}, "
                  f"Sidak {r['null_pick_sidak']:.3f}, adj {r['null_pick_adj']:.3f}, shrunk {r['null_pick_shrunk']:.3f}"
                  f";  p_max < 0.05 {r['null_pick_p_max']:.3f}")
        for eff in (0.1, 0.2):
            r = bookfit.simulate(K=4, mapseats=a.mapseats, n=a.n, q=a.q, reps=a.reps, effect=eff)
            print(f"planted +{eff:.2f} on one map-seat of {a.mapseats}, K 4: raw {r['planted_raw']:+.3f}  adj "
                  f"{r['planted_adj']:+.3f}  shrunk {r['planted_shrunk']:+.3f}  p_max < 0.05 in "
                  f"{r['planted_p_lt_05']:.2f} of matrices")
        return 0
    c = tune.load(a.name)
    f = bookfit.fit(matrix_diffs(c, a.seed_base), flips=a.flips)
    if a.json:
        print(json.dumps(f, indent=1))
    else:
        print_fit(f, c)
    return 0


# ------------------------------------------------------------------------------------------------- confirm

def start(bot: str, ref: str, pool: str, cells: int, seed_base: int, extra: list[str]) -> str:
    cmd = [str(GAMES), "ab", bot, "awubot", "--ref", ref, "--pool", pool, "--min-cells", str(cells),
           "--max-cells", str(cells), "--seed-base", str(seed_base), "--remote", *extra]
    p = subprocess.run(cmd, capture_output=True, text=True)
    if p.returncode != 0 or not p.stdout.strip():
        raise BookError(f"tools/games ab failed: {p.stderr[-1500:]}")
    return p.stdout.strip().splitlines()[0]


def seat_values(book: dict) -> dict:
    """(map, seat) -> the set of what the book gives it: (name, value) a switch or datum."""
    out: dict = {}
    for e in book["entries"]:
        item = (e.get("set") or e.get("data"), json.dumps(e.get("value"), sort_keys=True))
        for t in SEATS[e["seat"]]:
            out.setdefault((e["map"], t), set()).add(item)
    return out


def book_at(rev: str) -> dict:
    try:
        return load_text(tune.git("show", f"{rev}:src/challenger/book.jsonl"))
    except tune.TuneError:
        return {"declare": {}, "entries": []}


def changed_seats(book: dict, ref_book: dict) -> set:
    """The map-seats where BOOK and the reference's book differ: the ones a confirm against that reference tests."""
    a, b = seat_values(book), seat_values(ref_book)
    return {k for k in set(a) | set(b) if a.get(k) != b.get(k)}


def cmd_confirm(a) -> int:
    rev = tune.git("rev-parse", "--short=12", f"{a.rev}^{{commit}}")
    book = load_text(tune.git("show", f"{rev}:src/challenger/book.jsonl"))
    ref = tune.pin_ref(a.ref)
    changed = changed_seats(book, book_at(ref.partition("@")[2]))
    maps = sorted({m for m, _ in changed}, key=map_id)
    if not maps:
        raise BookError(f"{rev}: its book equals the reference's")
    seats = {m for m, _ in map_seats(book)}
    outside = [m for m in pools.GOAL if m not in seats and m not in maps] + pools.TEAM_MADE
    runs = {
        "goal": start(f"challenger@{rev}", ref, "goal", a.cells, a.seed_base, []),
        "entries": start(f"challenger@{rev}", ref, "map:" + ",".join(maps), a.map_cells * len(maps), a.seed_base + 1,
                         []),
        "outside": start(f"challenger@{rev}", ref, "map:" + ",".join(outside), len(outside) * a.outside_passes,
                         a.seed_base + 2, []),
    }
    CONFIRM_DIR.mkdir(parents=True, exist_ok=True)
    out = CONFIRM_DIR / f"confirm-{rev}-{a.seed_base}.json"
    out.write_text(json.dumps({"rev": rev, "ref": ref, "seed_base": a.seed_base, "maps": maps,
                               "changed": sorted(changed), "runs": runs,
                               "started": time.time()}, indent=1))
    print(f"confirm {rev} against {ref}: {out}")
    for k, v in runs.items():
        print(f"  {k:8s} {v}")
    print(f"wait (in the background): tools/games wait {' '.join(runs.values())}; then tools/book/book confirm-show "
          f"{out}")
    return 0


def load_text(text: str) -> dict:
    with tempfile_path(text) as p:
        return load(p)


class tempfile_path:
    def __init__(self, text):
        import tempfile
        self.f = tempfile.NamedTemporaryFile("w", suffix=".jsonl", delete=False)
        self.f.write(text)
        self.f.close()

    def __enter__(self):
        return Path(self.f.name)

    def __exit__(self, *exc):
        Path(self.f.name).unlink()


def paired_run(run_id: str) -> dict:
    """(map, seat) -> {seed: d} of a paired run (ours role o, the ref role r)."""
    rows = bookfit.read_rows(tune.run_dir(run_id) / "results.jsonl")
    ref = [dict(r, role="o") for r in rows if r.get("role") == "r"]
    return bookfit.pair(ref, [r for r in rows if r.get("role") == "o"])


def summary(d: dict) -> tuple[float, float, float, int]:
    """(mean, lo, hi, games) of the paired game differences in D ((map, seat) -> {seed: d}), 95% interval with the
    standard error clustered by cell (map, seed): a cell's two seats share their seed."""
    cells: dict = {}
    for (mp, _), g in d.items():
        for sd, x in g.items():
            c = cells.setdefault((mp, sd), [0, 0])
            c[0] += x
            c[1] += 1
    n = sum(m for _, m in cells.values())
    if not n:
        return 0.0, 0.0, 0.0, 0
    mean = sum(t for t, _ in cells.values()) / n
    k = len(cells)
    var = sum((t - mean * m) ** 2 for t, m in cells.values()) * k / max(1, k - 1) / n ** 2
    h = 1.96 * var ** 0.5
    return mean, mean - h, mean + h, n


def split(d: dict, seats: set) -> tuple[dict, dict]:
    return {k: v for k, v in d.items() if k in seats}, {k: v for k, v in d.items() if k not in seats}


def line(name: str, s: tuple) -> str:
    m, lo, hi, n = s
    return f"{name}: {m:+.4f} [{lo:+.4f}, {hi:+.4f}] ({n} games)"


def cmd_confirm_show(a) -> int:
    """The book's merge criterion (docs/evaluation.md "The book", item 3) on a confirm's three runs."""
    conf = json.loads(Path(a.file).read_text())
    book = load_text(tune.git("show", f"{conf['rev']}:src/challenger/book.jsonl"))
    if "changed" in conf:
        seats = {tuple(k) for k in conf["changed"]}
    else:  # a confirm started before the reference had a book of its own
        seats = changed_seats(book, book_at(conf["ref"].partition("@")[2]))
    carried = set(map_seats(book)) - seats
    print(f"book {conf['rev']} against {conf['ref']}: {len(seats)} map-seats differ from the reference's book, "
          f"{len(carried)} carried over; seed bases {conf['seed_base']} (goal), +1 (entries), +2 (outside); game "
          f"differences book - main, 95% intervals clustered by cell")
    for k, rid in conf["runs"].items():
        print(f"  {k:8s} {rid} [{tune.run_status(rid)}]")
    e = paired_run(conf["runs"]["entries"])
    ent, rest = split(e, seats)
    a_all = summary(ent)
    print("\n" + line("the changed map-seats together", a_all))
    below = []
    for mp, t in sorted(seats, key=lambda p: (map_id(p[0]), p[1])):
        s = summary({(mp, t): e.get((mp, t), {})})
        below += [(mp, t)] if s[2] < 0 else []
        print("  " + line(f"{mp:24s} {t}", s))
    if rest:
        print("  " + line("other map-seats on those maps (carried over or no entry)", summary(rest)))
    o = summary(paired_run(conf["runs"]["outside"]))
    print(line("maps outside the book", o))
    g_in, g_out = split(paired_run(conf["runs"]["goal"]), seats)
    g = summary({**g_in, **g_out})
    print(line("goal", g) + f"  (tools/games show {conf['runs']['goal']}: its CI by map)")
    print("  " + line("goal, the book's map-seats", summary(g_in)))
    print("  " + line("goal, the rest", summary(g_out)))
    done = all(tune.run_status(r) in ("done", "stopped") for r in conf["runs"].values())
    va = a_all[1] > 0 and not below
    vb = o[2] >= 0
    vc = g[2] >= 0
    print(f"\n(a) changed map-seats together lower bound > 0 and none below 0: {'PASS' if va else 'FAIL'}"
          + (f" (below 0: {below})" if below else ""))
    print(f"(b) maps outside the book show no harm (upper bound >= 0): {'PASS' if vb else 'FAIL'}")
    print(f"(c) goal not below 0 (upper bound >= 0): {'PASS' if vc else 'FAIL'}")
    print(f"verdict: {'PASS' if va and vb and vc else 'FAIL'}" + ("" if done else " (runs still playing: provisional)")
          + "; then smoke and the gauntlet (docs/evaluation.md Protocol item 3)")
    return 0 if va and vb and vc else 3

# ------------------------------------------------------------------------------------------------- commands

def cmd_gen(a) -> int:
    java, tune_text = generate()
    BOOK_JAVA.write_text(java)
    TUNE_JAVA.write_text(tune_text)
    book = load()
    print(f"Book.java: {len(book['entries'])} entries on {len(map_seats(book))} map-seats; "
          f"settable Tune constants: {', '.join(sorted(booked(book))) or 'none'}")
    return 0


def cmd_check(a) -> int:
    book = load()
    errs = validate(book, tune.parse(render_tune(TUNE_JAVA.read_text(), booked(book))))
    names = all_maps()
    vs = views(names)
    tw = twins(vs)
    goal_twins = sorted({(m, n) for (m, t), qs in tw.items() if m in pools.GOAL for n, _ in qs})
    if not errs:
        errs += check_twins(book, vs)
    if not errs:
        java, tune_text = generate(book)
        if BOOK_JAVA.read_text() != java or TUNE_JAVA.read_text() != tune_text:
            errs.append("Book.java or Tune.java is not what book.jsonl generates: run tools/book/book gen")
    keys = {}
    for n in pools.GOAL:
        for t in "AB":
            keys.setdefault(json.dumps(vs[n][t], sort_keys=True), []).append((n, t))
    dup = [g for g in keys.values() if len(g) > 1]
    if dup:
        errs.append(f"goal map-seats that look the same at round 1: {dup}")
    print(f"maps/: {len(names)} maps, {len(names) * 2} map-seats, {len({json.dumps(v[t], sort_keys=True) for v in vs.values() for t in 'AB'})} "
          f"distinct round-1 views; goal: {len(pools.GOAL) * 2} map-seats, all distinct: {not dup}")
    vis = set(pools.visible())
    if goal_twins:
        print("goal maps whose start a visible map copies (an entry on them must name the twin): " + ", ".join(
            f"{m}={n if n in vis else '(outside the visible pools)'}" for m, n in goal_twins))
    print(f"book: {len(book['entries'])} entries on {len(map_seats(book))} map-seats")
    for e in errs:
        print("error: " + e)
    return 1 if errs else 0


def cmd_ids(a) -> int:
    vs = views(pools.GOAL)
    for n in pools.GOAL:
        print(f"{map_id(n):3d} {n:28s} A key {vs[n]['A']['key']:9d} id {vs[n]['A']['id']:3d}   "
              f"B key {vs[n]['B']['key']:9d} id {vs[n]['B']['id']:3d}")
    return 0


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(prog="tools/book/book", description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    sub.add_parser("gen", help="write Book.java and Tune.java's finality from book.jsonl").set_defaults(f=cmd_gen)
    sub.add_parser("check", help="ids unique over maps/, entries valid, generated code current").set_defaults(
        f=cmd_check)
    sub.add_parser("ids", help="the goal maps' ids and round-1 keys").set_defaults(f=cmd_ids)
    sp = sub.add_parser("matrix", help="candidate values x maps: a tools/tune campaign on the maps' map: pool, main on "
                                       "the same cells (builds and starts the runs: run it in the background)")
    sp.add_argument("name")
    sp.add_argument("--point", action="append", required=True, metavar="LABEL:NAME=V[,NAME=V]")
    sp.add_argument("--maps", required=True, help="comma-separated goal maps")
    sp.add_argument("--cells-per-map", type=int, default=60)
    sp.add_argument("--seed-base", type=int, required=True, help="a fresh seed base")
    sp.add_argument("--rev", help="the base (default HEAD)")
    sp.set_defaults(f=cmd_matrix)
    sp = sub.add_parser("fit", help="each candidate's effect per map-seat: raw, selection-adjusted, shrunk")
    sp.add_argument("name", nargs="?")
    sp.add_argument("--seed-base", type=int)
    sp.add_argument("--flips", type=int, default=2000)
    sp.add_argument("--json", action="store_true")
    sp.add_argument("--simulate", action="store_true", help="the method on simulated null and planted matrices")
    sp.add_argument("--mapseats", type=int, default=60)
    sp.add_argument("--n", type=int, default=30, help="simulated cells a map-seat")
    sp.add_argument("--q", type=float, default=0.35, help="simulated share of games a candidate redraws")
    sp.add_argument("--reps", type=int, default=10)
    sp.set_defaults(f=cmd_fit)
    sp = sub.add_parser("confirm", help="the book at REV against main: goal, the entries' maps, maps outside it")
    sp.add_argument("--rev", default="HEAD")
    sp.add_argument("--ref", default="challenger@origin/main")
    sp.add_argument("--seed-base", type=int, required=True, help="a fresh base; the entries' run uses +1, outside +2")
    sp.add_argument("--cells", type=int, default=3000)
    sp.add_argument("--map-cells", type=int, default=60, help="cells a book map in the entries' run")
    sp.add_argument("--outside-passes", type=int, default=4, help="passes over the maps outside the book")
    sp.set_defaults(f=cmd_confirm)
    sp = sub.add_parser("confirm-show", help="a confirm's results")
    sp.add_argument("file")
    sp.set_defaults(f=cmd_confirm_show)
    a = ap.parse_args(argv)
    try:
        return a.f(a)
    except (BookError, tune.TuneError, store.SetupError) as e:
        print(f"error: {e}", file=sys.stderr)
        return 1
