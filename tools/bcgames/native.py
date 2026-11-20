"""The native engine (engine/, track X) behind tools/play and tools/games: binaries planned, built, kept and played.

A binary (`bcx`) holds the engine and a list of bots, each package once, compiled in. The list is BASE (awubot and the
gauntlet's opponents, from this checkout, in a fixed order), then the game's other bots (challenger at some revision,
...) appended. A bot's module index is its position, so binaries that differ only in the appended bots rebuild only
those bots' files. A game is native when one binary can hold both its bots: two builds of one package can't share a
binary, so such a game (challenger against challenger@REV) is played by the Java engine.

Builds run on this host (the translation reads this JDK's class files; the toolchain is here), niced, BUILDS at once
across every process of the host, JOBS compile jobs each, one per generated-files folder (gen/), chosen to share the
most modules with the build before. Two processes asking for the same binary build it once (a lock per binary).

Store (NDATA: BCGAMES_DATA/native, or BCGAMES_NATIVE_DATA):
  src/<engine id>/engine/   the engine's sources a binary is built from (build.py, jvm/, runtime/)
  classes/<pkg>-<hash>/     a bot's class files, from its jar (the hash is the game key's class hash)
  gen/<version>-<i>/        generated C++ and objects of one build at a time (flock), last.json: what it holds
  gen/<version>-<i>.engine/ the engine sources it builds from, at a fixed path (changed files rewritten only)
  bin/<key>/bcx             a binary (stripped), build.json: what it holds; bin/<key>.log: its build's output
  bin/<key>/BAD             the exactness guard found it playing a game the Java engine doesn't: never used again
  bin/<key>.use             a process that plays or ships a binary holds it shared (flock) for its life: in use

gc() bounds bin/ by count, age and size: the most recently used are kept, KEEP of them or any used within KEEP_S, up
to KEEP_BYTES in all; one used within GRACE_S or held by a live process is never removed.
"""
from __future__ import annotations

import fcntl
import hashlib
import json
import os
import queue
import shutil
import signal
import subprocess
import sys
import tempfile
import threading
import time
import zipfile
from contextlib import contextmanager
from dataclasses import dataclass
from pathlib import Path

from . import store

ENGINE = store.ROOT / "engine"
NDATA = Path(os.environ.get("BCGAMES_NATIVE_DATA") or store.DATA / "native").expanduser()
BUILDS = int(os.environ.get("BCGAMES_NATIVE_BUILDS", "2"))  # builds at once on this host
JOBS = int(os.environ.get("BCGAMES_NATIVE_JOBS", "8"))      # compile jobs a build
# klasses.cpp (class tables, class-init wrappers, dispatch chains: no game logic) changes with any bot and is one
# file: about 240 s at -O2, 8 s at -O1, the rest of a challenger rebuild about 90 s
GLUE_OPT = "-O1"
# the rest: -O2 and no ThinLTO, not engine/build.py's default -O3 + ThinLTO (engine/AGENTS.md, "Build
# configurations"): a changed challenger rebuilds in main's engine's time (68-75 s wall, 240-270 CPU s on clankerland)
# instead of ~120 s and ~560 CPU s, for games 31% dearer (enginebench 15.02 G a game against 11.48)
OPT = os.environ.get("BCGAMES_NATIVE_OPT", "-O2")
LTO = os.environ.get("BCGAMES_NATIVE_LTO", "off")
NICE = 5  # builds sit on a test's critical path: below games, not far below
KEEP = 24              # binaries kept (the most recently used), and any used in the last KEEP_S seconds...
KEEP_S = 6 * 3600
KEEP_BYTES = int(os.environ.get("BCGAMES_NATIVE_KEEP_BYTES", 4 << 30))  # ...up to this much in all (about 100 MB each)
GRACE_S = 1800         # one used this recently is kept whatever its size (a build that just ended, a test starting)
RETRY_FAILED_S = 3600  # a failed build is tried again after this long
TIMEOUT = 600          # seconds a native game may take (the slowest measured: ~45 s on a DoC machine)
# The sparring bots: awubot and tools/games' gauntlet opponents (games.MAIN_OPPONENTS + OTHER_OPPONENTS), sorted.
BASE = ("awubot", "awubot_mystery", "awubot_quals", "awubot_sus", "awubot_us", "opp_cerr", "opp_food", "opp_goobie",
        "opp_gravy", "opp_gst", "opp_obg", "opp_parmesan", "opp_powerpuff", "opp_spaark")


