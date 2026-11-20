"""Jobs: one command on one remote allocation (a lab machine, a GPU-cluster CPU slot, a cpucluster1 allocation).

A job is a folder, REMOTE_JOBS/ID, ID's first word its backend. Its files are job.sh's interface (change them only
with it): `commit` (the hash), `code.bundle` (a git bundle of that commit and main; or, beside `base.bundle`, a bundle
of main, only what the commit adds to it, absent when main holds the commit), `argv` (the command's words,
each ended by NUL), `with.tgz` (gzip'd tar of files the commit lacks, unpacked into the checkout; absent when none),
`env` (`NAME VALUE` lines job.sh exports; PATH's go first on PATH), `kit` (the engine's jars, a line each; a kit job's
only), `exit` (empty until the job has ended for good, then its code), `log` (every attempt's output), `out/` (the
command's $OUT), `backend-id` (Slurm's job id, or `SCHEDD CLUSTER` for Condor), `cancelled` (cancel asked its end),
`run` (the run a piece serves, tools/runs.py). The bundles and `with.tgz` go once the job has ended for good.

Results come back to the worktree's runs/ID (gitignored).
"""
from __future__ import annotations

import hashlib
import io
import os
import re
import secrets
import shlex
import subprocess
import sys
import tarfile
import tempfile
import time
from collections.abc import Callable
from dataclasses import dataclass, field, replace
from pathlib import Path

from tools import backends as B
from tools import kit, provision, remote
from tools.remote import (CPU_HOST, ESC, LOCAL, REMOTE_CAS, REMOTE_JOBS, REMOTE_ROOT, SLURM_HOSTS, SUBMIT, CONDOR,
                          Refused, Unreachable, forget, login, login_into, submit_host)

REPO = Path(__file__).resolve().parent.parent
LOCAL_RUNS = REPO / "runs"
WHERES = (*B.WHERES, *(("local",) if LOCAL else ()))  # local: tests only, a job run here by bash
# a second's ids differ by 6 hex digits (4 until 19 Nov, still read): two runs placing 40 pieces each in one second
# drew the same id (an mkdir on the volume refused the whole placement)
ID_TAIL = r"-\d{4}-\d{6}-[0-9a-f]{4,6}"
JOB_ID = rf"({'|'.join(WHERES)}){ID_TAIL}"
HELD, ENDED_UNRECORDED = 3, 4  # wait's and logs -f's codes for a held job and one that ended with no exit file
# between two reads of a job being waited for: light on the login nodes (BC26_POLL_S: tests' stand-in remote only)
POLL_S = float(os.environ.get("BC26_POLL_S", "30")) if LOCAL else 30.0
# a job its backend no longer lists, whose exit file a login node may not see yet (NFS caching); none here in tests
GONE_GRACE_S = 0 if LOCAL else 120
INPUTS = ("base.bundle", "code.bundle", "with.tgz")
LOG_CAP = 64 << 20  # bytes of a job's output kept in its log: a job's writes on the volume are bounded
START_LINE = re.compile(r"^== .* start on ", re.M)


# ---- the code a job runs ----
@dataclass(frozen=True)
class Code:
    commit: str
    subject: str
    bundle: bytes


def git(repo: Path, *args: str, env: dict[str, str] | None = None) -> bytes:
    p = subprocess.run(["git", "-C", str(repo), *args], capture_output=True, env=env)
    if p.returncode:
        raise Refused(f"git {' '.join(args)}: exit {p.returncode}: {p.stderr.decode(errors=ESC).strip()[-300:]}")
    return p.stdout


def bundle(repo: Path, ref: str) -> bytes:
    branches = git(repo, "branch", "--list", "main").strip()
    return git(repo, "bundle", "create", "-", ref, *(["main"] if branches else []))


def head(repo: Path = REPO) -> Code:
    """HEAD, refused while the tree has uncommitted or untracked files: a job runs a commit, never a tree it can't
    see."""
    dirty = git(repo, "status", "--porcelain").decode(errors=ESC).splitlines()
    if dirty:
        raise Refused("uncommitted or untracked files, which a job wouldn't run (it runs HEAD): commit them, or use "
                      "tools/run, which ships the working tree:\n  " + "\n  ".join(dirty[:10]))
    commit, subject = git(repo, "log", "-1", "--format=%H%n%s", "HEAD").decode(errors=ESC).splitlines()
    return Code(commit, subject, bundle(repo, "HEAD"))


def snapshot(repo: Path = REPO) -> Code:
    """The working tree as a commit whose parent is HEAD (tree_commit), bundled; HEAD itself when the tree is
    clean."""
    t = tree_commit(repo)
    return Code(t.commit, t.subject, t.bundle())


