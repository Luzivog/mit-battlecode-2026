# Usage:
#   python autoscrims/ABtest.py TEAM_A TEAM_B [--pool official|sprint1|quick|custom] [--maps M1,M2] [--maps-dir DIR]
#                               [--reps N] [--workers N] [--seed S | --seed-base S] [--engine override|official]
#   Optional .env: AB_PLAYER1, AB_PLAYER2, AB_MAPS, AB_MAPS_DIR, AB_REPS, AB_SEED, AB_WORKERS, AB_NO_BUILD, AB_TIMEOUT
# Notes:
#   Every map entry is played twice (TEAM_A as team A, then as team B) with the same seed.
#   --reps repeats each map N times with different seeds; --seed fixes one seed for all entries;
#   --seed-base makes the per-entry random seeds reproducible.
#   Default runner (--runner java) compiles once, snapshots build/classes to a temp dir and runs
#   battlecode.server.Main directly with `java` (no Gradle daemon per match; later edits/builds
#   cannot leak into a running test). --runner gradle is the original `./gradlew runWithoutBuild` path.
#   --engine override (default): src/battlecode/** overrides shadow the engine jar, so -seed is honoured.
#   --engine official: engine jar first on the classpath = unmodified engine, seed comes from the map file
#   (exactly what the official servers do). Results: match_results/abtest_<timestamp>.{txt,json}.
import io
import json
import math
import os
import random
import re
import shutil
import subprocess
import sys
import tempfile
import time
from argparse import ArgumentParser
from collections import Counter
from dataclasses import asdict, dataclass, field
from datetime import datetime
from multiprocessing.pool import ThreadPool
from pathlib import Path
from threading import Lock
from rich.console import Console, Group
from rich.live import Live
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
DEFAULT_MAPS_DIR = ROOT / "maps"
RESULTS_DIR = ROOT / "match_results"
SHIT_TESTS: set[str] = set(["troll1", "troll2", "5t4rv4t10n_1337", "coop"])  # Skipped when listing --maps-dir.
ERROR_LOG_LIMIT = 10
ERROR_LOG_LINES = 12
COMPILE_RETRY_DELAY = 10
COMPILE_RETRY_ATTEMPTS = 1

# The 43 maps bundled in the engine jar (battlecode/world/resources) = the public BC26 map pool.
# Default 5 (Jan 5-10), sprint-1 batch (Jan 12), sprint-2 batch (Jan 19). All are also in maps/.
DEFAULT_MAPS = ["DefaultSmall", "DefaultMedium", "DefaultLarge", "evileye", "cheeseguardians"]
SPRINT1_MAPS = [
    "arrows", "cheesefarm", "dirtfulcat", "dirtpassageway", "keepout", "Meow", "Nofreecheese", "pipes",
    "popthecork", "rift", "sittingducks", "starvation", "thunderdome", "trapped", "wallsofparadis", "ZeroDay",
]
SPRINT2_MAPS = [
    "5t4rv4t10n_1337", "averyfineline", "averystrangespace", "canyoudig", "cheesebottles", "closeup",
    "corridorofdoomanddespair", "EscapeTheNight", "hatefullattice", "jail", "knifefight", "mercifullattice",
    "minimaze", "peaceinourtime", "safelycontained", "streetsofnewyork", "TheHeist", "tiny", "toomuchcheese",
    "uneruesansfin", "whatsthecatdoin", "whereisthecheese",
]
OFFICIAL_MAPS = DEFAULT_MAPS + SPRINT1_MAPS + SPRINT2_MAPS
# Inner-loop subset: 5 rotational / 3 H / 4 V maps, 3 decided on points (vs awubot); ~70 s/game mean.
QUICK_MAPS = [
    "DefaultSmall", "DefaultMedium", "evileye", "arrows", "whereisthecheese", "cheesefarm",
    "jail", "TheHeist", "pipes", "sittingducks", "knifefight", "tiny",
]
# Team-made maps in maps/ (not official; excludes troll1/2, coop and "pathfinding test"): overfitting holdout.
CUSTOM_MAPS = [
    "DefaultSmallCatless", "DefaultMediumCatless", "DefaultLargeCatless", "arrowsCatless", "bridges",
    "bridgesCatless", "checkerboard", "cups", "far", "hidden", "maze1", "maze2", "maze3", "messy", "mining",
    "quadrants", "micro1", "micro2", "microtest", "microtest_wide", "pvparena", "pvparena_mines",
    "pvparena_mines2", "testpathfinding", "testpathfinding2",
]
MAP_POOLS = {
    "official": OFFICIAL_MAPS,           # 43 maps, 86 games per seed
    "sprint1": DEFAULT_MAPS + SPRINT1_MAPS,  # 21 maps == test_maps/
    "quick": QUICK_MAPS,                 # 12 maps, 24 games per seed (inner loop)
    "custom": CUSTOM_MAPS,               # 25 team-made maps (holdout)
}