class BuildError(Exception):
    pass


def _say(msg: str) -> None:
    print(f"tools/bcgames native: {msg}", file=sys.stderr, flush=True)


# ------------------------------------------------------------------------------------------------- engine sources

def engine_files(root: Path = ENGINE) -> list[Path]:
    """What a binary's code comes from, besides its bots: build.py, the translator and substitutes, the runtime."""
    files = [root / "build.py", root / "jvm" / "natives.txt"]
    for sub in ("jvm/src", "jvm/subst"):
        files += sorted(p for p in (root / sub).rglob("*") if p.is_file())
    files += sorted(p for p in (root / "runtime").iterdir() if p.is_file())
    return files


_toolchain: str | None = None


def can_build() -> bool:
    """This host can build binaries (DoC's nodes can't: there, only specs naming a binary play natively)."""
    return shutil.which("clang++") is not None and shutil.which("ninja") is not None and (ENGINE / "build.py").exists()


def toolchain() -> str:
    """The JDK whose class files are translated, and the C++ compiler."""
    global _toolchain
    if _toolchain is None:
        props = store._java_props()
        cc = subprocess.run(["clang++", "--version"], capture_output=True, text=True)
        if cc.returncode != 0:
            raise BuildError("clang++ is missing: the native engine can't be built here")
        _toolchain = f"{props['java.home']}|{props.get('java.runtime.version', '')}|{cc.stdout.splitlines()[0]}"
    return _toolchain


_snapshots: dict[str, tuple[str, Path]] = {}
_snap_lock = threading.Lock()


def snapshot(version: str) -> tuple[str, Path]:
    """(engine id, folder holding engine/): this checkout's engine sources copied into the store once, so a build never
    reads a file an agent is editing. The id hashes the copy, the version, its jar, the toolchain, GLUE_OPT, OPT and
    LTO."""
    with _snap_lock:
        if version in _snapshots:
            return _snapshots[version]
        (NDATA / "src").mkdir(parents=True, exist_ok=True)
        tmp = Path(tempfile.mkdtemp(prefix=".snap", dir=NDATA / "src"))
        try:
            h = hashlib.sha256(json.dumps([version, store.ENGINES[version], toolchain(), GLUE_OPT, OPT, LTO]).encode())
            for f in engine_files():
                rel = f.relative_to(ENGINE)
                data = f.read_bytes()
                h.update(rel.as_posix().encode() + b"\0" + data + b"\0")
                (tmp / "engine" / rel).parent.mkdir(parents=True, exist_ok=True)
                (tmp / "engine" / rel).write_bytes(data)
            eid = h.hexdigest()[:16]
            final = NDATA / "src" / eid
            if not final.exists():
                try:
                    os.rename(tmp, final)
                except OSError:
                    if not final.exists():
                        raise
        finally:
            shutil.rmtree(tmp, ignore_errors=True)
        _snapshots[version] = (eid, final)
        return eid, final


# ---------------------------------------------------------------------------------------------------------- plans

@dataclass(frozen=True)
class Bot:
    pkg: str
    hash: str      # the class hash of the game key (store.bot_jar / store.bot_classes)
    source: str    # its jar, or a folder holding <pkg>/


@dataclass(frozen=True)
class Plan:
    version: str
    bots: tuple[Bot, ...]   # the binary to build, in build order
    need: tuple[Bot, ...] = ()  # the game's bots: any binary holding them plays the game
    binary: str | None = None  # a given binary (remote nodes): no build

    def ids(self) -> list[list[str]]:
        return [[b.pkg, b.hash] for b in self.bots]


