import argparse
import time
import requests
import os
from dotenv import load_dotenv

load_dotenv()

API_BASE = os.getenv("BC_API_BASE", "https://api.battlecode.org/api")
EPISODE = os.getenv("BC_EPISODE", "bc26")
USERNAME = os.getenv("BC_USERNAME")
PASSWORD = os.getenv("BC_PASSWORD")
TEAM_PAGE_START = int(os.getenv("BC_TEAM_PAGE_START", "1"))
TEAM_PAGE_END = int(os.getenv("BC_TEAM_PAGE_END", "2"))
ONLY_US_TEAMS = os.getenv("BC_ONLY_US", "false").strip().lower() in ("1", "true", "yes")
REQUEST_SLEEP = float(os.getenv("BC_REQUEST_SLEEP", "0.1"))
SCRIM_MAPS_PER_REQUEST = int(os.getenv("BC_SCRIM_MAPS_PER_REQUEST", "10"))

# Fill these lists with the exact map names/team names you want to scrim.
# Map groups can be any length; they will be split into batches of SCRIM_MAPS_PER_REQUEST.
MAPS3 = [
    "DefaultLarge",
    "DefaultMedium",
    "DefaultSmall",
    "evileye",
    "peaceinourtime",
    "cheesefarm",
    "rift",
    "ZeroDay",
    "pipes",
    "Nofreecheese",
    "wallsofparadis",
    "dirtpassageway",
    "trapped",
    "Meow",
    "arrows",
    "starvation",
    "keepout",
    "dirtfulcat",
    "popthecork",
    "sittingducks",
    "thunderdome",
    "5t4rv4t10n_1337",
    "hatefullattice",
    "mercifullattice",
    "cheesebottles",
    "knifefight",
    "safelycontained",
    "TheHeist",
    "EscapeTheNight",
    "closeup",
    "averyfineline",
    "minimaze",
    "jail",
    "tiny",
    "streetsofnewyork",
    "corridorofdoomanddespair",
    "toomuchcheese",
    "canyoudig",
    "averystrangespace",
    "whatsthecatdoin",
    "whereisthecheese",
    "uneruesansfin",
]

MAPS1 = [
    "dirtfulcat", "arrows", "dirtpassageway", "Meow", "starvation", "thunderdome", "trapped", "averyfineline", "whereisthecheese", "tiny"
]
MAPS2 = [
    # "cheesefarm", "keepout", "pipes", "rift", "wallsofparadis", "ZeroDay", "sittingducks", "popthecork", "DefaultLarge", "Nofreecheese"
    "cheesefarm", "keepout", "pipes", "rift", "wallsofparadis", "ZeroDay", "sittingducks", "Nofreecheese", "jail", "hatefullattice"
]

# hack
for x in MAPS1:
    MAPS3.remove(x)
for x in MAPS2:
    MAPS3.remove(x)
# MAPS1 = MAPS3


SCRIM_TEAMS = [
    "Old But Gold",
    "muscallonge",
    "Mango Tango",
    "Goobie Woobies",
    "The Complex Merlin"
]

# "?" = alternate
# "+" = requester first
# "-" = requester last
ORDER = "+"

MAP_GROUPS = {
    "micro": MAPS1,
    "macro": MAPS2,
}

def get_teams(start_page=1, end_page=1):
    page = 1
    teams = []
    for i in range(start_page, end_page+1):
        url = f"{API_BASE}/team/{EPISODE}/t/?ordering=-rating%2Cname&page={i}"
        response = requests.get(url)
        data = response.json()
        page += 1
        teams += data['results']
    return teams

def get_us_teams(start_page=1, end_page=1):
    return [team for team in get_teams(start_page, end_page) if 3 in team['profile']['eligible_for'] and 2 not in team['profile']['eligible_for']]
    

def _normalize_name(name):
    return name.strip().lower()

