"""tools/piece.py run for real on run folders made as tools/runs.py makes them: claims, how many tasks run at once,
taking over a dead piece's claims, the start limit, giving claims back on SIGTERM."""
from __future__ import annotations

import json
import os
import signal
import subprocess
import sys
import tarfile
import time
from collections.abc import Mapping, Sequence
from pathlib import Path

from tools import piece

PIECE = Path(__file__).parent / "piece.py"
BOUND_S = 60


def make_run(tmp: Path, tasks: "Sequence[Mapping[str, object]]", task_cpus: int = 1, task_mem_mb: int = 100) -> Path:
    run = tmp / "run"
    for d in ("claims", "done", "lost"):
        (run / d).mkdir(parents=True)
    (run / "run.json").write_text(json.dumps({"id": "run-1118-120000-abcd", "tasks": len(tasks),
                                              "task_cpus": task_cpus, "task_mem_mb": task_mem_mb, "cwd": "."}))
    (run / "tasks.jsonl").write_text("".join(json.dumps(t) + "\n" for t in tasks))
    (tmp / "code").mkdir()
    (tmp / "tmp").mkdir()
    return run


def env(tmp: Path, cpus: int = 2, mem: int = 1000, job: str = "local-1118-120000-0001") -> dict[str, str]:
    return dict(os.environ, JOB_ID=job, JOB_CPUS=str(cpus), JOB_MEM_MB=str(mem), TMPDIR=str(tmp / "tmp"))


def run_piece(tmp: Path, run: Path, cpus: int = 2, mem: int = 1000) -> str:
    p = subprocess.run([sys.executable, str(PIECE), str(run)], cwd=tmp / "code", env=env(tmp, cpus, mem),
                       capture_output=True, text=True, timeout=BOUND_S)
    assert p.returncode == 0, p.stdout + p.stderr
    return p.stdout


def result(run: Path, n: int) -> tuple[dict[str, object], str, dict[str, bytes]]:
    with tarfile.open(run / "done" / f"{n}.tar") as tar:
        files = {m.name: tar.extractfile(m).read() for m in tar.getmembers() if m.isfile()}  # type: ignore[union-attr]
    return json.loads(files["result.json"]), files["log"].decode(), {k: v for k, v in files.items() if
                                                                     k.startswith("out/")}


SH = "bash", "-c"


def test_every_task_runs_with_its_tokens(tmp_path: Path) -> None:
    tasks = [{"argv": [*SH, 'echo "$1 $(cat $2) $JOB_CPUS $TASK" > $3/r; echo hi', "_", "{}", "{file}", "{out}"],
              "lines": [f"line{i}"]} for i in range(5)]
    run = make_run(tmp_path, tasks)
    run_piece(tmp_path, run)
    for i in range(5):
        r, log, out = result(run, i)
        assert r["code"] == 0 and r["task"] == i and r["start"] == 1
        assert log == "hi\n"
        assert out["out/r"].decode() == f"line{i} line{i} 1 {i}\n"
    assert not list((run / "lost").iterdir())


def test_at_once_by_cpus_and_memory(tmp_path: Path) -> None:
    tasks = [{"argv": [*SH, "date +%s.%N > $OUT/a; sleep 0.6; date +%s.%N > $OUT/b"], "lines": []} for _ in range(6)]
    for cpus, mem, most in ((3, 10_000, 3), (8, 250, 2)):
        run = make_run(tmp_path / f"{cpus}", tasks)
        out = run_piece(tmp_path / f"{cpus}", run, cpus=cpus, mem=mem)
        assert f"{most} at once" in out
        spans = []
        for i in range(6):
            o = result(run, i)[2]
            spans.append((float(o["out/a"]), float(o["out/b"])))
        overlap = max(sum(1 for a, b in spans if a <= t < b) for t, _ in spans)
        assert overlap == most


def test_a_dead_pieces_claim_is_taken_over(tmp_path: Path) -> None:
    run = make_run(tmp_path, [{"argv": ["true"], "lines": []} for _ in range(3)])
    for n, by, age in ((0, "local-1118-000000-dead", piece.STALE_S + 100), (1, "local-1118-000000-live", 0)):
        (run / "claims" / str(n)).mkdir()
        (run / "claims" / str(n) / "by").write_text(f"{by} host 1\n")
        (run / "claims" / str(n) / "beat").touch()
        old = time.time() - age
        os.utime(run / "claims" / str(n) / "beat", (old, old))
        os.utime(run / "claims" / str(n), (old, old))
    out = run_piece(tmp_path, run)
    assert result(run, 0)[0]["start"] == 2 and "no beat" in out
    assert not (run / "done" / "1.tar").exists()  # a live piece's claim is left alone
    assert result(run, 2)[0]["code"] == 0
    assert [p.name.split(".")[0] for p in (run / "lost").iterdir()] == ["0"]