def compose(base: list[Bot], game: list[Bot]) -> list[Bot] | None:
    """The bots of the binary for a game of GAME's bots: BASE, a base bot replaced in place by another build of its
    package, the rest appended in package order. None when the game's two bots are two builds of one package."""
    if len(game) == 2 and game[0].pkg == game[1].pkg and game[0].hash != game[1].hash:
        return None
    have = {(b.pkg, b.hash) for b in base}
    extra = {b.pkg: b for b in game if (b.pkg, b.hash) not in have}
    out = [extra.pop(b.pkg, b) for b in base]
    return out + [extra[p] for p in sorted(extra)]


def binary_key(version: str, eid: str, bots: list[list[str]]) -> str:
    return hashlib.sha256(json.dumps([version, eid, bots]).encode()).hexdigest()[:16]


class Planner:
    """Which binary plays a job: the job's bots composed onto BASE (resolved once through the resolver), or the binary
    the spec names (`bcx`, on remote nodes)."""

    def __init__(self, resolver):
        self.resolver = resolver
        self.base: list[Bot] | None = None
        self.lock = threading.Lock()

    def _base(self) -> list[Bot]:
        with self.lock:
            if self.base is None:
                base = []
                for pkg in BASE:
                    try:
                        jar, h = self.resolver.bot(pkg)
                    except store.SetupError as e:  # a checkout without it: binaries hold the rest
                        _say(f"base bot {pkg} left out: {e}")
                        continue
                    base.append(Bot(pkg, h, str(jar)))
                self.base = base
            return self.base

    def game_bots(self, job) -> list[Bot]:
        s = job.spec
        out = []
        for side in ("a", "b"):
            src, h = self.resolver.bot(s[side], s.get(f"classes_{side}", s.get("classes")))
            out.append(Bot(store.parse_bot(s[side])[0], h, str(src)))
        return out

    def plan(self, job) -> Plan | None:
        version = job.spec["engine"]
        game = self.game_bots(job)
        if job.spec.get("bcx"):
            if game[0].pkg == game[1].pkg and game[0].hash != game[1].hash:
                return None
            return Plan(version, tuple(game), tuple(game), binary=job.spec["bcx"])
        if not can_build():
            return None
        bots = compose(self._base(), game)
        return Plan(version, tuple(bots), tuple(sorted(set(game), key=lambda b: (b.pkg, b.hash)))) \
            if bots is not None else None


# ------------------------------------------------------------------------------------------------------- classes

def classes_dir(bot: Bot) -> Path:
    """A folder holding exactly <pkg>/'s class files of BOT, made once from its jar or folder."""
    final = NDATA / "classes" / f"{bot.pkg}-{bot.hash}"

    def make(tmp: Path) -> None:
        src = Path(bot.source)
        prefix = bot.pkg.replace(".", "/") + "/"
        if src.is_file():
            with zipfile.ZipFile(src) as z:
                for name in z.namelist():
                    if name.startswith(prefix) and not name.endswith("/"):
                        (tmp / name).parent.mkdir(parents=True, exist_ok=True)
                        (tmp / name).write_bytes(z.read(name))
        else:
            shutil.copytree(src / prefix, tmp / prefix)
            if store.bot_classes(bot.pkg, str(tmp))[1] != bot.hash:
                raise store.SetupError(f"{src / prefix} changed while it was being copied")

    return store._atomic_dir(final, make)


# -------------------------------------------------------------------------------------------------------- builds

def _flock(path: Path, block: bool = True):
    path.parent.mkdir(parents=True, exist_ok=True)
    f = open(path, "a")
    try:
        fcntl.flock(f, fcntl.LOCK_EX | (0 if block else fcntl.LOCK_NB))
    except OSError:
        f.close()
        return None
    return f


def run_measured(cmd: list[str], **kw) -> tuple[int, float]:
    """Runs CMD; (exit code, CPU seconds of it and every descendant it waited for)."""
    proc = subprocess.Popen(cmd, **kw)
    _, status, ru = os.wait4(proc.pid, 0)
    proc.returncode = os.waitstatus_to_exitcode(status)
    return proc.returncode, ru.ru_utime + ru.ru_stime


