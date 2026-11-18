"""How tools/compute reaches DoC: ssh to the login nodes it may use, and one condor command at a time on the Condor
submit host. Nothing else here touches a DoC machine (internals.md, "DoC rules").

Every remote path is under REMOTE_ROOT, ours alone: 0700, written with umask 077. BC26_LOCAL_REMOTE=DIR (tests only)
makes REMOTE_ROOT that folder of this machine and runs each login's command here with bash, so a test runs the real
shell of a job's life without DoC.
"""
from __future__ import annotations

import hashlib
import os
import re
import subprocess
import tempfile
import time
from collections.abc import Callable
from pathlib import Path

LOCAL = os.environ.get("BC26_LOCAL_REMOTE", "")  # tests: a folder here standing in for REMOTE_ROOT
# Our folder on /vol/bitbucket, apart from every other project's: every job's code bundle lies under it
REMOTE_ROOT = LOCAL or "/vol/bitbucket/user/bc26"
REMOTE_JOBS = f"{REMOTE_ROOT}/jobs"
REMOTE_RUNS = f"{REMOTE_ROOT}/runs"
RUNTIME = f"{REMOTE_ROOT}/runtime"  # the JDK, the Python and the engine's jars every job uses (provision.py)
DATA = Path.home() / "battlecode26-data"
UMASK = "umask 077; "  # before every remote command: what it creates is ours alone
MAX_ARG = 131_072  # Linux's cap on one argument (MAX_ARG_STRLEN): a longer command goes on ssh's stdin
SLURM_HOSTS = ("gpucluster2", "gpucluster3")  # the GPU cluster's login nodes: the second if the first is down
CPU_HOST = "cpucluster1.doc.ic.ac.uk"  # cpucluster1's login node
SUBMIT = "ash04.doc.ic.ac.uk"  # the Condor submit host, a lab machine: one login per call, never a kept connection
CONDOR = ("export CONDOR_CONFIG=/vol/condor/pool/doc/config/condor_config.global "
          "PATH=/vol/condor/pool/doc/release/Ubuntu-26.04/X86_64.LINUX/bin:$PATH")
ESC = "surrogateescape"  # a log may hold any bytes: decoded and encoded back exactly


class Refused(Exception):
    """A request the rules refuse, or a backend that can't be reached; the CLI prints it and exits 2."""


class Unreachable(Refused):
    """No login node answered (ssh's 255 from each), so the command may not have run at all."""


def ssh_argv(host: str, cmd: str, stream: int = 0) -> list[str]:
    """ssh's command line for CMD on HOST: only the Slurm login nodes, cpucluster1's and the submit host. The login
    nodes go over one kept-open connection each (STREAM: another one, for a bulk transfer beside it), never a new
    connection a call, which made gpucluster2 refuse new ones for minutes; the submit host is a lab machine, where a
    kept connection would count as its owner's load, so one login a call."""
    if host not in (*SLURM_HOSTS, CPU_HOST, SUBMIT):
        raise Refused(f"no ssh to {host}: DoC allows lab machines only through Condor")
    opts = ["-o", "BatchMode=yes", "-o", "LogLevel=ERROR", "-o", "ConnectTimeout=20", "-o", "ServerAliveInterval=15",
            "-o", "ServerAliveCountMax=4"]
    if host != SUBMIT:
        sock = Path(os.environ.get("XDG_RUNTIME_DIR", "/tmp")) / f"bc26-ssh-%C{f'-{stream}' if stream else ''}"
        opts += ["-o", "ControlMaster=auto", "-o", f"ControlPath={sock}", "-o", "ControlPersist=10m"]
    return ["ssh", *opts, host, cmd]


def _run(host: str, script: str, stdin: bytes | None, stream: int) -> subprocess.CompletedProcess[bytes]:
    if LOCAL:
        return subprocess.run(["bash", "-c", script], input=stdin, capture_output=True)
    return subprocess.run(ssh_argv(host, script, stream), input=stdin, capture_output=True)


def login(cmd: str, stdin: bytes | None = None, hosts: tuple[str, ...] = SLURM_HOSTS, stream: int = 0) -> str:
    """CMD on the first login node of HOSTS that answers; its stdout. ssh's 255 tries the next node, which runs CMD
    again, so a CMD that changes anything must refuse to run twice (a `mkdir` of its folder). Refused when CMD fails,
    Unreachable when no node answers. A CMD too long for one argument goes as a script on ssh's stdin."""
    script = UMASK + cmd
    if stdin is None and len(script.encode()) >= MAX_ARG:
        script, stdin = "bash -s", script.encode()
    p: subprocess.CompletedProcess[bytes] | None = None
    for host in hosts:
        p = _run(host, script, stdin, stream)
        if p.returncode != 255:
            break
    assert p is not None
    if p.returncode:
        kind = Unreachable if p.returncode == 255 else Refused
        raise kind(f"on {hosts[0]}: exit {p.returncode}: {p.stderr.decode(errors=ESC).strip()[-500:]}")
    return p.stdout.decode(errors=ESC)