def test_its_own_earlier_claim_is_taken_at_once(tmp_path: Path) -> None:
    run = make_run(tmp_path, [{"argv": ["true"], "lines": []}])
    (run / "claims" / "0").mkdir()
    (run / "claims" / "0" / "by").write_text("local-1118-120000-0001 host 1\n")
    out = run_piece(tmp_path, run)
    assert "an earlier attempt of this piece" in out and result(run, 0)[0]["code"] == 0


def test_a_task_lost_at_every_start_ends_lost(tmp_path: Path) -> None:
    run = make_run(tmp_path, [{"argv": [*SH, "echo ran > $OUT/x"], "lines": []}])
    for k in range(piece.MAX_STARTS):
        (run / "lost" / f"0.{k:08x}").mkdir()
    run_piece(tmp_path, run)
    r, log, out = result(run, 0)
    assert r["code"] == piece.LOST and "lost its machine" in log and not out


def test_a_missing_program_ends_127(tmp_path: Path) -> None:
    run = make_run(tmp_path, [{"argv": ["/nonexistent/program"], "lines": []}])
    run_piece(tmp_path, run)
    assert result(run, 0)[0]["code"] == 127


def test_sigterm_gives_its_claims_back(tmp_path: Path) -> None:
    run = make_run(tmp_path, [{"argv": ["sleep", "60"], "lines": []} for _ in range(2)])
    p = subprocess.Popen([sys.executable, str(PIECE), str(run)], cwd=tmp_path / "code", env=env(tmp_path),
                         stdout=subprocess.PIPE, text=True)
    end = time.monotonic() + BOUND_S
    while len(list((run / "claims").iterdir())) < 2:
        assert time.monotonic() < end
        time.sleep(0.05)
    time.sleep(0.5)
    p.send_signal(signal.SIGTERM)
    assert p.wait(BOUND_S) == 0
    assert not list((run / "claims").iterdir()) and not list((run / "done").iterdir())
    assert sorted(x.name.split(".")[0] for x in (run / "lost").iterdir()) == ["0", "1"]


def test_a_closed_run_starts_nothing(tmp_path: Path) -> None:
    run = make_run(tmp_path, [{"argv": ["true"], "lines": []}])
    (run / "closed").touch()
    run_piece(tmp_path, run)
    assert not list((run / "done").iterdir())


# ---- the pool: pieces serving every open pooled run ----
def pooled_run(root: Path, rid: str, tasks: "Sequence[Mapping[str, object]]", created: float, *, open_: bool = True,
               **meta: object) -> Path:
    """A pooled run as tools/runs.py makes one, at ROOT/runs/RID, listed in ROOT/pool/open unless not OPEN_."""
    run = root / "runs" / rid
    for d in ("claims", "done", "lost"):
        (run / d).mkdir(parents=True)
    m = {"id": rid, "tasks": len(tasks), "task_cpus": 1, "task_mem_mb": 100, "cwd": ".", "pool": True,
         "pool_v": piece.POOL_V, "env": "e1", "wheres": ["local", "condor"], "created": created, "commit": "c" * 40,
         "code_key": "k1", "with_key": "", "kit": False, "time_s": 14400, "condor_time_s": None} | meta
    (run / "run.json").write_text(json.dumps(m))
    (run / "tasks.jsonl").write_text("".join(json.dumps(t) + "\n" for t in tasks))
    if open_:
        (root / "pool" / "open").mkdir(parents=True, exist_ok=True)
        (root / "pool" / "open" / rid).touch()
    return run


def stamp(tag: str, secs: float = 0.0) -> dict[str, object]:
    """A task recording when it began and ended, as OUT/t."""
    return {"argv": [*SH, f'echo "{tag} $(date +%s.%N)" > $OUT/a; sleep {secs}; date +%s.%N > $OUT/b'], "lines": []}


