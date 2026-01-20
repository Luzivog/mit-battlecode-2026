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
# DEBUG_MAPS = ["pvparena"]
teamA = "awubot_microtrapless"
teamB = "awubot_microimprov"
# maps = ["microtest"]
maps = DEFAULT + SPRINT_1
seeds = [""] * 1
# maps = "8cats_big,bigface,breakthewall,Cage,cheesemining_big,cheeseorcat,dangerous,debug_cat_right,diagonalmap,dirt_around_mine,dirtroad"
# maps = "isolated_cat,isolated_cheese,killcat,killcatormine,maybedonothing,melbournetrain,minewithcat,mixedmaze,nowall_bigbattle_big,nowallnocat"
# maps = "oneexit_small,realiseuarecooked,roundabout,rush_big,rush_small,segtree,sixseven,small1v1,small_dirtmaze,tunnelbattle_big,tunnelbattle_big_dirtless,veryverysmall"
# maps = "CheeseMines,CheeseMinesMedium,colony,FootballField,Ruins,Snails,Towers"
# maps = "DefaultSmall,DefaultMedium,DefaultLarge,evileye,cheeseguardians"

# maps = DEBUG_MAPS

# maps = ["Cage,colony,FootballField,Islands,PickYourSpawn,Ruins,Sawtooth,Snails,TestTube,Towers,WheresYourShovel"]
# maps = ["CombatTest2"]

debug = len(maps) == 1 and len(seeds) == 1
GRADLEW = "gradlew.bat" if os.name == "nt" else "./gradlew"
SAMPLE_INTERVAL = 0.25
REPO_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_DIR = os.path.join(REPO_DIR, "match_stdout")
JAVA_MATCH_KEYWORDS = ("battlecode", "bc26")
LOG_LINE_LIMIT = 120
GAP_CONTEXT_LINES = 3


def _child_cpu_total():
    if resource is None:
        return None
    usage = resource.getrusage(resource.RUSAGE_CHILDREN)
    return usage.ru_utime + usage.ru_stime


def _slug(value, max_len=40):
    cleaned = re.sub(r"[^A-Za-z0-9._-]+", "_", str(value))
    cleaned = cleaned.strip("_") or "item"
    return cleaned[:max_len]


def _short_line(line, limit=LOG_LINE_LIMIT):
    text = line.strip()
    if len(text) > limit:
        return text[: limit - 3] + "..."
    return text


def _format_timed_line(stdout_timed, idx, start_ts):
    t, line = stdout_timed[idx]
    if start_ts is None:
        return _short_line(line)
    rel = t - start_ts
    return f"+{rel:.1f}s {_short_line(line)}"


def _write_stdout_file(teamA, teamB, map, seed, stdout):
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    filename = (
        f"{_slug(map)}__{_slug(teamA)}_vs_{_slug(teamB)}__seed{_slug(seed, 20)}__"
        f"{os.getpid()}_{time.time_ns()}.log"
    )
    path = os.path.join(OUTPUT_DIR, filename)
    with open(path, "w", encoding="utf-8", newline="") as handle:
        handle.write(stdout)
    return path


def _summarize_stdout_timing(stdout_timed, start_ts):
    if not stdout_timed or start_ts is None:
        return None
    line_count = len(stdout_timed)
    match_start = None
    match_end = None
    max_gap = None
    gap_before = None
    gap_after = None
    gap_before_index = None
    gap_after_index = None
    if line_count >= 2:
        prev_t, prev_line = stdout_timed[0]
        for idx, (t, line) in enumerate(stdout_timed[1:], start=1):
            gap = t - prev_t
            if max_gap is None or gap > max_gap:
                max_gap = gap
                gap_before = prev_line
                gap_after = line
                gap_before_index = idx - 1
                gap_after_index = idx
            prev_t, prev_line = t, line
    for t, line in stdout_timed:
        if match_start is None and "Match Starting" in line:
            match_start = t
        if match_end is None and "Match Finished" in line:
            match_end = t
    return {
        "line_count": line_count,
        "match_start": (match_start - start_ts) if match_start else None,
        "match_end": (match_end - start_ts) if match_end else None,
        "match_duration": (match_end - match_start)
        if match_start is not None and match_end is not None
        else None,
        "max_gap": max_gap,
        "gap_before": gap_before,
        "gap_after": gap_after,
        "gap_before_index": gap_before_index,
        "gap_after_index": gap_after_index,
    }


