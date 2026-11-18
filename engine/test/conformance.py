#!/usr/bin/env python3
"""Translator conformance: each engine/jvm/test/bcxtest/*.java with a main runs on the JVM and translated to C++
(bcxtest.Out.print is System.out there, a native here); the outputs must be identical.

    engine/test/conformance.py [NAME...]

Builds into ~/battlecode26-data/engine/build/conformance/NAME/. Exit 0 when every program matches.
"""
import os
import subprocess
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import build  # noqa: E402

ENGINE = build.ENGINE
TESTS = ENGINE / "jvm" / "test" / "bcxtest"
# tests in other packages: (package, class) run against build/classes (fuzz_lang's features)
EXTRA = {"FeatureMain": "fuzz_lang"}
OUT = build.DATA / "build" / "conformance"


def main():
    names = sys.argv[1:] or sorted(p.stem for p in TESTS.glob("*.java") if "void main(" in p.read_text()) + sorted(EXTRA)
    build.deps(["1.2.6"])
    cp = build.translator("1.2.6")
    classes = OUT / "classes"
    classes.mkdir(parents=True, exist_ok=True)
    bc = build.REPO / "build" / "classes"
    jar = build.JARS / "battlecode26-java-1.2.6.jar"
    build.sh(["javac", "-nowarn", "-d", str(classes), "-cp", os.pathsep.join([str(bc), str(jar)])] +
             [str(p) for p in TESTS.glob("*.java")] + [str(p) for p in (ENGINE / "jvm" / "test").glob("*/*.java") if p.parent != TESTS])
    extra_cp = os.pathsep.join([str(classes), str(bc), str(jar)])
    rt = ENGINE / "runtime"
    gci = build.DEPS / "gc-install"
    bad = 0
    for n in names:
        gen = OUT / n
        gen.mkdir(parents=True, exist_ok=True)
        pkg = EXTRA.get(n, "bcxtest")
        build.sh(["java", "-Xss64m", "-cp", os.pathsep.join([cp, extra_cp]), "bcx.tr.Main", "--out", str(gen),
                  "--natives", str(ENGINE / "jvm" / "natives.txt"), "--entry", f"{pkg}/{n}.main([Ljava/lang/String;)V"],
                 stdout=subprocess.DEVNULL)
        (gen / "v.cpp").write_text('extern "C" const char bcx_engine_version[] = "test";\n'
                                   'extern "C" const char* const bcx_bot_hashes[] = {nullptr};\n')
        srcs = sorted(gen.glob("m*.cpp")) + [gen / "klasses.cpp", gen / "v.cpp"] + \
            [rt / f for f in ("rt.cpp", "sched.cpp", "natives.cpp", "test_main.cpp")]
        objs = []
        procs = []
        for s in srcs:
            o = gen / (s.stem + ("_rt" if s.parent == rt else "") + ".o")
            objs.append(str(o))
            procs.append(subprocess.Popen(["clang++"] + build.CXXFLAGS + ["-O1", f"-I{gen}", f"-I{rt}", f"-I{gci / 'include'}",
                                           "-c", str(s), "-o", str(o)]))
        for p in procs:
            if p.wait() != 0:
                sys.exit(f"conformance: {n}: compile failed")
        hs = gen / "hsmath.o"
        build.sh(["clang", "-O2", "-ffp-contract=off", "-msse4.1", "-c", str(rt / "hsmath.c"), "-o", str(hs)])
        exe = gen / n
        build.sh(["clang++", "-o", str(exe)] + objs + [str(hs), str(gci / "lib" / "libgc.a"), "-lz"])
        want = subprocess.run(["java", "-cp", extra_cp, f"{pkg}.{n}"], capture_output=True, text=True).stdout
        got = subprocess.run([str(exe)], capture_output=True, text=True)
        if got.stdout == want and got.returncode == 0:
            print(f"{n}: ok ({len(want.splitlines())} lines)")
        else:
            bad += 1
            print(f"{n}: DIFFERS (exit {got.returncode})")
            wl, gl = want.splitlines(), got.stdout.splitlines()
            for i in range(max(len(wl), len(gl))):
                w = wl[i] if i < len(wl) else "<none>"
                g = gl[i] if i < len(gl) else "<none>"
                if w != g:
                    print(f"  line {i + 1}: jvm    {w!r}\n  line {i + 1}: native {g!r}")
            if got.stderr:
                print("  stderr: " + got.stderr[-500:])
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    main()