WIN_CONDITIONS = {
    "The winning team destroyed all of the enemy team's rat kings.": "KILL",
    "The winning team had more points at the end of the game.": "PTS",
    "The winning team won on tiebreakers (more cheese)": "CHEESE",
    "The winning team won on tiebreakers (more robots alive).": "ALIVE",
    "The winning team won arbitrarily (coin flip).": "RNG",
    "Other team has resigned. Congrats on scaring them I guess...": "RESIGN",
}

# JVM flags for --runner java (same --add-opens as build.gradle; heap a bit above runWithoutBuild's 350m).
JAVA_OPTS = [
    "-Xmx1g",
    "-XX:+UseSerialGC",
    "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.math=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.util=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
    "--add-opens=java.base/sun.security.action=ALL-UNNAMED",
]
CLASSPATH_INIT_SCRIPT = """
allprojects {
  tasks.register('abtestPrintClasspath') {
    doLast { println 'ABTEST_CP=' + project.sourceSets.main.runtimeClasspath.asPath }
  }
}
"""


def gradle_cmd() -> list[str]:
    if os.name == "nt":
        return ["cmd", "/c", str(ROOT / "gradlew.bat")]
    return [str(ROOT / "gradlew")]


def list_local_maps(maps_dir: Path) -> list[str]:
    maps = [p.stem for p in maps_dir.glob("*.map26")]
    if not maps:
        raise SystemExit(f"No .map26 files found in {maps_dir}")
    return sorted(maps, key=str.lower)


def filter_shit_tests(maps: list[str]) -> list[str]:
    if not SHIT_TESTS:
        return maps
    skip = {name.lower() for name in SHIT_TESTS}
    return [name for name in maps if name.lower() not in skip]


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
    """Exact two-sided binomial test of H0: win probability = 0.5."""
    if total <= 0:
        return 1.0
    wins = max(0, min(wins, total))
    tail = sum(math.comb(total, k) for k in range(0, min(wins, total - wins) + 1))
    p_value = 2.0 * (tail / (1 << total))
    return min(1.0, p_value)


def wilson_interval(wins: int, total: int, z: float = 1.96) -> tuple[float, float]:
    if total <= 0:
        return 0.0, 1.0
    p = wins / total
    denom = 1 + z * z / total
    centre = (p + z * z / (2 * total)) / denom
    half = z * math.sqrt(p * (1 - p) / total + z * z / (4 * total * total)) / denom
    return max(0.0, centre - half), min(1.0, centre + half)


def format_p_value(p_value: float) -> str:
    if p_value < 1e-6:
        return "<1e-6"
    return f"{p_value:.3g}"


def tail_lines(text: str, limit: int) -> list[str]:
    if limit <= 0:
        return []
    lines = [line.rstrip() for line in text.splitlines()]
    non_empty = [line for line in lines if line.strip()]
    target = non_empty if non_empty else lines
    return target[-limit:]