def sync_engine(snap: Path, gen: Path) -> Path:
    """SNAP's engine sources at a fixed path for GEN, rewriting only the files that changed: the compile commands name
    the runtime folder, so a new path would recompile every module."""
    dest = gen.with_name(gen.name + ".engine")
    files = {p.relative_to(snap): p for p in snap.rglob("*") if p.is_file()}
    for rel, p in files.items():
        q = dest / rel
        data = p.read_bytes()
        if not q.exists() or q.read_bytes() != data:
            q.parent.mkdir(parents=True, exist_ok=True)
            q.write_bytes(data)
    for q in list(dest.rglob("*")):
        rel = q.relative_to(dest)
        if q.is_file() and rel not in files and rel.parts[:2] != ("engine", "build"):  # build/: its maps
            q.unlink()
    return dest


@contextmanager
def gen_dir(version: str, eid: str, ids: list[list[str]]):
    """One of BUILDS generated-files folders of VERSION, locked; the free one sharing the most modules with IDS."""
    dirs = [NDATA / "gen" / f"{version}-{i}" for i in range(BUILDS)]

    def shared(g: Path) -> tuple[int, float]:
        try:
            last = json.loads((g / "last.json").read_text())
        except (OSError, ValueError):
            return (0, 0.0)
        same = sum(1 for x, y in zip(last.get("bots", []), ids) if x == y)
        return (1000 * (last.get("engine") == eid) + same, -(g / "last.json").stat().st_mtime)

    waited = False
    while True:
        for g in sorted(dirs, key=shared, reverse=True):
            lock = _flock(g / ".lock", block=False)
            if lock is None:
                continue
            try:
                yield g
            finally:
                fcntl.flock(lock, fcntl.LOCK_UN)
                lock.close()
            return
        if not waited:
            _say(f"waiting for one of the {BUILDS} build slots of this host")
            waited = True
        time.sleep(3)


def binary_dir(key: str) -> Path:
    return NDATA / "bin" / key


def bad(key: str) -> str | None:
    p = binary_dir(key) / "BAD"
    return p.read_text() if p.exists() else None


def mark_bad(key: str, why: str) -> None:
    d = binary_dir(key)
    if d.exists():
        (d / "BAD").write_text(why)


_touched: dict[str, float] = {}


def find(plan: Plan, eid: str) -> str | None:
    """The most recently used good binary of this engine holding the game's bots, if any."""
    need = {(b.pkg, b.hash) for b in plan.need}
    best, when = None, -1.0
    try:
        found = list((NDATA / "bin").iterdir())
    except OSError:
        return None
    for d in found:
        try:
            info = json.loads((d / "build.json").read_text())
            used = max((d / f).stat().st_mtime for f in ("used", "build.json") if (d / f).exists())
        except (OSError, ValueError):
            continue
        if (info.get("version") == plan.version and info.get("engine") == eid and not (d / "BAD").exists()
                and (d / "bcx").exists() and need <= {tuple(x) for x in info["bots"]} and used > when):
            best, when = d.name, used
    return best


def build(plan: Plan) -> tuple[str, Path]:
    """(key, binary) for PLAN: the plan's own binary, else any binary holding the game's bots, else the plan's
    binary built. Blocks while another process of this host builds the same one."""
    eid, snap = snapshot(plan.version)
    ids = plan.ids()
    key = binary_key(plan.version, eid, ids)
    d = binary_dir(key)
    if not (d / "bcx").exists() and plan.need and (other := find(plan, eid)):
        key, d = other, binary_dir(other)
    if not (d / "bcx").exists():
        lock = _flock(NDATA / "bin" / f"{key}.lock")
        try:
            if not (d / "bcx").exists():
                _build(plan, eid, snap, ids, key)
        finally:
            fcntl.flock(lock, fcntl.LOCK_UN)
            lock.close()
        try:
            gc()
        except OSError as e:  # the bound on bin/ is the next gc's: never a reason to play this run on Java
            _say(f"gc of {NDATA / 'bin'}: {e}")
    why = bad(key)
    if why:
        raise BuildError(f"binary {key} is marked bad: {why}")
    hold(key)
    if not (d / "bcx").exists():  # gc removed it before the hold: build it again
        return build(plan)
    if time.time() - _touched.get(key, 0) > 60:
        (d / "used").touch()
        _touched[key] = time.time()
    return key, d / "bcx"


