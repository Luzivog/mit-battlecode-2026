import subprocess
import re
import concurrent.futures
import random
import time

DIRT_TESTS = "cheesefarm,rift,ZeroDay,pipes,Nofreecheese,sittingducks"
DEFAULT = ["DefaultSmall", "DefaultMedium", "DefaultLarge", "evileye", "cheeseguardians"]
SPRINT_1 = ["cheesefarm", "rift", "ZeroDay", "pipes", "Nofreecheese", "wallsofparadis", "dirtpassageway", "trapped", "Meow", "arrows", "starvation", "keepout", "dirtfulcat", "popthecork", "sittingducks", "thunderdome"]
teamA = "lactosetolerantalexa"
teamB = "copy"
# maps = "8cats_big,bigface,breakthewall,Cage,cheesemining_big,cheeseorcat,dangerous,debug_cat_right,diagonalmap,dirt_around_mine,dirtroad"
# maps = "isolated_cat,isolated_cheese,killcat,killcatormine,maybedonothing,melbournetrain,minewithcat,mixedmaze,nowall_bigbattle_big,nowallnocat"
# maps = "oneexit_small,realiseuarecooked,roundabout,rush_big,rush_small,segtree,sixseven,small1v1,small_dirtmaze,tunnelbattle_big,tunnelbattle_big_dirtless,veryverysmall"
# maps = "CheeseMines,CheeseMinesMedium,colony,FootballField,Ruins,Snails,Towers"
# maps = "DefaultSmall,DefaultMedium,DefaultLarge,evileye,cheeseguardians"
maps = DEFAULT + SPRINT_1
# maps = ["Cage,colony,FootballField,Islands,PickYourSpawn,Ruins,Sawtooth,Snails,TestTube,Towers,WheresYourShovel"]
# maps = ["CombatTest2"]
seeds = [""] * 5

debug = len(maps) == 1 and len(seeds) == 1


def run_game(params):
    teamA, teamB, map, seed = params
    print(f"Run {teamA} vs {teamB} on {map} {seed}")
    result = subprocess.run(
        [
            './gradlew',
            'runWithoutBuild',
            f'-PteamA={teamA}',
            f'-PteamB={teamB}',
            f"-Pmaps={map}",
            f"-Pseed={seed}"
        ],
        capture_output=True,
        text=True
    ).stdout
    if "resign" in result:
        print(f"\033[0;31mResignation {teamA} vs {teamB} on {map} {seed}\033[0m")
    winner = None
    if "(A) wins" in result:
        winner = teamA
    elif "(B) wins" in result:
        winner = teamB
    if winner:
        print(f"{winner} wins on {map}")
    else:
        print(f"Tie on {map}")
    return winner


if __name__ == "__main__":
    # Build first
    subprocess.run(["./gradlew", "build"])

    score = {teamA: 0, teamB: 0}
    debug = len(maps) == 1 and len(seeds) == 1

    # Change this number so it doesn't kill your computer
    NUM_WORKERS = 1
    params = []
    for seed in seeds:
        # Assign seeds
        if not seed:
            seed = random.randint(0, 10 ** 9)
        params += [(teamA, teamB, map, seed) for map in maps]
        params += [(teamB, teamA, map, seed) for map in maps]

    start = time.time()
    with concurrent.futures.ProcessPoolExecutor(max_workers=NUM_WORKERS) as executor:
        results = list(executor.map(run_game, params))
    end = time.time()

    print(f"Ran {len(results)} matches in {end - start}s")

    for winner in results:
        if winner:
            score[winner] += 1
    print(f"SCORE {score[teamA]}-{score[teamB]}")