def is_compile_error(output: str) -> bool:
    lower = output.lower()
    if "compilation failed" in lower or "compilejava failed" in lower:
        return True
    if "error:" in lower and "compilejava" in lower:
        return True
    return False


def robot_player_paths(team: str) -> list[Path]:
    package_path = Path(*team.split("."))
    base = ROOT / "src" / package_path
    return [base / "RobotPlayer.java", base / "RobotPlayer.scala"]


def validate_team_exists(team: str) -> None:
    candidates = robot_player_paths(team)
    if any(path.exists() for path in candidates):
        return
    expected = ", ".join(str(path) for path in candidates)
    raise SystemExit(f"Bot '{team}' not found. Expected RobotPlayer at {expected}.")


def validate_maps_exist(maps: list[str]) -> None:
    # The engine loads maps/<name>.map26, falling back to the maps bundled in the engine jar.
    known = {p.stem for p in DEFAULT_MAPS_DIR.glob("*.map26")} | set(OFFICIAL_MAPS)
    missing = [name for name in maps if name not in known]
    if missing:
        raise SystemExit(f"Unknown map(s) (not in maps/ nor bundled in the engine): {', '.join(missing)}")


def parse_result(stdout: str) -> dict | None:
    winner_line = next((line for line in stdout.splitlines() if ") wins (round " in line), None)
    reason_line = next((line for line in stdout.splitlines() if line.startswith("[server] Reason: ")), None)
    if winner_line is None or reason_line is None:
        return None
    round_match = re.search(r"wins \(round (\d+)\)", winner_line)
    seed_match = re.search(r"^\[server\] Seed: (-?\d+)", stdout, re.MULTILINE)
    return {
        "a_wins": "(A) wins" in winner_line,
        "condition": reason_line.split(": ", 1)[1],
        "round": int(round_match.group(1)) if round_match else None,
        "engine_seed": int(seed_match.group(1)) if seed_match else None,
    }


class GradleRunner:
    """Original behaviour: one `./gradlew runWithoutBuild` per match (needs one Gradle daemon per worker)."""

    name = "gradle"

    def command(self, team_a: str, team_b: str, map_name: str, seed: int | None, replay: str) -> list[str]:
        return gradle_cmd() + [
            "runWithoutBuild",
            f"-PteamA={team_a}",
            f"-PteamB={team_b}",
            f"-Pmaps={map_name}",
            f"-Pseed={seed}",
            f"-Preplay={replay}",
        ]

    def close(self) -> None:
        pass