def _build(plan: Plan, eid: str, snap: Path, ids: list[list[str]], key: str) -> None:
    bins = NDATA / "bin"
    failed = bins / f"{key}.failed"
    if failed.exists() and time.time() - failed.stat().st_mtime < RETRY_FAILED_S:
        raise BuildError(failed.read_text())
    for attempt in (1, 2):
        try:
            return _build_once(plan, eid, snap, ids, key)
        except BuildError as e:
            # a step killed by a signal (the host's OOM killer) says nothing of the build: try once more
            if "failed (-" not in str(e) or attempt == 2:
                failed.write_text(str(e))
                raise
            _say(f"build of {key} was killed ({str(e).splitlines()[0]}); trying again in 60 s")
            time.sleep(60)


def _build_once(plan: Plan, eid: str, snap: Path, ids: list[list[str]], key: str) -> None:
    bins = NDATA / "bin"
    teams = [(b.pkg, classes_dir(b)) for b in plan.bots]
    log = bins / f"{key}.log"
    with gen_dir(plan.version, eid, ids) as gen:
        (gen / "last.json").unlink(missing_ok=True)
        src = sync_engine(snap, gen)
        cmd = ["nice", "-n", str(NICE), sys.executable, str(src / "engine" / "build.py"), "--engine", plan.version,
               "-j", str(JOBS), f"--glue-opt={GLUE_OPT}", f"--opt={OPT}", f"--lto={LTO}", "--gen", str(gen), "--out", str(gen / "bcx")]
        for pkg, c in teams:
            cmd += ["--team", f"{pkg}={c}"]
        _say(f"building binary {key} ({len(teams)} bots: {', '.join(p for p, _ in teams[len(BASE):]) or 'base'}) "
             f"in {gen.name}; log {log}")
        t0 = time.time()
        # the translator: serial GC (3.7 GB peak instead of 4.9), one at a time on the host (BCX_TRANSLATE_LOCK)
        env = dict(os.environ, BCX_TRANSLATE_LOCK=str(NDATA / "translate.lock"), JAVA_TOOL_OPTIONS="-XX:+UseSerialGC")
        with open(log, "w") as lf:
            lf.write(" ".join(cmd) + "\n")
            lf.flush()
            code, cpu = run_measured(cmd, stdout=lf, stderr=subprocess.STDOUT, cwd=src, env=env)
        wall = time.time() - t0
        if code != 0:
            out = log.read_text()
            out = out[out.find("\n") + 1:]  # the output, not the command
            raise BuildError(f"build of {key} failed (exit {code}), {log}:\n{out[-3000:]}")
        tmp = Path(tempfile.mkdtemp(prefix=f".{key}.", dir=bins))
        try:
            subprocess.run(["strip", "-o", str(tmp / "bcx"), str(gen / "bcx")], check=True, capture_output=True)
            info = {"key": key, "version": plan.version, "engine": eid, "bots": ids, "built": time.time(),
                    "wall_s": round(wall, 1), "cpu_s": round(cpu, 1), "gen": gen.name, "host": os.uname().nodename}
            (tmp / "build.json").write_text(json.dumps(info))
            os.rename(tmp, binary_dir(key))
        finally:
            shutil.rmtree(tmp, ignore_errors=True)
        (gen / "last.json").write_text(json.dumps({"engine": eid, "bots": ids}))
        (bins / f"{key}.failed").unlink(missing_ok=True)
        _say(f"built {key} in {wall:.0f} s ({cpu:.0f} CPU s)")


_held: dict[str, object] = {}


def hold(key: str) -> None:
    """KEY in use by this process until it ends: a shared flock on bin/<key>.use, which gc() never takes from it."""
    if key in _held:
        return
    f = open(NDATA / "bin" / f"{key}.use", "a")
    fcntl.flock(f, fcntl.LOCK_SH)  # waits while gc() removes it
    os.utime(f.name)  # fresh: gc() never unlinks the lock file of a binary it can't see yet
    _held[key] = f


def gc(dry: bool = False) -> list[tuple[str, int]]:
    """bin/ bounded (the module's docstring): the binaries removed (or, DRY, to be), with their bytes. One process
    at a time (bin/.gc.lock; a second skips): a binary another gc removed while this one sized it raised
    FileNotFoundError out of build(), and the runs waiting on that build played Java (21:46:09 on 19 Nov: three
    processes released by one build's lock all ran gc at once)."""
    bins = NDATA / "bin"
    lock = _flock(bins / ".gc.lock", block=False)
    if lock is None:
        return []
    try:
        return _gc(bins, dry)
    finally:
        lock.close()


