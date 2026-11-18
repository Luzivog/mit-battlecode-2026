"""Map pools. Every map is a file in maps/. Pools combine with commas: --pool tune,tournament."""
from __future__ import annotations

import json

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
    "tune": OFFICIAL + TEAM_MADE,
    "tournament": TOURNAMENT,
    "full": OFFICIAL + TEAM_MADE + TOURNAMENT,
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


def holdout() -> list[str]:
    return json.loads(HOLDOUT_FILE.read_text())["maps"]


def resolve(spec: str) -> tuple[list[str], bool]:
    """'quick' or 'tune,tournament' or 'map:a,b' -> (maps, sealed). Sealed pools never print map names."""
    maps: list[str] = []
    sealed = False
    if spec.startswith("map:"):
        maps = [m for m in spec[4:].split(",") if m]
    else:
        for name in spec.split(","):
            if name == "holdout":
                maps += holdout()
                sealed = True
            elif name in POOLS:
                maps += POOLS[name]
            else:
                raise store.SetupError(f"unknown pool {name!r} (pools: {', '.join(list(POOLS) + ['holdout'])}; "
                                       f"or map:NAME1,NAME2)")
    seen = set()
    maps = [m for m in maps if not (m in seen or seen.add(m))]
    for m in maps:
        store.map_file(m)
    return maps, sealed
