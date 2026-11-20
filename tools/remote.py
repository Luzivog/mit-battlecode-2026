"""How tools/compute reaches DoC: ssh to the login nodes it may use, and one condor command at a time on the Condor
submit host. Nothing else here touches a DoC machine (internals.md, "DoC rules").

Every remote path is under REMOTE_ROOT, ours alone: 0700, written with umask 077. BC26_LOCAL_REMOTE=DIR (tests only)
makes REMOTE_ROOT that folder of this machine and runs each login's command here with bash, so a test runs the real
shell of a job's life without DoC.
"""
from __future__ import annotations

import fcntl
import hashlib
import os
import re
import shlex
import subprocess
import tempfile
import threading
import time
from collections.abc import Callable, Iterator
from contextlib import contextmanager
from pathlib import Path


def load_env(*files: Path) -> None:
    """Each FILE's KEY=VALUE lines into the environment, unless the key is set already: the person's settings
    (`.env.example` names them), kept out of git."""
    for f in files:
        try:
            lines = f.read_text().splitlines()
        except OSError:
            continue
        for line in lines:
            key, sep, value = line.strip().partition("=")
            if sep and key and not key.startswith("#"):
                os.environ.setdefault(key.strip(), value.strip().strip("\"'"))


load_env(Path(__file__).resolve().parents[1] / ".env", Path.home() / "battlecode26-data" / ".env")
LOCAL = os.environ.get("BC26_LOCAL_REMOTE", "")  # tests: a folder here standing in for REMOTE_ROOT
# Our workspace on /vol/gpudata (BC26_WORKSPACE, made by `ws_allocate bc26 365` on gpucluster2; a 55 GB quota), apart
# from every other project's: every job's code bundle lies under it (a small file operation takes 1-6 ms there;
# /vol/bitbucket is full and takes 250 ms and more, on login nodes and lab machines alike). Unset: nothing remote runs.
REMOTE_ROOT = LOCAL or os.environ.get("BC26_WORKSPACE", "")
REMOTE_JOBS = f"{REMOTE_ROOT}/jobs"
REMOTE_RUNS = f"{REMOTE_ROOT}/runs"
REMOTE_POOL = f"{REMOTE_ROOT}/pool"  # open/RUN: the runs pooled pieces serve; pieces/JOB: each live piece's state
REMOTE_CAS = f"{REMOTE_ROOT}/cas"  # code bundles and shipped files, each stored once under its content's name
RUNTIME = f"{REMOTE_ROOT}/runtime"  # the JDK, the Python and the engine's jars every job uses (provision.py)
DATA = Path.home() / "battlecode26-data"
UMASK = "umask 077; "  # before every remote command: what it creates is ours alone
MAX_ARG = 131_072  # Linux's cap on one argument (MAX_ARG_STRLEN): a longer command goes on ssh's stdin
# The GPU cluster's login nodes: the second when the first can't be reached (a name whose kept connection was already
# tried is skipped: login)
SLURM_HOSTS = ("gpucluster2", "gpucluster3")
CPU_HOST = "cpucluster1.doc.ic.ac.uk"  # cpucluster1's login node
SUBMIT = "ash04.doc.ic.ac.uk"  # the Condor submit host, a lab machine: one login per call, never a kept connection
CONDOR = ("export CONDOR_CONFIG=/vol/condor/pool/doc/config/condor_config.global "
          "PATH=/vol/condor/pool/doc/release/Ubuntu-26.04/X86_64.LINUX/bin:$PATH")
ESC = "surrogateescape"  # a log may hold any bytes: decoded and encoded back exactly


class Refused(Exception):
    """A request the rules refuse, or a backend that can't be reached; the CLI prints it and exits 2."""


class Unreachable(Refused):
    """No login node answered (ssh's 255 from each), so the command may not have run at all."""


