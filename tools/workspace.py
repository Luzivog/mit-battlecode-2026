"""Our workspace on /vol/gpudata (remote.REMOTE_ROOT) holds nothing persistent: every entry is either the state of an
open run or a cache that makes jobs faster, and results live on clankerland. MANIFEST, at its root, names each
top-level entry: what it is, the tool that writes it, how long an entry lives, what breaks if it is deleted. The tools
write it (`tools/compute sweep` rewrites it and names any entry it doesn't list), never a person.

`tools/compute empty` deletes the whole workspace once no job of ours (queued, running or held) has its folder there
and no open run there changed in the last hour. It says what it would delete and why it can't yet, and deletes only
with --yes, in bounded steps.
"""
from __future__ import annotations

import re
import shlex

from tools.remote import (CONDOR, CPU_HOST, LOCAL, REMOTE_ROOT, SLURM_HOSTS, SUBMIT, Refused, Unreachable, forget,
                          login, unmark_all)

QUOTA_GB = 55  # the workspace's quota (ws_allocate's)
# A new run is refused while the workspace holds more than this (after one sweep): its uploads and the results of the
# runs already open need the rest, and a write past the quota fails mid-run
ROOM_GB = 40
STEP_S = 240.0  # one deletion step's bound; the next step goes on where it stopped
STEP_ENTRIES = 400  # entries of a folder one step deletes
MAX_STEPS = 500

# name -> (what it is, which tool writes it, how long an entry lives, what breaks if it is deleted)
ENTRIES: dict[str, tuple[str, str, str, str]] = {
    "MANIFEST": ("this list", "tools/compute sweep (workspace.py)", "rewritten by every sweep", "nothing"),
    ".swept": ("the time of the last sweep", "tools/compute sweep", "touched by every sweep",
               "nothing: the next tools/run sweeps at once"),
    "jobs": ("a folder a job (tools/compute run) or piece (tools/run): its command, code links, log, exit",
             "tools/jobs.py, tools/runs.py; job.sh on the node", "until 6 h after it ended",
             "a live job or piece fails; an ended one: nothing (its results were fetched or are in runs/)"),
    "runs": ("a folder a run: settings, tasks, claims, task results until fetched",
             "tools/runs.py; tools/piece.py on the node", "while open, then 6 h; 2 days if nothing touches it",
             "an open run loses its tasks and the results not yet fetched; a closed one: nothing"),
    "pool": ("open/RUN: the runs pieces serve; pieces/JOB: each live piece's free slots",
             "tools/runs.py, tools/piece.py, tools/view.py", "while the run is open, while the piece runs",
             "pieces stop serving other runs; new runs ask for pieces of their own"),
    "cas": ("code bundles and shipped files, one a content", "tools/jobs.py ship_cas (tools/run)",
            "6 h after its last use", "nothing: the next run uploads its code again"),
    "runtime": ("the JDK, Python and engine jars every job runs on (provision.py); bcx/: native game binaries",
                "tools/provision.py; tools/bcgames/imperial.py", "until their version changes; a binary 3 days unused",
                "running jobs fail; the next run uploads them again"),
}


def manifest_text() -> str:
    lines = [f"# {REMOTE_ROOT}: nothing persistent. Written by tools/compute sweep; do not edit.",
             "# entry | what it is | written by | an entry lives | if deleted"]
    lines += [f"{name} | {' | '.join(v)}" for name, v in ENTRIES.items()]
    return "\n".join(lines) + "\n"


def manifest_sh() -> str:
    """MANIFEST rewritten when it differs, then each top-level entry it doesn't list (`==unknown`)."""
    text = manifest_text()
    names = " ".join(shlex.quote(n) for n in ENTRIES)
    return (f"cd {REMOTE_ROOT} && {{ printf %s {shlex.quote(text)} | cmp -s - MANIFEST || "
            f"{{ printf %s {shlex.quote(text)} > .MANIFEST.tmp && mv -f .MANIFEST.tmp MANIFEST; }}; }}; "
            f"echo ==unknown; for f in * .[!.]*; do [ -e \"$f\" ] || continue; case \" {names} \" in "
            "*\" $f \"*) ;; *) echo \"$f\" ;; esac; done; cd /; ")