def _gc(bins: Path, dry: bool) -> list[tuple[str, int]]:
    try:
        found = [d for d in bins.iterdir() if d.is_dir() and not d.name.startswith(".")]
    except OSError:
        return []

    def used(d: Path) -> float:
        for f in ("used", "build.json"):
            try:
                return (d / f).stat().st_mtime
            except OSError:
                pass
        return 0.0

    def size(d: Path) -> int:
        try:
            return sum(f.stat().st_size for f in d.iterdir() if f.is_file())
        except OSError:  # removed meanwhile (a build's rename, a gc of an older version of this code)
            return 0

    now = time.time()
    gone, kept, total = [], 0, 0
    for d in sorted(found, key=used, reverse=True):
        n, age = size(d), now - used(d)
        if age > GRACE_S and not ((kept < KEEP or age < KEEP_S) and total + n <= KEEP_BYTES):
            with open(bins / f"{d.name}.use", "a") as f:
                try:
                    fcntl.flock(f, fcntl.LOCK_EX | fcntl.LOCK_NB)
                except BlockingIOError:  # a live process plays or ships it
                    pass
                else:
                    if not dry:
                        shutil.rmtree(d, ignore_errors=True)
                    gone.append((d.name, n))
                    continue
        kept += 1
        total += n
    names = {d.name for d in found} - {k for k, _ in gone}
    for f in bins.iterdir():  # what is left of binaries gone a day: their build log, locks, failure note
        stem = f.name.split(".")[0]
        if f.is_file() and stem and stem not in names and now - f.stat().st_mtime > 86400 and not dry:
            f.unlink(missing_ok=True)
    for d in bins.glob(".*.*"):
        if d.is_dir() and now - d.stat().st_mtime > 3600 and not dry:
            shutil.rmtree(d, ignore_errors=True)
    return gone


# ---------------------------------------------------------------------------------------------------------- games

NO_ROOM_MB = 1024  # under this free where a game writes, a native failure is put down to the disk


def no_room(error: str, where: Path) -> bool:
    """A native failure for want of disk: ENOSPC, or any failure with under NO_ROOM_MB free at WHERE (a full disk
    also shows as a bcx that exits 0 without its result, or dies of SIGBUS: 111 games on 4 lab machines, 19 Nov)."""
    if "No space left" in error or "Errno 28" in error:
        return True
    try:
        st = os.statvfs(where)
    except OSError:
        return False
    return st.f_bavail * st.f_frsize < NO_ROOM_MB << 20


LOCAL_KEEP_S = 1800  # a binary of the piece's cache unused this long goes when another one is unpacked


def local_binary(path: str) -> Path:
    """A binary a spec names (a remote node: on the shared volume, maybe gzip'd), copied once to this machine's disk
    so games don't page it in over the network: into JOB_CACHE (tools/piece.py: shared by the tasks of one piece, so
    unpacked once a piece under a lock, not 100 MB a task), else the task's TMPDIR. Binaries there unused for
    LOCAL_KEEP_S go when another is unpacked. When that disk is full (seen on DoC), unpacked next to it on the volume,
    once for every node: slower, never elsewhere."""
    src = Path(path)
    name = src.name[:-3] if src.name.endswith(".gz") else src.name
    local = Path(os.environ.get("JOB_CACHE") or tempfile.gettempdir()) / "bcx"
    errors = []
    for final in dict.fromkeys([local / name, src.parent / name]):
        if final.exists():
            if final.parent == local:
                os.utime(final)  # in use: kept
            return final
        tmp = None
        try:
            final.parent.mkdir(parents=True, exist_ok=True)
            with open(final.parent / f".{name}.lock", "a") as lock:
                fcntl.flock(lock, fcntl.LOCK_EX)  # one task of the piece unpacks it, the others wait for it
                if final.exists():
                    return final
                fd, tmp = tempfile.mkstemp(prefix=name + ".", dir=final.parent)
                with os.fdopen(fd, "wb") as out:
                    if src.name.endswith(".gz"):
                        import gzip
                        with gzip.open(src) as f:
                            shutil.copyfileobj(f, out, 1 << 20)
                    else:
                        with open(src, "rb") as f:
                            shutil.copyfileobj(f, out, 1 << 20)
                os.chmod(tmp, 0o700)
                os.replace(tmp, final)
            if final.parent == local:
                now = time.time()
                for old in local.iterdir():  # a running game keeps its unlinked binary
                    if old.name != name and not old.name.startswith(".") and now - old.stat().st_mtime > LOCAL_KEEP_S:
                        old.unlink(missing_ok=True)
            return final
        except OSError as e:  # a full disk (seen on DoC): the next place
            errors.append(f"{final.parent}: {e}")
            if tmp:
                Path(tmp).unlink(missing_ok=True)
    raise OSError("; ".join(errors))