# Every call is bounded on both sides: ssh is killed here after its bound and the command runs under `timeout -s KILL`
# there (19 Nov: unbounded reads of a folder that stopped answering hung for 28 minutes, and every tool behind them).
READ_S = 20.0  # a read or a small write; an upload gets 1 s more a MB (UPLOAD_MB_S)
UPLOAD_MB_S = 1.0
BULK_S = 600.0  # a bulk transfer's bound (login_into: a run's results)
SLACK_S = 10.0  # ssh here outlives the remote bound by this much, so the remote side's kill is the one that ends it
MASTER_S = 60.0  # making a kept connection (through the jump host)
# sshd allows 10 sessions a connection (MaxSessions); a session refused made ssh open a connection of its own (93 at
# once on 19 Nov). Every process of ours on this machine holds one of SESSIONS slots (a lock file) a session, at most
# BULK_SESSIONS of them for bulk transfers, and a client can't fall off the kept connection (ssh_argv)
SESSIONS, BULK_SESSIONS, SUBMIT_SESSIONS = 8, 3, 4
SLOT_WAIT_S = 300.0  # how long a call waits its turn for a session (every call holding one is bounded)
CLIENT = Path(os.environ.get("BC26_CLIENT_DIR") or (f"{LOCAL}.client" if LOCAL else DATA / "compute"))
SSH_TAG = os.environ.get("BC26_SSH_TAG", "bc26-ssh")  # the kept connections' socket names (a test uses its own)


def control_path(host: str) -> str:
    """HOST's kept connection's socket, as ssh resolves it with the usual configuration (the jump host included:
    ssh's %C hashes it, so a client whose proxy differs would look for another socket)."""
    if host not in _CONTROL:
        p = subprocess.run(["ssh", "-G", "-o", f"ControlPath={RUN_DIR}/{SSH_TAG}-%C", host], capture_output=True,
                           text=True, timeout=10, stdin=subprocess.DEVNULL)
        got = [x.split(" ", 1)[1] for x in p.stdout.splitlines() if x.startswith("controlpath ")]
        if p.returncode or not got:
            raise Unreachable(f"ssh -G {host}: exit {p.returncode}: {p.stderr.strip()[-200:]}")
        _CONTROL[host] = got[0]
    return _CONTROL[host]


RUN_DIR = os.environ.get("XDG_RUNTIME_DIR", "/tmp")
_CONTROL: dict[str, str] = {}
SSH_OPTS = ["-o", "BatchMode=yes", "-o", "LogLevel=ERROR", "-o", "ConnectTimeout=20", "-o", "ServerAliveInterval=15",
            "-o", "ServerAliveCountMax=4"]


def ssh_argv(host: str, cmd: str, stream: int = 0) -> list[str]:
    """ssh's command line for CMD on HOST: only the Slurm login nodes, cpucluster1's and the submit host. A login node
    is reached only over its kept connection (start_master): no ControlMaster, and a proxy that fails, so a session the
    master can't open ends 255 instead of opening a connection of its own. The submit host is a lab machine, where a
    kept connection would count as its owner's load, so one login a call. STREAM: unused (one connection a host)."""
    if host not in (*SLURM_HOSTS, CPU_HOST, SUBMIT):
        raise Refused(f"no ssh to {host}: DoC allows lab machines only through Condor")
    if not REMOTE_ROOT.startswith("/vol/gpudata/"):
        raise Refused("no workspace: set BC26_WORKSPACE in .env (.env.example), a folder under /vol/gpudata")
    opts = list(SSH_OPTS)
    if host != SUBMIT:
        opts += ["-o", "ControlMaster=no", "-o", f"ControlPath={control_path(host)}", "-o", "ProxyCommand=/bin/false"]
    return ["ssh", *opts, host, cmd]


def master_up(host: str) -> bool:
    p = subprocess.run(["ssh", "-O", "check", "-o", f"ControlPath={control_path(host)}", host], capture_output=True,
                       timeout=10, stdin=subprocess.DEVNULL)
    return p.returncode == 0


