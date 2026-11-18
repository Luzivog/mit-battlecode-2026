"""The game kit: what plays a Battlecode game with plain `java` and no Gradle, here or on a DoC node.

- The engine's jars, as Gradle's runtime classpath has them after the compiled bots (docs/testing.md §3): the
  engine named by engine_version.txt and scala-library, from the Gradle cache. Remote jobs find them in the runtime
  folder (provision.py), named by their content.
- build/classes: every package compiled, the engine overrides first on the classpath. `tools/compute run --kit` and
  `tools/run --kit` compile them (`./gradlew classes`) and ship them with the job.

A job run with the kit gets BC_ENGINE_JARS (the jars, ':'-joined) and BC_CLASSPATH (build/classes, then the jars);
`classpath()` gives the same here, so one command line plays the same game in both places.
"""
from __future__ import annotations

import os
import re
import subprocess
from pathlib import Path

from tools.remote import Refused

REPO = Path(__file__).resolve().parent.parent
GRADLE_FILES = Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1"
CLASSES = "build/classes"


def scala_version(repo: Path = REPO) -> str:
    m = re.search(r"name:\s*'scala-library',\s*version:\s*'([^']+)'", (repo / "build.gradle").read_text())
    if not m:
        raise Refused("build.gradle no longer names scala-library's version: update tools/kit.py")
    return m[1]


def cached(group: str, name: str, version: str) -> Path:
    """NAME-VERSION.jar of GROUP in the Gradle cache, or Refused (run ./gradlew classes once to fetch it)."""
    found = sorted((GRADLE_FILES / group / name / version).glob(f"*/{name}-{version}.jar"))
    if not found:
        raise Refused(f"no {name}-{version}.jar in {GRADLE_FILES}: run ./gradlew classes once")
    return found[0]


def jars(repo: Path = REPO) -> list[Path]:
    """The engine's jars in Gradle's runtime classpath order."""
    engine = (repo / "engine_version.txt").read_text().strip()
    return [cached("org.battlecode", "battlecode26-java", engine),
            cached("org.scala-lang", "scala-library", scala_version(repo))]


def classpath(repo: Path = REPO) -> str:
    """BC_CLASSPATH where it is set (a job with the kit), else this checkout's: build/classes, then the jars."""
    if cp := os.environ.get("BC_CLASSPATH"):
        return cp
    return ":".join([str(repo / CLASSES), *(str(j) for j in jars(repo))])


def compile_classes(repo: Path = REPO) -> None:
    """`./gradlew -q classes`: build/classes as the tree has it, so what a job ships is what the tree compiles to."""
    p = subprocess.run([str(repo / "gradlew"), "-q", "classes"], cwd=repo, capture_output=True, text=True)
    if p.returncode or not (repo / CLASSES).is_dir():
        raise Refused(f"./gradlew classes failed (exit {p.returncode}):\n{(p.stdout + p.stderr)[-2000:]}")


if __name__ == "__main__":
    print(classpath())
