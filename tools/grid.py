"""tools/run: heavy work on every free remote CPU, with nothing to choose (the compute skill).

    tools/run [OPTIONS] -- CMD...                 CMD once, alone on a whole remote machine; its output, its exit code
    tools/run [OPTIONS] --each FILE -- CMD {}     CMD once a line of FILE (or a --chunk of lines, as {file}),
                                                  as many at once as every free machine holds

The working tree goes as it is, uncommitted changes included, and its code and --with files go up once a content.
Each task's OUT comes back to runs/RUN/N/out/, its output to runs/RUN/N/log. It waits until every task has ended: run
it in the background; it prints the run's id first, and `tools/compute wait RUN` follows the run again if this
process ends. Runs share one pool (not --whole or --max-cpus): live pieces of any run take its tasks, so it asks for
pieces only beyond their free slots.
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from tools import backends as B
from tools import jobs as J
from tools import runs as R
from tools.compute import duration, size_mb, sweep_daily
from tools.remote import Refused


def main(argv: list[str] | None = None) -> int:
    p = argparse.ArgumentParser(prog="tools/run", description=__doc__,
                                formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--each", type=Path, metavar="FILE", help="CMD once a line of FILE: {} the line, else its words "
                   "appended")
    p.add_argument("--chunk", type=int, default=1, metavar="K", help="with --each: a task a K lines, {file} a file "
                   "holding them (default 1)")
    p.add_argument("--task-cpus", type=int, default=1, metavar="N", help="with --each: the CPUs a task uses "
                   "(default 1); its JOB_CPUS")
    p.add_argument("--task-mem", default=f"{R.DEFAULT_TASK_MEM_MB}M", metavar="SIZE",
                   help=f"with --each: the memory a task peaks at (default {R.DEFAULT_TASK_MEM_MB}M, a game's JVM)")
    p.add_argument("--whole", action="store_true", help="with --each: each task alone on a whole machine")
    p.add_argument("--where", default=",".join(B.WHERES), help="the backends it may use, comma-separated (default "
                   "all: condor,slurm,cpucluster1)")
    p.add_argument("--max-cpus", type=int, metavar="N", help="hold at most N CPUs at once")
    p.add_argument("--time", metavar="T", help="a piece's limit (10m, 4h): on Slurm and cpucluster1 its --time "
                   "(default 4h; it gives its tasks back and requeues), on Condor its removal (default none)")
    p.add_argument("--kit", action="store_true", help="compile build/classes, ship it, and give tasks the engine's "
                   "jars: BC_CLASSPATH and BC_ENGINE_JARS (tools/kit.py)")
    p.add_argument("--no-build", action="store_true", help="with --kit: ship build/classes as it is")
    p.add_argument("--with", dest="ships", action="append", default=[], metavar="PATH",
                   help="a git-ignored file or folder the tasks read, shipped to its path; repeatable")
    p.add_argument("--gather", action="append", default=[], metavar="NAME",
                   help="concatenate every task's out/NAME, in task order, into runs/RUN/NAME; repeatable")
    p.add_argument("--detach", action="store_true", help="submit, print the run's id and exit: follow it with "
                   "`tools/compute wait RUN`")
    p.add_argument("--json", action="store_true", help="the run's summary as JSON on stdout at its end")
    p.add_argument("cmd", nargs=argparse.REMAINDER, metavar="-- CMD")
    a = p.parse_args(argv)
    cmd: list[str] = a.cmd[1:] if a.cmd[:1] == ["--"] else a.cmd
    if not cmd:
        p.error("no command: tools/run -- CMD...")
    try:
        wheres = tuple(w for w in a.where.split(",") if w)
        if bad := [w for w in wheres if w not in J.WHERES]:
            raise Refused(f"--where: {bad[0]} isn't one of {', '.join(J.WHERES)}")
        if a.each is None:
            tasks, task_cpus = R.tasks_of(cmd, None, 1), 0
        else:
            lines = [x for x in a.each.read_text().splitlines() if x.strip()]
            tasks, task_cpus = R.tasks_of(cmd, lines, a.chunk), 0 if a.whole else a.task_cpus
        s = R.create(tasks, task_cpus, size_mb(a.task_mem), wheres, duration(a.time) if a.time else None,
                     a.max_cpus, a.kit, a.ships, tuple(a.gather), not a.no_build)
        print(s.id, flush=True)
        sweep_daily()
        try:
            R.place(s, len(tasks))
        except Refused:
            R.close(s.id, [])  # a run no piece serves is closed, so the sweep takes its folder
            raise
        R.mark_placed(s.id)
        if a.detach:
            return 0
        summary = R.follow(s.id)
    except Refused as e:
        print(f"tools/run: {e}", file=sys.stderr)
        return 2
    except KeyboardInterrupt:
        print("tools/run: interrupted; the run goes on: `tools/compute wait RUN` follows it again, "
              "`tools/compute cancel RUN` ends it", file=sys.stderr)
        return 130
    out = J.LOCAL_RUNS / s.id
    if a.each is None:
        log = out / "0" / "log"
        sys.stdout.write(log.read_text(errors="replace") if log.exists() else "")
    if a.json:
        print(json.dumps(summary, indent=1))
    R.report(summary, out)
    code = summary["exit"]
    assert isinstance(code, int)
    return code


if __name__ == "__main__":
    sys.exit(main())
