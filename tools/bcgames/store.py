"""Content-addressed inputs and the result cache, under ~/battlecode26-data/games (BCGAMES_DATA overrides).

engines/  official engine jars, sha256-checked        runner/<h>/  compiled bcplay classes
bots/     <pkg>-<srchash>.jar, one per source tree     maps/<name>-<maphash>/s<seed>/<name>.map26  seed variants
cache/games.sqlite  game results by key                runs/<id>/   tools/games runs
"""
from __future__ import annotations

import hashlib
import json
import os
import shutil
import sqlite3
import struct
import subprocess
import tarfile
import tempfile
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = Path(os.environ.get("BCGAMES_DATA", "~/battlecode26-data/games")).expanduser()
JAVA_DIR = Path(__file__).resolve().parent / "java"

ENGINES = {  # version -> sha256 of the official jar (releases.battlecode.org)
    "1.2.5": "b9f39d19a2a0bccd35540039747bacd0412b6016b2ef64c39846395989f20488",
    "1.2.6": "ea722532f6d523241ff4f708078563de2a010a39d7d524c66ae1890ab85c04ec",
}
DEFAULT_ENGINE = "1.2.6"
ENGINE_URL = "https://releases.battlecode.org/maven/org/battlecode/battlecode26-java/{v}/battlecode26-java-{v}.jar"
# Same flags as Gradle's compileJava in build.gradle: the class files are byte-identical to build/classes.
JAVAC_FLAGS = ["-g", "-encoding", "UTF-8", "-source", "21", "-target", "21", "-nowarn"]


class SetupError(Exception):
    pass


def sha256_bytes(b: bytes) -> str:
    return hashlib.sha256(b).hexdigest()


