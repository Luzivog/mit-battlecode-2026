package awubot.robot.rat;

import battlecode.common.GameActionException;

public abstract class Rat {
    public abstract void init() throws GameActionException;
    public abstract void run() throws GameActionException;
}
