"""What every remote job runs on, put once in the runtime folder on the shared volume (remote.RUNTIME), never per job:

- Temurin's JDK 21 (TEMURIN), from Adoptium's release, checked by its sha256: `java` on every node, whatever the node
  has (cpucluster1's is 11, the GPU cluster's 25).
- uv's CPython (PYTHON), python-build-standalone's, which runs on glibc 2.17: `python3` on every node.
- The engine's jars (tools/kit.py), each named by its content, for jobs with the kit.

Each goes up under a temporary name and is renamed in whole, so a job never sees half of one. A job's folder names
what it uses in its `env` and `kit` files (job.sh).
"""
from __future__ import annotations

import hashlib
import shutil
import subprocess
import sys
import tarfile
import tempfile
import urllib.request
from pathlib import Path

from tools import kit
from tools.remote import DATA, RUNTIME, Refused, login

# Temurin 21's latest release (api.adoptium.net, 18 Nov 2026): the same update as clankerland's own OpenJDK 21.0.13
TEMURIN = "jdk-21.0.13+9"
TEMURIN_URL = ("https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.13%2B9/"
               "OpenJDK21U-jdk_x64_linux_hotspot_21.0.13_9.tar.gz")
TEMURIN_SHA256 = "d7f5d5cf865a2902b70ff549b97586a7d3f234e016f041ab447ea740c7d66d1c"
PYTHON = "3.14.8"
PYTHON_DIR = f"cpython-{PYTHON}-linux-x86_64-gnu"  # uv's name for it, here and remote
CACHE = DATA / "cache"


def jdk_dir() -> str:
    return f"{RUNTIME}/{TEMURIN}"


def python_dir() -> str:
    return f"{RUNTIME}/{PYTHON_DIR}"


def jar_name(path: Path) -> str:
    """A jar's name in the runtime folder: its content's hash, then its own name."""
    return f"{hashlib.sha256(path.read_bytes()).hexdigest()[:16]}-{path.name}"


def remote_jars(repo: Path) -> list[str]:
    return [f"{RUNTIME}/jars/{jar_name(j)}" for j in kit.jars(repo)]


def env_file() -> bytes:
    """A job's `env`: what job.sh exports, one `NAME VALUE` a line; PATH's value goes first on PATH."""
    return (f"JAVA_HOME {jdk_dir()}\nPATH {jdk_dir()}/bin\nPATH {python_dir()}/bin\n").encode()


def kit_file(repo: Path) -> bytes:
    """A kit job's `kit`: the engine's jars on the volume, a line each, in classpath order."""
    return "".join(f"{j}\n" for j in remote_jars(repo)).encode()


def temurin_tarball() -> Path:
    """Temurin's tarball in CACHE, fetched once and checked."""
    dest = CACHE / Path(TEMURIN_URL).name
    if dest.is_file() and hashlib.sha256(dest.read_bytes()).hexdigest() == TEMURIN_SHA256:
        return dest
    CACHE.mkdir(parents=True, exist_ok=True)
    print(f"compute: fetching {TEMURIN} into {CACHE} (once)", file=sys.stderr)
    with tempfile.NamedTemporaryFile(dir=CACHE, delete=False) as tmp, urllib.request.urlopen(TEMURIN_URL) as r:
        shutil.copyfileobj(r, tmp)
    got = hashlib.sha256(Path(tmp.name).read_bytes()).hexdigest()
    if got != TEMURIN_SHA256:
        Path(tmp.name).unlink()
        raise Refused(f"{TEMURIN_URL}: sha256 {got}, not Adoptium's {TEMURIN_SHA256}")
    Path(tmp.name).rename(dest)
    return dest


def uv_python() -> Path:
    """uv's build of PYTHON here (installed when missing), as PYTHON_DIR names it."""
    for cmd in (["uv", "python", "install", PYTHON], ["uv", "python", "find", "--managed-python", PYTHON]):
        p = subprocess.run(cmd, capture_output=True, text=True)
        if p.returncode:
            raise Refused(f"{' '.join(cmd)}: exit {p.returncode}: {p.stderr.strip()[-300:]}")
    local = Path(p.stdout.strip()).resolve().parents[1]
    if local.name != PYTHON_DIR:
        raise Refused(f"uv's Python {PYTHON} is {local}, not {PYTHON_DIR}")
    return local


def unpack_into(dest: str, data: bytes, hosts: tuple[str, ...], flags: str = "-xf") -> None:
    """DATA, a tar, unpacked into DEST under a temporary name and renamed in whole; a second uploader's copy goes."""
    login(f'mkdir -p {RUNTIME} && t=$(mktemp -d {dest}.XXXXXX) && tar {flags} - -C "$t" && '
          f'{{ mv -T "$t" {dest} 2>/dev/null || rm -rf "$t"; }}', data, hosts)


def ensure(hosts: tuple[str, ...], repo: Path, with_kit: bool) -> None:
    """The JDK, the Python and (WITH_KIT) the engine's jars in RUNTIME, each uploaded when missing, before any job
    folder names them."""
    jars = kit.jars(repo) if with_kit else []
    names = [f"{RUNTIME}/jars/{jar_name(j)}" for j in jars]
    checks = [f"test -x {jdk_dir()}/bin/java || echo jdk", f"test -x {python_dir()}/bin/python3 || echo python",
              *(f"test -s {n} || echo jar{i}" for i, n in enumerate(names))]
    missing = login("; ".join(checks) + "; true", hosts=hosts).split()
    if "jdk" in missing:
        tarball = temurin_tarball()
        print(f"compute: uploading {TEMURIN} ({tarball.stat().st_size / 1e6:.0f} MB) to {jdk_dir()}", file=sys.stderr)
        # the tarball's one top folder is TEMURIN: unpacked one level down, then that folder renamed in
        login(f'mkdir -p {RUNTIME} && t=$(mktemp -d {RUNTIME}/.jdk.XXXXXX) && tar -xzf - -C "$t" && '
              f'{{ mv -T "$t/{TEMURIN}" {jdk_dir()} 2>/dev/null; rm -rf "$t"; }}', tarball.read_bytes(), hosts)
    if "python" in missing:
        local = uv_python()
        data = subprocess.run(["tar", "-C", str(local), "-cf", "-", "."], capture_output=True, check=True).stdout
        print(f"compute: uploading Python {PYTHON} ({len(data) / 1e6:.0f} MB) to {python_dir()}", file=sys.stderr)
        unpack_into(python_dir(), data, hosts)
    for i, (jar, name) in enumerate(zip(jars, names, strict=True)):
        if f"jar{i}" in missing:
            print(f"compute: uploading {jar.name} to {name}", file=sys.stderr)
            login(f'mkdir -p {RUNTIME}/jars && t=$(mktemp {name}.XXXXXX) && cat > "$t" && mv -f "$t" {name}',
                  jar.read_bytes(), hosts)


def tar_paths(repo: Path, paths: list[str]) -> bytes:
    """PATHS (relative to REPO) as one gzip'd tar (gzip -1: build/classes' 30 MB go as 7)."""
    import io
    buf = io.BytesIO()
    with tarfile.open(fileobj=buf, mode="w:gz", compresslevel=1) as tar:
        for rel in paths:
            tar.add(repo / rel, arcname=rel)
    return buf.getvalue()
