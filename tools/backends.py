"""The three backends as tools/compute reads them, and where a job of ours can start now. Pure parsers over captured
output (tools/fixtures/), plus the one login each read takes.

- slurm: DoC's GPU cluster, CPU work holding one card (a CPU job there must hold a card).
- cpucluster1: DoC's CPU-only Slurm cluster.
- condor: DoC's lab machines; a job takes a whole machine (request_cpus = 1 claims its one static slot).

Slurm decides, not this model: a Slurm slot counts only once `sbatch --test-only` with its exact options says it
starts now (WILL_RUN).
"""
from __future__ import annotations

import re
import shlex
from dataclasses import dataclass, field, replace

from tools.remote import CONDOR, CPU_HOST, SLURM_HOSTS, SUBMIT, Refused, cached, login

Where = str  # "slurm", "cpucluster1" or "condor" ("local" in tests)
WHERES = ("condor", "slurm", "cpucluster1")
SLURMS = ("slurm", "cpucluster1")

# ---- Slurm, on either cluster ----
SLURM_READ = r"""echo ==partitions; scontrol show partition -o
echo ==nodes; sinfo -h -N -O "NodeList:|,Partition:|,StateComplete:|,CPUsState:|,Memory:|,AllocMem:|,Gres:|,GresUsed:|,Threads:"
echo ==qos; scontrol show assoc_mgr flags=qos | awk -v me="$USER" '/^QOS=/{u=0; print; next} /^ +MaxTRESPN=/{print; next} /^ +[A-Za-z0-9_.-]+\(/{u=($1 ~ ("^" me "\\("))} u{print}'
echo ==jobs; SLURM_TIME_FORMAT=%s squeue -u "$USER" -h --noconvert -o "%i|%T|%P|%r|%j|%N|%M|%l|%C|%m|%S|%V|%e|%Q|%n|%b"
echo ==end"""
# Nodes a job of ours must not land on: hopper killed every job at its start with RaisedSignal:53 (26 to 29 Oct, the
# previous project's measurement)
EXCLUDE = ("hopper",)
SLURM_ENDED = {"COMPLETED", "FAILED", "CANCELLED", "TIMEOUT", "OUT_OF_MEMORY", "NODE_FAIL", "BOOT_FAIL", "DEADLINE",
               "PREEMPTED", "SPECIAL_EXIT", "REVOKED"}
# The GPU cluster's cards, strongest first: CPU work holds the weakest free card it can
SPEED = ("a100", "a30", "a40", "t4", "a16")
SLURM_CARDS = {"nvidia_a100_80gb_pcie": "a100", "nvidia_a40": "a40", "nvidia_a30": "a30", "tesla_t4": "t4",
               "nvidia_a16": "a16"}
# DoC's job_submit.lua keeps a CPU ask of up to 9 whole, and rewrites a larger one to tasks of 6 CPUs, allocating 6
# times the whole tasks (16, 56, 60 asked: 12, 54, 60 given; the previous project's measurement, 2 Nov)
SLURM_WHOLE_ASK, SLURM_TASK_CPUS = 9, 6
CPU_JOB_MIN_MEM_MB = 1000  # the least memory a Slurm CPU job gets a CPU (a convention)
USR1_LEAD_S = 120  # SIGUSR1 this long before --time: the job saves and requeues (job.sh)
PROBE_S = 120.0  # a login of sbatch --test-only probes (one a size of each slot tried)


@dataclass(frozen=True)
class Partition:
    name: str
    qos: str
    max_time_s: int | None
    tier: int
    max_mem_mb: int | None


@dataclass(frozen=True)
class Limit:
    cap: int | None  # None: no limit
    used: int


@dataclass(frozen=True)
class SlurmJob:
    id: str
    state: str
    partition: str
    reason: str
    name: str
    node: str
    elapsed: str
    limit: str
    cpus: int
    mem_mb: int
    req_nodes: str
    gpus: int | None

    @property
    def partitions(self) -> list[str]:
        return self.partition.split(",")