def _drain_stream(stream, sink, timed_sink=None):
    for line in iter(stream.readline, ""):
        sink.append(line)
        if timed_sink is not None:
            timed_sink.append((time.perf_counter(), line))
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


def _java_cpu_total():
    if psutil is None:
        return None, 0
    total = 0.0
    count = 0
    for proc in psutil.process_iter(["name", "cmdline"]):
        try:
            name = (proc.info.get("name") or "").lower()
            if "java" not in name:
                continue
            cmdline = proc.info.get("cmdline") or []
        except (psutil.Error, OSError):
            continue
        cmdline_text = " ".join(cmdline).lower()
        if not any(keyword in cmdline_text for keyword in JAVA_MATCH_KEYWORDS):
            continue
        try:
            times = proc.cpu_times()
            total += times.user + times.system
            count += 1
        except (psutil.Error, OSError):
            continue
    return total, count


def _run_with_psutil(cmd):
    start_ts = time.perf_counter()
    proc = psutil.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    stdout_lines = []
    stderr_lines = []
    stdout_timed = []
    out_thread = threading.Thread(
        target=_drain_stream, args=(proc.stdout, stdout_lines, stdout_timed)
    )
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
    java_cpu_accum = 0.0
    java_last_total = None
    java_active_samples = 0
    java_samples = 0
    java_max_procs = 0

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
        java_total, java_count = _java_cpu_total()
        if java_total is not None:
            if java_last_total is not None:
                delta = java_total - java_last_total
                if delta > 0:
                    java_cpu_accum += delta
                    java_active_samples += 1
            java_last_total = java_total
            java_samples += 1
            java_max_procs = max(java_max_procs, java_count)
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
    java_total, java_count = _java_cpu_total()
    if java_total is not None:
        if java_last_total is not None:
            delta = java_total - java_last_total
            if delta > 0:
                java_cpu_accum += delta
                java_active_samples += 1
        java_last_total = java_total
        java_samples += 1
        java_max_procs = max(java_max_procs, java_count)
    proc.wait()
    out_thread.join()
    err_thread.join()

    stdout = "".join(stdout_lines)
    stderr = "".join(stderr_lines)
    tree_cpu = cpu_max if cpu_seen else None
    return {
        "returncode": proc.returncode,
        "stdout": stdout,
        "stderr": stderr,
        "tree_cpu": tree_cpu,
        "tree_active_samples": active_samples,
        "tree_samples": samples,
        "tree_max_procs": children_max,
        "daemon_cpu": daemon_cpu_accum,
        "daemon_active_samples": daemon_active_samples,
        "daemon_samples": daemon_samples,
        "daemon_max_procs": daemon_max_procs,
        "java_cpu": java_cpu_accum,
        "java_active_samples": java_active_samples,
        "java_samples": java_samples,
        "java_max_procs": java_max_procs,
        "stdout_timed": stdout_timed,
        "stdout_start_ts": start_ts,
    }


