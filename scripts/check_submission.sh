#!/usr/bin/env bash
# Build submission.zip for one bot package and check it the way the official compile server (Saturn) does:
# unzip into a fresh scaffold's src/, run `./gradlew verify -Pteam=<pkg>`, and enforce the 5 MiB upload limit.
# Uploads nothing. Usage: scripts/check_submission.sh [package]   (default: challenger)
set -euo pipefail
PKG="${1:-challenger}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LIMIT=$((5 * 1024 * 1024))   # galaxy backend: FileValidator(max_size=5 * 1024 * 1024)

cd "$ROOT"
./gradlew -q zipForSubmit -Pteam="$PKG"
SIZE=$(stat -c %s submission.zip)
echo "submission.zip: $SIZE bytes (limit $LIMIT), $(unzip -Z1 submission.zip | grep -c '\.java$') .java files"
if unzip -Z1 submission.zip | grep -qv "^$PKG/"; then
  echo "FAIL: zip contains files outside $PKG/"; exit 1
fi
[ "$SIZE" -le "$LIMIT" ] || { echo "FAIL: zip exceeds the 5 MiB upload limit"; exit 1; }

SCAFFOLD="$(mktemp -d /tmp/bc26-submission-check.XXXXXX)"
cp -r build.gradle gradlew gradle gradle.properties engine_version.txt client_version.txt "$SCAFFOLD/"
mkdir "$SCAFFOLD/src" && (cd "$SCAFFOLD/src" && unzip -q "$ROOT/submission.zip")
if (cd "$SCAFFOLD" && ./gradlew -q verify -Pteam="$PKG" -x unpackClient > verify.log 2>&1); then
  echo "OK: $PKG compiles alone and passes the engine Verifier (scaffold: $SCAFFOLD)"
else
  tail -20 "$SCAFFOLD/verify.log"; echo "FAIL: verify failed (scaffold: $SCAFFOLD)"; exit 1
fi
