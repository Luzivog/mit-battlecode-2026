"""tools/compute's pure parts: the backends' parsers and placement on reads captured from DoC (tools/fixtures/, 18 Nov
2026), the submit descriptions, and the rules enforced before anything reaches DoC (the ssh whitelist, Kerberos's
guard, one partition a job). Nothing here logs in anywhere."""
from __future__ import annotations

import hashlib
import json
import subprocess
from pathlib import Path

import pytest

from tools import backends as B
from tools import jobs as J
from tools import remote

FIX = Path(__file__).parent / "fixtures"


def test_slurm_read_parses() -> None:
    s = B.parse_slurm((FIX / "slurm_read.txt").read_text())
    assert {"a30", "t4", "a16", "a100", "a40", "training", "long"} <= set(s.partitions)
    dove02 = s.nodes["dove02"]
    assert (dove02.card, dove02.cards, dove02.held, dove02.cpus, dove02.idle_cpus) == ("tesla_t4", 8, 0, 48, 48)
    assert not s.nodes["gpuvm35"].usable  # down+drain
    assert not s.nodes["hopper"].usable  # EXCLUDE
    assert s.nodes["dove03"].planned
    assert s.qos["multigpu"]["cpu"] == B.Limit(48, 0)
    assert s.qos["multigpu"]["node_cpu"] == B.Limit(32, 0)
    assert s.qos["tempgpu"]["gpu"] == B.Limit(4, 0)
    assert s.jobs == []


def test_slurm_slots_hold_our_caps() -> None:
    s = B.parse_slurm((FIX / "slurm_read.txt").read_text())
    slots = B.slurm_cpu_slots(s, 1, None, 600)
    assert slots, "the fixture has free cards"
    for x in slots:
        p = s.partitions[x.partition]
        n = s.nodes[x.node]
        assert n.usable and not n.planned and n.cards > n.held
        assert x.cpus <= n.idle_cpus
        if p.qos == "multigpu":
            assert x.cpus <= 32  # MaxTRESPN cpu=32
        assert B.whole_ask(x.cpus)
    # units caps the ask, and an ask of N is given whole
    assert all(x.cpus <= 4 for x in B.slurm_cpu_slots(s, 2, 4, 600))


def test_doc_lua_counts() -> None:
    assert [c for c in range(1, 20) if B.whole_ask(c)] == [1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 18]
    assert B.granted(1, 16, 30) == 12
    assert B.granted(10, 11, 30) == 12
    assert B.granted(13, 13, 14) is None


def test_cpucluster1_read_parses() -> None:
    c = B.parse_cpucluster((FIX / "cpucluster1_read.txt").read_text())
    assert set(c.partitions) == {"amd24", "amd48", "amd96"}
    assert c.cap == B.Limit(288, 0) and c.mem == B.Limit(512000, 0)
    assert c.node["node_cpu"] == B.Limit(32, 0)
    assert c.partitions["amd48"].def_mem_per_cpu_mb > 0
    # a node's free CPUs stop at the per-node cap and at our memory cap
    assert B.cpu_free(c, c.nodes["auklet02"], 1700) == 32
    assert B.cpu_free(c, c.nodes["auklet04"], 1700) == 0  # allocated
    best = B.cpucluster_best(c, 8, 1700, 600)
    assert best is not None and best[1] == 32


def test_condor_read_parses() -> None:
    c = B.parse_condor((FIX / "condor_read.txt").read_text())
    assert len(c.machines) > 300 and not c.refused
    free = c.free(1, 1700)
    assert free and all(m.state == "Unclaimed" for m in free)
    assert sum(c.by_os.values()) == sum(m.cpus for m in c.free())


def test_condor_sub_takes_a_whole_machine() -> None:
    sub = B.condor_sub("/vol/x/jobs/condor-1", "condor-1", 4, 1700, 1700, 600)
    lines = sub.splitlines()
    assert "request_cpus = 1" in lines  # DoC's rule: the one static slot, the whole machine
    assert "should_transfer_files = NO" in lines
    assert any(x.startswith("requirements = ") and "(TotalCpus >= 4)" in x and "(Memory >= 1700)" in x for x in lines)
    assert "periodic_remove = (JobStatus == 2) && (time() - EnteredCurrentStatus > 600)" in lines
    assert lines[-1] == "queue" and sub.endswith("\n")


