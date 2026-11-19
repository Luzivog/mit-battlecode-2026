#!/bin/bash
# Compiles the translator, the substitutes and the native-build Java classes into ~/battlecode26-data/engine/build.
set -e
cd "$(dirname "$0")"
J=~/battlecode26-data/engine/jars
B=${BCX_BUILD:-~/battlecode26-data/engine/build}
CP=$J/battlecode26-java-1.2.6.jar:$J/asm-commons-9.7.jar:$J/asm-analysis-9.7.jar:$J/asm-util-9.7.jar
rm -rf $B/jvm-tr && mkdir -p $B/jvm-tr
javac -nowarn -d $B/jvm-tr -cp $CP $(find src/bcx/tr -name '*.java')
javac -nowarn --add-exports java.base/jdk.internal.misc=ALL-UNNAMED --add-exports java.base/jdk.internal.math=ALL-UNNAMED --add-exports java.base/sun.nio.cs=ALL-UNNAMED -d $B/jvm-tr -cp $CP $(find subst -name '*.java') src/bcx/NativeMain.java src/bcx/NativeControlProvider.java src/bcx/NativeIO.java src/bcx/Match.java src/battlecode/world/*.java
