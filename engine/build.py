#!/usr/bin/env python3
"""Build the native engine: engine/build/bcx (engine 1.2.6) and, with --engine 1.2.5, engine/build/bcx-1.2.5.

    engine/build.py [--engine 1.2.6|1.2.5|all] [--bots PKG,PKG|all] [-j N] [--opt -O3] [--lto thin|off]
    engine/build.py --engine 1.2.6 --team PKG=CLASSES_DIR... --gen DIR --out FILE    (tools/bcgames/native.py)

Steps: fetch the jars and bdwgc once (~/battlecode26-data/engine/), compile the translator (engine/jvm), translate
the official engine and every bot package (each package under src/ with a RobotPlayer, from build/classes: run
./gradlew classes first) to C++ in ~/battlecode26-data/engine/build/gen-VERSION-CHECKOUT/, compile it with ninja and
link.
Generated files are rewritten only when they change, so a rebuild recompiles only what moved.
--team takes each package from its own classes folder instead of build/classes, in the order given (a team's module
index is its position, so a stable order keeps rebuilds incremental); --gen and --out name the generated-files folder
and the binary.
"""
import argparse
import hashlib
import os
import shutil
import subprocess
import sys
import urllib.request
import zipfile
from pathlib import Path

ENGINE = Path(__file__).resolve().parent
REPO = ENGINE.parent
DATA = Path.home() / "battlecode26-data" / "engine"
JARS = DATA / "jars"
DEPS = DATA / "deps"
OUT = ENGINE / "build"
MAVEN = "https://repo1.maven.org/maven2/org/ow2/asm"
ASM = ["asm-commons", "asm-analysis", "asm-util", "asm-tree"]
GC_VERSION = "8.2.8"
# robots run on their own stacks: the collector must not clear below the stack it believes in
GC_CFLAGS = "-O2 -fPIC -DALWAYS_SMALL_CLEAR_STACK"
# edits to the collector's source (file, text, replacement), each exact in behaviour:
# - GC_allochblk tries each larger free list in turn for a heap block; an empty list makes GC_allochblk_nth return
#   NULL at once, and after a collection most lists are empty, so the call is skipped (a refill of small objects
#   asks for a block at a time)
GC_PATCHES = [("allchblk.c",
               "    for (; start_list <= split_limit; ++start_list) {\n"
               "        result = GC_allochblk_nth(sz, kind, flags, start_list, may_split);",
               "    for (; start_list <= split_limit; ++start_list) {\n"
               "        if (NULL == GC_hblkfreelist[start_list]) continue;\n"
               "        result = GC_allochblk_nth(sz, kind, flags, start_list, may_split);")]
# the collector's source, build and install, one per configuration (DEPS is shared by every worktree)
GC_DIR = "gc-" + hashlib.sha256(repr((GC_CFLAGS, GC_PATCHES)).encode()).hexdigest()[:8]
GC_SHA256 = "7649020621cb26325e1fb5c8742590d92fb48ce5c259b502faf7d9fb5dabb160"
JAR_SHA256 = {"1.2.6": "ea722532f6d523241ff4f708078563de2a010a39d7d524c66ae1890ab85c04ec",
              "1.2.5": "b9f39d19a2a0bccd35540039747bacd0412b6016b2ef64c39846395989f20488"}
ENTRY = ("bcx/NativeMain.play(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZI"
         "Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;JZ)Ljava/lang/String;")
# -ffp-exception-behavior=strict: no compile-time folding of an FP operation that can raise (clang folds
# -inf*0 to +NaN; the CPU, so the JVM, gives -NaN)
CXXFLAGS = ["-std=c++20", "-g1", "-fwrapv", "-fno-strict-aliasing", "-ffp-contract=off", "-ffp-exception-behavior=strict",
            "-fno-rtti", "-fno-exceptions", "-w"]


def sh(cmd, **kw):
    r = subprocess.run(cmd, **kw)
    if r.returncode != 0:
        sys.exit(f"build: failed ({r.returncode}): {' '.join(map(str, cmd))}")
    return r


def sha256(p: Path) -> str:
    return hashlib.sha256(p.read_bytes()).hexdigest()