@dataclass(frozen=True)
class Tree:
    """The working tree as a commit (HEAD when clean), not yet bundled: KEY names its bundle's content (the commit and
    main's), so a bundle is made and shipped once a content (ship_cas)."""
    repo: Path
    commit: str
    subject: str
    main: str

    @property
    def key(self) -> str:
        return hashlib.sha256(f"{self.commit} {self.main}".encode()).hexdigest()[:24]

    def bundle(self) -> bytes:
        ref = f"refs/bc26-run/{secrets.token_hex(4)}"
        git(self.repo, "update-ref", ref, self.commit)
        try:
            return bundle(self.repo, ref)
        finally:
            git(self.repo, "update-ref", "-d", ref)

    @property
    def in_main(self) -> bool:
        """Main holds the commit: base.bundle alone carries it."""
        return bool(self.main) and subprocess.run(["git", "-C", str(self.repo), "merge-base", "--is-ancestor",
                                                   self.commit, self.main], capture_output=True).returncode == 0

    def thin(self) -> bytes:
        """A bundle of what the commit adds to main (beside base()'s: 35 MB of main a run became a few KB)."""
        ref = f"refs/bc26-run/{secrets.token_hex(4)}"
        git(self.repo, "update-ref", ref, self.commit)
        try:
            return git(self.repo, "bundle", "create", "-", ref, "--not", self.main)
        finally:
            git(self.repo, "update-ref", "-d", ref)

    def base(self) -> bytes:
        """A bundle of main, shared by every run made while main is this commit (named by it)."""
        return git(self.repo, "bundle", "create", "-", "refs/heads/main")


def tree_commit(repo: Path = REPO) -> Tree:
    """The working tree as a commit whose parent is HEAD, made with a copy of the index (nothing in the repo changes
    but loose objects); HEAD itself when the tree is clean. The same tree on the same HEAD makes the same commit (its
    author, committer and dates fixed: HEAD's date), so an unchanged tree ships nothing again."""
    main = git(repo, "rev-parse", "--verify", "-q", "refs/heads/main^{commit}").decode().strip() if git(
        repo, "branch", "--list", "main").strip() else ""
    if not git(repo, "--no-optional-locks", "status", "--porcelain").strip():
        commit, subject = git(repo, "log", "-1", "--format=%H%n%s", "HEAD").decode(errors=ESC).splitlines()
        return Tree(repo, commit, subject, main)
    with tempfile.TemporaryDirectory() as tmp:
        index = Path(tmp) / "index"
        real = Path(git(repo, "rev-parse", "--path-format=absolute", "--git-path", "index").decode().strip())
        if real.exists():
            index.write_bytes(real.read_bytes())
        env = os.environ | {"GIT_INDEX_FILE": str(index)}
        git(repo, "add", "-A", env=env)
        tree = git(repo, "write-tree", env=env).decode().strip()
    when = git(repo, "log", "-1", "--format=%ct", "HEAD").decode().strip() + " +0000"
    who = {"GIT_AUTHOR_NAME": "tools/run", "GIT_AUTHOR_EMAIL": "tools-run@bc26", "GIT_AUTHOR_DATE": when,
           "GIT_COMMITTER_NAME": "tools/run", "GIT_COMMITTER_EMAIL": "tools-run@bc26", "GIT_COMMITTER_DATE": when}
    commit = git(repo, "commit-tree", tree, "-p", "HEAD", "-m", "tools/run: the working tree",
                 env=os.environ | who).decode().strip()
    return Tree(repo, commit, "the working tree", main)


def content_key(repo: Path, paths: list[str]) -> str:
    """PATHS' content (their names, modes and bytes; not their times), as a hash: shipped files are stored under it."""
    h = hashlib.sha256()

    def add(p: Path) -> None:
        rel = str(p.relative_to(repo))
        if p.is_symlink():
            h.update(f"L {rel} {os.readlink(p)}\n".encode())
        elif p.is_dir():
            h.update(f"D {rel}\n".encode())
            for c in sorted(p.iterdir()):
                add(c)
        else:
            h.update(f"F {rel} {p.stat().st_mode & 0o777:o} ".encode())
            with open(p, "rb") as f:
                h.update(hashlib.file_digest(f, "sha256").digest())
    for rel in sorted(paths):
        add(repo / rel)
    return h.hexdigest()[:24]


def ship_cas(blobs: dict[str, Callable[[], bytes]], hosts: tuple[str, ...]) -> list[str]:
    """Each blob of BLOBS (its name in REMOTE_CAS, named by its content -> what makes its bytes) on the volume: one
    login finds those there (their times refreshed, for the sweep), the rest are made here and uploaded under a
    temporary name, renamed in whole. The names uploaded."""
    names = sorted(blobs)

    def there(ns: list[str]) -> set[str]:
        return set(login(f"mkdir -p {REMOTE_CAS} && cd {REMOTE_CAS} && for f in {' '.join(ns)}; do "
                         f"[ -s \"$f\" ] && touch \"$f\" && echo \"$f\"; done; true", hosts=hosts).split())
    have = there(names)
    sent = []
    for n in (n for n in names if n not in have):
        # one upload of a blob at a time on this machine (60 runs made at once each sent main's 35 MB): the one
        # that waited finds it there
        with remote.locked(remote.CLIENT / "ship" / n, remote.SLOT_WAIT_S):
            if n in there([n]):
                continue
            login(f't=$(mktemp {REMOTE_CAS}/.{n}.XXXXXX) && cat > "$t" && mv -f "$t" {REMOTE_CAS}/{n}', blobs[n](),
                  hosts)
            sent.append(n)
    return sent


