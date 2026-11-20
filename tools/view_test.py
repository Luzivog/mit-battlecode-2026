"""tools/view.py: however many processes here follow runs, the shared remote state is read by one remote command at a
time, at most every MAX_AGE_S (MIN_GAP_S for a newcomer), and each sees its runs and jobs in it."""
from __future__ import annotations

import os
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).parent

PROC = """
import sys, time
sys.path.insert(0, sys.argv[1])
from tools import view
rid = sys.argv[2]
end = time.monotonic() + 3
n = 0
while time.monotonic() < end:
    s = view.get(runs=[rid], jobs=["slurm-1119-000000-abcd"])
    assert s.run(rid) is not None and "slurm-1119-000000-abcd" in s.jobs
    n += 1
    time.sleep(0.05)
print(n)
"""


def test_many_followers_share_one_reader(tmp_path: Path) -> None:
    """20 processes, each following its own run and asking 20 times a second for 3 s: the remote state is read (a
    fake squeue logs each read) at most once a MIN_GAP_S..MAX_AGE_S, never at once, and each sees its run."""
    root = tmp_path / "remote"
    bin_ = tmp_path / "bin"
    bin_.mkdir()
    log = tmp_path / "reads"
    (bin_ / "squeue").write_text(f"#!/bin/bash\necho \"$(date +%s.%N) start\" >> {log}\nsleep 0.2\n"
                                 f"echo \"$(date +%s.%N) end\" >> {log}\n")
    (bin_ / "squeue").chmod(0o755)
    rids = [f"run-1119-000000-{i:04x}" for i in range(20)]
    for r in rids:
        for d in ("done", "claims"):
            (root / "runs" / r / d).mkdir(parents=True)
        (root / "runs" / r / "pieces").write_text("slurm-1119-000000-abcd\n")
    env = dict(os.environ, BC26_LOCAL_REMOTE=str(root), PATH=f"{bin_}:{os.environ['PATH']}")
    ps = [subprocess.Popen([sys.executable, "-c", PROC, str(HERE.parent), r], env=env, stdout=subprocess.PIPE,
                           stderr=subprocess.PIPE, text=True) for r in rids]
    asks = 0
    for p in ps:
        out, err = p.communicate(timeout=60)
        assert p.returncode == 0, err
        asks += int(out)
    lines = [x.split() for x in log.read_text().splitlines()]
    starts = [float(t) for t, w in lines if w == "start"]
    assert asks > 3 * len(starts)  # many asks a read (how many depends on this machine's load)
    print(f"{asks} asks, {len(starts)} reads")
    # a refresh runs squeue twice (the pool's liveness, the jobs' states): ~one refresh a MIN_GAP_S plus its own time
    # on the stand-in (0.2 s + 0.4 s), not one an ask
    assert 3 <= len(starts) <= 2 * (3 / 0.2 + 2)
    # one at a time: no read starts before the last one ended
    events = sorted((float(t), w) for t, w in lines)
    running = peak = 0
    for _, w in events:
        running += 1 if w == "start" else -1
        peak = max(peak, running)
    assert peak == 1


def test_a_dead_process_interest_is_dropped(tmp_path: Path, monkeypatch) -> None:  # type: ignore[no-untyped-def]
    from tools import remote, view
    d = tmp_path / "client-state" / "interest"
    d.mkdir(parents=True)
    (d / "1-abc.json").write_text('{"runs": ["run-1119-000000-aaaa"], "jobs": []}')
    (d / "2-abc.json").write_text('{"runs": ["run-1119-000000-bbbb"], "jobs": ["condor-1119-000000-abcd"]}')
    old = time.time() - view.INTEREST_S - 5
    os.utime(d / "1-abc.json", (old, old))
    assert view._interests() == ({"run-1119-000000-bbbb"}, {"condor-1119-000000-abcd"})
    assert not (d / "1-abc.json").exists()
    assert remote.CLIENT == tmp_path / "client-state"


def test_an_unreadable_cluster_is_shared_too(tmp_path: Path, monkeypatch) -> None:  # type: ignore[no-untyped-def]
    """A refresh that failed is the snapshot for MAX_AGE_S: every reader is Unreachable at once, none asks again."""
    import pytest

    from tools import remote, view
    calls = []

    def login(cmd, stdin=None, hosts=remote.SLURM_HOSTS, stream=0, timeout=None):  # type: ignore[no-untyped-def]
        calls.append(cmd)
        raise remote.Unreachable("on gpucluster2: no answer within 40 s")
    monkeypatch.setattr(remote, "login", login)
    for _ in range(5):
        with pytest.raises(remote.Unreachable, match="no answer"):
            view.get()
    assert len(calls) == 1