def spans(run: Path) -> list[tuple[float, float, str]]:
    """Every done task of RUN: (its start by the piece's clock, its end by its own, the piece's own run when another
    run's piece served it)."""
    out = []
    for f in sorted((run / "done").glob("*.tar")):
        r, _, o = result(run, int(f.name[:-4]))
        out.append((float(str(r["began"])), float(o["out/b"]), str(r.get("piece_run", ""))))
    return out


def start_piece(tmp: Path, run: Path, cpus: int = 2, job: str = "local-1118-120000-0001",
                **extra: str) -> subprocess.Popen[str]:
    (tmp / "code").mkdir(exist_ok=True)
    (tmp / "tmp").mkdir(exist_ok=True)
    return subprocess.Popen([sys.executable, str(PIECE), str(run)], cwd=tmp / "code",
                            env=env(tmp, cpus, 1000, job) | extra, stdout=subprocess.PIPE, text=True)


def test_a_pooled_piece_serves_other_open_runs(tmp_path: Path) -> None:
    root = tmp_path / "root"
    a = pooled_run(root, "run-1118-120000-aaaa", [stamp("a")], 1.0)
    b = pooled_run(root, "run-1118-120001-bbbb", [stamp("b") for _ in range(3)], 2.0)
    out = run_piece_at(tmp_path, a)
    assert len(spans(a)) == 1 and spans(a)[0][2] == ""
    assert [x[2] for x in spans(b)] == ["run-1118-120000-aaaa"] * 3  # B's tasks, run by A's piece
    assert "serving the pool" in out and "task run-1118-120001-bbbb/2: ended 0" in out
    assert not (root / "pool" / "open" / b.name).exists()  # every task done: B leaves the pool
    assert not list((root / "pool" / "pieces").iterdir())  # the piece's state goes with it


def run_piece_at(tmp: Path, run: Path, cpus: int = 2, job: str = "local-1118-120000-0001") -> str:
    p = start_piece(tmp, run, cpus, job)
    out, _ = p.communicate(timeout=BOUND_S)
    assert p.returncode == 0, out
    return out


def test_only_runs_it_can_serve(tmp_path: Path) -> None:
    root = tmp_path / "root"
    a = pooled_run(root, "run-1118-120000-aaaa", [stamp("a")], 1.0)
    others = [pooled_run(root, f"run-1118-12000{i}-000{i}", [stamp("x")], 2.0 + i, **m) for i, m in enumerate([
        {"task_mem_mb": 200}, {"task_cpus": 2}, {"env": "e2"}, {"wheres": ["slurm"]}, {"pool": False},
        {"pool_v": piece.POOL_V + 1}])]
    others.append(pooled_run(root, "run-1118-120009-0009", [stamp("x")], 9.0, open_=False))  # not listed
    run_piece_at(tmp_path, a)
    assert len(spans(a)) == 1 and all(not list((o / "done").iterdir()) for o in others)


def test_a_piece_with_little_time_left_serves_only_its_own_run(tmp_path: Path) -> None:
    """A Slurm piece of 4 s (124 s of --time, less SIGUSR1's lead) serves another run in its first half only."""
    root = tmp_path / "root"
    a = pooled_run(root, "run-1118-120000-aaaa", [stamp("a", 4)], 1.0, time_s=124, wheres=["slurm"])
    b = pooled_run(root, "run-1118-120001-bbbb", [stamp("b")], 2.0, wheres=["slurm"], open_=False)
    p = start_piece(tmp_path, a, job="slurm-1118-120000-0001")
    time.sleep(2.5)
    (root / "pool" / "open" / b.name).touch()  # listed once under 2 s are left
    out, _ = p.communicate(timeout=BOUND_S)
    assert len(spans(a)) == 1 and not list((b / "done").iterdir()), out


def test_ends_at_once_when_nothing_is_claimable(tmp_path: Path) -> None:
    root = tmp_path / "root"
    a = pooled_run(root, "run-1118-120000-aaaa", [stamp("a")], 1.0)
    b = pooled_run(root, "run-1118-120001-bbbb", [stamp("b")], 2.0)
    for run in (a, b):  # a's task done; b's claimed by a live piece
        (run / "claims" / "0").mkdir()
        (run / "claims" / "0" / "by").write_text("local-1118-000000-live host 1\n")
    (a / "done" / "0.tar").touch()
    t0 = time.monotonic()
    out = run_piece_at(tmp_path, a)
    assert time.monotonic() - t0 < 5 and "started" not in out and not list((b / "done").iterdir())


