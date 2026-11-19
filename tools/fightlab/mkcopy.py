"""The fight lab's copy of challenger: src/flch, never committed (docs/theory/fl-fight-lab.md).

    tools/fightlab/mkcopy.py [--src challenger] [--pkg flch] [--patch FILE.py ...]

flch is challenger under another package name, with three changes:
- Tune's constants are fields, not compile-time constants, so a game's properties can set them;
- from the round named by the property bc.testing.fl.r0, each robot loads bc.testing.fl.set ("NAME=V,NAME=V,salt=S") at
  the start of its turn: before that round every arm of a fork plays the same code with main's values;
- salt=S xors S into the robot's random stream (a null arm: the same values, another draw from the fork on).
A --patch file is Python run with `root` (the copy's Path) before the templates are regenerated, for code arms that
change templates (each must keep main's behaviour while its own constant holds main's value).
"""
from __future__ import annotations

import argparse
import re
import shutil
import subprocess
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
LINE = re.compile(r"^(\s*)public static final (int|double|long|boolean) ([A-Z0-9_]+) = ([^;]+);(.*)$")

LOADER = """
    // ---- the fight lab (tools/fightlab/mkcopy.py): main's values until round R0, then bc.testing.fl.set ----
    static final int R0 = r0();
    static boolean on;

    private static int r0() {
        String s = System.getProperty("bc.testing.fl.r0");
        return s == null ? Integer.MAX_VALUE : Integer.parseInt(s);
    }

    public static void tick(int round) {
        if (on || round < R0) return;
        on = true;
        String s = System.getProperty("bc.testing.fl.set");
        if (s == null || s.isEmpty()) return;
        // no String.split: past 10 parts its ArrayList grows by reflection, which the sandbox refuses
        int i = 0;
        while (i < s.length()) {
            int c = s.indexOf(',', i);
            if (c < 0) c = s.length();
            int e = s.indexOf('=', i);
            set(s.substring(i, e), s.substring(e + 1, c));
            i = c + 1;
        }
    }

    private static void set(String k, String v) {
        switch (k) {
%s
            case "salt" -> %s.util.Rng.salt(Integer.parseInt(v));
            default -> throw new IllegalArgumentException("fight lab: no constant " + k);
        }
    }
}
"""

PARSE = {"int": "Integer.parseInt(v)", "double": "Double.parseDouble(v)", "long": "Long.parseLong(v)",
         "boolean": "Boolean.parseBoolean(v)"}


def make(src: str, pkg: str, patches: list[Path]) -> list[str]:
    s_root, d_root = REPO / "src" / src, REPO / "src" / pkg
    if d_root.exists():
        shutil.rmtree(d_root)
    shutil.copytree(s_root, d_root, ignore=shutil.ignore_patterns("*.class", "CLAUDE.md", "*.txt"))
    word = re.compile(rf"\b{re.escape(src)}\b")
    for f in d_root.rglob("*"):
        if f.suffix in (".java", ".jinja2"):
            t = f.read_text()
            t2 = word.sub(pkg, t)
            if t2 != t:
                f.write_text(t2)
    for p in patches:
        code = p.read_text()
        exec(compile(code, str(p), "exec"), {"root": d_root, "pkg": pkg, "Path": Path, "re": re})
    gen = subprocess.run([sys.executable, str(REPO / "jinja.py"), "--package", pkg], cwd=REPO, capture_output=True,
                         text=True)
    if gen.returncode != 0 or "[error]" in gen.stdout + gen.stderr:
        raise SystemExit("jinja.py failed:\n" + gen.stdout + gen.stderr)
    # Tune: fields and the loader. A constant another class copies into a static final at class init stays final: it
    # could not follow the lab's values.
    frozen = set()
    for f in d_root.rglob("*.java"):
        if f.name != "Tune.java":
            for m in re.finditer(r"static final [^;]*;", f.read_text()):
                frozen |= set(re.findall(r"\bTune\.([A-Z0-9_]+)", m.group(0)))
    tune = d_root / "Tune.java"
    lines, names, cases = [], [], []
    for ln in tune.read_text().splitlines():
        m = LINE.match(ln)
        if m and m.group(3) not in frozen:
            ind, ty, name, val, rest = m.groups()
            ln = f"{ind}public static {ty} {name} = {val};{rest}"
            names.append(name)
            cases.append(f'            case "{name}" -> {name} = {PARSE[ty]};')
        lines.append(ln)
    body = "\n".join(lines).rstrip()
    assert body.endswith("}"), "Tune.java must end with its class's brace"
    tune.write_text(body[:-1] + LOADER % ("\n".join(cases), pkg))
    # the turn hook and the salt
    rp = d_root / "RobotPlayer.java"
    t = rp.read_text()
    hook = "            try {\n                Game.preTick();"
    assert t.count(hook) == 1, "RobotPlayer's loop changed: update mkcopy's hook"
    rp.write_text(t.replace(hook, "            try {\n                Tune.tick(rc.getRoundNum());\n                Game.preTick();"))
    rng = d_root / "util" / "Rng.java"
    t = rng.read_text()
    anchor = "public class Rng {\n    static int s0, s1, s2, s3;\n"
    assert t.count(anchor) == 1, "Rng changed: update mkcopy's salt"
    rng.write_text(t.replace(anchor, anchor + "\n    public static void salt(int s) {\n        s0 ^= s * 0x9E3779B9;\n"
                             "        s1 ^= s * 0x7F4A7C15;\n    }\n"))
    return names


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--src", default="challenger")
    ap.add_argument("--pkg", default="flch")
    ap.add_argument("--patch", type=Path, action="append", default=[])
    a = ap.parse_args()
    names = make(a.src, a.pkg, a.patch)
    print(f"src/{a.pkg}: {len(names)} constants settable from round bc.testing.fl.r0: {' '.join(names)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
