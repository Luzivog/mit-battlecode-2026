package opp_gst.Communication;

import battlecode.common.MapLocation;

public record EnemyRatKingSqueakInfo(MapLocation location, int health) implements SqueakInfo{

    @Override
    public String toString() {
        return "EnemyRatKingSqueakInfo{" +
                "location=" + location +
                ", health=" + health +
                '}';
    }
}