def test_the_fewest_running_run_goes_first(tmp_path: Path) -> None:
    """Three slots, a big old run and a small new one: the small one's tasks start among the first, not after the
    big one's; and the big one is served all along (no starvation)."""
    root = tmp_path / "root"
    a = pooled_run(root, "run-1118-120000-aaaa", [stamp("a")], 1.0)
    (a / "claims" / "0").mkdir()
    (a / "done" / "0.tar").touch()
    big = pooled_run(root, "run-1118-120001-bbbb", [stamp("big", 0.6) for _ in range(9)], 2.0)
    small = pooled_run(root, "run-1118-120002-cccc", [stamp("small", 0.6) for _ in range(2)], 3.0)
    out = run_piece_at(tmp_path, a, cpus=3)
    order = ["big" if big.name in x else "small" for x in out.splitlines() if ": started" in x]  # the start order
    assert order[:3] == ["big", "small", "big"]  # one look: fewest running first, the older on ties
    # its second task once its first has ended, while the big run still waits: not after the big run's backlog
    assert order.index("small", 2) < len(order) - 2
    assert len(spans(big)) == 9 and len(spans(small)) == 2


def test_a_closed_run_loses_only_its_tasks(tmp_path: Path) -> None:
    root = tmp_path / "root"
    a = pooled_run(root, "run-1118-120000-aaaa", [stamp("a", 4)], 1.0)
    b = pooled_run(root, "run-1118-120001-bbbb", [{"argv": ["sleep", "60"], "lines": []}], 2.0)
    p = start_piece(tmp_path, a)
    end = time.monotonic() + BOUND_S
    while not (b / "claims" / "0").exists():
        assert time.monotonic() < end
        time.sleep(0.05)
    (b / "closed").touch()
    out, _ = p.communicate(timeout=BOUND_S)
    assert p.returncode == 0 and "its run is closed" in out
    assert len(spans(a)) == 1 and not list((b / "done").iterdir())
    assert [x.name.split(".")[0] for x in (b / "lost").iterdir()] == ["0"]


def test_another_runs_checkout_and_kit(tmp_path: Path) -> None:
    """A run of other content runs in its own checkout (its bundle and shipped files), with its own kit."""
    root = tmp_path / "root"
    a = pooled_run(root, "run-1118-120000-aaaa", [stamp("a")], 1.0, kit=True)
    repo = tmp_path / "repo"
    repo.mkdir()
    g = ["git", "-C", str(repo), "-c", "user.name=t", "-c", "user.email=t@t"]
    subprocess.run([*g, "init", "-q", "-b", "main"], check=True)
    (repo / "sub").mkdir()
    (repo / "sub" / "f").write_text("b's code\n")
    subprocess.run([*g, "add", "-A"], check=True)
    subprocess.run([*g, "commit", "-qm", "b"], check=True)
    commit = subprocess.run([*g, "rev-parse", "HEAD"], capture_output=True, text=True, check=True).stdout.strip()
    jar = tmp_path / "engine.jar"
    jar.write_text("jar")
    b = pooled_run(root, "run-1118-120001-bbbb", [{"argv": [*SH, 'cat f ../shipped > $OUT/r; echo "$BC_CLASSPATH '
                                                   '$RUN_ID $PWD" >> $OUT/r'], "lines": []}], 2.0, commit=commit,
                   code_key="k2", with_key="w2", kit=True, cwd="sub")
    subprocess.run([*g, "bundle", "create", "-q", str(b / "code.bundle"), "HEAD", "main"], check=True)
    (tmp_path / "w").mkdir()
    (tmp_path / "w" / "shipped").write_text("b's shipped file\n")
    subprocess.run(["tar", "-czf", str(b / "with.tgz"), "-C", str(tmp_path / "w"), "shipped"], check=True)
    (b / "kit").write_text(f"{jar}\n")
    out = run_piece_at(tmp_path, a)
    r, log, o = result(b, 0)
    assert r["code"] == 0, log + out
    text = o["out/r"].decode().splitlines()
    assert text[:2] == ["b's code", "b's shipped file"]
    cp, rid, pwd = text[2].split()
    assert rid == b.name and pwd.endswith("/sub") and cp == f"{Path(pwd).parent}/build/classes:{jar}"
    assert "its checkout made" in out


