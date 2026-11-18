"""Map pools. Every map is a file in maps/. Pools combine with commas: --pool goal,stress.

A pool is a list of entries: "map" (pass 0 plays the map's own seed when the seed base is 0) or "map:seed" (pass 0
plays that cell). Later passes play derived seeds.
"""
from __future__ import annotations

import json
from pathlib import Path

from . import store

# The 43 public maps (default, sprint 1, sprint 2), identical to the engine jar's and the official repo's.
OFFICIAL = [
    "DefaultSmall", "DefaultMedium", "DefaultLarge", "evileye", "cheeseguardians",
    "arrows", "cheesefarm", "dirtfulcat", "dirtpassageway", "keepout", "Meow", "Nofreecheese", "pipes", "popthecork",
    "rift", "sittingducks", "starvation", "thunderdome", "trapped", "wallsofparadis", "ZeroDay",
    "5t4rv4t10n_1337", "averyfineline", "averystrangespace", "canyoudig", "cheesebottles", "closeup",
    "corridorofdoomanddespair", "EscapeTheNight", "hatefullattice", "jail", "knifefight", "mercifullattice", "minimaze",
    "peaceinourtime", "safelycontained", "streetsofnewyork", "TheHeist", "tiny", "toomuchcheese", "uneruesansfin",
    "whatsthecatdoin", "whereisthecheese",
]
# Team-made maps (the champion's), without the degenerate ones (in STRESS).
TEAM_MADE = [
    "DefaultSmallCatless", "DefaultMediumCatless", "DefaultLargeCatless", "arrowsCatless", "bridges",
    "bridgesCatless", "checkerboard", "cups", "far", "hidden", "maze1", "maze2", "maze3", "messy", "mining",
    "quadrants", "micro1", "micro2", "microtest", "microtest_wide", "pvparena", "pvparena_mines",
    "pvparena_mines2", "testpathfinding", "testpathfinding2",
]
# The visible two thirds of the 78 tournament maps recovered from replays (the other third is the sealed holdout).
TOURNAMENT = [
    "AceOfSpades", "Excavation", "FreeTheCheese", "Hike", "QueenOfHearts", "RUN", "UncomfortablyClose",
    "asabovesobelow", "attack_on_cat", "bed", "cheddarchokehold", "cheeseysquare", "constrictinglattice",
    "dangerouscrossing", "diamond", "directconfrontation", "externalsustenance", "foolsgold", "fullofsecrets",
    "gelcatinouscube", "gridlock", "groveisbeingdesecrated", "hellofromtheotherside", "holeinthewall", "houseofcards",
    "insertmeowname", "isnotgold", "kingofthehill", "learn_your_lesson", "longwayaround", "mincemeat",
    "moonlitriverpark", "mushroom", "neitheroptionisgood", "pocketsofcheese", "rose", "seepastyourgreed", "shell",
    "sieveofriches", "squaresaremodernart", "stareintotheabyss", "stuckinthemiddle", "sunandmoon", "superioristhmus",
    "t_minus_five_minutes", "thehallway", "therainbow", "therearenowalls", "tminusten", "warhammer", "where_are_you",
    "worldshardestgame",
]
# The goal's maps (docs/program.md: official + tournament), visible split. The 90% metric is measured here.
GOAL = OFFICIAL + TOURNAMENT
# DS§1.1's POINTS class (the 1.2.5 mirror's points and cooperation endings) and T3§6's points-prone tournament maps.
POINTS = ["TheHeist", "peaceinourtime", "ZeroDay", "averystrangespace", "cheesebottles", "minimaze", "pipes", "rift",
          "sittingducks", "uneruesansfin", "wallsofparadis",
          "groveisbeingdesecrated", "t_minus_five_minutes", "fullofsecrets", "rose", "clockwisdom"]
