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
MAPS_PER_SCRIM = 10

# Fill these lists with the exact map names/team names you want to scrim.
MAPS1 = [
    "dirtfulcat", "arrows", "dirtpassageway", "Meow", "starvation", "thunderdome", "trapped", "DefaultMedium", "DefaultSmall", "evileye"
]
MAPS2 = [
    "cheesefarm", "keepout", "pipes", "rift", "wallsofparadis", "ZeroDay", "sittingducks", "cheeseguardians", "DefaultLarge", "Nofreecheese"
]
SCRIM_TEAMS = [
    "Old But Gold",
    "Sloppy Joe Pirates",
    "The Complex Merlin",
    "Bruteforcer"
]

# "?" = alternate
# "+" = requester first
# "-" = requester last
ORDER = "+"

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
        raise SystemExit(f"{list_name} is empty. Add up to {MAPS_PER_SCRIM} map names.")
    if len(maps) > MAPS_PER_SCRIM:
        raise SystemExit(f"{list_name} has {len(maps)} maps; max is {MAPS_PER_SCRIM}.")

if __name__ == "__main__":
    if not USERNAME or not PASSWORD:
        raise SystemExit("Set BC_USERNAME and BC_PASSWORD in your environment or .env file.")

    url = f"{API_BASE}/token/"
    payload = {
        "username": USERNAME,
        "password": PASSWORD,
    }
    response = requests.post(url, data=payload)
    token = response.json()['access']

    maps1 = _clean_list(MAPS1)
    maps2 = _clean_list(MAPS2)
    scrim_names = _clean_list(SCRIM_TEAMS)
    if len(scrim_names) == 0:
        raise SystemExit("SCRIM_TEAMS is empty. Add team names to scrim.")
    _validate_map_list("MAPS1", maps1)
    _validate_map_list("MAPS2", maps2)

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
        for map_group in [maps1, maps2]:
            payload = {"is_ranked":False,"requested_to":team['id'],"player_order":ORDER,"map_names":map_group}
            print(payload)
            response = requests.post(srim_url, json=payload, headers={"Authorization": f"Bearer {token}"})
            print(response.json())
            time.sleep(REQUEST_SLEEP)