def test_its_state_says_its_free_slots(tmp_path: Path) -> None:
    root = tmp_path / "root"
    a = pooled_run(root, "run-1118-120000-aaaa", [{"argv": ["sleep", "3"], "lines": []}], 1.0)
    p = start_piece(tmp_path, a, cpus=3)
    from tools import view
    d = root / "pool" / "pieces"
    end = time.monotonic() + BOUND_S
    while not (names := [x.name for x in d.iterdir()] if d.exists() else []):
        assert time.monotonic() < end
        time.sleep(0.05)
    st = view.parse_state(names[0])  # its state is its file's name: read by a listing, no file opened
    assert (st["job"], st["slots"], st["running"], st["task_mem_mb"], st["env"]) == (
        "local-1118-120000-0001", 3, 1, 100, "e1")
    assert abs(int(str(st["at"])) - time.time()) < 10 and (d / names[0]).stat().st_size == 0
    p.communicate(timeout=BOUND_S)
    assert not list(d.iterdir())


def bundle_repo(tmp: Path) -> tuple[Path, str]:
    """A git repo of one commit (sub/f), its bundle at REPO.bundle; (the bundle, the commit)."""
    repo = tmp / "repo"
    repo.mkdir()
    g = ["git", "-C", str(repo), "-c", "user.name=t", "-c", "user.email=t@t"]
    subprocess.run([*g, "init", "-q", "-b", "main"], check=True)
    (repo / "sub").mkdir()
    (repo / "sub" / "f").write_text("code\n")
    subprocess.run([*g, "add", "-A"], check=True)
    subprocess.run([*g, "commit", "-qm", "c"], check=True)
    commit = subprocess.run([*g, "rev-parse", "HEAD"], capture_output=True, text=True, check=True).stdout.strip()
    bundle = tmp / "repo.bundle"
    subprocess.run([*g, "bundle", "create", "-q", str(bundle), "HEAD", "main"], check=True)
    return bundle, commit


def test_a_new_checkout_survives_busy_ones(tmp_path: Path) -> None:
    """More than CHECKOUTS other runs' checkouts busy: the next run's checkout is made once and its tasks run in it
    (it used to be deleted at once, so each claim made it again, and a task's start found no checkout)."""
    root = tmp_path / "root"
    bundle, commit = bundle_repo(tmp_path)
    a = pooled_run(root, "run-1118-120000-aaaa", [stamp("a", 2)], 1.0)
    busy = [pooled_run(root, f"run-1118-12000{i}-000{i}", [stamp(f"b{i}", 2)], 2.0 + i, commit=commit,
                       code_key=f"kb{i}") for i in range(1, piece.CHECKOUTS + 2)]
    c = pooled_run(root, "run-1118-120009-cccc", [stamp("c") for _ in range(3)], 9.0, commit=commit, code_key="kc")
    for r in [*busy, c]:
        os.link(bundle, r / "code.bundle")
    out = run_piece_at(tmp_path, a, cpus=len(busy) + 3)
    assert len(spans(c)) == 3 and all(len(spans(r)) == 1 for r in busy), out
    assert sum(f"run {c.name}: its checkout made" in x for x in out.splitlines()) == 1, out


def test_no_task_without_room_to_write(tmp_path: Path) -> None:
    run = make_run(tmp_path, [{"argv": ["true"], "lines": []}])
    p = subprocess.run([sys.executable, str(PIECE), str(run)], cwd=tmp_path / "code",
                       env=env(tmp_path) | {"BC26_DISK_MIN_MB": str(1 << 40)}, capture_output=True, text=True,
                       timeout=BOUND_S)
    assert p.returncode == 0 and "taking no task" in p.stdout and "started" not in p.stdout, p.stdout + p.stderr
    assert not list((run / "claims").iterdir()) and not list((run / "done").iterdir())


def test_a_task_its_host_cant_run_goes_back(tmp_path: Path) -> None:
    """A task ending GIVE_BACK: its claim goes back (claimable at once by another piece), no result, and the piece
    takes no more tasks."""
    tasks = [{"argv": [*SH, f'echo "disk full here"; exit {piece.GIVE_BACK}'], "lines": []}] + [
        {"argv": ["true"], "lines": []} for _ in range(3)]
    run = make_run(tmp_path, tasks)
    out = run_piece(tmp_path, run, cpus=1)
    assert "taking no more tasks" in out and "disk full here" in out, out
    assert not list((run / "done").iterdir()) and not list((run / "claims").iterdir())
    assert [x.name.split(".")[0] for x in (run / "lost").iterdir()] == ["0"]


