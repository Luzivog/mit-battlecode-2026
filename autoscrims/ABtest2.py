import math
import os
import random
import subprocess
import sys
import threading
from argparse import ArgumentParser
from collections import Counter
from dataclasses import dataclass, field
from datetime import datetime
from multiprocessing.pool import ThreadPool
from pathlib import Path
from rich.console import Console
from rich.live import Live
from rich.segment import Segments
from rich.table import Table
from rich.text import Text

ROOT = Path(__file__).parent.parent
MAPS_DIR = ROOT / "maps"


def gradle_cmd() -> list[str]:
    if os.name == "nt":
        return ["cmd", "/c", str(ROOT / "gradlew.bat")]
    return [str(ROOT / "gradlew")]


def list_local_maps() -> list[str]:
    maps = [p.stem for p in MAPS_DIR.glob("*.map26")]
    if not maps:
        raise SystemExit(f"No .map26 files found in {MAPS_DIR}")
    return sorted(maps, key=str.lower)

@dataclass
class Match:
    map: str
    reverse: bool
    repeat: int
    seed: int
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
    maps: list[str]
    matches: list[Match]
    console: Console
    timestamp: str
    repeats: int
    lock: threading.Lock = field(default_factory=threading.Lock)
    live: Live | None = None

    def render(self) -> Segments:
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

        return Segments([*maps_segments[:-2], Text(separator, style="bold"), *stats_segments[2:]])

    def print(self) -> None:
        self.console.print(self.render())

    def add_match(self, match: Match) -> None:
        with self.lock:
            self.matches.append(match)
            renderable = self.render()
            if self.live is not None:
                self.live.update(renderable)
            else:
                self.console.print(renderable)

    def get_maps_table(self) -> Table:
        table = Table()

        results_per_map = self.repeats * 2
        column_groups = max(1, 4 // max(1, self.repeats))
        for _ in range(column_groups):
            table.add_column("Map", style="bold", overflow="fold")
            for idx in range(results_per_map):
                table.add_column(str(idx + 1), overflow="fold")

        row_count = math.ceil(len(self.maps) / column_groups)
        for y in range(row_count):
            row = []

            for x in range(column_groups):
                map_idx = x * row_count + y
                if map_idx >= len(self.maps):
                    row.extend([""] * (1 + results_per_map))
                    continue

                map = self.maps[map_idx]
                wins = 0
                losses = 0
                slots = [""] * results_per_map
                for match in self.matches:
                    if match.map != map:
                        continue
                    slot = match.repeat * 2 + (1 if match.reverse else 0)
                    if slot < 0 or slot >= results_per_map:
                        continue
                    if match.player1_wins != match.reverse:
                        slots[slot] = f"[green]{match.win_condition_short}[/]"
                        wins += 1
                    else:
                        slots[slot] = f"[red]{match.win_condition_short}[/]"
                        losses += 1
                row.extend(slots)

                if wins == results_per_map:
                    map_color = "green"
                elif losses == results_per_map:
                    map_color = "red"
                elif wins + losses == results_per_map:
                    map_color = "yellow"
                else:
                    map_color = None

                map_prefix = f"[{map_color}]" if map_color is not None else ""
                map_suffix = "[/]" if map_color is not None else ""

                row.append(map_prefix + map + map_suffix)
                row.extend(slots)

            table.add_row(*row)

        return table

    def get_stats_table(self) -> Table:
        table = Table()

        table.add_column("Player", style="bold", overflow="fold")
        table.add_column("Results", overflow="fold")
        table.add_column("Win rate", overflow="fold")
        table.add_column("Maps", overflow="fold")

        player1_stats = PlayerStatistics()
        player2_stats = PlayerStatistics()

        matches_per_map = self.repeats * 2
        for map in self.maps:
            wins = 0
            losses = 0
            for match in self.matches:
                if match.map != map:
                    continue
                if match.player1_wins != match.reverse:
                    wins += 1
                    player1_stats.wins += 1
                    player1_stats.wins_by_condition[match.win_condition_short] += 1
                else:
                    losses += 1
                    player2_stats.wins += 1
                    player2_stats.wins_by_condition[match.win_condition_short] += 1

            if wins + losses == matches_per_map:
                if wins == matches_per_map:
                    player1_stats.win_maps += 1
                    player2_stats.lose_maps += 1
                elif losses == matches_per_map:
                    player1_stats.lose_maps += 1
                    player2_stats.win_maps += 1
                else:
                    player1_stats.draw_maps += 1
                    player2_stats.draw_maps += 1

        conditions = set(player1_stats.wins_by_condition.keys()) | set(player2_stats.wins_by_condition.keys())
        conditions = sorted(conditions, key=lambda c: player1_stats.wins_by_condition[c] + player2_stats.wins_by_condition[c], reverse=True)
        for condition in conditions:
            table.add_column(condition, overflow="fold")

        match_count = len(self.matches)

        for name, stats, other_stats in [
            (self.player1, player1_stats, player2_stats),
            (self.player2, player2_stats, player1_stats)
        ]:
            row = [
                name,
                f"[green]{stats.wins}[/]/[red]{match_count - stats.wins}[/]",
                f"{stats.wins / match_count * 100 if match_count > 0 else 0:,.2f}%",
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

def run_match(state: State, map: str, reverse: bool, repeat: int) -> None:
    player1 = state.player1 if not reverse else state.player2
    player2 = state.player2 if not reverse else state.player1

    seed = random.randint(0, 10**9)
    replay_name = f"replays/run-{state.timestamp}-{player1}-vs-{player2}-on-{map}-seed{seed}.bc26"

    process = subprocess.run(gradle_cmd() + [
        "runWithoutBuild",
        f"-PteamA={player1}",
        f"-PteamB={player2}",
        f"-Pmaps={map}",
        f"-Pseed={seed}",
        f"-PreplayPath={replay_name}"
    ], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, cwd=str(ROOT))
    stdout = process.stdout.decode("utf-8")

    if process.returncode != 0:
        print(f"Gradle exited with status code {process.returncode} after running {player1} vs. {player2} on {map}\n{stdout}")
        sys.exit(process.returncode)

    lines = stdout.splitlines()

    winner_line = next(line for line in lines if ") wins (" in line)
    player1_wins = "(A)" in winner_line

    win_condition_line = next(line for line in lines if line.startswith("[server] Reason: "))
    win_condition = win_condition_line.split(": ", 1)[1]

    state.add_match(Match(map, reverse, repeat, seed, player1_wins, win_condition, replay_name))

def main() -> None:
    parser = ArgumentParser(description="Compare the performance of two players.")
    parser.add_argument("player1", type=str, help="name of the first player")
    parser.add_argument("player2", type=str, help="name of the second player")
    parser.add_argument(
        "--repeats",
        type=int,
        default=1,
        help="Number of repeats per side (total matches per map = repeats * 2).",
    )
    parser.add_argument(
        "--no-live",
        action="store_true",
        help="Print tables after each match instead of live updates.",
    )

    args = parser.parse_args()

    # build_proc = subprocess.run([str(Path(__file__).parent.parent / "gradlew"), "build", "-x", "checkForUpdates"])
    # if build_proc.returncode != 0:
    #     sys.exit(build_proc.returncode)

    maps = list_local_maps()


    console = Console(highlight=False)
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")

    if args.repeats < 1:
        raise SystemExit("repeats must be >= 1")

    state = State(args.player1, args.player2, maps, [], console, timestamp, args.repeats)

    matches = []
    for map in maps:
        for repeat in range(args.repeats):
            for reverse in [False, True]:
                matches.append((state, map, reverse, repeat))

    if args.no_live:
        state.print()
        with ThreadPool(8) as pool:
            pool.starmap(run_match, matches)
    else:
        with Live(state.render(), console=console, refresh_per_second=4) as live:
            state.live = live
            with ThreadPool(8) as pool:
                pool.starmap(run_match, matches)

if __name__ == "__main__":
    main()
