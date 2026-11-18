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