def ship(paths: list[str], repo: Path = REPO, cwd: Path | None = None) -> list[str]:
    """--with: files and folders of the checkout the commit doesn't hold (a build's output), as paths from REPO. A
    tracked one is refused: the commit ships it already."""
    out: list[str] = []
    for spec in paths:
        p = ((cwd or Path.cwd()) / spec).resolve()
        if not p.is_relative_to(repo) or p == repo:
            raise Refused(f"--with {spec}: not inside this checkout ({repo})")
        if not p.exists():
            raise Refused(f"--with {spec}: no such file")
        rel = str(p.relative_to(repo))
        if git(repo, "ls-files", "--", rel).strip():
            raise Refused(f"--with {spec}: tracked, so the commit ships it already")
        out.append(rel)
    return out


# ---- a job ----
@dataclass(frozen=True)
class Job:
    id: str
    where: str
    dir: str


def new_id(where: str) -> str:
    return f"{where}-{time.strftime('%m%d-%H%M%S')}-{secrets.token_hex(3)}"


def job(job_id: str) -> Job:
    """The job an id names. Ids reach shell commands, so nothing but this shape gets through."""
    m = re.fullmatch(JOB_ID, job_id)
    if not m:
        raise Refused(f"{job_id!r} isn't a job id (they look like condor-1118-153000-3fa2)")
    return Job(job_id, m[1], f"{REMOTE_JOBS}/{job_id}")


def hosts_of(where: str) -> tuple[str, ...]:
    """The login nodes that reach a job's backend: cpucluster1's for its jobs, the GPU cluster's for the rest."""
    return (CPU_HOST,) if where == "cpucluster1" else SLURM_HOSTS


def job_files(code: Code, argv: list[str], with_tgz: bytes, with_kit: bool, extra: dict[str, bytes] | None = None,
              repo: Path = REPO) -> dict[str, bytes]:
    """A job folder's files at submission. `exit` and `log` exist from the start, empty: a name first made on a node
    can stay unseen on a login node for a minute (NFS caches its absence)."""
    files = {"job.sh": (Path(__file__).parent / "job.sh").read_bytes(), "commit": f"{code.commit}\n".encode(),
             "code.bundle": code.bundle, "argv": b"".join(a.encode() + b"\0" for a in argv),
             "env": provision.env_file(), "exit": b"", "log": b""}
    if with_tgz:
        files["with.tgz"] = with_tgz
    if with_kit:
        files["kit"] = provision.kit_file(repo)
    return files | (extra or {})


def folder_tar(files: dict[str, bytes]) -> bytes:
    """FILES (path -> bytes) as one tar, each file 0600."""
    buf = io.BytesIO()
    with tarfile.open(fileobj=buf, mode="w") as tar:
        for name, data in files.items():
            info = tarfile.TarInfo(name)
            info.size, info.mode, info.mtime = len(data), 0o600, int(time.time())
            tar.addfile(info, io.BytesIO(data))
    return buf.getvalue()


def private_root(hosts: tuple[str, ...]) -> None:
    """REMOTE_ROOT, made 0700 when missing; Refused while its group or others have any access to it."""
    mode = login(f"mkdir -p {REMOTE_ROOT} && chmod go-rwx {REMOTE_ROOT} 2>/dev/null; stat -c %a {REMOTE_ROOT}",
                 hosts=hosts).strip()
    if not re.fullmatch(r"[0-7]{3,4}", mode) or int(mode, 8) & 0o077:
        raise Refused(f"{REMOTE_ROOT} is mode {mode}, open to other DoC users, and every job's code lies under it")


def unpack_cmd(j: Job) -> str:
    """Makes the job's folder from the tar on stdin; `mkdir` of the folder itself fails if it exists, so a login
    retried on the other node never submits twice."""
    return f"mkdir -p {REMOTE_JOBS} && mkdir -m 700 {j.dir} && tar -xf - -C {j.dir}"


def checked_time(time_s: int | None, where: str) -> int:
    if time_s is None:
        raise Refused(f"{where} needs --time: the job's own estimate (90m, 2h). SIGUSR1 comes {B.USR1_LEAD_S} s "
                      "before it; a job that saves a checkpoint then and exits 0 requeues itself")
    if time_s <= B.USR1_LEAD_S:
        raise Refused(f"--time must exceed the {B.USR1_LEAD_S} s the checkpoint signal comes before it")
    return time_s


