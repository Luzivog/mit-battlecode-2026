package opp_gst.Communication;

import battlecode.common.MapLocation;

public record CheeseMineInfo(MapLocation location, int threatLevel) {
    @Override
    public String toString() {
        return "CheeseMineInfo{" +
                "location=" + location +
                ", threatLevel=" + threatLevel +
                '}';
    }
}
