"""Content-addressed C++ object (.o) cache wrapper for native engine builds (tools/bcgames/native.py).

Invoked as a `clang++` shim on PATH during `engine/build.py` (no change to engine/build.py, so binary keys stay
unchanged). Single-source `-c` compilations hash:
  - cache version + compiler identity (toolchain string + real binary size/mtime_ns)
  - normalized compiler flags (excluding -I paths, -o, -MF, -c, input path)
  - input source file bytes
  - all transitively resolved `#include "..."` (and local `#include <...>`) header contents in deterministic DFS order

Cache hits copy the cached .o atomically to `-o` and synthesize the `-MF` depfile with the current build's paths so
ninja's `.ninja_deps` remains exact across build slots (gen/1.2.6-0 vs 1.2.6-1) and alternating branches.
Stdlib only; runs under `python3 -S` (~15 ms startup).
"""
from __future__ import annotations

import hashlib
import os
import re
import shutil
import subprocess
import sys
import tempfile
import time
from pathlib import Path

CACHE_VERSION = "bcx-objcache-v1"
INCLUDE_QUOTED_RE = re.compile(rb'^\s*#\s*include\s+"([^"]+)"', re.M)
INCLUDE_ANGLE_RE = re.compile(rb'^\s*#\s*include\s+<([^>]+)>', re.M)
SOURCE_SUFFIXES = (".cpp", ".cc", ".cxx", ".c")
SKIP_FLAGS = frozenset({"-E", "-S", "-M", "-MM", "-fsyntax-only", "--version", "-v", "-include", "-imacros"})
VALUE_FLAGS = frozenset({"-o", "-MF", "-MT", "-MQ", "-I", "-isystem", "-iquote", "-D", "-U", "-x", "-target",
                         "--target", "-Xclang", "-mllvm"})


def parse_compile_args(argv: list[str]) -> tuple[Path, Path, Path | None, list[Path], list[str]] | None:
    """Parse a single-source `clang++ -c` invocation.
    Returns (src, out, depfile, include_dirs, norm_flags), or None if this invocation should bypass the cache."""
    if "-c" not in argv:
        return None
    has_c = False
    out: Path | None = None
    depfile: Path | None = None
    include_dirs: list[Path] = []
    inputs: list[Path] = []
    norm: list[str] = []
    i = 0
    n = len(argv)
    while i < n:
        a = argv[i]
        if a in SKIP_FLAGS or a.startswith("-include"):
            return None
        if a == "-c":
            has_c = True
            i += 1
        elif a == "-o":
            if i + 1 >= n:
                return None
            out = Path(argv[i + 1])
            i += 2
        elif a.startswith("-o") and len(a) > 2:
            out = Path(a[2:])
            i += 1
        elif a == "-MF":
            if i + 1 >= n:
                return None
            depfile = Path(argv[i + 1])
            i += 2
        elif a.startswith("-MF") and len(a) > 3:
            depfile = Path(a[3:])
            i += 1
        elif a in ("-MMD", "-MD", "-MP"):
            i += 1
        elif a in ("-MT", "-MQ"):
            if i + 1 >= n:
                return None
            i += 2
        elif a == "-I":
            if i + 1 >= n:
                return None
            include_dirs.append(Path(argv[i + 1]))
            i += 2
        elif a.startswith("-I") and len(a) > 2:
            include_dirs.append(Path(a[2:]))
            i += 1
        elif a in VALUE_FLAGS:
            if i + 1 >= n:
                return None
            norm.append(a)
            norm.append(argv[i + 1])
            i += 2
        elif a.startswith("-"):
            norm.append(a)
            i += 1
        else:
            inputs.append(Path(a))
            i += 1
    if not has_c or out is None or len(inputs) != 1:
        return None
    src = inputs[0]
    if src.suffix.lower() not in SOURCE_SUFFIXES or not src.is_file():
        return None
    return src, out, depfile, include_dirs, norm


def _find_header(name: str, cur_dir: Path | None, include_dirs: list[Path]) -> Path | None:
    if cur_dir is not None:
        cand = cur_dir / name
        if cand.is_file():
            return cand
    for d in include_dirs:
        cand = d / name
        if cand.is_file():
            return cand
    return None


def resolve_headers(src: Path, include_dirs: list[Path]) -> list[tuple[str, Path, bytes]] | None:
    """Resolve all local headers transitively included by SRC in deterministic DFS order.
    Returns [(include_name, resolved_path, bytes), ...] or None if any quoted include cannot be resolved."""
    seen: set[Path] = set()
    out: list[tuple[str, Path, bytes]] = []

    def scan(path: Path, data: bytes) -> bool:
        for m in INCLUDE_QUOTED_RE.finditer(data):
            inc = m.group(1).decode("utf-8", errors="surrogateescape")
            found = _find_header(inc, path.parent, include_dirs)
            if found is None:
                return False
            rp = found.resolve()
            if rp in seen:
                continue
            seen.add(rp)
            hdata = found.read_bytes()
            out.append((inc, found, hdata))
            if not scan(found, hdata):
                return False
        for m in INCLUDE_ANGLE_RE.finditer(data):
            inc = m.group(1).decode("utf-8", errors="surrogateescape")
            found = _find_header(inc, None, include_dirs)
            if found is None:
                continue  # system header (covered by compiler_id / toolchain)
            rp = found.resolve()
            if rp in seen:
                continue
            seen.add(rp)
            hdata = found.read_bytes()
            out.append((inc, found, hdata))
            if not scan(found, hdata):
                return False
        return True

    try:
        src_bytes = src.read_bytes()
    except OSError:
        return None
    if not scan(src, src_bytes):
        return None
    return out