POOLS = {
    # 8 maps, 1 seed: crashes (smallest map, most mines, most cats, heaviest dirt, forced cooperation, maze,
    # degenerate). Read the crashed-robots line.
    "smoke": ["tiny", "DefaultSmall", "quadrants", "sunandmoon", "superioristhmus", "t_minus_five_minutes",
              "minimaze", "coop"],
    # Inner loop: 16 maps, every size and symmetry, 4 of them champion-weak (keepout, whatsthecatdoin, jail, tminusten).
    "quick": ["DefaultSmall", "DefaultMedium", "DefaultLarge", "evileye", "arrows", "whereisthecheese", "cheesefarm",
              "jail", "TheHeist", "pipes", "sittingducks", "knifefight", "tiny", "keepout", "whatsthecatdoin",
              "tminusten"],
    "official": OFFICIAL,
    "tournament": TOURNAMENT,
    "goal": GOAL,                      # acceptance: official + visible tournament
    "tune": GOAL,                      # the plan's name for the same pool
    "teammade": TEAM_MADE,             # robustness only, not part of the goal
    "full": GOAL + TEAM_MADE,
    # Where the champion lost (docs/intel.md §2, §3.1).
    "weak": ["whatsthecatdoin", "corridorofdoomanddespair", "safelycontained", "jail", "keepout", "dirtfulcat",
             "TheHeist", "cheesebottles", "tminusten", "sieveofriches", "pocketsofcheese", "learn_your_lesson",
             "attack_on_cat", "mincemeat", "t_minus_five_minutes", "groveisbeingdesecrated", "asabovesobelow",
             "kingofthehill", "moonlitriverpark", "insertmeowname", "hellofromtheotherside"],
    # >= 26 mines (the champion kills its own kings once it knows 26), most cats, heaviest dirt, degenerate geometry.
    "stress": ["quadrants", "sieveofriches", "tminusten", "bridges", "bridgesCatless", "sunandmoon", "cheeseguardians",
               "worldshardestgame", "FreeTheCheese", "superioristhmus", "mincemeat", "troll1", "troll2", "coop",
               "pathfinding test", "5t4rv4t10n_1337"],
}
HOLDOUT_FILE = store.DATA / "SEALED-TEST-DO-NOT-READ" / "holdout.json"
# Named list pools (e.g. W0.3's loss classes): a file NAME.txt here, one "map" or "map:seed" a line, # comments.
LIST_DIR = store.DATA / "pools"
# Degenerate team-made maps, only in STRESS (and in the class pools they fall in).
DEGENERATE = ["troll1", "troll2", "coop", "pathfinding test"]
# Map-class pools (docs/theory/t4-maps.md §3), computed by mapclass.py over every visible map, so a new map in
# maps/ is classed the same way. Judge a change that targets a class on its pool first, then on `full`.
CLASS_POOLS = {"cat": "CATPATH", "starve": "MINEPOOR", "choke": "CHOKE", "faceoff": "FACEOFF",
               "nocontact": "NOCONTACT", "dirt": "DIRT", "overflow": None, "open": "OPEN"}
OVERFLOW_MINES = 26  # awubot destroys its kings once it knows 26 mines (docs/theory/t4-maps.md §3)


def holdout() -> list[str]:
    return json.loads(HOLDOUT_FILE.read_text())["maps"]


def visible() -> list[str]:
    return OFFICIAL + TEAM_MADE + TOURNAMENT + DEGENERATE


def class_pool(name: str) -> list[str]:
    from . import mapclass
    cls = mapclass.classify(visible())
    if name == "overflow":
        return [m for m in visible() if cls[m]["mines"] >= OVERFLOW_MINES]
    return [m for m in visible() if CLASS_POOLS[name] in cls[m]["classes"]]


def entry_map(entry: str) -> str:
    return entry.rsplit(":", 1)[0] if ":" in entry else entry


def entry_seed(entry: str) -> int | None:
    return int(entry.rsplit(":", 1)[1]) if ":" in entry else None


def list_pool(path: Path) -> list[str]:
    """A list pool: "map" or "map:seed" a line. Only visible maps: a sealed map is an error."""
    entries = [l.split("#", 1)[0].strip() for l in path.read_text().splitlines()]
    entries = [e for e in entries if e]
    seen = set(visible())
    outside = [e for e in entries if entry_map(e) not in seen]
    if outside:
        raise store.SetupError(f"pool {path}: {len(outside)} entries are not visible maps (first: "
                               f"{entry_map(outside[0])!r} is unknown or sealed); a visible pool takes visible maps only")
    for e in entries:
        entry_seed(e)  # a bad seed is an error here
    return entries


def resolve(spec: str) -> tuple[list[str], bool]:
    """'quick', 'goal,stress', 'map:a,b:7', 'file:PATH' or a list pool's name -> (entries, sealed).

    Sealed pools never print map names."""
    maps: list[str] = []
    sealed = False
    if spec.startswith("map:"):
        maps = [m for m in spec[4:].split(",") if m]
    elif spec.startswith("file:"):
        maps = list_pool(Path(spec[5:]).expanduser())
    else:
        for name in spec.split(","):
            if name == "holdout":
                maps += holdout()
                sealed = True
            elif name in POOLS:
                maps += POOLS[name]
            elif name == "points":
                vis = set(visible())
                maps += [m for m in POINTS if m in vis]
            elif name in CLASS_POOLS:
                maps += class_pool(name)
            elif (LIST_DIR / f"{name}.txt").exists():
                maps += list_pool(LIST_DIR / f"{name}.txt")
            else:
                raise store.SetupError(f"unknown pool {name!r} (pools: {', '.join(list(POOLS) + list(CLASS_POOLS))}, "
                                       f"holdout; "
                                       f"or map:NAME1,NAME2)")
    seen = set()
    maps = [m for m in maps if not (m in seen or seen.add(m))]
    for m in maps:
        store.map_file(entry_map(m))
        entry_seed(m)
    return maps, sealed