def test_checkpointed_time() -> None:
    assert B.checkpointed(3723) == ["--time=0-01:02:03", "--signal=B:USR1@120", "--requeue", "--open-mode=append"]


def test_ssh_only_to_login_nodes() -> None:
    for host in (*remote.SLURM_HOSTS, remote.CPU_HOST):
        argv = remote.ssh_argv(host, "true")
        assert "ControlMaster=auto" in argv and argv[-2:] == [host, "true"]
    sub = remote.ssh_argv(remote.SUBMIT, "true")
    assert not any("ControlMaster" in a for a in sub)  # a lab machine: one login a call
    for lab in ("beech17.doc.ic.ac.uk", "gallifrey.doc.ic.ac.uk", "ash05.doc.ic.ac.uk", "shell5"):
        with pytest.raises(remote.Refused, match="no ssh"):
            remote.ssh_argv(lab, "true")


def test_one_partition_a_job() -> None:
    j = J.job("slurm-1118-120000-abcd")
    with pytest.raises(remote.Refused, match="one partition"):
        J.sbatch(j, {}, ["-p", "a16,t4", "--cpus-per-task=2"], remote.SLURM_HOSTS)


def test_job_ids_are_checked() -> None:
    assert J.job("condor-1118-120000-abcd").where == "condor"
    for bad in ("condor-1118-120000-abcd; rm -rf /", "../x", "slurm-1-2-3"):
        with pytest.raises(remote.Refused):
            J.job(bad)


def ok(code: int = 0, err: bytes = b"") -> subprocess.CompletedProcess[bytes]:
    return subprocess.CompletedProcess([], code, b"", err)


def test_kerberos_never_resends_a_refused_password(tmp_path: Path) -> None:
    sent: list[bytes] = []
    refused = (tmp_path / "refused", tmp_path / "other")

    def kinit(pw: bytes) -> subprocess.CompletedProcess[bytes]:
        sent.append(pw)
        return ok(1, b"kinit: Password incorrect while getting initial credentials")

    with pytest.raises(remote.Refused, match="Password incorrect"):
        remote.kerberos_login(lambda: b"pw1\n", kinit, refused)
    assert refused[0].read_text().strip() == hashlib.sha256(b"pw1\n").hexdigest()
    with pytest.raises(remote.Refused, match="isn't sent again"):
        remote.kerberos_login(lambda: b"pw1\n", kinit, refused)
    assert sent == [b"pw1\n"]
    remote.kerberos_login(lambda: b"pw2\n", lambda pw: ok(), refused)  # a new password goes


def test_kerberos_reads_the_other_projects_record(tmp_path: Path) -> None:
    other = tmp_path / "other"
    other.write_text(hashlib.sha256(b"pw\n").hexdigest() + "\n")
    with pytest.raises(remote.Refused, match="isn't sent again"):
        remote.kerberos_login(lambda: b"pw\n", lambda pw: ok(), (tmp_path / "mine", other))


def test_kerberos_sends_no_empty_password(tmp_path: Path) -> None:
    with pytest.raises(remote.Refused, match="no password"):
        remote.kerberos_login(lambda: b"\n", lambda pw: ok(), (tmp_path / "a", tmp_path / "b"))


def test_a_lost_connection_records_nothing(tmp_path: Path) -> None:
    with pytest.raises(remote.Refused, match="exit 255"):
        remote.kerberos_login(lambda: b"pw\n", lambda pw: ok(255, b"ssh: connect"), (tmp_path / "a", tmp_path / "b"))
    assert not (tmp_path / "a").exists()


@pytest.mark.parametrize("said,tries", [(b"Connection timed out during banner exchange", 3),
                                        (b"kex_exchange_identification: read: Connection reset by peer", 3),
                                        (b"client_loop: send disconnect: Broken pipe", 1)])
