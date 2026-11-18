package opp_parmesan.Utils;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import opp_parmesan.Utils.Globals;
import opp_parmesan.Utils.Vision;

public class CatsAreWalls extends Globals {

    public static void pretend (MapLocation loc, Direction dir) {
        switch (dir) {
            case SOUTHWEST:
                loc = loc.add(Direction.SOUTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.WEST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                break;
            case SOUTHEAST:
                loc = loc.add(Direction.SOUTHEAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.EAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                break;
            case NORTHWEST:
                loc = loc.add(Direction.NORTHWEST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.EAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                break;
            case NORTHEAST:
                loc = loc.add(Direction.EAST).add(Direction.NORTHEAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.WEST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                break;
            case NORTH:
                loc = loc.add(Direction.NORTH).add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.EAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                break;
            case SOUTH:
                loc = loc.add(Direction.SOUTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.EAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                break;
            case WEST:
                loc = loc.add(Direction.WEST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                break;
            case EAST:
                loc = loc.add(Direction.EAST).add(Direction.EAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] |= 1L << loc.y;
                break;
        }
    }

    public static void unpretend (MapLocation loc, Direction dir) {
        switch (dir) {
            case SOUTHWEST:
                loc = loc.add(Direction.SOUTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.WEST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                break;
            case SOUTHEAST:
                loc = loc.add(Direction.SOUTHEAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.EAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                break;
            case NORTHWEST:
                loc = loc.add(Direction.NORTHWEST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.EAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                break;
            case NORTHEAST:
                loc = loc.add(Direction.EAST).add(Direction.NORTHEAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.WEST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                break;
            case NORTH:
                loc = loc.add(Direction.NORTH).add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.EAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                break;
            case SOUTH:
                loc = loc.add(Direction.SOUTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.EAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                break;
            case WEST:
                loc = loc.add(Direction.WEST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                break;
            case EAST:
                loc = loc.add(Direction.EAST).add(Direction.EAST);
                if (rc.onTheMap(loc)) opp_parmesan.Utils.Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                loc = loc.add(Direction.NORTH);
                if (rc.onTheMap(loc)) Vision.isDangerous[loc.x] &= ~(1L << loc.y);
                break;
        }
    }

}
