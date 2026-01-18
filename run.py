import subprocess
import re
import concurrent.futures
import random
import time
import os
import threading

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
seeds = [""]

debug = len(maps) == 1 and len(seeds) == 1
GRADLEW = "gradlew.bat" if os.name == "nt" else "./gradlew"
SAMPLE_INTERVAL = 0.25


def _child_cpu_total():
    if resource is None:
        return None
    usage = resource.getrusage(resource.RUSAGE_CHILDREN)
    return usage.ru_utime + usage.ru_stime


def _drain_stream(stream, sink):
    for line in iter(stream.readline, ""):
        sink.append(line)
    stream.close()


def _process_tree_cpu_total(pid):
    if psutil is None:
        return None, 0
    try:
        root = psutil.Process(pid)
        procs = [root] + root.children(recursive=True)
    except psutil.Error:
        return None, 0
    total = 0.0
    count = 0
    for proc in procs:
        try:
            times = proc.cpu_times()
            total += times.user + times.system
            count += 1
        except psutil.Error:
            continue
    return total, count


def _gradle_daemon_cpu_total():
    if psutil is None:
        return None, 0
    total = 0.0
    count = 0
    for proc in psutil.process_iter(["cmdline"]):
        try:
            cmdline = proc.info.get("cmdline") or []
        except (psutil.Error, OSError):
            continue
        if not cmdline:
            continue
        if not any("GradleDaemon" in arg for arg in cmdline):
            continue
        try:
            times = proc.cpu_times()
            total += times.user + times.system
            count += 1
        except (psutil.Error, OSError):
            continue
    return total, count


def _run_with_psutil(cmd):
    proc = psutil.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    stdout_lines = []
    stderr_lines = []
    out_thread = threading.Thread(target=_drain_stream, args=(proc.stdout, stdout_lines))
    err_thread = threading.Thread(target=_drain_stream, args=(proc.stderr, stderr_lines))
    out_thread.start()
    err_thread.start()

    cpu_total = None
    cpu_max = 0.0
    children_max = 0
    active_samples = 0
    samples = 0
    cpu_seen = False
    daemon_cpu_accum = 0.0
    daemon_last_total = None
    daemon_active_samples = 0
    daemon_samples = 0
    daemon_max_procs = 0

    while proc.poll() is None:
        total, count = _process_tree_cpu_total(proc.pid)
        if total is not None:
            if cpu_total is not None and total > cpu_total:
                active_samples += 1
            cpu_total = total
            cpu_max = max(cpu_max, total)
            children_max = max(children_max, count)
            cpu_seen = True
        daemon_total, daemon_count = _gradle_daemon_cpu_total()
        if daemon_total is not None:
            if daemon_last_total is not None:
                delta = daemon_total - daemon_last_total
                if delta > 0:
                    daemon_cpu_accum += delta
                    daemon_active_samples += 1
            daemon_last_total = daemon_total
            daemon_samples += 1
            daemon_max_procs = max(daemon_max_procs, daemon_count)
        samples += 1
        time.sleep(SAMPLE_INTERVAL)

    total, count = _process_tree_cpu_total(proc.pid)
    if total is not None:
        if cpu_total is not None and total > cpu_total:
            active_samples += 1
        cpu_total = total
        cpu_max = max(cpu_max, total)
        children_max = max(children_max, count)
        cpu_seen = True
    daemon_total, daemon_count = _gradle_daemon_cpu_total()
    if daemon_total is not None:
        if daemon_last_total is not None:
            delta = daemon_total - daemon_last_total
            if delta > 0:
                daemon_cpu_accum += delta
                daemon_active_samples += 1
        daemon_last_total = daemon_total
        daemon_samples += 1
        daemon_max_procs = max(daemon_max_procs, daemon_count)
    proc.wait()
    out_thread.join()
    err_thread.join()

    stdout = "".join(stdout_lines)
    stderr = "".join(stderr_lines)
    tree_cpu = cpu_max if cpu_seen else None
    return (
        proc.returncode,
        stdout,
        stderr,
        tree_cpu,
        active_samples,
        samples,
        children_max,
        daemon_cpu_accum,
        daemon_active_samples,
        daemon_samples,
        daemon_max_procs,
    )


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
    active_samples = None
    samples = None
    children_max = None
    daemon_cpu = None
    daemon_active_samples = None
    daemon_samples = None
    daemon_max_procs = None
    if use_psutil:
        (
            returncode,
            stdout,
            stderr,
            gradle_cpu,
            active_samples,
            samples,
            children_max,
            daemon_cpu,
            daemon_active_samples,
            daemon_samples,
            daemon_max_procs,
        ) = _run_with_psutil(cmd)
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
        child_cpu_label = "tree cpu"
    timing = (
        f"Timing {teamA} vs {teamB} on {map}: "
        f"wall {wall_elapsed:.3f}s, worker cpu {cpu_elapsed:.3f}s, rc {returncode}"
    )
    if child_cpu is not None:
        cpu_pct = (child_cpu / wall_elapsed * 100.0) if wall_elapsed else 0.0
        timing += f", {child_cpu_label} {child_cpu:.3f}s ({cpu_pct:.1f}%)"
        if active_samples is not None and samples:
            active_wall = min(active_samples * SAMPLE_INTERVAL, wall_elapsed)
            timing += f", tree active ~{active_wall:.1f}s/{samples} samples, max procs {children_max}"
    else:
        timing += ", child cpu n/a"
    if daemon_cpu is not None:
        daemon_pct = (daemon_cpu / wall_elapsed * 100.0) if wall_elapsed else 0.0
        timing += f", daemon cpu {daemon_cpu:.3f}s ({daemon_pct:.1f}%)"
        if daemon_active_samples is not None and daemon_samples:
            active_wall = min(daemon_active_samples * SAMPLE_INTERVAL, wall_elapsed)
            timing += f", daemon active ~{active_wall:.1f}s/{daemon_samples} samples, max daemons {daemon_max_procs}"
    print(timing)
    return winner


if __name__ == "__main__":
    # Build first
    subprocess.run([GRADLEW, "build"])

    score = {teamA: 0, teamB: 0}
    debug = len(maps) == 1 and len(seeds) == 1

    # Change this number so it doesn't kill your computer
    NUM_WORKERS = 16
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
