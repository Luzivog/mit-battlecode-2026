package awubot_us.robot.rat;

import battlecode.common.*;
import awubot_us.CellTracker;
import awubot_us.Pathfinder;
import awubot_us.comms.GlobalArray;
import awubot_us.robot.Strategy;

import static awubot_us.Game.*;
import static awubot_us.util.Util.indicate;

// TODO: finish writing this

public class RushStrategy extends Strategy {
    private ExploreStrategy exploreStrategy;
    private CheeseStrategy cheeseStrategy;
    private FormationStrategy formationStrategy;

    public RushStrategy() {
        exploreStrategy = new ExploreStrategy();
        cheeseStrategy = new CheeseStrategy();
        formationStrategy = new FormationStrategy();
    }

    @Override
    public boolean runTick() throws GameActionException {
        MapLocation myEnemyTarget = getEnemyKingLocation();
        if (myEnemyTarget != null) {
            indicate("RUSHING -> " + myEnemyTarget);
            rc.setIndicatorLine(myLoc, myEnemyTarget, 255, 128, 0);
            Pathfinder.makeMove(myEnemyTarget);
        } else {
            exploreStrategy.runTick();
        }
        return true;
    }

    private MapLocation getEnemyKingLocation() {
        if (CellTracker.isSymmetryKnown()) {
            switch (CellTracker.getSymmetry()) {
                case Rotational:
                    return new MapLocation(mapWidth - 1 - GlobalArray.nearestRatKing.x, mapHeight - 1 - GlobalArray.nearestRatKing.y);
                case Horizontal:
                    return new MapLocation(mapWidth - 1 - GlobalArray.nearestRatKing.x, GlobalArray.nearestRatKing.y);
                case Vertical:
                    return new MapLocation(GlobalArray.nearestRatKing.x, mapHeight - 1 - GlobalArray.nearestRatKing.y);
                default:
                    break;
            }
        }

        return new MapLocation(mapWidth - 1 - GlobalArray.nearestRatKing.x, mapHeight - 1 - GlobalArray.nearestRatKing.y);
    }

    private static MapLocation findNearbyCat() {
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.getType() == UnitType.CAT) {
                return robot.location;
            }
        }
        return null;
    }

    private void fleeFromCat(MapLocation catLoc) throws GameActionException {
        Direction awayFromCat = catLoc.directionTo(myLoc);
        Pathfinder.makeMove(myLoc.add(awayFromCat));
    }
}
