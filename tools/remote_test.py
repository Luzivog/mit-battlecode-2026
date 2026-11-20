"""tools/remote.py's bounds: every call ends within its time on both sides, and the processes of this machine share
SESSIONS sessions a kept connection, waiting their turn for one, never opening a connection of their own."""
from __future__ import annotations

import subprocess
import sys
import threading
import time
from pathlib import Path

import pytest

from tools import remote


@pytest.fixture
def local(tmp_path: Path, monkeypatch: pytest.MonkeyPatch) -> Path:
    """The stand-in remote: commands run here with bash; the session slots in a folder of the test's."""
    root = tmp_path / "remote"
    root.mkdir()
    monkeypatch.setattr(remote, "LOCAL", str(root))
    monkeypatch.setattr(remote, "CLIENT", tmp_path / "client")
    return root


def test_a_call_ends_within_its_bound(local: Path) -> None:
    t0 = time.monotonic()
    with pytest.raises(remote.Unreachable, match="bound ended it after 1 s"):
        remote.login("sleep 30", timeout=1)
    assert time.monotonic() - t0 < 5
    assert remote.login("echo ok") == "ok\n"


def test_a_transfer_ends_within_its_bound(local: Path, tmp_path: Path) -> None:
    t0 = time.monotonic()
    with pytest.raises(remote.Unreachable, match="ran out"):
        remote.login_into("sleep 30", tmp_path / "into", timeout=1)
    assert time.monotonic() - t0 < 5


def test_the_remote_side_is_bounded_too() -> None:
    """The command's exit code and stdin pass through; past its bound it and its children are gone, and stderr says
    so."""
    def sh(script: str, t: float, stdin: bytes = b"") -> subprocess.CompletedProcess[bytes]:
        return subprocess.run(["bash", "-c", remote.bounded(script, t)], input=stdin, capture_output=True, timeout=10)
    p = sh('read x; echo "$x $((1 + 1))"; exit 3', 5, b"in\n")
    assert (p.returncode, p.stdout) == (3, b"in 2\n") and not remote.killed(p.returncode, p.stderr)
    t0 = time.monotonic()
    p = sh("sleep 30 & echo started; sleep 30", 1)  # a child holding the output pipe goes too
    assert remote.killed(p.returncode, p.stderr) and p.stdout == b"started\n" and time.monotonic() - t0 < 4
    assert sh("echo 'it''s'", 5).stdout == b"its\n"