class JavaRunner:
    """Runs battlecode.server.Main directly on a private snapshot of build/classes."""

    name = "java"

    def __init__(self, engine: str, show_indicators: bool):
        self.engine = engine
        self.show_indicators = show_indicators
        self.snapshot_dir: Path | None = None
        self.classes_dir: Path | None = None
        self.classpath: str | None = None

    def prepare(self, build: bool, teams: list[str]) -> str | None:
        """Compile (optional), resolve the engine classpath and snapshot classes. Returns error output or None."""
        with tempfile.NamedTemporaryFile("w", suffix=".gradle", delete=False) as handle:
            handle.write(CLASSPATH_INIT_SCRIPT)
            init_script = handle.name
        try:
            tasks = (["classes"] if build else []) + ["abtestPrintClasspath"]
            proc = subprocess.run(
                gradle_cmd() + ["--console=plain", "-q", "-I", init_script] + tasks,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                cwd=str(ROOT),
                text=True,
            )
        finally:
            os.unlink(init_script)
        output = proc.stdout or ""
        cp_line = next((line for line in output.splitlines() if line.startswith("ABTEST_CP=")), None)
        if proc.returncode != 0 or cp_line is None:
            return output
        build_outputs = {str((ROOT / "build" / "classes").resolve()), str((ROOT / "build" / "resources" / "main").resolve())}
        libraries = [entry for entry in cp_line[len("ABTEST_CP="):].strip().split(os.pathsep)
                     if entry and str(Path(entry).resolve()) not in build_outputs]
        self.snapshot_dir = Path(tempfile.mkdtemp(prefix="abtest-classes-"))
        self.classes_dir = self.snapshot_dir / "classes"
        shutil.copytree(ROOT / "build" / "classes", self.classes_dir)
        for team in teams:
            if not (self.classes_dir / Path(*team.split(".")) / "RobotPlayer.class").exists():
                return f"build/classes has no compiled RobotPlayer for '{team}' (run ./gradlew classes)"
        if self.engine == "official":
            # Engine jar first: its battlecode.* classes win over src/battlecode overrides (= official engine).
            entries = libraries + [str(self.classes_dir)]
        else:
            entries = [str(self.classes_dir)] + libraries
        self.classpath = os.pathsep.join(entries)
        return None

    def command(self, team_a: str, team_b: str, map_name: str, seed: int | None, replay: str) -> list[str]:
        java_home = os.getenv("JAVA_HOME")
        java = str(Path(java_home) / "bin" / "java") if java_home and (Path(java_home) / "bin" / "java").exists() else "java"
        props = {
            "bc.server.websocket": "false",
            "bc.server.wait-for-client": "false",
            "bc.server.mode": "headless",
            "bc.server.robot-player-to-system-out": "false",
            "bc.server.debug": "false",
            "bc.engine.debug-methods": "false",
            "bc.engine.enable-profiler": "false",
            "bc.engine.show-indicators": "true" if self.show_indicators else "false",
            "bc.game.team-a": team_a,
            "bc.game.team-b": team_b,
            "bc.game.team-a.url": str(self.classes_dir),
            "bc.game.team-b.url": str(self.classes_dir),
            "bc.game.team-a.package": team_a,
            "bc.game.team-b.package": team_b,
            "bc.game.maps": map_name,
            "bc.game.map-path": str(DEFAULT_MAPS_DIR),
            "bc.game.seed": str(seed if seed is not None else 0),
            "bc.server.validate-maps": "true",
            "bc.server.alternate-order": "false",
            "bc.server.save-file": replay,
        }
        return [java] + JAVA_OPTS + [f"-D{k}={v}" for k, v in props.items()] + [
            "-cp", self.classpath, "battlecode.server.Main", "-c=-"]

    def close(self) -> None:
        if self.snapshot_dir is not None:
            shutil.rmtree(self.snapshot_dir, ignore_errors=True)


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
    map_name: str = ""
    seed: int | None = None
    team_a: str = ""
    team_b: str = ""
    round: int | None = None
    duration_s: float = 0.0

    @property
    def win_condition_short(self) -> str:
        return WIN_CONDITIONS.get(self.win_condition, self.win_condition)

@dataclass
class PlayerStatistics:
    wins: int = 0
    win_maps: int = 0
    draw_maps: int = 0
    lose_maps: int = 0
    wins_by_condition: Counter = field(default_factory=lambda: Counter())


@dataclass
class ErrorLog:
    timestamp: str
    context: str
    lines: list[str]


