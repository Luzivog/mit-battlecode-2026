"""Runs: many tasks spread over every free remote CPU (`tools/run`). No daemon: the run's folder on /vol/bitbucket is
the record, its pieces (jobs.py jobs running tools/piece.py) claim its tasks, and whoever follows it (`tools/run`
itself, or `tools/compute wait RUN`) fetches results, replaces pieces that were all lost, and closes it.

A run is REMOTE_RUNS/ID (ID: run-MMDD-HHMMSS-XXXX): `run.json` (its settings), `tasks.jsonl` (a task a line:
{"argv": [...], "lines": [...]}), `commit`, `code.bundle`, `with.tgz` (hard-linked into each piece's job folder),
`piece.py` (the submitter's, so a piece always speaks its follower's protocol), `pieces` (each piece's job id, a
line), `claims/`, `lost/`, `done/` (tools/piece.py), `replace.K` (round K of replacements was submitted), `closed`
(every task is done, or the run was cancelled: pieces stop). Results come back to the worktree's runs/ID/N/
(result.json, log, out/).
"""
from __future__ import annotations

import json
import re
import secrets
import shlex
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from dataclasses import asdict, dataclass
from pathlib import Path

from tools import backends as B
from tools import jobs as J
from tools import kit, provision
from tools.remote import (CPU_HOST, LOCAL, REMOTE_RUNS, SLURM_HOSTS, Refused, Unreachable, login, login_into)

RUN_ID = rf"run{J.ID_TAIL}"
DEFAULT_TASK_MEM_MB = 1700  # a game's JVM peaks at 0.5-1.6 GB (docs/testing.md §7), plus headroom
DEFAULT_TIME_S = 4 * 3600  # a Slurm or cpucluster1 piece's --time: at it, the piece gives its tasks back and requeues
MAX_ROUNDS = 3  # rounds of replacement pieces a run may get, once all its pieces were lost
QUEUED_PIECES = 4  # Condor pieces queued for a run when no backend has a free machine for it now
ORDER = ("cpucluster1", "condor", "slurm", "local")  # of equal offers, the CPU-only cluster first, the GPU cluster last


@dataclass(frozen=True)
class Settings:
    id: str
    tasks: int
    task_cpus: int  # 0: each task takes a whole machine, alone
    task_mem_mb: int
    wheres: tuple[str, ...]
    time_s: int
    condor_time_s: int | None  # Condor pieces removed after this long (only when --time was given)
    max_cpus: int | None
    kit: bool
    cwd: str
    commit: str
    gather: tuple[str, ...]


def new_run_id() -> str:
    return f"run-{time.strftime('%m%d-%H%M%S')}-{secrets.token_hex(2)}"


def run_dir(rid: str) -> str:
    if not re.fullmatch(RUN_ID, rid):
        raise Refused(f"{rid!r} isn't a run id (they look like run-1118-153000-3fa2)")
    return f"{REMOTE_RUNS}/{rid}"


def is_run(ident: str) -> bool:
    return ident.startswith("run-")


# ---- what each backend offers now ----
@dataclass(frozen=True)
class Offer:
    where: str
    slots: int  # tasks it runs at once
    cpus: int
    mem_mb: int
    slot: B.CpuSlot | None = None  # a GPU-cluster slot
    partition: str = ""  # a cpucluster1 partition