SPEC_FIELDS = ("id", "a", "b", "map", "engine", "classes", "classes_a", "classes_b", "replay", "indicators", "bcx")


def play_game(binary: Path, spec: dict, a: Path, b: Path, workdir: Path, timeout: float,
              replay: str | None) -> dict:
    """One game in its own bcx process: the result's outcome fields (engine/AGENTS.md), or {"error": ...}."""
    workdir.mkdir(parents=True, exist_ok=True)
    g = Path(tempfile.mkdtemp(prefix="g-", dir=workdir))
    try:
        line = {"id": "g", "a": spec["a"].partition("@")[0], "b": spec["b"].partition("@")[0], "map": spec["map"],
                "seed": int(spec["seed"]), "engine": spec["engine"], "classes_a": str(a), "classes_b": str(b),
                "replay": bool(replay), "indicators": False}
        (g / "spec.jsonl").write_text(json.dumps(line) + "\n")
        proc = subprocess.Popen([str(binary), "play", str(g / "spec.jsonl"), str(g / "out"), "-j", "1",
                                 "--no-indicators", "--root", str(store.ROOT)],
                                stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, start_new_session=True)
        try:
            out, err = proc.communicate(timeout=timeout)
        except subprocess.TimeoutExpired:
            os.killpg(proc.pid, signal.SIGKILL)
            proc.communicate()
            return {"error": f"native: timeout after {timeout:.0f} s"}
        lines = [x for x in out.splitlines() if x.strip()]
        try:
            r = json.loads(lines[-1])
        except (IndexError, ValueError):
            return {"error": f"native: no result (exit {proc.returncode}): {err.strip()[-500:]}"}
        if r.get("error"):
            return {"error": f"native: {r['error']}"}
        res = {k: v for k, v in r.items() if k not in SPEC_FIELDS}
        if replay:
            Path(replay).parent.mkdir(parents=True, exist_ok=True)
            shutil.move(str(g / "out" / "g" / "replay.bc26"), replay)
        return res
    finally:
        shutil.rmtree(g, ignore_errors=True)