def login_into(cmd: str, into: Path, hosts: tuple[str, ...] = SLURM_HOSTS, stream: int = 1) -> None:
    """CMD, whose stdout is a tar, on the first login node of HOSTS that answers, unpacked into INTO as it comes (the
    bytes never in Python), over a connection of its own (STREAM)."""
    into.mkdir(parents=True, exist_ok=True)
    for host in hosts:
        with tempfile.TemporaryFile() as err:
            argv = ["bash", "-c", UMASK + cmd] if LOCAL else ssh_argv(host, UMASK + cmd, stream)
            src = subprocess.Popen(argv, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, stderr=err)
            assert src.stdout is not None
            tar = subprocess.run(["tar", "-xf", "-", "-C", str(into)], stdin=src.stdout, capture_output=True)
            src.stdout.close()
            code = src.wait()
            if code == 255 and not LOCAL:
                continue
            err.seek(0)
            if code or tar.returncode:
                raise Refused(f"on {host}: exit {code}, tar {tar.returncode}: "
                              f"{(err.read() + tar.stderr).decode(errors=ESC).strip()[-500:]}")
            return
    raise Unreachable(f"on {hosts[0]}: exit 255")


# ---- the Condor submit host: Condor authenticates a submit or a removal by a Kerberos ticket there ----
BW = Path.home() / ".local" / "bin" / "bw"  # the Bitwarden CLI, unlocked by the user
BW_ITEM = "00000000-0000-0000-0000-000000000000"  # Bitwarden's "Imperial College"
PRINCIPAL = "user@IC.AC.UK"
# The sha256 of the last password the Kerberos server refused: never sent again (each refusal counts toward the
# account's lockout). The previous project's record too, read only.
KINIT_REFUSED = DATA / "kinit-refused"
KINIT_REFUSED_OTHER = Path.home() / "battlecode-data" / "kinit-refused"
# kinit's answers that the server refused the password itself (MIT Kerberos's texts); any other failure says nothing
# of the password
KINIT_REFUSED_TEXT = re.compile(r"Password incorrect|Preauthentication failed|credentials have been revoked|"
                                r"Password has expired|not found in Kerberos database")


def bitwarden_password() -> bytes:
    bw = subprocess.run([str(BW), "get", "password", BW_ITEM], capture_output=True, stdin=subprocess.DEVNULL)
    if bw.returncode:
        raise Refused(f"{BW} exited {bw.returncode}: {bw.stderr.decode(errors=ESC).strip()[:300]}")
    return bw.stdout


def kinit_on_submit(password: bytes) -> subprocess.CompletedProcess[bytes]:
    return subprocess.run(ssh_argv(SUBMIT, f"kinit {PRINCIPAL}"), input=password, capture_output=True)


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


def ssh_submit(cmd: str, sleep: Callable[[float], None] = time.sleep,
               run: Callable[[list[str]], subprocess.CompletedProcess[bytes]] | None = None
               ) -> subprocess.CompletedProcess[bytes]:
    """CMD on SUBMIT, tried again (SUBMIT_TRIES in all) while ssh fails before connecting."""
    go = run or (lambda argv: subprocess.run(argv, capture_output=True))
    for k in range(SUBMIT_TRIES):
        p = go(ssh_argv(SUBMIT, cmd))
        said = p.stderr.decode(errors=ESC).strip()
        if p.returncode != 255 or not NOT_CONNECTED.search(said) or k == SUBMIT_TRIES - 1:
            return p
        sleep(SUBMIT_PAUSE_S)
    raise AssertionError("unreachable")


def submit_host(cmd: str, ticket: bool = True) -> str:
    """One condor command on SUBMIT; with TICKET, after making sure it holds a Kerberos ticket (a submit or a removal
    needs one; a read doesn't)."""
    if ticket and ssh_submit("klist -s").returncode:
        kerberos_login()
    p = ssh_submit(f"{UMASK}{CONDOR}; {cmd}")
    if p.returncode:
        raise Refused(f"{SUBMIT}: {cmd.split()[0]}: exit {p.returncode}: {p.stderr.decode(errors=ESC).strip()[-500:]}")
    return p.stdout.decode(errors=ESC)
