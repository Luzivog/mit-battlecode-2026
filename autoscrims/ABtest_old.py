# Usage:
#   python autoscrims/ABtest.py TEAM_A TEAM_B [--maps-dir DIR] [--maps MAP1,MAP2] [--reps N]
#   Optional .env: AB_PLAYER1, AB_PLAYER2, AB_MAPS_DIR, AB_REPS, AB_SEED, AB_WORKERS, AB_NO_BUILD, AB_TIMEOUT
# Notes:
#   --reps repeats each map N times (each repeat runs both sides); --seed fixes the seed for all entries.
import math
import os
import random
import subprocess
import sys
from argparse import ArgumentParser
from collections import Counter
from dataclasses import dataclass, field
from datetime import datetime
from multiprocessing.pool import ThreadPool
from pathlib import Path
from threading import Lock
from rich.console import Console
from rich.segment import Segments
from rich.table import Table
from rich.text import Text

try:
    from dotenv import load_dotenv
except ImportError:
    load_dotenv = None

if load_dotenv is not None:
    load_dotenv()

ROOT = Path(__file__).parent.parent
DEFAULT_MAPS_DIR = ROOT / "test_maps"


def gradle_cmd() -> list[str]:
    if os.name == "nt":
        return ["cmd", "/c", str(ROOT / "gradlew.bat")]
    return [str(ROOT / "gradlew")]


def list_local_maps(maps_dir: Path) -> list[str]:
    maps = [p.stem for p in maps_dir.glob("*.map26")]
    if not maps:
        raise SystemExit(f"No .map26 files found in {maps_dir}")
    return sorted(maps, key=str.lower)


def env_first(*names: str) -> str | None:
    for name in names:
        value = os.getenv(name)
        if value:
            return value
    return None


def parse_bool(value: str | None) -> bool:
    if value is None:
        return False
    return value.strip().lower() in ("1", "true", "yes", "y", "on")


def parse_int(value: str | None) -> int | None:
    if value is None:
        return None
    try:
        return int(value)
    except ValueError:
        return None


def parse_float(value: str | None) -> float | None:
    if value is None:
        return None
    try:
        return float(value)
    except ValueError:
        return None


def parse_maps(value: str | None) -> list[str] | None:
    if not value:
        return None
    parts = value.replace(",", " ").split()
    return [part for part in parts if part]


def binom_p_value(wins: int, total: int) -> float:
    if total <= 0:
        return 1.0
    wins = max(0, min(wins, total))
    tail = sum(math.comb(total, k) for k in range(0, min(wins, total - wins) + 1))
    p_value = 2.0 * (tail / (1 << total))
    return min(1.0, p_value)


def format_p_value(p_value: float) -> str:
    if p_value < 1e-6:
        return "<1e-6"
    return f"{p_value:.3g}"

@dataclass
class MapEntry:
    name: str
    label: str
    rep: int


@dataclass
class Match:
    map_label: str
    reverse: bool
    player1_wins: bool
    win_condition: str
    replay_name: str

    @property
    def win_condition_short(self) -> str:
        return {
           "The winning team destroyed all of the enemy team's rat kings.": "KILL",
           "The winning team had more points at the end of the game.": "PTS",
           "The winning team won arbitrarily (coin flip).": "RNG",
        }.get(self.win_condition, self.win_condition)

@dataclass
class PlayerStatistics:
    wins: int = 0
    win_maps: int = 0
    draw_maps: int = 0
    lose_maps: int = 0
    wins_by_condition: Counter = field(default_factory=lambda: Counter())