# Our jobs in every scheduler, with the command each runs (its job folder's path says which tree it uses)
JOBS_SH = (f"echo ==sq; squeue -u \"$USER\" -h -o '%j|%T|%o'; echo ==sq_rc; echo $?; echo ==cq; {CONDOR}; "
           f"condor_q -name {SUBMIT} \"$USER\" -af:, JobBatchName JobStatus Args; echo ==cq_rc; echo $?; ")


def our_jobs() -> list[tuple[str, str, str]]:
    """(job, state, command) of each job of ours queued, running or held anywhere. Unreachable when a scheduler
    couldn't be read: nothing is deleted on a guess."""
    from tools.backends import CONDOR_STATE, sections
    out: list[tuple[str, str, str]] = []
    for hosts in (SLURM_HOSTS, (CPU_HOST,)):
        s = sections(login(JOBS_SH if hosts == SLURM_HOSTS else JOBS_SH.split("echo ==cq;")[0], hosts=hosts))
        if s.get("sq_rc") != ["0"] or (hosts == SLURM_HOSTS and s.get("cq_rc") != ["0"]):
            raise Unreachable(f"our queues on {hosts[0]} unread")
        out += [(f[0], f[1], f[2]) for f in (x.split("|", 2) for x in s.get("sq", [])) if len(f) == 3]
        out += [(f[0], CONDOR_STATE.get(int(f[1]), f[1]) if f[1].isdigit() else f[1], f[2])
                for f in (x.split(", ", 2) for x in s.get("cq", [])) if len(f) == 3]
    return out


ACTIVE_MIN = 60  # a run not closed whose folder, done/ or claims/ changed this recently is in use


def active_runs(root: str) -> list[str]:
    """ROOT's open pooled runs (pool/open) not closed whose folder, results or claims changed in the last ACTIVE_MIN
    minutes, and `runs/` itself when a run was made there that recently. One read of a few folders: a walk of every
    run folder outlasts its bound when there are thousands."""
    out = login(f"cd {root} 2>/dev/null || exit 0; [ -n \"$(find runs -maxdepth 0 -mmin -{ACTIVE_MIN} 2>/dev/null)\" ] "
                "&& echo 'runs/ (a run made)'; for f in pool/open/*; do r=${f##*/}; d=runs/$r; [ -e \"$f\" ] && "
                f"[ ! -e \"$d/closed\" ] && [ -n \"$(find \"$d\" \"$d/done\" \"$d/claims\" -maxdepth 0 -mmin "
                f"-{ACTIVE_MIN} 2>/dev/null)\" ] && echo \"$r\"; done; true", timeout=STEP_S)
    return [x for x in out.splitlines() if x.strip()]


def busy(root: str) -> list[str]:
    """Why ROOT can't be deleted now: jobs of ours (queued, running or held) whose folders are under it, and its runs
    still moving (active_runs)."""
    users = [f"{j} ({st})" for j, st, cmd in our_jobs() if f"{root}/" in cmd]
    runs = active_runs(root)
    said = []
    if users:
        said.append(f"{len(users)} jobs of ours use it: {', '.join(users[:5])}{' ...' if len(users) > 5 else ''}")
    if runs:
        said.append(f"{len(runs)} open runs there changed in the last {ACTIVE_MIN} minutes: {', '.join(runs[:5])}"
                    + (" ..." if len(runs) > 5 else ""))
    return said


def entries(root: str) -> list[tuple[str, str]]:
    """ROOT's top-level entries, each with its size as the filesystem counts it (Ceph: no walk) or its entry count
    (an NFS volume: one readdir)."""
    out = login(f"cd {root} 2>/dev/null || exit 0; for f in * .[!.]*; do [ -e \"$f\" ] || continue; "
                "b=$(getfattr -n ceph.dir.rbytes --only-values \"$f\" 2>/dev/null) && "
                "{ printf '%s\\t%s GB\\n' \"$f\" \"$((b / 1000000000))\"; continue; }; "
                "if [ -d \"$f\" ]; then printf '%s\\t%s entries\\n' \"$f\" \"$(ls -f \"$f\" | wc -l)\"; "
                "else printf '%s\\tfile\\n' \"$f\"; fi; done", timeout=STEP_S)
    return [(a, b) for a, _, b in (x.partition("\t") for x in out.splitlines()) if a]