@dataclass
class State:
    player1: str
    player2: str
    maps: list[MapEntry]
    matches: list[Match]
    console: Console
    timestamp: str
    results_path: Path | None = None
    snapshot_console: Console | None = None
    live: Live | None = None
    in_progress: set[tuple[str, bool]] = field(default_factory=set)
    errors: list[ErrorLog] = field(default_factory=list)
    failures: list[dict] = field(default_factory=list)
    meta: dict = field(default_factory=dict)
    abort: bool = False
    lock: Lock = field(default_factory=Lock, repr=False)

    def add_error(self, context: str, output: str) -> None:
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        lines = tail_lines(output, ERROR_LOG_LINES)
        self.errors.append(ErrorLog(timestamp, context, lines))
        self.failures.append({"time": timestamp, "context": context, "tail": lines})
        if len(self.errors) > ERROR_LOG_LIMIT:
            self.errors = self.errors[-ERROR_LOG_LIMIT:]

    def render_errors(self) -> Text | None:
        if not self.errors:
            return None
        lines = ["", "Errors (most recent first):"]
        for entry in reversed(self.errors[-ERROR_LOG_LIMIT:]):
            lines.append(f"{entry.timestamp} | {entry.context}")
            for line in entry.lines:
                lines.append(f"  {line}")
        return Text("\n".join(lines), style="red")

    def write_snapshot(self, renderable) -> None:
        if self.results_path is None:
            return
        if self.snapshot_console is None:
            width = getattr(self.console, "width", None) or 120
            self.snapshot_console = Console(
                width=width,
                record=True,
                color_system=None,
                force_terminal=False,
                file=io.StringIO(),
            )
        self.snapshot_console.print(renderable)
        text = self.snapshot_console.export_text(clear=True)
        self.results_path.parent.mkdir(parents=True, exist_ok=True)
        self.results_path.write_text(text, encoding="utf-8", newline="\n")
        self.write_json()

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

        renderable = Segments([*maps_segments[:-2], Text(separator, style="bold"), *stats_segments[2:]])
        legend = Text(
            f"Columns: 1 = {self.player1} is team A, 2 = {self.player1} is team B. "
            f"+COND = {self.player1} won, -COND = {self.player1} lost.",
            style="dim",
        )
        renderable = Group(renderable, legend)
        errors_text = self.render_errors()
        if errors_text is not None:
            renderable = Group(renderable, errors_text)
        if self.live is None:
            self.console.print(renderable)
        else:
            self.live.update(renderable, refresh=True)
        self.write_snapshot(renderable)

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
                    elif match.player1_wins:
                        slots[1 if reverse else 0] = f"[green]+{match.win_condition_short}[/]"
                        wins += 1
                    else:
                        slots[1 if reverse else 0] = f"[red]-{match.win_condition_short}[/]"
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

    def compute_stats(self) -> tuple[PlayerStatistics, PlayerStatistics]:
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

                if match.player1_wins:
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
        return player1_stats, player2_stats

    def get_stats_table(self) -> Table:
        table = Table()

        table.add_column("Player", style="bold", overflow="fold")
        table.add_column("Results", overflow="fold")
        table.add_column("Win rate", overflow="fold")
        table.add_column("p-value", overflow="fold")
        table.add_column("Maps", overflow="fold")

        player1_stats, player2_stats = self.compute_stats()

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

    def summary(self) -> dict:
        p1, _ = self.compute_stats()
        games = len(self.matches)
        lo, hi = wilson_interval(p1.wins, games)
        as_a = [m for m in self.matches if not m.reverse]
        as_b = [m for m in self.matches if m.reverse]
        durations = [m.duration_s for m in self.matches]
        return {
            "games": games,
            "player1_wins": p1.wins,
            "player1_losses": games - p1.wins,
            "win_rate": p1.wins / games if games else None,
            "win_rate_ci95": [lo, hi],
            "p_value_binomial_two_sided": binom_p_value(p1.wins, games),
            "pairs_won": p1.win_maps,
            "pairs_split": p1.draw_maps,
            "pairs_lost": p1.lose_maps,
            "p_value_pair_sign_test": binom_p_value(p1.win_maps, p1.win_maps + p1.lose_maps),
            "player1_wins_as_team_a": sum(m.player1_wins for m in as_a),
            "games_as_team_a": len(as_a),
            "player1_wins_as_team_b": sum(m.player1_wins for m in as_b),
            "games_as_team_b": len(as_b),
            "team_a_wins": sum(1 for m in self.matches if m.player1_wins != m.reverse),
            "player1_wins_by_condition": dict(p1.wins_by_condition),
            "player1_losses_by_condition": dict(Counter(m.win_condition_short for m in self.matches if not m.player1_wins)),
            "failures": len(self.failures),
            "mean_match_seconds": sum(durations) / len(durations) if durations else None,
            "max_match_seconds": max(durations) if durations else None,
        }

    def write_json(self) -> None:
        if self.results_path is None:
            return
        payload = {
            "player1": self.player1,
            "player2": self.player2,
            "timestamp": self.timestamp,
            **self.meta,
            "summary": self.summary(),
            "matches": [
                {**asdict(m), "winner": self.player1 if m.player1_wins else self.player2,
                 "condition": m.win_condition_short}
                for m in sorted(self.matches, key=lambda m: (m.map_label.lower(), m.reverse))
            ],
            "failures": self.failures,
        }
        self.results_path.with_suffix(".json").write_text(json.dumps(payload, indent=1), encoding="utf-8")

    def get_viewer_url(self, replay_name: str) -> str:
        return f"http://localhost:8000/client/resources/app/dist/index.html?gameSource=http://localhost:8000/{replay_name}"