@dataclass
class State:
    player1: str
    player2: str
    maps: list[MapEntry]
    matches: list[Match]
    console: Console
    timestamp: str
    in_progress: set[tuple[str, bool]] = field(default_factory=set)
    lock: Lock = field(default_factory=Lock, repr=False)

    def print(self) -> None:
        maps_table = self.get_maps_table()
        stats_table = self.get_stats_table()

        maps_segments = list(maps_table.__rich_console__(self.console, self.console.options))
        stats_segments = list(stats_table.__rich_console__(self.console, self.console.options))

        maps_footer = maps_segments[-2].text
        stats_header = stats_segments[0].text

        separator = "┢"

        separator_length = max(len(maps_footer), len(stats_header))
        for i in range(1, separator_length):
            has_next = i < separator_length - 1
            has_up = i < len(maps_footer) and maps_footer[i] in "┴┘"
            has_down = i < len(stats_header) and stats_header[i] in "┳┓"

            if has_next and not has_up and not has_down:
                separator += "━"
            elif has_next and has_up and not has_down:
                separator += "┷"
            elif has_next and not has_up and has_down:
                separator += "┳"
            elif has_next and has_up and has_down:
                separator += "╈"
            elif not has_next and has_up and not has_down:
                separator += "┙"
            elif not has_next and not has_up and has_down:
                separator += "┓"
            elif not has_next and has_up and has_down:
                separator += "┪"

        self.console.print(Segments([*maps_segments[:-2], Text(separator, style="bold"), *stats_segments[2:]]))

    def get_maps_table(self) -> Table:
        table = Table()

        column_groups = 4
        for _ in range(column_groups):
            for column in ["Map", "1", "2"]:
                table.add_column(column, style="bold" if column == "Map" else None, overflow="fold")

        row_count = math.ceil(len(self.maps) / column_groups)
        for y in range(row_count):
            row = []

            for x in range(column_groups):
                map_idx = x * row_count + y
                if map_idx >= len(self.maps):
                    row.extend([""] * 3)
                    continue

                map_entry = self.maps[map_idx]
                map_label = map_entry.label
                wins = 0
                losses = 0
                slots = ["", ""]

                for reverse in [False, True]:
                    match = next(
                        (
                            match
                            for match in self.matches
                            if match.map_label == map_label and match.reverse == reverse
                        ),
                        None,
                    )
                    if match is None:
                        if (map_label, reverse) in self.in_progress:
                            slots[1 if reverse else 0] = "[orange1]...[/]"
                        else:
                            slots[1 if reverse else 0] = ""
                    elif match.player1_wins != reverse:
                        # row.append(f"[green][link={self.get_viewer_url(match.replay_name)}]{match.win_condition_short}[/][/]")
                        slots[1 if reverse else 0] = f"[green]{match.win_condition_short}[/]"
                        wins += 1
                    else:
                        # row.append(f"[red][link={self.get_viewer_url(match.replay_name)}]{match.win_condition_short}[/][/]")
                        slots[1 if reverse else 0] = f"[red]{match.win_condition_short}[/]"
                        losses += 1

                if wins == 2:
                    map_color = "green"
                elif losses == 2:
                    map_color = "red"
                elif wins + losses == 2:
                    map_color = "yellow"
                else:
                    map_color = None

                map_prefix = f"[{map_color}]" if map_color is not None else ""
                map_suffix = "[/]" if map_color is not None else ""

                row.extend([map_prefix + map_label + map_suffix, slots[0], slots[1]])

            table.add_row(*row)

        return table

    def get_stats_table(self) -> Table:
        table = Table()

        table.add_column("Player", style="bold", overflow="fold")
        table.add_column("Results", overflow="fold")
        table.add_column("Win rate", overflow="fold")
        table.add_column("p-value", overflow="fold")
        table.add_column("Maps", overflow="fold")

        player1_stats = PlayerStatistics()
        player2_stats = PlayerStatistics()

        for map_entry in self.maps:
            wins = 0
            losses = 0

            for reverse in [False, True]:
                match = next(
                    (
                        match
                        for match in self.matches
                        if match.map_label == map_entry.label and match.reverse == reverse
                    ),
                    None,
                )
                if match is None:
                    continue

                if match.player1_wins != reverse:
                    wins += 1
                    player1_stats.wins += 1
                    player1_stats.wins_by_condition[match.win_condition_short] += 1
                else:
                    losses += 1
                    player2_stats.wins += 1
                    player2_stats.wins_by_condition[match.win_condition_short] += 1

            if wins == 2:
                player1_stats.win_maps += 1
                player2_stats.lose_maps += 1
            elif losses == 2:
                player1_stats.lose_maps += 1
                player2_stats.win_maps += 1
            elif wins + losses == 2:
                player1_stats.draw_maps += 1
                player2_stats.draw_maps += 1

        conditions = set(player1_stats.wins_by_condition.keys()) | set(player2_stats.wins_by_condition.keys())
        conditions = sorted(conditions, key=lambda c: player1_stats.wins_by_condition[c] + player2_stats.wins_by_condition[c], reverse=True)
        for condition in conditions:
            table.add_column(condition, overflow="fold")

        match_count = len(self.matches)
        p_value_str = format_p_value(binom_p_value(player1_stats.wins, match_count))

        for name, stats, other_stats in [
            (self.player1, player1_stats, player2_stats),
            (self.player2, player2_stats, player1_stats)
        ]:
            row = [
                name,
                f"[green]{stats.wins}[/]/[red]{match_count - stats.wins}[/]",
                f"{stats.wins / match_count * 100 if match_count > 0 else 0:,.2f}%",
                p_value_str,
                f"[green]{stats.win_maps}[/]/[yellow]{stats.draw_maps}[/]/[red]{stats.lose_maps}[/]"
            ]

            for condition in conditions:
                wins = stats.wins_by_condition[condition]
                losses = other_stats.wins_by_condition[condition]
                row.append(f"[green]{wins}[/]/[red]{losses}[/]")

            table.add_row(*row)

        return table

    def get_viewer_url(self, replay_name: str) -> str:
        return f"http://localhost:8000/client/resources/app/dist/index.html?gameSource=http://localhost:8000/{replay_name}"

