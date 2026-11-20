#!/usr/bin/env bash
# The node side of tools/compute: `job.sh JOB_DIR BACKEND` (slurm, cpucluster1, condor; local in tests). The files
# JOB_DIR holds are tools/jobs.py's docstring's (an interface: change them only together).
#
# It clones the submitted commit from base.bundle (when there: main, shared by many runs) then code.bundle (when
# there: what the commit adds to main) into scratch (deleted at the end), unpacks with.tgz into it, and runs
# argv there with: JOB_ID, JOB_DIR, JOB_OUT and OUT (JOB_DIR/out, where results go), JOB_CPUS (the CPUs it may use),
# JOB_MEM_MB (the memory it may use), `env`'s names (java and python3 first on PATH), and for a kit job BC_ENGINE_JARS
# and BC_CLASSPATH (tools/kit.py). HOME and TMPDIR are scratch. Its output goes to JOB_DIR/log, cut at LOG_CAP bytes.
# JOB_DIR/exit, empty from submission, gets the exit code once the job has ended for good (written in place: a new
# name can stay unseen for a minute on another NFS client). A setup that fails before the command starts ends 255.
#   SIGUSR1 (Slurm, 120 s before --time): passed to the command, which may save a checkpoint in JOB_OUT and exit 0;
#   the job then requeues itself and the next attempt resumes. Any other exit after it ends the job with that code.
#   SIGTERM (cancel, Condor eviction): passed on; on Condor no exit file, since Condor reruns an evicted job.
# It keeps to what the oldest lab release has (bash 4.4, git 2.17 on Ubuntu 18.04).
set -u
umask 077
dir=$1 backend=$2
LOG_CAP=$((64 << 20))  # tools/jobs.py's LOG_CAP
exec >>"$dir/log" 2>&1
: >"$dir/exit"  # an attempt that Slurm requeued from outside may have written it
rm -f "$dir/memory"
export JOB_ID=${dir##*/} JOB_DIR=$dir JOB_OUT=$dir/out OUT=$dir/out JOB_CPUS JOB_MEM_MB
JOB_CPUS=$(nproc)
# On Slurm, the CPUs asked where nproc shows more (an odd ask on a two-thread node is given a whole core). DoC's
# job_submit.lua rewrites a large --cpus-per-task and keeps the ask only in SLURM_TRES_PER_TASK.
ask=${SLURM_CPUS_PER_TASK:-}
case ${SLURM_TRES_PER_TASK:-} in *cpu=*) ask=${SLURM_TRES_PER_TASK#*cpu=} ask=${ask%%,*} ;; esac
if [ -n "$ask" ] && [ "$ask" -lt "$JOB_CPUS" ]; then
	JOB_CPUS=$ask
fi
# The memory it may use: Slurm's allocation, else what the machine has free now (a lab machine is ours whole)
if [ -n "${SLURM_MEM_PER_NODE:-}" ]; then
	JOB_MEM_MB=$SLURM_MEM_PER_NODE
elif [ -n "${SLURM_MEM_PER_CPU:-}" ]; then
	JOB_MEM_MB=$((SLURM_MEM_PER_CPU * ${SLURM_CPUS_ON_NODE:-$JOB_CPUS}))
else
	JOB_MEM_MB=$(($(sed -n 's/^MemAvailable: *\([0-9]*\) kB$/\1/p' /proc/meminfo) / 1024))
fi
cg=/sys/fs/cgroup$(sed -n 's/^0:://p' /proc/self/cgroup)
# The cgroup is the job's own when job.sh is its only process (Slurm's task cgroup); Condor starts every job in
# condor.service's cgroup, whose counts aren't the job's, so such a job records none.
own=1
while read -r p; do [ "$p" = $$ ] || own=; done 2>/dev/null <"$cg/cgroup.procs"
memory_() {
	local kills= peak= k v
	[ -n "$own" ] || return 0
	while read -r k v; do [ "$k" = oom_kill ] && kills=$v; done 2>/dev/null <"$cg/memory.events"
	read -r peak 2>/dev/null <"$cg/memory.peak"
	[ -n "$kills" ] && printf 'peak %s\noom_kill %s\n' "$peak" "$kills" >"$dir/memory"
	[ -z "$peak" ] || echo "== memory peak $peak bytes"
	[ "${kills:-0}" = 0 ] || echo "== the kernel's OOM killer took $kills of this job's processes"
}
# Ended for good: its inputs go, and the exit file gets the code.
done_() { memory_; rm -f "$dir/base.bundle" "$dir/code.bundle" "$dir/with.tgz"; echo "$1" >"$dir/exit"; exit 0; }
setup_() { echo "== setup failed: $1"; done_ 255; }
scratch=$(mktemp -d "${TMPDIR:-/tmp}/bc26-job-XXXXXX") || setup_ "no scratch folder"
trap 'trap "" TERM; rm -rf "$scratch"' EXIT
read -r commit <"$dir/commit" || setup_ "no commit file"
mkdir -p "$JOB_OUT" "$scratch/home" "$scratch/tmp" || setup_ "mkdir"
code=$scratch/code
# The bundles hold the commit and main, base first (code.bundle may need its commits): every head fetched by its hash,
# main kept as main (git log main works)
fetch_() {
	[ -s "$1" ] || return 0
	heads=$(git -C "$code" bundle list-heads "$1") &&
		git -C "$code" fetch -q "$1" $(printf '%s\n' "$heads" | cut -d' ' -f1 | sort -u) &&
		main=$(printf '%s\n' "$heads" | sed -n 's| refs/heads/main$||p') &&
		{ [ -z "$main" ] || git -C "$code" update-ref refs/heads/main "$main"; }
}
{ git -c init.defaultBranch=job init -q "$code" && fetch_ "$dir/base.bundle" && fetch_ "$dir/code.bundle" &&
	git -C "$code" -c advice.detachedHead=false checkout -q --detach "$commit"; } || setup_ "checkout of $commit"
if [ -s "$dir/with.tgz" ]; then
	tar -xzf "$dir/with.tgz" -C "$code" || setup_ "unpacking with.tgz"
fi
export HOME=$scratch/home XDG_CACHE_HOME=$scratch/home/.cache TMPDIR=$scratch/tmp
if [ -s "$dir/env" ]; then
	while read -r name value; do
		case $name in
		PATH) [ -e "$value" ] || setup_ "$value is gone"; PATH=$value:$PATH ;;
		*) export "$name=$value" ;;
		esac
	done <"$dir/env"
	export PATH
