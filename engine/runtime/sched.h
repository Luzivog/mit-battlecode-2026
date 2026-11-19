// Robots as coroutines: the native replacement for SandboxedRobotPlayer / PlayerControlProvider's threads.
#pragma once
#include "rt.h"

void sched_init();
void sched_pause();                    // the robot yields until its next turn (RobotMonitor.pause's pauser)
[[noreturn]] void sched_robot_die();   // RobotDeathException: the robot ends for good
jint sched_identity_hash(Obj* o);      // ObjectMethods.identityHashCode of the running robot
Obj** sched_enum_cache(Klass* k);      // the running robot's cache of k's enum constants

// The NativeControlProvider natives (see engine/jvm/src/bcx/NativeControlProvider.java)
void sched_spawn(jint id, Obj* pkg, Obj* rc);
void sched_run(jint id, jint limit);
void sched_kill(jint id);
jint sched_bytecodes_used(jint id);
jboolean sched_terminated(jint id);
jboolean sched_crashed(jint id);
void sched_match_ended();