def run_match(
    state: State,
    map_entry: MapEntry,
    reverse: bool,
    seed: int,
    job_idx: int,
    timeout: float | None,
) -> None:
    player1 = state.player1 if not reverse else state.player2
    player2 = state.player2 if not reverse else state.player1
    total = len(state.maps) * 2

    replay_name = (
        f"matches/run-{state.timestamp}-{player1}-vs-{player2}-on-"
        f"{map_entry.name}-rep{map_entry.rep}-seed{seed}.bc26"
    )

    with state.lock:
        state.in_progress.add((map_entry.label, reverse))
        state.print()

    try:
        process = subprocess.run(
            gradle_cmd()
            + [
                "runWithoutBuild",
                f"-PteamA={player1}",
                f"-PteamB={player2}",
                f"-Pmaps={map_entry.name}",
                f"-Pseed={seed}",
                f"-Preplay={replay_name}",
            ],
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            cwd=str(ROOT),
            timeout=timeout,
        )
    except subprocess.TimeoutExpired:
        with state.lock:
            state.in_progress.discard((map_entry.label, reverse))
            state.print()
        return
    stdout = process.stdout.decode("utf-8")

    if process.returncode != 0:
        with state.lock:
            state.in_progress.discard((map_entry.label, reverse))
            state.print()
        print(
            f"Gradle exited with status code {process.returncode} after running "
            f"{player1} vs. {player2} on {map_entry.name}\n{stdout}"
        )
        sys.exit(process.returncode)

    lines = stdout.splitlines()

    winner_line = next(line for line in lines if ") wins (" in line)
    player1_wins = "(A)" in winner_line

    win_condition_line = next(line for line in lines if line.startswith("[server] Reason: "))
    win_condition = win_condition_line.split(": ", 1)[1]

    with state.lock:
        state.in_progress.discard((map_entry.label, reverse))
        state.matches.append(Match(map_entry.label, reverse, player1_wins, win_condition, replay_name))
        state.print()

