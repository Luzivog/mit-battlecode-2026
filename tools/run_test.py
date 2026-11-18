"""tools/run and tools/compute end to end, as an agent calls them, on the stand-in remote (BC26_LOCAL_REMOTE: every
login's command runs here with bash, and the `local` backend runs job.sh here). Everything but Slurm and Condor
themselves runs for real: the bundle, the run folder, pieces claiming tasks, the follower fetching and gathering,
wait, cancel."""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

import pytest

HERE = Path(__file__).parent
BOUND_S = 120


def git(repo: Path, *args: str) -> str:
    return subprocess.run(["git", "-C", str(repo), "-c", "user.name=t", "-c", "user.email=t@t", *args],
                          capture_output=True, check=True, text=True).stdout.strip()


@pytest.fixture
def world(tmp_path: Path) -> tuple[Path, dict[str, str]]:
    """A checkout holding these tools (committed on a branch off main), and the environment that points them at a
    stand-in remote with a stand-in runtime."""
    repo = tmp_path / "repo"
    (repo / "tools").mkdir(parents=True)
    for f in HERE.iterdir():
        if f.suffix in (".py", ".sh") or f.name in ("compute", "run"):
            shutil.copy(f, repo / "tools" / f.name)
    (repo / ".gitignore").write_text("runs/\nbuilt/\n__pycache__/\n")
    git(repo, "init", "-q", "-b", "main")
    git(repo, "add", "-A")
    git(repo, "commit", "-qm", "tools")
    git(repo, "checkout", "-qb", "work")
    remote = tmp_path / "remote"
    for d, exe in (("jdk-21.0.13+9", "java"), ("cpython-3.14.8-linux-x86_64-gnu", "python3")):
        (remote / "runtime" / d / "bin").mkdir(parents=True)
        target = sys.executable if exe == "python3" else (shutil.which("java") or "/bin/false")
        (remote / "runtime" / d / "bin" / exe).symlink_to(target)
    env = dict(os.environ, BC26_LOCAL_REMOTE=str(remote), BC26_POLL_S="0.3")
    return repo, env


def tool(repo: Path, env: dict[str, str], *argv: str, cwd: Path | None = None) -> subprocess.CompletedProcess[str]:
    return subprocess.run([str(repo / "tools" / argv[0]), *argv[1:]], cwd=cwd or repo, env=env, capture_output=True,
                          text=True, timeout=BOUND_S)


def test_each_spreads_tasks_and_gathers(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    (repo / "lines.txt").write_text("".join(f"{i}\n" for i in range(7)))
    (repo / "uncommitted.txt").write_text("the working tree goes\n")
    p = tool(repo, env, "run", "--where", "local", "--each", "lines.txt", "--gather", "r.jsonl", "--json", "--",
             "bash", "-c", 'echo "{\\"n\\": $1, \\"tree\\": \\"$(cat uncommitted.txt)\\"}" > $OUT/r.jsonl', "_", "{}")
    assert p.returncode == 0, p.stderr
    rid = p.stdout.splitlines()[0]
    summary = json.loads(p.stdout[p.stdout.index("{"):])
    assert summary["ok"] == 7 and summary["exit"] == 0
    rows = [json.loads(x) for x in (repo / "runs" / rid / "r.jsonl").read_text().splitlines()]
    assert [r["n"] for r in rows] == list(range(7))  # gathered in task order
    assert rows[0]["tree"] == "the working tree goes"
    assert (repo / "runs" / rid / "6" / "result.json").exists()


def test_chunks_go_as_a_file(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    (repo / "games.jsonl").write_text("".join(json.dumps({"game": i}) + "\n" for i in range(5)))
    p = tool(repo, env, "run", "--where", "local", "--each", "games.jsonl", "--chunk", "2", "--gather", "results.jsonl",
             "--", "bash", "-c", 'cp "$1" "$2/results.jsonl"', "_", "{file}", "{out}")
    assert p.returncode == 0, p.stderr
    rid = p.stdout.splitlines()[0]
    assert (repo / "runs" / rid / "results.jsonl").read_text() == (repo / "games.jsonl").read_text()
    assert len([d for d in (repo / "runs" / rid).iterdir() if d.name.isdigit()]) == 3


def test_a_failed_task_fails_the_run(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    (repo / "lines.txt").write_text("0\n3\n0\n")
    p = tool(repo, env, "run", "--where", "local", "--each", "lines.txt", "--", "bash", "-c", 'echo out $1; exit $1',
             "_", "{}")
    assert p.returncode == 3
    assert "task 1 ended 3" in p.stderr and "out 3" in p.stderr and "2 of 3 tasks ended 0" in p.stderr


def test_one_command_prints_its_output_and_exit(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    (repo / "built").mkdir()
    (repo / "built" / "x").write_text("a build output\n")
    p = tool(repo, env, "run", "--where", "local", "--with", "built", "--", "bash", "-c",
             'cat built/x; echo "cpus $JOB_CPUS"; exit 5')
    assert p.returncode == 5, p.stderr
    assert "a build output\ncpus 2\n" in p.stdout  # a whole task: JOB_CPUS is its machine's (2 here: taskset)


def test_compute_run_then_wait(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    (repo / "dirty").write_text("x")
    p = tool(repo, env, "compute", "run", "--where", "local", "--", "true")
    assert p.returncode == 2 and "uncommitted or untracked" in p.stderr  # a job runs HEAD
    (repo / "dirty").unlink()
    p = tool(repo, env, "compute", "run", "--where", "local", "--", "bash", "-c", 'echo hello > $OUT/h; exit 4')
    assert p.returncode == 0, p.stderr
    jid = p.stdout.strip()
    p = tool(repo, env, "compute", "wait", jid, "--json", "--poll", "0.3")
    assert p.returncode == 4, p.stderr
    report = json.loads(p.stdout[p.stdout.index("["):])
    assert report[0]["exit"] == 4 and report[0]["phase"] == "ended"
    assert (repo / "runs" / jid / "out" / "h").read_text() == "hello\n"
    assert "== " in (repo / "runs" / jid / "log").read_text()


def test_cancel_ends_a_run(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    (repo / "lines.txt").write_text("a\nb\n")
    p = tool(repo, env, "run", "--where", "local", "--detach", "--each", "lines.txt", "--", "sleep", "60")
    assert p.returncode == 0, p.stderr
    rid = p.stdout.strip()
    remote = Path(env["BC26_LOCAL_REMOTE"])
    import time
    end = time.monotonic() + BOUND_S
    while len(list((remote / "runs" / rid / "claims").iterdir())) < 2:
        assert time.monotonic() < end
        time.sleep(0.1)
    p = tool(repo, env, "compute", "cancel", rid)
    assert p.returncode == 0 and "closed" in p.stdout, p.stderr
    p = tool(repo, env, "compute", "wait", rid, "--poll", "0.3")
    assert p.returncode == 1  # its tasks never ended
    assert (remote / "runs" / rid / "closed").exists()


def test_refusals(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    assert tool(repo, env, "compute", "logs", "condor-1118-120000-zzzz").returncode == 2
    p = tool(repo, env, "run", "--where", "nowhere", "--", "true")
    assert p.returncode == 2 and "--where" in p.stderr
    p = tool(repo, env, "run", "--each", "/dev/null", "--chunk", "2", "--", "echo", "{}")
    assert p.returncode == 2
