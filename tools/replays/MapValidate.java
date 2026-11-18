import battlecode.world.*;
import java.io.File;
import java.nio.file.*;
public class MapValidate {
    public static void main(String[] a) throws Exception {
        int ok = 0, bad = 0;
        for (File f : new File(a[0]).listFiles()) {
            if (!f.getName().endsWith(".map26")) continue;
            try { LiveMap m = GameMapIO.Serial.deserialize(Files.readAllBytes(f.toPath()), false); m.assertIsValid(); ok++; }
            catch (Exception e) { bad++; System.out.println("INVALID " + f.getName() + ": " + e.getMessage()); }
        }
        System.out.println("valid=" + ok + " invalid=" + bad);
    }
}