def _clean_list(items):
    return [item for item in items if item and item.strip()]

def _validate_map_list(list_name, maps):
    if len(maps) == 0:
        raise SystemExit(f"{list_name} is empty. Add at least one map name.")
    if SCRIM_MAPS_PER_REQUEST < 1:
        raise SystemExit("SCRIM_MAPS_PER_REQUEST must be at least 1.")

def _chunk_list(items, size):
    for i in range(0, len(items), size):
        yield items[i:i + size]

def _parse_csv(value):
    return [item.strip() for item in value.split(",") if item.strip()]

def _select_map_groups(maps_arg):
    requested = _parse_csv(maps_arg)
    if not requested:
        raise SystemExit("--maps is empty. Use micro, macro, or both.")
    groups = []
    invalid = []
    for name in requested:
        key = name.lower()
        if key in MAP_GROUPS:
            groups.append((key, _clean_list(MAP_GROUPS[key])))
        else:
            invalid.append(name)
    if invalid:
        raise SystemExit(f"Unknown --maps value(s): {', '.join(invalid)}. Use micro and/or macro.")
    return groups

def _select_orders(sides_arg):
    requested = _parse_csv(sides_arg)
    if not requested:
        raise SystemExit("--sides is empty. Use +, -, or ?.")
    invalid = [side for side in requested if side not in ("+", "-", "?")]
    if invalid:
        raise SystemExit(f"Unknown --sides value(s): {', '.join(invalid)}. Use +, -, or ?.")
    return requested

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--maps",
        default="micro,macro",
        help="Which map groups to use: micro, macro, or comma-separated list (default: micro,macro).",
    )
    parser.add_argument(
        "--sides",
        default="+",
        help='Player order: "+", "-", "?" or comma-separated list (default: +).',
    )
    args = parser.parse_args()

    if not USERNAME or not PASSWORD:
        raise SystemExit("Set BC_USERNAME and BC_PASSWORD in your environment or .env file.")

    url = f"{API_BASE}/token/"
    payload = {
        "username": USERNAME,
        "password": PASSWORD,
    }
    response = requests.post(url, data=payload)
    token = response.json()['access']

    map_groups = _select_map_groups(args.maps)
    scrim_names = _clean_list(SCRIM_TEAMS)
    if len(scrim_names) == 0:
        raise SystemExit("SCRIM_TEAMS is empty. Add team names to scrim.")
    for group_name, maps in map_groups:
        _validate_map_list(group_name, maps)

    orders = _select_orders(args.sides)

    teams = get_us_teams(TEAM_PAGE_START, TEAM_PAGE_END) if ONLY_US_TEAMS else get_teams(TEAM_PAGE_START, TEAM_PAGE_END)
    teams_by_name = {_normalize_name(team['name']): team for team in teams}

    selected_teams = []
    missing_names = []
    for name in scrim_names:
        key = _normalize_name(name)
        team = teams_by_name.get(key)
        if team:
            selected_teams.append(team)
        else:
            missing_names.append(name)

    if len(selected_teams) == 0:
        raise SystemExit("No matching teams found on the specified pages.")

    print("Scrimming teams:")
    for team in selected_teams:
        print(team['name'])
    if missing_names:
        print("Missing teams (not found on specified pages):")
        for name in missing_names:
            print(name)

    srim_url = f"{API_BASE}/compete/{EPISODE}/request/"
    for team in selected_teams:
        for group_name, map_group in map_groups:
            for order in orders:
                for map_chunk in _chunk_list(map_group, SCRIM_MAPS_PER_REQUEST):
                    payload = {
                        "is_ranked": False,
                        "requested_to": team['id'],
                        "player_order": order,
                        "map_names": map_chunk,
                    }
                    print(payload)
                    response = requests.post(srim_url, json=payload, headers={"Authorization": f"Bearer {token}"})
                    print(response.json())
                    time.sleep(REQUEST_SLEEP)