def run_match(
    state: State,
    runner,
    map_entry: MapEntry,
    reverse: bool,
    seed: int | None,
    timeout: float | None,
) -> None:
    if state.abort:
        return
    player1 = state.player1 if not reverse else state.player2
    player2 = state.player2 if not reverse else state.player1
    seed_label = "map" if seed is None else seed

    replay_name = (
        f"matches/run-{state.timestamp}-{player1}-vs-{player2}-on-"
        f"{map_entry.name}-rep{map_entry.rep}-seed{seed_label}.bc26"
    )

    with state.lock:
        state.in_progress.add((map_entry.label, reverse))
        state.print()

    stdout = ""
    process = None
    start = time.time()
    for attempt in range(COMPILE_RETRY_ATTEMPTS + 1):
        try:
            process = subprocess.run(
                runner.command(player1, player2, map_entry.name, seed, replay_name),
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                cwd=str(ROOT),
                timeout=timeout,
            )
        except subprocess.TimeoutExpired:
            timeout_msg = (
                f"Timeout after {timeout}s: {player1} vs {player2} "
                f"on {map_entry.label} seed {seed_label}"
            )
            with state.lock:
                state.in_progress.discard((map_entry.label, reverse))
                state.add_error("timeout", timeout_msg)
                state.print()
            return

        stdout = process.stdout.decode("utf-8", errors="replace")
        if process.returncode == 0:
            break

        if runner.name == "gradle" and is_compile_error(stdout) and attempt < COMPILE_RETRY_ATTEMPTS:
            time.sleep(COMPILE_RETRY_DELAY)
            continue
        break

    result = parse_result(stdout) if process is not None else None
    if process is None or process.returncode != 0 or result is None:
        compile_error = is_compile_error(stdout)
        error_kind = "compile error" if compile_error else "run error"
        rc = process.returncode if process is not None else None
        context = f"{error_kind} (rc {rc}): {player1} vs {player2} on {map_entry.label} seed {seed_label}"
        with state.lock:
            state.in_progress.discard((map_entry.label, reverse))
            state.add_error(context, stdout or "No output.")
            if compile_error:
                state.abort = True
            state.print()
        return

    with state.lock:
        state.in_progress.discard((map_entry.label, reverse))
        state.matches.append(Match(
            map_entry.label,
            reverse,
            result["a_wins"] != reverse,
            result["condition"],
            replay_name,
            map_name=map_entry.name,
            seed=result["engine_seed"] if result["engine_seed"] is not None else seed,
            team_a=player1,
            team_b=player2,
            round=result["round"],
            duration_s=round(time.time() - start, 1),
        ))
        state.print()


