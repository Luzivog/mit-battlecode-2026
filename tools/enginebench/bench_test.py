"""Tests of enginebench: each runs the real check on a small case and asserts what a user would see.

    .venv/bin/pytest tools/enginebench/bench_test.py

The game tests need the data folder (~/battlecode26-data/enginebench) and engine/build/bcx; they skip without them.
"""
import json
import os
import shutil
import subprocess
from pathlib import Path

import pytest

import bench

HAVE_DATA = (bench.KINDS["visible"] / "manifest.json").exists()
BINARY = bench.REPO / "engine" / "build" / "bcx"


def manifest():
    os.environ["ENGINEBENCH_DESIGN"] = os.environ.get("ENGINEBENCH_DESIGN", "")
    m = json.loads((bench.KINDS["visible"] / "manifest.json").read_text())
    m["dir"] = str(bench.KINDS["visible"])
    return m


def cheapest(m):
    return min(m["scenarios"], key=lambda sc: sc.get("main_wall_s", 1e9))


def fake_run(tmp_path, sc, m):
    """A run folder holding the reference's own outputs, as a perfect candidate would write them."""
    d = tmp_path / sc["id"]
    g = d / "out" / "games" / sc["id"]
    g.mkdir(parents=True)
    (d / "tmp").mkdir()
    (d / "home").mkdir()
    (d / "out" / "spec.jsonl").write_text(json.dumps(sc["spec"]) + "\n")
    ref = Path(m["dir"]) / "ref" / sc["id"]
    for f in ("hashes.txt", "replay.bc26"):
        shutil.copyfile(ref / f, g / f)
    res = json.loads((ref / "result.json").read_text())
    res.update(backend="native", cpu_s=1.0, wall_ms=1000)
    (g / "result.json").write_text(json.dumps(res) + "\n")
    return {"id": sc["id"], "dir": str(d), "exit": 0, "rss_kb": 1000, "wall_s": 1.0, "instr_u": 123, "timeout": False}