def delete_tree(root: str, keep: tuple[str, ...] = ()) -> int:
    """Everything under ROOT but KEEP deleted in steps of STEP_S at most (each deletes up to STEP_ENTRIES entries of
    each folder, deepest first through `rm -rf`; one cut short by its bound goes on in the next). The steps taken."""
    ours = (REMOTE_ROOT.startswith("/vol/gpudata/") and (root == REMOTE_ROOT or root.startswith(REMOTE_ROOT + "/"))
            or (LOCAL and root.startswith(LOCAL)))
    if not ours or ".." in root:
        raise Refused(f"{root}: not a tree of ours")
    skip = " ".join(f"! -name {shlex.quote(k)}" for k in keep)
    for step in range(1, MAX_STEPS + 1):
        try:
            left = login(f"cd {root} 2>/dev/null || {{ echo 0; exit 0; }}; "
                         f"for t in $(find . -mindepth 1 -maxdepth 1 {skip} -printf '%f\\n'); do "
                         f"if [ -d \"$t\" ] && [ ! -L \"$t\" ]; then find \"$t\" -mindepth 1 -maxdepth 1 -print0 | "
                         f"head -z -n {STEP_ENTRIES} | xargs -0 -r rm -rf; rmdir \"$t\" 2>/dev/null; "
                         f"else rm -f \"$t\"; fi; done; find . -mindepth 1 -maxdepth 1 {skip} | wc -l",
                         timeout=STEP_S).split()
        except Unreachable:  # the step ran out of its bound: the next goes on
            continue
        if left and left[-1] == "0":
            return step
    raise Refused(f"{root}: still not empty after {MAX_STEPS} steps")


def empty(yes: bool) -> str:
    """The workspace deleted whole (MANIFEST kept) when nothing uses it; without YES, what would go."""
    why = busy(REMOTE_ROOT)
    listing = "\n".join(f"  {name}: {size}" for name, size in entries(REMOTE_ROOT))
    if why:
        return f"{REMOTE_ROOT} is in use, nothing deleted: " + "; ".join(why) + f"\n{listing}"
    if not yes:
        return f"would delete everything under {REMOTE_ROOT} (`tools/compute empty --yes`):\n{listing}"
    steps = delete_tree(REMOTE_ROOT, keep=("MANIFEST",))
    forget()
    unmark_all()  # the runtime went with it: the next run puts it back
    return f"{REMOTE_ROOT} emptied in {steps} steps"


def used_bytes() -> int:
    """The workspace's size: Ceph's count (no walk); `du` where there is none (the tests' stand-in)."""
    out = login(f"getfattr -n ceph.dir.rbytes --only-values {REMOTE_ROOT} 2>/dev/null || "
                f"du -sb {REMOTE_ROOT} | cut -f1").split()
    if not out or not out[-1].isdigit():
        raise Unreachable(f"{REMOTE_ROOT}'s size unread")
    return int(out[-1])


def check_room() -> None:
    """Refused, before anything of a new run is written, while the workspace holds more than ROOM_GB after a sweep."""
    used = used_bytes()
    if used <= ROOM_GB * 1e9:
        return
    from tools import compute  # the sweep: compute imports runs, which imports this
    compute.sweep(force=True)
    used = used_bytes()
    if used > ROOM_GB * 1e9:
        raise Refused(f"{REMOTE_ROOT} holds {used / 1e9:.1f} GB of its {QUOTA_GB} GB quota, over the {ROOM_GB} GB a new "
                      "run may start at, even after a sweep: no new run until open runs close (`tools/compute empty` "
                      "says what holds it)")