def start_master(host: str) -> None:
    """HOST's kept connection, made by one process at a time (a lock), unless one is up; it lasts 10 minutes past its
    last session. Unreachable when it can't be made."""
    with locked(CLIENT / "ssh" / f"{Path(control_path(host)).name}.master", MASTER_S):
        if master_up(host):
            return
        with tempfile.TemporaryFile() as err:
            try:
                p = subprocess.run(["ssh", *SSH_OPTS, "-o", "ControlMaster=yes", "-o",
                                    f"ControlPath={control_path(host)}", "-o", "ControlPersist=10m", "-f", "-N", host],
                                   stdin=subprocess.DEVNULL, stdout=subprocess.DEVNULL, stderr=err, timeout=MASTER_S)
            except subprocess.TimeoutExpired:
                raise Unreachable(f"on {host}: no connection within {MASTER_S:.0f} s") from None
            err.seek(0)
            if p.returncode:
                raise Unreachable(f"on {host}: connecting: exit {p.returncode}: "
                                  f"{err.read().decode(errors=ESC).strip()[-300:]}")


@contextmanager
def locked(path: Path, wait_s: float) -> Iterator[None]:
    """An exclusive lock on PATH for the block, waited for WAIT_S at most (then Unreachable)."""
    path.parent.mkdir(parents=True, exist_ok=True)
    fd = os.open(path, os.O_RDWR | os.O_CREAT, 0o600)
    try:
        end = time.monotonic() + wait_s
        while True:
            try:
                fcntl.flock(fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
                break
            except BlockingIOError:
                if time.monotonic() >= end:
                    raise Unreachable(f"{path.name}: held by another process for {wait_s:.0f} s") from None
                time.sleep(0.05)
        yield
    finally:
        os.close(fd)


def held(path: Path) -> bool:
    """Whether another process holds the lock on PATH now."""
    try:
        fd = os.open(path, os.O_RDWR)
    except OSError:
        return False
    try:
        fcntl.flock(fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
        return False
    except BlockingIOError:
        return True
    finally:
        os.close(fd)


@contextmanager
def session(host: str, bulk: bool, wait_s: float) -> Iterator[None]:
    """One of HOST's session slots, shared by every process here: SESSIONS a kept connection (BULK_SESSIONS of them
    for bulk transfers), SUBMIT_SESSIONS logins at once to the submit host. Unreachable when none frees within
    WAIT_S."""
    if host == SUBMIT:
        key, slots = "submit", range(SUBMIT_SESSIONS)
    else:
        key = host if LOCAL else Path(control_path(host)).name
        slots = range(SESSIONS - BULK_SESSIONS, SESSIONS) if bulk else range(SESSIONS)
    d = CLIENT / "ssh"
    d.mkdir(parents=True, exist_ok=True)
    end = time.monotonic() + wait_s
    while True:
        for i in slots:
            fd = os.open(d / f"{key}.{i}", os.O_RDWR | os.O_CREAT, 0o600)
            try:
                fcntl.flock(fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
            except BlockingIOError:
                os.close(fd)
                continue
            try:
                yield
            finally:
                os.close(fd)
            return
        if time.monotonic() >= end:
            raise Unreachable(f"{host}: all {len(slots)} sessions busy for {wait_s:.0f} s")
        time.sleep(0.05)


# The remote side's bound: the command runs in a session of its own (setsid), beside a `sleep` of its bound; when the
# sleep ends first, the bound is said on stderr and the whole session is killed, the command's children too (GNU
# timeout kills only its child on the login nodes, and a child left holding the output kept the ssh session open). A
# background job's stdin is /dev/null unless it is named, so it is.
BOUND_SH = ('sleep {t} </dev/null >/dev/null 2>&1 & w=$!; bash -c "$1" 0<&0 & p=$!; wait -n; e=$?; '
            'if kill -0 $p 2>/dev/null; then echo "{said} {t} s" >&2; kill -KILL -$$; fi; kill $w 2>/dev/null; exit $e')
TIMED_OUT = "bc26: the remote bound ended it after"


def bounded(script: str, t: float) -> str:
    """SCRIPT run under a kill of it and every process it started after T seconds (BOUND_SH)."""
    inner = BOUND_SH.format(t=int(t), said=TIMED_OUT)
    return f"setsid bash -c {shlex.quote(inner)} _ {shlex.quote(script)}; exit $?"


def killed(code: int, err: bytes) -> bool:
    return TIMED_OUT.encode() in err


# sshd's answer when the connection already holds its 10 sessions (other processes' calls, old tools' among them): the
# command never ran, so the call waits its turn and asks again
SESSION_REFUSED = re.compile(r"Session open refused|session request failed")


def _attempts(host: str, launch: Callable[[], tuple[int, bytes]], wait_s: float) -> tuple[int, bytes]:
    """LAUNCH (one ssh call: its code and stderr) until it gets a session: again while sshd refuses one (for WAIT_S at
    most), and once more after making the kept connection again when it had gone. Locally, once."""
    end = time.monotonic() + wait_s
    pause, remade = 0.2, False
    while True:
        code, err = launch()
        if code != 255 or LOCAL:
            return code, err
        if SESSION_REFUSED.search(err.decode(errors=ESC)) and time.monotonic() + pause < end:
            time.sleep(pause)
            pause = min(pause * 2, 5.0)
            continue
        if not remade and not master_up(host):
            start_master(host)
            remade = True
            continue
        return code, err


def _run(host: str, script: str, stdin: bytes | None, t: float, bulk: bool = False
         ) -> subprocess.CompletedProcess[bytes]:
    """SCRIPT on HOST within T seconds (both sides), in one session slot (waited for: SLOT_WAIT_S). Unreachable when it
    ran out of time, or no session could be had."""
    got: list[subprocess.CompletedProcess[bytes]] = []

    def launch() -> tuple[int, bytes]:
        argv = ["bash", "-c", bounded(script, t)] if LOCAL else ssh_argv(host, bounded(script, t))
        try:
            p = subprocess.run(argv, input=stdin, capture_output=True, timeout=t + SLACK_S,
                               stdin=None if stdin is not None else subprocess.DEVNULL)
        except subprocess.TimeoutExpired:
            raise Unreachable(f"on {host}: no answer within {t + SLACK_S:.0f} s") from None
        got.append(p)
        return p.returncode, p.stderr
    with session(host, bulk, SLOT_WAIT_S):
        code, err = _attempts(host, launch, SLOT_WAIT_S)
    if killed(code, err):
        raise Unreachable(f"on {host}: the remote bound ended it after {t:.0f} s")
    return got[-1]


def login(cmd: str, stdin: bytes | None = None, hosts: tuple[str, ...] = SLURM_HOSTS, stream: int = 0,
          timeout: float | None = None) -> str:
    """CMD on the first login node of HOSTS that answers; its stdout. ssh's 255 tries the next node (one with another
    kept connection), which runs CMD again, so a CMD that changes anything must refuse to run twice (a `mkdir` of its
    folder). Refused when CMD fails, Unreachable when no node answers or it ran out of time (TIMEOUT: READ_S, plus
    UPLOAD_MB_S a MB of STDIN). A CMD too long for one argument goes as a script on ssh's stdin."""
    script = UMASK + cmd
    if stdin is None and len(script.encode()) >= MAX_ARG // 2:  # quoted once more by `bounded`
        script, stdin = "bash -s", script.encode()
    t = timeout or READ_S + UPLOAD_MB_S * len(stdin or b"") / 1e6
    bulk = len(stdin or b"") > 1 << 20
    p: subprocess.CompletedProcess[bytes] | None = None
    tried: set[str] = set()
    for host in hosts:
        key = host if LOCAL else control_path(host)
        if key in tried:  # another name of a connection already tried (gpucluster2 and 3 are one host)
            continue
        tried.add(key)
        p = _run(host, script, stdin, t, bulk)
        if p.returncode != 255:
            break
    assert p is not None
    if p.returncode:
        kind = Unreachable if p.returncode == 255 else Refused
        raise kind(f"on {hosts[0]}: exit {p.returncode}: {p.stderr.decode(errors=ESC).strip()[-500:]}")
    return p.stdout.decode(errors=ESC)


def login_into(cmd: str, into: Path, hosts: tuple[str, ...] = SLURM_HOSTS, stream: int = 1,
               timeout: float = BULK_S) -> None:
    """CMD, whose stdout is a tar, on the first login node of HOSTS that answers, unpacked into INTO as it comes (the
    bytes never in Python), in a bulk session slot, within TIMEOUT."""
    into.mkdir(parents=True, exist_ok=True)
    tried: set[str] = set()
    for host in hosts:
        key = host if LOCAL else control_path(host)
        if key in tried:
            continue
        tried.add(key)
        tars: list[subprocess.CompletedProcess[bytes]] = []

        def launch() -> tuple[int, bytes]:
            script = bounded(UMASK + cmd, timeout)
            argv = ["bash", "-c", script] if LOCAL else ssh_argv(host, script)
            with tempfile.TemporaryFile() as err:
                src = subprocess.Popen(argv, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, stderr=err)
                assert src.stdout is not None
                timer = threading.Timer(timeout + SLACK_S, src.kill)
                timer.start()
                try:
                    tars.append(subprocess.run(["tar", "-xf", "-", "-C", str(into)], stdin=src.stdout,
                                               capture_output=True))
                    src.stdout.close()
                    code = src.wait()
                finally:
                    timer.cancel()
                err.seek(0)
                return code, err.read()
        with session(host, True, SLOT_WAIT_S):
            code, err = _attempts(host, launch, SLOT_WAIT_S)
        if code == 255 and not LOCAL:
            continue
        if killed(code, err) or code == -9:
            raise Unreachable(f"on {host}: the transfer ran out of its {timeout:.0f} s")
        if code or tars[-1].returncode:
            raise Refused(f"on {host}: exit {code}, tar {tars[-1].returncode}: "
                          f"{(err + tars[-1].stderr).decode(errors=ESC).strip()[-500:]}")
        return
    raise Unreachable(f"on {hosts[0]}: exit 255")


# Reads of the schedulers (sinfo, condor_status, our queues), shared by every process here for CACHE_S: 66 placements at
# once each read them (19 Nov). A submit makes them stale at once (forget).
CACHE_S = 0.5 if LOCAL else 5.0
STALE_OK = False  # a process that must not wait (tools/compute status) takes the last read while another reads anew


def cached(cmd: str, hosts: tuple[str, ...] = SLURM_HOSTS, max_age: float = CACHE_S, timeout: float | None = None
           ) -> str:
    """CMD's stdout as any process here read it within MAX_AGE since our last submit or removal, else read now (one
    process at a time a command). A read begun before a submit is never taken after it: each read is filed under the
    generation it began in (forget starts a new one), so a placement always sees the pieces the last one queued."""
    key = hashlib.sha256(f"{hosts} {cmd}".encode()).hexdigest()[:20]
    d = CLIENT / "cache"
    lock = d / f"{key}.lock"

    def current() -> Path:
        try:
            gen = (d / "generation").read_text().strip()
        except OSError:
            gen = "0"
        return d / f"{key}.{gen}"

    def fresh() -> str | None:
        f = current()
        try:
            if time.time() - f.stat().st_mtime <= max_age:
                return f.read_bytes().decode(errors=ESC)
        except OSError:
            pass
        return None
    if max_age > 0 and (got := fresh()) is not None:
        return got
    if STALE_OK and held(lock):  # another process reads it now: the last read of any generation
        last = sorted(d.glob(f"{key}.*[0-9]"), key=lambda x: x.stat().st_mtime)
        if last:
            return last[-1].read_bytes().decode(errors=ESC)
    with locked(lock, min(2 * (timeout or READ_S) + SLACK_S, SLOT_WAIT_S)):
        if max_age > 0 and (got := fresh()) is not None:
            return got
        f = current()
        out = login(cmd, hosts=hosts, timeout=timeout)
        tmp = d / f".{key}.{os.getpid()}"
        tmp.write_bytes(out.encode(errors=ESC))
        os.replace(tmp, f)
        return out


def forget() -> None:
    """Every cached read stale, and any read under way filed where no reader looks: something of ours was just
    submitted or removed."""
    d = CLIENT / "cache"
    d.mkdir(parents=True, exist_ok=True)
    tmp = d / f".generation.{os.getpid()}"
    tmp.write_text(str(time.time_ns()))
    os.replace(tmp, d / "generation")
    for f in d.glob("*"):
        if not f.name.endswith(".lock") and f.name != "generation" and not f.name.startswith("."):
            f.unlink(missing_ok=True)


def fresh(stamp: str, every_s: float) -> bool:
    """Whether something every process here does at most every EVERY_S (a sweep, the runtime's check) was done within
    it: CLIENT/stamp-STAMP's age. The caller marks it done (mark) once it succeeded."""
    try:
        return time.time() - (CLIENT / f"stamp-{stamp}").stat().st_mtime < every_s
    except OSError:
        return False


def mark(stamp: str) -> None:
    CLIENT.mkdir(parents=True, exist_ok=True)
    (CLIENT / f"stamp-{stamp}").touch()


def unmark_all() -> None:
    """Every stamp gone: what they vouched for (the runtime on the volume) may be gone too."""
    for f in CLIENT.glob("stamp-*"):
        f.unlink(missing_ok=True)


# ---- the Condor submit host: Condor authenticates a submit or a removal by a Kerberos ticket there ----
BW = Path.home() / ".local" / "bin" / "bw"  # the Bitwarden CLI, unlocked by the user
BW_ITEM = os.environ.get("BC26_BW_ITEM", "")  # the Bitwarden item holding the Kerberos password (.env)
PRINCIPAL = os.environ.get("BC26_KERBEROS_PRINCIPAL", "")  # USER@IC.AC.UK (.env)
# The sha256 of the last password the Kerberos server refused: never sent again (each refusal counts toward the
# account's lockout). The previous project's record too, read only.
KINIT_REFUSED = DATA / "kinit-refused"
KINIT_REFUSED_OTHER = Path.home() / "battlecode-data" / "kinit-refused"
# kinit's answers that the server refused the password itself (MIT Kerberos's texts); any other failure says nothing
# of the password
KINIT_REFUSED_TEXT = re.compile(r"Password incorrect|Preauthentication failed|credentials have been revoked|"
                                r"Password has expired|not found in Kerberos database")


def bitwarden_password() -> bytes:
    if not BW_ITEM or not PRINCIPAL:
        raise Refused("no Kerberos login: set BC26_BW_ITEM and BC26_KERBEROS_PRINCIPAL in .env (.env.example)")
    try:
        bw = subprocess.run([str(BW), "get", "password", BW_ITEM], capture_output=True, stdin=subprocess.DEVNULL,
                            timeout=60)
    except subprocess.TimeoutExpired:
        raise Refused(f"{BW} gave no answer within 60 s") from None
    if bw.returncode:
        raise Refused(f"{BW} exited {bw.returncode}: {bw.stderr.decode(errors=ESC).strip()[:300]}")
    return bw.stdout


def kinit_on_submit(password: bytes) -> subprocess.CompletedProcess[bytes]:
    return _submit_run(ssh_argv(SUBMIT, bounded(f"kinit {PRINCIPAL}", SUBMIT_S)), password)


def kerberos_login(password: Callable[[], bytes] = bitwarden_password,
                   kinit: Callable[[bytes], subprocess.CompletedProcess[bytes]] = kinit_on_submit,
                   refused: tuple[Path, ...] = (KINIT_REFUSED, KINIT_REFUSED_OTHER)) -> None:
    """A Kerberos ticket on SUBMIT from Bitwarden's password, unless the Kerberos server refused that same password
    before (its sha256 in a REFUSED file): a refused password is never sent again, nor an empty one (a locked bw
    prints none). A new password in Bitwarden (then `bw sync`), or a kinit by hand on SUBMIT, logs in again."""
    pw = password()
    if not pw.strip():
        raise Refused(f"no kinit on {SUBMIT}: Bitwarden gave no password for {PRINCIPAL} (is {BW} unlocked?)")
    digest = hashlib.sha256(pw).hexdigest()
    if any(f.exists() and f.read_text().strip() == digest for f in refused):
        raise Refused(f"no kinit on {SUBMIT}: the Kerberos server refused Bitwarden's password for {PRINCIPAL}, so it "
                      f"isn't sent again: put the current one in Bitwarden and run `bw sync`, or run kinit on {SUBMIT} "
                      "by hand")
    k = kinit(pw)
    if k.returncode:
        said = k.stderr.decode(errors=ESC).strip()
        if KINIT_REFUSED_TEXT.search(said):
            refused[0].parent.mkdir(parents=True, exist_ok=True)
            refused[0].write_text(digest + "\n")
        raise Refused(f"kinit on {SUBMIT}: exit {k.returncode}: {said[-300:]}")


# ssh's words for a connection that failed before any command ran (SUBMIT's banner exchange timed out at times on 18
# Nov): only such a failure is tried again, since a command that may have run (a condor_submit) must not run twice
NOT_CONNECTED = re.compile(r"banner exchange|kex_exchange_identification|Connection timed out|Connection refused|"
                           r"Could not resolve hostname|Connection closed by .* port 22$")
SUBMIT_TRIES, SUBMIT_PAUSE_S = 3, 10.0
SUBMIT_S = 120.0  # a command on the submit host (a login a call: the jump host and a lab machine's sshd included)
# After the submit host gave no answer, placements leave Condor out this long (submit_down): a try costs 2 to 7 minutes
# under the placement lock, and on 20 Nov, ash04 down, every placement failed on it with cpucluster1 free
SUBMIT_DOWN_S = 600.0


def _submit_run(argv: list[str], stdin: bytes | None = None) -> subprocess.CompletedProcess[bytes]:
    """ARGV (ssh to SUBMIT) in one of its session slots, within SUBMIT_S; a timeout reads as ssh's 255 with no word of
    a connection failure, so it is never run again."""
    with session(SUBMIT, False, SLOT_WAIT_S):
        try:
            return subprocess.run(argv, input=stdin, capture_output=True, timeout=SUBMIT_S + SLACK_S,
                                  stdin=None if stdin is not None else subprocess.DEVNULL)
        except subprocess.TimeoutExpired:
            return subprocess.CompletedProcess(argv, 255, b"", f"no answer within {SUBMIT_S:.0f} s".encode())


def ssh_submit(cmd: str, sleep: Callable[[float], None] = time.sleep,
               run: Callable[[list[str]], subprocess.CompletedProcess[bytes]] | None = None
               ) -> subprocess.CompletedProcess[bytes]:
    """CMD on SUBMIT, tried again (SUBMIT_TRIES in all) while ssh fails before connecting."""
    go = run or _submit_run
    for k in range(SUBMIT_TRIES):
        p = go(ssh_argv(SUBMIT, bounded(cmd, SUBMIT_S)))
        said = p.stderr.decode(errors=ESC).strip()
        if p.returncode != 255 or not NOT_CONNECTED.search(said) or k == SUBMIT_TRIES - 1:
            return p
        sleep(SUBMIT_PAUSE_S)
    raise AssertionError("unreachable")


def submit_down() -> float | None:
    """How many seconds ago SUBMIT gave this machine's tools no answer (submit_host), while under SUBMIT_DOWN_S."""
    try:
        age = time.time() - (CLIENT / "submit-down").stat().st_mtime
    except OSError:
        return None
    return age if age < SUBMIT_DOWN_S else None


def submit_host(cmd: str, ticket: bool = True) -> str:
    """One condor command on SUBMIT; with TICKET, after making sure it holds a Kerberos ticket (a submit or a removal
    needs one; a read doesn't). Unreachable when the ticket check got no answer (ssh's 255): CMD did not run, no
    password is sent, and the time is noted (submit_down)."""
    if ticket:
        k = ssh_submit("klist -s")
        note = CLIENT / "submit-down"
        if k.returncode == 255:
            note.parent.mkdir(parents=True, exist_ok=True)
            note.touch()
            raise Unreachable(f"{SUBMIT} gave no answer: {k.stderr.decode(errors=ESC).strip()[-300:]}")
        note.unlink(missing_ok=True)
        if k.returncode:
            kerberos_login()
    p = ssh_submit(f"{UMASK}{CONDOR}; {cmd}")
    if p.returncode:
        raise Refused(f"{SUBMIT}: {cmd.split()[0]}: exit {p.returncode}: {p.stderr.decode(errors=ESC).strip()[-500:]}")
    return p.stdout.decode(errors=ESC)