def compute_key(compiler_id: str, norm_flags: list[str], src: Path, src_bytes: bytes,
                headers: list[tuple[str, Path, bytes]]) -> str:
    h = hashlib.sha256()
    h.update(CACHE_VERSION.encode() + b"\0")
    h.update(compiler_id.encode("utf-8", errors="surrogateescape") + b"\0")
    h.update("\0".join(norm_flags).encode("utf-8", errors="surrogateescape") + b"\0")
    h.update(src.suffix.lower().encode() + b"\0")
    h.update(hashlib.sha256(src_bytes).digest())
    for inc, _, hdata in headers:
        h.update(b"\0" + inc.encode("utf-8", errors="surrogateescape") + b"\0")
        h.update(hashlib.sha256(hdata).digest())
    return h.hexdigest()[:32]


def _dep_escape(p: Path) -> str:
    return str(p).replace("\\", "\\\\").replace(" ", "\\ ")


def write_depfile(depfile: Path, out: Path, src: Path, headers: list[tuple[str, Path, bytes]]) -> None:
    depfile.parent.mkdir(parents=True, exist_ok=True)
    deps = " ".join([_dep_escape(src), *(_dep_escape(p) for _, p, _ in headers)])
    content = f"{_dep_escape(out)}: {deps}\n"
    fd, tmp = tempfile.mkstemp(prefix=".d.", dir=depfile.parent)
    try:
        with os.fdopen(fd, "w") as f:
            f.write(content)
        os.replace(tmp, depfile)
    except OSError:
        Path(tmp).unlink(missing_ok=True)
        raise


def _atomic_copy(src: Path, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    fd, tmp = tempfile.mkstemp(prefix=".obj.", dir=dest.parent)
    try:
        with os.fdopen(fd, "wb") as out_f, open(src, "rb") as in_f:
            shutil.copyfileobj(in_f, out_f, 1 << 20)
        os.replace(tmp, dest)
    except OSError:
        Path(tmp).unlink(missing_ok=True)
        raise


def _record_stat(stats_path: str, tag: bytes) -> None:
    if not stats_path:
        return
    try:
        fd = os.open(stats_path, os.O_WRONLY | os.O_CREAT | os.O_APPEND, 0o600)
        try:
            os.write(fd, tag)
        finally:
            os.close(fd)
    except OSError:
        pass


def run_cached(real_cxx: str, cache_dir: Path, compiler_id: str, argv: list[str],
               stats_path: str = "") -> int:
    """Run REAL_CXX on ARGV with content-addressed .o caching in CACHE_DIR. Returns the exit code."""
    parsed = parse_compile_args(argv)
    if parsed is None:
        return subprocess.run([real_cxx, *argv]).returncode
    src, out, depfile, include_dirs, norm_flags = parsed
    headers = resolve_headers(src, include_dirs)
    if headers is None:
        return subprocess.run([real_cxx, *argv]).returncode
    try:
        src_bytes = src.read_bytes()
    except OSError:
        return subprocess.run([real_cxx, *argv]).returncode
    key = compute_key(compiler_id, norm_flags, src, src_bytes, headers)
    entry = cache_dir / key[:2] / f"{key}.o"
    if entry.is_file():
        try:
            _atomic_copy(entry, out)
            if depfile is not None:
                write_depfile(depfile, out, src, headers)
            try:
                if time.time() - entry.stat().st_mtime > 60:
                    os.utime(entry)
            except OSError:
                pass
            _record_stat(stats_path, b"H\n")
            return 0
        except OSError:
            pass  # race with gc or transient fs error: fall through to real compile
    p = subprocess.run([real_cxx, *argv])
    if p.returncode == 0 and out.is_file():
        try:
            _atomic_copy(out, entry)
        except OSError:
            pass
        _record_stat(stats_path, b"M\n")
    return p.returncode


def gc(cache_dir: Path, keep_bytes: int, grace_s: float = 1800.0, dry: bool = False) -> list[tuple[str, int]]:
    """Bound CACHE_DIR to KEEP_BYTES by LRU (mtime), never removing entries touched within GRACE_S.
    Returns [(name, bytes), ...] removed (or to be removed when DRY)."""
    if not cache_dir.is_dir():
        return []
    now = time.time()
    entries: list[tuple[float, int, Path]] = []
    for sub in cache_dir.iterdir():
        if not sub.is_dir() or len(sub.name) != 2:
            continue
        try:
            items = list(sub.iterdir())
        except OSError:
            continue
        for f in items:
            try:
                st = f.stat()
            except OSError:
                continue
            if f.name.startswith("."):
                if now - st.st_mtime > 3600 and not dry:
                    f.unlink(missing_ok=True)
                continue
            if f.suffix == ".o":
                entries.append((st.st_mtime, st.st_size, f))
    entries.sort(key=lambda x: x[0], reverse=True)
    total = 0
    gone: list[tuple[str, int]] = []
    for mtime, size, path in entries:
        if now - mtime <= grace_s or total + size <= keep_bytes:
            total += size
            continue
        if not dry:
            path.unlink(missing_ok=True)
        gone.append((path.name, size))
    return gone


def main() -> int:
    real_cxx = os.environ.get("BCX_REAL_CXX", "")
    cache_dir = os.environ.get("BCX_OBJCACHE_DIR", "")
    compiler_id = os.environ.get("BCX_OBJCACHE_ID", "")
    stats_path = os.environ.get("BCX_OBJCACHE_STATS", "")
    argv = sys.argv[1:]
    if not real_cxx:
        sys.stderr.write("objcache: BCX_REAL_CXX is unset\n")
        return 127
    if not cache_dir or "-c" not in argv:
        os.execv(real_cxx, [real_cxx, *argv])
    return run_cached(real_cxx, Path(cache_dir), compiler_id, argv, stats_path)


if __name__ == "__main__":
    sys.exit(main())
