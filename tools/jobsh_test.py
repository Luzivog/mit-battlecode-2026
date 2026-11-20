"""tools/job.sh, run for real here on job folders made as tools/jobs.py makes them (its interface): the checkout, the
environment a command gets, exit codes, the bounded log, signals."""
from __future__ import annotations

import io
import os
import signal
import subprocess
import tarfile
import time
from pathlib import Path

import pytest

HERE = Path(__file__).parent
JOB_SH = HERE / "job.sh"
BOUND_S = 60  # a job.sh run's bound: far above the second each takes, so a hang fails instead of holding the run


def git(repo: Path, *args: str) -> str:
    return subprocess.run(["git", "-C", str(repo), "-c", "user.name=t", "-c", "user.email=t@t", *args],
                          capture_output=True, check=True, text=True).stdout.strip()


@pytest.fixture
def repo(tmp_path: Path) -> tuple[Path, str]:
    r = tmp_path / "repo"
    r.mkdir()
    git(r, "init", "-q", "-b", "main")
    (r / "f").write_text("on main\n")
    git(r, "add", "f")
    git(r, "commit", "-qm", "main")
    git(r, "checkout", "-qb", "work")
    (r / "f").write_text("on work\n")
    git(r, "commit", "-qam", "work")
    return r, git(r, "rev-parse", "HEAD")


def runtime(root: Path) -> Path:
    """A stand-in runtime folder: bin/ with java and python3."""
    rt = root / "runtime"
    (rt / "bin").mkdir(parents=True, exist_ok=True)
    return rt


def make_job(tmp: Path, repo: tuple[Path, str], argv: list[str], env: str | None = None, kit: str | None = None,
             with_files: dict[str, bytes] | None = None, job_sh: Path = JOB_SH) -> Path:
    r, commit = repo
    d = tmp / "jobs" / "local-1118-120000-abcd"
    d.mkdir(parents=True)
    (d / "job.sh").write_bytes(job_sh.read_bytes())
    (d / "commit").write_text(commit + "\n")
    (d / "code.bundle").write_bytes(subprocess.run(["git", "-C", str(r), "bundle", "create", "-", "HEAD", "main"],
                                                   capture_output=True, check=True).stdout)
    (d / "argv").write_bytes(b"".join(a.encode() + b"\0" for a in argv))
    (d / "env").write_text(env if env is not None else f"PATH {runtime(tmp)}/bin\nJAVA_HOME {runtime(tmp)}\n")
    if kit is not None:
        (d / "kit").write_text(kit)
    if with_files:
        buf = io.BytesIO()
        with tarfile.open(fileobj=buf, mode="w:gz") as tar:
            for name, data in with_files.items():
                info = tarfile.TarInfo(name)
                info.size = len(data)
                tar.addfile(info, io.BytesIO(data))
        (d / "with.tgz").write_bytes(buf.getvalue())
    (d / "exit").write_text("")
    (d / "log").write_text("")
    return d


def clean_env() -> dict[str, str]:
    return {k: v for k, v in os.environ.items() if not k.startswith("SLURM_")}


def run_job(d: Path, backend: str = "local") -> str:
    subprocess.run(["bash", str(d / "job.sh"), str(d), backend], env=clean_env(), timeout=BOUND_S, check=True)
    return (d / "exit").read_text().strip()


def test_runs_the_commit_with_its_environment(tmp_path: Path, repo: tuple[Path, str]) -> None:
    d = make_job(tmp_path, repo, ["bash", "-c", 'cat f > "$OUT/f"; echo "cpus=$JOB_CPUS mem=$JOB_MEM_MB '
                                  'java=$JAVA_HOME out=$JOB_OUT"; git log --oneline main -1; echo "path=$PATH"'])
    assert run_job(d) == "0"
    assert (d / "out" / "f").read_text() == "on work\n"
    log = (d / "log").read_text()
    assert " start on " in log and "exit 0" in log
    assert f"java={runtime(tmp_path)}" in log and f"out={d}/out" in log
    assert " main" in log  # main came in the bundle, as main
    assert f"path={runtime(tmp_path)}/bin:" in log  # env's PATH goes first
    assert not (d / "code.bundle").exists()  # an ended job's inputs go