def fetch(url: str, dest: Path, digest: str = None):
    if dest.exists() and (digest is None or sha256(dest) == digest):
        return
    dest.parent.mkdir(parents=True, exist_ok=True)
    tmp = dest.with_suffix(".part")
    with urllib.request.urlopen(url) as r, open(tmp, "wb") as f:
        shutil.copyfileobj(r, f)
    if digest and sha256(tmp) != digest:
        sys.exit(f"build: {url} has the wrong sha256")
    tmp.rename(dest)


def deps(versions):
    for v in versions:
        fetch(f"https://releases.battlecode.org/maven/org/battlecode/battlecode26-java/{v}/battlecode26-java-{v}.jar",
              JARS / f"battlecode26-java-{v}.jar", JAR_SHA256[v])
    for a in ASM:
        fetch(f"{MAVEN}/{a}/9.7/{a}-9.7.jar", JARS / f"{a}-9.7.jar")
    gc = DEPS / GC_DIR / "install" / "lib" / "libgc.a"
    stamp = DEPS / GC_DIR / "install" / "flags"
    if not gc.exists() or not stamp.exists() or stamp.read_text() != GC_CFLAGS:
        tar = DEPS / f"gc-{GC_VERSION}.tar.gz"
        fetch(f"https://github.com/ivmai/bdwgc/releases/download/v{GC_VERSION}/gc-{GC_VERSION}.tar.gz", tar, GC_SHA256)
        src = DEPS / GC_DIR / "src"
        shutil.rmtree(src, ignore_errors=True)
        src.mkdir(parents=True)
        sh(["tar", "xzf", str(tar), "-C", str(src)])
        for name, text, repl in GC_PATCHES:
            f = src / f"gc-{GC_VERSION}" / name
            code = f.read_text()
            if code.count(text) != 1:
                sys.exit(f"build: the collector patch for {name} does not apply")
            f.write_text(code.replace(text, repl))
        bdir = DEPS / GC_DIR / "build"
        shutil.rmtree(bdir, ignore_errors=True)
        bdir.mkdir(parents=True)
        sh(["cmake", str(src / f"gc-{GC_VERSION}"), "-G", "Ninja", "-DCMAKE_BUILD_TYPE=Release", "-DCMAKE_C_COMPILER=clang",
            f"-DCMAKE_INSTALL_PREFIX={DEPS / GC_DIR / 'install'}", "-DBUILD_SHARED_LIBS=OFF", "-Denable_threads=OFF",
            "-Denable_cplusplus=OFF", "-Dbuild_cord=OFF", "-Denable_docs=OFF", "-Denable_parallel_mark=OFF",
            "-Denable_gcj_support=OFF", "-Denable_java_finalization=OFF", "-Denable_disclaim=OFF",
            "-Denable_large_config=ON", "-Dbuild_tests=OFF", f"-DCMAKE_C_FLAGS={GC_CFLAGS}"], cwd=bdir,
           stdout=subprocess.DEVNULL)
        sh(["ninja", "install"], cwd=bdir, stdout=subprocess.DEVNULL)
        stamp.write_text(GC_CFLAGS)


def translator(version: str) -> str:
    """Compile engine/jvm (translator, substitutes, native-build classes) against the version's jar."""
    jar = JARS / f"battlecode26-java-{version}.jar"
    cp = [str(jar)] + [str(JARS / f"{a}-9.7.jar") for a in ASM]
    src = ENGINE / "jvm"
    files = sorted(list((src / "src" / "bcx" / "tr").rglob("*.java")) + list((src / "subst").rglob("*.java")) +
                   [src / "src" / "bcx" / f for f in ("NativeMain.java", "NativeControlProvider.java", "NativeIO.java",
                                                      "Match.java")] +
                   list((src / "src" / "battlecode" / "world").glob("*.java")))
    h = hashlib.sha256(version.encode())
    for f in files:
        h.update(str(f.relative_to(src)).encode() + b"\0" + f.read_bytes())
    out = DATA / "build" / f"jvm-tr-{h.hexdigest()[:16]}"
    if not (out / ".ok").exists():
        # compiled aside and renamed in whole: builds of two checkouts at once may share this folder
        tmp = out.with_name(f"{out.name}.tmp{os.getpid()}")
        shutil.rmtree(tmp, ignore_errors=True)
        tmp.mkdir(parents=True)
        sh(["javac", "-nowarn", "-encoding", "UTF-8",
            "--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED",
            "--add-exports", "java.base/jdk.internal.math=ALL-UNNAMED",
            "--add-exports", "java.base/sun.nio.cs=ALL-UNNAMED", "--add-exports", "java.base/jdk.internal.access=ALL-UNNAMED",
            "-d", str(tmp), "-cp", os.pathsep.join(cp)] + [str(f) for f in files])
        (tmp / ".ok").touch()
        if not (out / ".ok").exists():
            shutil.rmtree(out, ignore_errors=True)
            try:
                tmp.rename(out)
            except OSError:  # another build renamed its copy in first
                if not (out / ".ok").exists():
                    raise
        shutil.rmtree(tmp, ignore_errors=True)
    return os.pathsep.join([str(out)] + cp)


