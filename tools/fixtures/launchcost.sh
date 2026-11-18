#!/usr/bin/env bash
# What a process launch costs on a node, and whether exec auditing is visible there: 2000 launches of /bin/true and
# 40 of `java -version` (the compute skill's numbers). Run as a job: tools/compute run --where W -- bash tools/fixtures/launchcost.sh
echo "host $(hostname -s), $(nproc) CPUs, $(grep -m1 'model name' /proc/cpuinfo | cut -d: -f2-)"
echo "auditd: $(systemctl is-active auditd 2>&1); process accounting: $(systemctl is-active acct psacct 2>&1 | tr '\n' ' ')"
t0=$(date +%s%N)
for i in $(seq 2000); do /bin/true; done
t1=$(date +%s%N)
for i in $(seq 40); do java -version >/dev/null 2>&1; done
t2=$(date +%s%N)
echo "/bin/true: $(( (t1 - t0) / 2000 / 1000 )) us a launch"
echo "java -version: $(( (t2 - t1) / 40 / 1000000 )) ms a launch"