@dataclass(frozen=True)
class SlurmNode:
    name: str
    card: str
    partitions: tuple[str, ...]
    usable: bool
    cards: int
    held: int
    cpus: int
    idle_cpus: int
    mem_mb: int
    alloc_mem_mb: int
    planned: bool
    threads: int


@dataclass(frozen=True)
class Slurm:
    partitions: dict[str, Partition]
    nodes: dict[str, SlurmNode]
    qos: dict[str, dict[str, Limit]]
    jobs: list[SlurmJob]


def sections(text: str) -> dict[str, list[str]]:
    out: dict[str, list[str]] = {}
    cur: list[str] = []
    for line in text.splitlines():
        if line.startswith("=="):
            cur = out.setdefault(line[2:].strip(), [])
        else:
            cur.append(line)
    return out


def seconds(spec: str) -> int | None:
    """Slurm's [D-]HH:MM:SS (or MM:SS) in seconds; None for UNLIMITED."""
    if spec in ("UNLIMITED", "INFINITE", "NONE"):
        return None
    days, _, hms = spec.rpartition("-")
    parts = [int(x) for x in hms.split(":")]
    while len(parts) < 3:
        parts.insert(0, 0)
    return int(days or 0) * 86400 + parts[0] * 3600 + parts[1] * 60 + parts[2]


def _indices(spec: str) -> set[int]:
    out: set[int] = set()
    for part in spec.split(","):
        a, _, b = part.partition("-")
        if a.isdigit():
            out.update(range(int(a), int(b or a) + 1))
    return out


def cards(gres: str, used: str) -> tuple[int, int]:
    """(GPUs, GPUs held) of a node from sinfo's Gres and GresUsed; a card with a shard in use isn't free whole."""
    total = sum(int(m) for m in re.findall(r"gpu:[^:,]+:(\d+)", gres))
    held = set[int]()
    for m in re.findall(r"gpu:[^:,]+:\d+\(IDX:([^)]*)\)", used):
        held |= _indices(m)
    for m in re.findall(r"shard:[^:,]+:\d+\(([^)]*)\)", used):
        held |= {i for i, s in enumerate(m.split(",")) if not s.startswith("0/")}
    return total, len(held)


def parse_qos(lines: list[str]) -> dict[str, dict[str, Limit]]:
    """Each QoS's per-user limits on us (gpu, shard, cpu, mem MB, jobs, submit) and per-node limits on one job
    (node_cpu, node_mem: MaxTRESPN)."""
    qos: dict[str, dict[str, Limit]] = {}
    cur: dict[str, Limit] = {}
    names = {"MaxJobsPU": "jobs", "MaxSubmitJobsPU": "submit", "cpu": "cpu", "mem": "mem", "gres/gpu": "gpu",
             "gres/shard": "shard"}
    for line in lines:
        m = re.match(r"QOS=([\w.-]+)\(", line)
        if m:
            cur = qos.setdefault(m[1], {})
        if line.lstrip().startswith("MaxTRESPN="):
            for key, cap in re.findall(r"(cpu|mem)=(\d+)", line):
                cur[f"node_{key}"] = Limit(int(cap), 0)
            continue
        if not line.lstrip().startswith(("MaxJobsPU=", "MaxTRESPU=")):
            continue
        for key, cap, used in re.findall(r"([\w/]+)=(N|\d+)\((\d+)\)", line):
            if key in names:
                cur[names[key]] = Limit(None if cap == "N" else int(cap), int(used))
    return qos


def usable(state: str, name: str) -> bool:
    """A node takes our jobs: up (only these flags), and not in EXCLUDE."""
    return name not in EXCLUDE and set(state.split("+")) <= {"idle", "mixed", "allocated", "planned"}


def megabytes(spec: str) -> int:
    m = re.fullmatch(r"(\d+)M", spec)
    if not m:
        raise Refused(f"squeue's memory {spec!r} isn't a size in MB")
    return int(m[1])


def cards_asked(f: list[str]) -> int | None:
    if not f:
        return None
    m = re.search(r"gres/gpu(?::[^:,]+)?:(\d+)", f[0])
    return int(m[1]) if m else 0