def classes_hash(classes: Path, pkg: str) -> str:
    """FNV-1a 64 over the package's class files (relative path, NUL, bytes, NUL), sorted by path: runtime/main.cpp
    computes the same to check a spec's classes against the build's."""
    h = 0xcbf29ce484222325
    root = classes / pkg
    for f in sorted(root.rglob("*.class"), key=lambda p: str(p.relative_to(root)).encode()):
        for chunk in (str(f.relative_to(root)).encode(), b"\0", f.read_bytes(), b"\0"):
            for byte in chunk:
                h = ((h ^ byte) * 0x100000001b3) & 0xFFFFFFFFFFFFFFFF
    return f"{h:016x}"


def bot_packages(classes: Path):
    return sorted(p.parent.name for p in classes.glob("*/RobotPlayer.class"))


def build(version: str, bots, jobs: int, opt: str, teams=None, gen: Path = None, exe: Path = None,
          glue_opt: str = None, lto: bool = True):
    """TEAMS: [(package, classes dir)], default every bot of build/classes (or BOTS). GEN, EXE: the generated-files
    folder (default one per checkout) and the binary (default engine/build/bcx[-VERSION]). GLUE_OPT: klasses.cpp's
    optimization (default OPT): class tables, class-init wrappers and dispatch chains, no game logic; it changes with
    any bot and takes about 4 minutes at -O2, 8 s at -O1."""
    if teams is None:
        classes = REPO / "build" / "classes"
        if not classes.exists():
            sys.exit("build: build/classes is missing; run ./gradlew classes first")
        if bots == ["all"]:
            bots = bot_packages(classes)
        teams = [(b, classes) for b in bots]
    cp = translator(version)
    # one folder per checkout: builds in two worktrees at once must not share generated files
    gen = gen or DATA / "build" / f"gen-{version}-{hashlib.sha256(str(REPO).encode()).hexdigest()[:8]}"
    gen.mkdir(parents=True, exist_ok=True)
    cmd = ["java", "-Xss64m", "-Xmx8g", "-cp", cp, "bcx.tr.Main", "--out", str(gen), "--natives",
           str(ENGINE / "jvm" / "natives.txt"), "--entry", ENTRY]
    for b, classes in teams:
        cmd += ["--team", f"{b}={classes}"]
    print(f"build {version}: translating the engine and {len(teams)} bots", flush=True)
    lock = os.environ.get("BCX_TRANSLATE_LOCK")  # one translation at a time on a host (each peaks at 4-5 GB)
    if lock:
        import fcntl
        with open(lock, "a") as f:
            fcntl.flock(f, fcntl.LOCK_EX)
            sh(cmd)
    else:
        sh(cmd)
    vc = gen / "version.cpp"
    text = f'extern "C" const char bcx_engine_version[] = "{version}";\n'
    text += 'extern "C" const char* const bcx_bot_hashes[] = {' + ", ".join(
        f'"{b}", "{classes_hash(Path(classes), b)}"' for b, classes in teams) + ', nullptr};\n'
    if not vc.exists() or vc.read_text() != text:
        vc.write_text(text)
    rt = ENGINE / "runtime"
    gci = DEPS / GC_DIR / "install"
    inc = f"-I{gen} -I{rt} -I{gci / 'include'}"
    srcs = sorted(gen.glob("m*.cpp")) + [gen / "klasses.cpp", gen / "version.cpp"] + \
        [rt / f for f in ("rt.cpp", "sched.cpp", "natives.cpp", "main.cpp")]
    exe = exe or OUT / ("bcx" if version == "1.2.6" else f"bcx-{version}")
    # ThinLTO: the translated code calls many small methods across files (getters, index arithmetic) and the link
    # inlines them; a changed bot then costs a link that redoes the LTO backend of what moved (engine/AGENTS.md)
    cxx = CXXFLAGS + (["-flto=thin"] if lto else [])
    # with its own optimization, klasses.cpp is a plain object: under ThinLTO the link would redo it at OPT
    glue = CXXFLAGS if glue_opt else cxx
    link = (f"clang++ -static -flto=thin -fuse-ld=lld -Wl,--thinlto-jobs={jobs} -Wl,--thinlto-cache-dir={gen / 'lto-cache'} "
            f"{opt}" if lto else "clang++ -static")
    lines = [f"cxxflags = {' '.join(cxx)} {opt} {inc}",
             "rule cxx\n  command = clang++ $cxxflags -MMD -MF $out.d -c $in -o $out\n  depfile = $out.d\n  deps = gcc",
             f"rule cxxglue\n  command = clang++ {' '.join(glue)} {glue_opt or opt} {inc} -MMD -MF $out.d -c $in -o $out\n"
             "  depfile = $out.d\n  deps = gcc",
             "rule cc\n  command = clang -O2 -ffp-contract=off -fno-fast-math -msse4.1 -c $in -o $out",
             f"rule link\n  command = {link} -o $out $in {gci / 'lib' / 'libgc.a'} -lz"]
    objs = []
    for s in srcs:
        o = gen / "obj" / (s.stem + ("_rt" if s.parent == rt else "") + ".o")
        objs.append(o)
        lines.append(f"build {o}: {'cxxglue' if s.name == 'klasses.cpp' else 'cxx'} {s}")
    o = gen / "obj" / "hsmath.o"
    objs.append(o)
    lines.append(f"build {o}: cc {rt / 'hsmath.c'}")
    lines.append(f"build {exe}: link {' '.join(map(str, objs))}")
    (gen / "build.ninja").write_text("\n".join(lines) + "\n")
    OUT.mkdir(exist_ok=True)
    exe.parent.mkdir(parents=True, exist_ok=True)
    sh(["ninja", "-f", str(gen / "build.ninja"), "-j", str(jobs)], cwd=gen)  # its logs live in gen
    # the jar's bundled maps, for maps not in maps/
    md = OUT / f"maps-{version}"
    if not md.exists():
        tmp = OUT / f".maps-{version}.tmp"
        shutil.rmtree(tmp, ignore_errors=True)
        tmp.mkdir()
        with zipfile.ZipFile(JARS / f"battlecode26-java-{version}.jar") as z:
            for n in z.namelist():
                if n.startswith("battlecode/world/resources/") and n.endswith(".map26"):
                    (tmp / Path(n).name).write_bytes(z.read(n))
        tmp.rename(md)
    print(f"build {version}: {exe}")


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--engine", default="1.2.6", choices=["1.2.6", "1.2.5", "all"])
    ap.add_argument("--bots", default="all", help="comma-separated packages, or all (default)")
    ap.add_argument("-j", type=int, default=8)
    ap.add_argument("--opt", default="-O3")
    ap.add_argument("--lto", default="thin", choices=["thin", "off"], help="ThinLTO at the link (default thin)")
    ap.add_argument("--glue-opt", help="klasses.cpp's optimization (default: --opt)")
    ap.add_argument("--team", action="append", default=[], metavar="PKG=DIR",
                    help="a bot from its own classes folder, in this order (replaces --bots and build/classes)")
    ap.add_argument("--gen", type=Path, help="generated-files folder (default: one per checkout and version)")
    ap.add_argument("--out", type=Path, help="the binary (default engine/build/bcx[-VERSION])")
    a = ap.parse_args()
    versions = ["1.2.6", "1.2.5"] if a.engine == "all" else [a.engine]
    if (a.team or a.gen or a.out) and len(versions) != 1:
        sys.exit("build: --team, --gen and --out build one engine version")
    teams = [tuple(t.split("=", 1)) for t in a.team] or None
    deps(versions)
    for v in versions:
        build(v, a.bots.split(","), a.j, a.opt, teams, a.gen, a.out, a.glue_opt, a.lto == "thin")


if __name__ == "__main__":
    main()