def run_game(params):
    teamA, teamB, map, seed = params
    wall_start = time.perf_counter()
    cpu_start = time.process_time()
    child_cpu_before = _child_cpu_total()
    print(GRADLEW)
    print(f"Run {teamA} vs {teamB} on {map} {seed}")
    use_psutil = psutil is not None
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
    java_cpu = None
    java_active_samples = None
    java_samples = None
    java_max_procs = None
    stdout_timed = None
    stdout_start_ts = None
    if use_psutil:
        result_info = _run_with_psutil(cmd)
        returncode = result_info["returncode"]
        stdout = result_info["stdout"]
        stderr = result_info["stderr"]
        gradle_cpu = result_info["tree_cpu"]
        active_samples = result_info["tree_active_samples"]
        samples = result_info["tree_samples"]
        children_max = result_info["tree_max_procs"]
        daemon_cpu = result_info["daemon_cpu"]
        daemon_active_samples = result_info["daemon_active_samples"]
        daemon_samples = result_info["daemon_samples"]
        daemon_max_procs = result_info["daemon_max_procs"]
        java_cpu = result_info["java_cpu"]
        java_active_samples = result_info["java_active_samples"]
        java_samples = result_info["java_samples"]
        java_max_procs = result_info["java_max_procs"]
        stdout_timed = result_info["stdout_timed"]
        stdout_start_ts = result_info["stdout_start_ts"]
    else:
        completed = subprocess.run(cmd, capture_output=True, text=True)
        stdout = completed.stdout
        stderr = completed.stderr
        returncode = completed.returncode
    result = stdout
    _write_stdout_file(teamA, teamB, map, seed, stdout)
    # print(result)
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
    log_summary = _summarize_stdout_timing(stdout_timed, stdout_start_ts)
    if log_summary:
        log_line = (
            f"Log timing {teamA} vs {teamB} on {map}: lines {log_summary['line_count']}"
        )
        if log_summary["match_start"] is not None:
            log_line += f", match start +{log_summary['match_start']:.1f}s"
        if log_summary["match_end"] is not None:
            log_line += f", match finish +{log_summary['match_end']:.1f}s"
        if log_summary["match_duration"] is not None:
            log_line += f", match duration {log_summary['match_duration']:.1f}s"
        if log_summary["max_gap"] is not None:
            log_line += f", max gap {log_summary['max_gap']:.1f}s"
        print(log_line)
        if log_summary["max_gap"] is not None:
            before = _short_line(log_summary["gap_before"])
            after = _short_line(log_summary["gap_after"])
            print(f"Log gap max {log_summary['max_gap']:.1f}s between: {before} -> {after}")
            gap_before_index = log_summary.get("gap_before_index")
            gap_after_index = log_summary.get("gap_after_index")
            if (
                gap_before_index is not None
                and gap_after_index is not None
                and stdout_timed
            ):
                before_start = max(0, gap_before_index - GAP_CONTEXT_LINES)
                before_end = gap_before_index + 1
                after_start = gap_after_index
                after_end = min(
                    len(stdout_timed), gap_after_index + GAP_CONTEXT_LINES + 1
                )
                print("Log gap context before:")
                for idx in range(before_start, before_end):
                    print(f"  {_format_timed_line(stdout_timed, idx, stdout_start_ts)}")
                print("Log gap context after:")
                for idx in range(after_start, after_end):
                    print(f"  {_format_timed_line(stdout_timed, idx, stdout_start_ts)}")
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
    if java_cpu is not None:
        java_pct = (java_cpu / wall_elapsed * 100.0) if wall_elapsed else 0.0
        timing += f", java cpu {java_cpu:.3f}s ({java_pct:.1f}%)"
        if java_active_samples is not None and java_samples:
            active_wall = min(java_active_samples * SAMPLE_INTERVAL, wall_elapsed)
            timing += f", java active ~{active_wall:.1f}s/{java_samples} samples, max java procs {java_max_procs}"
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
    results = []
    total_matches = len(params)
    with concurrent.futures.ProcessPoolExecutor(max_workers=NUM_WORKERS) as executor:
        futures = [executor.submit(run_game, param) for param in params]
        completed = 0
        for future in concurrent.futures.as_completed(futures):
            winner = future.result()
            results.append(winner)
            completed += 1
            if winner:
                score[winner] += 1
            print(
                f"Running score ({completed}/{total_matches}): "
                f"{teamA} {score[teamA]} - {teamB} {score[teamB]}"
            )
    end = time.time()

    print(f"Ran {len(results)} matches in {end - start}s")
    print(f"SCORE {score[teamA]}-{score[teamB]}")