fi
if [ -s "$dir/kit" ]; then
	jars=
	while read -r jar; do
		[ -s "$jar" ] || setup_ "the engine jar $jar is gone"
		jars=${jars:+$jars:}$jar
	done <"$dir/kit"
	export BC_ENGINE_JARS=$jars BC_CLASSPATH=$code/build/classes:$jars
fi
argv=()
while IFS= read -r -d '' a; do argv+=("$a"); done <"$dir/argv"
cpu=$(sed -n 's/^model name[[:space:]]*:[[:space:]]*//p;T;q' /proc/cpuinfo)
echo "== $(date -u +%FT%TZ) start on $(hostname -s), attempt ${SLURM_RESTART_COUNT:-0}, $JOB_CPUS CPUs, $JOB_MEM_MB MB, commit $commit, cpu ${cpu:-unknown}"
# The command's output reaches the log through a FIFO and a cap: a job's writes on the shared volume are bounded.
mkfifo "$scratch/fifo" || setup_ "mkfifo"
{
	head -c "$LOG_CAP"
	n=$(wc -c)
	[ "$n" = 0 ] || printf '\n== log cut at %s bytes: %s more dropped\n' "$LOG_CAP" "$n"
} <"$scratch/fifo" & cap=$!
sig= pid= intr=
trap 'sig=USR1 intr=1; kill -USR1 $pid 2>/dev/null' USR1
trap 'sig=TERM intr=1; kill -TERM $pid 2>/dev/null' TERM
(cd "$code" && exec "${argv[@]}") >"$scratch/fifo" 2>&1 </dev/null & pid=$!
# A trapped signal ends a wait early (its status is the signal's, not the command's), so only then wait again, until
# a wait returns the command's own status. A pid already waited for answers 127 (a trap that came just after the
# command's own status): the status kept is that one.
rc=
while :; do
	intr=
	wait $pid; r=$?
	[ "$r" = 127 ] && [ -n "$rc" ] && break
	rc=$r
	[ -n "$intr" ] || break
done
wait $cap
echo "== $(date -u +%FT%TZ) exit $rc${sig:+ after SIG$sig}"
# HOME is scratch, deleted with the job: a job that would end 0 having left a file there ends 253, naming it.
if [ -z "$sig" ] && [ "$rc" = 0 ]; then
	wrote=$(find "$HOME" -mindepth 1 -name '.*' -prune -o -type f -print 2>/dev/null | head -n 5)
	[ -z "$wrote" ] || { echo "== it wrote under its HOME, scratch here and deleted with it:"; echo "$wrote"; done_ 253; }
fi
case $sig,$rc in
USR1,0)
	for try in 1 2 3 4 5 6; do
		scontrol requeue "$SLURM_JOB_ID" && echo "== requeued: the next attempt resumes from JOB_OUT" && exit 0
		[ "$try" = 6 ] || sleep 10
	done
	echo "== the checkpoint is saved but Slurm refused the requeue: resubmit to resume"
	done_ 75 ;;
USR1,*) echo "== the command failed after SIGUSR1, so the job ends here" && done_ "$rc" ;;
TERM,*) [ "$backend" = condor ] && exit 0; done_ "$rc" ;;
*) done_ "$rc" ;;
esac
