import subprocess
import re
import concurrent.futures
import random
import time
import os

try:
    import resource
except ImportError:
    resource = None

try:
    import psutil
except ImportError:
    psutil = None

DIRT_TESTS = "cheesefarm,rift,ZeroDay,pipes,Nofreecheese,sittingducks"
DEFAULT = ["DefaultSmall", "DefaultMedium", "DefaultLarge", "evileye", "cheeseguardians"]
SPRINT_1 = ["cheesefarm", "rift", "ZeroDay", "pipes", "Nofreecheese", "wallsofparadis", "dirtpassageway", "trapped", "Meow", "arrows", "starvation", "keepout", "dirtfulcat", "popthecork", "sittingducks", "thunderdome"]
DEBUG_MAPS = ["pvparena"]
teamA = "awubot"
teamB = "awubot_defensivespawning"
# maps = "8cats_big,bigface,breakthewall,Cage,cheesemining_big,cheeseorcat,dangerous,debug_cat_right,diagonalmap,dirt_around_mine,dirtroad"
# maps = "isolated_cat,isolated_cheese,killcat,killcatormine,maybedonothing,melbournetrain,minewithcat,mixedmaze,nowall_bigbattle_big,nowallnocat"
# maps = "oneexit_small,realiseuarecooked,roundabout,rush_big,rush_small,segtree,sixseven,small1v1,small_dirtmaze,tunnelbattle_big,tunnelbattle_big_dirtless,veryverysmall"
# maps = "CheeseMines,CheeseMinesMedium,colony,FootballField,Ruins,Snails,Towers"
# maps = "DefaultSmall,DefaultMedium,DefaultLarge,evileye,cheeseguardians"
maps = DEFAULT + SPRINT_1
# maps = DEBUG_MAPS
# maps = ["Cage,colony,FootballField,Islands,PickYourSpawn,Ruins,Sawtooth,Snails,TestTube,Towers,WheresYourShovel"]
# maps = ["CombatTest2"]
seeds = [""] * 2

debug = len(maps) == 1 and len(seeds) == 1
GRADLEW = "gradlew.bat" if os.name == "nt" else "./gradlew"


def _child_cpu_total():
    if resource is None:
        return None
    usage = resource.getrusage(resource.RUSAGE_CHILDREN)
    return usage.ru_utime + usage.ru_stime


def run_game(params):
    teamA, teamB, map, seed = params
    wall_start = time.perf_counter()
    cpu_start = time.process_time()
    child_cpu_before = _child_cpu_total()
    print(GRADLEW)
    print(f"Run {teamA} vs {teamB} on {map} {seed}")
    use_psutil = psutil is not None and resource is None
    cmd = [
        GRADLEW,
        'runWithoutBuild',
        f'-PteamA={teamA}',
        f'-PteamB={teamB}',
        f"-Pmaps={map}",
        f"-Pseed={seed}"
    ]
    gradle_cpu = None
    if use_psutil:
        proc = psutil.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        stdout, stderr = proc.communicate()
        returncode = proc.returncode
        try:
            cpu_times = proc.cpu_times()
            gradle_cpu = cpu_times.user + cpu_times.system
        except Exception:
            gradle_cpu = None
    else:
        completed = subprocess.run(cmd, capture_output=True, text=True)
        stdout = completed.stdout
        stderr = completed.stderr
        returncode = completed.returncode
    result = stdout
    print(result)
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
    wall_elapsed = time.perf_counter() - wall_start
    cpu_elapsed = time.process_time() - cpu_start
    child_cpu_after = _child_cpu_total()
    child_cpu = None
    child_cpu_label = None
    if child_cpu_before is not None and child_cpu_after is not None:
        child_cpu = max(0.0, child_cpu_after - child_cpu_before)
        child_cpu_label = "children cpu"
    elif gradle_cpu is not None:
        child_cpu = gradle_cpu
        child_cpu_label = "gradle cpu"
    timing = (
        f"Timing {teamA} vs {teamB} on {map}: "
        f"wall {wall_elapsed:.3f}s, worker cpu {cpu_elapsed:.3f}s, rc {returncode}"
    )
    if child_cpu is not None:
        cpu_pct = (child_cpu / wall_elapsed * 100.0) if wall_elapsed else 0.0
        timing += f", {child_cpu_label} {child_cpu:.3f}s ({cpu_pct:.1f}%)"
    else:
        timing += ", child cpu n/a"
    print(timing)
    return winner


if __name__ == "__main__":
    # Build first
    subprocess.run([GRADLEW, "build"])

    score = {teamA: 0, teamB: 0}
    debug = len(maps) == 1 and len(seeds) == 1

    # Change this number so it doesn't kill your computer
    NUM_WORKERS = 8
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
