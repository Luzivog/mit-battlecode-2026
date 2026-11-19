package bcx;

import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;
import battlecode.world.control.RobotControlProvider;

/**
 * PlayerControlProvider for the native engine: each robot is a coroutine (engine/runtime/sched.cpp) running its
 * team's translated classes, seeded with its id, with the bytecode limit set before each turn.
 */
public final class NativeControlProvider implements RobotControlProvider {
    private final String pkg;

    public NativeControlProvider(String pkg) { this.pkg = pkg; }

    public void matchStarted(GameWorld world) {}
    public void matchEnded() { matchEnded0(); }
    public void robotSpawned(InternalRobot robot) { spawn0(robot.getID(), pkg, robot.getController()); }
    public void robotKilled(InternalRobot robot) { kill0(robot.getID()); }
    public void roundStarted() {}
    public void roundEnded() {}
    public void runRobot(InternalRobot robot) { run0(robot.getID(), robot.getBytecodeLimit()); }
    public int getBytecodesUsed(InternalRobot robot) { return bytecodesUsed0(robot.getID()); }
    public boolean getTerminated(InternalRobot robot) { return terminated0(robot.getID()); }

    /** Whether the robot's run() ended in its last turn as the engine reports a crash (sched.cpp, robot_main). */
    public static boolean crashed(int id) { return crashed0(id); }

    private static native void spawn0(int id, String pkg, battlecode.common.RobotController rc);
    private static native boolean crashed0(int id);
    private static native void kill0(int id);
    private static native void run0(int id, int limit);
    private static native int bytecodesUsed0(int id);
    private static native boolean terminated0(int id);
    private static native void matchEnded0();
}