def sbatch(j: Job, files: dict[str, bytes], args: list[str], hosts: tuple[str, ...], pre: str = "") -> str:
    """Unpacks the job (then PRE, ` && ...` commands: a run's files linked in) and queues it in one login,
    `--test-only` first: a job Slurm says it never starts isn't queued, and a failed sbatch removes the folder. Slurm's
    job id."""
    if any(a.startswith("-p") or a.startswith("--partition") for a in args):
        parts = [args[i + 1] for i, a in enumerate(args) if a == "-p"] + [a.split("=", 1)[1] for a in args
                                                                          if a.startswith("--partition=")]
        if len(parts) != 1 or "," in parts[0]:
            raise Refused(f"one partition a job (DoC's rule), not {parts}")
    cmd = ["sbatch", f"--job-name=bc26-{j.id}", *args, f"--output={j.dir}/log", f"--chdir={j.dir}",
           f"{j.dir}/job.sh", j.dir, j.where]
    probe, queue = shlex.join([cmd[0], "--test-only", *cmd[1:]]), shlex.join([cmd[0], "--parsable", *cmd[1:]])
    never = ('case "$a" in *" to start at "*) ;; *) printf \'slurm says it would not start: %s\\n\' '
             '"$(printf \'%s\\n\' "$a" | grep -v \'\\*\\*\\*\' | tail -n 3 | tr \'\\n\' \' \')" >&2; false ;; esac')
    sid = login(f"{unpack_cmd(j)}{pre} && {{ a=$({probe} 2>&1); {never} && {queue} > {j.dir}/backend-id || "
                f"{{ rm -rf {j.dir}; false; }}; }} && cat {j.dir}/backend-id", folder_tar(files), hosts).strip()
    forget()  # our queue changed: the shared scheduler reads are stale
    return sid


def submit_slurm(j: Job, files: dict[str, bytes], slot: B.CpuSlot, time_s: int) -> str:
    sid = sbatch(j, files, B.cpu_slot_args(slot, checked_time(time_s, "Slurm")), SLURM_HOSTS)
    say(f"compute: {j.id}: Slurm job {sid} on {slot.partition}: {slot.node}, {slot.cpus} CPUs, {slot.mem_mb} MB, "
        f"one {slot.card} card")
    return sid


def submit_cpucluster(j: Job, files: dict[str, bytes], part: str, cpus: int, mb_per_cpu: int, time_s: int) -> str:
    args = ["-p", part, "--ntasks=1", f"--cpus-per-task={cpus}", f"--mem-per-cpu={mb_per_cpu}M",
            *B.checkpointed(checked_time(time_s, "cpucluster1"))]
    sid = sbatch(j, files, args, (CPU_HOST,))
    say(f"compute: {j.id}: cpucluster1 job {sid} on {part}: {cpus} CPUs, {mb_per_cpu} MB a CPU")
    return sid


def submit_condor(jobs: list[tuple[Job, dict[str, bytes], str]], shared: dict[str, str] | None = None
                  ) -> list[str | None]:
    """Each (job, files, submit description) in one login that makes every folder (SHARED: file name -> a path
    already on the volume, hard-linked in), then their condor_submits on the submit host, eight at a time. Each
    job's cluster, or None where condor_submit failed (its folder left with no backend-id: it never runs). When the
    submit host gives no answer, none was submitted: their folders are removed and each is None."""
    if not jobs:
        return []
    tar = folder_tar({f"{j.id}/{name}": data for j, f, sub in jobs
                      for name, data in (f | {"job.sub": sub.encode()}).items()})
    links = "".join(f" && ln {src} {j.dir}/{name}" for j, _, _ in jobs for name, src in (shared or {}).items())
    login(f"mkdir -p {REMOTE_JOBS} && mkdir -m 700 {' '.join(j.dir for j, _, _ in jobs)} && "
          f"tar -xf - -C {REMOTE_JOBS}{links}", tar, SLURM_HOSTS)
    subs = " ".join(f"{j.dir}/job.sub" for j, _, _ in jobs)
    try:
        out = submit_host(f"for f in {subs}; do {{ if o=$(condor_submit -terse \"$f\" 2>&1); then echo \"C ${{o%%.*}}\"; "
                          f"else echo \"F $(printf %s \"$o\" | tr '\\n' ' ' | cut -c1-300)\"; fi; }} > \"$f.said\" & "
                          f"[ \"$(jobs -pr | wc -l)\" -lt 8 ] || wait -n; done; wait; "
                          f"for f in {subs}; do cat \"$f.said\"; rm -f \"$f.said\"; done")
    except Unreachable as e:  # no condor_submit ran: nothing is queued
        say(f"compute: no Condor job submitted ({e})")
        login(f"rm -rf {' '.join(j.dir for j, _, _ in jobs)}", hosts=SLURM_HOSTS)
        return [None] * len(jobs)
    except Refused as e:
        return reconcile(jobs, str(e))
    finally:
        forget()  # our queue changed: the shared scheduler reads are stale
    said = [line.split(" ", 1) for line in out.splitlines() if line[:2] in ("C ", "F ")]
    if len(said) != len(jobs):
        raise Refused(f"condor_submit answered {len(said)} of {len(jobs)} jobs: {out.strip()[-300:]}")
    got: list[str | None] = []
    for (j, _, _), (kind, rest) in zip(jobs, said, strict=True):
        if kind == "C" and rest.strip().isdigit():
            got.append(rest.strip())
        else:
            say(f"compute: {j.id}: condor_submit failed: {rest.strip()}")
            got.append(None)
    ok = [(j, c) for (j, _, _), c in zip(jobs, got, strict=True) if c is not None]
    if ok:
        login(" && ".join(f"echo {SUBMIT} {c} > {j.dir}/backend-id" for j, c in ok), hosts=SLURM_HOSTS)
    return got