def test_its_tasks_share_a_cache(tmp_path: Path) -> None:
    run = make_run(tmp_path, [{"argv": [*SH, 'echo "$JOB_CACHE" > $OUT/c; touch "$JOB_CACHE/x$TASK"; ls "$JOB_CACHE" '
                                        '> $OUT/l'], "lines": []} for _ in range(2)])
    run_piece(tmp_path, run, cpus=1)
    (_, _, o0), (_, _, o1) = result(run, 0), result(run, 1)
    assert o0["out/c"] == o1["out/c"] and o1["out/l"].split() == [b"x0", b"x1"]


def test_a_checkout_is_made_only_for_a_claimed_task(tmp_path: Path, monkeypatch) -> None:
    """fill(): the room check, then the claim, then the checkout; a checkout made before a claim that failed (or pruned
    by the room check before its task started) was made again for every claim."""
    run = make_run(tmp_path, [stamp(str(i)) for i in range(4)])
    for k, v in env(tmp_path, cpus=4).items():
        monkeypatch.setenv(k, v)
    p = piece.Piece(run)
    calls: list[str] = []
    monkeypatch.setattr(p, "room", lambda: calls.append("room") or True)
    monkeypatch.setattr(p, "claim", lambda r, n: calls.append(f"claim {n}") or n == 2)  # only task 2 is free
    monkeypatch.setattr(p, "ready", lambda r: calls.append("ready") or True)
    monkeypatch.setattr(p, "starts", lambda r, n: 0)
    monkeypatch.setattr(p, "start", lambda r, n: calls.append(f"start {n}"))
    assert p.fill() == 1
    assert calls == ["room", "room", "claim 0", "room", "claim 1", "room", "claim 2", "ready", "start 2", "room",
                     "claim 3"]


def test_its_state_is_written_seldom(tmp_path: Path, monkeypatch) -> None:  # type: ignore[no-untyped-def]
    """At its start; a change at most every STATE_S, at once when its free slots reach or leave 0; else every
    STATE_BEAT_S."""
    root = tmp_path / "root"
    a = pooled_run(root, "run-1118-120000-aaaa", [{"argv": ["true"], "lines": []}] * 3, 1.0)
    monkeypatch.setenv("JOB_ID", "local-1118-120000-0009")
    monkeypatch.setenv("JOB_CPUS", "3")
    monkeypatch.setenv("TMPDIR", str(tmp_path))
    p = piece.Piece(a)
    clock = [1000.0]
    monkeypatch.setattr(piece.time, "monotonic", lambda: clock[0])
    d = root / "pool" / "pieces"
    writes: list[int] = []

    def step(dt: float, running: int) -> None:
        clock[0] += dt
        p.running = {(a.name, i): None for i in range(running)}  # type: ignore[misc]
        before = p.state_at
        p.write_state()
        writes.append(int(p.state_at != before))
        assert [x.name for x in d.iterdir()] == [p.state_file]  # one file, renamed
    step(0, 0)  # its start
    step(1, 1)  # a change, free slots left: waits for STATE_S
    step(1, 3)  # full: at once
    step(1, 2)  # free again: at once
    step(1, 1)  # a change within STATE_S
    step(piece.STATE_S, 1)  # ... written once STATE_S has passed
    step(1, 1)
    step(piece.STATE_BEAT_S, 1)  # unchanged: every STATE_BEAT_S
    assert writes == [1, 0, 1, 1, 0, 1, 0, 1]


def test_a_failed_checkout_costs_no_start(tmp_path: Path, monkeypatch) -> None:  # type: ignore[no-untyped-def]
    """A piece that can't check a run out (its code is a shape this piece can't read) releases its claim unrun and
    uncounted: three such pieces ended every task of 60 runs LOST."""
    run = make_run(tmp_path, [stamp("0")])
    for k, v in env(tmp_path, cpus=1).items():
        monkeypatch.setenv(k, v)
    p = piece.Piece(run)
    monkeypatch.setattr(p, "ready", lambda r: False)
    assert p.fill() == 0
    assert not (run / "claims" / "0").exists() and not list((run / "lost").iterdir())