def test_exit_code_is_the_commands(tmp_path: Path, repo: tuple[Path, str]) -> None:
    assert run_job(make_job(tmp_path, repo, ["bash", "-c", "exit 7"])) == "7"


def test_with_files_and_kit(tmp_path: Path, repo: tuple[Path, str]) -> None:
    jars = [tmp_path / "a.jar", tmp_path / "b.jar"]
    for j in jars:
        j.write_text("jar")
    d = make_job(tmp_path, repo, ["bash", "-c", 'cat build/classes/x; echo "cp=$BC_CLASSPATH"; echo "jars=$BC_ENGINE_JARS"'],
                 kit="".join(f"{j}\n" for j in jars), with_files={"build/classes/x": b"shipped\n"})
    assert run_job(d) == "0"
    log = (d / "log").read_text()
    assert "shipped" in log
    assert f"jars={jars[0]}:{jars[1]}\n" in log
    assert f"/code/build/classes:{jars[0]}:{jars[1]}\n" in log


@pytest.mark.parametrize("env,kit", [("PATH /nonexistent/bin\n", None), (None, "/nonexistent.jar\n")])
def test_a_missing_runtime_fails_the_setup(tmp_path: Path, repo: tuple[Path, str], env: str | None,
                                          kit: str | None) -> None:
    d = make_job(tmp_path, repo, ["true"], env=env, kit=kit)
    assert run_job(d) == "255"
    assert "setup failed" in (d / "log").read_text()


def test_an_unknown_commit_fails_the_setup(tmp_path: Path, repo: tuple[Path, str]) -> None:
    d = make_job(tmp_path, repo, ["true"])
    (d / "commit").write_text("0" * 40 + "\n")
    assert run_job(d) == "255"


def test_a_file_left_in_home_is_named(tmp_path: Path, repo: tuple[Path, str]) -> None:
    d = make_job(tmp_path, repo, ["bash", "-c", 'echo x > "$HOME/result"; mkdir -p "$HOME/.cache"; echo y > "$HOME/.cache/z"'])
    assert run_job(d) == "253"
    assert "/result" in (d / "log").read_text() and "/.cache/z" not in (d / "log").read_text()


def test_the_log_is_bounded(tmp_path: Path, repo: tuple[Path, str]) -> None:
    small = tmp_path / "job_small.sh"
    small.write_text(JOB_SH.read_text().replace("LOG_CAP=$((64 << 20))", "LOG_CAP=1000"))
    d = make_job(tmp_path, repo, ["bash", "-c", "head -c 5000 /dev/zero | tr '\\0' x; echo"], job_sh=small)
    assert run_job(d) == "0"
    lines = (d / "log").read_text().splitlines()
    assert lines[1] == "x" * 1000
    assert lines[2] == "== log cut at 1000 bytes: 4001 more dropped"


def wait_started(d: Path) -> None:
    end = time.monotonic() + BOUND_S
    while " start on " not in (d / "log").read_text():
        assert time.monotonic() < end, "job.sh never started its command"
        time.sleep(0.05)
    time.sleep(0.3)


@pytest.mark.parametrize("backend,want", [("local", "143")] * 6 + [("condor", "")])
def test_sigterm_ends_it(tmp_path: Path, repo: tuple[Path, str], backend: str, want: str) -> None:
    """A cancel's SIGTERM reaches the command; on Condor (an eviction) no exit file, since Condor runs it again. Six
    times: a second wait for a command already waited for answered 127, which went into `exit` one time in three."""
    d = make_job(tmp_path, repo, ["sleep", "30"])
    p = subprocess.Popen(["bash", str(d / "job.sh"), str(d), backend], env=clean_env())
    wait_started(d)
    p.send_signal(signal.SIGTERM)
    assert p.wait(BOUND_S) == 0
    assert (d / "exit").read_text().strip() == want
    assert "exit 143 after SIGTERM" in (d / "log").read_text()
