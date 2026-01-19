#!/usr/bin/env python3
import argparse
import concurrent.futures
import os
import random
import re
import subprocess
import threading
import time
from collections import Counter


ILLEGAL_FILENAME_CHARS = re.compile(r'[<>:"/\\|?*]+')


def sanitize_filename(name):
    return ILLEGAL_FILENAME_CHARS.sub("_", name).strip()


def list_map_names(maps_dir):
    if not os.path.isdir(maps_dir):
        raise SystemExit(f"Maps directory not found: {maps_dir}")
    maps = []
    for entry in os.listdir(maps_dir):
        if entry.lower().endswith(".map26"):
            base, _ = os.path.splitext(entry)
            maps.append(base)
    if not maps:
        raise SystemExit(f"No .map26 files found in: {maps_dir}")
    return sorted(maps, key=str.lower)


def gradle_cmd():
    if os.name == "nt":
        return ["cmd", "/c", "gradlew.bat"]
    return ["./gradlew"]


class ThreadStats:
    def __init__(self):
        self.lock = threading.Lock()
        self.active = 0
        self.max_active = 0
        self.thread_ids = set()

    def start(self):
        tid = threading.get_ident()
        with self.lock:
            self.thread_ids.add(tid)
            self.active += 1
            if self.active > self.max_active:
                self.max_active = self.active
        return tid

    def finish(self):
        with self.lock:
            self.active -= 1


def parse_winner(output):
    if "(A) wins" in output:
        return "A"
    if "(B) wins" in output:
        return "B"
    return None


def run_match(job):
    (
        map_name,
        seed,
        team_a,
        team_b,
        base_cmd,
        gradle_args,
        engine_props,
        verbose,
        stats,
    ) = job
    thread_name = threading.current_thread().name
    stats.start()
    start = time.time()
    try:
        print(f"[{thread_name}] Start {team_a} vs {team_b} on {map_name} seed {seed}")
        replay_name = sanitize_filename(
            f"{team_a}-vs-{team_b}-on-{map_name}-seed-{seed}.bc26"
        )
        cmd = base_cmd + gradle_args + [
            "run",
            "-x",
            "build",
            f"-PteamA={team_a}",
            f"-PteamB={team_b}",
            f"-Pmaps={map_name}",
            f"-Pseed={seed}",
            f"-Preplay=matches/{replay_name}",
        ] + engine_props
        result = subprocess.run(cmd, capture_output=True, text=True)
        output = (result.stdout or "") + (result.stderr or "")
        if verbose:
            print(output)
        winner = parse_winner(output)
        duration = time.time() - start
        status = "ok" if result.returncode == 0 else f"exit {result.returncode}"
        if "resign" in output:
            status = f"{status}, resign detected"
        print(
            f"[{thread_name}] {winner} won {team_a} vs {team_b} on {map_name} in {duration:.1f}s ({status})"
        )
        return {
            "map": map_name,
            "seed": seed,
            "team_a": team_a,
            "team_b": team_b,
            "winner": winner,
            "returncode": result.returncode,
        }
    finally:
        stats.finish()


def default_workers():
    cpu = os.cpu_count() or 4
    return max(2, min(12, cpu))


def main():
    parser = argparse.ArgumentParser(
        description="Run all local maps twice (swap teams) using multithreading."
    )
    parser.add_argument("team_a", help="Team name for slot A.")
    parser.add_argument("team_b", help="Team name for slot B.")
    parser.add_argument("--maps-dir", default="maps", help="Directory of .map26 files.")
    parser.add_argument(
        "--workers",
        type=int,
        default=default_workers(),
        help="Number of worker threads.",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=None,
        help="Fixed seed for every map (optional).",
    )
    parser.add_argument(
        "--no-build",
        action="store_true",
        help="Skip the initial gradle build step.",
    )
    parser.add_argument(
        "--engine-output",
        action="store_true",
        help="Enable robot output (slower).",
    )
    parser.add_argument(
        "--validate-maps",
        action="store_true",
        help="Validate maps before running (slower).",
    )
    parser.add_argument(
        "--show-indicators",
        action="store_true",
        help="Show indicators (slower).",
    )
    parser.add_argument(
        "--debug",
        action="store_true",
        help="Enable engine debug methods (slower).",
    )
    parser.add_argument(
        "--gradle-arg",
        action="append",
        default=[],
        help="Extra gradle arg (repeatable), e.g. --gradle-arg --no-daemon.",
    )
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Print full match output for each run.",
    )
    args = parser.parse_args()

    maps = list_map_names(args.maps_dir)
    base_cmd = gradle_cmd()
    os.makedirs("matches", exist_ok=True)

    if not args.no_build:
        subprocess.run(base_cmd + ["build"], check=False)

    rng = random.Random(args.seed) if args.seed is not None else random.Random()
    seed_by_map = {
        map_name: (args.seed if args.seed is not None else rng.randint(0, 10**9))
        for map_name in maps
    }

    engine_props = [
        f"-PoutputVerbose={'true' if args.engine_output else 'false'}",
        f"-PvalidateMaps={'true' if args.validate_maps else 'false'}",
        f"-PshowIndicators={'true' if args.show_indicators else 'false'}",
        f"-Pdebug={'true' if args.debug else 'false'}",
    ]
    gradle_args = list(args.gradle_arg)
    stats = ThreadStats()
    jobs = []
    for map_name in maps:
        seed = seed_by_map[map_name]
        jobs.append(
            (
                map_name,
                seed,
                args.team_a,
                args.team_b,
                base_cmd,
                gradle_args,
                engine_props,
                args.verbose,
                stats,
            )
        )
        jobs.append(
            (
                map_name,
                seed,
                args.team_b,
                args.team_a,
                base_cmd,
                gradle_args,
                engine_props,
                args.verbose,
                stats,
            )
        )

    total_jobs = len(jobs)
    print(f"Running {total_jobs} matches across {len(maps)} maps with {args.workers} workers.")
    start = time.time()
    results = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=args.workers) as executor:
        futures = [executor.submit(run_match, job) for job in jobs]
        for future in concurrent.futures.as_completed(futures):
            results.append(future.result())
    elapsed = time.time() - start

    wins = Counter()
    failures = 0
    for result in results:
        if result["returncode"] != 0:
            failures += 1
        winner = result["winner"]
        if winner == "A":
            wins[result["team_a"]] += 1
        elif winner == "B":
            wins[result["team_b"]] += 1

    print(f"Finished {total_jobs} matches in {elapsed:.1f}s.")
    print(f"Wins: {dict(wins)}")
    if failures:
        print(f"Failures: {failures}")

    threads_used = len(stats.thread_ids)
    if total_jobs > 1 and args.workers > 1:
        if stats.max_active > 1 and threads_used > 1:
            print(
                f"Multithreading verified: {threads_used} threads used, max parallel {stats.max_active}."
            )
        else:
            print(
                "WARNING: multithreading not observed (only one active thread at a time)."
            )


if __name__ == "__main__":
    main()