@pytest.mark.skipif(not HAVE_DATA, reason="no enginebench data")
def test_validate_accepts_the_reference_and_names_each_difference(tmp_path):
    m = manifest()
    sc = cheapest(m)
    r = fake_run(tmp_path, sc, m)
    assert bench.validate(sc, r, m) == []
    g = Path(r["dir"]) / "out" / "games" / sc["id"]

    # a result field
    res = json.loads((g / "result.json").read_text())
    res["A"]["cheese"] += 1
    (g / "result.json").write_text(json.dumps(res))
    assert any("result differs" in x and "A" in x for x in bench.validate(sc, r, m))
    res["A"]["cheese"] -= 1
    (g / "result.json").write_text(json.dumps(res))

    # one round's state hash
    lines = (g / "hashes.txt").read_text().splitlines()
    k = len(lines) // 2
    parts = lines[k].split()
    parts[-1] = "0" * len(parts[-1])
    lines[k] = " ".join(parts)
    (g / "hashes.txt").write_text("\n".join(lines) + "\n")
    why = bench.validate(sc, r, m)
    assert any("state hashes differ" in x and f"round {parts[0]}" in x for x in why), why
    shutil.copyfile(Path(m["dir"]) / "ref" / sc["id"] / "hashes.txt", g / "hashes.txt")

    # the replay: re-gzipped with another level (same flatbuffer), then one flatbuffer byte
    import gzip
    raw = gzip.decompress((g / "replay.bc26").read_bytes())
    (g / "replay.bc26").write_bytes(gzip.compress(raw, compresslevel=1, mtime=0))
    assert any("gzip container differs" in x for x in bench.validate(sc, r, m))
    raw2 = bytearray(raw)
    raw2[len(raw2) // 2] ^= 1
    (g / "replay.bc26").write_bytes(gzip.compress(bytes(raw2), mtime=0))
    assert any("replay differs" in x and f"offset {len(raw2) // 2}" in x for x in bench.validate(sc, r, m))
    shutil.copyfile(Path(m["dir"]) / "ref" / sc["id"] / "replay.bc26", g / "replay.bc26")

    # memory over the cap
    r2 = dict(r, rss_kb=sc["rss_cap_kb"] + 1)
    assert any("peak RSS" in x for x in bench.validate(sc, r2, m))

    # a file kept outside the outputs (a cache between runs)
    (Path(r["dir"]) / "home" / ".cache").write_text("x")
    assert any("wrote outside its outputs: home/.cache" in x for x in bench.validate(sc, r, m))
    (Path(r["dir"]) / "home" / ".cache").unlink()
    (g / "bfs.cache").write_text("x")
    assert any("wrote outside its outputs" in x for x in bench.validate(sc, r, m))
    (g / "bfs.cache").unlink()

    # no instruction count, a timeout, an error result
    assert any("perf counted" in x for x in bench.validate(sc, dict(r, instr_u=None), m))
    assert any("timeout" in x for x in bench.validate(sc, dict(r, timeout=True), m))
    (g / "result.json").write_text(json.dumps({"id": sc["id"], "error": "boom"}))
    assert any("error: boom" in x for x in bench.validate(sc, r, m))


def test_score_is_the_weighted_sum():
    scs = [{"id": "a", "weight": 0.25}, {"id": "b", "weight": 0.75}, {"id": "f", "weight": 0.0}]
    rows = [{"id": "a", "instr_u": 4e9}, {"id": "b", "instr_u": 8e9}, {"id": "f", "instr_u": 1e12}]
    assert bench.score_of(rows, scs) == pytest.approx(7.0)
    assert bench.score_of(rows[:2] + [{"id": "f", "instr_u": None}], scs) is None


def git(repo, *a):
    subprocess.run(["git", *a], cwd=repo, check=True, capture_output=True,
                   env=dict(os.environ, GIT_AUTHOR_NAME="t", GIT_AUTHOR_EMAIL="t@t", GIT_COMMITTER_NAME="t",
                            GIT_COMMITTER_EMAIL="t@t"))


def repo_with_main(tmp_path):
    r = tmp_path / "repo"
    (r / "tools" / "enginebench").mkdir(parents=True)
    (r / "engine" / "runtime").mkdir(parents=True)
    (r / "tools" / "enginebench" / "bench.py").write_text("# the eval\n")
    (r / "engine" / "runtime" / "rt.cpp").write_text("int x = 1;\n")
    git(r, "init", "-q")
    git(r, "add", ".")
    git(r, "commit", "-qm", "main")
    git(r, "update-ref", "refs/remotes/origin/main", "HEAD")
    return r


def test_pin_refuses_an_edited_or_extra_eval_file(tmp_path, monkeypatch):
    monkeypatch.delenv("ENGINEBENCH_DESIGN", raising=False)
    r = repo_with_main(tmp_path)
    assert bench.check_pinned(r) == "pinned to origin/main"
    (r / "tools" / "enginebench" / "bench.py").write_text("# the eval, easier\n")
    with pytest.raises(bench.Refused, match="differs from origin/main"):
        bench.check_pinned(r)
    git(r, "checkout", "--", "tools/enginebench/bench.py")
    (r / "tools" / "enginebench" / "helper.py").write_text("")
    with pytest.raises(bench.Refused, match="helper.py"):
        bench.check_pinned(r)


def test_code_naming_the_eval_or_a_scenario_is_invalid(tmp_path):
    r = repo_with_main(tmp_path)
    scs = [{"spec": {"map": "DefaultSmall", "seed": 878794226}}]
    assert bench.check_names(r, scs) == []
    f = r / "engine" / "runtime" / "rt.cpp"
    f.write_text('int x = 1;\nif (map == "DefaultSmall") skip();\n')
    assert any('"DefaultSmall"' in x and "rt.cpp:2" in x for x in bench.check_names(r, scs))
    f.write_text("int x = 1;\nlong s = 878794226;\n")
    assert any("878794226" in x for x in bench.check_names(r, scs))
    f.write_text('int x = 1;\nconst char* p = "/home/u/battlecode26-data/enginebench/visible";\n')
    assert any("enginebench" in x for x in bench.check_names(r, scs))
    # a test or a doc may name them
    f.write_text("int x = 1;\n")
    (r / "engine" / "test").mkdir()
    (r / "engine" / "test" / "t.py").write_text('M = "DefaultSmall"\n')
    (r / "engine" / "AGENTS.md").write_text("see tools/enginebench\n")
    assert bench.check_names(r, scs) == []


def test_cpu_specific_builds_are_invalid(tmp_path):
    r = repo_with_main(tmp_path)
    n = tmp_path / "build.ninja"
    n.write_text("cxxflags = -std=c++20 -O2 -fwrapv\nrule cc\n  command = clang -O2 -msse4.1 -c $in -o $out\n")
    assert bench.check_portable(r, n) == []
    for bad in ("-march=native", "-mavx2", "-O3 -ffast-math", "-march=znver5", "-Ofast"):
        n.write_text(f"cxxflags = -std=c++20 -O2 {bad}\n")
        assert any("build flag" in x for x in bench.check_portable(r, n)), bad
    n.write_text("cxxflags = -O2 -march=x86-64-v2 -mtune=native\n")
    assert bench.check_portable(r, n) == []
    (r / "engine" / "runtime" / "fast.cpp").write_text("#include <immintrin.h>\n")
    assert any("CPU-specific code" in x for x in bench.check_portable(r, n))
    assert any("cannot find" in x for x in bench.check_portable(r, None))


def test_another_machine_is_refused(monkeypatch):
    here = bench.machine()
    m = {"recorded": dict(here)}
    bench.check_machine(m)
    m["recorded"]["cpu"] = "Intel(R) Xeon(R) CPU E5-2680 v4 @ 2.40GHz"
    with pytest.raises(bench.Refused, match="cpu"):
        bench.check_machine(m)


SPY = r"""
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
int main(int argc, char** argv) {
    const char* paths[] = {"/etc/passwd", "/usr/bin/env", "%s", "/bench/classes", "/bench/root/maps", 0};
    for (int i = 0; paths[i]; i++) printf("%s %s\n", paths[i], access(paths[i], R_OK) == 0 ? "readable" : "hidden");
    const char* home = getenv("HOME");
    char p[256]; snprintf(p, sizeof p, "%s/.bfs-cache", home ? home : "/nohome");
    FILE* f = fopen(p, "w"); if (f) { fputs("x", f); fclose(f); }
    f = fopen("/tmp/memo", "w"); if (f) { fputs("x", f); fclose(f); }
    f = fopen("/bench/classes/x", "w"); printf("classes writable %d\n", f != 0);
    printf("PATH %s\n", getenv("PATH") ? getenv("PATH") : "unset");
    fflush(stdout);
    // a process that leaves its parent still counts: 96 MB touched in an orphaned grandchild
    if (fork() == 0) {
        if (fork() == 0) {
            usleep(200000);
            volatile char* m = malloc(96 << 20);
            for (long i = 0; i < (96 << 20); i += 4096) m[i] = 1;
            _exit(0);
        }
        _exit(0);
    }
    return 0;
}
"""


@pytest.mark.skipif(shutil.which("clang") is None or shutil.which("bwrap") is None, reason="needs clang and bwrap")
def test_sandbox_shows_only_the_inputs_and_keeps_nothing():
    tmp_path = bench.DATA / "scratch" / "test-sandbox"  # not under /tmp: the sandbox mounts its own /tmp
    shutil.rmtree(tmp_path, ignore_errors=True)
    tmp_path.mkdir(parents=True)
    secret = tmp_path / "secret.txt"
    secret.write_text("the reference")
    src = tmp_path / "spy.c"
    src.write_text(SPY.replace("%s\", \"/bench/classes", f"{secret}\", \"/bench/classes", 1))
    spy = tmp_path / "spy"
    subprocess.run(["clang", "-static", "-O1", "-o", str(spy), str(src)], check=True)
    (tmp_path / "root" / "maps").mkdir(parents=True)
    (tmp_path / "classes").mkdir()
    m = {"dir": str(tmp_path / "root"), "classes_dir": str(tmp_path / "classes")}
    sc = {"id": "spy", "spec": {"id": "spy", "a": "x", "b": "y", "map": "m"}}
    r = bench.play(sc, spy, m, tmp_path / "runs", {}, 60)
    out = (tmp_path / "runs" / "spy" / "stdout.txt").read_text()
    assert "/etc/passwd hidden" in out and "/usr/bin/env hidden" in out and f"{secret} hidden" in out
    assert "/bench/classes readable" in out and "/bench/root/maps readable" in out
    assert "classes writable 0" in out and "PATH unset" in out
    assert r["instr_u"] and r["instr_u"] > 0
    assert r["exit"] == 0 and r["rss_kb"] > 96 * 1024, r
    # what it wrote is seen: the check that keeps a game from caching between runs
    left = sorted(str(p.relative_to(tmp_path / "runs" / "spy")) for p in (tmp_path / "runs" / "spy").rglob("*")
                  if p.is_file() and p.parent.name in ("tmp", "home"))
    assert left == ["home/.bfs-cache", "tmp/memo"]


@pytest.mark.skipif(not (HAVE_DATA and BINARY.exists()), reason="needs the data and engine/build/bcx")
def test_one_real_game_is_valid_and_counted(tmp_path):
    m = manifest()
    sc = cheapest(m)
    r = bench.play(sc, BINARY, m, tmp_path, {}, 300)
    assert bench.validate(sc, r, m) == []
    assert r["instr_u"] > 1e8
    # the count is the same game to game: the score is exact enough to compare one change
    r2 = bench.play(sc, BINARY, m, tmp_path / "again", {}, 300)
    assert abs(r2["instr_u"] - r["instr_u"]) / r["instr_u"] < 1e-3