def test_the_submit_host_is_tried_again_only_before_a_connection(said: bytes, tries: int) -> None:
    calls: list[list[str]] = []

    def run(argv: list[str]) -> subprocess.CompletedProcess[bytes]:
        calls.append(argv)
        return ok(255, said)

    p = remote.ssh_submit("condor_submit x", sleep=lambda s: None, run=run)
    assert p.returncode == 255 and len(calls) == tries  # a command that may have run is never run twice
    assert all(c[-2] == remote.SUBMIT for c in calls)


def test_the_submit_host_answer_is_taken_at_once() -> None:
    answers = [ok(255, b"Connection timed out during banner exchange"), ok(0)]
    p = remote.ssh_submit("true", sleep=lambda s: None, run=lambda argv: answers.pop(0))
    assert p.returncode == 0 and not answers


# cpucluster1 as its login node read it on 18 Nov 2026 21:40, its controller's StateSaveLocation full (0 KB free), and
# the same read with 5 GB free there: the only difference between a cluster that takes jobs and one that can't
FULL = (FIX / "cpucluster1_read_full.txt").read_text()
WELL = FULL.replace("/var/spool/slurm/shared 0\n", "/var/spool/slurm/shared 5242880\n")


def test_cpucluster1_takes_jobs_while_its_controller_has_room() -> None:
    assert WELL != FULL
    assert not B.parse_cpucluster(FULL).takes_jobs
    assert B.parse_cpucluster(WELL).takes_jobs
    elsewhere = FULL.replace("cloud-vm-45-10 cloud-vm-45-10 ", "cloud-vm-45-10 vm-slurmctld01 ")
    assert B.parse_cpucluster(elsewhere).takes_jobs  # a controller on another host: sbatch decides
    assert B.parse_cpucluster((FIX / "cpucluster1_read.txt").read_text()).takes_jobs  # no statesave line: the same
    assert B.CPU_READ.startswith(B.SLURM_READ.removesuffix("echo ==end")) and "==statesave" in B.CPU_READ


def fake_doc(monkeypatch: pytest.MonkeyPatch, cpucluster1: str | None) -> None:
    """Every read answered from the fixtures (cpucluster1's with CPUCLUSTER1; None: its login node unreachable), and
    no Slurm slot granted."""
    def login(cmd: str, stdin: bytes | None = None, hosts: tuple[str, ...] = remote.SLURM_HOSTS,
              stream: int = 0) -> str:
        if hosts == (remote.CPU_HOST,):
            if cpucluster1 is None:
                raise remote.Unreachable("on cpucluster1.doc.ic.ac.uk: exit 255")
            return cpucluster1
        if cmd == "bash -s":
            return ""
        return (FIX / ("condor_read.txt" if "condor_status" in cmd else "slurm_read.txt")).read_text()
    monkeypatch.setattr(B, "login", login)
    from tools import compute, runs
    monkeypatch.setattr(runs, "login", lambda cmd, *a, **k: "1000\n")  # the pool: no piece
    monkeypatch.setattr(compute, "login", lambda cmd, *a, **k: "0\n")


@pytest.mark.parametrize("read", [FULL, None])
def test_a_cpucluster1_that_cant_take_jobs_is_left_out_silently(monkeypatch: pytest.MonkeyPatch,
                                                                capsys: pytest.CaptureFixture[str],
                                                                read: str | None) -> None:
    from tools import compute, runs
    fake_doc(monkeypatch, read)
    compute.status(False)
    text = capsys.readouterr().out
    assert "Condor:" in text and "cpucluster1" not in text
    compute.status(True)
    got = json.loads(capsys.readouterr().out)
    assert "cpucluster1" not in got and got["task_slots_now"]["cpucluster1"] == 0
    notes: list[str] = []
    found = runs.offers(B.WHERES, 1, 1700, 600, notes, 48)
    assert found and not notes and all(o.where != "cpucluster1" for o in found)


