"""tools/book/book: the per-map book of challenger (tools/book/AGENTS.md).

The book (src/challenger/book.jsonl) holds entries (map, seat, constant of Tune.java, value) and per-map data. `gen`
writes src/challenger/Book.java from it, and drops `final` from the Tune.java constants an entry sets, so that only
those become settable per robot at its first turn. A robot knows its map from what its first king sees at round 1:
the map's size, the king's team, location and ID, and every wall, dirt and mine tile of its r^2 25 view.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))
from bcgames import pools, store  # noqa: E402
from bctune import tune  # noqa: E402

ROOT = TOOLS.parent
BOOK = ROOT / "src/challenger/book.jsonl"
BOOK_JAVA = ROOT / "src/challenger/Book.java"
TUNE_JAVA = ROOT / "src/challenger/Tune.java"
VIEW_CACHE = store.DATA / "bookview"
SEATS = {"A": ["A"], "B": ["B"], "both": ["A", "B"]}
DATA_TYPES = {"tiles": "MapLocation[]", "ints": "int[]"}
VIEW_R2 = 25  # the king's vision, 360 degrees from its centre (docs/rules.md §7)
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
    masks = {"#": 0, "d": 0, "M": 0}
    for dx in range(-5, 6):
        for dy in range(-5, 6):
            if dx * dx + dy * dy > VIEW_R2 or not (0 <= x + dx < w and 0 <= y + dy < h):
                continue
            c = m["grid"][y + dy][x + dx]
            if c in masks:
                masks[c] |= 1 << ((dx + 5) * 11 + dy + 5)
    split = lambda v: [v & (2 ** 64 - 1), v >> 64]  # noqa: E731
    return {"key": (1 if team == "B" else 0) << 24 | w << 18 | h << 12 | x << 6 | y, "id": k["id"],
            "walls": split(masks["#"]), "dirt": split(masks["d"]), "mines": split(masks["M"])}


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
            elif d["type"] == "tiles" and not (isinstance(v, list) and all(
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
         "// identifies the map at round 1 from its size, the king's team, location and ID and every wall, dirt and",
         "// mine of its r² 25 view, and writes the map's id to slot 15; every other robot reads it at its first turn.",
         "// Only the map-seats the book has entries for are identified: elsewhere map is 0 and nothing runs but the",
         "// slot read. init runs right after Game.init, before any class that reads Tune is loaded.",
         "public final class Book {",
         "    public static int map; // the map's id (tools/book/book ids) on a map-seat with entries, else 0",
         "    public static int seat; // 0 team A, 1 team B (set where map != 0)"]
    for name, d in sorted(book["declare"].items()):
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
            L.append(f"                if (id == {v['id']} && w0 == {jlong(v['walls'][0])} && w1 == {jlong(v['walls'][1])}"
                     f" && d0 == {jlong(v['dirt'][0])} && d1 == {jlong(v['dirt'][1])}")
            L.append(f"                        && m0 == {jlong(v['mines'][0])} && m1 == {jlong(v['mines'][1])})"
                     f" return {map_id(m)};")
        L += ["                return 0;", "            }"]
    L += ["            default -> {", "                return 0;", "            }", "        }", "    }", "",
          "    private static int id;",
          "    private static long w0, w1, d0, d1, m0, m1;", "",
          "    // Bit (dx + 5) * 11 + (dy + 5) of each 128-bit mask (w0 low, w1 high; a long shift takes its count mod 64).",
          "    private static void scan() throws GameActionException {",
          "        id = Game.rc.getID();",
          "        MapInfo[] infos = Game.rc.senseNearbyMapInfos();",
          "        int base = (Game.myLocX - 5) * 11 + Game.myLocY - 5;",
          "        for (int k = infos.length; --k >= 0; ) {",
          "            MapInfo t = infos[k];",
          "            MapLocation l = t.getMapLocation();",
          "            int i = l.x * 11 + l.y - base;",
          "            if (t.isWall()) {",
          "                if (i < 64) w0 |= 1L << i; else w1 |= 1L << i;",
          "            } else if (t.isDirt()) {",
          "                if (i < 64) d0 |= 1L << i; else d1 |= 1L << i;",
          "            } else if (t.hasCheeseMine()) {",
          "                if (i < 64) m0 |= 1L << i; else m1 |= 1L << i;",
          "            }",
          "        }",
          "    }", "",
          "    private static void apply() {",
          "        seat = Game.team == Team.A ? 0 : 1;",
          "        switch (map << 1 | seat) {"]
    for m, t in seats:
        L.append(f"            case {map_id(m) << 1 | (t == 'B')} -> {{ // {m} seat {t}")
        for e in book["entries"]:
            if e["map"] == m and t in SEATS[e["seat"]]:
                if "set" in e:
                    L.append(f"                Tune.{e['set']} = {java_value(e['value'], params[e['set']]['type'])};")
                else:
                    kind = book["declare"][e["data"]]["type"]
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
    a = ap.parse_args(argv)
    try:
        return a.f(a)
    except (BookError, tune.TuneError, store.SetupError) as e:
        print(f"error: {e}", file=sys.stderr)
        return 1