def reconcile(jobs: list[tuple[Job, dict[str, bytes], str]], why: str) -> list[str | None]:
    """After a submit whose answer was lost (WHY): each job Condor holds gets its backend-id, read from the queue
    through a login node; every other folder is removed, its job never queued."""
    say(f"compute: condor_submit's answer was lost ({why}); reading the queue")
    q = login(f"{CONDOR}; condor_q -name {SUBMIT} \"$USER\" -af JobBatchName ClusterId", hosts=SLURM_HOSTS)
    queued = {f[0].removeprefix("bc26-"): f[1] for f in (line.split() for line in q.splitlines()) if len(f) == 2}
    got = [queued.get(j.id) for j, _, _ in jobs]
    cmds = [f"echo {SUBMIT} {c} > {j.dir}/backend-id" if c else f"rm -rf {j.dir}" for (j, _, _), c in
            zip(jobs, got, strict=True)]
    login(" && ".join(cmds), hosts=SLURM_HOSTS)
    for (j, _, _), c in zip(jobs, got, strict=True):
        say(f"compute: {j.id}: " + (f"Condor job {c}" if c else "never queued; its folder removed"))
    return got


def submit_local(j: Job, files: dict[str, bytes], shared: dict[str, str] | None = None) -> str:
    """Tests only: job.sh run here, detached, its process group the job."""
    login(unpack_cmd(j) + "".join(f" && ln {src} {j.dir}/{name}" for name, src in (shared or {}).items()),
          folder_tar(files))
    p = subprocess.Popen(["taskset", "-c", "0,1", "bash", f"{j.dir}/job.sh", j.dir, "local"], stdin=subprocess.DEVNULL,
                         stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, start_new_session=True)
    Path(j.dir, "backend-id").write_text(f"local {p.pid}\n")
    return str(p.pid)


def say(msg: str) -> None:
    print(msg, file=sys.stderr, flush=True)


# ---- `tools/compute run` ----
@dataclass(frozen=True)
class Place:
    """Where a job goes, chosen before anything of it is written: a Slurm slot, a cpucluster1 partition and size, or
    Condor (a whole machine of at least CPUS cores and MEM_MB MiB)."""
    where: str
    cpus: int
    mem_mb: int
    time_s: int | None
    slot: B.CpuSlot | None = None
    partition: str = ""
    note: str = ""