def slots_of(cpus: int, mem_mb: int, task_cpus: int, task_mem_mb: int) -> int:
    return 1 if task_cpus == 0 else min(cpus // task_cpus, mem_mb // task_mem_mb)


def offers(wheres: tuple[str, ...], task_cpus: int, task_mem_mb: int, time_s: int, notes: list[str],
           need: int | None = None, named: bool = False) -> list[Offer]:
    """What each backend of WHERES could start for this run now, read at once, best first (most slots, then ORDER).
    NEED: the tasks to run at once, the most a Slurm or cpucluster1 offer asks for (a lab machine is taken whole). A
    backend that can't be read is left out, its reason in NOTES. cpucluster1 counts only while it can take jobs (its
    read's takes_jobs), and is left out without a word otherwise, unless NAMED: asked for alone by name, it is offered
    as read, and sbatch says why it refuses."""
    tc, mb_cpu = max(task_cpus, 1), max(1, -(-task_mem_mb // max(task_cpus, 1)))

    def condor() -> list[Offer]:
        c = B.read_condor()
        return [Offer("condor", s, m.cpus, m.memory_mb) for m in c.free(tc, task_mem_mb)
                if (s := slots_of(m.cpus, m.memory_mb, task_cpus, task_mem_mb)) >= 1]

    def cpucluster() -> list[Offer]:
        try:
            c = B.read_cpucluster()
        except Refused:
            if named:
                raise
            return []
        if not (c.takes_jobs or named):
            return []
        out: list[Offer] = []
        cap = c.cap.cap - c.cap.used if c.cap.cap is not None else None
        mem = c.mem.cap - c.mem.used if c.mem.cap is not None else None
        left = need
        for name, n in sorted(c.nodes.items(), key=lambda x: -x[1].idle):
            if not n.usable or (left is not None and left <= 0):
                continue
            part = next((p for p in n.partitions if p in c.partitions and (
                c.partitions[p].max_time_s is None or time_s <= (c.partitions[p].max_time_s or 0))), None)
            if part is None:
                continue
            free = B.cpu_free(c, n, mb_cpu)
            free = min(free, *(() if cap is None else (cap,)), *(() if mem is None else (mem // mb_cpu,)))
            if left is not None and task_cpus:
                free = min(free, left * tc)
            free -= free % tc
            if free < tc:
                continue
            out.append(Offer("cpucluster1", slots_of(free, free * mb_cpu, task_cpus, task_mem_mb), free,
                             free * mb_cpu, partition=part))
            left = None if left is None else left - out[-1].slots
            cap = None if cap is None else cap - free
            mem = None if mem is None else mem - free * mb_cpu
        return out

    def slurm() -> list[Offer]:
        units = None if need is None or not task_cpus else need * tc
        slots = B.slurm_starts_now(B.read_slurm(), tc, units, time_s, max(B.CPU_JOB_MIN_MEM_MB, mb_cpu))
        seen: set[str] = set()
        out: list[Offer] = []
        for x in slots:  # one a node: slots on one node's several partitions are the same CPUs
            if x.node not in seen:
                seen.add(x.node)
                out.append(Offer("slurm", slots_of(x.cpus, x.mem_mb, task_cpus, task_mem_mb), x.cpus, x.mem_mb, x))
        return out[:1]  # each pick changes what the caps leave: one now, the next from a fresh read (place_pieces)

    def local() -> list[Offer]:  # tests: three machines of 2 CPUs
        return [Offer("local", slots_of(2, 4096, task_cpus, task_mem_mb), 2, 4096)] * 3

    readers = {"condor": condor, "cpucluster1": cpucluster, "slurm": slurm, "local": local}
    out: list[Offer] = []
    with ThreadPoolExecutor(len(wheres)) as pool:
        futures = {w: pool.submit(readers[w]) for w in wheres}
        for w, f in futures.items():
            try:
                out += f.result()
            except Refused as e:
                notes.append(f"{w}: unreadable: {e}")
    return sorted([o for o in out if o.slots >= 1], key=lambda o: (-o.slots, -o.cpus, ORDER.index(o.where)))


def choose(found: list[Offer], need: int, max_cpus: int | None) -> list[Offer]:
    """The offers to take: best first until their slots hold NEED tasks at once, or MAX_CPUS is reached."""
    took: list[Offer] = []
    slots = cpus = 0
    for o in found:
        if slots >= need or (max_cpus is not None and cpus + o.cpus > max_cpus):
            break
        took.append(o)
        slots += o.slots
        cpus += o.cpus
    return took


# ---- creating a run ----
@dataclass(frozen=True)
class Task:
    argv: list[str]
    lines: list[str]


def tasks_of(cmd: list[str], lines: list[str] | None, chunk: int) -> list[Task]:
    """One task (CMD) when LINES is None; else CMD once a CHUNK of LINES. {} is the line (CHUNK 1 only), {file} a file
    of the task's lines, {out} its OUT, {cpus} its CPUs; with none of {} and {file}, the line's words are appended."""
    if lines is None:
        return [Task(cmd, [])]
    if chunk < 1:
        raise Refused("--chunk: at least 1")
    uses = any("{}" in a for a in cmd)
    if uses and chunk > 1:
        raise Refused("{} is one line: with --chunk, name the task's lines as {file}")
    tasks: list[Task] = []
    for i in range(0, len(lines), chunk):
        part = lines[i:i + chunk]
        if uses or any("{file}" in a for a in cmd):
            tasks.append(Task(list(cmd), part))
        else:
            tasks.append(Task([*cmd, *shlex.split(part[0])] if chunk == 1 else [*cmd, "{file}"], part))
    return tasks


def create(tasks: list[Task], task_cpus: int, task_mem_mb: int, wheres: tuple[str, ...], time_s: int | None,
           max_cpus: int | None, with_kit: bool, ships: list[str], gather: tuple[str, ...], build: bool = True,
           repo: Path = J.REPO, cwd: Path | None = None) -> Settings:
    """The run's folder made and filled (one login after the uploads); its settings. Nothing is submitted yet."""
    if not tasks:
        raise Refused("no tasks: the file has no lines")
    here = (cwd or Path.cwd()).resolve()
    if with_kit and build:
        kit.compile_classes(repo)
    code = J.snapshot(repo)
    paths = J.ship(ships, repo, here) + ([kit.CLASSES] if with_kit and kit.CLASSES not in ships else [])
    with_tgz = provision.tar_paths(repo, paths) if paths else b""
    rid = new_run_id()
    s = Settings(rid, len(tasks), task_cpus, task_mem_mb, wheres, time_s or DEFAULT_TIME_S, time_s, max_cpus, with_kit,
                 str(here.relative_to(repo)) if here.is_relative_to(repo) else ".", code.commit, gather)
    hosts = SLURM_HOSTS
    if not LOCAL:
        J.private_root(hosts)
        if "cpucluster1" in wheres:
            J.private_root((CPU_HOST,))
        provision.ensure(hosts, repo, with_kit)
    files = {"run.json": json.dumps(asdict(s)).encode(), "commit": f"{code.commit}\n".encode(),
             "code.bundle": code.bundle, "piece.py": (Path(__file__).parent / "piece.py").read_bytes(),
             "tasks.jsonl": "".join(json.dumps({"argv": t.argv, "lines": t.lines}) + "\n" for t in tasks).encode(),
             "pieces": b""} | ({"with.tgz": with_tgz} if with_tgz else {})
    d = run_dir(rid)
    login(f"mkdir -p {REMOTE_RUNS} && mkdir -m 700 {d} && tar -xf - -C {d} && mkdir {d}/claims {d}/done {d}/lost",
          J.folder_tar(files), hosts)
    J.say(f"run {rid}: {len(tasks)} task{'s' * (len(tasks) != 1)} of commit {code.commit[:12]} ({code.subject})"
          + (f", shipping {', '.join(paths)} ({len(with_tgz) / 1e6:.1f} MB)" if paths else ""))
    return s


def read_settings(rid: str) -> Settings:
    raw = json.loads(login(f"cat {run_dir(rid)}/run.json"))
    raw["wheres"], raw["gather"] = tuple(raw["wheres"]), tuple(raw["gather"])
    return Settings(**raw)


# ---- pieces ----
def piece_files(s: Settings, has_with: bool) -> tuple[dict[str, bytes], dict[str, str]]:
    """A piece's job files, and the run's files hard-linked into its folder (name -> path)."""
    d = run_dir(s.id)
    files = {"job.sh": (Path(__file__).parent / "job.sh").read_bytes(), "commit": f"{s.commit}\n".encode(),
             "argv": b"".join(a.encode() + b"\0" for a in ["python3", f"{d}/piece.py", d]),
             "env": provision.env_file(), "exit": b"", "log": b"", "run": f"{s.id}\n".encode()}
    if s.kit:
        files["kit"] = provision.kit_file(J.REPO)
    return files, {"code.bundle": f"{d}/code.bundle", **({"with.tgz": f"{d}/with.tgz"} if has_with else {})}


def submit_pieces(s: Settings, took: list[Offer], queued_condor: int = 0) -> list[tuple[str, int]]:
    """A piece for each offer of TOOK (QUEUED_CONDOR more Condor pieces to wait for machines); (job id, its tasks at
    once) of each one submitted, appended to the run's `pieces` once its backend took it. One whose submit failed is
    named, and left out."""
    d = run_dir(s.id)
    has_with = login(f"test -e {d}/with.tgz && echo yes; true").strip() == "yes"
    files, shared = piece_files(s, has_with)
    got: list[tuple[str, int]] = []
    condor = [o for o in took if o.where == "condor"]
    n_condor = len(condor) + queued_condor
    if n_condor:
        mem = min([o.mem_mb for o in condor] or [s.task_mem_mb])
        js: list[J.Job] = []
        while len(js) < n_condor:
            j = J.job(J.new_id("condor"))
            if j.id not in {x.id for x in js}:
                js.append(j)
        subs = [(j, files, B.condor_sub(j.dir, j.id, max(s.task_cpus, 1), min(mem, s.task_mem_mb),
                                        s.task_mem_mb if s.task_cpus else None, s.condor_time_s)) for j in js]
        clusters = J.submit_condor(subs, shared)
        slots = [o.slots for o in condor] + [1] * queued_condor
        got += [(j.id, n) for j, c, n in zip(js, clusters, slots, strict=True) if c is not None]
        J.say(f"run {s.id}: {sum(c is not None for c in clusters)} Condor piece(s), a lab machine each"
              + (f" ({queued_condor} to wait for a machine)" if queued_condor else ""))
    for o in took:
        if o.where == "condor":
            continue
        j = J.job(J.new_id(o.where))
        pre = "".join(f" && ln {src} {j.dir}/{name}" for name, src in shared.items())
        try:
            if o.where == "slurm":
                assert o.slot is not None
                J.sbatch(j, files, B.cpu_slot_args(o.slot, s.time_s), SLURM_HOSTS, pre=pre)
            elif o.where == "cpucluster1":
                # --ntasks=1: cpucluster1's job_submit.lua refuses a job that leaves its task count unset
                J.sbatch(j, files, ["-p", o.partition, "--ntasks=1", f"--cpus-per-task={o.cpus}",
                                    f"--mem-per-cpu={o.mem_mb // o.cpus}M", *B.checkpointed(s.time_s)],
                         (CPU_HOST,), pre=pre)
            else:
                J.submit_local(j, files, shared)
        except Refused as e:
            J.say(f"run {s.id}: piece {j.id} not submitted: {e}")
            continue
        got.append((j.id, o.slots))
        J.say(f"run {s.id}: piece {j.id}: {o.cpus} CPUs, {o.mem_mb} MB, {o.slots} tasks at once"
              + (f" ({o.slot.node}, {o.slot.partition})" if o.slot else f" ({o.partition})" if o.partition else ""))
    if got:
        login(f"printf '%s\\n' {' '.join(i for i, _ in got)} >> {d}/pieces")
    return got


def place_pieces(s: Settings, need: int) -> list[str]:
    """Pieces for NEED tasks at once on what the backends offer now, best first. The GPU cluster's slots go one at a
    time, each from a read made after the last one's submit (each changes what our caps leave). When nothing is free,
    QUEUED_PIECES Condor pieces wait for machines. Refused when no piece could be submitted."""
    notes: list[str] = []
    found = offers(s.wheres, s.task_cpus, s.task_mem_mb, s.time_s, notes, need, named=s.wheres == ("cpucluster1",))
    took = choose(found, need, s.max_cpus)
    for n in notes:
        J.say(f"run {s.id}: {n}")
    got = submit_pieces(s, [o for o in took if o.where != "slurm"]) if any(o.where != "slurm" for o in took) else []
    left, cpus = need - sum(n for _, n in got), sum(o.cpus for o in took if o.where != "slurm")
    slurm = [o for o in took if o.where == "slurm"]
    for _ in range(8 if slurm else 0):  # at most 8 GPU-cluster pieces (multigpu allows 8 jobs)
        if left <= 0 or not slurm:
            break
        o = slurm.pop()
        if s.max_cpus is not None and cpus + o.cpus > s.max_cpus:
            break
        one = submit_pieces(s, [o])
        got += one
        left -= sum(n for _, n in one)
        cpus += o.cpus
        if one and left > 0:
            slurm = offers(("slurm",), s.task_cpus, s.task_mem_mb, s.time_s, [], left)[:1]
    if not got:
        if "condor" not in s.wheres:
            raise Refused(f"no piece could start on {', '.join(s.wheres)} now (`tools/compute status`)")
        queued = min(QUEUED_PIECES, need)
        J.say(f"run {s.id}: no backend took a piece now: {queued} Condor piece(s) wait in its queue")
        got = submit_pieces(s, [], queued)
        if not got:
            raise Refused("no piece could be submitted (the reasons are above)")
    return [i for i, _ in got]


# ---- following a run ----
@dataclass
class Progress:
    done: set[int]
    claimed: set[int]
    pieces: list[str]
    closed: bool
    rounds: int


def read_progress(rid: str) -> Progress:
    d = run_dir(rid)
    s = B.sections(login(f"cd {d} && echo ==done && ls done && echo ==claims && ls claims && echo ==pieces && "
                         f"cat pieces && echo ==closed && {{ ls closed 2>/dev/null; true; }} && echo ==rounds && "
                         f"{{ ls -d replace.* 2>/dev/null; true; }}"))

    def nums(key: str, suf: str = "") -> set[int]:
        stems = (x[:len(x) - len(suf)] for x in s.get(key, []) if x.endswith(suf))
        return {int(x) for x in stems if x.isdigit()}
    return Progress(nums("done", ".tar"), nums("claims"), [x for x in s.get("pieces", []) if x.strip()],
                    bool(s.get("closed")), len(s.get("rounds", [])))


def fetch(rid: str, names: list[int], out: Path) -> None:
    """Tasks NAMES' done tars, unpacked into OUT/N/."""
    for i in range(0, len(names), 200):
        part = names[i:i + 200]
        tmp = out / ".done"
        login_into(f"tar -C {run_dir(rid)}/done -cf - {' '.join(f'{n}.tar' for n in part)}", tmp)
        for n in part:
            dest = out / str(n)
            dest.mkdir(parents=True, exist_ok=True)
            import tarfile
            with tarfile.open(tmp / f"{n}.tar") as tar:
                tar.extractall(dest, filter="data")
            (tmp / f"{n}.tar").unlink()


def result_of(out: Path, n: int) -> dict[str, object]:
    try:
        r: dict[str, object] = json.loads((out / str(n) / "result.json").read_text())
        return r
    except (OSError, ValueError):
        return {"task": n, "code": None}


def close(rid: str, pieces: list[str]) -> None:
    """The run closed (pieces stop claiming), its inputs deleted (a piece still running holds its own links), and its
    pieces that never started cancelled: no machine is held for nothing."""
    d = run_dir(rid)
    login(f"touch {d}/closed && rm -f {d}/code.bundle {d}/with.tgz")
    if not pieces:
        return
    states = J.read_states(pieces)
    for pid, st in states.items():
        if st.phase in ("queued", "held") and "started" not in st.flags:
            try:
                J.say(f"run {rid}: {J.cancel(pid)} ({pid}, never started)")
            except Refused as e:
                J.say(f"run {rid}: cancelling {pid}: {e}")


def follow(rid: str, poll_s: float = J.POLL_S, out_root: Path = J.LOCAL_RUNS) -> dict[str, object]:
    """RID followed until every task is done, or its pieces were lost MAX_ROUNDS times: results fetched as they come,
    pieces replaced when every one is gone and tasks are left. Its summary (also runs/ID/summary.json)."""
    s = read_settings(rid)
    out = out_root / rid
    out.mkdir(parents=True, exist_ok=True)
    (out / "run.json").write_text(json.dumps(asdict(s), indent=1) + "\n")
    have = {int(p.name) for p in out.iterdir() if p.name.isdigit() and (p / "result.json").exists()}
    shown, dead_since, w = "", None, J.Waiter()
    finals: dict[str, J.State] = {}
    while True:
        try:
            pr = read_progress(rid)
            new = sorted(pr.done - have)
            if new:
                fetch(rid, new, out)
                have |= set(new)
            live = [p for p in pr.pieces if p not in finals]
            states = J.read_states(live) if live else {}
        except Unreachable as e:  # a lost connection says nothing about the run: keep following
            J.say(f"run {rid}: {e}; trying again in {poll_s:.0f} s")
            time.sleep(poll_s)
            continue
        now = time.time()
        for pid, st in states.items():
            if (f := w.settle(pid, st, now)) is not None:
                finals[pid] = f
                if f.phase == "held":
                    J.say(f"run {rid}: piece {J.describe(pid, f)}")
        running = sum(1 for st in states.values() if st.phase == "running")
        line = (f"run {rid}: {len(have)} of {s.tasks} tasks done, {len(pr.claimed - have)} running; pieces: "
                f"{running} running, {sum(1 for p in pr.pieces if p not in finals) - running} waiting, "
                f"{len(finals)} ended")
        if line != shown:
            J.say(line)
            shown = line
        if len(have) >= s.tasks or pr.closed:
            if not pr.closed:
                close(rid, [p for p in pr.pieces if p not in finals])
            return summarize(s, out, finals)
        alive = [p for p in pr.pieces if p not in finals]
        held = [p for p, f in finals.items() if f.phase == "held"]
        if held and not alive:  # never routed around: the run stays open, and its follower stops, saying why
            J.say(f"run {rid}: no piece runs and {len(held)} held: " + "; ".join(J.describe(p, finals[p]) for p in held)
                  + f". Report it; `tools/compute wait {rid}` follows the run again")
            summary = summarize(s, out, finals)
            summary["exit"] = J.HELD
            return summary
        broken = [p for p, f in finals.items() if f.exit not in (None, 0, 75) and f.phase != "held"]
        if broken and not alive:  # a piece that failed (not one lost) would fail again: the run stops, its log shown
            p = broken[0]
            tail = J.read_log(J.job(p), 0).decode(errors="replace").splitlines()[-15:]
            J.say(f"run {rid}: piece {J.describe(p, finals[p])}, so no piece is started again; its log ends:\n  "
                  + "\n  ".join(tail))
            close(rid, [])
            return summarize(s, out, finals)
        if alive:
            dead_since = None
        else:
            dead_since = dead_since or now
            left = s.tasks - len(have)
            unclaimed = s.tasks - len(pr.done | pr.claimed)
            from tools.piece import STALE_S
            if unclaimed > 0 or now - dead_since > (0 if LOCAL else STALE_S + 60):
                if pr.rounds >= MAX_ROUNDS:
                    J.say(f"run {rid}: every piece ended with {left} tasks left, after {pr.rounds} rounds of "
                          "replacements: closing it")
                    close(rid, [])
                    return summarize(s, out, finals)
                k = pr.rounds + 1
                try:
                    login(f"mkdir {run_dir(rid)}/replace.{k}")  # one follower submits round K
                except Refused:
                    pass
                else:
                    J.say(f"run {rid}: every piece has ended with {left} tasks left: replacement round {k}")
                    place_pieces(s, left)
                    dead_since = None
        time.sleep(poll_s)


def summarize(s: Settings, out: Path, finals: dict[str, J.State]) -> dict[str, object]:
    results = [result_of(out, n) for n in range(s.tasks)]
    codes = [r.get("code") for r in results]
    failed = [n for n, c in enumerate(codes) if c != 0]
    gathered: dict[str, str] = {}
    for name in s.gather:
        dest = out / name
        with open(dest, "wb") as f:
            for n in range(s.tasks):
                part = out / str(n) / "out" / name
                if part.is_file():
                    f.write(part.read_bytes())
        gathered[name] = str(dest)
    secs = [float(str(r["s"])) for r in results if isinstance(r.get("s"), (int, float))]
    peaks = [int(str(r["peak"])) for r in results if isinstance(r.get("peak"), int)]
    summary: dict[str, object] = {
        "id": s.id, "tasks": s.tasks, "done": sum(c is not None for c in codes), "ok": codes.count(0),
        "failed": failed, "codes": {str(n): codes[n] for n in failed}, "results": str(out), "gathered": gathered,
        "task_s_total": round(sum(secs), 1), "task_peak_max": max(peaks, default=0),
        "hosts": sorted({str(r.get("host")) for r in results if r.get("host")}),
        "pieces": {p: J.describe(p, st) for p, st in finals.items()},
        "exit": exit_code(codes)}
    (out / "summary.json").write_text(json.dumps(summary, indent=1) + "\n")
    return summary


def exit_code(codes: list[object]) -> int:
    """0 when every task ended 0; else the first positive code; else 1 (a task lost, killed or never run)."""
    bad = [c for c in codes if c != 0]
    if not bad:
        return 0
    return next((c for c in bad if isinstance(c, int) and 0 < c < 256), 1)


def cancel(rid: str) -> str:
    pr = read_progress(rid)
    login(f"touch {run_dir(rid)}/closed && rm -f {run_dir(rid)}/code.bundle {run_dir(rid)}/with.tgz")
    said = []
    for pid, st in J.read_states(pr.pieces).items():
        if st.phase in ("queued", "running", "held", "unknown"):
            said.append(J.cancel(pid))
    return f"run {rid} closed; " + ("; ".join(said) if said else "no piece was live")


def report(summary: dict[str, object], out: Path) -> None:
    """The end of a run on stderr: its failed tasks' log ends, then a line."""
    failed = summary["failed"]
    assert isinstance(failed, list)
    for n in failed[:5]:
        log = out / str(n) / "log"
        tail = "\n".join(log.read_text(errors="replace").splitlines()[-20:]) if log.exists() else "(no log)"
        r = result_of(out, n)
        print(f"--- task {n} ended {r.get('code')} on {r.get('host') or 'no machine'}:\n{tail}", file=sys.stderr)
    print(f"run {summary['id']}: {summary['ok']} of {summary['tasks']} tasks ended 0; results in {summary['results']}",
          file=sys.stderr)
