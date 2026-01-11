package experiment.robot;

import battlecode.common.GameActionException;

public abstract class Strategy {
    public boolean tryStrategy(Strategy strat) throws GameActionException {
        if (strat == null) return false;
        if (strat.isComplete()) return false;
        strat.runTick();
        return !strat.isComplete();
    }

    public abstract boolean isComplete() throws GameActionException;

    public abstract void runTick() throws GameActionException;
}
