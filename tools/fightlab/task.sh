#!/bin/bash
# One fight-lab task (tools/run --each): play the jobs of FILE with fightlab.FightLab, results into OUT/results.jsonl.
# Run from anywhere: it plays from the checkout holding it (maps/, build/classes, tools/fightlab/{cls,lib}).
set -e
FILE=$(realpath "$1"); OUT=$(realpath "$2")
cd "$(dirname "$(realpath "$0")")/../.."
mkdir -p "$OUT"
exec java -Xmx1g -XX:+UseSerialGC -XX:+UnlockDiagnosticVMOptions -XX:-BytecodeVerificationRemote \
  -cp tools/fightlab/cls:tools/fightlab/lib/battlecode26-java-1.2.6.jar \
  fightlab.FightLab "$FILE" "$OUT/results.jsonl" --bots build/classes --maps maps 2> "$OUT/stderr.log"
