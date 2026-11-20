"""tools/compute: heavy work on DoC's Condor lab machines, the GPU cluster's CPUs and cpucluster1, for one job at a
time (`run`), and the commands that watch jobs and runs (`tools/run`'s). No daemon and no state of its own: Slurm,
Condor and the folders of our workspace on /vol/gpudata are the record. The compute skill says how to use it; its
references/internals.md how it works.

Exit codes: 2 when refused or a backend can't be reached. `wait` and `logs -f` exit with the job's own code, 3 when
Slurm or Condor holds it, 4 when it ended without an exit file (cancelled, its node failed); for a run, 0 when every
task ended 0, else the first task's positive code, else 1.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from dataclasses import asdict

from tools import backends as B
from tools import jobs as J
from tools import runs as R
from tools import view
from tools import workspace as W
from tools import remote
from tools.remote import (CONDOR, CPU_HOST, LOCAL, REMOTE_CAS, REMOTE_JOBS, REMOTE_POOL, REMOTE_ROOT, REMOTE_RUNS,
                          SUBMIT, Refused, login)

# The workspace holds 55 GB (remote.REMOTE_ROOT), and a day of tests wrote more than that with the retention below in
# days: results are pulled as they come, so what has ended goes within hours
KEEP_H = 6  # an ended job's or a closed run's folder is deleted this long after its end (sweep)
STALE_DAYS = 2  # a run nothing has touched for this long is deleted, closed or not
CAS_KEEP_H = 6  # a stored bundle or shipped file no run or job holds is deleted this long after its last use
SWEEP_EVERY_MIN = 60  # `run` and `tools/run` sweep at most this often
SWEEP_S = 600.0  # the sweep's bound: it walks every job and run folder


def size_mb(spec: str) -> int:
    """500M, 8G, 1.6G (decimal-ish: G = 1000 M) or a count of MB."""
    m = re.fullmatch(r"(\d+(?:\.\d+)?)([MG]?)", spec)
    if not m:
        raise Refused(f"{spec!r}: give a size such as 500M, 1.6G or 8G")
    return int(float(m[1]) * (1000 if m[2] == "G" else 1))


def duration(spec: str) -> int:
    """90m, 2h, 1d12h, 45s in seconds."""
    parts = re.findall(r"(\d+)([dhms])", spec)
    if not parts or "".join(a + b for a, b in parts) != spec:
        raise Refused(f"--time {spec!r}: give e.g. 10m, 90m, 2h or 1d12h")
    return sum(int(n) * {"d": 86400, "h": 3600, "m": 60, "s": 1}[u] for n, u in parts)


# ---- status ----
STATUS_WAIT_S = 8.0  # status waits this long for a session or another process's read, then says what it couldn't read
STATUS_AGE_S = 30.0  # status shows the pool as the shared view last read it when that is this recent


def status(as_json: bool, task_cpus: int = 1, task_mem_mb: int = R.DEFAULT_TASK_MEM_MB) -> None:
    def safe(f):  # type: ignore[no-untyped-def]
        try:
            return f()
        except Refused as e:
            return e

    remote.SLOT_WAIT_S = view.LOCK_WAIT_S = STATUS_WAIT_S  # a busy machine makes status say so, not wait
    remote.STALE_OK = True  # another process's read under way: the last one, not a wait
    age = remote.CACHE_S  # every read below shared with this machine's other tools: one login each at most
    with ThreadPoolExecutor(4) as pool:
        fc, fs, fk = (pool.submit(safe, lambda f=f: f(age)) for f in (B.read_condor, B.read_slurm, B.read_cpucluster))
        # the last snapshot when young enough or while another process refreshes it: its age is printed
        fv = pool.submit(safe, lambda: view.get(wait=False, max_age=STATUS_AGE_S))
        c, s, k, v = fc.result(), fs.result(), fk.result(), fv.result()
    notes: list[str] = []
    free = {o: [x for x in R.offers((o,), task_cpus, task_mem_mb, R.DEFAULT_TIME_S, notes, max_age=age)]
            for o in B.WHERES}
    out: dict[str, object] = {"task": {"cpus": task_cpus, "mem_mb": task_mem_mb}}
    if isinstance(c, Refused):
        out["condor"] = {"error": str(c)}
    else:
        fr = c.free()
        out["condor"] = {"machines": len(c.machines), "idle_machines": len(fr), "idle_cpus": sum(m.cpus for m in fr),
                         "idle_cpus_by_release": c.by_os, "task_slots_now": sum(o.slots for o in free["condor"]),
                         "jobs": [asdict(j) for j in c.jobs]}
    if isinstance(s, Refused):
        out["slurm"] = {"error": str(s)}
    else:
        best = free["slurm"][0] if free["slurm"] else None
        out["slurm"] = {"best_slot_now": asdict(best.slot) if best and best.slot else None,
                        "task_slots_now": best.slots if best else 0,
                        "caps": {q: {k2: asdict(v) for k2, v in lim.items()} for q, lim in s.qos.items()
                                 if any(v.cap for v in lim.values())},
                        "jobs": [asdict(j) for j in s.jobs if j.name.startswith("bc26-")]}
    if not isinstance(k, Refused) and k.takes_jobs:  # a cpucluster1 that can't take jobs isn't a backend
        out["cpucluster1"] = {"partitions": {p.name: asdict(p) for p in k.partitions.values()},
                              "cap": asdict(k.cap), "mem": asdict(k.mem),
                              "per_node": {n: asdict(v) for n, v in k.node.items()},
                              "task_slots_now": sum(o.slots for o in free["cpucluster1"]),
                              "jobs": [asdict(j) for j in k.jobs if j.name.startswith("bc26-")]}
    out["task_slots_now"] = {w: sum(o.slots for o in free[w]) for w in B.WHERES}
    if isinstance(v, Refused):
        out["pool"] = {"error": str(v)}
    else:
        pieces, _ = v.pool()
        live = [p for p in pieces if p.get("live") and isinstance(p.get("slots"), int)
                and isinstance(p.get("running"), int)]
        out["pool"] = {"age_s": round(time.time() - v.at, 1), "pieces": len(live),
                       "slots": sum(int(str(p["slots"])) for p in live),
                       "running": sum(int(str(p["running"])) for p in live), "open_runs": len(v.open_runs()),
                       "by_backend": {w: sum(1 for p in live if p.get("backend") == w) for w in B.WHERES}}
    cd = out["condor"]
    assert isinstance(cd, dict)
    down = remote.submit_down()
    if down is not None:
        cd["submit_down_s"] = round(down)
    if as_json:
        print(json.dumps(out, indent=1))
        return
    t = f"{task_cpus} CPU{'s' * (task_cpus != 1)} and {task_mem_mb} MB"
    if down is not None:
        print(f"Condor: its submit host ({remote.SUBMIT}) gave no answer {down:.0f} s ago: no piece goes there now")
    if "error" in cd:
        print(f"Condor: unreadable: {cd['error']}")
    else:
        print(f"Condor: {cd['idle_machines']} of {cd['machines']} lab machines idle ({cd['idle_cpus']} CPUs: "
              + ", ".join(f"{o} {n}" for o, n in sorted(c.by_os.items())) + f"); tasks of {t} at once on them: "
              f"{cd['task_slots_now']}")
    sd = out["slurm"]
    assert isinstance(sd, dict)
    if "error" in sd:
        print(f"Slurm (GPU cluster): unreadable: {sd['error']}")
    else:
        assert not isinstance(s, Refused)
        best = free["slurm"][0] if free["slurm"] else None
        print("Slurm (GPU cluster, CPU work holding one card): best slot now: " + (
            f"{best.cpus} CPUs and {best.mem_mb} MB on {best.slot.node} ({best.slot.partition}, a {best.slot.card} "
            f"card): {best.slots} tasks of {t}" if best and best.slot else "none"))
        for q, lim in s.qos.items():
            if lim.get("gpu") and lim["gpu"].cap:
                print(f"  our {q} caps: " + ", ".join(f"{k2} {v.used}/{v.cap}" for k2, v in lim.items() if v.cap))
    kd = out.get("cpucluster1")
    if isinstance(kd, dict):
        assert not isinstance(k, Refused)
        print("cpucluster1: " + ", ".join(f"{p.name} {p.idle_cpus} idle CPUs" for p in k.partitions.values())
              + f"; our cap {k.cap.used}/{k.cap.cap} CPUs, {k.mem.used}/{k.mem.cap} MB, "
              + ", ".join(f"{n} {v.cap}" for n, v in k.node.items()) + f" a job a node; tasks of {t} at once: "
              f"{kd['task_slots_now']}")
    for n in notes:
        print(f"  {n}")
    rows: list[str] = []
    if not isinstance(c, Refused):
        rows += [f"  {j.name.removeprefix('bc26-')}  condor {j.state}" + (f" on {j.host.split('@')[-1]}"
                 if j.host != "undefined" else "") + (f" ({j.hold_reason})" if j.hold_reason else "") for j in c.jobs]
    for name, sl in (("slurm", s), ("cpucluster1", k)):
        if not isinstance(sl, Refused):
            rows += [f"  {j.name.removeprefix('bc26-')}  {name} {j.state.lower()} {j.partition}"
                     + (f" on {j.node}" if j.node else "") + f", {j.elapsed} of {j.limit}"
                     for j in sl.jobs if j.name.startswith("bc26-")]
    pd = out["pool"]
    assert isinstance(pd, dict)
    print(f"Pool: unread: {pd['error']}" if "error" in pd else
          f"Pool ({pd['age_s']:.0f} s ago): {pd['pieces']} live pieces, {pd['running']} of {pd['slots']} slots running, "
          f"{pd['open_runs']} open "
          "runs")
    print("Our jobs:" + ("\n" + "\n".join(rows) if rows else " none"))


# ---- wait ----
def wait(ids: list[str], as_json: bool, poll_s: float, pull: bool) -> int:
    """Every job and run of IDS followed until it has ended, a job's folder pulled to runs/ID; the first non-zero
    exit of IDS in their order."""
    codes: dict[str, int] = {}
    report: dict[str, dict[str, object]] = {}
    jobs = [i for i in ids if not R.is_run(i)]
    for i in jobs:
        J.job(i)
    for rid in (i for i in ids if R.is_run(i)):
        summary = R.follow(rid, poll_s)
        R.report(summary, J.LOCAL_RUNS / rid)
        print(f"{rid}: {summary['ok']} of {summary['tasks']} tasks ended 0, exit {summary['exit']}; results in "
              f"{summary['results']}/ (N/out/, N/log, summary.json)", flush=True)
        code = summary["exit"]
        assert isinstance(code, int)
        codes[rid] = code
        report[rid] = summary
    w = J.Waiter()
    left = list(jobs)
    finals: dict[str, J.State] = {}
    shown: dict[str, str] = {}
    while left:
        try:
            states = view.get(jobs=left).states(left)
        except Refused as e:  # a lost connection says nothing about the jobs: keep waiting
            J.say(f"compute: {e}; trying again in {poll_s:.0f} s")
            time.sleep(poll_s)
            continue
        now = time.time()
        for i in list(left):
            st = states[i]
            if shown.get(i) != st.detail:
                J.say(J.describe(i, st))
                shown[i] = st.detail
            if (f := w.settle(i, st, now)) is not None:
                finals[i] = f
                left.remove(i)
        if left:
            time.sleep(poll_s)
    for i in jobs:
        st = finals[i]
        codes[i] = J.code_of(st)
        where = ""
        if pull and st.phase != "gone":
            try:
                where = str(J.pull(i))
            except Refused as e:
                J.say(f"compute: pulling {i}: {e}")
        report[i] = {"id": i, "phase": st.phase, "detail": st.detail, "exit": st.exit, "code": codes[i],
                     "peak": st.peak, "results": where}
        print(J.describe(i, st) + (f"; results in {where}/ (log, exit, out/)" if where else ""))
    if as_json:
        print(json.dumps([report[i] for i in ids], indent=1))
    return next((codes[i] for i in ids if codes[i] != 0), 0)


# ---- sweep ----
def sweep(force: bool = True) -> str:
    """Deletes our job folders ended (an exit code written) or cancelled, and our runs closed, KEEP_H ago, and runs
    nothing has touched for STALE_DAYS (no follower ever closed them): every write on the volume is bounded. Stored
    bundles and shipped files (REMOTE_CAS) no run or job links any more go CAS_KEEP_H after their last use; the pool
    forgets closed or deleted runs and pieces silent for an hour. Its last words: the workspace's size, as Ceph
    counts it (no walk). Without FORCE, at most every SWEEP_EVERY_MIN (`.swept`'s age):
    `run` and `tools/run` call it so. It rewrites MANIFEST (workspace.py) and names entries it doesn't list."""
    once = "" if force else f'[ -z "$(find {REMOTE_ROOT}/.swept -mmin -{SWEEP_EVERY_MIN} 2>/dev/null)" ] || {{ echo skipped; exit 0; }}; '
    # a job still in a queue keeps its folder, whatever its files say (a cancel whose removal failed): our queues are
    # read first, and no job folder goes when one of them can't be read
    try:
        cpu_q = login("squeue -u \"$USER\" -h -o %j", hosts=(CPU_HOST,)).split()
    except Refused:
        cpu_q = None
    queued = (f"ok=1; q=$({{ squeue -u \"$USER\" -h -o %j && {CONDOR} && condor_q -name {SUBMIT} \"$USER\" -af "
              f"JobBatchName; }} 2>/dev/null) || ok=; qs=\" $(printf '%s ' $q {' '.join(cpu_q or [])}) \"; "
              + ("cok=; " if cpu_q is None else "cok=1; "))
    cmd = (f"{once}touch {REMOTE_ROOT}/.swept; {queued}n=0; for d in {REMOTE_JOBS}/*/; do d=${{d%/}}; "
           f"[ -d \"$d\" ] || continue; [ -n \"$ok\" ] || break; case ${{d##*/}} in cpucluster1-*) [ -n \"$cok\" ] || "
           f"continue ;; esac; case \"$qs\" in *\" bc26-${{d##*/}} \"*) continue ;; esac; "
           f"if {{ [ -s $d/exit ] && [ -n \"$(find $d/exit -mmin +{KEEP_H * 60})\" ]; }} || "
           f"{{ [ -e $d/cancelled ] && [ -n \"$(find $d/cancelled -mmin +{KEEP_H * 60})\" ]; }}; "
           f"then rm -rf $d && n=$((n+1)); fi; done; "
           f"m=0; for d in {REMOTE_RUNS}/*/; do d=${{d%/}}; [ -d \"$d\" ] || continue; "
           f"if {{ [ -e $d/closed ] && [ -n \"$(find $d/closed -mmin +{KEEP_H * 60})\" ]; }} || "
           f"[ -z \"$(find $d $d/done -maxdepth 0 -mtime -{STALE_DAYS})\" ]; then rm -rf $d && m=$((m+1)); fi; done; "
           f"for f in {REMOTE_POOL}/open/*; do [ -e \"$f\" ] || continue; r={REMOTE_RUNS}/${{f##*/}}; "
           f"{{ [ -d $r ] && [ ! -e $r/closed ]; }} || rm -f \"$f\"; done; "
           f"find {REMOTE_POOL}/pieces -maxdepth 1 -type f -mmin +60 -delete 2>/dev/null; "
           f"c=$(find {REMOTE_CAS} -maxdepth 1 -type f -links 1 -mmin +{CAS_KEEP_H * 60} -print -delete 2>/dev/null "
           f"| wc -l); "
           f"echo \"$n jobs and $m runs deleted, $c stored blobs\"; {{ b=$(getfattr -n ceph.dir.rbytes --only-values {REMOTE_ROOT} 2>/dev/null) && [ -n \"$b\" ] && echo \"$((b / 1000000000)) of {W.QUOTA_GB} GB\"; }} || echo \"size unread\"; "
           f"{W.manifest_sh()}")
    said = B.sections("==said\n" + login(cmd, timeout=SWEEP_S))
    unknown = [x for x in said.get("unknown", []) if x.strip()]
    return ("; ".join(x for x in said.get("said", []) if x.strip()) + " in use"
            + (f"; not in the manifest: {', '.join(unknown)}" if unknown else ""))


def sweep_daily() -> None:
    """sweep() at most once an hour, and this checkout's old run results packed (runs.compact_local, at most hourly); a
    failure only said."""
    try:
        packed = R.compact_local()
        if packed:
            J.say(f"compute: {len(packed)} runs' results packed under runs/ (RUN/tasks.tgz), "
                  f"{sum(n for _, n in packed) / 1e6:.0f} MB freed")
    except OSError as e:
        J.say(f"compute: packing old run results: {e}")
    if remote.fresh("swept", SWEEP_EVERY_MIN * 60):  # a tool here swept within the hour: no login to ask
        return
    remote.mark("swept")
    try:
        said = sweep(force=False)
        if "deleted" in said and not said.startswith("0 jobs and 0 runs deleted, 0 stored"):
            J.say(f"compute: sweep: {said}")
    except Refused as e:
        J.say(f"compute: sweep: {e}")


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(prog="tools/compute", description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    st = sub.add_parser("status", help="what each backend can start now, and our jobs")
    st.add_argument("--json", action="store_true")
    st.add_argument("--task-cpus", type=int, default=1, help="size the 'tasks at once' counts for tasks of N CPUs")
    st.add_argument("--task-mem", default=f"{R.DEFAULT_TASK_MEM_MB}M", help="... and of this memory (default 1700M)")
    r = sub.add_parser("run", help="ship HEAD and run CMD as one job on WHERE; prints the job id")
    r.add_argument("--where", required=True, choices=J.WHERES)
    r.add_argument("--cpu", type=int, default=1, metavar="N", help="the CPUs it needs (Condor: a whole machine of "
                   "at least N, its JOB_CPUS all of them; Slurm and cpucluster1: N on one node)")
    r.add_argument("--mem", help="the memory it needs at least (8G, 500M)")
    r.add_argument("--time", help="its own estimate (10m, 2h): required on Slurm and cpucluster1, where SIGUSR1 comes "
                   "120 s before it; on Condor, the job is removed after it")
    r.add_argument("--with", dest="ships", action="append", default=[], metavar="PATH",
                   help="a file or folder the commit doesn't hold (a build's output), shipped to its path; repeatable")
    r.add_argument("--kit", action="store_true", help="compile build/classes, ship it, and give the job the engine's "
                   "jars: BC_CLASSPATH and BC_ENGINE_JARS")
    r.add_argument("--no-build", action="store_true", help="with --kit: ship build/classes as it is")
    r.add_argument("argv", nargs=argparse.REMAINDER, metavar="-- CMD")
    lg = sub.add_parser("logs", help="a job's state and the end of its log")
    lg.add_argument("id")
    lg.add_argument("-n", "--lines", type=int, default=100)
    lg.add_argument("-f", "--follow", action="store_true", help="stream it until the job ends; exit with its code")
    wt = sub.add_parser("wait", help="block until every job or run named has ended, pull the results, exit with "
                        "their status: start it as a background command, and its exit wakes you")
    wt.add_argument("ids", nargs="+", metavar="ID")
    wt.add_argument("--json", action="store_true")
    wt.add_argument("--poll", type=float, default=J.POLL_S, help=f"seconds between reads (default {J.POLL_S:.0f}, "
                    "at least 20)")
    wt.add_argument("--no-pull", action="store_true")
    pl = sub.add_parser("pull", help="copy a job's folder (log, exit, out/) to runs/ID")
    pl.add_argument("id")
    cn = sub.add_parser("cancel", help="end a job, or a run and its pieces, for good")
    cn.add_argument("id")
    sub.add_parser("sweep", help=f"delete our ended jobs' and closed runs' folders older than {KEEP_H} hours, and "
                   "rewrite the workspace's MANIFEST")
    em = sub.add_parser("empty", help="delete the whole workspace when no run is open and no job of ours is queued "
                        "(say what would go, without --yes)")
    em.add_argument("--yes", action="store_true")
    a = ap.parse_args(argv)
    try:
        if a.cmd == "status":
            status(a.json, a.task_cpus, size_mb(a.task_mem))
        elif a.cmd == "run":
            cmd: list[str] = a.argv[1:] if a.argv[:1] == ["--"] else a.argv
            J.run(a.where, a.cpu, duration(a.time) if a.time else None, cmd, a.ships, a.kit,
                  size_mb(a.mem) if a.mem else None, not a.no_build)
            sweep_daily()
        elif a.cmd == "logs":
            return J.logs(a.id, a.lines, a.follow)
        elif a.cmd == "wait":
            if a.poll < 20 and not LOCAL:
                raise Refused("--poll: at least 20 s, light on the login nodes")
            return wait(a.ids, a.json, a.poll, not a.no_pull)
        elif a.cmd == "pull":
            print(J.pull(a.id))
        elif a.cmd == "cancel":
            print(R.cancel(a.id) if R.is_run(a.id) else J.cancel(a.id))
        elif a.cmd == "empty":
            print(W.empty(a.yes))
        else:
            print(sweep())
    except Refused as e:
        print(f"compute: {e}", file=sys.stderr)
        return 2
    except KeyboardInterrupt:
        print("compute: interrupted; the jobs and runs go on (`tools/compute wait ID` to follow them again)",
              file=sys.stderr)
        return 130
    return 0