def sha256_file(p: Path) -> str:
    h = hashlib.sha256()
    with open(p, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def _atomic_dir(final: Path, build) -> Path:
    """Build a directory with build(tmpdir) and rename it into place; concurrent builders are harmless."""
    if final.exists():
        return final
    final.parent.mkdir(parents=True, exist_ok=True)
    tmp = Path(tempfile.mkdtemp(prefix=final.name + ".tmp", dir=final.parent))
    try:
        build(tmp)
        try:
            os.rename(tmp, final)
        except OSError:
            if not final.exists():
                raise
    finally:
        shutil.rmtree(tmp, ignore_errors=True)
    return final


def _atomic_write(final: Path, data: bytes) -> None:
    final.parent.mkdir(parents=True, exist_ok=True)
    fd, tmp = tempfile.mkstemp(prefix=final.name + ".tmp", dir=final.parent)
    with os.fdopen(fd, "wb") as f:
        f.write(data)
    os.replace(tmp, final)


# ---------------------------------------------------------------------------------------------------------- engines

def engine_jar(version: str) -> Path:
    if version not in ENGINES:
        raise SetupError(f"unknown engine {version!r} (known: {', '.join(ENGINES)})")
    jar = DATA / "engines" / f"battlecode26-java-{version}.jar"
    if not jar.exists():
        with urllib.request.urlopen(ENGINE_URL.format(v=version), timeout=120) as r:
            data = r.read()
        if sha256_bytes(data) != ENGINES[version]:
            raise SetupError(f"engine {version}: downloaded jar has the wrong sha256")
        _atomic_write(jar, data)
    return jar


def _java_props() -> dict[str, str]:
    proc = subprocess.run(["java", "-XshowSettings:properties", "-version"], capture_output=True, text=True)
    props = {}
    for line in proc.stderr.splitlines():
        k, sep, v = line.strip().partition("=")
        if sep:
            props[k.strip()] = v.strip()
    if "java.home" not in props:
        raise SetupError("cannot run java (is a JDK 21 on PATH?)")
    return props


def jdk_fingerprint() -> str:
    """The JDK as games see it (bcplay.JdkPrint): part of every game key, because bots' java.util classes are
    instrumented from the running JDK. Ubuntu's OpenJDK 21.0.13 and Temurin 21.0.13+9 (DoC's nodes) print the same.
    Cached per JDK install."""
    props = _java_props()
    ident = sha256_bytes((props["java.home"] + "|" + props.get("java.runtime.version", "")).encode())[:16]
    cache = DATA / "jdk" / f"{ident}.txt"
    if cache.exists():
        return cache.read_text().strip()
    runner_dir, _ = runner_classes()
    proc = subprocess.run(["java", "-cp", f"{engine_jar(DEFAULT_ENGINE)}{os.pathsep}{runner_dir}", "bcplay.JdkPrint"],
                          capture_output=True, text=True)
    if proc.returncode != 0:
        raise SetupError("bcplay.JdkPrint failed: " + proc.stderr[-500:])
    fp = proc.stdout.split()[0]
    _atomic_write(cache, fp.encode())
    return fp


def runner_classes() -> tuple[Path, str]:
    """Compiles tools/bcgames/java once per source version. Returns (class dir, runner version).

    The runner version, part of every game key, hashes only Play.java: the game loop. MapInfo changes don't touch
    game results.
    """
    sources = sorted(JAVA_DIR.rglob("*.java"))
    h = hashlib.sha256()
    for s in sources:
        h.update(s.relative_to(JAVA_DIR).as_posix().encode() + b"\0" + s.read_bytes() + b"\0")
    version = sha256_file(JAVA_DIR / "bcplay" / "Play.java")[:12]

    def build(tmp: Path) -> None:
        proc = subprocess.run(["javac", "-nowarn", "-d", str(tmp), "-cp", str(engine_jar(DEFAULT_ENGINE))]
                              + [str(s) for s in sources], capture_output=True, text=True)
        if proc.returncode != 0:
            raise SetupError("javac bcplay failed:\n" + proc.stderr[-3000:])

    return _atomic_dir(DATA / "runner" / h.hexdigest()[:12], build), version


# ------------------------------------------------------------------------------------------------------------- bots

def parse_bot(ref: str) -> tuple[str, str | None]:
    """'pkg' (working tree) or 'pkg@REV' (git revision of this repo) -> (pkg, rev)."""
    pkg, _, rev = ref.partition("@")
    if not pkg or not all(part.isidentifier() for part in pkg.split(".")):
        raise SetupError(f"bad bot {ref!r}: expected a package name, optionally @REV")
    return pkg, rev or None


def _bot_source(pkg: str, rev: str | None, dest: Path) -> None:
    rel = "src/" + pkg.replace(".", "/")
    if rev is None:
        src = ROOT / rel
        if not (src / "RobotPlayer.java").exists():
            raise SetupError(f"bot {pkg!r}: no {rel}/RobotPlayer.java")
        shutil.copytree(src, dest / rel)
        return
    proc = subprocess.run(["git", "-C", str(ROOT), "archive", "--format=tar", rev, rel], capture_output=True)
    if proc.returncode != 0:
        raise SetupError(f"bot {pkg}@{rev}: git archive failed: {proc.stderr.decode()[-500:]}")
    tar_path = dest / "src.tar"
    tar_path.write_bytes(proc.stdout)
    with tarfile.open(tar_path) as t:
        if hasattr(tarfile, "data_filter"):
            t.extractall(dest, filter="data")
        else:
            t.extractall(dest)
    tar_path.unlink()
    if not (dest / rel / "RobotPlayer.java").exists():
        raise SetupError(f"bot {pkg}@{rev}: no {rel}/RobotPlayer.java at that revision")


def _tree_hash(base: Path) -> str:
    h = hashlib.sha256()
    for p in sorted(x for x in base.rglob("*") if x.is_file()):
        h.update(p.relative_to(base).as_posix().encode() + b"\0" + p.read_bytes() + b"\0")
    return h.hexdigest()


def bot_jar(ref: str) -> tuple[Path, str]:
    """Compiles a bot alone (like the submission server) into a jar. Returns (jar, class hash).

    The jar is cached by source hash; the class hash (sha256 over its class files) is what game results are keyed on.
    """
    pkg, rev = parse_bot(ref)
    with tempfile.TemporaryDirectory(prefix="bcbot-") as tmp:
        tmp = Path(tmp)
        _bot_source(pkg, rev, tmp)
        srchash = _tree_hash(tmp / "src")[:16]
        jar = DATA / "bots" / f"{pkg}-{srchash}.jar"
        meta = jar.with_suffix(".json")
        if jar.exists() and meta.exists():
            return jar, json.loads(meta.read_text())["class_hash"]
        out = tmp / "classes"
        out.mkdir()
        files = sorted(str(p) for p in (tmp / "src").rglob("*.java"))
        proc = subprocess.run(["javac", *JAVAC_FLAGS, "-d", str(out), "-cp", str(engine_jar(DEFAULT_ENGINE)), *files],
                              capture_output=True, text=True)
        if proc.returncode != 0:
            raise SetupError(f"bot {ref}: compile error\n" + proc.stdout[-3000:] + proc.stderr[-3000:])
        classes = sorted(p for p in out.rglob("*") if p.is_file())
        if not classes:
            raise SetupError(f"bot {ref}: no classes")
        h = hashlib.sha256()
        buf = tmp / "bot.jar"
        with zipfile.ZipFile(buf, "w", zipfile.ZIP_STORED) as z:
            for p in classes:
                name = p.relative_to(out).as_posix()
                data = p.read_bytes()
                h.update(name.encode() + b"\0" + data + b"\0")
                info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
                z.writestr(info, data)
        class_hash = h.hexdigest()[:16]
        _atomic_write(meta, json.dumps({"bot": ref, "class_hash": class_hash}).encode())
        _atomic_write(jar, buf.read_bytes())
        return jar, class_hash


def bot_classes(pkg: str, path: str) -> tuple[Path, str]:
    """A prebuilt bot: a class directory or jar holding <pkg>/... Returns (path, class hash over the package)."""
    p = Path(path).expanduser().resolve()
    prefix = pkg.replace(".", "/") + "/"
    h = hashlib.sha256()
    n = 0
    if p.is_dir():
        for f in sorted(x for x in (p / prefix).rglob("*") if x.is_file()):
            h.update(f.relative_to(p).as_posix().encode() + b"\0" + f.read_bytes() + b"\0")
            n += 1
    elif p.is_file():
        with zipfile.ZipFile(p) as z:
            for name in sorted(i for i in z.namelist() if i.startswith(prefix) and not i.endswith("/")):
                h.update(name.encode() + b"\0" + z.read(name) + b"\0")
                n += 1
    if n == 0:
        raise SetupError(f"no classes of {pkg} in {path}")
    return p, h.hexdigest()[:16]


# ------------------------------------------------------------------------------------------------------------- maps

def map_file(name: str) -> Path:
    p = ROOT / "maps" / f"{name}.map26"
    if not p.exists():
        raise SetupError(f"unknown map {name!r} (no maps/{name}.map26)")
    return p


def _seed_offset(b: bytes) -> int:
    """Byte offset of GameMap.randomSeed (flatbuffer field 4, vtable slot at +12) in a .map26 file."""
    root = struct.unpack_from("<I", b, 0)[0]
    vt = root - struct.unpack_from("<i", b, root)[0]
    if struct.unpack_from("<H", b, vt)[0] <= 12 or struct.unpack_from("<H", b, vt + 12)[0] == 0:
        raise SetupError("map has no explicit randomSeed field")
    return root + struct.unpack_from("<H", b, vt + 12)[0]


def embedded_seed(name: str) -> int:
    b = map_file(name).read_bytes()
    return struct.unpack_from("<i", b, _seed_offset(b))[0]


def map_hash(name: str) -> str:
    return sha256_file(map_file(name))[:16]


def map_variant(name: str, seed: int) -> Path:
    """Directory holding <name>.map26 with its embedded seed replaced by `seed` (4 bytes patched in place).

    The unmodified engine then plays exactly what the src/battlecode seed override plays with -Pseed=seed.
    """
    b = bytearray(map_file(name).read_bytes())
    d = DATA / "maps" / f"{name}-{sha256_bytes(bytes(b))[:16]}" / f"s{seed}"
    target = d / f"{name}.map26"
    if not target.exists():
        struct.pack_into("<i", b, _seed_offset(bytes(b)), seed)
        _atomic_write(target, bytes(b))
    return d


# ------------------------------------------------------------------------------------------------------------ cache

class Cache:
    """Game results by key. SQLite in WAL mode, so concurrent processes can share it."""

    def __init__(self, path: Path | None = None):
        path = path or DATA / "cache" / "games.sqlite"
        path.parent.mkdir(parents=True, exist_ok=True)
        self.db = sqlite3.connect(str(path), timeout=60, check_same_thread=False)
        self.db.execute("PRAGMA journal_mode=WAL")
        self.db.execute("CREATE TABLE IF NOT EXISTS games (key TEXT PRIMARY KEY, result TEXT NOT NULL)")
        self.db.commit()

    def get(self, key: str) -> dict | None:
        row = self.db.execute("SELECT result FROM games WHERE key = ?", (key,)).fetchone()
        return json.loads(row[0]) if row else None

    def put(self, key: str, result: dict) -> None:
        self.db.execute("INSERT OR REPLACE INTO games (key, result) VALUES (?, ?)", (key, json.dumps(result)))
        self.db.commit()

    def purge_native(self, build: str) -> int:
        """Removes every result a native binary played (native_build); returns how many."""
        n = self.db.execute("DELETE FROM games WHERE json_extract(result, '$.native_build') = ?", (build,)).rowcount
        self.db.commit()
        return n


def game_key(runner: str, engine: str, a_hash: str, b_hash: str, a_pkg: str, b_pkg: str, map_name: str,
             mhash: str, seed: int) -> str:
    """Everything a game's result depends on. JVM flags, load and games-per-JVM don't (docs/testing.md)."""
    parts = [runner, engine, ENGINES[engine], a_pkg, a_hash, b_pkg, b_hash, map_name, mhash, seed]
    return sha256_bytes(json.dumps(parts).encode())[:32]
