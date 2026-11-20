package bcplay;

import battlecode.common.RobotInfo;
import battlecode.world.GameMapIO;
import battlecode.world.LiveMap;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;

/**
 * Prints one JSON line per map file: what the engine loads from it (GameMapIO.loadMap), as counts and as hashes of
 * each part, so two files can be compared by content rather than by bytes, and the map itself for mapclass.py:
 * grid rows (y = 0 first; '#' wall, 'M' mine, 'd' dirt, 'c' cheese, '.'), bodies, cat waypoints.
 * Usage: java -cp ENGINE.jar:. bcplay.MapInfo FILE.map26...
 */
public final class MapInfo {
    public static void main(String[] args) throws Exception {
        for (String path : args) {
            File f = new File(path);
            String name = f.getName().replaceAll("\\.map26$", "");
            LiveMap m = GameMapIO.loadMap(name, f.getParentFile(), false);
            int n = m.getWidth() * m.getHeight();
            int walls = 0, dirt = 0, mines = 0, cheese = 0;
            for (int i = 0; i < n; i++) {
                if (m.getWallArray()[i]) walls++;
                if (m.getDirtArray()[i]) dirt++;
                if (m.getCheeseMineArray()[i]) mines++;
                cheese += m.getCheeseArray()[i];
            }
            StringBuilder bodies = new StringBuilder();
            StringBuilder bodiesNoTeam = new StringBuilder();
            int kingsA = 0, kingsB = 0;
            for (RobotInfo r : m.getInitialBodies()) {
                String common = r.ID + ":" + r.type + ":" + r.location.x + "," + r.location.y + ":" + r.direction + ":" + r.chirality + ";";
                bodies.append(r.team).append(':').append(common);
                bodiesNoTeam.append(common);
                if (r.type.name().equals("RAT_KING")) { if (r.team.name().equals("A")) kingsA++; else kingsB++; }
            }
            StringBuilder cats = new StringBuilder();
            ArrayList<Integer> ids = m.getCatWaypointIDs();
            for (int i = 0; i < ids.size(); i++) {
                cats.append(ids.get(i)).append(':');
                for (int w : m.getCatWaypoints().get(i)) cats.append(w).append(',');
                cats.append(';');
            }
            System.out.println("{\"file\":" + Play.quote(path) + ",\"name\":" + Play.quote(m.getMapName())
                    + ",\"width\":" + m.getWidth() + ",\"height\":" + m.getHeight()
                    + ",\"symmetry\":\"" + m.getSymmetry() + "\",\"seed\":" + m.getSeed()
                    + ",\"walls\":" + walls + ",\"dirt\":" + dirt + ",\"mines\":" + mines + ",\"cheese\":" + cheese
                    + ",\"cats\":" + m.getNumCats() + ",\"kings_a\":" + kingsA + ",\"kings_b\":" + kingsB
                    + ",\"bodies\":" + m.getInitialBodies().length
                    + ",\"h_walls\":\"" + h(m.getWallArray()) + "\",\"h_dirt\":\"" + h(m.getDirtArray())
                    + "\",\"h_mines\":\"" + h(m.getCheeseMineArray()) + "\",\"h_cheese\":\"" + h(m.getCheeseArray())
                    + "\",\"h_cats\":\"" + h(cats.toString()) + "\",\"h_bodies\":\"" + h(bodies.toString())
                    + "\",\"h_bodies_noteam\":\"" + h(bodiesNoTeam.toString()) + "\"," + layout(m) + "}");
        }
    }

    static String layout(LiveMap m) {
        int w = m.getWidth(), hgt = m.getHeight();
        StringBuilder sb = new StringBuilder("\"grid\":[");
        for (int y = 0; y < hgt; y++) {
            sb.append(y > 0 ? ",\"" : "\"");
            for (int x = 0; x < w; x++) {
                int i = x + y * w;
                sb.append(m.getWallArray()[i] ? '#' : m.getCheeseMineArray()[i] ? 'M' : m.getDirtArray()[i] ? 'd'
                        : m.getCheeseArray()[i] > 0 ? 'c' : '.');
            }
            sb.append('"');
        }
        sb.append("],\"body_list\":[");
        RobotInfo[] bs = m.getInitialBodies();
        for (int k = 0; k < bs.length; k++) {
            RobotInfo r = bs[k];
            sb.append(k > 0 ? "," : "").append("{\"id\":").append(r.ID).append(",\"team\":\"").append(r.team)
                    .append("\",\"type\":\"").append(r.type).append("\",\"x\":").append(r.location.x)
                    .append(",\"y\":").append(r.location.y).append(",\"chir\":").append(r.chirality).append('}');
        }
        sb.append("],\"cat_waypoints\":[");
        ArrayList<Integer> ids = m.getCatWaypointIDs();
        for (int k = 0; k < ids.size(); k++) {
            sb.append(k > 0 ? "," : "").append("{\"id\":").append(ids.get(k)).append(",\"wp\":[");
            int[] wp = m.getCatWaypoints().get(k);
            for (int j = 0; j < wp.length; j++) {
                sb.append(j > 0 ? "," : "").append('[').append(wp[j] % w).append(',').append(wp[j] / w).append(']');
            }
            sb.append("]}");
        }
        return sb.append(']').toString();
    }

    static String h(boolean[] a) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (boolean b : a) sb.append(b ? '1' : '0');
        return h(sb.toString());
    }

    static String h(int[] a) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int x : a) sb.append(x).append(',');
        return h(sb.toString());
    }

    static String h(String s) throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) sb.append(String.format("%02x", d[i]));
        return sb.toString();
    }
}
