#!/usr/bin/env python3

import argparse
import time
from pathlib import Path

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

from jinja2 import Environment, FileSystemLoader, nodes
from jinja2.ext import Extension


# =====================
# EXISTING COMPILER LOGIC
# =====================

class DebugExtension(Extension):
    tags = {"debug"}

    def parse(self, parser):
        lineno = next(parser.stream).lineno
        args = [nodes.Const(lineno)]
        while parser.stream.current.type != 'block_end':
            args.append(parser.parse_expression())
        return nodes.CallBlock(
            self.call_method("_debug", args), [], [], [], lineno=lineno
        )

    def _debug(self, lineno, *args, caller):
        flattened_args = []
        for arg in args:
            if isinstance(arg, tuple):
                flattened_args.extend(arg)
            else:
                flattened_args.append(arg)

        message = ' + ", " + '.join(
            f'"{str(arg)} = " + {str(arg)}' for arg in flattened_args
        )
        return f'System.out.println("[{lineno}] " + {message});'


def bitAnd(a, b): return a & b
def bitOr(a, b): return a | b
def bitLeftShift(a, b): return a << b
def bitRightShift(a, b): return a >> b
def intDiv(a, b): return a // b

def capitalise(s):
    return s if not s else s[0].upper() + s[1:]

def radialMask(rsqr, cx, cy):
    mask0 = mask1 = 0
    for x in range(9):
        for y in range(9):
            dist = (x - cx) ** 2 + (y - cy) ** 2
            if dist > rsqr:
                continue
            if y >= 7:
                mask1 |= 1 << (9 * (y - 7) + x)
            else:
                mask0 |= 1 << (9 * y + x)
    return (mask0, mask1)


def make_env():
    env = Environment(
        loader=FileSystemLoader("./"),
        trim_blocks=True,
        lstrip_blocks=True,
    )
    env.add_extension(DebugExtension)
    env.globals.update({
        "bitAnd": bitAnd,
        "bitOr": bitOr,
        "bitLeftShift": bitLeftShift,
        "bitRightShift": bitRightShift,
        "intDiv": intDiv,
        "capitalise": capitalise,
        "radialMask": radialMask,
        "MAX_MAP_SIZE": 60,
    })
    return env


ENV = make_env()


def compile_template(path: Path, debug: bool):
    if not path.name.endswith(".jinja2"):
        return
    if path.name.startswith("."):
        return

    try:
        output_path = path.with_suffix("")
        template = ENV.get_template(str(path))

        rendered = template.render(
            directions=[
                'Direction.NORTH',
                'Direction.NORTHEAST',
                'Direction.EAST',
                'Direction.SOUTHEAST',
                'Direction.SOUTH',
                'Direction.SOUTHWEST',
                'Direction.WEST',
                'Direction.NORTHWEST',
                'Direction.CENTER'
            ],
            shortDirections=['N','NE','E','SE','S','SW','W','NW','C'],
            debug=debug
        )

        output_path.write_text(rendered)
        print(f"[compiled] {path} → {output_path}")

    except Exception as e:
        print(f"[error] {path}: {e}")


def compile_all(debug: bool):
    for path in Path(".").rglob("*.jinja2"):
        compile_template(path, debug)


# =====================
# WATCH MODE
# =====================

class JinjaWatchHandler(FileSystemEventHandler):
    def __init__(self, debug: bool):
        self.debug = debug

    def on_modified(self, event):
        if not event.is_directory:
            compile_template(Path(event.src_path), self.debug)

    def on_created(self, event):
        if not event.is_directory:
            compile_template(Path(event.src_path), self.debug)

    def on_moved(self, event):
        if not event.is_directory:
            compile_template(Path(event.dest_path), self.debug)


# =====================
# MAIN
# =====================

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--watch", action="store_true",
                        help="Watch for changes and recompile incrementally")
    parser.add_argument("--debug", action="store_true",
                        help="Enable debug rendering")
    args = parser.parse_args()

    # Always do a full rebuild first
    compile_all(args.debug)

    if not args.watch:
        return

    print("Watching for .jinja2 changes (Ctrl+C to stop)...")
    observer = Observer()
    observer.schedule(
        JinjaWatchHandler(args.debug),
        path=".",
        recursive=True
    )
    observer.start()

    try:
        while True:
            time.sleep(0.5)
    except KeyboardInterrupt:
        observer.stop()

    observer.join()


if __name__ == "__main__":
    main()