def parse_jobs(lines: list[str]) -> list[SlurmJob]:
    return [SlurmJob(f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7], int(f[8]), megabytes(f[9]), "".join(f[14:15]),
                     cards_asked(f[15:16]))
            for f in (line.split("|") for line in lines) if len(f) >= 13]


def parse_slurm(text: str) -> Slurm:
    """The read of SLURM_READ on the GPU cluster."""
    s = sections(text)
    if "end" not in s:
        raise Refused("the Slurm read was cut short")
    nodes: dict[str, SlurmNode] = {}
    for f in (line.split("|") for line in s.get("nodes", [])):
        if len(f) < 9:
            continue
        name, part = f[0], f[1].rstrip("*")
        gpus, held = cards(f[6], f[7])
        model = re.search(r"gpu:([^:,]+):", f[6])
        if not gpus or not model:
            continue
        _, idle, _, total = (int(x) for x in f[3].split("/"))
        seen = nodes.get(name)
        nodes[name] = SlurmNode(name, model[1], tuple(sorted({*(seen.partitions if seen else ()), part})),
                                usable(f[2], name), gpus, held, total, idle, int(f[4]), int(f[5] or 0),
                                "planned" in f[2].split("+"), int(f[8]))
    in_use = {p for n in nodes.values() if n.usable for p in n.partitions}
    partitions: dict[str, Partition] = {}
    for line in s.get("partitions", []):
        kv = dict(re.findall(r"(\w+)=(\S*)", line))
        name = kv.get("PartitionName", "")
        if "gres/gpu=" in kv.get("TRES", "") and name in in_use:
            mm = kv.get("MaxMemPerNode", "")
            partitions[name] = Partition(name, kv.get("QoS", ""), seconds(kv.get("MaxTime", "UNLIMITED")),
                                         int(kv.get("PriorityTier", "0")), int(mm) if mm.isdigit() else None)
    return Slurm(partitions, nodes, parse_qos(s.get("qos", [])), parse_jobs(s.get("jobs", [])))


def read_slurm(max_age: float = 0.0) -> Slurm:
    return parse_slurm(cached(SLURM_READ, max_age=max_age))


# ---- GPU-cluster CPU slots ----
@dataclass(frozen=True)
class CpuSlot:
    """Where a CPU job of ours could start on the GPU cluster now: one node of one partition, one of its free cards,
    the CPUs it gets and their memory."""
    partition: str
    node: str
    card: str
    cpus: int
    mem_mb: int


def whole_ask(cpus: int) -> bool:
    return cpus <= SLURM_WHOLE_ASK or cpus % SLURM_TASK_CPUS == 0


def granted(least: int, most: int, room: int) -> int | None:
    """The CPUs a job needing LEAST and able to use MOST asks in ROOM: the most it can use that DoC's lua allocates as
    asked, else the fewest above that, up to ROOM; None when none is."""
    return next((c for c in range(most, least - 1, -1) if whole_ask(c)), None) or next(
        (c for c in range(max(least, most + 1), room + 1) if whole_ask(c)), None)