def test_a_cpucluster1_that_takes_jobs_is_offered(monkeypatch: pytest.MonkeyPatch,
                                                  capsys: pytest.CaptureFixture[str]) -> None:
    from tools import compute, runs
    fake_doc(monkeypatch, WELL)
    compute.status(True)
    got = json.loads(capsys.readouterr().out)
    assert got["task_slots_now"]["cpucluster1"] > 0 and "partitions" in got["cpucluster1"]
    compute.status(False)
    assert "cpucluster1: " in capsys.readouterr().out
    assert any(o.where == "cpucluster1" for o in runs.offers(B.WHERES, 1, 1700, 600, [], 48))


def test_cpucluster1_asked_by_name_is_offered_as_read(monkeypatch: pytest.MonkeyPatch) -> None:
    """Asked for alone, by name, it goes to sbatch, whose own refusal the agent then reads."""
    from tools import runs
    fake_doc(monkeypatch, FULL)
    assert runs.offers(("cpucluster1",), 1, 1700, 600, [], 6, named=True)
    fake_doc(monkeypatch, None)
    notes: list[str] = []
    runs.offers(("cpucluster1",), 1, 1700, 600, notes, 6, named=True)
    assert notes and "exit 255" in notes[0]


def test_cpucluster1_offers_leave_room_for_our_pending_jobs(monkeypatch: pytest.MonkeyPatch) -> None:
    """Our cap counts running jobs only: our pending ones take their CPUs from what is offered, so concurrent runs
    don't queue job after job behind the same free CPUs."""
    from tools import runs
    read = (FIX / "cpucluster1_read.txt").read_text()
    assert B.parse_cpucluster(read).cap == B.Limit(288, 0)

    def offered(pending_cpus: list[int]) -> int:
        jobs = "".join(f"{900 + i}|PENDING|amd96|QOSMaxCpuPerUserLimit|bc26-cpucluster1-1119-0000{i:02d}-abcd||0:00|"
                       f"4:00:00|{c}|1700M|N/A|0|N/A|1000||N/A\n" for i, c in enumerate(pending_cpus))
        fake_doc(monkeypatch, read.replace("==jobs\n", "==jobs\n" + jobs))
        return sum(o.cpus for o in runs.offers(("cpucluster1",), 1, 1700, 600, [], None))
    assert offered([]) > 100
    assert offered([200]) <= 88
    assert offered([150, 138]) == 0


def test_the_pooled_follower_adds_pieces_only_when_tasks_wait(monkeypatch: pytest.MonkeyPatch) -> None:
    from tools import runs as R
    placed: list[int] = []
    free = {"slots": 0}
    monkeypatch.setattr(R, "pool_free", lambda s: (free["slots"], 1))
    monkeypatch.setattr(R, "place_pieces", lambda s, need: placed.append(need) or [])
    monkeypatch.setattr(R, "login", lambda cmd, *a, **k: "")
    s = R.Settings("run-1119-000000-abcd", 10, 1, 1700, ("condor",), 3600, None, None, False, ".", "c", (),
                   True, 1, "e", 0.0, "k", "")
    now = 10_000.0
    pr = R.Progress(set(), {0, 1, 2, 3}, [], False, 0)  # 6 tasks unclaimed
    queued = {"condor-1119-000000-0001": J.State("queued", "idle")}

    def look(states: dict[str, J.State], **kw: object) -> bool:
        args = dict(held=False, starting=False, now=now, placed_at=0.0, moved_at=now) | kw
        return R.follow_pool(s, pr, list(states), states, args["held"], args["starting"], args["now"],  # type: ignore[arg-type]
                             args["placed_at"], args["moved_at"])  # type: ignore[arg-type]
    monkeypatch.setattr(R, "submitted", lambda jid: now - 10)
    assert not look(queued) and not placed  # a piece of its own is coming
    assert not look({}, held=True) and not look({}, starting=True) and not look({}, placed_at=now - 5)
    free["slots"] = 6
    assert look({}) and not placed  # live pieces have room for every waiting task
    free["slots"] = 2
    assert look({}) and placed == [4]
    monkeypatch.setattr(R, "submitted", lambda jid: now - R.QUEUED_S - 1)
    assert look(queued) and placed == [4, 4]  # queued too long to wait for