def print_summary(state: State, elapsed: float, workers: int) -> None:
    s = state.summary()
    p1, p2 = state.player1, state.player2
    games = s["games"]
    lines = [f"== A/B summary: {p1} vs {p2} | {state.meta.get('pool_desc', '')} =="]
    if games:
        lo, hi = s["win_rate_ci95"]
        lines.append(
            f"{p1}: {s['player1_wins']}/{games} games = {100 * s['win_rate']:.1f}% "
            f"(95% CI {100 * lo:.1f}-{100 * hi:.1f}%), two-sided binomial p = {format_p_value(s['p_value_binomial_two_sided'])}"
        )
        lines.append(
            f"pairs (same map+seed, both sides): won {s['pairs_won']} / split {s['pairs_split']} / lost {s['pairs_lost']}, "
            f"sign-test p = {format_p_value(s['p_value_pair_sign_test'])}"
        )
        lines.append(
            f"{p1} as team A: {s['player1_wins_as_team_a']}/{s['games_as_team_a']}, "
            f"as team B: {s['player1_wins_as_team_b']}/{s['games_as_team_b']} | team A won {s['team_a_wins']}/{games}"
        )
        wins_by = ", ".join(f"{k} {v}" for k, v in sorted(s["player1_wins_by_condition"].items())) or "-"
        losses_by = ", ".join(f"{k} {v}" for k, v in sorted(s["player1_losses_by_condition"].items())) or "-"
        lines.append(f"{p1} wins by: {wins_by} | losses by: {losses_by}")
        lines.append(
            f"wall {elapsed:.0f}s with {workers} workers = {elapsed / games:.1f}s/game; "
            f"match mean {s['mean_match_seconds']:.1f}s, max {s['max_match_seconds']:.1f}s"
        )
    lines.append(f"failures: {s['failures']}" + (" (see Errors above / JSON)" if s["failures"] else ""))
    if state.results_path is not None:
        lines.append(f"results: {state.results_path} and {state.results_path.with_suffix('.json')}")
    text = "\n".join(lines)
    print("\n" + text)
    if state.results_path is not None:
        with state.results_path.open("a", encoding="utf-8") as handle:
            handle.write("\n" + text + "\n")


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
        help="Comma/space-separated map list (overrides --pool and --maps-dir).",
    )
    parser.add_argument(
        "--pool",
        choices=sorted(MAP_POOLS),
        default=None,
        help="Named map pool: official (43 engine maps), sprint1 (21, == test_maps/), quick (12), custom (25 team-made, holdout).",
    )
    parser.add_argument(
        "--maps-dir",
        default=default_maps_dir or str(DEFAULT_MAPS_DIR),
        help="Directory of .map26 files to list when neither --maps nor --pool is given (SHIT_TESTS skipped).",
    )
    parser.add_argument(
        "--reps",
        type=int,
        default=default_reps,
        help="Repeat each map this many times with different seeds (each repeat plays both sides).",
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
        "--seed-base",
        type=int,
        default=None,
        help="Seed for the generator of per-entry random seeds (reproducible runs). Random if omitted.",
    )
    parser.add_argument(
        "--engine",
        choices=["override", "official"],
        default="override",
        help="override: src/battlecode overrides active, --seed honoured. "
             "official: unmodified engine jar, seed = the map file's embedded seed (1 deterministic game per side).",
    )
    parser.add_argument(
        "--runner",
        choices=["java", "gradle"],
        default="java",
        help="java: direct JVM per match on a snapshot of build/classes (default). gradle: ./gradlew runWithoutBuild.",
    )
    parser.add_argument(
        "--show-indicators",
        action="store_true",
        help="Write indicator dots/lines/strings into replays (java runner; gradle runner uses gradle.properties).",
    )
    parser.add_argument(
        "--no-build",
        action="store_true",
        default=default_no_build,
        help="Skip compiling (java runner: still snapshots the current build/classes).",
    )

    args = parser.parse_args()

    if not args.player1 or not args.player2:
        raise SystemExit("Provide player1/player2 args or set AB_PLAYER1/AB_PLAYER2 in .env.")

    validate_team_exists(args.player1)
    validate_team_exists(args.player2)

    if args.maps:
        base_maps = parse_maps(args.maps)
        pool_desc = f"{len(base_maps)} maps (--maps)"
    elif args.pool:
        base_maps = list(MAP_POOLS[args.pool])
        pool_desc = f"pool '{args.pool}' ({len(base_maps)} maps)"
    else:
        base_maps = filter_shit_tests(list_local_maps(Path(args.maps_dir)))
        pool_desc = f"{len(base_maps)} maps from {args.maps_dir}"
    if not base_maps:
        raise SystemExit("No maps selected.")
    validate_maps_exist(base_maps)
    timeout = args.timeout if args.timeout and args.timeout > 0 else None
    if args.reps < 1:
        raise SystemExit("--reps must be >= 1")
    if args.engine == "official":
        if args.runner != "java":
            raise SystemExit("--engine official requires --runner java")
        if args.reps != 1 or args.seed is not None or args.seed_base is not None:
            raise SystemExit("--engine official uses each map's embedded seed: use --reps 1 and no --seed/--seed-base")
    map_entries = []
    for map_name in base_maps:
        for rep in range(1, args.reps + 1):
            label = f"{map_name} #{rep}" if args.reps > 1 else map_name
            map_entries.append(MapEntry(map_name, label, rep))
    os.makedirs(ROOT / "matches", exist_ok=True)

    console = Console(highlight=False, width=None if sys.stdout.isatty() else 160)
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")

    seed_base = None
    if args.engine == "official":
        seed_by_label = {entry.label: None for entry in map_entries}
    elif args.seed is not None:
        seed_by_label = {entry.label: args.seed for entry in map_entries}
    else:
        seed_base = args.seed_base if args.seed_base is not None else random.randrange(1 << 31)
        rng = random.Random(seed_base)
        seed_by_label = {entry.label: rng.randint(0, 10**9) for entry in map_entries}

    results_path = RESULTS_DIR / f"abtest_{timestamp}.txt"
    state = State(args.player1, args.player2, map_entries, [], console, timestamp, results_path=results_path)
    state.meta = {
        "runner": args.runner,
        "engine": args.engine,
        "engine_version": (ROOT / "engine_version.txt").read_text().strip(),
        "pool_desc": pool_desc,
        "maps": base_maps,
        "reps": args.reps,
        "fixed_seed": args.seed,
        "seed_base": seed_base,
        "workers": args.workers,
    }
    if args.runner == "java":
        runner = JavaRunner(args.engine, args.show_indicators)
    else:
        runner = GradleRunner()

    start = time.time()
    try:
        with Live("", console=console, refresh_per_second=4) as live:
            state.live = live
            state.print()

            if args.runner == "java":
                error_output = runner.prepare(build=not args.no_build, teams=[args.player1, args.player2])
                if error_output is not None:
                    error_kind = "compile error" if is_compile_error(error_output) else "build error"
                    with state.lock:
                        state.add_error(f"{error_kind}: gradle classes / classpath", error_output)
                        state.print()
                    sys.exit(1)
            elif not args.no_build:
                build_proc = subprocess.run(
                    gradle_cmd() + ["build"],
                    stdout=subprocess.PIPE,
                    stderr=subprocess.STDOUT,
                    cwd=str(ROOT),
                    text=True,
                )
                if build_proc.returncode != 0:
                    output = build_proc.stdout or ""
                    error_kind = "compile error" if is_compile_error(output) else "build error"
                    with state.lock:
                        state.add_error(f"{error_kind}: gradle build", output)
                        state.print()
                    sys.exit(build_proc.returncode)

            start = time.time()
            matches = []
            for entry in map_entries:
                seed = seed_by_label[entry.label]
                for reverse in [False, True]:
                    matches.append((state, runner, entry, reverse, seed, timeout))

            with ThreadPool(args.workers) as pool:
                pool.starmap(run_match, matches)
    finally:
        runner.close()

    print_summary(state, time.time() - start, args.workers)
    if state.failures:
        sys.exit(2)

if __name__ == "__main__":
    main()