def test_sessions_never_exceed_the_kept_connections(local: Path) -> None:
    """40 calls at once from threads (each a session of its own lock file, as separate processes are): at most
    SESSIONS run at once, and every one of them runs, waiting its turn."""
    (local / "on").mkdir()
    cmd = (f"d=$(mktemp -d {local}/on/x.XXXXXX); ls {local}/on | wc -l >> {local}/seen; sleep 0.3; rmdir \"$d\"")
    errors: list[BaseException] = []

    def one() -> None:
        try:
            remote.login(cmd)
        except BaseException as e:  # noqa: BLE001
            errors.append(e)
    threads = [threading.Thread(target=one) for _ in range(40)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    seen = [int(x) for x in (local / "seen").read_text().split()]
    assert not errors and len(seen) == 40
    assert max(seen) <= remote.SESSIONS and max(seen) >= 2


def test_bulk_transfers_leave_sessions_for_reads(local: Path, tmp_path: Path) -> None:
    held = threading.Event()
    done: list[float] = []

    def bulk() -> None:
        remote.login_into("sleep 2; tar -cf - --files-from /dev/null", tmp_path / "into")
    threads = [threading.Thread(target=bulk) for _ in range(6)]
    for t in threads:
        t.start()
    time.sleep(0.5)
    held.set()
    t0 = time.monotonic()
    remote.login("true")  # a read doesn't wait behind the transfers
    done.append(time.monotonic() - t0)
    for t in threads:
        t.join()
    assert done[0] < 1.0


def test_another_process_holding_every_session_makes_a_call_wait(local: Path, tmp_path: Path) -> None:
    """Slots are lock files: another process holding them all makes this one wait, then fail Unreachable when its
    wait runs out (never a session beyond SESSIONS)."""
    hold = (f"import fcntl, os, sys, time\nfds = []\nfor i in range({remote.SESSIONS}):\n"
            f"    fd = os.open('{tmp_path / 'client' / 'ssh'}/gpucluster2.%d' % i, os.O_RDWR | os.O_CREAT)\n"
            "    fcntl.flock(fd, fcntl.LOCK_EX); fds.append(fd)\nprint('held', flush=True)\ntime.sleep(3)\n")
    (tmp_path / "client" / "ssh").mkdir(parents=True)
    p = subprocess.Popen([sys.executable, "-c", hold], stdout=subprocess.PIPE, text=True)
    assert p.stdout is not None and p.stdout.readline() == "held\n"
    t0 = time.monotonic()
    assert remote.login("echo ok") == "ok\n"  # waited for the other process's slots
    assert 1.5 < time.monotonic() - t0 < 6
    p.wait()


def test_a_full_connection_is_waited_for_not_left(monkeypatch: pytest.MonkeyPatch, tmp_path: Path) -> None:
    """sshd refusing a session (its 10 already open) is asked again; the call never opens a connection of its own."""
    monkeypatch.setattr(remote, "LOCAL", "")
    monkeypatch.setattr(remote, "CLIENT", tmp_path / "client")
    monkeypatch.setattr(remote, "control_path", lambda h: f"/run/x/bc26-ssh-{h}")
    monkeypatch.setattr(remote, "master_up", lambda host: True)
    monkeypatch.setattr(remote.time, "sleep", lambda s: None)
    answers = [255, 255, 255, 0]
    calls: list[list[str]] = []

    def run(argv: list[str], **kw: object) -> subprocess.CompletedProcess[bytes]:
        calls.append(argv)
        code = answers.pop(0)
        err = b"mux_client_request_session: session request failed: Session open refused by peer" if code else b""
        return subprocess.CompletedProcess(argv, code, b"ok\n" if not code else b"", err)
    monkeypatch.setattr(remote.subprocess, "run", run)
    assert remote.login("true") == "ok\n" and len(calls) == 4
    assert all("ProxyCommand=/bin/false" in c and "ControlMaster=no" in c for c in calls)


def test_a_gone_connection_is_made_again_once(monkeypatch: pytest.MonkeyPatch, tmp_path: Path) -> None:
    monkeypatch.setattr(remote, "LOCAL", "")
    monkeypatch.setattr(remote, "CLIENT", tmp_path / "client")
    monkeypatch.setattr(remote, "control_path", lambda h: "/run/x/bc26-ssh-one")
    up = [False]
    made: list[str] = []
    monkeypatch.setattr(remote, "master_up", lambda host: up[0])
    monkeypatch.setattr(remote, "start_master", lambda host: (made.append(host), up.__setitem__(0, True)))
    answers = [255, 0]
    monkeypatch.setattr(remote.subprocess, "run", lambda argv, **kw: subprocess.CompletedProcess(
        argv, (c := answers.pop(0)), b"" if c else b"ok\n", b"Control socket connect: No such file" if c else b""))
    assert remote.login("true") == "ok\n" and made == ["gpucluster2"]


def test_names_of_one_connection_are_tried_once(monkeypatch: pytest.MonkeyPatch, tmp_path: Path) -> None:
    """Two names of one kept connection: a call that failed on one isn't run again on the other."""
    monkeypatch.setattr(remote, "LOCAL", "")
    monkeypatch.setattr(remote, "CLIENT", tmp_path / "client")
    monkeypatch.setattr(remote, "control_path", lambda h: "/run/x/bc26-ssh-one")
    monkeypatch.setattr(remote, "master_up", lambda host: True)
    calls: list[list[str]] = []
    monkeypatch.setattr(remote.subprocess, "run", lambda argv, **kw: (
        calls.append(argv), subprocess.CompletedProcess(argv, 255, b"", b"Connection reset"))[1])
    with pytest.raises(remote.Unreachable):
        remote.login("true")
    assert len(calls) == 1