def main() -> None:
    default_player1 = env_first("AB_PLAYER1", "BC_TEAM_A", "BC_PLAYER1")
    default_player2 = env_first("AB_PLAYER2", "BC_TEAM_B", "BC_PLAYER2")
    default_maps = env_first("AB_MAPS", "BC_MAPS")
    default_maps_dir = env_first("AB_MAPS_DIR", "BC_MAPS_DIR")
    default_reps = parse_int(env_first("AB_REPS", "BC_REPS")) or 1
    default_timeout = parse_float(env_first("AB_TIMEOUT", "BC_TIMEOUT"))
    default_workers = env_first("AB_WORKERS", "BC_WORKERS")
    default_seed = env_first("AB_SEED", "BC_SEED")
    default_no_build = parse_bool(env_first("AB_NO_BUILD", "BC_NO_BUILD"))

    parser = ArgumentParser(description="Compare the performance of two players.")
    parser.add_argument("player1", nargs="?", default=default_player1, help="name of the first player")
    parser.add_argument("player2", nargs="?", default=default_player2, help="name of the second player")
    parser.add_argument(
        "--maps",
        default=default_maps,
        help="Comma/space-separated map list (defaults to all local .map26 files).",
    )
    parser.add_argument(
        "--maps-dir",
        default=default_maps_dir or str(DEFAULT_MAPS_DIR),
        help="Directory of .map26 files (ignored if --maps is set).",
    )
    parser.add_argument(
        "--reps",
        type=int,
        default=default_reps,
        help="Repeat each map this many times (each repeat plays both sides).",
    )
    parser.add_argument(
        "--timeout",
        type=float,
        default=default_timeout,
        help="Seconds before aborting a single match (omit or <=0 to disable).",
    )
    parser.add_argument(
        "--workers",
        type=int,
        default=int(default_workers) if default_workers else 8,
        help="Number of parallel workers.",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=int(default_seed) if default_seed else None,
        help="Fixed seed for every map entry (optional).",
    )
    parser.add_argument(
        "--no-build",
        action="store_true",
        default=default_no_build,
        help="Skip the initial gradle build step.",
    )

    args = parser.parse_args()

    if not args.player1 or not args.player2:
        raise SystemExit("Provide player1/player2 args or set AB_PLAYER1/AB_PLAYER2 in .env.")

    base_maps = parse_maps(args.maps) if args.maps else list_local_maps(Path(args.maps_dir))
    timeout = args.timeout if args.timeout and args.timeout > 0 else None
    if args.reps < 1:
        raise SystemExit("--reps must be >= 1")
    map_entries = []
    for map_name in base_maps:
        for rep in range(1, args.reps + 1):
            label = f"{map_name} #{rep}" if args.reps > 1 else map_name
            map_entries.append(MapEntry(map_name, label, rep))
    os.makedirs(ROOT / "matches", exist_ok=True)

    if not args.no_build:
        subprocess.run(gradle_cmd() + ["build"], check=False, cwd=str(ROOT))
    

    console = Console(highlight=False)
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")

    state = State(args.player1, args.player2, map_entries, [], console, timestamp)
    state.print()

    rng = random.Random(args.seed) if args.seed is not None else random.Random()
    seed_by_label = {}
    for entry in map_entries:
        seed_by_label[entry.label] = (
            args.seed if args.seed is not None else rng.randint(0, 10**9)
        )

    matches = []
    total_jobs = len(map_entries) * 2
    job_idx = 0
    for entry in map_entries:
        seed = seed_by_label[entry.label]
        for reverse in [False, True]:
            job_idx += 1
            matches.append((state, entry, reverse, seed, job_idx, timeout))

    with ThreadPool(args.workers) as pool:
        pool.starmap(run_match, matches)

if __name__ == "__main__":
    main()