def place(where: str, cpus: int, time_s: int | None, mem_mb: int | None) -> Place:
    """Where a job of at least CPUS CPUs and MEM_MB MB can go on WHERE, or Refused."""
    if where == "slurm":
        est = checked_time(time_s, "Slurm")
        per = max(B.CPU_JOB_MIN_MEM_MB, -(-(mem_mb or 0) // cpus))
        slots = B.slurm_starts_now(B.read_slurm(), cpus, cpus, est, per)
        if not slots:
            raise Refused(f"--cpu {cpus}: no GPU-cluster node has a free card and {cpus} CPUs with their memory free "
                          "within our caps now (`tools/compute status`)")
        return Place(where, slots[0].cpus, slots[0].mem_mb, est, slot=slots[0])
    if where == "cpucluster1":
        est = checked_time(time_s, "cpucluster1")
        c = B.read_cpucluster()
        if not c.partitions:
            raise Refused("no cpucluster1 node takes jobs now")
        per = -(-mem_mb // cpus) if mem_mb else max(p.def_mem_per_cpu_mb for p in c.partitions.values())
        best = B.cpucluster_best(c, cpus, per, est)
        if best is not None:
            return Place(where, cpus, cpus * per, est, partition=best[0])
        # "ever": an empty node of a partition and our caps with none of them in use; today's use only delays it
        unused = replace(c, cap=B.Limit(c.cap.cap, 0), mem=B.Limit(c.mem.cap, 0))
        big = [p for p in c.partitions.values() if (p.max_time_s is None or est <= p.max_time_s)
               and B.cpu_free(unused, B.CpuNode(True, p.node_cpus, p.node_cpus, p.node_cpus * per, 0), per) >= cpus]
        if not big:
            raise Refused(f"--cpu {cpus}: one job runs on one node, and no cpucluster1 node can ever give a job of "
                          f"ours {cpus} CPUs at {per} MB each within our caps (`tools/compute status`)")
        part = max(big, key=lambda p: p.idle_cpus)
        full = [f"our CPUs {c.cap.used}/{c.cap.cap}" if c.cap.cap is not None and c.cap.cap - c.cap.used < cpus else "",
                f"our memory {c.mem.used}/{c.mem.cap} MB" if c.mem.cap is not None and c.mem.cap - c.mem.used < cpus * per
                else ""]
        why = ", ".join(x for x in full if x) or "no node has the CPUs and memory free"
        return Place(where, cpus, cpus * per, est, partition=part.name,
                     note=f"no node can start it now ({why}): it waits in {part.name}'s queue")
    if where == "condor":
        return Place(where, cpus, mem_mb or 0, time_s)
    if where == "local":
        return Place(where, cpus, mem_mb or 0, time_s)
    raise Refused(f"--where: one of {', '.join(WHERES)}")


def submit(j: Job, files: dict[str, bytes], p: Place) -> str:
    """J's folder made and its job queued where P says; the backend's id."""
    if p.where == "slurm":
        assert p.slot is not None and p.time_s is not None
        return submit_slurm(j, files, p.slot, p.time_s)
    if p.where == "cpucluster1":
        assert p.time_s is not None
        if p.note:
            say(f"compute: {j.id}: {p.note}")
        return submit_cpucluster(j, files, p.partition, p.cpus, p.mem_mb // p.cpus, p.time_s)
    if p.where == "condor":
        got = submit_condor([(j, files, B.condor_sub(j.dir, j.id, p.cpus, p.mem_mb, None, p.time_s))])[0]
        if got is None:
            raise Refused("condor_submit failed (above)")
        return got
    return submit_local(j, files)


def run(where: str, cpus: int, time_s: int | None, argv: list[str], ships: list[str], with_kit: bool,
        mem_mb: int | None, build: bool = True, repo: Path = REPO) -> str:
    """ARGV on WHERE; its job id printed first on stdout. CPUS: what it needs (Condor: a whole machine of at least that
    many; Slurm and cpucluster1: that many on one node, or, on Slurm, the next count DoC's lua gives whole).
    MEM_MB: the least memory it needs. WITH_KIT: build/classes compiled and shipped, the engine's jars named."""
    if not argv:
        raise Refused("no command after --")
    if where not in WHERES:
        raise Refused(f"--where: one of {', '.join(WHERES)}")
    if argv[0].removeprefix("./") == "gradlew":
        raise Refused("a job runs no Gradle: ship build/classes with --kit and run java (tools/kit.py)")
    if with_kit and build:
        kit.compile_classes(repo)
    code = head(repo)
    paths = ship(ships, repo) + ([kit.CLASSES] if with_kit and kit.CLASSES not in ships else [])
    with_tgz = provision.tar_paths(repo, paths) if paths else b""
    hosts = hosts_of(where)
    if where != "local":
        private_root(hosts)
        provision.ensure(hosts, repo, with_kit)
    p = place(where, cpus, time_s, mem_mb)
    j = job(new_id(where))
    files = job_files(code, argv, with_tgz, with_kit, repo=repo)
    print(j.id, flush=True)  # first, so the id is known even if the connection drops during the submit
    say(f"compute: {j.id} runs commit {code.commit[:12]} ({code.subject})"
        + (f" with {len(with_tgz) / 1e6:.1f} MB of --with files" if with_tgz else ""))
    submit(j, files, p)
    return j.id


# ---- reading jobs ----
@dataclass(frozen=True)
class State:
    phase: str  # queued, running, held, ended, gone (no folder), unknown (no answer)
    detail: str
    exit: int | None = None
    flags: frozenset[str] = frozenset()  # started, cancelled, queued (its backend took it)
    peak: int | None = None  # the job's memory peak (bytes), where its cgroup was its own


FOLDER_SH = r"""for j in "$@"; do d=JOBS/$j
if [ -d "$d" ]; then e=$(head -c 20 "$d/exit" 2>/dev/null | tr -d '\n'); b=$(cat "$d/backend-id" 2>/dev/null)
f=; [ -e "$d/cancelled" ] && f=${f}c; grep -q '^== .* start on ' "$d/log" 2>/dev/null && f=${f}s
p=$(sed -n 's/^peak //p' "$d/memory" 2>/dev/null)
printf 'J\t%s\t1\t%s\t%s\t%s\t%s\n' "$j" "$e" "$b" "$f" "$p"; else printf 'J\t%s\t0\t\t\t\t\n' "$j"; fi; done"""


def read_states(ids: list[str]) -> dict[str, State]:
    """Every job of IDS: its folder's record and its backend's word, in one login a cluster (now: a follower reads
    them through tools/view.py instead)."""
    out: dict[str, State] = {}
    for hosts in (SLURM_HOSTS, (CPU_HOST,)):
        js = [job(i) for i in ids if hosts_of(job(i).where) == hosts]
        if js:
            out |= parse_states(js, B.sections(login(states_cmd(js) + "true", hosts=hosts)))
    return out


def states_cmd(js: list[Job], queues: bool = False) -> str:
    """The command reading JS's folders and our queue of each of their schedulers (QUEUES: of every scheduler the
    login node reaches, whatever JS holds), in parse_states's sections."""
    script = FOLDER_SH.replace("JOBS", REMOTE_JOBS)
    cmd = f"echo ==folders; set -- {' '.join(j.id for j in js)}; {script}; "
    slurm = queues or any(j.where in B.SLURMS for j in js)
    condor = queues or any(j.where == "condor" for j in js)
    local = [j for j in js if j.where == "local"]
    # every job of ours in one listing each (a list of ids would fail whole on one Slurm has forgotten)
    if slurm:
        cmd += "echo ==slurm; squeue -u \"$USER\" -h -o '%i|%T|%r|%N'; echo ==slurm_rc; echo $?; "
    if condor:
        cmd += (f"echo ==condor; {CONDOR}; condor_q -name {SUBMIT} \"$USER\" -af ClusterId JobStatus RemoteHost "
                f"{B.CONDOR_HELD}; echo ==condor_rc; echo $?; ")
    if local:
        cmd += ("echo ==local; for j in " + " ".join(j.id for j in local) + f"; do set -- $(cat {REMOTE_JOBS}/$j/"
                "backend-id 2>/dev/null); [ -n \"${2:-}\" ] && kill -0 \"$2\" 2>/dev/null && echo \"$2\"; done; ")
    return cmd


def parse_states(js: list[Job], s: dict[str, list[str]]) -> dict[str, State]:
    """JS's states from states_cmd's sections S."""
    squeue = {f[0]: f for f in (line.split("|") for line in s.get("slurm", [])) if len(f) >= 4}
    slurm_ok = s.get("slurm_rc", ["0"]) == ["0"]
    condor_ok = s.get("condor_rc", ["0"]) == ["0"]
    cq = {f[0]: f for f in ((line.split(maxsplit=4) + [""] * 5)[:5] for line in s.get("condor", [])) if f[0]}
    alive = set(s.get("local", []))
    wanted = {j.id for j in js}
    out: dict[str, State] = {}
    for line in s.get("folders", []):
        f = line.split("\t")
        if len(f) < 7 or f[0] != "J" or f[1] not in wanted:
            continue
        jid, exists, ex, bid, flags, peak = f[1], f[2], f[3], f[4], f[5], f[6]
        j = job(jid)
        code = int(ex) if re.fullmatch(r"-?\d+", ex) else None
        fl = frozenset((["cancelled"] if "c" in flags else []) + (["started"] if "s" in flags else [])
                       + (["queued"] if bid.strip() else []))
        pk = int(peak) if peak.isdigit() else None
        if exists != "1":
            out[jid] = State("gone", "no job folder")
        elif code is not None:
            out[jid] = State("ended", f"exit {code}", code, fl, pk)
        elif not bid.strip():
            out[jid] = State("ended", "never queued (its submit failed)", None, fl)
        elif j.where in B.SLURMS:
            if not slurm_ok:
                out[jid] = State("unknown", "squeue didn't answer", None, fl)
                continue
            q = squeue.get(bid.strip())
            if q is None:
                out[jid] = State("ended", "gone from Slurm", None, fl)
            else:
                word = "held" if q[1].endswith("_HOLD") or re.search(r"(?i:held)|JobHold", q[2]) else (
                    "running" if q[1] in ("RUNNING", "CONFIGURING", "COMPLETING") else
                    "ended" if q[1] in B.SLURM_ENDED else "queued")
                out[jid] = State(word, " ".join(x for x in (q[1].lower(), "" if q[2] == "None" else q[2], q[3]) if x),
                                 None, fl)
        elif j.where == "condor":
            if not condor_ok:
                out[jid] = State("unknown", "condor_q didn't answer", None, fl)
                continue
            q = cq.get(bid.split()[-1])
            if q is None:
                out[jid] = State("ended", "gone from Condor", None, fl)
                continue
            st = B.CONDOR_STATE.get(int(q[1]), q[1]) if q[1].isdigit() else q[1]
            if st == "held":
                word = "queued" if q[3] == "true" else "held"  # the one hold Condor releases itself
                out[jid] = State(word, f"held: {q[4]}", None, fl)
            else:
                out[jid] = State({"idle": "queued", "running": "running", "transferring": "running",
                                  "suspended": "running"}.get(st, "ended"),
                                 st + (f" on {q[2].split('@')[-1]}" if q[2] not in ("undefined", "") else ""), None, fl)
        else:
            pid = bid.split()[-1]
            out[jid] = State("running", "here", None, fl) if pid in alive else State("ended", "its process ended",
                                                                                         None, fl)
    return out


def read_log(j: Job, offset: int) -> bytes:
    return login(f"tail -c +{offset + 1} {j.dir}/log 2>/dev/null; true", hosts=hosts_of(j.where)).encode(errors=ESC)


@dataclass
class Waiter:
    """Ended for good: an exit code read, or gone from its backend for GONE_GRACE_S with none (an exit file written
    on a node can stay unseen on a login node for a while)."""
    gone_since: dict[str, float] = field(default_factory=dict[str, float])

    def settle(self, jid: str, st: State, now: float) -> State | None:
        """ST once it is final, else None."""
        if st.phase == "ended" and st.exit is None:
            first = self.gone_since.setdefault(jid, now)
            return st if now - first >= GONE_GRACE_S else None
        self.gone_since.pop(jid, None)
        return st if st.phase in ("ended", "held", "gone") else None


def code_of(st: State) -> int:
    if st.phase == "held":
        return HELD
    return st.exit if st.exit is not None else ENDED_UNRECORDED


def describe(jid: str, st: State) -> str:
    peak = f", memory peak {st.peak / 1e9:.2f} GB" if st.peak else ""
    if st.phase == "ended" and st.exit is not None:
        return f"{jid}: ended, exit {st.exit}{peak}"
    if st.phase == "ended":
        return f"{jid}: ended without an exit file ({st.detail}{'; cancelled' if 'cancelled' in st.flags else ''})"
    return f"{jid}: {st.phase}, {st.detail}"


def logs(job_id: str, lines: int, follow: bool, poll_s: float = POLL_S) -> int:
    j = job(job_id)
    st = read_states([j.id])[j.id]
    if st.phase == "gone":
        raise Refused(f"no job folder {j.dir}")
    data = read_log(j, 0)
    text = data.decode(errors="replace")
    print(describe(j.id, st))
    sys.stdout.write("\n".join(text.splitlines()[-lines:]) + "\n" if lines and text else text)
    sys.stdout.flush()
    if not follow:
        return 0
    offset, w = len(data), Waiter()
    final = w.settle(j.id, st, time.time())
    while final is None:
        time.sleep(poll_s)
        try:
            st = read_states([j.id])[j.id]
            data = read_log(j, offset)
        except Refused as e:  # a dropped connection says nothing about the job: keep listening
            say(f"compute: {e}; trying again in {poll_s:.0f} s")
            continue
        sys.stdout.write(data.decode(errors="replace"))
        sys.stdout.flush()
        offset += len(data)
        final = w.settle(j.id, st, time.time())
    print(describe(j.id, final))
    return code_of(final)


def pull(job_id: str, dest: Path | None = None) -> Path:
    """The job folder without its inputs, to DEST (default the worktree's runs/ID)."""
    j = job(job_id)
    out = dest or LOCAL_RUNS / j.id
    leave = " ".join(f"--exclude=./{n}" for n in (*INPUTS, "job.sh", "argv", "env", "kit", "job.sub"))
    login_into(f"tar -C {j.dir} {leave} -cf - .", out, hosts_of(j.where))
    return out


def cancel(job_id: str) -> str:
    """Ends the job for good, `cancelled` written first, so a reader that sees it end knows why."""
    return cancel_many([job_id])[job_id]


# condor_rm's answers. For one cluster: "All jobs in cluster N have been marked for removal", or "Couldn't
# find/remove all jobs in cluster N" (it left the queue: nothing to cancel). For several it matches a constraint,
# "(ClusterId == A || ClusterId == B ...)", and answers once for all of them: "Couldn't find/remove all jobs matching
# constraint (...)" when any had left. Either way none of them is left queued.
CONDOR_DONE = re.compile(r"All jobs (?:in cluster \d+|matching constraint \([^)]*\)) have been marked for removal")
CONDOR_GONE = re.compile(r"Couldn't find/remove all jobs (?:in cluster \d+|matching constraint \([^)]*\))")


def cancel_many(job_ids: list[str]) -> dict[str, str]:
    """Each job ended for good (`cancelled` written first), in one login a cluster and one removal a scheduler:
    condor_rm of every Condor cluster at once, scancel of every Slurm job of a cluster at once. A job the scheduler no
    longer has (condor_rm's "Couldn't find/remove", scancel's "Invalid job id") counts as cancelled. What each one
    says, by id."""
    out: dict[str, str] = {}
    by_hosts: dict[tuple[str, ...], list[Job]] = {}
    for jid in dict.fromkeys(job_ids):
        j = job(jid)
        by_hosts.setdefault(hosts_of(j.where), []).append(j)
    for hosts, js in by_hosts.items():
        listing = login("; ".join(f"touch {j.dir}/cancelled && echo {j.id} $(cat {j.dir}/backend-id 2>/dev/null)"
                                  for j in js), hosts=hosts)
        bids = {x.split()[0]: x.split()[1:] for x in listing.splitlines() if x.split()}
        condor: dict[str, dict[int, str]] = {}
        slurm: dict[int, str] = {}
        for j in js:
            bid = bids.get(j.id, [])
            if not bid:
                out[j.id] = f"{j.id}: never queued"
            elif j.where in B.SLURMS:
                slurm[int(bid[0])] = j.id
            elif j.where == "condor":
                condor.setdefault(bid[0], {})[int(bid[1])] = j.id
            else:
                try:
                    os.killpg(int(bid[-1]), 15)
                except ProcessLookupError:
                    pass
                out[j.id] = f"killed process group {bid[-1]}"
        if slurm:
            said = login(f"scancel {' '.join(map(str, slurm))} 2>&1; true", hosts=hosts)
            gone = {int(x) for x in re.findall(r"job id (\d+): Invalid job id", said)}
            for sid, jid in slurm.items():
                out[jid] = f"Slurm job {sid} " + ("already gone" if sid in gone else "cancelled")
        for schedd, clusters in condor.items():
            said = submit_host(f"condor_rm -name {shlex.quote(schedd)} {' '.join(map(str, clusters))} 2>&1; true")
            done = {int(x) for m in CONDOR_DONE.findall(said) for x in re.findall(r"\d+", m)}
            gone = {int(x) for m in CONDOR_GONE.findall(said) for x in re.findall(r"\d+", m)}
            for c, jid in clusters.items():
                out[jid] = (f"Condor cluster {c} marked for removal" if c in done else
                            f"Condor cluster {c} removed or already gone" if c in gone else
                            f"Condor cluster {c}: condor_rm said {said.strip()[-300:]!r}")
        login(f"rm -f {' '.join(f'{j.dir}/{n}' for j in js for n in INPUTS)}", hosts=hosts)
    forget()
    return out
