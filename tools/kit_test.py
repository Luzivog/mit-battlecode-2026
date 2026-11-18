"""The game kit: the engine's jars as Gradle's runtime classpath has them, and a real game played with plain java on
it (tools/kitgame), as a remote job plays it."""
from __future__ import annotations

import json
import subprocess
from pathlib import Path

import pytest

from tools import kit

REPO = Path(__file__).resolve().parent.parent


def test_jars_are_the_pinned_engine_then_scala() -> None:
    engine, scala = kit.jars()
    assert engine.name == f"battlecode26-java-{(REPO / 'engine_version.txt').read_text().strip()}.jar"
    assert scala.name == f"scala-library-{kit.scala_version()}.jar"
    assert engine.is_file() and scala.is_file()


def test_classpath_puts_the_overrides_first(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.delenv("BC_CLASSPATH", raising=False)
    cp = kit.classpath().split(":")
    assert cp[0] == str(REPO / "build" / "classes") and cp[1:] == [str(j) for j in kit.jars()]
    monkeypatch.setenv("BC_CLASSPATH", "/job/build/classes:/vol/x.jar")
    assert kit.classpath() == "/job/build/classes:/vol/x.jar"


@pytest.mark.skipif(not (REPO / "build/classes/afk/RobotPlayer.class").exists(), reason="build/classes not compiled")
def test_kitgame_plays_a_deterministic_game(tmp_path: Path) -> None:
    games = tmp_path / "games.txt"
    games.write_text("afk suicide DefaultSmall 158\n")
    hashes = []
    for k in range(2):
        p = subprocess.run([str(REPO / "tools" / "kitgame"), str(games), str(tmp_path / f"o{k}")], capture_output=True,
                           text=True, timeout=120)
        assert p.returncode == 0, p.stdout + p.stderr
        r = json.loads((tmp_path / f"o{k}" / "results.jsonl").read_text())
        assert (r["winner"], r["winner_side"], r["round"]) == ("afk", "A", 1)
        hashes.append(r["replay_sha256"])
    assert hashes[0] == hashes[1]
