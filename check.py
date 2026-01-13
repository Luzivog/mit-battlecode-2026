#!/usr/bin/env python3
import os
import sys

FORBIDDEN = ("rc.move", "rc.turn")
ALLOWED_FILENAME = "Game.java"
ALLOWED_SUFFIXES = (".java", ".jinja2")

def main(root="."):
    violations = []

    for dirpath, _, filenames in os.walk(root):
        for name in filenames:
            if not name.endswith(ALLOWED_SUFFIXES):
                continue
            if name == ALLOWED_FILENAME:
                continue

            path = os.path.join(dirpath, name)

            try:
                with open(path, "r", encoding="utf-8") as f:
                    for lineno, line in enumerate(f, 1):
                        for token in FORBIDDEN:
                            if token in line:
                                violations.append(
                                    f"{path}:{lineno}: contains '{token}'"
                                )
            except (UnicodeDecodeError, OSError):
                continue  # ignore unreadable files

    if violations:
        print("Forbidden function usage found:\n", file=sys.stderr)
        for v in violations:
            print(v, file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
