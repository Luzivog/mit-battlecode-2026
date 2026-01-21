package awubot_bugfixes.comms;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Enemy {
    public MapLocation location;
    public Direction direction;
    public int health;

    public Enemy(MapLocation location, Direction direction, int health) {
        this.location = location;
        this.direction = direction;
        this.health = health;
    }
}