def whole_cores(cpus: int, threads: int) -> int:
    return max(1, -(-cpus // threads)) * threads


def slurm_cpu_slots(s: Slurm, cpus: int, units: int | None, est: int | None, mb_per_cpu: int = CPU_JOB_MIN_MEM_MB
                    ) -> list[CpuSlot]:
    """Every (partition, node) the read shows a job needing CPUS CPUs, and able to use UNITS (None: any), could start
    on now, in a partition whose MaxTime holds EST: one free card, the node's idle CPUs in whole cores within its free
    memory at MB_PER_CPU a CPU and our QoS's caps (what our pending jobs ask held against them). A proposal: Slurm
    answers it (slurm_sizes)."""
    most = max((n.threads for n in s.nodes.values()), default=1)

    def threads_of(j: SlurmJob) -> int:
        ns = ([n.threads for n in s.nodes.values() if n.name == j.req_nodes]
              or [n.threads for n in s.nodes.values() if set(j.partitions) & set(n.partitions)])
        return max(ns, default=most)

    waiting = [j for j in s.jobs if j.state == "PENDING"]
    held: dict[str, dict[str, int]] = {}
    for j in waiting:
        for q in {s.partitions[x].qos for x in j.partitions if x in s.partitions}:
            h = held.setdefault(q, dict.fromkeys(("jobs", "gpu", "cpu", "mem"), 0))
            for k, v in {"jobs": 1, "gpu": 1 if j.gpus is None else j.gpus, "cpu": whole_cores(j.cpus, threads_of(j)),
                         "mem": j.mem_mb}.items():
                h[k] += v
    out: list[CpuSlot] = []
    for n in s.nodes.values():
        mine = [j for j in waiting if j.req_nodes == n.name]
        if not n.usable or n.planned or n.cards - n.held - len(mine) < 1:
            continue
        idle = n.idle_cpus - sum(whole_cores(j.cpus, n.threads) for j in mine)
        free_mb = n.mem_mb - n.alloc_mem_mb - sum(j.mem_mb for j in mine)
        for name in n.partitions:
            p = s.partitions.get(name)
            if p is None or (est is not None and p.max_time_s is not None and p.max_time_s < est):
                continue
            h = held.get(p.qos, {})
            left = {k: lim.cap - lim.used - h.get(k, 0) for k, lim in s.qos.get(p.qos, {}).items()
                    if lim.cap is not None}
            room_mb = min(free_mb, left.get("mem", free_mb), left.get("node_mem", free_mb), p.max_mem_mb or free_mb)
            room = min(idle, left.get("cpu", idle), left.get("node_cpu", idle), room_mb // mb_per_cpu)
            room -= room % n.threads
            c = granted(max(cpus, 1), room if units is None else min(units, room), room)
            if c is None or any(left.get(k, 1) < 1 for k in ("jobs", "submit", "gpu")):
                continue
            out.append(CpuSlot(name, n.name, SLURM_CARDS.get(n.card, n.card), c, room_mb * c // room))
    return out


def weakest(card: str) -> int:
    return -SPEED.index(card) if card in SPEED else 1


def checkpointed(time_s: int) -> list[str]:
    """--time, with SIGUSR1 USR1_LEAD_S before it and a requeue after (job.sh), its log appended."""
    d, r = divmod(time_s, 86400)
    return [f"--time={d}-{r // 3600:02d}:{r % 3600 // 60:02d}:{r % 60:02d}", f"--signal=B:USR1@{USR1_LEAD_S}",
            "--requeue", "--open-mode=append"]


def cpu_slot_args(slot: CpuSlot, est: int | None) -> list[str]:
    """sbatch's options for CPU work in SLOT: one partition, one card, the slot's CPUs and memory on its node."""
    return ["-p", slot.partition, "--gres=gpu:1", f"--nodelist={slot.node}", f"--cpus-per-task={slot.cpus}",
            f"--mem={slot.mem_mb}M", *(checkpointed(est) if est is not None else [])]


# Slurm's own answer to "would this job start now" (sbatch --test-only: its lua, QoS, partition limits and node
# selection, submitting nothing). WILL_RUN, fed to `bash -s`, defines `ask I X ARGS...`: unless slot I already holds,
# it asks Slurm about ARGS and prints `Q<TAB>I<TAB>X<TAB>VERDICT<TAB>ANSWER`, VERDICT `now` when Slurm starts it at once
# with X CPUs or more, `later` when only later, `never` otherwise.
WILL_RUN = r"""ask() {
	i=$1 x=$2
	shift 2
	eval "[ -z \"\${held_$i:-}\" ]" || return 0
	o=$(sbatch --test-only "$@" --wrap true 2>&1)
	n=$(date +%Y-%m-%dT%H:%M:%S)
	r=$(printf '%s\n' "$o" | grep -m1 ' to start at ') || r=$(printf '%s\n' "$o" | grep -m1 'allocation failure') ||
		r=$(printf '%s\n' "$o" | grep 'error:' | grep -v '\*\*\*' | tail -n 1)
	v=never
	case $r in
	*" to start at "*" using "*" processors"*)
		t=${r#* to start at } p=${r#* using }
		t=${t%% *} p=${p%% *}
		if [ "$p" -ge "$x" ]; then
			v=later
			[ "$(printf '%s\n%s\n' "$t" "$n" | sort | head -n 1)" = "$t" ] && v=now
		fi
		;;
	esac
	[ "$v" = now ] && eval "held_$i=1"
	printf 'Q\t%s\t%s\t%s\t%s\n' "$i" "$x" "$v" "$r"
}
"""


def answered(out: str) -> dict[int, int]:
    """WILL_RUN's `now` lines: slot index -> the CPUs Slurm starts it with."""
    got: dict[int, int] = {}
    for line in out.splitlines():
        f = line.split("\t")
        if len(f) == 5 and f[0] == "Q" and f[3] == "now":
            got.setdefault(int(f[1]), int(f[2]))
    return got


def slurm_sizes(slots: list[CpuSlot], least: int, est: int | None) -> list[CpuSlot | None]:
    """Each of SLOTS as Slurm starts it now (one login for all): at the most CPUs, from its own count down through
    every count of at least LEAST the lua allocates whole, its memory scaled; None where it starts at none."""
    if not slots:
        return []
    rungs = [[x.cpus, *(c for c in range(x.cpus - 1, max(least, 1) - 1, -1) if whole_ask(c))] for x in slots]

    def at(slot: CpuSlot, c: int) -> CpuSlot:
        return replace(slot, cpus=c, mem_mb=slot.mem_mb * c // slot.cpus)

    script = WILL_RUN + "".join(shlex.join(["ask", str(i), str(c), *cpu_slot_args(at(slot, c), est)]) + "\n"
                                for i, slot in enumerate(slots) for c in rungs[i])
    got = answered(cached(script, timeout=PROBE_S))  # shared for CACHE_S, as the read it follows; a submit forgets it
    return [at(slot, got[i]) if i in got else None for i, slot in enumerate(slots)]


def slurm_starts_now(s: Slurm, cpus: int, units: int | None, est: int | None,
                     mb_per_cpu: int = CPU_JOB_MIN_MEM_MB) -> list[CpuSlot]:
    """The GPU-cluster slots Slurm starts now for a job of at least CPUS CPUs, best first: the most CPUs, then the
    weakest card, then the higher PriorityTier."""
    slots = [x for x in slurm_sizes(slurm_cpu_slots(s, cpus, units, est, mb_per_cpu), cpus, est) if x is not None]
    return sorted(slots, key=lambda x: (-x.cpus, weakest(x.card), -s.partitions[x.partition].tier, x.partition, x.node))


# ---- cpucluster1 ----
@dataclass(frozen=True)
class CpuNode:
    usable: bool
    cpus: int
    idle: int
    mem_mb: int
    alloc_mb: int
    partitions: tuple[str, ...] = ()


@dataclass(frozen=True)
class CpuPartition:
    name: str
    max_time_s: int | None
    node_cpus: int  # the largest usable node's CPUs
    idle_cpus: int
    def_mem_per_cpu_mb: int


@dataclass(frozen=True)
class CpuCluster:
    partitions: dict[str, CpuPartition]
    nodes: dict[str, CpuNode]
    cap: Limit  # our per-user CPUs (the QoS's MaxTRESPU cpu)
    mem: Limit  # our per-user memory, MB
    node: dict[str, Limit]  # MaxTRESPN: the most one job may take on one node (node_cpu, node_mem MB)
    submit: Limit
    jobs: list[SlurmJob]
    takes_jobs: bool = True  # its controller can save a batch job's script (takes_jobs)


# cpucluster1's login node is its Slurm controller, which saves every batch job's script and environment into its
# StateSaveLocation; with that filesystem full every sbatch fails ("I/O error writing script/environment to file",
# job_mgr.c's _copy_job_desc_to_file). Measured failing at 0, 15 and 53 MB free (18 Nov); a script is a few KB, but
# the controller rewrites its job_state file there too, so a GB is the bar.
STATE_FREE_MIN_KB = 1 << 20
# Added to SLURM_READ on cpucluster1: this host, the controller's, the StateSaveLocation and its free KB there
STATESAVE_READ = r"""echo ==statesave; cfg=$(scontrol show config); d=$(printf '%s
' "$cfg" | sed -n 's/^StateSaveLocation *= *//p')
h=$(printf '%s
' "$cfg" | sed -n 's/^SlurmctldHost\[0\] *= *\([^( ]*\).*/\1/p')
printf '%s %s %s %s
' "$(hostname -s)" "${h:--}" "${d:--}" "$(df -Pk "$d" 2>/dev/null | awk 'NR==2 {print $4}')"
"""
CPU_READ = SLURM_READ.replace("echo ==end", STATESAVE_READ + "echo ==end")


def takes_jobs(lines: list[str]) -> bool:
    """Whether cpucluster1 can take a batch job, from CPU_READ's statesave line: not when its controller is this
    login node and the StateSaveLocation there has less than STATE_FREE_MIN_KB free. Where that can't be read (another
    controller host, no df), sbatch decides."""
    f = " ".join(lines).split()
    return not (len(f) == 4 and f[0] == f[1] and f[3].isdigit() and int(f[3]) < STATE_FREE_MIN_KB)


def parse_cpucluster(text: str) -> CpuCluster:
    """CPU_READ on cpucluster1. Every partition shares one QoS, so our caps are one number each."""
    s = sections(text)
    if "end" not in s:
        raise Refused("cpucluster1's Slurm read was cut short")
    qos = parse_qos(s.get("qos", []))
    nodes: dict[str, CpuNode] = {}
    for f in (line.split("|") for line in s.get("nodes", [])):
        if len(f) < 6:
            continue
        name, part = f[0], f[1].rstrip("*")
        _, idle, _, total = (int(x) for x in f[3].split("/"))
        seen = nodes.get(name)
        nodes[name] = CpuNode(usable(f[2], name), total, idle, int(f[4]), int(f[5] or 0),
                              tuple(sorted({*(seen.partitions if seen else ()), part})))
    lines = s.get("partitions", [])
    qos_names = {dict(re.findall(r"(\w+)=(\S*)", line)).get("QoS", "") for line in lines}
    if len(qos_names) > 1:
        raise Refused(f"cpucluster1's partitions now have QoS {', '.join(sorted(qos_names))}: tools/compute reads one "
                      "cap for them all")
    ours = qos.get(next(iter(qos_names), ""), {})
    partitions: dict[str, CpuPartition] = {}
    for line in lines:
        kv = dict(re.findall(r"(\w+)=(\S*)", line))
        name = kv.get("PartitionName", "")
        ns = [n for n in nodes.values() if n.usable and name in n.partitions]
        if ns:
            partitions[name] = CpuPartition(name, seconds(kv.get("MaxTime", "UNLIMITED")), max(n.cpus for n in ns),
                                            sum(n.idle for n in ns), int(kv.get("DefMemPerCPU", "0")) or 1)
    return CpuCluster(partitions, nodes, ours.get("cpu", Limit(None, 0)), ours.get("mem", Limit(None, 0)),
                      {k: v for k, v in ours.items() if k in ("node_cpu", "node_mem")},
                      ours.get("submit", Limit(None, 0)), parse_jobs(s.get("jobs", [])),
                      takes_jobs(s.get("statesave", [])))


def read_cpucluster(max_age: float = 0.0) -> CpuCluster:
    return parse_cpucluster(cached(CPU_READ, (CPU_HOST,), max_age))


def cpu_free(c: CpuCluster, n: CpuNode, mb_per_cpu: int) -> int:
    """The CPUs one job of ours could start with on node N now at MB_PER_CPU a CPU: its idle CPUs within its free
    memory and our caps (per node, and what is left of our per-user CPUs and memory)."""
    caps = [n.idle, (n.mem_mb - n.alloc_mb) // mb_per_cpu]
    if (x := c.node.get("node_cpu")) and x.cap is not None:
        caps.append(x.cap)
    if (x := c.node.get("node_mem")) and x.cap is not None:
        caps.append(x.cap // mb_per_cpu)
    if c.cap.cap is not None:
        caps.append(c.cap.cap - c.cap.used)
    if c.mem.cap is not None:
        caps.append((c.mem.cap - c.mem.used) // mb_per_cpu)
    return max(0, min(caps))


def cpucluster_best(c: CpuCluster, cpus: int, mb_per_cpu: int, time_s: int) -> tuple[str, int] | None:
    """(partition, CPUs) for a job of at least CPUS CPUs at MB_PER_CPU that a node could start now, the most CPUs a
    node gives; None when no node can."""
    best: tuple[str, int] | None = None
    for p in c.partitions.values():
        if p.max_time_s is not None and time_s > p.max_time_s:
            continue
        for n in c.nodes.values():
            if n.usable and p.name in n.partitions:
                free = cpu_free(c, n, mb_per_cpu)
                if free >= cpus and (best is None or free > best[1]):
                    best = (p.name, free)
    return best


def cpucluster_grants(asks: list[tuple[str, int, int]], est: int) -> list[int | None]:
    """For each (partition, CPUs, MB a CPU) of ASKS, the most CPUs up to its own that Slurm starts now there
    (halving down to 1), or None."""
    if not asks:
        return []
    rungs = [sorted({max(1, x >> k) for k in range(x.bit_length())}, reverse=True) for _, x, _ in asks]
    script = WILL_RUN + "".join(shlex.join(["ask", str(i), str(x), "-p", part, f"--cpus-per-task={x}",
                                            f"--mem-per-cpu={mb}M", *checkpointed(est)]) + "\n"
                                for i, (part, _, mb) in enumerate(asks) for x in rungs[i])
    got = answered(login("bash -s", script.encode(), (CPU_HOST,), timeout=PROBE_S))
    return [got.get(i) for i in range(len(asks))]


# ---- Condor ----
CONDOR_STATE = {1: "idle", 2: "running", 3: "removed", 4: "completed", 5: "held", 6: "transferring", 7: "suspended"}
CONDOR_OSES = ("Ubuntu18", "Ubuntu20", "Ubuntu22", "Ubuntu24", "Ubuntu26")
CONDOR_COLUMNS = ("Machine", "State", "OpSysAndVer", "TotalCpus", "Memory", "JobStarts")
# The one hold Condor releases by itself: its first machine couldn't yet see the new job folder (HoldReasonCode 7,
# subcode 2: no condor.out to open; 12 of 5,982 jobs, the previous project's measurement), released once onto another
CONDOR_RELEASE = "(HoldReasonCode == 7) && (HoldReasonSubCode == 2) && (NumShadowStarts - NumJobStarts == 1)"
CONDOR_HELD = "PeriodicRelease HoldReason"
# Lab machines that don't mount our workspace: every job there is held at once ("Failed to open
# 'WORKSPACE/jobs/.../condor.out' as standard output": melchett, 523 pieces, 19 Nov)
CONDOR_EXCLUDE = ("melchett.doc.ic.ac.uk",)
# A machine whose execute folder has less free (Condor's Disk, KiB) takes no piece: a piece takes no task under 2 GB
# free (piece.DISK_MIN_MB) and ends at once, so such a machine stayed free and drew piece after piece (gpu35, 0.9 GB,
# 19 Nov; 8 of 343 machines under 4 GB)
CONDOR_DISK_KB = 4 << 20


@dataclass(frozen=True)
class CondorMachine:
    name: str
    state: str  # Unclaimed, Owner, Claimed, ...
    release: str
    cpus: int
    memory_mb: int
    job_starts: int | None


@dataclass(frozen=True)
class CondorJob:
    name: str  # JobBatchName: bc26-ID
    cluster: str
    state: str
    host: str
    hold_reason: str
    releases: bool
    kind: str = ""  # a piece's BC26Kind (condor_sub): the runs it serves


@dataclass(frozen=True)
class Condor:
    machines: dict[str, CondorMachine]
    jobs: list[CondorJob]
    refused: tuple[str, ...]
    by_os: dict[str, int] = field(default_factory=dict[str, int])

    def free(self, cpus: int = 1, mem_mb: int = 0) -> list[CondorMachine]:
        """Unclaimed machines a job could take: of our releases, with CPUS cores and MEM_MB MiB."""
        return [m for m in self.machines.values() if m.state == "Unclaimed" and m.release in CONDOR_OSES
                and m.cpus >= cpus and m.memory_mb >= mem_mb and m.name not in CONDOR_EXCLUDE]


def parse_condor(text: str) -> Condor:
    s = sections(text)
    machines: dict[str, CondorMachine] = {}
    refused: list[str] = []
    for line in s.get("machines", []):
        if not line.strip():
            continue
        v = line.split(", ")
        if len(v) != len(CONDOR_COLUMNS):
            refused.append(line[:200])
            continue
        ad = dict(zip(CONDOR_COLUMNS, v, strict=True))
        try:
            m = CondorMachine(ad["Machine"], ad["State"], ad["OpSysAndVer"], int(float(ad["TotalCpus"])),
                              int(ad["Memory"]), int(ad["JobStarts"]) if ad["JobStarts"].isdigit() else None)
        except ValueError:
            refused.append(line[:200])
            continue
        machines[m.name] = m
    jobs = [CondorJob(f[0], f[1], CONDOR_STATE.get(int(f[2]), f[2]), f[3], "" if f[6] in ("undefined", "") else f[6],
                      f[4] == "true", "" if f[5] == "undefined" else f[5])
            for f in ((line.split(maxsplit=6) + ["", ""])[:7] for line in s.get("jobs", [])) if f[2].isdigit()]
    c = Condor(machines, jobs, tuple(refused))
    for m in c.free():
        c.by_os[m.release] = c.by_os.get(m.release, 0) + m.cpus
    return c


CONDOR_READ = (f"{CONDOR}; echo ==machines; condor_status -af:, {' '.join(CONDOR_COLUMNS)}; echo ==jobs; "
               f"condor_q -name {SUBMIT} $USER -af JobBatchName ClusterId JobStatus RemoteHost PeriodicRelease "
               "BC26Kind HoldReason; "
               "echo ==end")


def read_condor(max_age: float = 0.0) -> Condor:
    """The pool and our jobs, read from a GPU-cluster login node (no lab machine logged into)."""
    return parse_condor(cached(CONDOR_READ, max_age=max_age))


def condor_sub(job_dir: str, job_id: str, cpus: int, mem_mb: int, slot_mb: int | None, remove_s: int | None,
               kind: str = "") -> str:
    """The submit description: request_cpus = 1 (a lab machine is one static slot, so this claims all of it, DoC's
    rule) on a machine of our releases with CPUS cores and MEM_MB MiB, read and written in place on the shared volume.
    Ranked by the workers it runs (SLOT_MB a worker: the CPUs its memory holds; None: its CPUs), the machine of its
    last start last. REMOVE_S: removed once it has run that long (a bound, for a first job). KIND: the runs a piece
    serves (BC26Kind: placement counts the queued pieces of each kind)."""
    release = " || ".join(f'OpSysAndVer == "{o}"' for o in CONDOR_OSES)
    workers = ("TARGET.TotalCpus" if slot_mb is None else
               f"ifThenElse(TARGET.TotalCpus * {slot_mb} <= TARGET.Memory, TARGET.TotalCpus, "
               f"TARGET.Memory / {slot_mb})")
    return "\n".join([
        "universe = vanilla", "executable = /bin/bash", "transfer_executable = false",
        f'arguments = "{job_dir}/job.sh {job_dir} condor"', "should_transfer_files = NO", "request_cpus = 1",
        f"requirements = ({release}) && (TotalCpus >= {cpus}) && (Memory >= {mem_mb}) && (Disk >= {CONDOR_DISK_KB})"
        + "".join(f' && (Machine != "{m}")' for m in CONDOR_EXCLUDE),
        "job_machine_attrs = Machine", f"rank = ifThenElse(TARGET.Machine =?= MY.MachineAttrMachine0, 0, {workers})",
        f"output = {job_dir}/condor.out", f"error = {job_dir}/condor.err", f"log = {job_dir}/condor.log",
        f'JobBatchName = "bc26-{job_id}"', "notification = never", f"periodic_release = {CONDOR_RELEASE}",
        *([f'+BC26Kind = "{kind}"'] if kind else []),
        *([f"periodic_remove = (JobStatus == 2) && (time() - EnteredCurrentStatus > {remove_s})"] if remove_s else []),
        "queue", ""])