def test_a_piece_lives_while_its_scheduler_runs_it() -> None:
    """Liveness is the scheduler's word where its queue was read, the state's age elsewhere; a dead piece's file is
    named for removal at the next refresh. States are file names; a file named by its job id alone (an older piece)
    holds its state inside."""
    from tools import view
    from tools.piece import state_name
    now = 1_000_000.0

    def piece(job: str, age: float) -> str:
        return state_name(job, 4, 1, 1, 1700, "e", None, None, now - age)
    names = {j: piece(j, a) for j, a in (("condor-1119-000000-aaaa", 5000), ("condor-1119-000000-bbbb", 1),
                                         ("slurm-1119-000000-cccc", 1), ("cpucluster1-1119-000000-dddd", 30),
                                         ("cpucluster1-1119-000000-eeee", 4000))}
    gpu = {"pieces": [*names.values(), "condor-1119-000000-ffff", "condor-1119-000000-gggg"],
           "old": ['condor-1119-000000-ffff:{"v": 4, "slots": 8, "running": 2, "at": 999999}'],
           "sq": ["bc26-slurm-1119-000000-cccc PENDING"], "sq_rc": ["0"],
           "cq": ["bc26-condor-1119-000000-aaaa 2", "bc26-condor-1119-000000-bbbb 1",
                  "bc26-condor-1119-000000-ffff 2"], "cq_rc": ["0"]}
    pieces, _ = view.Snapshot(now, [], [], gpu, None).pool()
    by = {str(p["name"]).split(",")[0]: (p["live"], p["dead"]) for p in pieces}
    assert by["condor-1119-000000-aaaa"] == (True, False)  # running, however old its state
    assert by["condor-1119-000000-bbbb"] == (False, True)  # idle again (evicted): its file goes
    assert by["slurm-1119-000000-cccc"] == (False, True)
    assert by["cpucluster1-1119-000000-dddd"] == (True, False)  # its queue unread: by its state's age
    assert by["cpucluster1-1119-000000-eeee"] == (False, True)
    assert by["condor-1119-000000-ffff"] == (True, False)  # an older piece's file, read inside
    st = next(p for p in pieces if p["name"] == "condor-1119-000000-ffff")
    assert (st["slots"], st["running"]) == (8, 2)
    assert next(p for p in pieces if str(p["name"]).startswith("condor-1119-000000-aaaa"))["slots"] == 4
    gpu["cq_rc"] = ["1"]  # condor_q failed: nothing of Condor's is judged dead by its absence
    by = {str(p["name"]).split(",")[0]: (p["live"], p["dead"]) for p in view.Snapshot(now, [], [], gpu, None).pool()[0]}
    assert by["condor-1119-000000-bbbb"] == (True, False) and by["condor-1119-000000-aaaa"] == (False, True)
    dead = names["condor-1119-000000-bbbb"]
    assert view.dead_sh([dead, "x; rm -rf /", "condor-1119-000000-aaaa"]).endswith(
        f"rm -f -- {dead} condor-1119-000000-aaaa); ")
    # an older protocol's file (no `at`) is judged by its scheduler alone, never by an age it doesn't say
    old = {"pieces": ["cpucluster1-1119-000000-gggg", "condor-1119-000000-hhhh"],
           "old": ['cpucluster1-1119-000000-gggg:{"v": 2, "backend": "cpucluster1", "slots": 4, "running": 1}',
                   'condor-1119-000000-hhhh:{"v": 2, "backend": "condor", "slots": 4, "running": 1}'],
           "cq": ["bc26-condor-1119-000000-hhhh 2"], "cq_rc": ["0"]}
    by = {p["name"]: (p["live"], p["dead"]) for p in view.Snapshot(now, [], [], old, None).pool()[0]}
    assert by == {"cpucluster1-1119-000000-gggg": (False, False), "condor-1119-000000-hhhh": (True, False)}


def test_status_takes_the_last_snapshot_while_another_process_refreshes(monkeypatch) -> None:  # type: ignore[no-untyped-def]
    import fcntl

    from tools import remote, view
    old = view.Snapshot(time.time() - 60, [], [], {"pieces": [], "open": ["run-1120-000000-aaaa"]}, None)
    remote.CLIENT.mkdir(parents=True, exist_ok=True)
    view._store(old)
    monkeypatch.setattr(remote, "login", lambda *a, **k: (_ for _ in ()).throw(AssertionError("no read")))
    lock = open(remote.CLIENT / view.LOCK, "a")
    fcntl.flock(lock, fcntl.LOCK_EX)  # another process's refresh under way
    try:
        t0 = time.monotonic()
        s = view.get(wait=False)
        assert time.monotonic() - t0 < 0.5 and s.open_runs() == ["run-1120-000000-aaaa"] and time.time() - s.at > 59
    finally:
        lock.close()


def test_a_refresh_reads_only_the_runs_due(monkeypatch) -> None:  # type: ignore[no-untyped-def]
    """A run's folder read within RUN_REREAD_S is carried over, not read again: 30 open runs read at every refresh
    took 2 s."""
    from tools import remote, view
    cmds: list[str] = []

    def login(cmd, *a, **k):  # type: ignore[no-untyped-def]
        cmds.append(cmd)
        runs = [x.split("/runs/")[1].split(" ")[0] for x in cmd.split("if cd ")[1:]]
        return "==pieces\n==open\n" + "".join(f"==run:{r}:done\n1.tar\n==run:{r}:claims\n1\n" for r in runs)
    monkeypatch.setattr(remote, "login", login)
    a, b = "run-1120-000000-aaaa", "run-1120-000000-bbbb"
    view.get(runs=[a])
    s = view.get(runs=[a, b])  # a newcomer: only its run is read
    assert cmds[-1].count("if cd ") == 1 and f"/runs/{b} " in cmds[-1]
    assert s.run(a) == s.run(b) and s.covers([a, b], [])
    monkeypatch.setattr(view, "RUN_REREAD_S", 0.0)
    view._store(view.Snapshot(0, [a, b], [], s.gpu, None, run_at=s.run_at))  # old: refreshed at the next read
    view.get(runs=[a, b])
    assert cmds[-1].count("if cd ") == 2
