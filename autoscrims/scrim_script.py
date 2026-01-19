import random
import time
import requests
import os
from dotenv import load_dotenv

load_dotenv()

API_BASE = os.getenv("BC_API_BASE", "https://api.battlecode.org/api")
EPISODE = os.getenv("BC_EPISODE", "bc26java")
USERNAME = os.getenv("BC_USERNAME")
PASSWORD = os.getenv("BC_PASSWORD")
TEAM_PAGE_START = int(os.getenv("BC_TEAM_PAGE_START", "1"))
TEAM_PAGE_END = int(os.getenv("BC_TEAM_PAGE_END", "2"))
MAP_COUNT = int(os.getenv("BC_MAP_COUNT", "10"))
ONLY_US_TEAMS = os.getenv("BC_ONLY_US", "false").strip().lower() in ("1", "true", "yes")
REQUEST_SLEEP = float(os.getenv("BC_REQUEST_SLEEP", "0.1"))



def get_teams(start_page=1, end_page=1):
    page = 1
    teams = []
    for i in range(start_page, end_page+1):
        url = f"{API_BASE}/team/{EPISODE}/t/?ordering=-rating%2Cname&page={i}"
        response = requests.get(url)
        data = response.json()
        page += 1
        teams += data['results']
    #filter for US teams
    return teams

def get_us_teams(start_page=1, end_page=1):
    return [team for team in get_teams(start_page, end_page) if 3 in team['profile']['eligible_for'] and 2 not in team['profile']['eligible_for']]
    

def get_maps():
    url = f"{API_BASE}/episode/{EPISODE}/map/"
    response = requests.get(url)
    data = response.json()
    return data

def get_map_names():
    maps = get_maps()
    return [m['name'] for m in maps]

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

    teams = get_us_teams(TEAM_PAGE_START, TEAM_PAGE_END) if ONLY_US_TEAMS else get_teams(TEAM_PAGE_START, TEAM_PAGE_END)
    for team in teams:
        print(team['name'])
    all_map_names = get_map_names()
    map_count = min(MAP_COUNT, len(all_map_names))
    map_names = random.sample(all_map_names, map_count)
    srim_url = f"{API_BASE}/compete/{EPISODE}/request/"
    for team in teams:
        payload = {"is_ranked":False,"requested_to":team['id'],"player_order":"?","map_names":map_names}
        print(payload)
        response = requests.post(srim_url, json=payload, headers={"Authorization": f"Bearer {token}"})
        print(response.json())
        time.sleep(REQUEST_SLEEP)
