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

from tools.piece import POOL_V

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
             "bash", "-c", 'echo "{\\"n\\": $1, \\"tree\\": \\"$(cat uncommitted.txt)\\"}" > $OUT/r.jsonl; sleep 1', "_", "{}")
    assert p.returncode == 0, p.stderr
    rid = p.stdout.splitlines()[0]
    summary = json.loads(p.stdout[p.stdout.index("{"):])
    assert summary["ok"] == 7 and summary["exit"] == 0
    rows = [json.loads(x) for x in (repo / "runs" / rid / "r.jsonl").read_text().splitlines()]
    assert [r["n"] for r in rows] == list(range(7))  # gathered in task order
    assert rows[0]["tree"] == "the working tree goes"
    pieces = {json.loads((repo / "runs" / rid / str(n) / "result.json").read_text())["piece"] for n in range(7)}
    assert len(pieces) >= 2  # several pieces claimed from one run


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
    assert p.stdout.startswith(f"{rid}: 0 of 2 tasks ended 0, exit 1; results in ")
    assert (remote / "runs" / rid / "closed").exists()


def test_refusals(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    assert tool(repo, env, "compute", "logs", "condor-1118-120000-zzzz").returncode == 2
    p = tool(repo, env, "run", "--where", "nowhere", "--", "true")
    assert p.returncode == 2 and "--where" in p.stderr
    p = tool(repo, env, "run", "--each", "/dev/null", "--chunk", "2", "--", "echo", "{}")
    assert p.returncode == 2


def test_sweep_deletes_only_old_ended_folders(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    root = Path(env["BC26_LOCAL_REMOTE"])
    for name, age in (("old", "10 days ago"), ("new", "1 hour ago")):
        (root / "jobs" / name).mkdir(parents=True)
        (root / "jobs" / name / "exit").write_text("0\n")
        subprocess.run(["touch", "-d", age, str(root / "jobs" / name / "exit")], check=True)
    (root / "jobs" / "running").mkdir()
    (root / "jobs" / "running" / "exit").write_text("")
    subprocess.run(["touch", "-d", "10 days ago", str(root / "jobs" / "running" / "exit")], check=True)
    ages: list[tuple[str, str | None, str | None]] = [("closed", "10 days ago", None), ("open", None, "1 day ago"),
                                                      ("forgotten", None, "40 days ago")]
    for run_name, closed_age, run_age in ages:
        d = root / "runs" / f"run-1118-000000-{run_name}"
        (d / "done").mkdir(parents=True)
        if closed_age:
            (d / "closed").touch()
            subprocess.run(["touch", "-d", closed_age, str(d / "closed")], check=True)
        if run_age:
            subprocess.run(["touch", "-d", run_age, str(d), str(d / "done")], check=True)
    p = tool(repo, env, "compute", "sweep")
    assert p.returncode == 0 and p.stdout.startswith("1 jobs and 2 runs deleted"), p.stdout + p.stderr
    assert sorted(x.name for x in (root / "jobs").iterdir()) == ["new", "running"]
    assert [x.name for x in (root / "runs").iterdir()] == ["run-1118-000000-open"]


# ---- the pool ----
def wait_for(cond, what: str) -> None:  # type: ignore[no-untyped-def]
    import time
    end = time.monotonic() + BOUND_S
    while not cond():
        assert time.monotonic() < end, what
        time.sleep(0.05)


def piece_states(remote: Path) -> list[dict[str, object]]:
    d = remote / "pool" / "pieces"
    out = []
    for f in (d.iterdir() if d.exists() else []):
        if not f.name.startswith("."):
            try:
                out.append(json.loads(f.read_text()))
            except ValueError:
                pass
    return out


def test_a_new_run_takes_live_pieces_free_slots(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    remote = Path(env["BC26_LOCAL_REMOTE"])
    (repo / "one.txt").write_text("x\n")
    first = tool(repo, env, "run", "--where", "local", "--detach", "--task-mem", "100M", "--each", "one.txt", "--",
                 "sleep", "6")
    assert first.returncode == 0, first.stderr
    wait_for(lambda: any(p["running"] == 1 for p in piece_states(remote)), "the first run's piece runs its task")
    p = tool(repo, env, "run", "--where", "local", "--task-mem", "100M", "--json", "--each", "one.txt", "--", "bash",
             "-c", "echo served")
    assert p.returncode == 0, p.stderr
    assert "1 live piece has 1 free slot: no new piece" in p.stderr
    summary = json.loads(p.stdout[p.stdout.index("{"):])
    assert summary["pieces_requested"] == 0 and summary["served_by_others"] == 1 and summary["ok"] == 1
    r = json.loads((repo / "runs" / summary["id"] / "0" / "result.json").read_text())
    assert r["piece_run"] == first.stdout.strip()


def test_a_run_ships_its_content_once(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    (repo / "built").mkdir()
    (repo / "built" / "x").write_text("a build output\n")
    said = []
    for _ in range(2):
        p = tool(repo, env, "run", "--where", "local", "--with", "built", "--", "cat", "built/x")
        assert p.returncode == 0 and p.stdout.endswith("\na build output\n"), p.stderr
        said.append(p.stderr)
    assert "its code (" in said[0] and "MB)" in said[0] and "built (" in said[0]
    assert "its code (on the volume) and built (on the volume)" in said[1]
    (repo / "built" / "x").write_text("another build output\n")
    p = tool(repo, env, "run", "--where", "local", "--with", "built", "--", "cat", "built/x")
    assert p.stdout.endswith("\nanother build output\n") and "its code (on the volume)" in p.stderr
    assert "built (on the volume)" not in p.stderr
    assert len(list((Path(env["BC26_LOCAL_REMOTE"]) / "cas").iterdir())) == 3


def test_cancelling_a_pooled_run_spares_the_tasks_its_pieces_serve(world: tuple[Path, dict[str, str]]) -> None:
    repo, env = world
    remote = Path(env["BC26_LOCAL_REMOTE"])
    (repo / "one.txt").write_text("x\n")
    first = tool(repo, env, "run", "--where", "local", "--detach", "--task-mem", "100M", "--each", "one.txt", "--",
                 "sleep", "30")
    rid1 = first.stdout.strip()
    wait_for(lambda: any(p["running"] == 1 for p in piece_states(remote)), "the first run's piece runs its task")
    second = tool(repo, env, "run", "--where", "local", "--detach", "--task-mem", "100M", "--each", "one.txt", "--",
                  "bash", "-c", "sleep 3; echo done")
    rid2 = second.stdout.strip()
    assert "no new piece" in second.stderr
    wait_for(lambda: (remote / "runs" / rid2 / "claims" / "0").exists(), "the first run's piece takes the second's")
    p = tool(repo, env, "compute", "cancel", rid1)
    assert p.returncode == 0 and "1 running piece stop its tasks" in p.stdout, p.stdout + p.stderr
    p = tool(repo, env, "compute", "wait", rid2, "--json", "--poll", "0.3")
    assert p.returncode == 0, p.stderr
    assert (repo / "runs" / rid2 / "0" / "log").read_text() == "done\n"
    p = tool(repo, env, "compute", "wait", rid1, "--poll", "0.3")
    assert p.returncode == 1 and not (remote / "pool" / "open" / rid1).exists()


def test_the_follower_adds_pieces_while_tasks_wait(world: tuple[Path, dict[str, str]]) -> None:
    """Free slots counted at submission that never take the run's tasks (the piece ended): its follower adds
    pieces."""
    repo, env = world
    remote = Path(env["BC26_LOCAL_REMOTE"])
    (repo / "two.txt").write_text("a\nb\n")
    p = tool(repo, env, "run", "--where", "local", "--json", "--task-mem", "100M", "--each", "two.txt", "--", "true")
    assert p.returncode == 0, p.stderr
    runtime = json.loads((remote / "runs" / p.stdout.splitlines()[0] / "run.json").read_text())["env"]
    fake = remote / "pool" / "pieces" / "local-1118-000000-fake"
    fake.write_text(json.dumps({"v": POOL_V, "job": fake.name, "backend": "local", "slots": 4, "running": 0,
                                "task_cpus": 1, "task_mem_mb": 100, "env": runtime, "deadline": None}))
    p = tool(repo, env, "run", "--where", "local", "--detach", "--task-mem", "100M", "--each", "two.txt", "--", "true")
    assert p.returncode == 0 and "has 4 free slots: no new piece" in p.stderr, p.stderr
    assert (remote / "runs" / p.stdout.strip() / "placed").exists()  # its follower may add pieces now
    fake.unlink()  # that piece ended before taking them
    p = tool(repo, env, "compute", "wait", p.stdout.strip(), "--json", "--poll", "0.3")
    assert p.returncode == 0, p.stderr
    assert "2 tasks wait, 0 free slots on 0 live pieces: pieces added (round 1)" in p.stderr


def test_old_run_results_are_packed(tmp_path: Path) -> None:
    """runs.compact_local: a run unchanged for a day keeps its summary and gathered files; its per-task folders go
    into tasks.tgz and their result.json lines into tasks.jsonl. A recent run is left as it is; at most hourly."""
    import tarfile
    import time
    from tools import runs as R
    old, new = tmp_path / "run-1118-120000-aaaa", tmp_path / "run-1119-120000-bbbb"
    for d in (old, new):
        for n in range(3):
            (d / str(n) / "out").mkdir(parents=True)
            (d / str(n) / "result.json").write_text(json.dumps({"task": n, "code": 0}))
            (d / str(n) / "out" / "results.jsonl").write_text(f"game {n}\n")
        (d / "summary.json").write_text("{}")
        (d / "results.jsonl").write_text("gathered\n")
    t = time.time() - 2 * 86400
    for p in [old / "summary.json", *(old / str(n) for n in range(3))]:
        os.utime(p, (t, t))
    packed = R.compact_local(tmp_path)
    assert [r for r, _ in packed] == [old.name]
    assert sorted(x.name for x in old.iterdir()) == ["results.jsonl", "summary.json", "tasks.jsonl", "tasks.tgz"]
    assert [json.loads(x)["task"] for x in (old / "tasks.jsonl").read_text().splitlines()] == [0, 1, 2]
    with tarfile.open(old / "tasks.tgz") as tar:
        assert tar.extractfile("2/out/results.jsonl").read() == b"game 2\n"  # type: ignore[union-attr]
    assert (new / "0" / "result.json").exists()
    for p in [new / "summary.json", *(new / str(n) for n in range(3))]:
        os.utime(p, (t, t))
    assert R.compact_local(tmp_path) == []  # within the hour
    assert [r for r, _ in R.compact_local(tmp_path, force=True)] == [new.name]