class Backend:
    """Native games for play.Runner. submit() plans the job's binary (False: not native, play it on Java); a thread
    builds each missing binary once; WORKERS threads play through the runner's gate. on_result(job, res) gets the
    outcome, or {"error": ...} when the native engine couldn't play it (a build failure, a bad binary, an engine
    error), which the runner plays on Java."""

    def __init__(self, resolver, workers: int, workdir: Path, timeout: float, gate):
        self.planner = Planner(resolver)
        self.workdir, self.timeout, self.gate = workdir, min(timeout, TIMEOUT), gate
        self.ready: dict[Plan, tuple[str, Path] | Exception] = {}
        self.waiting: dict[Plan, list] = {}
        self.lock = threading.Lock()
        self.q: queue.Queue = queue.Queue()
        self.closed = False
        self.threads = [threading.Thread(target=self._loop, daemon=True) for _ in range(workers)]
        for t in self.threads:
            t.start()

    def submit(self, job, on_result) -> bool:
        try:
            plan = self.planner.plan(job)
        except (store.SetupError, BuildError) as e:
            _say(f"{job.spec.get('id')}: not native: {e}")
            return False
        if plan is None:
            return False
        with self.lock:
            got = self.ready.get(plan)
            if got is None:
                first = plan not in self.waiting
                self.waiting.setdefault(plan, []).append((job, on_result))
                if first:
                    threading.Thread(target=self._prepare, args=(plan,), daemon=True).start()
                return True
        self._enqueue(plan, got, job, on_result)
        return True

    def _prepare(self, plan: Plan) -> None:
        try:
            if plan.binary:
                local = local_binary(plan.binary)
                got = (local.name, local)
            else:
                got = build(plan)
            for b in plan.bots:
                classes_dir(b)
        except Exception as e:  # noqa: BLE001 - every job of this binary goes to Java with the reason
            got = e
        with self.lock:
            self.ready[plan] = got
            jobs = self.waiting.pop(plan, [])
        for job, cb in jobs:
            self._enqueue(plan, got, job, cb)

    def _enqueue(self, plan: Plan, got, job, cb) -> None:
        if isinstance(got, Exception):
            cb(job, {"error": f"native: {type(got).__name__}: {str(got)[-1500:]}"})
            return
        key, binary = got
        why = bad(key) if not plan.binary else None
        if why:
            cb(job, {"error": f"native: binary {key} is marked bad: {why}"})
            return
        self.q.put((plan, key, binary, job, cb))

    def _loop(self) -> None:
        while True:
            item = self.q.get()
            if item is None or self.closed:
                return
            plan, key, binary, job, cb = item
            token = self.gate.acquire()
            if self.closed:
                self.gate.release(token)
                return
            try:
                a, b = (classes_dir(x) for x in self.planner.game_bots(job))
                res = play_game(binary, job.spec, a, b, self.workdir, self.timeout, job.replay or None)
            except Exception as e:  # noqa: BLE001
                res = {"error": f"native: runner: {type(e).__name__}: {e}"}
            finally:
                self.gate.release(token)
            if not res.get("error"):
                res["backend"] = "native"
                res["native_build"] = key
            if not self.closed:
                cb(job, res)

    def close(self) -> None:
        self.closed = True
        for _ in self.threads:
            self.q.put(None)


# ---------------------------------------------------------------------------------------------- exactness guard

GUARD_RATE = float(os.environ.get("BCGAMES_GUARD", 1 / 32))  # share of native games also played on Java


def sampled(key: str, rate: float) -> bool:
    """Deterministic by game key: the same games are guarded whoever plays them."""
    return rate > 0 and int(key[:8], 16) < rate * 0x100000000


def comparable(r: dict, exact_crashes: bool) -> dict:
    """What two engines must agree on for one game: the replay (digest), and the outcome fields. A game the engine
    ends by its unseeded coin flip (WON_BY_DUBIOUS_REASONS) agrees up to the flip: no winner, no digest."""
    out = {k: r.get(k) for k in ("winner", "win_type", "round", "cooperation", "cat_rng_draws", "series", "digest")}
    for t in ("A", "B"):
        out[t] = {k: v for k, v in (r.get(t) or {}).items() if k != "crashed" or exact_crashes}
    if r.get("win_type") == "WON_BY_DUBIOUS_REASONS":
        out.pop("winner"), out.pop("digest")
    return out


def verdict(native: dict, java1: dict, java2: dict | None = None, mirror: bool = False) -> str:
    """same: the engines agree. java-nondeterministic: the Java engine's two runs differ and one equals the native
    run (engine/AGENTS.md "Known gaps": the official engine is not deterministic on some machines). mismatch: neither
    explains it (the Java runs agree with each other, or neither equals the native run): a native engine bug until
    shown otherwise. pending: a second Java run is needed.
    Crash counts are compared only where tools/play's Java scanner attributes them exactly: two packages (MIRROR
    false) and no crash it couldn't attribute (engine_errors)."""
    exact = not mirror and not any((x or {}).get("engine_errors") for x in (java1, java2))
    n, j1 = comparable(native, exact), comparable(java1, exact)
    if n == j1:
        return "same"
    if java2 is None:
        return "pending"
    j2 = comparable(java2, exact)
    if j2 == n and j2 != j1:
        return "java-nondeterministic"
    return "mismatch"
